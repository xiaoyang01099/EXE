package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.*;

@Mixin(value = GameRenderer.class, priority = 500)
public abstract class EXEOculusAfterLevelMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void exe_beginWorldRender(float partialTick, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        EXERenderFrameState.Snapshot snap = EXERenderFrameState.beginFrame();
        SpecialLateRenderQueue.beginFrame(snap);
        CosmicBeamLateRenderQueue.beginFrame(snap);
        StarLineLateRenderQueue.beginFrame(snap);
        EXERainbowCosmicItemLateRenderQueue.beginFrame(snap);
        EXERainbowCosmicBlockLateRenderQueue.beginFrame(snap);
        GaiaGuardianLateRenderQueue.beginFrame(snap);
        EXECosmicItemLateRenderQueue.beginFrame(snap);
        EXECosmicBlockLateRenderQueue.beginFrame(snap);
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z",
                    ordinal = 0
            )
    )
    private void exe_renderLateWorldPasses(float partialTick, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        if (!EXERenderFrameState.current().shaderPackActive()) {
            return;
        }
        EXECosmicItemLateRenderQueue.renderAfterLevel();
        EXECosmicArmorLateRenderQueue.renderAfterLevel();
        GaiaGuardianLateRenderQueue.renderAfterLevel();
        SpecialLateRenderQueue.renderAfterLevel();
        CosmicBeamLateRenderQueue.renderAfterLevel(poseStack, partialTick);
        EXERainbowCosmicItemLateRenderQueue.renderAfterLevel();
        EXERainbowCosmicBlockLateRenderQueue.renderAfterLevel();
        StarLineLateRenderQueue.renderAfterLevel(poseStack);
        EXECosmicBlockLateRenderQueue.renderAfterLevel();
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void exe_endWorldRender(float partialTick, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        exe_finishAllFrames();
    }

    @Unique
    private static void exe_finishAllFrames() {
        try {
            SpecialLateRenderQueue.endFrame();
        } finally {
            try {
                CosmicBeamLateRenderQueue.endFrame();
            } finally {
                try {
                    StarLineLateRenderQueue.endFrame();
                } finally {
                    try {
                        EXECosmicItemLateRenderQueue.endFrame();
                    } finally {
                        try {
                            EXECosmicArmorLateRenderQueue.endFrame();
                        } finally {
                            try {
                                EXERainbowCosmicBlockLateRenderQueue.endFrame();
                            } finally {
                                try {
                                    EXERainbowCosmicItemLateRenderQueue.endFrame();
                                } finally {
                                    try {
                                        EXECosmicBlockLateRenderQueue.endFrame();
                                    } finally {
                                        try {
                                            GaiaGuardianLateRenderQueue.endFrame();
                                        } finally {
                                            EXERenderFrameState.endFrame();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}