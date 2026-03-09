package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Util.CosmicParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(TerrainParticle.class)
public abstract class TerrainParticleGetTypeMixin extends TextureSheetParticle {

    @Unique
    private boolean useCosmic;

    protected TerrainParticleGetTypeMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void captureStateA(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, BlockState state, CallbackInfo ci) {
        this.useCosmic = state != null && state.is(ModBlockss.ASTRAL_BLOCK.get());
    }

    @Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void captureStateB(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, BlockState state, BlockPos pos, CallbackInfo ci) {
        this.useCosmic = state != null && state.is(ModBlockss.ASTRAL_BLOCK.get());
    }

    @Inject(method = "getRenderType()Lnet/minecraft/client/particle/ParticleRenderType;", at = @At("HEAD"), cancellable = true)
    private void getRenderType(CallbackInfoReturnable<ParticleRenderType> cir) {
        if (this.useCosmic) {
            cir.setReturnValue(CosmicParticleType.INSTANCE);
        }
    }
}