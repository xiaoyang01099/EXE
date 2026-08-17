package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuo.endless.client.AvaritiaShaders;
import com.yuo.endless.client.model.InfinityArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXECosmicArmorLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

@Mixin(value = InfinityArmorModel.class, remap = false)
public abstract class EXEInfinityArmorModelMixin extends HumanoidModel<Player> {
    @Shadow @Final
    private static ResourceLocation MASK;
    @Shadow @Final
    private static ResourceLocation MASK_INV;
    @Shadow @Final
    private static ResourceLocation WING;
    @Shadow
    public abstract Iterable<ModelPart> bodyParts();
    @Shadow
    public abstract Iterable<ModelPart> hatsOver();
    @Shadow
    public abstract Iterable<ModelPart> bodyPartsOver();

    @Shadow
    public abstract void renderToBufferWing(
            PoseStack pPoseStack,
            VertexConsumer pBuffer,
            int pPackedLight,
            int pPackedOverlay,
            float pRed,
            float pGreen,
            float pBlue,
            float pAlpha
    );

    @Unique
    private static boolean exe$modelRender;
    @Unique
    private static boolean exe$player;
    @Unique
    private static boolean exe$playerFlying;

    public EXEInfinityArmorModelMixin(ModelPart pRoot) {
        super(pRoot);
    }

    @Inject(
            method = "renderToBuffer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exe_deferCosmicArmor(
            PoseStack pPoseStack,
            VertexConsumer pBuffer,
            int pPackedLight,
            int pPackedOverlay,
            float pRed,
            float pGreen,
            float pBlue,
            float pAlpha,
            CallbackInfo ci
    ) {
        exe_captureArmorState();

        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);

        exe_setupCosmicUniforms();

        EXECosmicArmorLateRenderQueue.enqueuePart(
                pPoseStack, this.head, MASK,
                pPackedLight, pPackedOverlay,
                pRed, pGreen, pBlue, pAlpha
        );

        if (exe$modelRender && !exe$player) {
            for (ModelPart part : this.hatsOver()) {
                EXECosmicArmorLateRenderQueue.enqueuePart(
                        pPoseStack, part, MASK_INV,
                        pPackedLight, pPackedOverlay,
                        pRed, pGreen, pBlue, pAlpha
                );
            }
        }

        for (ModelPart part : this.bodyParts()) {
            EXECosmicArmorLateRenderQueue.enqueuePart(
                    pPoseStack, part, MASK,
                    pPackedLight, pPackedOverlay,
                    pRed, pGreen, pBlue, pAlpha
            );
        }

        if (exe$modelRender && !exe$player) {
            for (ModelPart part : this.bodyPartsOver()) {
                EXECosmicArmorLateRenderQueue.enqueuePart(
                        pPoseStack, part, MASK_INV,
                        pPackedLight, pPackedOverlay,
                        pRed, pGreen, pBlue, pAlpha
                );
            }
        }

        if (exe$playerFlying && !AvaritiaShaders.inventoryRender) {
            EXECosmicArmorLateRenderQueue.enqueueWing(
                    pPoseStack,
                    (InfinityArmorModel)(Object)this,
                    WING,
                    pPackedLight,
                    pPackedOverlay,
                    pRed, pGreen, pBlue, pAlpha
            );
        }

        ci.cancel();
    }

    @Unique
    private void exe_captureArmorState() {
        try {
            java.lang.reflect.Field modelRenderField = InfinityArmorModel.class
                    .getDeclaredField("modelRender");
            modelRenderField.setAccessible(true);
            exe$modelRender = modelRenderField.getBoolean(null);

            java.lang.reflect.Field playerField = InfinityArmorModel.class
                    .getDeclaredField("player");
            playerField.setAccessible(true);
            exe$player = playerField.getBoolean(null);

            java.lang.reflect.Field playerFlyingField = InfinityArmorModel.class
                    .getDeclaredField("playerFlying");
            playerFlyingField.setAccessible(true);
            exe$playerFlying = playerFlyingField.getBoolean(null);
        } catch (Exception e) {
            exe$modelRender = false;
            exe$player = false;
            exe$playerFlying = false;
        }
    }

    @Unique
    private void exe_setupCosmicUniforms() {
        Minecraft mc = Minecraft.getInstance();

        AvaritiaShaders.cosmicOpacity.set(1.0F);

        if (AvaritiaShaders.inventoryRender) {
            AvaritiaShaders.cosmicExternalScale.set(100.0F);
        } else {
            AvaritiaShaders.cosmicExternalScale.set(1.0F);
            AvaritiaShaders.cosmicYaw.set(
                    (float)((double)(mc.player.getYRot() * 2.0F) * Math.PI / 360.0)
            );
            AvaritiaShaders.cosmicPitch.set(
                    -((float)((double)(mc.player.getXRot() * 2.0F) * Math.PI / 360.0))
            );
        }

        for(int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(com.yuo.endless.EndlessUtils.fa("shader/cosmic_" + i));
            AvaritiaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (AvaritiaShaders.cosmicUVs != null) {
            AvaritiaShaders.cosmicUVs.set(AvaritiaShaders.COSMIC_UVS);
        }
    }
}