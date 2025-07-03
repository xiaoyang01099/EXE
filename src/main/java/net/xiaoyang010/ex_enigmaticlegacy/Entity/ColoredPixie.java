package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.entity.EntityPixie;

/**
 * 自定义颜色的精灵实体
 */
public class ColoredPixie extends EntityPixie {

    // 颜色数据同步
    private static final EntityDataAccessor<Float> RED = SynchedEntityData.defineId(ColoredPixie.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GREEN = SynchedEntityData.defineId(ColoredPixie.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BLUE = SynchedEntityData.defineId(ColoredPixie.class, EntityDataSerializers.FLOAT);

    public ColoredPixie(EntityType<? extends EntityPixie> type, Level level) {
        super((EntityType<EntityPixie>) type, level);
    }

    public ColoredPixie(Level world) {
        super(world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RED, 1.0F);
        entityData.define(GREEN, 0.25F);
        entityData.define(BLUE, 0.9F);
    }

    /**
     * 设置精灵颜色
     */
    public void setColor(float red, float green, float blue) {
        entityData.set(RED, red);
        entityData.set(GREEN, green);
        entityData.set(BLUE, blue);
    }

    public float getRed() {
        return entityData.get(RED);
    }

    public float getGreen() {
        return entityData.get(GREEN);
    }

    public float getBlue() {
        return entityData.get(BLUE);
    }

    @Override
    public void baseTick() {
        // 复制父类逻辑，但使用自定义颜色
        if (!level.isClientSide
                && (getTarget() == null || tickCount > 200)) {
            discard();
        }

        // 自定义颜色粒子效果
        if (level.isClientSide) {
            for (int i = 0; i < 4; i++) {
                float r = getRed();
                float g = getGreen();
                float b = getBlue();

                SparkleParticleData data = SparkleParticleData.sparkle(
                        0.1F + (float) Math.random() * 0.25F,
                        r, g, b, 12
                );

                level.addParticle(data,
                        getX() + (Math.random() - 0.5) * 0.25,
                        getY() + 0.5 + (Math.random() - 0.5) * 0.25,
                        getZ() + (Math.random() - 0.5) * 0.25,
                        0, 0, 0
                );
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level != null && level.isClientSide) {
            for (int i = 0; i < 12; i++) {
                SparkleParticleData data = SparkleParticleData.sparkle(
                        1F + (float) Math.random() * 0.25F,
                        getRed(), getGreen(), getBlue(), 5
                );

                level.addParticle(data,
                        getX() + (Math.random() - 0.5) * 0.25,
                        getY() + 0.5 + (Math.random() - 0.5) * 0.25,
                        getZ() + (Math.random() - 0.5) * 0.25,
                        0, 0, 0
                );
            }
        }
        super.remove(reason);
    }
}