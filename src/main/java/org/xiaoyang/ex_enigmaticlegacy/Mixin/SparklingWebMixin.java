package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;

@Mixin(Entity.class)
public class SparklingWebMixin {
    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    public void ex_enigmaticlegacy$skipSparklingCobweb(BlockState state, Vec3 motionMultiplier, CallbackInfo callbackInfo) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!state.is(Blocks.COBWEB)) return;
        if (!livingEntity.hasEffect(ModEffects.SPARKLING_EFFECT.get())) return;

        callbackInfo.cancel();
    }
}
