package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

public final class EXERenderLifecycle {

    public static void clearForWorldUnload() {
        SpecialLateRenderQueue.endFrame();
        CosmicBeamLateRenderQueue.endFrame();
        StarLineLateRenderQueue.endFrame();
        EXERainbowCosmicItemLateRenderQueue.endFrame();
        EXERainbowCosmicBlockLateRenderQueue.endFrame();
        GaiaGuardianLateRenderQueue.endFrame();
        EXECosmicItemLateRenderQueue.endFrame();
        EXECosmicArmorLateRenderQueue.endFrame();
        EXECosmicBlockLateRenderQueue.endFrame();
        EXERenderFrameState.clear();
    }

    private EXERenderLifecycle() {}
}