package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.api.RewardType;

public class EntityRewardItemStack extends ItemEntity {
    private static final EntityDataAccessor<String> PLAYER_NAME =
            SynchedEntityData.defineId(EntityRewardItemStack.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> REWARD_TYPE =
            SynchedEntityData.defineId(EntityRewardItemStack.class, EntityDataSerializers.BYTE);

    public EntityRewardItemStack(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public EntityRewardItemStack(Level level, double x, double y, double z, String playerName, RewardType type) {
        super(EntityType.ITEM, level);
        this.setPos(x, y, z);
        this.setPlayerName(playerName);
        this.setRewardType(type);
    }

    public EntityRewardItemStack(Level level, double x, double y, double z, ItemStack stack, String playerName, RewardType type) {
        this(level, x, y, z, stack, type);
        this.setPlayerName(playerName);
    }

    public EntityRewardItemStack(Level level, double x, double y, double z, ItemStack stack, RewardType type) {
        super(EntityType.ITEM, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setRewardType(type);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLAYER_NAME, "");
        this.entityData.define(REWARD_TYPE, (byte) 0);
    }

    public void setRewardType(RewardType type) {
        this.entityData.set(REWARD_TYPE, (byte) type.ordinal());
    }

    public RewardType getRewardType() {
        return RewardType.values()[this.entityData.get(REWARD_TYPE)];
    }

    public void setPlayerName(String playerName) {
        this.entityData.set(PLAYER_NAME, playerName);
    }

    public String getPlayerName() {
        return this.entityData.get(PLAYER_NAME);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getPlayerName() != null) {
            tag.putString("playerName", this.getPlayerName());
        }
        tag.putByte("type", (byte) this.getRewardType().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("playerName")) {
            this.setPlayerName(tag.getString("playerName"));
        }
        this.setRewardType(RewardType.values()[tag.getByte("type")]);
    }

    @Override
    public void tick() {
        if (this.age == 0) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL,
                    3.0F, 0.75F + this.random.nextFloat());
        }

        if (!this.onGround) {
            if (this.level.isClientSide && this.age % 2 < 2) {
                double motX = this.random.nextGaussian() * 0.05;
                double motY = -this.getDeltaMovement().y * 0.5;
                double motZ = this.random.nextGaussian() * 0.05;
                this.level.addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY() - 0.3, this.getZ(),
                        motX, motY, motZ);
            }

            if (this.level instanceof ServerLevel serverLevel) {
                for (int sparkCount = 1; sparkCount <= 5; ++sparkCount) {
                    double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
                    double motY = random.nextDouble();
                    double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
                    serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            this.getX(), this.getY(), this.getZ(),
                            1, motX, motY, motZ, 0.0D);
                }

                if (this.tickCount % 5 == 0) {
                    for (int sparkCount = 1; sparkCount <= 10; ++sparkCount) {
                        double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble() / 2.0;
                        double motY = random.nextDouble() / 2.0;
                        double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble() / 2.0;
                        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                                this.getX(), this.getY(), this.getZ(),
                                1, motX, motY, motZ, 0.0D);
                    }
                }
            }
        }

        super.tick();
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level.isClientSide) {
            ItemStack itemstack = this.getItem();
            if (!itemstack.isEmpty()) {
                if (player.getInventory().add(itemstack)) {
                    player.take(this, 1);
                    this.discard();
                }
            }
        }
    }
}