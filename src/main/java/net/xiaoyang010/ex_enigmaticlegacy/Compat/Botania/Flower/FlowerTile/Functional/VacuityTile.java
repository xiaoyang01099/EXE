package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class VacuityTile extends TileEntityFunctionalFlower {
    public VacuityTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<VacuityTile> {
        public FunctionalWandHud(VacuityTile flower) {
            super(flower);
        }
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
        // 只攻击实现了Monster接口的实体（敌对生物）
        return entity instanceof Monster
                // 确保不会攻击无敌状态的玩家（保留原有的保护）
                && !(entity instanceof Player player && player.getAbilities().invulnerable);
    }

    private int decreaseAirSupply(LivingEntity entity, int air) {
        int i = EnchantmentHelper.getRespiration(entity);
        return i > 0 && level.getRandom().nextInt(i + 1) > 0 ? air : air - 5;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    private int getRange() {
        return 20;
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
        return 0x4B0082;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
    }
}