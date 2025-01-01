package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.FunctionalFlower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.util.List;
import java.util.Random;

public class SubTileVacuity extends TileEntityFunctionalFlower {
    public SubTileVacuity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (!level.isClientSide && getMana() >= cost()) {
            AABB range = new AABB(getEffectivePos().offset(-getRange(), -getRange(), -getRange()),
                    getEffectivePos().offset(getRange(), getRange(), getRange()));

            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, range);
            addMana(-cost());

            list.forEach(entity -> {
                if (canAttack(entity)) {
                    int air = entity.getAirSupply();
                    air = decreaseAirSupply(entity, air);
                    entity.setAirSupply(air);

                    if (entity.getAirSupply() <= -20) {
                        entity.setAirSupply(0);
                        Vec3 motion = entity.getDeltaMovement();

                        Random rand = level.getRandom();
                        for(int i = 0; i < 8; ++i) {
                            double d2 = rand.nextDouble() - rand.nextDouble();
                            double d3 = rand.nextDouble() - rand.nextDouble();
                            double d4 = rand.nextDouble() - rand.nextDouble();

                            if(level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.BUBBLE,
                                        entity.getX() + d2,
                                        entity.getY() + d3,
                                        entity.getZ() + d4,
                                        1,
                                        motion.x, motion.y, motion.z,
                                        0.1);
                            }
                        }
                        entity.hurt(DamageSource.DROWN, 2.0F);
                    }
                }
            });
        }
    }

    private boolean canAttack(LivingEntity entity) {
        return !(entity instanceof Player player && player.getAbilities().invulnerable);
    }

    private int decreaseAirSupply(LivingEntity entity, int air) {
        int i = EnchantmentHelper.getRespiration(entity);
        return i > 0 && level.getRandom().nextInt(i + 1) > 0 ? air : air - 5;
    }

    private int getRange() {
        return 4;
    }

    private int cost() {
        return 1000;
    }

    @Override
    public int getMaxMana() {
        return 6000;
    }

    @Override
    public int getColor() {
        return 0xD3D610;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
    }
}