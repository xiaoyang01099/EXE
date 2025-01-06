package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CatMewEntity extends Monster {
    //是否二阶段
    private static final EntityDataAccessor<Boolean> isTwoPhase = SynchedEntityData.defineId(CatMewEntity.class, EntityDataSerializers.BOOLEAN);;
    private static final String NBT_IS__PHASE = "isTwoPhase";
    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    private static final int phaseUpHeight = 5; //转变时上升高度
    private int phaseTime = 100; //转变时间
    private boolean phase = false; //是否处于转变中
    private double catEndY = 0;
    private int shieldParticles = 0;
    private WorldBorder worldBorder;

    public CatMewEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CatMewEntity(SpawnEntity spawnEntity, Level level) {
        this(ModEntities.KIND_MIAO.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1) addWorldBorder();

        if (!getIsTwoPhase() && (this.getHealth() / this.getMaxHealth() <= 0.30f))
            starPhase();

        if (phaseTime == 0)
            endPhase();

        //转变倒计时
        if (this.phase && phaseTime > 0)
            phaseIng();

        if (getIsTwoPhase()){ //二阶段攻击
            maintainPhaseTwo();
            if (isAttack(5, 5))
                shootWitherSkull(this.getTarget());
            if (isAttack(5, 3))
                shootWitherFireBall(this.getTarget());
            if (isAttack(10, 4))
                shootWitherDragonBall(this.getTarget());
            if (isAttack(20, 2) && this.getTarget() instanceof Player player){
                summonLightning(player);
            }
        }else {
            if (isAttack(60, 3)) shootWitherSkull(this.getTarget());

            if (isAttack(100, 2)) shootArrow();

            if (isAttack(40, 10) && this.getTarget() instanceof Player player){
                summonLightning(player);
            }
        }
    }

    public boolean getIsTwoPhase() {
        return this.getEntityData().get(isTwoPhase);
    }

    public void setIsTwoPhase(boolean flag) {
        this.getEntityData().set(isTwoPhase, flag);
    }

    @Override
    public boolean hurt(DamageSource source, float pAmount) {
        Entity sourceDirectEntity = source.getDirectEntity();
        if (sourceDirectEntity instanceof LightningBolt){
            return false;
        }
        return super.hurt(source, pAmount);
    }

    @Override
    public void die(DamageSource pDamageSource) {
        if (this.phase){ //转变时被击杀
            this.phaseTime = 100;
            this.phase = false;
        }
        if (!getIsTwoPhase()) {
            starPhase();
        }
        else super.die(pDamageSource);
    }

    @Override
    public void setHealth(float pHealth) {
        if (pHealth == 0.0 && !getIsTwoPhase())
            starPhase();
        else super.setHealth(pHealth);
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (this.phase){ //转变时被击杀
            this.phaseTime = 100;
            this.phase = false;
        }
        removeWorldBorder();
        super.remove(pReason);
    }

    @Override
    public void kill() {
//        super.kill();
    }

    @Override
    protected int getExperienceReward(Player pPlayer) {
        return 1024;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hit) {
        super.dropCustomDeathLoot(source, looting, hit);
        spawnLoot();
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    //免疫伤害
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.isExplosion() || source.isFall()) return true;
        return this.isRemoved() || this.invulnerable && source != DamageSource.OUT_OF_WORLD && !source.isCreativePlayer();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(isTwoPhase, false);
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
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean(NBT_IS__PHASE, getIsTwoPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setIsTwoPhase(pCompound.getBoolean(NBT_IS__PHASE));
        if (this.hasCustomName()) {
            this.bossInfo.setName(this.getDisplayName());
        }

    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossInfo.setName(this.getDisplayName());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35d)
                .add(Attributes.MAX_HEALTH, 114514)
                .add(Attributes.ARMOR, 100)
                .add(Attributes.ATTACK_DAMAGE, 20)
                .add(Attributes.FOLLOW_RANGE, 96)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100)
                .add(Attributes.ATTACK_KNOCKBACK, 1);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow"));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * 添加边界
     */
    private void addWorldBorder(){
        if (this.level instanceof  ServerLevel serverLevel){
            this.worldBorder = serverLevel.getWorldBorder();
            worldBorder.setCenter(this.getX(), this.getZ());
            worldBorder.setSize(64);
            worldBorder.setWarningBlocks(2);
            worldBorder.setDamagePerBlock(1024);
        }
    }

    /**
     * 移除边界
     */
    private void removeWorldBorder(){
        if (this.level instanceof  ServerLevel serverLevel && this.worldBorder != null){
            this.worldBorder.setSize(Integer.MAX_VALUE);
        }
    }

    /**
     * 攻击前判定
     * @param timeTick 间隔
     * @param chance 概率
     */
    private boolean isAttack(int timeTick, int chance){
        return this.level.getGameTime() % timeTick == 0 && this.getTarget() != null && this.level.random.nextInt(chance) < 1;
    }

    /**
     * 转变中逻辑
     */
    public void phaseIng(){
        --this.phaseTime;

        if (this.shieldParticles < 360) this.shieldParticles += 10;

        //向上移动
        double high = Math.abs(this.getY() - catEndY);
        if (high < phaseUpHeight){
            this.setDeltaMovement(0, 0.05, 0);
        }else {
            // 达到最大高度后悬停
            this.setDeltaMovement(0, 0, 0);
        }

        // 旋转的护盾粒子效果
        spawnShieldParticles();

        // 产生能量波纹
        if (phaseTime % 20 == 0) {
            spawnEnergyRipple();
        }
    }

    /**
     * 转变结束
     */
    public void endPhase(){
        setIsTwoPhase(true); //进入二阶段
        this.setInvulnerable(false); //移除无敌
        this.phase = false; //移除转变状态
        this.heal(this.getMaxHealth() - this.getHealth()); //回满血量
        this.phaseTime = 100;
        this.setNoGravity(false);
        this.shieldParticles = 0;
    }

    /**
     * 进入第二阶段（血量低于30%）
     */
    public void starPhase(){
        this.phase = true; //转变
        this.catEndY = this.getY(); //记录Y坐标
        this.setInvulnerable(true); //无敌
        this.setNoGravity(true); //无重力
        starPhaseEffect();
    }

    /**
     * 发射箭矢
     */
    private void shootArrow(){
        AABB aabb = this.getBoundingBox().inflate(8);
        if (this.level.isClientSide) return;

        for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (living.isAlive() && !(living instanceof CatMewEntity)){
                BlockPos above = living.getOnPos().above(8);
                Arrow arrow  = new Arrow(this.level, this);
                arrow.shoot(0, -1, 0, 3.0f, 1.0f);
                arrow.setBaseDamage(10d);
                arrow.setSecondsOnFire(3);
                arrow.setPierceLevel((byte) 2);

                this.level.addFreshEntity(arrow);
            }
        }

    }

    /**
     * 二阶段移动
     */
    private void maintainPhaseTwo() {
        double HOVER_HEIGHT = 10.0D; // 飞行高度
        double MOVE_SPEED = 0.3D;

        if (this.getTarget() == null) return;

        // 计算当前与目标的水平距离
        double dx = this.getX() - this.getTarget().getX();
        double dz = this.getZ() - this.getTarget().getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // 计算目标飞行高度
        double targetY = this.getTarget().getY() + HOVER_HEIGHT;

        // 圆周运动的角度随时间变化
        double angle = (this.tickCount * 0.05) % (2 * Math.PI);

        // 计算新的目标位置（在目标周围做圆周运动）
        double circleRadius = 8.0D; // 圆周运动半径
        double targetX = this.getTarget().getX() + Math.cos(angle) * circleRadius;
        double targetZ = this.getTarget().getZ() + Math.sin(angle) * circleRadius;

        // 计算移动向量
        double moveX = targetX - this.getX();
        double moveY = targetY - this.getY();
        double moveZ = targetZ - this.getZ();

        // 标准化移动向量并应用速度
        double moveLength = Math.sqrt(moveX * moveX + moveY * moveY + moveZ * moveZ);
        if (moveLength > 0) {
            moveX = (moveX / moveLength) * MOVE_SPEED;
            moveY = (moveY / moveLength) * MOVE_SPEED;
            moveZ = (moveZ / moveLength) * MOVE_SPEED;

            // 设置实体移动
            this.setDeltaMovement(moveX, moveY, moveZ);
        }

        // 随机变向飞行
        if (this.random.nextFloat() < 0.05) { // 5%的概率改变方向
            this.tickCount += 200; // 快速改变角度
        }

        // 如果距离目标太远，加速追击
        if (horizontalDist > 15.0D) {
            double catchUpSpeed = 0.5D;
            this.setDeltaMovement(
                    (this.getTarget().getX() - this.getX()) * catchUpSpeed,
                    (targetY - this.getY()) * catchUpSpeed,
                    (this.getTarget().getZ() - this.getZ()) * catchUpSpeed
            );
        }
    }

    /**
     * 生成掉落物
     */
    private void spawnLoot(){
        ItemStack stack = new ItemStack(Items.DIAMOND, 64);
        ItemStack stack0 = new ItemStack(Items.NETHERITE_INGOT, 16);
        ItemStack stack1 = new ItemStack(ModItems.ASTRAL_NUGGET.get(), 16);
        ItemStack stack2 = new ItemStack(Items.NETHER_STAR, 2);
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(stack);
        stacks.add(stack0);
        stacks.add(stack1);
        stacks.add(stack2);

        for (ItemStack itemStack : stacks) {
            ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), itemStack);
            this.level.addFreshEntity(item);
        }
    }

    /**
     * 粒子能量波纹
     */
    private void spawnEnergyRipple() {
        if (this.level instanceof ServerLevel serverLevel) {
            double baseRadius = 0.5;
            int particleCount = 36;

            for (int ring = 0; ring < 3; ring++) {
                double radius = baseRadius + (ring * 0.5);
                for (int i = 0; i < particleCount; i++) {
                    double angle = ((Math.PI * 2 * i) / particleCount);
                    double x = this.getX() + Math.cos(angle) * radius;
                    double z = this.getZ() + Math.sin(angle) * radius;

                    serverLevel.sendParticles(ParticleTypes.WITCH, x, this.getY() + ring * 0.2, z, 1, 0, 0, 0, 0.02);
                }
            }
        }
    }

    /**
     * 粒子护盾
     */
    private void spawnShieldParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            int particleCount = 36; // 每圈的粒子数
            double radius = 2.0;
            double yOffset = Math.sin(this.phaseTime * 0.05) * 0.5; // 上下浮动效果

            // 水平环
            for (int i = 0; i < particleCount; i++) {
                double angle = ((Math.PI * 2 * i) / particleCount) + Math.toRadians(this.shieldParticles);
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;

                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, this.getY() + 1 + yOffset, z, 1, 0, 0, 0, 0.02);
            }

            // 垂直环
            for (int i = 0; i < particleCount; i++) {
                double angle = ((Math.PI * 2 * i) / particleCount) + Math.toRadians(this.shieldParticles);
                double y = this.getY() + Math.cos(angle) * radius;
                double x = this.getX() + Math.sin(angle) * radius;

                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 1, this.getZ(), 1, 0, 0, 0, 0.02);
            }
        }
    }

    /**
     * 转变开始效果
     */
    private void starPhaseEffect() {
        // 播放开始音效
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 1.0F);

        // 清除所有附近实体的目标
        AABB area = this.getBoundingBox().inflate(8.d);
        for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living instanceof CatMewEntity) continue;
            if (living.isAlive() && !(living instanceof Player)){
                living.kill();
            }
            if (living instanceof Player player && player.isAlive()){
                player.hurt(DamageSource.mobAttack(this), 10f);
            }
        }

        this.removeAllEffects(); //清除所有buff

        summonLightning(this);
        summonLightning(this);

        // 发送地震效果
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 10,  // 粒子数量
                    3.0, // X范围
                    3.0, // Y范围
                    3.0, // Z范围
                    0.5  // 速度
            );
        }
    }

    /**
     * 发射调零头
     * @param target 目标
     */
    public void shootWitherSkull(LivingEntity target) {
        if (!this.level.isClientSide) {
            Vec3 vec3 = getShootVec3(target);
            WitherSkull witherskull = new WitherSkull(this.level, this, vec3.x, vec3.y, vec3.z);
            witherskull.setOwner(this);
            witherskull.setDangerous(this.level.random.nextInt(3) < 1); //快慢
            witherskull.setPosRaw(this.getX(), this.getY(), this.getZ());
            this.level.addFreshEntity(witherskull);

            // 添加发射音效
            this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 1.0F);

            // 第二阶段额外效果
            if (getIsTwoPhase() && target instanceof Player player && this.level.random.nextInt(5) < 1) {
                summonLightning(player);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
            }
        }
    }

    /**
     * 发射火球
     * @param target 目标
     */
    public void shootWitherFireBall(LivingEntity target) {
        if (!this.level.isClientSide) {
            Vec3 vec3 = getShootVec3(target);
            Fireball fireball = new LargeFireball(level, this, vec3.x, vec3.y, vec3.z, 4);
            this.level.addFreshEntity(fireball);
            this.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
        }
    }

    /**
     * 发射龙息
     * @param target 目标
     */
    public void shootWitherDragonBall(LivingEntity target) {
        if (!this.level.isClientSide) {
            Vec3 vec3 = getShootVec3(target);
            DragonFireball dragonFireball = new DragonFireball(level, this, vec3.x, vec3.y, vec3.z);
            this.level.addFreshEntity(dragonFireball);
            this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.0F, 1.0F);
        }
    }

    /**
     * 获取发射向量
     * @param target 目标
     */
    private Vec3 getShootVec3(LivingEntity target){
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        double d3 = target.getX() - d0;
        double d4 = target.getY() + (double)target.getEyeHeight() * 0.5 - d1;
        double d5 = target.getZ() - d2;
        return new Vec3(d3, d4, d5);
    }

    /**
     * 召唤雷电
     * @param target 目标
     */
    private void summonLightning(LivingEntity target) {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level);
            if (lightning != null) {
                lightning.moveTo(target.getX(), target.getY(), target.getZ(), 0.0F, 0.0F);
                serverLevel.addFreshEntity(lightning);
            }
        }
    }
}