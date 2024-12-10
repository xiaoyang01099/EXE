package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.EffectCut;

import java.util.List;

public class EntitySlash extends Entity {
	private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(EntitySlash.class, EntityDataSerializers.INT);
	private Player player = null;

	public EntitySlash(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void tick() {
		if (this.player == null || !this.player.isAlive()) {
			this.discard();
			return;
		}

		if (getLifetime() % 2 == 0 && !level.isClientSide) {
			// 计算斩击效果的位置
			float yaw = -90.0F - this.player.getYRot();
			float offX = 0.5F * (float)Math.sin(Math.toRadians(yaw));
			float offZ = 0.5F * (float)Math.cos(Math.toRadians(yaw));

			Vec3 lookVec = this.player.getViewVector(1.0F);
			double x1 = this.player.getX() + lookVec.x * 0.5 + offX;
			double y1 = this.player.getY() + lookVec.y * 0.5 + this.player.getEyeHeight();
			double z1 = this.player.getZ() + lookVec.z * 0.5 + offZ;

			double x2 = this.player.getX() + lookVec.x * 4.0;
			double y2 = this.player.getY() + this.player.getEyeHeight() + lookVec.y * 4.0;
			double z2 = this.player.getZ() + lookVec.z * 4.0;

			// 更新实体位置
			this.setPos(this.player.getX(), this.player.getY(), this.player.getZ());

			// 创建斩击效果
			/*EffectSlash slash = new EffectSlash(x1, y1, z1)
					.setSlashProperties(
							this.player.getYRot(),
							this.player.getXRot(),
							30.0F + level.random.nextFloat() * 120.0F
					)
					.setColor(0.35F, 0.35F, 1.0F, 1.0F);

			// 发送效果包到客户端
			PacketHandler.sendToAll(new MessageEffect(FXHandler.FX_SLASH, slash));*/

			// 检测命中实体
			double lx = this.player.getX() + lookVec.x * 2.0;
			double ly = this.player.getY() + this.player.getEyeHeight() + lookVec.y * 2.0;
			double lz = this.player.getZ() + lookVec.z * 2.0;

			AABB hitBox = new AABB(
					lx - 2.0, ly - 2.0, lz - 2.0,
					lx + 2.0, ly + 2.0, lz + 2.0
			);

			List<LivingEntity> entities = level.getEntitiesOfClass(
					LivingEntity.class,
					hitBox,
					entity -> entity != player
			);

			for (LivingEntity target : entities) {
				target.invulnerableTime = 0;
				target.setLastHurtByMob(player);

				if (target.getHealth() > 0.0F) {
					// 创建命中效果
					EffectCut cut = new EffectCut(
							target.getX(),
							target.getY() + target.getBbHeight() / 2.0F,
							target.getZ()
					)
							.setSlashProperties(
									player.getYRot(),
									player.getXRot(),
									level.random.nextFloat() * 360.0F
							)
							.setColor(0.35F, 0.35F, 1.0F, 1.0F);

					// 发送效果包到客户端
					/*PacketHandler.sendToAll(new MessageEffect(RegistryManager.FX_CUT, cut));*/
				}

				target.hurt(DamageSource.playerAttack(player), 6.0F);
			}
		}

		// 更新生命时间
		setLifetime(getLifetime() - 1);
		if (getLifetime() <= 0) {
			this.discard();
		}
	}

	private int getLifetime() {
		return this.entityData.get(LIFETIME);
	}

	private void setLifetime(int value) {
		this.entityData.set(LIFETIME, value);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(LIFETIME, 12);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		this.discard();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		// 不需要保存任何数据
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}