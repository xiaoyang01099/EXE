package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.FunctionalFlower.SnowFlower;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@Mixin(Biome.class)
public class BiomeMixin {

//    @Inject(
//            method = "getPrecipitation",
//            at = @At("RETURN"),
//            cancellable = true
//    )
    private void onGetPrecipitation(CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (!SnowFlower.SNOW_FLOWER_POSITIONS.isEmpty() &&
                cir.getReturnValue() == Biome.Precipitation.RAIN) {
            cir.setReturnValue(Biome.Precipitation.SNOW);
        }
    }

//    @Inject(
//            method = "getTemperature",
//            at = @At("RETURN"),
//            cancellable = true
//    )
    private void onGetTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!SnowFlower.SNOW_FLOWER_POSITIONS.isEmpty() &&
                cir.getReturnValue() > 0.15F) {
            cir.setReturnValue(0.0F);
        }
    }
}