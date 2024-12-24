package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.WitherSkullAttackGoal;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.levelgen.Heightmap;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LightningBolt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber
public class MiaoMiaoEntity extends Monster {


    private boolean isInPhaseTwo = false;
    private static final double RANGED_ATTACK_RADIUS = 5.0D;
    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.PINK, ServerBossEvent.BossBarOverlay.PROGRESS);
    private final List<Vec3> particlePositions = new ArrayList<>();
    private boolean isLightningCircleActive = false;
    private final List<LightningBolt> lightningCircle = new ArrayList<>();
    private static final double LIGHTNING_CIRCLE_RADIUS = 50.0D;
    private static final int LIGHTNING_CIRCLE_POINTS = 36;
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
    public @NotNull SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow"));
    }

    private void enterPhaseTwo() {
        isInPhaseTwo = true;
        // 启用飞行能力
        this.setNoGravity(true);
        // 提高移动速度
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.5);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 免疫爆炸和摔落伤害
        if (source.isExplosion() || source == DamageSource.FALL) {
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
            WitherSkull witherSkull = new WitherSkull(EntityType.WITHER_SKULL, this.level) {
                @Override
                protected void onHit(@NotNull HitResult result) {
                    if (!this.level.isClientSide) {
                        // 创建不破坏方块的爆炸
                        this.level.explode(
                                this,                      // 爆炸源实体
                                this.getX(),              // X 坐标
                                this.getY(),              // Y 坐标
                                this.getZ(),              // Z 坐标
                                1.0F,                     // 爆炸半径
                                true,                    // 是否生成火焰
                                Explosion.BlockInteraction.NONE  // 不破坏方块
                        );
                    }
                    super.onHit(result);
                }
            };
            witherSkull.setOwner(this);

            // 计算方向向量
            double dX = target.getX() - this.getX();
            double dY = target.getY(0.5D) - witherSkull.getY();
            double dZ = target.getZ() - this.getZ();

            // 计算距离并归一化方向
            double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
            dX = dX / distance;
            dY = dY / distance;
            dZ = dZ / distance;

            // 根据阶段调整精确度和速度
            double spread = isInPhaseTwo ? 0.05D : 0.1D; // 第二阶段更精确
            float speed = isInPhaseTwo ? 4.0F : 2.0F;    // 第二阶段更快
            float inaccuracy = isInPhaseTwo ? 0.5F : 1.0F;

            // 添加随机偏移
            dX += this.random.nextGaussian() * spread;
            dY += this.random.nextGaussian() * spread;
            dZ += this.random.nextGaussian() * spread;

            witherSkull.setPos(
                    this.getX() + dX * 2.0D,
                    this.getY(0.5D) + 0.5D,
                    this.getZ() + dZ * 2.0D
            );

            witherSkull.shoot(dX, dY, dZ, speed, inaccuracy);

            // 第二阶段增加追踪能力
            if (isInPhaseTwo) {
                witherSkull.setDeltaMovement(
                        witherSkull.getDeltaMovement().multiply(1.5D, 1.5D, 1.5D)
                );
            }

            this.level.addFreshEntity(witherSkull);

            // 添加发射音效
            this.playSound(
                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
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
                this.tickCount += 20; // 快速改变角度
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

    @Override
    public void tick() {
        super.tick();

        if (!this.level.isClientSide) {
            // 确保血条始终同步
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (isInPhaseTwo) {
            maintainPhaseTwo();
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
                {0, -1}   // 南
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