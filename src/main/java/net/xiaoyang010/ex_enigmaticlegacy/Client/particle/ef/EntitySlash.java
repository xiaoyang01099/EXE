package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EffectMessage;

import java.util.List;
import java.util.Random;

public class EntitySlash extends Entity {
    public static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(EntitySlash.class, EntityDataSerializers.INT);
    public Player player = null;
    public static Random rand = new Random();

    public EntitySlash(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.player == null || !this.player.isAlive()) {
            this.discard();
            return;
        }

        int currentLifetime = this.entityData.get(LIFETIME);

        if (currentLifetime % 2 == 0 && !this.level.isClientSide) {
            float offX = 0.5F * (float)Math.sin(Math.toRadians(-90.0F - this.player.getYRot()));
            float offZ = 0.5F * (float)Math.cos(Math.toRadians(-90.0F - this.player.getYRot()));

            Vec3 lookVec = this.player.getLookAngle();

            double x1 = this.player.getX() + lookVec.x * 0.5 + offX;
            double y1 = this.player.getY() + lookVec.y * 0.5 + this.player.getEyeHeight();
            double z1 = this.player.getZ() + lookVec.z * 0.5 + offZ;

            double x2 = this.player.getX() + lookVec.x * 4.0;
            double y2 = this.player.getY() + this.player.getEyeHeight() + lookVec.y * 4.0;
            double z2 = this.player.getZ() + lookVec.z * 4.0;

            this.setPos(this.player.getX(), this.player.getY(), this.player.getZ());

            Effect slash = new EffectSlash(this.player.level.dimension().location().hashCode())
                    .setSlashProperties(
                            this.player.getYRot(),
                            this.player.getXRot(),
                            30.0F + rand.nextFloat() * 120.0F,
                            2.0F,
                            1.5F,
                            120.0F
                    )
                    .setPosition(x1, y1, z1)
                    .setMotion(
                            (x2 - x1) / 5.0,
                            (y2 - y1) / 5.0,
                            (z2 - z1) / 5.0
                    )
                    .setLife(5)
                    .setAdditive(true)
                    .setColor(0.35F, 0.35F, 1.0F, 1.0F);

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new EffectMessage(FXHandler.FX_SLASH, slash.write())
            );

            double lx = this.player.getX() + lookVec.x * 2.0;
            double ly = this.player.getY() + this.player.getEyeHeight() + lookVec.y * 2.0;
            double lz = this.player.getZ() + lookVec.z * 2.0;

            AABB damageBox = new AABB(
                    lx - 2.0, ly - 2.0, lz - 2.0,
                    lx + 2.0, ly + 2.0, lz + 2.0
            );

            List<LivingEntity> entities = this.level.getEntitiesOfClass(
                    LivingEntity.class,
                    damageBox
            );

            for (LivingEntity entity : entities) {
                if (!entity.getUUID().equals(this.player.getUUID())) {
                    entity.invulnerableTime = 0;
                    entity.setLastHurtByPlayer(this.player);

                    if (entity.getHealth() > 0.0F) {
                        Effect cut = new EffectCut(this.level.dimension().location().hashCode())
                                .setSlashProperties(
                                        this.player.getYRot(),
                                        this.player.getXRot(),
                                        rand.nextFloat() * 360.0F
                                )
                                .setColor(0.35F, 0.35F, 1.0F, 1.0F)
                                .setPosition(
                                        entity.getX(),
                                        entity.getY() + entity.getBbHeight() / 2.0F,
                                        entity.getZ()
                                )
                                .setAdditive(true)
                                .setLife(10);

                        NetworkHandler.CHANNEL.send(
                                PacketDistributor.ALL.noArg(),
                                new EffectMessage(FXHandler.FX_CUT, cut.write())
                        );
                    }

                    entity.hurt(DamageSource.playerAttack(this.player), 6.0F);
                }
            }
        }

        this.entityData.set(LIFETIME, currentLifetime - 1);

        if (currentLifetime - 1 <= 0) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LIFETIME, 12);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
