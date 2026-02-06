package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionData;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.block.tile.mana.TilePool;

import java.util.Comparator;

@Mixin(value = TilePool.class, remap = false)
public abstract class TilePoolMixin {
    @Shadow
    public int manaCap;
    @Shadow
    private int mana;

    @ModifyVariable(
            method = "serverTick",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            name = "transfRate",
            remap = false
    )
    private static int applyCurseTransferBonus(
            int transfRate,
            Level level,
            BlockPos worldPosition,
            BlockState state,
            TilePool self) {

        AABB bounds = new AABB(
                worldPosition.getX() - 8,
                worldPosition.getY() - 8,
                worldPosition.getZ() - 8,
                worldPosition.getX() + 9,
                worldPosition.getY() + 9,
                worldPosition.getZ() + 9
        );

        Player nearestCursed = level.getEntitiesOfClass(Player.class, bounds)
                .stream()
                .filter(CurseAbilityHandler.INSTANCE::isCursed)
                .min(Comparator.comparingDouble(p ->
                        p.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ())))
                .orElse(null);

        if (nearestCursed != null) {
            float strength = CurseAbilityHandler.INSTANCE.getCurseStrength(nearestCursed);
            return (int) (transfRate * (1.0f + strength));
        }

        return transfRate;
    }

    @Inject(method = "isFull", at = @At("HEAD"), cancellable = true)
    private void applyCorruptionToIsFull(CallbackInfoReturnable<Boolean> cir) {
        TilePool pool = (TilePool) (Object) this;
        Level level = pool.getLevel();
        BlockPos pos = pool.getBlockPos();

        if (level != null && !level.isClientSide) {
            try {
                PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, pos);
                int corruption = corruptionData.getCorruption();

                if (corruption > 0) {
                    int effectiveCapacity = getEffectiveCapacity(corruption);
                    BlockState stateBelow = level.getBlockState(pos.below());
                    boolean isManaVoid = stateBelow.is(vazkii.botania.common.block.ModBlocks.manaVoid);

                    if (!isManaVoid && mana >= effectiveCapacity) {
                        cir.setReturnValue(true);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Inject(method = "getAvailableSpaceForMana", at = @At("HEAD"), cancellable = true)
    private void applyCorruptionToAvailableSpace(CallbackInfoReturnable<Integer> cir) {
        TilePool pool = (TilePool) (Object) this;
        Level level = pool.getLevel();
        BlockPos pos = pool.getBlockPos();

        if (level != null && !level.isClientSide) {
            try {
                PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, pos);
                int corruption = corruptionData.getCorruption();

                if (corruption > 0) {
                    int effectiveCapacity = getEffectiveCapacity(corruption);
                    int currentMana = pool.getCurrentMana();

                    int space = Math.max(0, effectiveCapacity - currentMana);
                    if (space > 0) {
                        cir.setReturnValue(space);
                    } else if (level.getBlockState(pos.below()).is(vazkii.botania.common.block.ModBlocks.manaVoid)) {
                        cir.setReturnValue(effectiveCapacity);
                    } else {
                        cir.setReturnValue(0);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private int getEffectiveCapacity(int corruption) {
        float penalty = corruption / 100.0F;
        return (int) (manaCap * (1.0F - penalty * 0.5F));
    }

    @ModifyVariable(
            method = "serverTick",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            name = "transfRate",
            remap = false
    )
    private static int applyCorruptionTransferPenalty(
            int transfRate,
            Level level,
            BlockPos worldPosition,
            BlockState state,
            TilePool self) {

        try {
            PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, worldPosition);
            int corruption = corruptionData.getCorruption();

            if (corruption > 0) {
                float penalty = corruption / 100.0F;
                return (int) (transfRate * (1.0F - penalty * 0.5F));
            }
        } catch (Exception e) {
        }

        return transfRate;
    }

    @Inject(method = "clientTick", at = @At("TAIL"))
    private static void spawnCorruptionParticles(
            Level level,
            BlockPos worldPosition,
            BlockState state,
            TilePool self,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {

        try {
            PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, worldPosition);
            int corruption = corruptionData.getCorruption();

            if (corruption > 25 && level.getGameTime() % 20 == 0) {
                int particleCount = corruption / 25;

                for (int i = 0; i < particleCount; i++) {
                    double x = worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                    double y = worldPosition.getY() + 0.5;
                    double z = worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;

                    float brightness = 0.3F - (corruption / 100.0F) * 0.2F;

                    vazkii.botania.client.fx.WispParticleData data =
                            vazkii.botania.client.fx.WispParticleData.wisp(
                                    0.1F, brightness, brightness, brightness, true
                            );

                    level.addParticle(data, x, y, z, 0, 0.02, 0);
                }
            }
        } catch (Exception e) {
        }
    }
}
