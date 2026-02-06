package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.ICursedManaBurst;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.ICursedManaReceiver;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.ICursedManaSpreader;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * 诅咒魔力脉冲实体
 */
public class EntityCursedManaBurst extends ThrowableProjectile implements ICursedManaBurst {
    private static final String TAG_TICKS_EXISTED = "ticksExisted";
    private static final String TAG_COLOR = "color";
    private static final String TAG_MANA = "cursedMana";
    private static final String TAG_STARTING_MANA = "startingCursedMana";
    private static final String TAG_MIN_MANA_LOSS = "minCursedManaLoss";
    private static final String TAG_TICK_MANA_LOSS = "cursedManaLossTick";
    private static final String TAG_SPREADER_X = "spreaderX";
    private static final String TAG_SPREADER_Y = "spreaderY";
    private static final String TAG_SPREADER_Z = "spreaderZ";
    private static final String TAG_GRAVITY = "gravity";
    private static final String TAG_LENS_STACK = "lensStack";
    private static final String TAG_HAS_SHOOTER = "hasShooter";
    private static final String TAG_SHOOTER = "shooterUUID";
    private static final String TAG_LAST_COLLISION_X = "lastCollisionX";
    private static final String TAG_LAST_COLLISION_Y = "lastCollisionY";
    private static final String TAG_LAST_COLLISION_Z = "lastCollisionZ";
    private static final String TAG_WARPED = "warped";
    private static final String TAG_LEFT_SOURCE = "leftSource";
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MANA = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START_MANA = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MIN_MANA_LOSS = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MANA_LOSS_PER_TICK = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<BlockPos> SOURCE_COORDS = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<ItemStack> SOURCE_LENS = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> LEFT_SOURCE_POS = SynchedEntityData.defineId(EntityCursedManaBurst.class, EntityDataSerializers.BOOLEAN);
    private float accumulatedManaLoss = 0;
    private boolean fake = false;
    private final Set<BlockPos> alreadyCollidedAt = new HashSet<>();
    private boolean fullManaLastTick = true;
    private UUID shooterIdentity = null;
    private int _ticksExisted = 0;
    private BlockPos lastCollision;
    private boolean warped = false;
    private static final int SPIRAL_PARTICLE_COUNT = 8; // 每次生成的螺旋粒子数量
    private static final double SPIRAL_RADIUS = 0.3; // 螺旋半径
    private static final double SPIRAL_ROTATION_SPEED = 0.5; // 螺旋旋转速度
    private static final int SPIRAL_PARTICLE_INTERVAL = 1; // 每 N tick 生成一次螺旋粒子
    private ICursedManaReceiver collidedTile = null;
    private boolean noParticles = false;
    private boolean scanBeam = false;

    public EntityCursedManaBurst(EntityType<? extends EntityCursedManaBurst> type, Level world) {
        super(type, world);
    }

