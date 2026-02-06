package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FXRegistry {
    public static Map<Integer, Function<CompoundTag, Void>> effects = new HashMap<>();
    private static int id = 0;

    public static int registerEffect(@Nonnull Function<CompoundTag, Void> func) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            effects.put(id, func);
        }
        return id++;
    }
}
