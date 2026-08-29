package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import org.xiaoyang.ex_enigmaticlegacy.Client.particle.TrueDemonWeaponParticleEmitter;

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
        EXEMobLateRenderQueue.endFrame();
        EXECosmicBlockLateRenderQueue.endFrame();
        EXEEffectLateRenderQueue.endFrame();
        EXEGapingVoidLateRenderQueue.endFrame();
        EXEParticleLateRenderQueue.clearAll();
        TrueDemonWeaponParticleEmitter.clearCache();
        EXERenderFrameState.clear();
    }

    private EXERenderLifecycle() {}
}