    public EntityCursedManaBurst(EntityType<? extends EntityCursedManaBurst> type, Level level, BlockPos pos, float rotX, float rotY) {
        this(type, level);
        setBurstSourceCoords(pos);
        moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        setYRot(-(rotX + 90F));
        setXRot(rotY);
        setDeltaMovement(calculateBurstVelocity(getXRot(), getYRot()));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, 0x8B00FF);
        entityData.define(MANA, 0);
        entityData.define(START_MANA, 0);
        entityData.define(MIN_MANA_LOSS, 0);
        entityData.define(MANA_LOSS_PER_TICK, 0F);
        entityData.define(GRAVITY, 0F);
        entityData.define(SOURCE_COORDS, BlockPos.ZERO);
        entityData.define(SOURCE_LENS, ItemStack.EMPTY);
        entityData.define(LEFT_SOURCE_POS, false);
    }

    public static Vec3 calculateBurstVelocity(float xRot, float yRot) {
        float f = 0.4F;
        double mx = Mth.sin(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI) * f / 2D;
        double mz = -(Mth.cos(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI) * f) / 2D;
        double my = Mth.sin(xRot / 180.0F * (float) Math.PI) * f / 2D;
        return new Vec3(mx, my, mz);
    }

    @Override
    public void tick() {
        setTicksExisted(getTicksExisted() + 1);

        if ((!level.isClientSide || fake) && !hasLeftSource() && !blockPosition().equals(getBurstSourceBlockPos())) {
            entityData.set(LEFT_SOURCE_POS, true);
        }

        super.tick();

        if (!fake && isAlive()) {
            ping();
        }

        int mana = getCursedMana();
        if (getTicksExisted() >= getMinCursedManaLoss()) {
            accumulatedManaLoss += getCursedManaLossPerTick();
            int loss = (int) accumulatedManaLoss;
            setCursedMana(mana - loss);
            accumulatedManaLoss -= loss;

            if (getCursedMana() <= 0) {
                discard();
            }
        }

        particles();
        fullManaLastTick = getCursedMana() == getStartingCursedMana();
    }

    public void particles() {
        if (!isAlive() || !level.isClientSide) {
            return;
        }

        int color = getColor();
        float r = (color >> 16 & 0xFF) / 255F;
        float g = (color >> 8 & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        float size = getParticleSize();

        if (!fake) {
            WispParticleData data = WispParticleData.wisp(0.2F * size, r * 0.7F, g * 0.3F, b, true);
            level.addParticle(data, getX(), getY(), getZ(),
                    (Math.random() - 0.5F) * 0.02F,
                    (Math.random() - 0.5F) * 0.02F,
                    (Math.random() - 0.5F) * 0.02F);

            if (getTicksExisted() % SPIRAL_PARTICLE_INTERVAL == 0) {
                spawnSpiralParticles(r, g, b, size);
            }
        }
    }

    public ICursedManaReceiver getCollidedTile(boolean noParticles) {
        this.noParticles = noParticles;
        int iterations = 0;
        while (isAlive() && iterations < 200) {
            tick();
            iterations++;
        }
        return collidedTile;
    }

    public void setScanBeam() {
        scanBeam = true;
    }

    public float getParticleSize() {
        return (float) getCursedMana() / (float) getStartingCursedMana();
    }

    @Override
    protected float getGravity() {
        return getBurstGravity();
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult hit) {
        if (!isFake()) {
            super.onHitBlock(hit);
        }
        BlockPos collidePos = hit.getBlockPos();
        if (collidePos.equals(lastCollision)) {
            return;
        }
        lastCollision = collidePos.immutable();
        BlockEntity tile = level.getBlockEntity(collidePos);
        BlockPos sourcePos = getBurstSourceBlockPos();
        if (!hasLeftSource() && collidePos.equals(sourcePos)) {
            return;
        }
        if (tile instanceof ICursedManaReceiver receiver) {
            collidedTile = receiver;
            if (!fake && !noParticles && !level.isClientSide) {
                if (receiver.canReceiveCursedManaFromBursts() && onReceiverImpact(receiver)) {
                }
            }
        }
        onHitCommon(hit, true);
        if (!hasAlreadyCollidedAt(collidePos)) {
            alreadyCollidedAt.add(collidePos);
        }
    }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult hit) {
        super.onHitEntity(hit);
        onHitCommon(hit, false);
    }

    private void onHitCommon(HitResult hit, boolean shouldKill) {
        if (shouldKill && isAlive()) {
            if (!fake && level.isClientSide) {
                int color = getColor();
                float r = (color >> 16 & 0xFF) / 255F;
                float g = (color >> 8 & 0xFF) / 255F;
                float b = (color & 0xFF) / 255F;

                for (int i = 0; i < 4; i++) {
                    WispParticleData data = WispParticleData.wisp(0.15F, r, g, b);
                    level.addParticle(data, getX(), getY(), getZ(),
                            (Math.random() - 0.5F) * 0.04F,
                            (Math.random() - 0.5F) * 0.04F,
                            (Math.random() - 0.5F) * 0.04F);
                }
            }
            discard();
        }
    }

    /**
     * ✨ 生成螺旋粒子效果
     */
    private void spawnSpiralParticles(float r, float g, float b, float size) {
        Vec3 motion = getDeltaMovement();
        Vec3 direction = motion.normalize();

        // 计算垂直于运动方向的两个正交向量
        Vec3 perpendicular1 = getPerpendicular(direction);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        // 能量波动效果
        double pulse = Math.sin(getTicksExisted() * 0.3) * 0.15 + 1.0;

        for (int i = 0; i < SPIRAL_PARTICLE_COUNT; i++) {
            // 计算螺旋角度
            double angle = (i / (double) SPIRAL_PARTICLE_COUNT) * Math.PI * 2
                    + getTicksExisted() * SPIRAL_ROTATION_SPEED;

            // 螺旋半径随时间波动
            double radius = SPIRAL_RADIUS * pulse * size;

            // 计算螺旋位置偏移
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;

            // 将偏移转换到世界坐标系
            Vec3 offset = perpendicular1.scale(offsetX).add(perpendicular2.scale(offsetY));

            // 粒子位置
            double px = getX() + offset.x;
            double py = getY() + offset.y;
            double pz = getZ() + offset.z;

            // 计算螺旋运动速度
            double spiralVx = -Math.sin(angle) * SPIRAL_ROTATION_SPEED * 0.05;
            double spiralVy = Math.cos(angle) * SPIRAL_ROTATION_SPEED * 0.05;
            Vec3 spiralVelocity = perpendicular1.scale(spiralVx).add(perpendicular2.scale(spiralVy));

            // 粒子速度（跟随光束运动 + 螺旋运动）
            double vx = motion.x * 0.8 + spiralVelocity.x;
            double vy = motion.y * 0.8 + spiralVelocity.y;
            double vz = motion.z * 0.8 + spiralVelocity.z;

            // 粒子大小随角度和脉冲变化
            float particleSize = (0.15F + (float) Math.abs(Math.sin(angle)) * 0.1F) * size * (float) pulse;

            // 颜色随角度变化（产生渐变效果）
            float colorMult = 0.8F + (float) Math.abs(Math.cos(angle + getTicksExisted() * 0.1)) * 0.4F;

            WispParticleData spiralData = WispParticleData.wisp(particleSize,
                    r * colorMult, g * colorMult, b * colorMult, false);

            level.addParticle(spiralData, px, py, pz, vx, vy, vz);
        }

        // ✨ 核心光球（增强中心亮度）
        if (getTicksExisted() % 3 == 0) {
            WispParticleData coreData = WispParticleData.wisp(0.4F * size * (float) pulse,
                    r * 1.3F, g * 1.3F, b * 1.3F, true);
            level.addParticle(coreData, getX(), getY(), getZ(),
                    motion.x * 0.5, motion.y * 0.5, motion.z * 0.5);
        }

        // ✨ 螺旋尾迹粒子
        if (getTicksExisted() % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double trailDistance = -0.3 - i * 0.2;
                double tx = getX() + direction.x * trailDistance;
                double ty = getY() + direction.y * trailDistance;
                double tz = getZ() + direction.z * trailDistance;

                // 尾迹也有轻微的螺旋效果
                double trailAngle = getTicksExisted() * SPIRAL_ROTATION_SPEED + i * Math.PI / 3;
                double trailRadius = SPIRAL_RADIUS * 0.5 * size;
                Vec3 trailOffset = perpendicular1.scale(Math.cos(trailAngle) * trailRadius)
                        .add(perpendicular2.scale(Math.sin(trailAngle) * trailRadius));

                WispParticleData trailData = WispParticleData.wisp(0.2F * size,
                        r * 0.7F, g * 0.7F, b * 0.7F, true);
                level.addParticle(trailData,
                        tx + trailOffset.x, ty + trailOffset.y, tz + trailOffset.z,
                        motion.x * 0.3, motion.y * 0.3, motion.z * 0.3);
            }
        }
    }

    /**
     * ✨ 获取垂直于给定向量的向量
     */
    private Vec3 getPerpendicular(Vec3 vec) {
        Vec3 arbitrary = Math.abs(vec.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        return vec.cross(arbitrary).normalize();
    }


    private boolean onReceiverImpact(ICursedManaReceiver receiver) {
        if (hasWarped()) {
            return false;
        }

        int mana = getCursedMana();
        if (mana > 0) {
            receiver.receiveCursedMana(mana);
            return true;
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!fake) {
            var spreader = getShooter();
            if (spreader != null) {
                spreader.setCanShoot(true);
            }
        }
    }

    @Nullable
    private ICursedManaSpreader getShooter() {
        BlockEntity tile = level.getBlockEntity(getBurstSourceBlockPos());
        return tile instanceof ICursedManaSpreader spreader ? spreader : null;
    }

    @Override
    public boolean isFake() {
        return fake;
    }

    @Override
    public void setFake(boolean fake) {
        this.fake = fake;
    }

    @Override
    public int getColor() {
        return entityData.get(COLOR);
    }

    @Override
    public void setColor(int color) {
        entityData.set(COLOR, color);
    }

    @Override
    public int getCursedMana() {
        return entityData.get(MANA);
    }

    @Override
    public void setCursedMana(int mana) {
        entityData.set(MANA, mana);
    }

    @Override
    public int getStartingCursedMana() {
        return entityData.get(START_MANA);
    }

    @Override
    public void setStartingCursedMana(int mana) {
        entityData.set(START_MANA, mana);
    }

    @Override
    public int getMinCursedManaLoss() {
        return entityData.get(MIN_MANA_LOSS);
    }

    @Override
    public void setMinCursedManaLoss(int minManaLoss) {
        entityData.set(MIN_MANA_LOSS, minManaLoss);
    }

    @Override
    public float getCursedManaLossPerTick() {
        return entityData.get(MANA_LOSS_PER_TICK);
    }

    @Override
    public void setCursedManaLossPerTick(float mana) {
        entityData.set(MANA_LOSS_PER_TICK, mana);
    }

    @Override
    public float getBurstGravity() {
        return entityData.get(GRAVITY);
    }

    @Override
    public void setGravity(float gravity) {
        entityData.set(GRAVITY, gravity);
    }

    @Override
    public BlockPos getBurstSourceBlockPos() {
        return entityData.get(SOURCE_COORDS);
    }

    @Override
    public void setBurstSourceCoords(BlockPos pos) {
        entityData.set(SOURCE_COORDS, pos);
    }

    @Override
    public ItemStack getSourceLens() {
        return entityData.get(SOURCE_LENS);
    }

    @Override
    public void setSourceLens(ItemStack lens) {
        entityData.set(SOURCE_LENS, lens);
    }

    @Override
    public int getTicksExisted() {
        return _ticksExisted;
    }

    public void setTicksExisted(int ticks) {
        _ticksExisted = ticks;
    }

    @Override
    public boolean hasLeftSource() {
        return entityData.get(LEFT_SOURCE_POS);
    }

    @Override
    public boolean hasAlreadyCollidedAt(BlockPos pos) {
        return alreadyCollidedAt.contains(pos);
    }

    @Override
    public void setCollidedAt(BlockPos pos) {
        if (!hasAlreadyCollidedAt(pos)) {
            alreadyCollidedAt.add(pos.immutable());
        }
    }

    @Override
    public void setShooterUUID(UUID uuid) {
        shooterIdentity = uuid;
    }

    @Override
    public UUID getShooterUUID() {
        return shooterIdentity;
    }

    @Override
    public void ping() {
        var spreader = getShooter();
        if (spreader != null) {
            spreader.pingback(this, getShooterUUID());
        }
    }

    @Override
    public boolean hasWarped() {
        return warped;
    }

    @Override
    public void setWarped(boolean warped) {
        this.warped = warped;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_TICKS_EXISTED, getTicksExisted());
        tag.putInt(TAG_COLOR, getColor());
        tag.putInt(TAG_MANA, getCursedMana());
        tag.putInt(TAG_STARTING_MANA, getStartingCursedMana());
        tag.putInt(TAG_MIN_MANA_LOSS, getMinCursedManaLoss());
        tag.putFloat(TAG_TICK_MANA_LOSS, getCursedManaLossPerTick());
        tag.putFloat(TAG_GRAVITY, getBurstGravity());

        ItemStack stack = getSourceLens();
        CompoundTag lensCmp = new CompoundTag();
        if (!stack.isEmpty()) {
            lensCmp = stack.save(lensCmp);
        }
        tag.put(TAG_LENS_STACK, lensCmp);

        BlockPos coords = getBurstSourceBlockPos();
        tag.putInt(TAG_SPREADER_X, coords.getX());
        tag.putInt(TAG_SPREADER_Y, coords.getY());
        tag.putInt(TAG_SPREADER_Z, coords.getZ());

        if (lastCollision != null) {
            tag.putInt(TAG_LAST_COLLISION_X, lastCollision.getX());
            tag.putInt(TAG_LAST_COLLISION_Y, lastCollision.getY());
            tag.putInt(TAG_LAST_COLLISION_Z, lastCollision.getZ());
        }

        UUID identity = getShooterUUID();
        boolean hasShooter = identity != null;
        tag.putBoolean(TAG_HAS_SHOOTER, hasShooter);
        if (hasShooter) {
            tag.putUUID(TAG_SHOOTER, identity);
        }

        tag.putBoolean(TAG_WARPED, warped);
        tag.putBoolean(TAG_LEFT_SOURCE, hasLeftSource());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag cmp) {
        super.readAdditionalSaveData(cmp);
        setTicksExisted(cmp.getInt(TAG_TICKS_EXISTED));
        setColor(cmp.getInt(TAG_COLOR));
        setCursedMana(cmp.getInt(TAG_MANA));
        setStartingCursedMana(cmp.getInt(TAG_STARTING_MANA));
        setMinCursedManaLoss(cmp.getInt(TAG_MIN_MANA_LOSS));
        setCursedManaLossPerTick(cmp.getFloat(TAG_TICK_MANA_LOSS));
        setGravity(cmp.getFloat(TAG_GRAVITY));

        CompoundTag lensCmp = cmp.getCompound(TAG_LENS_STACK);
        ItemStack stack = ItemStack.of(lensCmp);
        if (!stack.isEmpty()) {
            setSourceLens(stack);
        } else {
            setSourceLens(ItemStack.EMPTY);
        }

        int x = cmp.getInt(TAG_SPREADER_X);
        int y = cmp.getInt(TAG_SPREADER_Y);
        int z = cmp.getInt(TAG_SPREADER_Z);
        setBurstSourceCoords(new BlockPos(x, y, z));

        if (cmp.contains(TAG_LAST_COLLISION_X)) {
            x = cmp.getInt(TAG_LAST_COLLISION_X);
            y = cmp.getInt(TAG_LAST_COLLISION_Y);
            z = cmp.getInt(TAG_LAST_COLLISION_Z);
            lastCollision = new BlockPos(x, y, z);
        }

        ListTag motion = cmp.getList("Motion", Tag.TAG_DOUBLE);
        setDeltaMovement(motion.getDouble(0), motion.getDouble(1), motion.getDouble(2));

        boolean hasShooter = cmp.getBoolean(TAG_HAS_SHOOTER);
        if (hasShooter) {
            UUID serializedUuid = cmp.getUUID(TAG_SHOOTER);
            UUID identity = getShooterUUID();
            if (!serializedUuid.equals(identity)) {
                setShooterUUID(serializedUuid);
            }
        }

        warped = cmp.getBoolean(TAG_WARPED);
        entityData.set(LEFT_SOURCE_POS, cmp.getBoolean(TAG_LEFT_SOURCE));
    }
}

