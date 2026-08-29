package org.xiaoyang.ex_enigmaticlegacy.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ClientKeyChargeRegistry;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.TridentPlusGlowRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.TridentPlusGlowShader;

public class TridentPlusItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static ItemDisplayContext currentDisplayContext = ItemDisplayContext.NONE;
    private static final float TRIDENT_GLOW_CHARGE_TICKS = 20.0F;
    private static final float TRIDENT_GLOW_STRENGTH = 0.72F;
    private static final int FULL_BRIGHT_LIGHT = 15728880;
    private TridentModel model;

    public TridentPlusItemRenderer() {
        this(Minecraft.getInstance().getBlockEntityRenderDispatcher());
    }

    public TridentPlusItemRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher, Minecraft.getInstance().getEntityModels());
    }

    public static void prepareRenderContext(ItemDisplayContext displayContext) {
        currentDisplayContext = displayContext;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        TridentModel tridentModel = getModel();
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(bufferSource, tridentModel.renderType(TridentModel.TEXTURE), false, stack.hasFoil());
        tridentModel.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    public void renderChargeGlow(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay, TridentModel tridentModel) {
        if (!shouldRenderChargeGlow(stack, displayContext)) return;
        if (!TridentPlusGlowShader.isLoaded()) return;
        float chargeProgress = getChargeProgress(stack);
        boolean fullyCharged = chargeProgress >= 1.0F;
        Minecraft minecraft = Minecraft.getInstance();
        float time = minecraft.level == null ? 0.0F : (minecraft.level.getGameTime() + minecraft.getFrameTime()) / 20.0F;
        TridentPlusGlowShader.setGlowParams(time, chargeProgress, TRIDENT_GLOW_STRENGTH, fullyCharged ? 1.0F : 0.0F);
        VertexConsumer glowConsumer = bufferSource.getBuffer(TridentPlusGlowRenderType.getRenderType());
        tridentModel.renderToBuffer(poseStack, glowConsumer, FULL_BRIGHT_LIGHT, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean shouldRenderChargeGlow(ItemStack stack, ItemDisplayContext displayContext) {
        if (displayContext == ItemDisplayContext.GUI) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem() && player.getUseItem() == stack;
    }

    public float getChargeProgress(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isUsingItem() || player.getUseItem() != stack) return 0.0F;
        float usedTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
        return Mth.clamp(usedTicks / TRIDENT_GLOW_CHARGE_TICKS, 0.0F, 1.0F);
    }

    public TridentModel getModel() {
        if (this.model == null) {
            this.model = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
        }
        return this.model;
    }

    public void applyBowChargeTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                        float partialTick, float equipProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float progress = ClientKeyChargeRegistry.getProgress(player, partialTick);

        poseStack.translate(direction * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
        poseStack.translate(direction * -0.2785682F, 0.18344387F, 0.15731531F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-13.935F));
        poseStack.mulPose(Axis.YP.rotationDegrees(direction * 35.3F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction * -9.785F));
        poseStack.translate(0.0F, 0.0F, progress * 0.04F);
        poseStack.scale(1.0F, 1.0F, 1.0F + progress * 0.2F);
        poseStack.mulPose(Axis.YN.rotationDegrees(direction * 45.0F));
    }
}
