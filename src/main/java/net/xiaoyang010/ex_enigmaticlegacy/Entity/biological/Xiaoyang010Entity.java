
package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.AbstractArrow;
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
import net.minecraft.core.BlockPos;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.List;

@Mod.EventBusSubscriber
public class Xiaoyang010Entity extends Monster {
	private long lastHurtSoundTime = 0;
	private int attackCooldown = 0;
	private int specialAttackTimer = 0;
	private int phaseChangeCooldown = 200; // 10秒相位转换冷却
	private int currentPhase = 1;
	private boolean isInvulnerablePhase = false;
	private int teleportCooldown = 0;
	private int summonCooldown = 0;
	private int immunityTicks = 0;
	private static final int IMMUNITY_DURATION = 20; // 1秒无敌时间
	private static final float MAX_DAMAGE_PER_HIT = 100.0F; // 单次最大伤害
	private static final float HEALTH_THRESHOLD = 0.1F; // 10%生命值触发强化
	private long lastDamageTime = 0;
	private static final long DAMAGE_COOLDOWN = 500; // 0.5秒最小伤害间隔

	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.BLUE, ServerBossEvent.BossBarOverlay.PROGRESS);

	public Xiaoyang010Entity(PlayMessages.SpawnEntity packet, Level world) {
		this(ModEntities.XIAOYANG_010.get(), world);
	}

	public Xiaoyang010Entity(EntityType<Xiaoyang010Entity> type, Level world) {
		super(type, world);
		maxUpStep = 0.6f;
		xpReward = 10000;
		setNoAi(false);
		setCustomName(new TextComponent("§c§l§oxiaoyang"));
		setCustomNameVisible(true);
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KILLYOU.get()));
		this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
	}

	@Override
	public void tick() {
		super.tick();

		if (immunityTicks > 0) {
			immunityTicks--;
		}

		if (this.getHealth() < this.getMaxHealth() * HEALTH_THRESHOLD) {
			this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 2));
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1));
			this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
		}

		if (!level.isClientSide) {
			// 冷却计时器
			if (attackCooldown > 0) attackCooldown--;
			if (specialAttackTimer > 0) specialAttackTimer--;
			if (teleportCooldown > 0) teleportCooldown--;
			if (summonCooldown > 0) summonCooldown--;
			if (phaseChangeCooldown > 0) phaseChangeCooldown--;

			// 生命值低于30%时进入狂暴状态
			float healthPercentage = this.getHealth() / this.getMaxHealth();
			if (healthPercentage < 0.3f && currentPhase == 1 && phaseChangeCooldown <= 0) {
				enterBerserkPhase();
			}

			// 特殊技能释放
			if (this.getTarget() != null) {
				// 闪现突袭
				if (teleportCooldown <= 0 && random.nextFloat() < 0.1f) {
					performTeleportAttack();
				}

				// 召唤雷暴
				if (attackCooldown <= 0 && random.nextFloat() < 0.15f) {
					summonLightningStorm();
				}

				// 召唤分身
				if (summonCooldown <= 0 && healthPercentage < 0.5f) {
					summonClones();
				}

				// 二阶段特殊技能
				if (currentPhase == 2) {
					executePhaseTwo();
				}
			}
		}
	}

	private void enterBerserkPhase() {
		currentPhase = 2;
		phaseChangeCooldown = 200;
		isInvulnerablePhase = true;

		// 特效和增益
		this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 3));
		this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 2));
		this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400, 1));

		// 播放特效
		if (level instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 50; i++) {
				double offsetX = random.nextDouble() * 4 - 2;
				double offsetY = random.nextDouble() * 4;
				double offsetZ = random.nextDouble() * 4 - 2;
				serverLevel.sendParticles(ParticleTypes.EXPLOSION,
						this.getX() + offsetX,
						this.getY() + offsetY,
						this.getZ() + offsetZ,
						1, 0, 0, 0, 0);
			}
		}

		// 对周围玩家造成伤害和击退
		AABB box = this.getBoundingBox().inflate(8.0D);
		List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity entity : nearbyEntities) {
			if (entity instanceof Player) {
				entity.hurt(DamageSource.mobAttack(this), 20.0F);
				double dx = entity.getX() - this.getX();
				double dz = entity.getZ() - this.getZ();
				entity.setDeltaMovement(dx * 0.5, 1.0, dz * 0.5);
			}
		}
	}

	private void executePhaseTwo() {
		if (this.getTarget() != null && specialAttackTimer <= 0) {
			int attackPattern = random.nextInt(4);
			switch (attackPattern) {
				case 0:
					executeFireballBarrage();
					break;
				case 1:
					executeWitherStorm();
					break;
				case 2:
					executeLightningCircle();
					break;
				case 3:
					executeExplosiveCharge();
					break;
			}
			specialAttackTimer = 60;
		}
	}

	private void executeFireballBarrage() {
		LivingEntity target = this.getTarget();
		if (target != null) {
			for (int i = 0; i < 5; i++) {
				SmallFireball fireball = new SmallFireball(level, this,
						target.getX() - this.getX() + random.nextDouble() - 0.5,
						target.getY() - this.getY() + 0.5,
						target.getZ() - this.getZ() + random.nextDouble() - 0.5);
				level.addFreshEntity(fireball);
			}
		}
	}

	private void executeWitherStorm() {
		if (level instanceof ServerLevel serverLevel && this.getTarget() != null) {
			LivingEntity target = this.getTarget();

			for (int i = 0; i < 8; i++) {
				double angle = (Math.PI * 2 * i) / 8;
				double radius = 3.0;
				double x = this.getX() + Math.cos(angle) * radius;
				double z = this.getZ() + Math.sin(angle) * radius;

				WitherSkull skull = new WitherSkull(level, this,
						target.getX() - x,
						target.getY() + target.getEyeHeight() * 0.5 - (this.getY() + 2),
						target.getZ() - z);

				skull.setPos(x, this.getY() + 2, z);
				skull.setDangerous(true);

				double speed = 0.95;
				double inaccuracy = 1.0;
				skull.shoot(target.getX() - x,
						target.getY() + target.getEyeHeight() * 0.5 - (this.getY() + 2),
						target.getZ() - z,
						(float)speed,
						(float)inaccuracy);

				level.addFreshEntity(skull);
			}

			this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.WITHER_SHOOT,
					SoundSource.HOSTILE, 1.0F, 1.0F);
		}
	}

	private void executeLightningCircle() {
		if (level instanceof ServerLevel serverLevel) {
			double radius = 5.0;
			for (int i = 0; i < 8; i++) {
				double angle = (Math.PI * 2 * i) / 8;
				double x = this.getX() + Math.cos(angle) * radius;
				double z = this.getZ() + Math.sin(angle) * radius;
				LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
				if (lightning != null) {
					lightning.moveTo(x, this.getY(), z);
					serverLevel.addFreshEntity(lightning);
				}
			}
		}
	}

	private void executeExplosiveCharge() {
		if (this.getTarget() != null) {
			Vec3 direction = this.getTarget().position().subtract(this.position()).normalize();
			this.setDeltaMovement(direction.x * 2, 0.4, direction.z * 2);

			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.EXPLOSION,
						this.getX(), this.getY(), this.getZ(),
						10, 0.5, 0.5, 0.5, 0);

				AABB explosionBox = this.getBoundingBox().inflate(3.0D);
				List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, explosionBox);
				for (LivingEntity victim : victims) {
					if (victim != this) {
						victim.hurt(DamageSource.mobAttack(this), 25.0F);
						victim.setDeltaMovement(0, 1.0, 0);
					}
				}
			}
		}
	}

	private void performTeleportAttack() {
		if (this.getTarget() != null) {
			// 记录原始位置用于特效
			double oldX = this.getX();
			double oldY = this.getY();
			double oldZ = this.getZ();

			// 在目标背后随机位置闪现
			double distance = 2.0;
			double angle = this.getTarget().getYRot() + 180 + (random.nextDouble() - 0.5) * 60;
			double x = this.getTarget().getX() - Math.sin(Math.toRadians(angle)) * distance;
			double z = this.getTarget().getZ() + Math.cos(Math.toRadians(angle)) * distance;

			// 传送前的特效
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.PORTAL,
						oldX, oldY, oldZ,
						20, 0.5, 0.5, 0.5, 0.1);

				// 播放末影人传送音效
				this.level.playSound(null, oldX, oldY, oldZ,
						SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.HOSTILE, 1.0F, 1.0F);
			}

			// 执行传送
			this.moveTo(x, this.getTarget().getY(), z);

			// 传送后的特效
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.PORTAL,
						this.getX(), this.getY(), this.getZ(),
						20, 0.5, 0.5, 0.5, 0.1);
				serverLevel.sendParticles(ParticleTypes.EXPLOSION,
						this.getX(), this.getY(), this.getZ(),
						5, 0.2, 0.2, 0.2, 0);

				// 播放到达音效
				this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
						SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.HOSTILE, 1.0F, 1.0F);
			}

			// 造成伤害
			this.getTarget().hurt(DamageSource.mobAttack(this), 15.0F);

			// 设置冷却
			teleportCooldown = 50;
		}
	}

	private void summonLightningStorm() {
		if (level instanceof ServerLevel serverLevel) {
			AABB box = this.getBoundingBox().inflate(10.0D);
			List<Player> players = level.getEntitiesOfClass(Player.class, box);

			for (Player player : players) {
				LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
				if (lightning != null) {
					lightning.moveTo(player.position());
					serverLevel.addFreshEntity(lightning);
					player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
				}
			}
			attackCooldown = 100;
		}
	}

	private void summonClones() {
		if (level instanceof ServerLevel serverLevel) {
			int cloneCount = 2 + random.nextInt(2);
			for (int i = 0; i < cloneCount; i++) {
				double offsetX = (random.nextDouble() - 0.5) * 6;
				double offsetZ = (random.nextDouble() - 0.5) * 6;

				CloneEntity clone = new CloneEntity(ModEntities.CLONE_ENTITY.get(), level);
				clone.moveTo(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);

				// 给分身添加发光效果
				clone.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));

				// 如果有目标，分身继承目标
				if (this.getTarget() != null) {
					clone.setTarget(this.getTarget());
				}

				// 生成特效
				serverLevel.sendParticles(ParticleTypes.PORTAL,
						clone.getX(), clone.getY(), clone.getZ(),
						30, 0.5, 1.0, 0.5, 0.1);

				serverLevel.playSound(null, clone.getX(), clone.getY(), clone.getZ(),
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);

				serverLevel.addFreshEntity(clone);
			}
			summonCooldown = 200;
		}
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

	public void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
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
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastHurtSoundTime > 3000) {
			lastHurtSoundTime = currentTime;
			return ForgeRegistries.SOUND_EVENTS.getValue(
					new ResourceLocation("ex_enigmaticlegacy:nothing"));
		}
		return null;
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow"));
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// 原有的伤害免疫
		if (source.getDirectEntity() instanceof AbstractArrow)
			return false;
		if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud)
			return false;
		if (source == DamageSource.FALL)
			return false;
		if (source == DamageSource.CACTUS)
			return false;
		if (source == DamageSource.DROWN)
			return false;
		if (source == DamageSource.LIGHTNING_BOLT)
			return false;
		if (source.isExplosion())
			return false;
		if (source.getMsgId().equals("trident"))
			return false;
		if (source == DamageSource.ANVIL)
			return false;
		if (source == DamageSource.DRAGON_BREATH)
			return false;
		if (source == DamageSource.WITHER)
			return false;
		if (source.getMsgId().equals("witherSkull"))
			return false;

		if (immunityTicks > 0) {
			return false;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastDamageTime < DAMAGE_COOLDOWN) {
			return false;
		}
		lastDamageTime = currentTime;

		float actualDamage = Math.min(amount, MAX_DAMAGE_PER_HIT);

		float healthPercentage = this.getHealth() / this.getMaxHealth();

		if (healthPercentage < 0.3f) {
			actualDamage *= 0.5f;
		} else if (healthPercentage < 0.6f) {
			actualDamage *= 0.7f;
		}

		immunityTicks = IMMUNITY_DURATION;

		if (this.getHealth() - actualDamage < this.getMaxHealth() * 0.3 && random.nextFloat() < 0.3f) {
			this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
			this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
			if (!level.isClientSide) {
				AABB box = this.getBoundingBox().inflate(5.0D, 2.0D, 5.0D);
				List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
				for (LivingEntity entity : entities) {
					if (entity != this) {
						entity.hurt(DamageSource.mobAttack(this), 10.0F);
						double dx = entity.getX() - this.getX();
						double dz = entity.getZ() - this.getZ();
						double magnitude = Math.sqrt(dx * dx + dz * dz);
						if (magnitude != 0) {
							entity.setDeltaMovement(dx / magnitude * 2, 0.5, dz / magnitude * 2);
						}
					}
				}
			}
		}

		return super.hurt(source, actualDamage);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.tickCount % 20 == 0) {
			this.heal(0.5F);
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

	@Override
	public void customServerAiStep() {
		super.customServerAiStep();
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}

	public static void init() {
		SpawnPlacements.register(ModEntities.XIAOYANG_010.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getBlockState(pos.below()).getMaterial() == Material.GRASS && world.getRawBrightness(pos, 0) > 8));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.35)
				.add(Attributes.MAX_HEALTH, 10000)
				.add(Attributes.ARMOR, 150)
				.add(Attributes.ATTACK_DAMAGE, 400)
				.add(Attributes.FOLLOW_RANGE, 120)
				.add(Attributes.KNOCKBACK_RESISTANCE, 100)
				.add(Attributes.ATTACK_KNOCKBACK, 6)
				.add(Attributes.ARMOR_TOUGHNESS, 50);
	}
}
