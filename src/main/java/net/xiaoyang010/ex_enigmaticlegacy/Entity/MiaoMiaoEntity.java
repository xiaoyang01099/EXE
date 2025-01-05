package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.WitherSkullAttackGoal;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class MiaoMiaoEntity extends Monster {
    private static final double MAX_TRANSITION_HEIGHT = 5.0D; // 最大上升高度
    private double transitionStartY; // 记录开始相变时的Y坐标
    private boolean isTransitionComplete = false; // 标记相变是否完成
    private List<LightningBolt> transitionLightnings = new ArrayList<>();
    private int phaseTransitionTicks = 0;
    private boolean isInvulnerableDuringTransition = false;
    private int shieldParticlesAngle = 0;
    private static final int TRANSITION_DURATION = 160; // 8秒
    private List<LivingEntity> nearbyEntities = new ArrayList<>();
    private boolean isInPhaseTwo = false;
    private static final double RANGED_ATTACK_RADIUS = 5.0D;
    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    private final List<Vec3> particlePositions = new ArrayList<>();
    private boolean isLightningCircleActive = false;
    private final List<LightningBolt> lightningCircle = new ArrayList<>();
    private static final double LIGHTNING_CIRCLE_RADIUS = 50.0D;
    private static final int LIGHTNING_CIRCLE_POINTS = 36; // 每10度一个闪电
    private int tickCounter = 0;
    private double circleOriginX;
    private double circleOriginY;
    private double circleOriginZ;

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

        // 添加远程攻击AI，第二阶段时速度更快
        this.goalSelector.addGoal(1, new WitherSkullAttackGoal(this, RANGED_ATTACK_RADIUS, 2.0F) {
            @Override
            public boolean canUse() {
                // 第二阶段时必定使用远程攻击
                if (isInPhaseTwo) {
                    return super.canUse();
                }
                // 第一阶段时正常判断
                return super.canUse() && MiaoMiaoEntity.this.distanceToSqr(MiaoMiaoEntity.this.getTarget()) >= RANGED_ATTACK_RADIUS * RANGED_ATTACK_RADIUS;
            }

            @Override
            public void tick() {
                super.tick();
                // 第二阶段时提高攻击频率
                if (isInPhaseTwo) {
                    this.attackTime = Math.max(this.attackTime - 1, 0);
                }
            }
        });

        // 近战攻击AI
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected double getAttackReachSqr(LivingEntity entity) {
                return MiaoMiaoEntity.this.getBbWidth() * MiaoMiaoEntity.this.getBbWidth() + entity.getBbWidth();
            }

            @Override
            public boolean canUse() {
                // 第二阶段禁用近战
                if (isInPhaseTwo) {
                    return false;
                }
                // 第一阶段时在较近距离才使用近战
                return super.canUse() && MiaoMiaoEntity.this.distanceToSqr(MiaoMiaoEntity.this.getTarget()) < RANGED_ATTACK_RADIUS * RANGED_ATTACK_RADIUS;
            }
        });

        // 基础移动和行为AI
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1) {
            @Override
            public boolean canUse() {
                // 第二阶段时提高移动速度
                if (isInPhaseTwo) {
                    this.mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.5);
                }
                return super.canUse();
            }
        });

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
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow"));
    }

    private void enterPhaseTwo() {
        isInPhaseTwo = true;

        // 设置飞行能力
        this.setNoGravity(true);

        // 提高属性
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.5);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D); // 提高攻击力

        // 给予额外效果
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 如果伤害来自闪电，且闪电是过渡期间产生的，直接免疫
        if (source.getDirectEntity() instanceof LightningBolt lightning &&
                (isInvulnerableDuringTransition || transitionLightnings.contains(lightning))) {
            return false;
        }

        // 免疫爆炸和摔落伤害
        if (source.isExplosion() || source == DamageSource.FALL) {
            return false;
        }

        if (isInvulnerableDuringTransition) {
            // 在过渡期间免疫所有伤害
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            // 更新血条
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());

            // 检查是否需要进入第二阶段（血量低于10%）
            if (!isInPhaseTwo && (this.getHealth() / this.getMaxHealth() <= 0.1f)) {
                enterPhaseTwo();
            }
        }

        if (source.getDirectEntity() instanceof Player) {
            Player attacker = (Player) source.getDirectEntity();

            if (!isInPhaseTwo) {
                // 第一阶段的攻击逻辑
                double distance = this.distanceTo(attacker);
                if (distance > 10) {
                    if (random.nextFloat() < 0.7f) { // 70%概率发动组合技
                        summonLightning(attacker);
                        shootWitherSkull(attacker);
                    } else {
                        // 30%概率发动群体闪电攻击
                        summonLightningBurst(attacker);
                    }
                } else {
                    performMeleeAttack(attacker);
                    // 近战时有机会释放击退波
                    if (random.nextFloat() < 0.3f) {
                        performKnockbackWave();
                    }
                }
            } else {
                // 第二阶段只进行远程攻击
                summonLightning(attacker);
                shootWitherSkull(attacker);
            }
        }
        return hurt;
    }

    // 清理资源方法
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // 清理所有过渡期间的闪电
        if (!this.level.isClientSide) {
            for (LightningBolt lightning : transitionLightnings) {
                if (lightning.isAlive()) {
                    lightning.remove(RemovalReason.DISCARDED);
                }
            }
            transitionLightnings.clear();
        }
    }

    private void summonLightningBurst(LivingEntity target) {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            int burstCount = 5;
            double radius = 3.0D;

            for (int i = 0; i < burstCount; i++) {
                double angle = (2 * Math.PI * i) / burstCount;
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;

                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level);
                if (lightning != null) {
                    lightning.moveTo(target.getX() + xOffset, target.getY(), target.getZ() + zOffset, 0.0F, 0.0F);
                    serverLevel.addFreshEntity(lightning);
                }
            }
        }
    }

    private void performKnockbackWave() {
        if (!this.level.isClientSide) {
            AABB knockbackArea = this.getBoundingBox().inflate(5.0D);
            List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, knockbackArea);

            for (LivingEntity entity : entities) {
                if (entity != this) {
                    // 计算击退方向
                    double dx = entity.getX() - this.getX();
                    double dz = entity.getZ() - this.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance > 0) {
                        double multiplier = 2.0D;
                        entity.setDeltaMovement(
                                entity.getDeltaMovement().add(
                                        dx / distance * multiplier,
                                        0.5D,
                                        dz / distance * multiplier
                                )
                        );
                    }
                }
            }
        }
    }

    public void shootWitherSkull(LivingEntity target) {
        if (!this.level.isClientSide) {
            double d0 = this.getX();
            double d1 = this.getY();
            double d2 = this.getZ();
            double d3 = target.getX() - d0;
            double d4 = target.getY() + (double)target.getEyeHeight() * 0.5 - d1;
            double d5 = target.getZ() - d2;
            WitherSkull witherskull = new WitherSkull(this.level, this, d3, d4, d5);
            witherskull.setOwner(this);
            witherskull.setDangerous(true);

            witherskull.setPosRaw(d0, d1, d2);
            this.level.addFreshEntity(witherskull);

            // 第二阶段增加追踪能力
//            if (isInPhaseTwo) {
//                witherSkull.setDeltaMovement(
//                        witherSkull.getDeltaMovement().multiply(1.5D, 1.5D, 1.5D)
//                );
//            }

            // 添加发射音效
            this.playSound(
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("")),
                    1.0F,
                    1.0F
            );

            // 第二阶段额外效果
            if (isInPhaseTwo && target instanceof Player) {
                Player player = (Player) target;
                summonLightning(player);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
            }
        }
    }

    private void maintainPhaseTwo() {
        if (!this.level.isClientSide && isInPhaseTwo && this.getTarget() != null) {
            double HOVER_HEIGHT = 10.0D; // 飞行高度
            double ATTACK_RANGE = 6.0D;
            double MOVE_SPEED = 0.3D;

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
    }

    // 近战攻击方法
    private void performMeleeAttack(LivingEntity target) {
        this.doHurtTarget(target);
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
    }

    public static void init() {
        SpawnPlacements.register(ModEntities.KIND_MIAO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> (world.getBlockState(pos.below()).getMaterial() == Material.GRASS && world.getRawBrightness(pos, 0) > 8));
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.35);
        builder = builder.add(Attributes.MAX_HEALTH, 114514);
        builder = builder.add(Attributes.ARMOR, 100);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 20);
        builder = builder.add(Attributes.FOLLOW_RANGE, 100);
        builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 100);
        builder = builder.add(Attributes.ATTACK_KNOCKBACK, 1);
        return builder;
    }

    private void startPhaseTransition() {
        if (!this.level.isClientSide) {

            if (phaseTransitionTicks > 0) {
                return;
            }

            // 记录初始位置
            this.transitionStartY = this.getY();
            this.isTransitionComplete = false;

            // 开始相变
            phaseTransitionTicks = TRANSITION_DURATION;
            isInvulnerableDuringTransition = true;
            transitionLightnings.clear();

            // 播放开始音效
            this.level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                    SoundSource.HOSTILE,
                    3.0F,
                    1.0F
            );

            // 清除所有附近实体的目标
            AABB area = this.getBoundingBox().inflate(32.0D);
            nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity entity : nearbyEntities) {
                if (entity instanceof Mob) {
                    ((Mob) entity).setTarget(null);
                }
            }

            // 发送地震效果
            if (this.level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        10,  // 粒子数量
                        3.0, // X范围
                        3.0, // Y范围
                        3.0, // Z范围
                        0.5  // 速度
                );
            }
        }
    }

    private void handlePhaseTransition() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            // 无敌状态
            this.isInvulnerableDuringTransition = true;

            // 计算当前高度相对于起始位置的偏移
            double currentHeightOffset = this.getY() - this.transitionStartY;

            // 控制上升运动
            if (currentHeightOffset < MAX_TRANSITION_HEIGHT && !isTransitionComplete) {
                this.setNoGravity(true);
                this.setDeltaMovement(0, 0.05, 0);
            } else {
                // 达到最大高度后悬停
                this.setDeltaMovement(0, 0, 0);
                isTransitionComplete = true;
//                this.setHealth(this.getHealth() + 11451);
            }

            // 旋转的护盾粒子效果
            spawnShieldParticles(serverLevel);

            // 产生能量波纹
            if (phaseTransitionTicks % 20 == 0) {
                spawnEnergyRipple(serverLevel);
            }

            // 添加闪电效果
            if (phaseTransitionTicks % 40 == 0) {
                // 清理旧的闪电
                transitionLightnings.removeIf(lightning -> !lightning.isAlive());

                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 * i) / 4;
                    double radius = 3.0;
                    double x = this.getX() + Math.cos(angle) * radius;
                    double z = this.getZ() + Math.sin(angle) * radius;

                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level);
                    if (lightning != null) {
                        lightning.setPos(x, this.getY(), z);
                        lightning.setVisualOnly(true); // 设置为视觉效果，不造成伤害
                        serverLevel.addFreshEntity(lightning);
                        transitionLightnings.add(lightning); // 添加到列表中追踪
                    }
                }
            }

            // 在过渡即将结束时
            if (phaseTransitionTicks <= 20) {
                serverLevel.sendParticles(
                        ParticleTypes.EXPLOSION_EMITTER,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        1, 0, 0, 0, 0
                );

                if (phaseTransitionTicks <= 0) {
                    finishPhaseTransition();
                    return;
                }
            }

            phaseTransitionTicks--;
            shieldParticlesAngle += 10;
        }
    }




    private void spawnShieldParticles(ServerLevel serverLevel) {
        int particleCount = 36; // 每圈的粒子数
        double radius = 2.0;
        double yOffset = Math.sin(phaseTransitionTicks * 0.05) * 0.5; // 上下浮动效果

        // 水平环
        for (int i = 0; i < particleCount; i++) {
            double angle = ((Math.PI * 2 * i) / particleCount) + Math.toRadians(shieldParticlesAngle);
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x,
                    this.getY() + 1 + yOffset,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0.02
            );
        }

        // 垂直环
        for (int i = 0; i < particleCount; i++) {
            double angle = ((Math.PI * 2 * i) / particleCount) + Math.toRadians(shieldParticlesAngle);
            double y = this.getY() + Math.cos(angle) * radius;
            double x = this.getX() + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x,
                    y + 1,
                    this.getZ(),
                    1,
                    0,
                    0,
                    0,
                    0.02
            );
        }
    }

    private void spawnEnergyRipple(ServerLevel serverLevel) {
        double baseRadius = 0.5;
        int particleCount = 36;

        for (int ring = 0; ring < 3; ring++) {
            double radius = baseRadius + (ring * 0.5);
            for (int i = 0; i < particleCount; i++) {
                double angle = ((Math.PI * 2 * i) / particleCount);
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        x,
                        this.getY() + ring * 0.2,
                        z,
                        1,
                        0,
                        0,
                        0,
                        0.02
                );
            }
        }
    }

    private void finishPhaseTransition() {
        if (!this.level.isClientSide) {
            // 移除无敌状态
            isInvulnerableDuringTransition = false;

            // 重置相关状态
            phaseTransitionTicks = 0;
            isTransitionComplete = false;

            // 允许重力，但保持飞行能力
            this.setNoGravity(false);

            // 正式进入第二阶段
            enterPhaseTwo();

            for (LightningBolt lightning : transitionLightnings) {
                if (lightning.isAlive()) {
                    lightning.remove(RemovalReason.DISCARDED);
                }
            }
            transitionLightnings.clear();
        }

        // 播放完成音效
        this.level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("")),
                SoundSource.HOSTILE,
                3.0F,
                1.0F
        );

        if (this.level instanceof ServerLevel serverLevel) {
            // 爆炸粒子效果等...
            for (int i = 0; i < 20; i++) {
                double offsetX = random.nextGaussian() * 2;
                double offsetY = random.nextGaussian() * 2;
                double offsetZ = random.nextGaussian() * 2;

                serverLevel.sendParticles(
                        ParticleTypes.EXPLOSION_EMITTER,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }

        if (this.getTarget() != null) {
            this.setTarget(this.getTarget());
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 安全检查：如果实体超出世界高度限制，强制结束过渡
        if (this.getY() > this.level.getMaxBuildHeight() - 10) {
            if (phaseTransitionTicks > 0) {
                finishPhaseTransition();
            }
            this.setDeltaMovement(0, 0, 0);
            this.setPos(this.getX(), this.level.getMaxBuildHeight() - 10, this.getZ());
        }

//        if (level.getGameTime() % 20 == 0) shootWitherSkull(Minecraft.getInstance().player);

        if (!isInPhaseTwo && (this.getHealth() / this.getMaxHealth() <= 0.1f)) {
            startPhaseTransition();
            isInPhaseTwo = true;
        }

        // 如果正在进行相变动画
        if (phaseTransitionTicks > 0) {
            handlePhaseTransition();
        }

        if (!this.level.isClientSide) {
            // 确保血条始终同步
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (isInPhaseTwo) {
            maintainPhaseTwo();
            this.heal(11451);
            this.isInvulnerableDuringTransition = false;
        }

        tickCounter++;

        // 检查血量百分比
        float healthPercentage = this.getHealth() / this.getMaxHealth() * 100;

        // 血量低于30%时的特殊效果
        if (healthPercentage <= 30) {
            // 生成星星粒子效果
            createStarParticles();

            // 激活雷电圈（如果还没激活）
            if (!isLightningCircleActive) {
                createLightningCircle();
                isLightningCircleActive = true;
            }

            // 维护雷电圈效果
            if (isLightningCircleActive) {
                manageLightningCircle();
            }
        }

        // 检查玩家状态
        if (isLightningCircleActive && tickCounter % 20 == 0) { // 每秒检查一次
            checkPlayersAndDisappear();
        }
    }

    private void checkPlayersAndDisappear() {
        if (!this.level.isClientSide) {
            // 创建检查区域
            AABB checkArea = new AABB(
                    circleOriginX - LIGHTNING_CIRCLE_RADIUS - 10,
                    circleOriginY - 10,
                    circleOriginZ - LIGHTNING_CIRCLE_RADIUS - 10,
                    circleOriginX + LIGHTNING_CIRCLE_RADIUS + 10,
                    circleOriginY + 10,
                    circleOriginZ + LIGHTNING_CIRCLE_RADIUS + 10
            );

            // 获取范围内的所有玩家
            List<Player> players = this.level.getEntitiesOfClass(Player.class, checkArea);

            if (!players.isEmpty()) {
                boolean allPlayersDead = true;
                for (Player player : players) {
                    // 检查玩家是否还活着
                    if (player.isAlive() && !player.isSpectator()) {
                        allPlayersDead = false;
                        break;
                    }
                }

                // 如果所有玩家都被打败
                if (allPlayersDead) {
                    // 播放消失效果
                    playDisappearEffects();
                    // 移除boss
                    this.remove(RemovalReason.DISCARDED);
                }
            }
        }
    }

    private void playDisappearEffects() {
        if (this.level instanceof ServerLevel serverLevel) {
            // 播放音效
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")),
                    SoundSource.HOSTILE,
                    1.0F,
                    1.0F
            );

            // 创建粒子效果
            for (int i = 0; i < 100; i++) {
                double radius = random.nextDouble() * 2.0;
                double angle = random.nextDouble() * Math.PI * 2;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        this.getX() + offsetX,
                        this.getY() + random.nextDouble() * 2.0,
                        this.getZ() + offsetZ,
                        1, 0, 0, 0, 0.15
                );
            }

            // 产生一圈紫色粒子
            for (int i = 0; i < 360; i += 5) {
                double angle = Math.toRadians(i);
                double radius = 3.0;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        this.getX() + offsetX,
                        this.getY() + 1,
                        this.getZ() + offsetZ,
                        1, 0, 0, 0, 0.05
                );
            }
        }
    }

    private void createStarParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            // 每tick生成多个星星粒子
            for (int i = 0; i < 5; i++) {
                double offsetX = random.nextGaussian() * 2;
                double offsetY = random.nextGaussian() * 2;
                double offsetZ = random.nextGaussian() * 2;

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x + offsetX, y + 1 + offsetY, z + offsetZ,
                        1, 0, 0, 0, 0.05);
            }
        }
    }

    private void createLightningCircle() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            lightningCircle.clear();
            particlePositions.clear();

            this.circleOriginX = this.getX();
            this.circleOriginY = this.getY();
            this.circleOriginZ = this.getZ();

            // 创建圆形雷电和记录粒子位置
            for (int i = 0; i < LIGHTNING_CIRCLE_POINTS; i++) {
                double angle = (2 * Math.PI * i) / LIGHTNING_CIRCLE_POINTS;
                double xOffset = Math.cos(angle) * LIGHTNING_CIRCLE_RADIUS;
                double zOffset = Math.sin(angle) * LIGHTNING_CIRCLE_RADIUS;

                // 生成闪电
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightning != null) {
                    double lightningX = circleOriginX + xOffset;
                    double lightningZ = circleOriginZ + zOffset;
                    lightning.setPos(lightningX, circleOriginY, lightningZ);
                    lightning.setVisualOnly(true);
                    serverLevel.addFreshEntity(lightning);
                    lightningCircle.add(lightning);

                    // 记录闪电位置用于粒子效果
                    particlePositions.add(new Vec3(lightningX, circleOriginY, lightningZ));

                    // 在闪电之间添加额外的粒子点，使边界更连续
                    if (i > 0) {
                        Vec3 prevPos = particlePositions.get(particlePositions.size() - 2);
                        Vec3 currentPos = particlePositions.get(particlePositions.size() - 1);

                        // 在两个闪电之间添加额外的粒子点
                        for (int j = 1; j < 5; j++) {
                            double ratio = j / 5.0;
                            double interpolatedX = prevPos.x + (currentPos.x - prevPos.x) * ratio;
                            double interpolatedZ = prevPos.z + (currentPos.z - prevPos.z) * ratio;
                            particlePositions.add(new Vec3(interpolatedX, circleOriginY, interpolatedZ));
                        }
                    }
                }
            }

            // 连接最后一个点和第一个点
            if (!particlePositions.isEmpty()) {
                Vec3 firstPos = particlePositions.get(0);
                Vec3 lastPos = particlePositions.get(particlePositions.size() - 1);
                for (int j = 1; j < 5; j++) {
                    double ratio = j / 5.0;
                    double interpolatedX = lastPos.x + (firstPos.x - lastPos.x) * ratio;
                    double interpolatedZ = lastPos.z + (firstPos.z - lastPos.z) * ratio;
                    particlePositions.add(new Vec3(interpolatedX, circleOriginY, interpolatedZ));
                }
            }
        }
    }

    private void spawnBoundaryParticles() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            // 计算当前时间的正弦值，用于创造波动效果
            double timeWave = Math.sin(tickCounter * 0.1) * 0.5 + 0.5;

            for (Vec3 pos : particlePositions) {
                // 基础位置随时间轻微波动
                double yOffset = timeWave * 0.3;

                // 1. 基础边界粒子 - 灵魂火焰
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x,
                        pos.y + 1 + yOffset,
                        pos.z,
                        1,
                        0.05,
                        0.1,
                        0.05,
                        0.01
                );

                // 2. 星星效果 - 使用END_ROD，根据时间波动调整生成概率
                if (random.nextFloat() < 0.2 * timeWave) {
                    // 计算螺旋上升路径
                    double angle = tickCounter * 0.1 + random.nextFloat() * Math.PI * 2;
                    double spiralRadius = 0.3;
                    double spiralX = pos.x + Math.cos(angle) * spiralRadius;
                    double spiralZ = pos.z + Math.sin(angle) * spiralRadius;

                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            spiralX,
                            pos.y + random.nextFloat() * 3, // 随机高度
                            spiralZ,
                            1,
                            0,    // 固定X位置
                            2, // 缓慢上升
                            0,    // 固定Z位置
                            0
                    );
                }

                // 3. 间隔性闪烁效果 - 使用小型末地光束制造点缀
                if (tickCounter % 40 == 0 && random.nextFloat() < 0.1) { // 每2秒的低概率事件
                    for (int i = 0; i < 3; i++) { // 每个位置生成3个粒子形成一个小光束
                        serverLevel.sendParticles(
                                ParticleTypes.END_ROD,
                                pos.x,
                                pos.y + i * 0.5,
                                pos.z,
                                1,
                                0.02,
                                0,
                                0.02,
                                0.02
                        );
                    }
                }
            }
        }
    }

    private void spawnBeaconBeams(ServerLevel serverLevel) {
        // 在四个基本方向生成信标光柱
        double[][] directions = {
                {1, 0},   // 东
                {0, 1},   // 北
                {-1, 0},  // 西
                {0, -1},  // 南
                {1,-1},   // 东南
                {-1, -1}, //西南
                {1, 1},  //东北
                {-1, 1}, //西北
        };

        for (double[] dir : directions) {
            double beamX = circleOriginX + dir[0] * LIGHTNING_CIRCLE_RADIUS;
            double beamZ = circleOriginZ + dir[1] * LIGHTNING_CIRCLE_RADIUS;

            // 生成信标光柱核心 - 使用白色粒子
            for (int y = 0; y < 256; y++) {
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        beamX,
                        circleOriginY + y,
                        beamZ,
                        1,
                        0,
                        0,
                        0,
                        0
                );

                // 添加外围光晕效果
                if (y % 4 == 0) { // 每隔4格添加一次光晕
                    double radius = 0.2;
                    for (int i = 0; i < 4; i++) {
                        double angle = i * Math.PI / 2;
                        serverLevel.sendParticles(
                                ParticleTypes.PORTAL,
                                beamX + Math.cos(angle) * radius,
                                circleOriginY + y,
                                beamZ + Math.sin(angle) * radius,
                                1,
                                0,
                                0,
                                0,
                                0
                        );
                    }
                }
            }
        }
    }

    private void manageLightningCircle() {
        if (!this.level.isClientSide) {

            double bossDistanceFromCenter = Math.sqrt(
                    Math.pow(this.getX() - circleOriginX, 2) +
                            Math.pow(this.getZ() - circleOriginZ, 2)
            );

            if (bossDistanceFromCenter > LIGHTNING_CIRCLE_RADIUS - 1.0) {
                // 计算角度
                double angle = Math.atan2(this.getZ() - circleOriginZ, this.getX() - circleOriginX);
                // 将boss推回圈内
                this.setPos(
                        circleOriginX + Math.cos(angle) * (LIGHTNING_CIRCLE_RADIUS - 1.0),
                        this.getY(),
                        circleOriginZ + Math.sin(angle) * (LIGHTNING_CIRCLE_RADIUS - 1.0)
                );
                // 清除水平移动速度
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            }

            if (this.level instanceof ServerLevel serverLevel) {
                spawnBeaconBeams(serverLevel);
            }

            // 更新粒子效果
            if (tickCounter % 5 == 0) { // 每5tick生成一次粒子
                spawnBoundaryParticles();
            }

            // 每20tick检查一次玩家
            if (tickCounter % 20 == 0) {
                AABB checkArea = new AABB(
                        circleOriginX - LIGHTNING_CIRCLE_RADIUS - 10,
                        circleOriginY - 10,
                        circleOriginZ - LIGHTNING_CIRCLE_RADIUS - 10,
                        circleOriginX + LIGHTNING_CIRCLE_RADIUS + 10,
                        circleOriginY + 10,
                        circleOriginZ + LIGHTNING_CIRCLE_RADIUS + 10
                );


                List<Player> players = this.level.getEntitiesOfClass(Player.class, checkArea);
                for (Player player : players) {
                    // 计算玩家到圈子中心的距离
                    double playerDistanceSquared = (player.getX() - circleOriginX) * (player.getX() - circleOriginX) +
                            (player.getZ() - circleOriginZ) * (player.getZ() - circleOriginZ);
                    double radiusSquared = LIGHTNING_CIRCLE_RADIUS * LIGHTNING_CIRCLE_RADIUS;

                    if (playerDistanceSquared <= radiusSquared) {
                        // 在范围内的玩家受到伤害
                        player.hurt(DamageSource.LIGHTNING_BOLT, 10.0F);
                        this.setTarget(player);
                    } else {
                        // 超出范围的玩家
                        if (player.getAbilities().instabuild) {
                            // 创造模式玩家直接死亡
                            player.kill();
                        } else {
                            // 将生存模式玩家推回圈内
                            double angle = Math.atan2(player.getZ() - circleOriginZ, player.getX() - circleOriginX);
                            player.teleportTo(
                                    circleOriginX + Math.cos(angle) * (LIGHTNING_CIRCLE_RADIUS - 1.0),
                                    player.getY(),
                                    circleOriginZ + Math.sin(angle) * (LIGHTNING_CIRCLE_RADIUS - 1.0)
                            );
                            player.hurt(DamageSource.MAGIC, 20.0F);
                        }
                    }
                }

                // 清除范围内的其他实体（除了boss自己和凋零骷髅头）
                List<Entity> entities = this.level.getEntities(this, checkArea);
                for (Entity entity : entities) {
                    if (entity instanceof WitherSkull || entity == this) {
                        continue;
                    }
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        entity.remove(RemovalReason.KILLED);
                    }
                }
            }

            // 维护雷电圈效果
            if (tickCounter % 40 == 0) { // 每2秒刷新一次雷电
                for (LightningBolt lightning : lightningCircle) {
                    if (!lightning.isAlive()) {
                        LightningBolt newLightning = EntityType.LIGHTNING_BOLT.create(this.level);
                        if (newLightning != null) {
                            newLightning.setPos(lightning.getX(), lightning.getY(), lightning.getZ());
                            newLightning.setVisualOnly(true);
                            this.level.addFreshEntity(newLightning);
                        }
                    }
                }
            }

            // 粒子效果显示边界
            if (tickCounter % 5 == 0) { // 每5tick生成一次粒子
                for (int i = 0; i < LIGHTNING_CIRCLE_POINTS; i++) {
                    double angle = (2 * Math.PI * i) / LIGHTNING_CIRCLE_POINTS;
                    double xOffset = Math.cos(angle) * LIGHTNING_CIRCLE_RADIUS;
                    double zOffset = Math.sin(angle) * LIGHTNING_CIRCLE_RADIUS;

                    ((ServerLevel) this.level).sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            circleOriginX + xOffset,
                            circleOriginY + 1,
                            circleOriginZ + zOffset,
                            1, 0, 0, 0, 0
                    );
                }
            }
        }
    }
}