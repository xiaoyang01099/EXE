package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectBeam;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectCut;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectManager;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectSlash;

public class FXHandler {
    public static int FX_BEAM = 0;
    public static int FX_SLASH = 0;
    public static int FX_CUT = 0;

    private static boolean initialized = false;

    @OnlyIn(Dist.CLIENT)
    public static void registerEffects() {
        if (initialized) {
            return;
        }
        initialized = true;


        FX_BEAM = FXRegistry.registerEffect((nbt) -> {
            EffectBeam beam = new EffectBeam();
            beam.read(nbt);
            EffectManager.addEffect(beam);
            return null;
        });

        FX_SLASH = FXRegistry.registerEffect((nbt) -> {
            EffectSlash slash = new EffectSlash();
            slash.read(nbt);
            EffectManager.addEffect(slash);
            return null;
        });

        FX_CUT = FXRegistry.registerEffect((nbt) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                EffectCut cut = new EffectCut(mc.level.dimension().location().hashCode());
                cut.read(nbt);
                EffectManager.addEffect(cut);
            }
            return null;
        });}
}
