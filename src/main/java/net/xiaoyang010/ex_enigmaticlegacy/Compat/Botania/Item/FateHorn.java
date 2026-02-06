package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.List;

public class FateHorn extends Item {
    private static final double RANGE = 7.0;
    private static final int MANA_PER_TICK = 100;
    private static final float PARTICLE_R = 0xDF / 255f;
    private static final float PARTICLE_G = 0xB4 / 255f;
    private static final float PARTICLE_B = 0x12 / 255f;

    public FateHorn(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        AABB searchBox = new AABB(
                player.getX() - RANGE, player.getY() - RANGE, player.getZ() - RANGE,
                player.getX() + RANGE, player.getY() + RANGE, player.getZ() + RANGE
        );

        List<Mob> entities = level.getEntitiesOfClass(Mob.class, searchBox,
                mob -> mob != null && mob.isAlive() && !mob.isRemoved() && canAffect(mob));

        if (!level.isClientSide && !entities.isEmpty()) {
            boolean hasMana = ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_TICK, true);

            if (hasMana) {
                for (Mob mob : entities) {
                    mob.addEffect(new MobEffectInstance(
                            ModEffects.ROOTED.get(),
                            60,
                            0,
                            false,
                            true,
                            true
                    ));
                }

                if (remainingUseDuration % 20 == 0) {
                    level.playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.NOTE_BLOCK_BASS,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.5F);
                }
            } else {
                player.stopUsingItem();
                return;
            }
        }

        if (level.isClientSide) {
            spawnParticleRing(level, player.getX(), player.getY(), player.getZ(), RANGE);
        }
    }

    private boolean canAffect(Mob mob) {
        if (!mob.canChangeDimensions()) {
            return false;
        }

        if (mob.isInvulnerable()) {
            return true;
        }

        // 可以添加更多条件：
        // 例如：只影响敌对生物
        // if (mob instanceof Enemy) {
        //     return true;
        // }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticleRing(Level level, double x, double y, double z, double range) {
        int particleCount = (int) (range * 16);

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double offsetX = Math.cos(angle) * range;
            double offsetZ = Math.sin(angle) * range;

            double particleX = x + offsetX;
            double particleY = y + 0.5;
            double particleZ = z + offsetZ;

            double motionX = -Math.cos(angle) * 0.02;
            double motionY = 0.05;
            double motionZ = -Math.sin(angle) * 0.02;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    1.0F,
                    PARTICLE_R, PARTICLE_G, PARTICLE_B,
                    8
            );

            level.addParticle(sparkle,
                    particleX, particleY, particleZ,
                    motionX, motionY, motionZ);

            if (i % 4 == 0) {
                WispParticleData wisp = WispParticleData.wisp(
                        0.8F,
                        PARTICLE_R, PARTICLE_G, PARTICLE_B,
                        0.7F
                );

                level.addParticle(wisp,
                        particleX, particleY, particleZ,
                        motionX * 0.5, motionY * 0.5, motionZ * 0.5);
            }
        }

        for (int i = 0; i < 5; i++) {
            double randomAngle = level.random.nextDouble() * 2 * Math.PI;
            double randomRadius = level.random.nextDouble() * range * 0.3;
            double offsetX = Math.cos(randomAngle) * randomRadius;
            double offsetZ = Math.sin(randomAngle) * randomRadius;

            SparkleParticleData centerSparkle = SparkleParticleData.sparkle(
                    1.2F,
                    PARTICLE_R, PARTICLE_G, PARTICLE_B,
                    6
            );

            level.addParticle(centerSparkle,
                    x + offsetX, y + 0.5, z + offsetZ,
                    0, 0.1, 0);
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}
