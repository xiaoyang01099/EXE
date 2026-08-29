package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuo.endless.EndlessUtils;
import com.yuo.endless.client.AvaritiaShaders;
import com.yuo.endless.client.model.InfinityArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXECosmicArmorLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

import java.lang.reflect.Field;

@Mixin(value = InfinityArmorModel.class, remap = false)
public abstract class EXEInfinityArmorModelMixin extends HumanoidModel<Player> {
    @Shadow
    public static ResourceLocation MASK;
    @Shadow
    public static ResourceLocation MASK_INV;
    @Shadow
    public static ResourceLocation WING;
    @Unique
    private static boolean exe$modelRender;
    @Unique
    private static boolean exe$player;
    @Unique
    private static boolean exe$playerFlying;
    @Shadow
    public abstract Iterable<ModelPart> hatsOver();
    @Shadow
    public abstract Iterable<ModelPart> bodyParts();
    @Shadow
    public abstract Iterable<ModelPart> bodyPartsOver();
    @Unique
    @Nullable
    private LivingEntity exe$wearer;

    public EXEInfinityArmorModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "renderToBuffer(" + "Lcom/mojang/blaze3d/vertex/PoseStack;" + "Lcom/mojang/blaze3d/vertex/VertexConsumer;" + "IIFFFF" + ")V",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = 1
    )
    private void exe$deferCosmicArmor(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha,
            CallbackInfo ci
    ) {
        if (EXERenderFrameState.isShadowPass()) {
            super.renderToBuffer(
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            ci.cancel();
            return;
        }

        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        exe$captureArmorState();

        super.renderToBuffer(
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        exe$setupCosmicUniforms();

        float headScale;
        float bodyScale;
        float babyOffsetFactor;

        if (this.young) {
            headScale = 1.5F / this.babyHeadScale;
            bodyScale = 1.0F / this.babyBodyScale;
            babyOffsetFactor = 1.0F;
        } else {
            headScale = 1.0F;
            bodyScale = 0.9F;
            babyOffsetFactor = 0.0F;
        }

        poseStack.pushPose();

        try {
            poseStack.scale(
                    headScale,
                    headScale,
                    headScale
            );

            poseStack.translate(
                    0.0D,
                    this.babyYHeadOffset
                            / 16.0F
                            * babyOffsetFactor,
                    -0.03D
            );

            EXECosmicArmorLateRenderQueue.enqueuePart(
                    poseStack,
                    this.head,
                    MASK,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            if (exe$modelRender && !exe$player) {
                for (ModelPart part : this.hatsOver()) {
                    EXECosmicArmorLateRenderQueue.enqueuePart(
                            poseStack,
                            part,
                            MASK_INV,
                            packedLight,
                            packedOverlay,
                            red,
                            green,
                            blue,
                            alpha
                    );
                }
            }
        } finally {
            poseStack.popPose();
        }
        poseStack.pushPose();

        try {
            poseStack.scale(
                    bodyScale,
                    bodyScale,
                    bodyScale
            );

            poseStack.translate(
                    0.0D,
                    this.bodyYOffset
                            / 16.0F
                            * babyOffsetFactor,
                    0.0D
            );

            for (ModelPart part : this.bodyParts()) {
                EXECosmicArmorLateRenderQueue.enqueuePart(
                        poseStack,
                        part,
                        MASK,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha
                );

                EXECosmicArmorLateRenderQueue.enqueueEyePart(
                        poseStack,
                        part,
                        EXECosmicArmorLateRenderQueue.EyeType.BODY_GLOW,
                        packedLight,
                        packedOverlay
                );
            }

            if (exe$modelRender && !exe$player) {
                for (ModelPart part : this.bodyPartsOver()) {
                    EXECosmicArmorLateRenderQueue.enqueuePart(
                            poseStack,
                            part,
                            MASK_INV,
                            packedLight,
                            packedOverlay,
                            red,
                            green,
                            blue,
                            alpha
                    );
                }
            }
        } finally {
            poseStack.popPose();
        }

        poseStack.pushPose();

        try {
            poseStack.scale(
                    headScale,
                    headScale,
                    headScale
            );

            poseStack.translate(
                    0.0D,
                    this.babyYHeadOffset
                            / 16.0F
                            * babyOffsetFactor,
                    -0.03D
            );

            EXECosmicArmorLateRenderQueue.enqueuePart(
                    poseStack,
                    this.hat,
                    MASK,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            if (exe$modelRender) {
                EXECosmicArmorLateRenderQueue.enqueueEyePart(
                        poseStack,
                        this.hat,
                        EXECosmicArmorLateRenderQueue.EyeType.HAT_RAINBOW,
                        packedLight,
                        packedOverlay
                );
            }
        } finally {
            poseStack.popPose();
        }

        if (exe$playerFlying
                && !AvaritiaShaders.inventoryRender
                && this.exe$isChestArmorPass()
                && !this.exe$isFirstPersonLocalWearer()) {

            poseStack.pushPose();

            try {
                poseStack.scale(
                        bodyScale,
                        bodyScale,
                        bodyScale
                );

                poseStack.translate(
                        0.0D,
                        this.bodyYOffset
                                / 16.0F
                                * babyOffsetFactor,
                        0.0D
                );

                EXECosmicArmorLateRenderQueue.enqueueWing(
                        this.exe$wearer,
                        poseStack,
                        (InfinityArmorModel) (Object) this,
                        WING,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha
                );
            } finally {
                poseStack.popPose();
            }
        }

        ci.cancel();
    }

    @Inject(
            method =
                    "update(" +
                            "Lnet/minecraft/world/entity/LivingEntity;" +
                            ")V",
            at = @At("HEAD"),
            remap = false
    )
    private void exe$captureWearer(
            LivingEntity entity,
            CallbackInfo ci
    ) {
        this.exe$wearer = entity;
    }

    @Inject(
            method =
                    "renderToBufferWing(" +
                            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                            "Lcom/mojang/blaze3d/vertex/VertexConsumer;" +
                            "IIFFFF" +
                            ")V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void exe$deferImmediateWing(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha,
            CallbackInfo ci
    ) {
        if (EXERenderFrameState.isShadowPass()) {
            ci.cancel();
            return;
        }

        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        if (EXECosmicArmorLateRenderQueue.isRenderingQueuedWing()) {
            return;
        }
        ci.cancel();
    }

    @Unique
    private boolean exe$isChestArmorPass() {
        return !this.head.visible
                && this.body.visible
                && this.leftArm.visible
                && this.rightArm.visible
                && !this.leftLeg.visible
                && !this.rightLeg.visible;
    }

    @Unique
    private boolean exe$isFirstPersonLocalWearer() {
        Minecraft minecraft = Minecraft.getInstance();

        return this.exe$wearer != null
                && this.exe$wearer == minecraft.player
                && minecraft.options
                .getCameraType()
                .isFirstPerson();
    }

    @Unique
    private void exe$captureArmorState() {
        try {
            Field modelRenderField =
                    InfinityArmorModel.class.getDeclaredField(
                            "modelRender"
                    );

            modelRenderField.setAccessible(true);

            exe$modelRender =
                    modelRenderField.getBoolean(null);

            Field playerField =
                    InfinityArmorModel.class.getDeclaredField(
                            "player"
                    );

            playerField.setAccessible(true);

            exe$player =
                    playerField.getBoolean(null);

            Field playerFlyingField =
                    InfinityArmorModel.class.getDeclaredField(
                            "playerFlying"
                    );

            playerFlyingField.setAccessible(true);

            exe$playerFlying =
                    playerFlyingField.getBoolean(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            exe$modelRender = false;
            exe$player = false;
            exe$playerFlying = false;
        }
    }

    @Unique
    private void exe$setupCosmicUniforms() {
        Minecraft minecraft = Minecraft.getInstance();

        if (AvaritiaShaders.cosmicShader == null) {
            return;
        }

        if (AvaritiaShaders.cosmicOpacity != null) {
            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }

        if (AvaritiaShaders.inventoryRender) {
            if (AvaritiaShaders.cosmicExternalScale != null) {
                AvaritiaShaders.cosmicExternalScale.set(
                        100.0F
                );
            }
        } else {
            if (AvaritiaShaders.cosmicExternalScale != null) {
                AvaritiaShaders.cosmicExternalScale.set(
                        1.0F
                );
            }

            if (minecraft.player != null) {
                if (AvaritiaShaders.cosmicYaw != null) {
                    AvaritiaShaders.cosmicYaw.set(
                            (float) (
                                    minecraft.player.getYRot()
                                            * 2.0F
                                            * Math.PI
                                            / 360.0D
                            )
                    );
                }

                if (AvaritiaShaders.cosmicPitch != null) {
                    AvaritiaShaders.cosmicPitch.set(
                            -(float) (
                                    minecraft.player.getXRot()
                                            * 2.0F
                                            * Math.PI
                                            / 360.0D
                            )
                    );
                }
            }
        }

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite =
                    minecraft
                            .getTextureAtlas(
                                    InventoryMenu.BLOCK_ATLAS
                            )
                            .apply(
                                    EndlessUtils.fa(
                                            "shader/cosmic_" + i
                                    )
                            );

            int offset = i * 4;

            AvaritiaShaders.COSMIC_UVS[offset] =
                    sprite.getU0();

            AvaritiaShaders.COSMIC_UVS[offset + 1] =
                    sprite.getV0();

            AvaritiaShaders.COSMIC_UVS[offset + 2] =
                    sprite.getU1();

            AvaritiaShaders.COSMIC_UVS[offset + 3] =
                    sprite.getV1();
        }

        if (AvaritiaShaders.cosmicUVs != null) {
            AvaritiaShaders.cosmicUVs.set(
                    AvaritiaShaders.COSMIC_UVS
            );
        }
    }
}