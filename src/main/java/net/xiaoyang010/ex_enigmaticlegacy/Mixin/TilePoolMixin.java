package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.ClientCorruptionCache;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionData;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.client.fx.WispParticleData;
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
            at = @At(value = "STORE", ordinal = 0),
            name = "transfRate",
            remap = false
    )
    private static int modifyTransferRate(
            int transfRate,
            Level level,
            BlockPos worldPosition,
            BlockState state,
            TilePool self) {

        float finalRate = transfRate;

        AABB bounds = new AABB(
                worldPosition.getX() - 8, worldPosition.getY() - 8, worldPosition.getZ() - 8,
                worldPosition.getX() + 9, worldPosition.getY() + 9, worldPosition.getZ() + 9
        );

        Player nearestCursed = level.getEntitiesOfClass(Player.class, bounds)
                .stream()
                .filter(CurseAbilityHandler.INSTANCE::isCursed)
                .min(Comparator.comparingDouble(p ->
                        p.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ())))
                .orElse(null);

        if (nearestCursed != null) {
            float strength = CurseAbilityHandler.INSTANCE.getCurseStrength(nearestCursed);
            finalRate = finalRate * (1.0f + strength);
        }

        try {
            PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, worldPosition);
            int corruption = corruptionData.getCorruption();
            if (corruption > 0) {
                float penalty = corruption / 100.0F;
                finalRate = finalRate * (1.0F - penalty * 0.5F);
            }
        } catch (Exception ignored) {}

        return Math.max(1, (int) finalRate);
    }

    @Inject(
            method = "isFull",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void applyCorruptionToIsFull(CallbackInfoReturnable<Boolean> cir) {
        TilePool pool = (TilePool) (Object) this;
        Level level = pool.getLevel();
        BlockPos pos = pool.getBlockPos();

        if (level == null || level.isClientSide) return;

        try {
            PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, pos);
            int corruption = corruptionData.getCorruption();
            if (corruption > 0) {
                int effectiveCapacity = getEffectiveCapacity(corruption);
                boolean isManaVoid = level.getBlockState(pos.below())
                        .is(vazkii.botania.common.block.ModBlocks.manaVoid);
                if (!isManaVoid && mana >= effectiveCapacity) {
                    cir.setReturnValue(true);
                }
            }
        } catch (Exception ignored) {}
    }

    @Inject(
            method = "getAvailableSpaceForMana",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void applyCorruptionToAvailableSpace(CallbackInfoReturnable<Integer> cir) {
        TilePool pool = (TilePool) (Object) this;
        Level level = pool.getLevel();
        BlockPos pos = pool.getBlockPos();

        if (level == null || level.isClientSide) return;

        try {
            PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, pos);
            int corruption = corruptionData.getCorruption();
            if (corruption > 0) {
                int effectiveCapacity = getEffectiveCapacity(corruption);
                int currentMana = pool.getCurrentMana();
                boolean isManaVoid = level.getBlockState(pos.below())
                        .is(vazkii.botania.common.block.ModBlocks.manaVoid);
                if (isManaVoid) {
                    cir.setReturnValue(effectiveCapacity);
                } else {
                    cir.setReturnValue(Math.max(0, effectiveCapacity - currentMana));
                }
            }
        } catch (Exception ignored) {}
    }

    @Inject(
            method = "clientTick",
            at = @At("TAIL"),
            remap = false
    )
    private static void spawnCorruptionParticles(
            Level level,
            BlockPos worldPosition,
            BlockState state,
            TilePool self,
            CallbackInfo ci) {

        int corruption = ClientCorruptionCache.getCorruption(worldPosition);
        if (corruption <= 25) return;
        if (level.getGameTime() % 10 != 0) return;

        int particleCount = corruption / 25; // 1~4 个粒子
        for (int i = 0; i < particleCount; i++) {
            double x = worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;

            // 污染越重颜色越深，带紫色调
            float brightness = Math.max(0.05F, 0.3F - (corruption / 100.0F) * 0.25F);
            float size = 0.1F + (corruption / 100.0F) * 0.1F;

            WispParticleData data = WispParticleData.wisp(
                    size,
                    brightness,
                    0.0F,
                    brightness * 0.5F,
                    true
            );
            level.addParticle(data, x, y, z,
                    (Math.random() - 0.5) * 0.01,
                    0.01 + (corruption / 100.0F) * 0.02,
                    (Math.random() - 0.5) * 0.01);
        }
    }

    private int getEffectiveCapacity(int corruption) {
        float penalty = corruption / 100.0F;
        return (int) (manaCap * (1.0F - penalty * 0.5F));
    }
}