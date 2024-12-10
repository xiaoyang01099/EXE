package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

@Mod.EventBusSubscriber
public class MiaoMiaoEntity extends Monster {

    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    private boolean isAngry = false;

    public MiaoMiaoEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.KIND_MIAO.get(), world);
    }

    public MiaoMiaoEntity(EntityType<MiaoMiaoEntity> type, Level world) {
        super(type, world);
        maxUpStep = 0.6f;
        xpReward = 0;
        setNoAi(false);
        setCustomName(new TextComponent("miaomiao"));
        setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KILLYOU.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.STARFLOWERSTONE.get()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected double getAttackReachSqr(LivingEntity entity) {
                return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
            }
        });
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(ModItems.MIAOMIAOTOU.get()));
    }

    @Override
    public SoundEvent getAmbientSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:what"));
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:scray")), 0.15f, 1);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:nothing"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Player) {
            Player attacker = (Player) source.getDirectEntity();
            double distance = this.distanceTo(attacker);


            if (!isAngry) {
                this.triggerBossMusic();
                isAngry = true;  //
            }

            if (distance > 10) {
                summonLightning(attacker);
                shootWitherSkull(attacker);
            } else {
                this.performMeleeAttack(attacker);
            }
        }
        return super.hurt(source, amount);
    }

    // 发射凋零头
    private void shootWitherSkull(LivingEntity target) {
        if (!this.level.isClientSide) {
            WitherSkull witherSkull = new WitherSkull(EntityType.WITHER_SKULL, this.level);
            witherSkull.setOwner(this);

            // 计算方向向量
            double dX = target.getX() - this.getX();
            double dY = target.getY(0.5D) - witherSkull.getY(); // 调整高度瞄准
            double dZ = target.getZ() - this.getZ();

            // 使用 shoot 方法发射凋零头，设置发射速度和偏转角度
            witherSkull.shoot(dX, dY, dZ, 2.0F, 0); // 2.0F 是速度, 0 是偏移角度
            this.level.addFreshEntity(witherSkull);
        }
    }

    // 近战攻击方法
    private void performMeleeAttack(LivingEntity target) {
        this.doHurtTarget(target);
    }

    // 播放boss音乐
    private void triggerBossMusic() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:cute"));
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, SoundSource.MUSIC, 1.0f, 1.0f);
        }
    }

    // 当玩家停止攻击或者仇恨结束时停止播放音乐
    private void stopBossMusic() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            SoundEvent stopSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:music.stop"));
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), stopSound, SoundSource.MUSIC, 1.0f, 1.0f);
        }
    }

    // 召唤雷电
    private void summonLightning(LivingEntity target) {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level);
            lightning.moveTo(target.getX(), target.getY(), target.getZ(), 0.0F, 0.0F);
            serverLevel.addFreshEntity(lightning);
        }
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
        this.stopBossMusic();  // 当不再有玩家看见该生物时，停止音乐
        isAngry = false; // 重置仇恨状态
    }

    public static void init() {
        SpawnPlacements.register(ModEntities.KIND_MIAO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> (world.getBlockState(pos.below()).getMaterial() == Material.GRASS && world.getRawBrightness(pos, 0) > 8));
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 1024);
        builder = builder.add(Attributes.ARMOR, 100);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 300);
        builder = builder.add(Attributes.FOLLOW_RANGE, 100);
        builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 100);
        builder = builder.add(Attributes.ATTACK_KNOCKBACK, 5);
        return builder;
    }
}
