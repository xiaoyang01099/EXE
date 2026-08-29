package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuo.endless.EndlessUtils;
import com.yuo.endless.client.AvaritiaShaders;
import com.yuo.endless.client.render.MobLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXEMobLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

@Mixin(value = MobLayer.class, remap = false)
public abstract class EXEMobLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    protected EXEMobLayerMixin() {
        super(null);
    }

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void exe$deferMobCosmicLayer(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (EXERenderFrameState.isShadowPass()) {
            ci.cancel();
            return;
        }

        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            ci.cancel();
            return;
        }

        exe$setupCosmicUniforms();
        M model = this.getParentModel();
        EXEMobLateRenderQueue.enqueue(model, poseStack, entity, packedLight, 1);
        ci.cancel();
    }

    private void exe$setupCosmicUniforms() {
        Minecraft minecraft = Minecraft.getInstance();

        if (AvaritiaShaders.cosmicShader == null
                || minecraft.player == null
                || minecraft.level == null) {
            return;
        }

        float yaw = (float) (minecraft.player.getYRot() * 2.0F * Math.PI / 360.0D);
        float pitch = -(float) (minecraft.player.getXRot() * 2.0F * Math.PI / 360.0D);
        AvaritiaShaders.cosmicTime.set((float) (System.currentTimeMillis() - (long) AvaritiaShaders.renderTime) / 2000.0F);
        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(1.0F);
        AvaritiaShaders.cosmicOpacity.set(1.0F);

        for (int i = 0; i < 10; ++i) {
            var sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(EndlessUtils.fa("shader/cosmic_" + i));
            AvaritiaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (AvaritiaShaders.cosmicUVs != null) {
            AvaritiaShaders.cosmicUVs.set(
                    AvaritiaShaders.COSMIC_UVS
            );
        }
    }
}