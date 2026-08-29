package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import javax.annotation.Nullable;

// TridentPlusEntity 是天雷战戟投掷实体，负责命中后召唤落点雷电实体。
public class TridentPlusEntity extends ThrownTrident {
    private static final int MAID_INFINITE_TRIDENT_GROUND_LIFE = 1200; // 女仆无限战戟落地存在 60 秒。
    private static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(TridentPlusEntity.class, EntityDataSerializers.BYTE); // 忠诚等级同步字段。
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.defineId(TridentPlusEntity.class, EntityDataSerializers.BOOLEAN); // 附魔光效同步字段。
    private ItemStack tridentStack = ItemStack.EMPTY; // 投掷出去的天雷战戟物品栈。
    private boolean dealtDamage; // 是否已经造成直接命中伤害。
    private boolean landingTriggered; // 是否已经生成落点雷电实体。
    private boolean maidInfiniteThrow; // 是否为女仆无限投掷生成的临时弹体。
    private int maidInfiniteInGroundTicks; // 女仆无限投掷弹体落地后的存在 tick。
    public int tridentPlusReturnTickCount; // 客户端返回音效计数。

    public TridentPlusEntity(EntityType<? extends TridentPlusEntity> type, Level level) {
        super(type, level);
    }

    public TridentPlusEntity(Level level, LivingEntity owner, ItemStack stack) {
        this(ModEntities.TRIDENT_PLUS_ENTITY.get(), level);
        this.tridentStack = stack.copy();
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        this.entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
        this.entityData.set(FOIL, stack.hasFoil());
    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOYALTY, (byte) 0);
        this.entityData.define(FOIL, false);
    }

    // 在原版飞行 tick 后追加自定义忠诚返回逻辑。
    @Override
    public void tick() {
        super.tick();
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        tickLoyaltyReturn();
        if (!this.level().isClientSide() && !this.landingTriggered && this.inGround) {
            triggerLandingEffect(this.position());
        }
        tickMaidInfiniteThrowDespawn();
    }

    // 设置女仆无限投掷弹体标记。
    public void setMaidInfiniteThrow(boolean maidInfiniteThrow) {
        this.maidInfiniteThrow = maidInfiniteThrow;
        if (maidInfiniteThrow) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
    }

    // 处理天雷战戟忠诚返回，避免使用原版 ThrownTrident 私有字段。
    public void tickLoyaltyReturn() {
        if (this.maidInfiniteThrow) return;
        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(LOYALTY);
        if (loyalty <= 0 || (!this.dealtDamage && !this.isNoPhysics()) || owner == null) return;

        if (!isAcceptibleReturnOwner(owner)) {
            if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
                this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }
            this.discard();
            return;
        }

        this.setNoPhysics(true);
        Vec3 toOwnerEye = owner.getEyePosition().subtract(this.position());
        this.setPosRaw(this.getX(), this.getY() + toOwnerEye.y * 0.015D * loyalty, this.getZ());
        if (this.level().isClientSide) {
            this.yOld = this.getY();
        }

        double returnSpeed = 0.05D * loyalty;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(toOwnerEye.normalize().scale(returnSpeed)));
        if (this.tridentPlusReturnTickCount == 0) {
            this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
        }
        this.tridentPlusReturnTickCount++;
    }

    // 女仆无限投掷弹体落地后按固定时间自动消失。
    public void tickMaidInfiniteThrowDespawn() {
        if (!this.maidInfiniteThrow) return;
        if (!this.inGround) {
            this.maidInfiniteInGroundTicks = 0;
            return;
        }

        this.maidInfiniteInGroundTicks++;
        if (this.maidInfiniteInGroundTicks >= MAID_INFINITE_TRIDENT_GROUND_LIFE) {
            this.discard();
        }
    }

    // 判断忠诚返回的主人是否仍然有效。
    public boolean isAcceptibleReturnOwner(Entity owner) {
        if (owner == null || !owner.isAlive()) return false;
        return !(owner instanceof ServerPlayer serverPlayer) || !serverPlayer.isSpectator();
    }

    @Override
    public ItemStack getPickupItem() {
        return this.tridentStack.isEmpty() ? super.getPickupItem() : this.tridentStack.copy();
    }

    @Override
    public boolean isFoil() {
        return this.entityData.get(FOIL);
    }

    @Override
    @Nullable
    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.dealtDamage ? null : super.findHitEntity(start, end);
    }

    // 直接命中实体时使用天雷战戟投掷伤害，并生成一次落点雷电。
    @Override
    public void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = TridentPlusConfig.thrownDamage();
        if (target instanceof LivingEntity living) {
            damage += EnchantmentHelper.getDamageBonus(this.tridentStack, living.getMobType());
        }

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;
        if (target.hurt(damageSource, damage)) {
            if (target.getType() != EntityType.ENDERMAN && target instanceof LivingEntity livingTarget) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, owner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
                }
                this.doPostHurtEffects(livingTarget);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        triggerLandingEffect(result.getLocation());
    }

    // 命中方块后生成一次落点雷电。
    @Override
    public void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        triggerLandingEffect(result.getLocation());
    }

    // 服务端生成落点雷电实体，客户端通过实体同步看到视觉。
    public void triggerLandingEffect(Vec3 position) {
        if (this.level().isClientSide() || this.landingTriggered) return;
        this.landingTriggered = true;
        TridentLightningStrikeEntity strike = new TridentLightningStrikeEntity(ModEntities.TRIDENT_LIGHTNING_STRIKE.get(), this.level());
        strike.setStrikeData(position, this.getOwner(), isChanneling(), this.random.nextInt());
        this.level().addFreshEntity(strike);
    }

    @Override
    public boolean isChanneling() {
        return EnchantmentHelper.hasChanneling(this.tridentStack);
    }

    @Override
    public boolean tryPickup(Player player) {
        if (this.maidInfiniteThrow) return false;
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    public SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TridentPlusItem", 10)) {
            this.tridentStack = ItemStack.of(tag.getCompound("TridentPlusItem"));
        }
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.landingTriggered = tag.getBoolean("LandingTriggered");
        this.maidInfiniteThrow = tag.getBoolean("MaidInfiniteThrow");
        this.maidInfiniteInGroundTicks = tag.getInt("MaidInfiniteInGroundTicks");
        if (this.maidInfiniteThrow) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
        this.entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.tridentStack));
        this.entityData.set(FOIL, this.tridentStack.hasFoil());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.tridentStack.isEmpty()) {
            tag.put("TridentPlusItem", this.tridentStack.save(new CompoundTag()));
        }
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putBoolean("LandingTriggered", this.landingTriggered);
        tag.putBoolean("MaidInfiniteThrow", this.maidInfiniteThrow);
        tag.putInt("MaidInfiniteInGroundTicks", this.maidInfiniteInGroundTicks);
    }

    @Override
    public void tickDespawn() {
        if (this.maidInfiniteThrow) return;
        int loyalty = this.entityData.get(LOYALTY);
        if (this.pickup != Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
