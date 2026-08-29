package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.Loader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.RawModel;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.FlySwordEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashDomainEntity;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordHeldItemRenderer;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.BloomRender;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineRender;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineStyle;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.PostRenderTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.PostRenderTaskRenderContext;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.PostRenderTaskSubmitter;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.GlStateSnapshot;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.fbos.MainFBORender;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleSystem;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.List;
import java.util.Map;

public class PostProcessing {
    private static final float[] POSITIONS = { -1, 1, -1, -1, 1, 1, 1, -1 };
    private static final float MAX_PARTICLE_DELTA_SECONDS = 0.05f;
    private final RawModel quad;
    private final FinalRender<Entity> finalRender;
    private final BloomRender bloomRender;
    private final MainFBORender mainFBO;
    private final FBO mcFBO;
    private final ParticleSystem particleSystem;
    private final GlStateSnapshot snapshot;
    private final DimensionSlashScreenEffect dimensionSlashScreenEffect;
    private final MiaoOutlineRender miaoOutlineRender;
    private final PostRenderContext postRenderContext;
    private final PostRenderTaskSubmitter taskSubmitter;
    private final ScreenDarkeningEffect screenDarkeningEffect;
    private float partialTick;
    private Camera camera;
    private Matrix4f viewMatrix;
    private int lastWidth;
    private int lastHeight;
    private float lastFrameClientTime = -1f;
    private boolean isRendering = false;

    public PostProcessing(Loader loader) {
        quad = loader.loadToVAO(POSITIONS, 2);
        finalRender = new FinalRender<>();
        bloomRender = new BloomRender();

        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        FrameBufferUtil.FboDepthSpec depthSpec = FrameBufferUtil.chooseCompatibleDepthSpec(Minecraft.getInstance().getMainRenderTarget());
        mainFBO = new MainFBORender(depthSpec.depthBufferType, depthSpec.depthInternalFormat);
        mcFBO = new FBO(width, height, depthSpec.depthBufferType, 1, depthSpec.depthInternalFormat);
        lastWidth = width;
        lastHeight = height;

        snapshot = new GlStateSnapshot();
        particleSystem = new ParticleSystem();
        dimensionSlashScreenEffect = new DimensionSlashScreenEffect();
        miaoOutlineRender = new MiaoOutlineRender();
        postRenderContext = new PostRenderContext();
        taskSubmitter = new PostRenderTaskSubmitter(this);
        screenDarkeningEffect = new ScreenDarkeningEffect(width, height);
    }

    public void onFramebufferResize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (width == lastWidth && height == lastHeight) return;

