package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.FlySwordEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicBowParticleEffectEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ColorfulEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.RailgunBeamEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.BattoSlashEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashStrikeEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.SwordAuraEntity;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordHeldItemRenderer;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.*;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineStyle;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueueRegistration;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.FlySwordQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.QueueAddAction;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.FinalShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.*;

import java.util.*;
import java.util.function.Predicate;

public class FinalRender<T extends Entity> {
    private FinalShader shader;
    public final Map<Class<? extends Entity>, EntityQueueRegistration<? extends Entity>> queueRegistrations;
    public final Map<PostRenderPhase, List<EntityQueue<? extends Entity>>> queuesByPhase;
    public final Map<PostRenderPhase, List<EntityQueue<? extends Entity>>> activeQueuesByPhase;
    public final Map<PostRenderQueueType, PostRenderTaskQueue<? extends PostRenderTask>> taskQueueRegistrations;
    public final Map<PostRenderPhase, List<PostRenderTaskQueue<? extends PostRenderTask>>> taskQueuesByPhase;
    public final Map<PostRenderPhase, List<PostRenderTaskQueue<? extends PostRenderTask>>> activeTaskQueuesByPhase;
    public final Set<PostRenderTaskQueue<? extends PostRenderTask>> activeTaskQueueSet;
    public final MultiBufferSource.BufferSource fboBuffer;
    public final FlySwordQueue flySwordQueue;
    public final List<FlySwordEntity> pendingFlySwordTrails;
    public final CoinLightningQueue lightningQueue;
    public final ShockwaveQueue shockwaveQueue;
    public final CircleShockwaveQueue circleShockwaveQueue;
    public final SmokeParticleQueue smokeParticleQueue;
    public final FlySwordHeldModelQueue flySwordHeldModelQueue;
    public final GoldenSpiralEffectQueue goldenSpiralEffectQueue;
    public final ExcaliburSpiralQueue excaliburSpiralQueue;
    public final MiaoOutlineQueue miaoOutlineQueue;

    public FinalRender() {
        shader = new FinalShader();
        shader.start();
        shader.loadUniforms();
        shader.stop();

        queueRegistrations = new HashMap<>();
        queuesByPhase = new EnumMap<>(PostRenderPhase.class);
        activeQueuesByPhase = new EnumMap<>(PostRenderPhase.class);
        taskQueueRegistrations = new EnumMap<>(PostRenderQueueType.class);
        taskQueuesByPhase = new EnumMap<>(PostRenderPhase.class);
        activeTaskQueuesByPhase = new EnumMap<>(PostRenderPhase.class);
        activeTaskQueueSet = Collections.newSetFromMap(new IdentityHashMap<>());
        fboBuffer = MultiBufferSource.immediate(new BufferBuilder(8192));
        flySwordQueue = new FlySwordQueue();
        pendingFlySwordTrails = new ArrayList<>();
        queuesByPhase.computeIfAbsent(flySwordQueue.getPhase(), phase -> new ArrayList<>()).add(flySwordQueue);

        registerQueue(RailgunBeamEntity.class, new RailgunBeamQueue());
        registerQueue(ColorfulEntity.class, new ColorfulCoinQueue());
        registerQueue(MagicBowParticleEffectEntity.class, new StarJudgementCircleQueue(), MagicBowParticleEffectEntity::isStarJudgementVisual);
        registerQueue(SwordAuraEntity.class, new SwordAuraQueue());
        registerQueue(DimensionSlashStrikeEntity.class, new DimensionSlashStrikeQueue());
        registerQueue(BattoSlashEntity.class, new BattoSlashQueue());

        lightningQueue = new CoinLightningQueue();
        shockwaveQueue = new ShockwaveQueue();
        circleShockwaveQueue = new CircleShockwaveQueue();
        smokeParticleQueue = new SmokeParticleQueue();
        flySwordHeldModelQueue = new FlySwordHeldModelQueue();
        goldenSpiralEffectQueue = new GoldenSpiralEffectQueue();
        excaliburSpiralQueue = new ExcaliburSpiralQueue();
        miaoOutlineQueue = new MiaoOutlineQueue();

        registerTaskQueue(new LightningPostQueue(lightningQueue));
        registerTaskQueue(new ShockwavePostQueue(shockwaveQueue));
        registerTaskQueue(new CircleShockwavePostQueue(circleShockwaveQueue));
        registerTaskQueue(new SmokeParticlePostQueue(smokeParticleQueue));
        registerTaskQueue(new FlySwordHeldModelPostQueue(flySwordHeldModelQueue));
        registerTaskQueue(new GoldenSpiralEffectPostQueue(goldenSpiralEffectQueue));
        registerTaskQueue(new ExcaliburSpiralPostQueue(excaliburSpiralQueue));
    }

