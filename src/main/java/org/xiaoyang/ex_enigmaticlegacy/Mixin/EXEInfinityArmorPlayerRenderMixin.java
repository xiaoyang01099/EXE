package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuo.endless.client.model.InfinityArmorModel;
import com.yuo.endless.event.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXECosmicArmorLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

@Mixin(value = InfinityArmorModel.PlayerRender.class, remap = false)
public abstract class EXEInfinityArmorPlayerRenderMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exe_deferPlayerCosmicLayer(
            PoseStack pPoseStack,
            MultiBufferSource pBuffer,
            int pPackedLight,
            Player l,
            float pLimbSwing,
            float pLimbSwingAmount,
            float pPartialTick,
            float pAgeInTicks,
            float pNetHeadYaw,
            float pHeadPitch,
            CallbackInfo ci
    ) {
        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        if (EXERenderFrameState.isShadowPass()) {
            ci.cancel();
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player == l && mc.options.getCameraType().isFirstPerson()) {
            ci.cancel();
            return;
        }

        if (!EventHandler.isInfinite(l)) {
            ci.cancel();
            return;
        }

        PlayerModel<Player> playerModel = ((InfinityArmorModel.PlayerRender) (Object) this).getParentModel();
        EXECosmicArmorLateRenderQueue.enqueuePlayerLayer(pPoseStack, playerModel, pPackedLight);
        ci.cancel();
    }
}