        lastWidth = width;
        lastHeight = height;
        FrameBufferUtil.FboDepthSpec depthSpec = FrameBufferUtil.chooseCompatibleDepthSpec(Minecraft.getInstance().getMainRenderTarget());
        mcFBO.resize(width, height, depthSpec.depthBufferType, depthSpec.depthInternalFormat);
        mainFBO.resize(width, height, depthSpec.depthBufferType, depthSpec.depthInternalFormat);
        screenDarkeningEffect.resize(width, height);
        bloomRender.resize(width, height);
    }

    public void syncDepthSpec(RenderTarget renderTarget) {
        FrameBufferUtil.FboDepthSpec depthSpec = FrameBufferUtil.chooseCompatibleDepthSpec(renderTarget);
        if (depthSpec.matches(mcFBO) && depthSpec.matches(mainFBO.getFbo())) return;
        mcFBO.resize(lastWidth, lastHeight, depthSpec.depthBufferType, depthSpec.depthInternalFormat);
        mainFBO.resize(lastWidth, lastHeight, depthSpec.depthBufferType, depthSpec.depthInternalFormat);
    }

    public void doPostProcessing() {
        if (!shouldRender()) return;
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        syncDepthSpec(renderTarget);
        snapshot.save();
        postRenderContext.beginFrame(snapshot.prevVao);

        FrameBufferUtil.copyFBO(renderTarget, mcFBO);
        int sceneTexture = screenDarkeningEffect.renderIfNeeded(mcFBO, quad);
        buildBuffer(renderTarget);

        int bloomTexture = bloomRender.render(mainFBO.getFbo().getColourTexture(1), quad, renderTarget.frameBufferId);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        start();
        finalRender.render(sceneTexture, mainFBO.getFbo().getColourTexture(), bloomTexture, dimensionSlashScreenEffect);
        end();
        dimensionSlashScreenEffect.clearFrame();

        snapshot.restore();
        isRendering = false;

    }

    public boolean shouldRender() {
        return isRendering || particleSystem.hasActiveParticles() || finalRender.hasActiveEffects() || screenDarkeningEffect.hasActive();
    }

    private void start() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);
    }

    private void end() {
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL11.glDepthMask(true);
        snapshot.setToMCVao();
    }

    public void add(Entity entity, PoseStack pose) {
        finalRender.addBloomQueue(entity, pose, RenderSystem.getModelViewMatrix());
        isRendering = true;
    }

    public void requestFlySwordTrail(FlySwordEntity entity) {
        finalRender.requestFlySwordTrail(entity);
    }

    public void flushFlySwordTrailPose(PoseStack pose) {
        if (!finalRender.hasPendingFlySwordTrails()) return;
        finalRender.flushFlySwordTrails(new Matrix4f(pose.last().pose()));
        isRendering = true;
    }

    public void addParticle(ParticleEmitTask task) {
        Exe.submitAkatTask(() -> particleSystem.emit(task));
        isRendering = true;
    }

    public void addScreenDarkening(float strength, int lifeTicks, int fadeInTicks, int fadeOutTicks) {
        screenDarkeningEffect.add(strength, lifeTicks, fadeInTicks, fadeOutTicks);
        isRendering = true;
    }

    public void addBloomTask(Entity entity, PoseStack pose){
        finalRender.addBloomQueue(entity, pose, RenderSystem.getModelViewMatrix());
        isRendering = true;
    }

    public void addDimensionSlashField(DimensionSlashDomainEntity entity, float partialTick) {
        camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        dimensionSlashScreenEffect.add(entity, camera, partialTick);
        isRendering = true;
    }

    public void addMiaoOutline(Entity entity, MiaoOutlineStyle style) {
        finalRender.addMiaoOutline(entity, style);
        isRendering = true;
    }

    public void submit(PostRenderTask task) {
        if (task == null) return;
        finalRender.submit(task);
        isRendering = true;
    }

    public PostRenderTaskSubmitter effects() {
        return taskSubmitter;
    }

    public void submitFlySwordHeldModel(BakedModel model, Matrix4f modelViewMatrix,
                                        boolean plusSword, long gameTime, FlySwordHeldItemRenderer.FlySwordFlowParams flowParams) {
        finalRender.submitFlySwordHeldModel(model, modelViewMatrix, plusSword, gameTime, flowParams);
        isRendering = true;
    }

    public void buildBuffer(RenderTarget renderTarget) {
        if (camera == null) {
            camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        }
        viewMatrix = MathUtil.createViewMatrix(camera);

        FrameBufferUtil.copyFBODepth(renderTarget, mainFBO.getFbo());
        postRenderContext.bindFrameBuffer(mainFBO.getFbo(), false, 0, 1);

        float frameDelta = getParticleFrameDeltaSeconds() * 3;
        PostRenderTaskRenderContext taskContext = new PostRenderTaskRenderContext(postRenderContext, finalRender.fboBuffer,
                camera, partialTick, viewMatrix, frameDelta, mcFBO.getColourTexture(), mcFBO.getDepthTexture(),
                mcFBO.getWidth(), mcFBO.getHeight());
        boolean hasDepthBloomQueues = hasPhaseQueues(PostRenderPhase.DEPTH_TESTED_WORLD);
        boolean hasParticles = particleSystem.hasActiveParticles();
        boolean hasAlwaysVisibleQueues = hasPhaseQueues(PostRenderPhase.ALWAYS_VISIBLE_WORLD);
        boolean hasDepthWorldPhase = hasDepthBloomQueues || hasParticles;
        if (hasDepthWorldPhase) {
            postRenderContext.setDrawBuffers(mainFBO.getFbo(), 0, 1);
            postRenderContext.setDepthState(true, false, GL11.GL_LEQUAL);

            if (hasDepthBloomQueues) {
                finalRender.renderTaskQueuesByPhase(PostRenderPhase.DEPTH_TESTED_WORLD, taskContext);
                postRenderContext.prepareRenderTypePhase(true, false, GL11.GL_LEQUAL);
                finalRender.renderBloomQueuesByPhase(PostRenderPhase.DEPTH_TESTED_WORLD, camera, partialTick, viewMatrix, frameDelta);
            }

            if (hasParticles) {
                postRenderContext.setDepthState(true, false, GL11.GL_LEQUAL);
                postRenderContext.setDrawBuffers(mainFBO.getFbo(), 0, 1);
                particleSystem.updateAndRender(frameDelta, RenderSystem.getProjectionMatrix(), camera);
            }
        }

        if (hasAlwaysVisibleQueues) {
            postRenderContext.prepareRenderTypePhase(false, false, GL11.GL_ALWAYS);
            BufferUploader.reset();
            postRenderContext.setDrawBuffers(mainFBO.getFbo(), 0, 1);
            finalRender.renderTaskQueuesByPhase(PostRenderPhase.ALWAYS_VISIBLE_WORLD, taskContext);
            postRenderContext.prepareRenderTypePhase(false, false, GL11.GL_ALWAYS);
            finalRender.renderBloomQueuesByPhase(PostRenderPhase.ALWAYS_VISIBLE_WORLD, camera, partialTick, viewMatrix, frameDelta);
        }

        if (finalRender.hasMiaoOutlineTasks()) {
            postRenderContext.prepareRenderTypePhase(false, false, GL11.GL_ALWAYS);
            BufferUploader.reset();
            for (Map.Entry<MiaoOutlineStyle.Kind, List<MiaoOutlineTask>> entry : finalRender.getMiaoOutlineQueue().groupTasksByKind().entrySet()) {
                MiaoOutlineStyle style = MiaoOutlineStyle.create(entry.getKey());
                postRenderContext.clearColorAttachment(mainFBO.getFbo(), 2, 0f, 0f, 0f, 0f);
                finalRender.getMiaoOutlineQueue().renderDepthMask(finalRender.fboBuffer, entry.getValue(), camera, partialTick, viewMatrix, style);
                miaoOutlineRender.render(mainFBO.getFbo(), quad, style, partialTick);
            }
            finalRender.clearMiaoOutlineTasks();
        }
        postRenderContext.prepareRenderTypePhase(true, false, GL11.GL_LEQUAL);
        postRenderContext.bindMinecraftFrameBuffer(mainFBO.getFbo(), renderTarget);
    }

    public boolean hasPhaseQueues(PostRenderPhase phase) {
        return finalRender.hasTaskQueuesByPhase(phase) || finalRender.hasBloomQueuesByPhase(phase);
    }

    public float getParticleFrameDeltaSeconds() {
        float now = MathUtil.getClientTime(partialTick);
        if (lastFrameClientTime < 0f) {
            lastFrameClientTime = now;
            return 0f;
        }

        float dt = now - lastFrameClientTime;
        lastFrameClientTime = now;
        return Math.min(Math.max(dt, 0f), MAX_PARTICLE_DELTA_SECONDS);
    }

    public void setPartialTick(float partialTick, Camera camera) {
        this.partialTick = partialTick;
        this.camera = camera;
    }

    public void cleanUp() {
        finalRender.cleanUp();
        bloomRender.cleanUp();
        particleSystem.cleanUp();
        miaoOutlineRender.cleanUp();
        screenDarkeningEffect.cleanUp();
    }

    public FinalRender<Entity> getFinalRender() {
        return finalRender;
    }
}