    public void render(int mcTexture, int mainTexture, int bloomTexture, DimensionSlashScreenEffect screenEffect) {
        shader.start();
        shader.loadDimensionSlashEffect(screenEffect);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL13.glBindTexture(GL13.GL_TEXTURE_2D, mcTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL13.glBindTexture(GL13.GL_TEXTURE_2D, mainTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL13.glBindTexture(GL13.GL_TEXTURE_2D, bloomTexture);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        shader.stop();
    }

    public <E extends Entity> void registerQueue(Class<E> entityClass, EntityQueue<E> queue) {
        registerQueue(entityClass, queue, entity -> true);
    }

    public <E extends Entity> void registerQueue(Class<E> entityClass, EntityQueue<E> queue, Predicate<E> filter) {
        registerQueue(entityClass, queue, filter, (targetQueue, entity, pose, modelViewMatrix) -> targetQueue.add(entity));
    }

    public <E extends Entity> void registerQueue(Class<E> entityClass, EntityQueue<E> queue, Predicate<E> filter, QueueAddAction<E> addAction) {
        EntityQueueRegistration<E> registration = new EntityQueueRegistration<>(entityClass, queue, filter, addAction);
        queueRegistrations.put(entityClass, registration);
        queuesByPhase.computeIfAbsent(queue.getPhase(), phase -> new ArrayList<>()).add(queue);
    }

    public <Q extends PostRenderTask> void registerTaskQueue(PostRenderTaskQueue<Q> queue) {
        if (queue == null) return;
        taskQueueRegistrations.put(queue.queueType(), queue);
        taskQueuesByPhase.computeIfAbsent(queue.phase(), phase -> new ArrayList<>()).add(queue);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void submit(PostRenderTask task) {
        if (task == null) return;
        PostRenderTaskQueue queue = taskQueueRegistrations.get(task.queueType());
        if (queue == null) return;
        queue.add(task);
        markTaskQueueActive(queue);
    }

    // 将无实体任务队列加入当前活跃队列索引，后续 phase 判断不再扫描全部注册队列。
    public void markTaskQueueActive(PostRenderTaskQueue<? extends PostRenderTask> queue) {
        if (queue == null) return;
        if (!activeTaskQueueSet.add(queue)) return;
        activeTaskQueuesByPhase.computeIfAbsent(queue.phase(), phase -> new ArrayList<>()).add(queue);
    }

    // 按实体运行时 class 查找队列注册项，当前注册实体均为具体类。
    public EntityQueueRegistration<? extends Entity> findRegistration(Entity entity) {
        return queueRegistrations.get(entity.getClass());
    }

    // 统一提交实体 bloom/phase 队列，飞剑拖尾和普通 bloom 都走这个入口。
    public void addBloomQueue(T entity, PoseStack pose, Matrix4f modelViewMatrix) {
        if (entity == null) return;
        EntityQueueRegistration<? extends Entity> registration = findRegistration(entity);
        if (registration == null) return;
        if (!registration.tryAdd(entity, pose, modelViewMatrix)) return;
        markActive(registration.queue);
    }

    // 记录本帧飞剑 renderer 已经渲染到的移动飞剑，等待 AFTER_ENTITIES 阶段提供正确矩阵。
    public void requestFlySwordTrail(FlySwordEntity entity) {
        if (entity == null) return;
        pendingFlySwordTrails.add(entity);
    }

    // 判断本帧是否存在等待 AFTER_ENTITIES 矩阵的飞剑拖尾。
    public boolean hasPendingFlySwordTrails() {
        return !pendingFlySwordTrails.isEmpty();
    }

    // 使用 AFTER_ENTITIES 阶段矩阵把本帧飞剑拖尾正式加入渲染队列。
    public void flushFlySwordTrails(Matrix4f modelMatrix) {
        if (pendingFlySwordTrails.isEmpty()) return;
        flySwordQueue.updateModelMatrix(modelMatrix);
        for (FlySwordEntity entity : pendingFlySwordTrails) {
            flySwordQueue.add(entity);
        }
        pendingFlySwordTrails.clear();
        markActive(flySwordQueue);
    }

    // 将队列标记为本帧 active，避免同一帧同一队列重复进入 active list。
    public void markActive(EntityQueue<? extends Entity> queue) {
        if (queue.activeInFrame) return;
        activeQueuesByPhase.computeIfAbsent(queue.getPhase(), phase -> new ArrayList<>()).add(queue);
        queue.activeInFrame = true;
    }

    // 缓存 FlySwordHeldItemRenderer 提交的真实手持飞剑模型视图矩阵。
    public void submitFlySwordHeldModel(BakedModel model, Matrix4f modelViewMatrix,
                                        boolean plusSword, long gameTime, FlySwordHeldItemRenderer.FlySwordFlowParams flowParams) {
        submit(new FlySwordHeldModelTask(model, modelViewMatrix, plusSword, gameTime, flowParams));
    }

    // 提交实体 Miao 描边任务，mask 和 bloom 由 PostProcessing 统一调度。
    public void addMiaoOutline(Entity entity, MiaoOutlineStyle style) {
        miaoOutlineQueue.add(entity, style == null ? MiaoOutlineStyle.AUTO_TRACKING_RED : style);
    }

    // 返回 Miao 描边队列，PostProcessing 用它完成 CA2 深度 mask 和径向后处理。
    public MiaoOutlineQueue getMiaoOutlineQueue() {
        return miaoOutlineQueue;
    }

    // 判断当前帧是否存在 Miao 描边任务。
    public boolean hasMiaoOutlineTasks() {
        return miaoOutlineQueue.hasTasks();
    }

    // 清空当前帧 Miao 描边任务。
    public void clearMiaoOutlineTasks() {
        miaoOutlineQueue.clear();
    }

    // 判断指定 phase 是否存在实体队列内容。
    public boolean hasBloomQueuesByPhase(PostRenderPhase phase) {
        List<EntityQueue<? extends Entity>> activeQueues = activeQueuesByPhase.get(phase);
        return activeQueues != null && !activeQueues.isEmpty();
    }

    // 判断指定 phase 是否存在无实体任务队列内容。
    public boolean hasTaskQueuesByPhase(PostRenderPhase phase) {
        List<PostRenderTaskQueue<? extends PostRenderTask>> activeQueues = activeTaskQueuesByPhase.get(phase);
        return activeQueues != null && !activeQueues.isEmpty();
    }

    // 按后处理阶段渲染无实体任务队列，阶段状态在每个队列前恢复一次，隔离自管 GL 状态。
    public void renderTaskQueuesByPhase(PostRenderPhase phase, PostRenderTaskRenderContext context) {
        List<PostRenderTaskQueue<? extends PostRenderTask>> activeQueues = activeTaskQueuesByPhase.get(phase);
        if (activeQueues == null || activeQueues.isEmpty()) return;
        int stableSize = activeQueues.size();
        for (int i = 0; i < stableSize; i++) {
            PostRenderTaskQueue<? extends PostRenderTask> queue = activeQueues.get(i);
            if (context != null) context.prepareRenderTypePhase(phase);
            queue.render(context);
        }
        compactActiveTaskQueues(phase);
    }

    // 渲染后压缩 active list，保留生命周期未结束的跨帧无实体队列。
    public void compactActiveTaskQueues(PostRenderPhase phase) {
        List<PostRenderTaskQueue<? extends PostRenderTask>> activeQueues = activeTaskQueuesByPhase.get(phase);
        if (activeQueues == null || activeQueues.isEmpty()) return;
        int write = 0;
        int originalSize = activeQueues.size();
        for (int read = 0; read < originalSize; read++) {
            PostRenderTaskQueue<? extends PostRenderTask> queue = activeQueues.get(read);
            if (queue.hasActive()) {
                activeQueues.set(write, queue);
                write++;
                continue;
            }
            activeTaskQueueSet.remove(queue);
        }
        while (activeQueues.size() > write) {
            activeQueues.remove(activeQueues.size() - 1);
        }
    }

    // 按后处理阶段渲染本帧 active 的 bloom 队列，让 PostProcessing 可以在阶段边界统一设置 GL 状态。
    public void renderBloomQueuesByPhase(PostRenderPhase phase, Camera camera, float partialTick, Matrix4f viewMatrix, float frameDeltaSeconds) {
        List<EntityQueue<? extends Entity>> activeQueues = activeQueuesByPhase.get(phase);
        if (activeQueues == null || activeQueues.isEmpty()) return;

        for (EntityQueue<? extends Entity> bloomQueue : activeQueues) {
            if (!bloomQueue.entities.isEmpty()) {
                bloomQueue.render(fboBuffer, camera, partialTick, viewMatrix, frameDeltaSeconds);
                bloomQueue.entities.clear();
            }
            bloomQueue.activeInFrame = false;
        }
        activeQueues.clear();
    }

    // 判断最终渲染器是否仍有跨帧播放的效果。
    public boolean hasActiveEffects() {
        return hasTaskQueuesByPhase(PostRenderPhase.DEPTH_TESTED_WORLD)
                || hasTaskQueuesByPhase(PostRenderPhase.ALWAYS_VISIBLE_WORLD)
                || hasBloomQueuesByPhase(PostRenderPhase.DEPTH_TESTED_WORLD)
                || hasBloomQueuesByPhase(PostRenderPhase.ALWAYS_VISIBLE_WORLD)
                || miaoOutlineQueue.hasTasks();
    }

    // 释放最终合成 shader，并清空实体队列、无实体 task 队列和特殊 GPU 资源。
    public void cleanUp() {
        for (PostRenderTaskQueue<? extends PostRenderTask> queue : taskQueueRegistrations.values()) {
            queue.clear();
        }
        smokeParticleQueue.cleanUp();
        miaoOutlineQueue.clear();
        pendingFlySwordTrails.clear();
        for (List<EntityQueue<? extends Entity>> queues : queuesByPhase.values()) {
            for (EntityQueue<? extends Entity> bloomQueue : queues) {
                if (bloomQueue instanceof SwordAuraQueue swordAuraQueue) {
                    swordAuraQueue.cleanUp();
                }
                bloomQueue.entities.clear();
                bloomQueue.activeInFrame = false;
            }
        }
        activeQueuesByPhase.clear();
        activeTaskQueuesByPhase.clear();
        activeTaskQueueSet.clear();
        shader.cleanUp();
    }
}
