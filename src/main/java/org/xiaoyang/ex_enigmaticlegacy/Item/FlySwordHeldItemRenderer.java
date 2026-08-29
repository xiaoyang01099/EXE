package org.xiaoyang.ex_enigmaticlegacy.Item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Map;
import java.util.WeakHashMap;

// FlySwordHeldItemRenderer 在玩家手持或展示框飞剑时只提交矩阵给后处理队列，不直接绘制原版不透明模型。
public class FlySwordHeldItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static BakedModel currentHandModel; // 当前手持 3D baked model，由 FlySwordBakedModel.applyTransform 写入。
    private static final float THIRD_PERSON_SCALE = 1.3F; // 第三人称手持飞剑额外缩放倍率。
    private static final Map<ItemStack, FlySwordFlowParams> FLOW_PARAMS_BY_STACK = new WeakHashMap<>(); // 按物品栈缓存的稳定双噪声流动参数。

    public FlySwordHeldItemRenderer() {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher());
    }

    public FlySwordHeldItemRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher, Minecraft.getInstance().getEntityModels());
    }

    // 记录当前飞剑 3D 模型，供 BEWLR 阶段提交后处理重放。
    public static void prepareRenderContext(BakedModel handModel) {
        currentHandModel = handModel;
    }

    // 判断当前上下文是否只交给后处理飞剑队列渲染。
    public static boolean shouldUseHeldPostRenderer(ItemDisplayContext displayContext) {
        return displayContext != ItemDisplayContext.GUI;
    }

    // 读取同一个物品栈固定不变的两组噪声速度与起始相位。
    public static FlySwordFlowParams getFlowParams(ItemStack stack) {
        return FLOW_PARAMS_BY_STACK.computeIfAbsent(stack, FlySwordFlowParams::new);
    }

    // 清空物品栈流动参数缓存，供客户端世界退出或资源重载后调用。
    public static void clearFlowParams() {
        FLOW_PARAMS_BY_STACK.clear();
    }

    // 手持或展示框飞剑渲染阶段只提交当前模型视图矩阵，实际透明模型在后处理 bloom 队列中统一重放。
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!shouldUseHeldPostRenderer(displayContext)) return;
        if (Exe.POST == null || currentHandModel == null) return;

        // 原版第三人称变换已完成，在捕获矩阵前额外放大手持飞剑。
//        if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
//                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
//            poseStack.scale(THIRD_PERSON_SCALE, THIRD_PERSON_SCALE, THIRD_PERSON_SCALE);
//        }

        // 这里保存真实手持模型矩阵，避免后处理阶段重新推算第一/第三人称位置。
        Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
        long gameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        boolean plusSword = stack.getItem() instanceof FlySwordPlusItem;
        Exe.POST.submitFlySwordHeldModel(currentHandModel, modelViewMatrix, plusSword, gameTime,
                getFlowParams(stack));
    }

    // FlySwordFlowParams 保存一个物品栈的两张噪声流动参数。
    public static class FlySwordFlowParams {
        public final float noise1SpeedX; // 第一张噪声 X 方向速度。
        public final float noise1SpeedY; // 第一张噪声 Y 方向速度。
        public final float noise1PhaseX; // 第一张噪声 X 方向起始相位。
        public final float noise1PhaseY; // 第一张噪声 Y 方向起始相位。
        public final float noise2SpeedX; // 第二张噪声 X 方向速度。
        public final float noise2SpeedY; // 第二张噪声 Y 方向速度。
        public final float noise2PhaseX; // 第二张噪声 X 方向起始相位。
        public final float noise2PhaseY; // 第二张噪声 Y 方向起始相位。

        // 按物品栈身份哈希生成会话内稳定的随机参数。
        public FlySwordFlowParams(ItemStack stack) {
            long seed = Integer.toUnsignedLong(System.identityHashCode(stack)) * 0x9E3779B97F4A7C15L;
            java.util.Random random = new java.util.Random(seed);
            noise1SpeedX = lerp(0.06F, 0.14F, random.nextFloat());
            noise1SpeedY = lerp(0.12F, 0.36F, random.nextFloat());
            noise1PhaseX = random.nextFloat();
            noise1PhaseY = random.nextFloat();
            noise2SpeedX = lerp(0.05F, 0.16F, random.nextFloat());
            noise2SpeedY = lerp(0.18F, 0.32F, random.nextFloat());
            noise2PhaseX = random.nextFloat();
            noise2PhaseY = random.nextFloat();
        }

        // 在线性区间内生成随机浮点数。
        public static float lerp(float min, float max, float amount) {
            return min + (max - min) * amount;
        }
    }
}
