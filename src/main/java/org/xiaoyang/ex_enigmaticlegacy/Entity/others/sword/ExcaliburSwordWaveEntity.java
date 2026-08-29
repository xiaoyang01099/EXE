package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.BlockUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ExExcaliburConfig;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

// ExcaliburSwordWaveEntity 是 EX 咖喱棒剑气控制实体，负责锥形体积伤害和客户端视觉同步。
public class ExcaliburSwordWaveEntity extends Entity {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 构造 V 字侧轴使用的世界上方向。
    public static final Vec3 WORLD_SIDE = new Vec3(-1.0D, 0.0D, 0.0D); // 垂直瞄准时的默认侧轴。
    public static final EntityDataAccessor<Float> FORWARD_X = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // 发射方向 X。
    public static final EntityDataAccessor<Float> FORWARD_Y = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // 发射方向 Y。
    public static final EntityDataAccessor<Float> FORWARD_Z = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // 发射方向 Z。
    public static final EntityDataAccessor<Float> SIDE_X = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // V 字侧轴 X。
    public static final EntityDataAccessor<Float> SIDE_Y = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // V 字侧轴 Y。
    public static final EntityDataAccessor<Float> SIDE_Z = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // V 字侧轴 Z。
    public static final EntityDataAccessor<Float> MAX_RANGE = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // 创建时最大射程快照。
    public static final EntityDataAccessor<Float> BRANCH_DISTANCE = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.FLOAT); // 创建时单侧分叉快照。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(ExcaliburSwordWaveEntity.class, EntityDataSerializers.INT); // 客户端稳定视觉种子。
    public static double BLOCK_DESTROY_SEGMENT_LENGTH = 3.0D; // 普通剑气每段方块清理长度。
    public static double BLOCK_DESTROY_SIDE_PADDING = 0.75D; // 普通剑气方块破坏额外左右宽度。
    public static double BLOCK_DESTROY_HEIGHT_PADDING = 0.50D; // 普通剑气方块破坏额外上下高度。
    public static double END_SHOCKWAVE_DESTROY_HEIGHT_DOWN = 6.0D; // 终点冲击波向下破坏高度。
    public static int END_SHOCKWAVE_DESTROY_SLICE_SIZE = 16; // 终点冲击波方块破坏 XZ 分片宽度。
    public static int END_SHOCKWAVE_DESTROY_SLICES_PER_TICK = 1; // 每 tick 处理的终点冲击波破坏分片数量。
    public static double END_SHOCKWAVE_EDGE_BREAK_START = 0.82D; // 终点冲击波半径比例超过该值后进入破碎边缘区。
    public static double END_SHOCKWAVE_EDGE_KEEP_MIN = 0.18D; // 终点冲击波最外圈最低方块保留概率。
    public static double END_SHOCKWAVE_EDGE_KEEP_MAX = 0.55D; // 终点冲击波破碎边缘起点方块保留概率。
    @Nullable
    public UUID ownerUUID; // 释放者 UUID，用于排除自身并归属伤害。
    public int clientLastParticleTick = -1; // 客户端按本地 tick 防止 Renderer 多帧重复提交。
    public boolean clientEndEffectPlayed = false; // 客户端终点暗化、音效和星星粒子是否已播放。
    public boolean clientEndShockwavePlayed = false; // 客户端终点五层冲击波和底部法阵是否已播放。
    public int lastDestroyedConeSegment = -1; // 上一次已清理的普通剑气距离段。
    public double lastDestroyedConeDistance = 0.0D; // 普通剑气已经清理到的前进距离。
    public boolean endShockwaveBlockDestroyQueued = false; // 终点冲击波圆柱方块破坏任务是否已经入队。
    public Queue<EndShockwaveBlockDestroySlice> pendingEndShockwaveBlockDestroySlices = new ArrayDeque<>(); // 等待分 tick 处理的终点冲击波破坏分片。

    public ExcaliburSwordWaveEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    // 按玩家松开 C 键瞬间的状态创建 EX 剑气控制实体。
    public static ExcaliburSwordWaveEntity create(Player owner) {
        ExcaliburSwordWaveEntity entity = new ExcaliburSwordWaveEntity(ModEntities.EXCALIBUR_SWORD_WAVE.get(), owner.level());
        Vec3 forward = safeNormalize(owner.getLookAngle(), owner.getViewVector(1.0F));
        Vec3 side = resolveSide(forward, owner.getYRot());
        Vec3 origin = owner.getEyePosition().add(forward.scale(ExcaliburSwordWaveEffects.SPAWN_FORWARD_OFFSET));

        entity.ownerUUID = owner.getUUID();
        entity.entityData.set(FORWARD_X, (float) forward.x);
        entity.entityData.set(FORWARD_Y, (float) forward.y);
        entity.entityData.set(FORWARD_Z, (float) forward.z);
        entity.entityData.set(SIDE_X, (float) side.x);
        entity.entityData.set(SIDE_Y, (float) side.y);
        entity.entityData.set(SIDE_Z, (float) side.z);
        entity.entityData.set(MAX_RANGE, (float) ExExcaliburConfig.maxRange());
        entity.entityData.set(BRANCH_DISTANCE, (float) ExExcaliburConfig.branchDistance());
        entity.entityData.set(VISUAL_SEED, owner.getRandom().nextInt());
        entity.setPos(origin);
        return entity;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(FORWARD_X, 0.0F);
        this.entityData.define(FORWARD_Y, 0.0F);
        this.entityData.define(FORWARD_Z, 1.0F);
        this.entityData.define(SIDE_X, -1.0F);
        this.entityData.define(SIDE_Y, 0.0F);
        this.entityData.define(SIDE_Z, 0.0F);
        this.entityData.define(MAX_RANGE, (float) ExExcaliburConfig.DEFAULT_MAX_RANGE);
        this.entityData.define(BRANCH_DISTANCE, (float) ExExcaliburConfig.DEFAULT_BRANCH_DISTANCE);
        this.entityData.define(VISUAL_SEED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int damageTravelTicks = this.getDamageTravelTicks();
        int waveAge = this.getWaveAge();

        // 客户端只使用本地 tickCount 驱动 Renderer，不同步已推进 tick。
        if (this.level().isClientSide()) return;

        // 前置光柱落地后开始结算服务端锥形伤害，客户端视觉可用提前值独立调整出生时间。
        if (waveAge > 0 && waveAge <= damageTravelTicks + this.getDamagePathKeepTicks()) {
            this.damageCurrentCone(waveAge);
            this.destroyBlocksCurrentCone(waveAge);
        }
        if (ExcaliburEndShockwaveEffects.shouldDamageThisTick(this)) {
            this.damageEndShockwaveCylinder();
            this.destroyBlocksEndShockwaveCylinder();
        }
        this.processPendingEndShockwaveBlockDestroySlices();
        if (this.tickCount >= this.getDiscardTick() && this.pendingEndShockwaveBlockDestroySlices.isEmpty()) {
            this.discard();
        }
    }

    // 对当前保留路径内的完整 V 字锥形体积执行一次伤害判定。
    public void damageCurrentCone(int waveAge) {
        double currentDistance = this.getDamageDistanceAtTick(waveAge);
        double startDistance = this.getRetainedDamageStartDistance(waveAge);
        if (currentDistance <= startDistance) return;

        AABB searchBox = this.createDamageConeSearchBox(startDistance, currentDistance);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::canTryDamage);
        for (LivingEntity target : targets) {
            if (!this.isInsideDamageCone(target, startDistance, currentDistance)) continue;
            this.hurtTarget(target);
        }
    }

    // 对终点冲击波圆柱范围执行持续伤害判定。
    public void damageEndShockwaveCylinder() {
        Vec3 center = ExcaliburEndShockwaveEffects.resolveShockwaveCenter(this);
        AABB searchBox = this.createEndShockwaveSearchBox(center);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::canTryDamage);
        for (LivingEntity target : targets) {
            if (!this.isInsideEndShockwaveCylinder(target, center)) continue;
            this.hurtTarget(target);
        }
    }

    // 按普通 EX 剑气推进距离段清理方块，同一距离段只处理一次。
    public void destroyBlocksCurrentCone(int waveAge) {
        double currentDistance = this.getDamageDistanceAtTick(waveAge);
        if (currentDistance <= this.lastDestroyedConeDistance) return;

        // 以已清理距离为起点向前分段补齐，避免同一段内前沿继续推进时漏清后半段。
        double segmentStartDistance = Math.max(0.0D, this.lastDestroyedConeDistance);
        double segmentLength = Math.max(0.25D, BLOCK_DESTROY_SEGMENT_LENGTH);
        while (segmentStartDistance < currentDistance) {
            double segmentEndDistance = Math.min(currentDistance, segmentStartDistance + segmentLength);
            if (segmentEndDistance <= segmentStartDistance) break;
            final double destroyStartDistance = segmentStartDistance;
            final double destroyEndDistance = segmentEndDistance;
            AABB searchBox = this.createBlockDestroyConeSearchBox(destroyStartDistance, destroyEndDistance);
            this.destroyBlocksInBox(searchBox, pos -> this.isBlockInsideDamageCone(pos, destroyStartDistance, destroyEndDistance));
            segmentStartDistance = segmentEndDistance;
        }
        this.lastDestroyedConeDistance = currentDistance;
        this.lastDestroyedConeSegment = this.getDestroyConeSegment(currentDistance);
    }

    // 终点冲击波触发时只创建一次分片破坏队列，后续 tick 分批清理。
    public void destroyBlocksEndShockwaveCylinder() {
        this.enqueueEndShockwaveBlockDestroy();
    }

    // 将终点冲击波圆柱破坏范围按 XZ 空间分片入队，避免同 tick 遍历完整高圆柱。
    public void enqueueEndShockwaveBlockDestroy() {
        if (this.endShockwaveBlockDestroyQueued) return;
        this.endShockwaveBlockDestroyQueued = true;
        Vec3 center = ExcaliburEndShockwaveEffects.resolveShockwaveCenter(this);
        double radius = ExcaliburEndShockwaveEffects.getDamageRadius();
        int minX = Mth.floor(center.x - radius - 1.0D);
        int maxX = Mth.floor(center.x + radius + 1.0D);
        int minY = Mth.floor(center.y - this.getEndShockwaveDestroyHeightDown());
        int maxY = Mth.floor(center.y + this.getEndShockwaveDestroyHeightUp());
        int minZ = Mth.floor(center.z - radius - 1.0D);
        int maxZ = Mth.floor(center.z + radius + 1.0D);
        int sliceSize = Math.max(1, END_SHOCKWAVE_DESTROY_SLICE_SIZE);

        // 按 XZ 小块切分，Y 保留完整配置高度，确保视觉圆柱高度内会被逐片处理。
        for (int x = minX; x <= maxX; x += sliceSize) {
            int sliceMaxX = Math.min(maxX, x + sliceSize - 1);
            for (int z = minZ; z <= maxZ; z += sliceSize) {
                int sliceMaxZ = Math.min(maxZ, z + sliceSize - 1);
                this.pendingEndShockwaveBlockDestroySlices.add(new EndShockwaveBlockDestroySlice(
                        center, x, sliceMaxX, minY, maxY, z, sliceMaxZ, radius,
                        this.getEndShockwaveDestroyHeightUp(), this.getEndShockwaveDestroyHeightDown()));
            }
        }
    }

    // 每 tick 处理少量终点冲击波破坏分片，把大范围方块清除压力摊开。
    public void processPendingEndShockwaveBlockDestroySlices() {
        int slicesToProcess = Math.max(1, END_SHOCKWAVE_DESTROY_SLICES_PER_TICK);
        for (int i = 0; i < slicesToProcess; i++) {
            EndShockwaveBlockDestroySlice slice = this.pendingEndShockwaveBlockDestroySlices.poll();
            if (slice == null) return;
            this.destroyBlocksInEndShockwaveSlice(slice);
        }
    }

    // 清理单个终点冲击波空间分片，先按 XZ 圆柱列过滤，再遍历 Y，减少 AABB 四角无效访问。
    public void destroyBlocksInEndShockwaveSlice(EndShockwaveBlockDestroySlice slice) {
        if (slice == null) return;
        AABB sliceBox = slice.toAabb();
        if (!this.canDestroyRange(sliceBox)) return;
        double radiusSqr = slice.radius * slice.radius;
        for (int x = slice.minX; x <= slice.maxX; x++) {
            double dx = x + 0.5D - slice.center.x;
            for (int z = slice.minZ; z <= slice.maxZ; z++) {
                double dz = z + 0.5D - slice.center.z;
                double horizontalSqr = dx * dx + dz * dz;
                if (horizontalSqr > radiusSqr) continue;
                double distanceRatio = Math.sqrt(horizontalSqr) / Math.max(0.0001D, slice.radius);
                for (int y = slice.minY; y <= slice.maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!this.shouldDestroyEndShockwaveBlock(pos, distanceRatio)) continue;
                    this.clearBlockOrFluid(pos);
                }
            }
        }
    }

    // 遍历粗筛 AABB 并清理满足精筛条件的方块，清理前只做一次范围性 BlockUtil 检查。
    public void destroyBlocksInBox(AABB box, BlockDestroyPredicate predicate) {
        if (box == null || predicate == null) return;
        if (!this.canDestroyRange(box)) return;
        BlockPos minPos = new BlockPos(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ));
        BlockPos maxPos = new BlockPos(Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ));
        for (BlockPos mutablePos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockPos pos = mutablePos.immutable();
            if (!predicate.shouldDestroy(pos)) continue;
            this.clearBlockOrFluid(pos);
        }
    }

    // 判断粗筛范围是否允许被技能改为空气，避免逐块重复触发放置事件检查。
    public boolean canDestroyRange(AABB rangeBox) {
        if (rangeBox == null) return false;
        BlockPos minPos = new BlockPos(Mth.floor(rangeBox.minX), Mth.floor(rangeBox.minY), Mth.floor(rangeBox.minZ));
        BlockPos maxPos = new BlockPos(Mth.floor(rangeBox.maxX), Mth.floor(rangeBox.maxY), Mth.floor(rangeBox.maxZ));
        return BlockUtil.isPlaceBlock(this.level(), this, minPos, maxPos, Blocks.AIR.defaultBlockState());
    }

    // 粗筛和技能范围精筛通过后，直接把目标位置替换为空气。
    public void clearBlockOrFluid(BlockPos pos) {
        if (pos == null) return;
        BlockState state = this.level().getBlockState(pos);
        if (this.shouldSkipBlockClear(state)) return;
        this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    // 基岩不参与咖喱棒方块清理，避免技能直接删除不可破坏方块。
    public boolean shouldSkipBlockClear(BlockState state) {
        return state != null && state.is(Blocks.BEDROCK);
    }

    // 多点采样判断方块是否和普通剑气破坏锥形相交，减少只测中心点造成的破坏空隙。
    public boolean isBlockInsideDamageCone(BlockPos pos, double startDistance, double currentDistance) {
        if (pos == null) return false;
        Vec3 base = Vec3.atLowerCornerOf(pos);
        return this.isPointInsideBlockDestroyCone(base.add(0.5D, 0.5D, 0.5D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(0.5D, 1.0D, 0.5D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(0.5D, 0.0D, 0.5D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(0.0D, 0.5D, 0.5D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(1.0D, 0.5D, 0.5D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(0.5D, 0.5D, 0.0D), startDistance, currentDistance)
                || this.isPointInsideBlockDestroyCone(base.add(0.5D, 0.5D, 1.0D), startDistance, currentDistance);
    }

    // 判断单点是否落入普通剑气方块破坏锥形，破坏范围可比伤害范围略宽但不影响实体伤害。
    public boolean isPointInsideBlockDestroyCone(Vec3 point, double startDistance, double currentDistance) {
        if (point == null) return false;
        Vec3 forward = this.getForward();
        Vec3 side = this.getSide();
        Vec3 relative = point.subtract(this.position());
        double forwardDistance = relative.dot(forward);
        if (forwardDistance < startDistance || forwardDistance > currentDistance) return false;

        double halfWidth = this.getDamageHalfWidth(forwardDistance) + BLOCK_DESTROY_SIDE_PADDING;
        double sideDistance = Math.abs(relative.dot(side));
        if (sideDistance > halfWidth) return false;

        Vec3 centerLinePoint = this.position().add(forward.scale(forwardDistance));
        double verticalOffset = point.y - centerLinePoint.y;
        return verticalOffset >= -ExExcaliburConfig.damageHeightDown() - BLOCK_DESTROY_HEIGHT_PADDING
                && verticalOffset <= ExExcaliburConfig.damageHeightUp() + BLOCK_DESTROY_HEIGHT_PADDING;
    }

    // 判断方块中心点是否处于终点冲击波破坏圆柱内，并在边缘保留随机残块。
    public boolean isBlockInsideEndShockwaveDestroyCylinder(BlockPos pos, Vec3 center) {
        if (pos == null) return false;
        Vec3 point = Vec3.atCenterOf(pos);
        if (!this.isPointInsideEndShockwaveDestroyCylinder(point, center)) return false;
        Vec3 safeCenter = center == null ? this.position() : center;
        double radius = Math.max(0.0001D, ExcaliburEndShockwaveEffects.getDamageRadius());
        double dx = point.x - safeCenter.x;
        double dz = point.z - safeCenter.z;
        double distanceRatio = Math.sqrt(dx * dx + dz * dz) / radius;
        if (distanceRatio < END_SHOCKWAVE_EDGE_BREAK_START) return true;
        double edgeT = Mth.clamp((distanceRatio - END_SHOCKWAVE_EDGE_BREAK_START) / (1.0D - END_SHOCKWAVE_EDGE_BREAK_START), 0.0D, 1.0D);
        return !this.shouldKeepEndShockwaveEdgeBlock(pos, edgeT);
    }

    // 判断点是否处于终点冲击波破坏圆柱的基础范围内。
    public boolean isPointInsideEndShockwaveDestroyCylinder(Vec3 point, Vec3 center) {
        if (point == null) return false;
        Vec3 safeCenter = center == null ? this.position() : center;
        double dx = point.x - safeCenter.x;
        double dz = point.z - safeCenter.z;
        double damageRadius = ExcaliburEndShockwaveEffects.getDamageRadius();
        if (dx * dx + dz * dz > damageRadius * damageRadius) return false;
        double verticalOffset = point.y - safeCenter.y;
        return verticalOffset >= -this.getEndShockwaveDestroyHeightDown()
                && verticalOffset <= this.getEndShockwaveDestroyHeightUp();
    }

    // 根据圆柱边缘破碎规则判断终点冲击波分片内的单个方块是否应该清除。
    public boolean shouldDestroyEndShockwaveBlock(BlockPos pos, double distanceRatio) {
        if (pos == null) return false;
        if (distanceRatio < END_SHOCKWAVE_EDGE_BREAK_START) return true;
        double edgeT = Mth.clamp((distanceRatio - END_SHOCKWAVE_EDGE_BREAK_START) / (1.0D - END_SHOCKWAVE_EDGE_BREAK_START), 0.0D, 1.0D);
        return !this.shouldKeepEndShockwaveEdgeBlock(pos, edgeT);
    }

    // 使用稳定随机值决定终点冲击波外圈边缘方块是否保留。
    public boolean shouldKeepEndShockwaveEdgeBlock(BlockPos pos, double edgeT) {
        double safeEdgeT = Mth.clamp(edgeT, 0.0D, 1.0D);
        double keepChance = Mth.lerp(safeEdgeT, END_SHOCKWAVE_EDGE_KEEP_MAX, END_SHOCKWAVE_EDGE_KEEP_MIN);
        return this.stableBlockNoise(pos) < keepChance;
    }

    // 根据方块坐标生成 0 到 1 的稳定随机值，让破碎边缘不会每帧闪烁。
    public double stableBlockNoise(BlockPos pos) {
        if (pos == null) return 0.0D;
        long seed = pos.asLong();
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= seed >>> 33;
        return (seed & 0xFFFFFFL) / (double) 0x1000000L;
    }

    // 为终点冲击波圆柱精筛构造粗略包围盒。
    public AABB createEndShockwaveSearchBox(Vec3 center) {
        Vec3 safeCenter = center == null ? this.position() : center;
        double damageRadius = ExcaliburEndShockwaveEffects.getDamageRadius();
        double verticalPadding = Math.max(ExExcaliburConfig.damageHeightUp(), ExExcaliburConfig.damageHeightDown()) + 1.0D;
        return new AABB(safeCenter, safeCenter).inflate(damageRadius + 1.0D, verticalPadding, damageRadius + 1.0D);
    }

    // 为终点冲击波方块破坏构造使用配置圆台高度的完整粗筛包围盒。
    public AABB createEndShockwaveDestroySearchBox(Vec3 center) {
        Vec3 safeCenter = center == null ? this.position() : center;
        double damageRadius = ExcaliburEndShockwaveEffects.getDamageRadius();
        return new AABB(
                safeCenter.x - damageRadius - 1.0D,
                safeCenter.y - this.getEndShockwaveDestroyHeightDown(),
                safeCenter.z - damageRadius - 1.0D,
                safeCenter.x + damageRadius + 1.0D,
                safeCenter.y + this.getEndShockwaveDestroyHeightUp(),
                safeCenter.z + damageRadius + 1.0D);
    }

    // 使用目标多个代表点精筛是否落入终点冲击波圆柱伤害范围。
    public boolean isInsideEndShockwaveCylinder(LivingEntity target, Vec3 center) {
        if (target == null) return false;
        AABB box = target.getBoundingBox();
        Vec3 targetCenter = box.getCenter();
        Vec3 eye = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        Vec3 feet = new Vec3(target.getX(), box.minY + 0.05D, target.getZ());
        Vec3 top = new Vec3(target.getX(), box.maxY, target.getZ());
        return this.isPointInsideEndShockwaveCylinder(targetCenter, center)
                || this.isPointInsideEndShockwaveCylinder(eye, center)
                || this.isPointInsideEndShockwaveCylinder(feet, center)
                || this.isPointInsideEndShockwaveCylinder(top, center);
    }

    // 判断单点是否位于终点冲击波圆柱内，半径取最外层圆台半径加配置 padding。
    public boolean isPointInsideEndShockwaveCylinder(Vec3 point, Vec3 center) {
        if (point == null) return false;
        Vec3 safeCenter = center == null ? this.position() : center;
        double dx = point.x - safeCenter.x;
        double dz = point.z - safeCenter.z;
        double damageRadius = ExcaliburEndShockwaveEffects.getDamageRadius();
        if (dx * dx + dz * dz > damageRadius * damageRadius) return false;
        double verticalOffset = point.y - safeCenter.y;
        return verticalOffset >= -ExExcaliburConfig.damageHeightDown()
                && verticalOffset <= ExExcaliburConfig.damageHeightUp();
    }

    // 为锥形精筛构造粗略包围盒，避免遍历全世界实体。
    public AABB createDamageConeSearchBox(double startDistance, double currentDistance) {
        Vec3 start = this.position().add(this.getForward().scale(Math.max(0.0D, startDistance)));
        Vec3 end = this.position().add(this.getForward().scale(Math.max(0.0D, currentDistance)));
        double sidePadding = this.getBranchDistance() + ExExcaliburConfig.damageSidePadding() + 1.0D;
        double verticalPadding = Math.max(ExExcaliburConfig.damageHeightUp(), ExExcaliburConfig.damageHeightDown()) + 1.0D;
        return new AABB(start, end).inflate(sidePadding, verticalPadding, sidePadding);
    }

    // 为普通剑气方块破坏构造包含额外破坏 padding 的粗筛包围盒。
    public AABB createBlockDestroyConeSearchBox(double startDistance, double currentDistance) {
        Vec3 start = this.position().add(this.getForward().scale(Math.max(0.0D, startDistance)));
        Vec3 end = this.position().add(this.getForward().scale(Math.max(0.0D, currentDistance)));
        double sidePadding = this.getBranchDistance() + ExExcaliburConfig.damageSidePadding() + BLOCK_DESTROY_SIDE_PADDING + 1.0D;
        double verticalPadding = Math.max(
                ExExcaliburConfig.damageHeightUp() + BLOCK_DESTROY_HEIGHT_PADDING,
                ExExcaliburConfig.damageHeightDown() + BLOCK_DESTROY_HEIGHT_PADDING) + 1.0D;
        return new AABB(start, end).inflate(sidePadding, verticalPadding, sidePadding);
    }

    // 使用目标多个代表点精筛是否落入当前 V 字锥形伤害体积。
    public boolean isInsideDamageCone(LivingEntity target, double startDistance, double currentDistance) {
        if (target == null) return false;
        AABB box = target.getBoundingBox();
        Vec3 center = box.getCenter();
        Vec3 eye = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        Vec3 feet = new Vec3(target.getX(), box.minY + 0.05D, target.getZ());
        Vec3 top = new Vec3(target.getX(), box.maxY, target.getZ());
        return this.isPointInsideDamageCone(center, startDistance, currentDistance)
                || this.isPointInsideDamageCone(eye, startDistance, currentDistance)
                || this.isPointInsideDamageCone(feet, startDistance, currentDistance)
                || this.isPointInsideDamageCone(top, startDistance, currentDistance);
    }

    // 判断单个世界坐标点是否处于当前 V 字锥形伤害体积内。
    public boolean isPointInsideDamageCone(Vec3 point, double startDistance, double currentDistance) {
        if (point == null) return false;
        Vec3 forward = this.getForward();
        Vec3 side = this.getSide();
        Vec3 relative = point.subtract(this.position());
        double forwardDistance = relative.dot(forward);
        if (forwardDistance < startDistance || forwardDistance > currentDistance) return false;

        double halfWidth = this.getDamageHalfWidth(forwardDistance);
        double sideDistance = Math.abs(relative.dot(side));
        if (sideDistance > halfWidth) return false;

        Vec3 centerLinePoint = this.position().add(forward.scale(forwardDistance));
        double verticalOffset = point.y - centerLinePoint.y;
        return verticalOffset >= -ExExcaliburConfig.damageHeightDown()
                && verticalOffset <= ExExcaliburConfig.damageHeightUp();
    }

    // 排除释放者、死亡目标和实体伤害白名单，其他目标允许被锥形体积持续命中。
    public boolean canTryDamage(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.ownerUUID != null && this.ownerUUID.equals(target.getUUID())) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 使用服务端 EX 咖喱棒伤害配置对单次锥形命中结算伤害。
    public void hurtTarget(LivingEntity target) {
        float damage = ExExcaliburConfig.damage();
        Player owner = this.ownerUUID == null ? null : this.level().getPlayerByUUID(this.ownerUUID);

        // 清除原版受伤无敌帧，让锥形保留路径可以连续 tick 造成持续伤害。
        target.invulnerableTime = 0;
        if (owner != null) {
            target.hurt(owner.damageSources().playerAttack(owner), damage);
        } else {
            target.hurt(this.damageSources().magic(), damage);
        }
    }

    // 根据当前 V 字总宽度计算至少三路、保持奇数且不超过硬上限的路线数量。
    public int getLaneCountAtDistance(double distance) {
        double currentWidth = this.getBranchOffset(distance) * 2.0D;
        int laneCount = Math.max(3, Mth.ceil(currentWidth / ExcaliburSwordWaveEffects.LANE_SPACING) + 1);
        if ((laneCount & 1) == 0) laneCount++;
        return Math.min(laneCount, ExcaliburSwordWaveEffects.MAX_LANE_COUNT);
    }

    // 在当前前沿宽度内均匀计算指定路线位置，奇数路保证中间存在中心线。
    public Vec3 getLanePosition(double distance, int laneIndex, int laneCount) {
        Vec3 center = this.position().add(this.getForward().scale(distance));
        if (laneCount <= 1) return center;
        double laneT = Mth.clamp(laneIndex / (double) (laneCount - 1), 0.0D, 1.0D);
        double branchOffset = this.getBranchOffset(distance);
        double laneOffset = Mth.lerp(laneT, -branchOffset, branchOffset);
        return center.add(this.getSide().scale(laneOffset));
    }

    public double getDistanceAtTick(int age) {
        return Math.min(Math.max(0, age) * ExcaliburSwordWaveEffects.FORWARD_SPEED, this.getMaxRange());
    }

    public double getDamageDistanceAtTick(int age) {
        double damageTime = Math.max(0.0D, age - 1.0D);
        double damageT = Mth.clamp(damageTime / this.getDamageTravelTicks(), 0.0D, 1.0D);
        double distanceT = Math.pow(damageT, Math.max(0.05F, ExcaliburSwordWaveEffects.EX_WAVE_DAMAGE_DISTANCE_POWER));
        return this.getMaxRange() * distanceT;
    }

    public double getRetainedDamageStartDistance(int waveAge) {
        int keepTicks = this.getDamagePathKeepTicks();
        if (keepTicks <= 0) return 0.0D;
        int retainedStartAge = Math.max(1, waveAge - keepTicks);
        return this.getDamageDistanceAtTick(retainedStartAge);
    }

    // 根据当前伤害前沿距离计算方块清理区域段。
    public int getDestroyConeSegment(double currentDistance) {
        double segmentLength = Math.max(0.25D, BLOCK_DESTROY_SEGMENT_LENGTH);
        return Math.max(0, Mth.ceil(Math.max(0.0D, currentDistance) / segmentLength) - 1);
    }

    // 返回指定方块清理区域段的起始距离。
    public double getDestroyConeSegmentStartDistance(int segment) {
        double segmentLength = Math.max(0.25D, BLOCK_DESTROY_SEGMENT_LENGTH);
        return Math.max(0, segment) * segmentLength;
    }

    // 返回指定方块清理区域段的结束距离，不超过当前伤害前沿。
    public double getDestroyConeSegmentEndDistance(int segment, double currentDistance) {
        double segmentLength = Math.max(0.25D, BLOCK_DESTROY_SEGMENT_LENGTH);
        double segmentEndDistance = (Math.max(0, segment) + 1.0D) * segmentLength;
        return Math.min(Math.max(0.0D, currentDistance), segmentEndDistance);
    }

    // 返回服务端锥形伤害的实际半宽，额外 padding 用于覆盖视觉发光边缘。
    public double getDamageHalfWidth(double distance) {
        return this.getBranchOffset(distance) + ExExcaliburConfig.damageSidePadding();
    }

    public double getBranchOffset(double distance) {
        double progress = this.getMaxRange() <= 0.0D ? 1.0D : Mth.clamp(distance / this.getMaxRange(), 0.0D, 1.0D);
        return this.getBranchDistance() * progress;
    }

    public int getTravelTicks() {
        return Math.max(1, Mth.ceil(this.getMaxRange() / ExcaliburSwordWaveEffects.FORWARD_SPEED));
    }

    public int getDamageTravelTicks() {
        return Math.max(1, ExcaliburSwordWaveEffects.EX_WAVE_DAMAGE_TRAVEL_TICKS);
    }

    public int getDamagePathKeepTicks() {
        return Math.max(0, ExcaliburSwordWaveEffects.EX_WAVE_DAMAGE_PATH_KEEP_TICKS);
    }

    // 返回终点冲击波方块破坏向上高度，贴合配置圆台主体高度。
    public double getEndShockwaveDestroyHeightUp() {
        return Math.max(1.0D, ExcaliburEndShockwaveEffects.getBaseHeight());
    }

    // 返回终点冲击波方块破坏向下高度，覆盖起点下移和地面附近方块。
    public double getEndShockwaveDestroyHeightDown() {
        return Math.max(0.0D, END_SHOCKWAVE_DESTROY_HEIGHT_DOWN);
    }

    // 返回实体服务端生命周期回收 tick，覆盖普通剑气尾迹和终点冲击波持续伤害。
    public int getDiscardTick() {
        int swordWaveTailTick = ExcaliburSwordWaveEffects.EX_WAVE_START_TICKS
                + Math.max(this.getDamageTravelTicks() + this.getDamagePathKeepTicks(),
                ExcaliburSwordWaveEffects.getVisualTravelTicks() + 1 - ExcaliburSwordWaveEffects.EX_WAVE_VISUAL_ADVANCE_TICKS)
                + ExcaliburSwordWaveEffects.PARTICLE_TAIL_TICKS;
        return Math.max(swordWaveTailTick, ExcaliburEndShockwaveEffects.getEntityDiscardTick());
    }

    // 返回扣除光柱劈落阶段后的 EX 剑气推进年龄，保留阶段内剑气已经从起点开始前冲。
    public int getWaveAge() {
        return Math.max(0, this.tickCount - ExcaliburSwordWaveEffects.EX_WAVE_START_TICKS + 1);
    }

    public Vec3 getForward() {
        return safeNormalize(new Vec3(
                this.entityData.get(FORWARD_X),
                this.entityData.get(FORWARD_Y),
                this.entityData.get(FORWARD_Z)), new Vec3(0.0D, 0.0D, 1.0D));
    }

    public Vec3 getSide() {
        return safeNormalize(new Vec3(
                this.entityData.get(SIDE_X),
                this.entityData.get(SIDE_Y),
                this.entityData.get(SIDE_Z)), WORLD_SIDE);
    }

    public double getMaxRange() {
        return Math.max(1.0D, this.entityData.get(MAX_RANGE));
    }

    public double getBranchDistance() {
        return Math.max(0.0D, this.entityData.get(BRANCH_DISTANCE));
    }

    public int getVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    // 根据完整发射方向构造 V 字水平侧轴，垂直瞄准时使用玩家 yaw 回退。
    public static Vec3 resolveSide(Vec3 forward, float playerYaw) {
        Vec3 side = forward.cross(WORLD_UP);
        if (side.lengthSqr() >= 1.0E-8D) return side.normalize();
        double yawRadians = Math.toRadians(playerYaw);
        return safeNormalize(new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians)), WORLD_SIDE);
    }

    public static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        if (value == null || value.lengthSqr() < 1.0E-8D) return fallback;
        return value.normalize();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    // EX 剑气是当前服务端会话中的短生命周期技能实体，不写入世界存档。
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // BlockDestroyPredicate 用于给 AABB 方块遍历提供技能范围精筛条件。
    public interface BlockDestroyPredicate {
        boolean shouldDestroy(BlockPos pos);
    }

    // EndShockwaveBlockDestroySlice 保存终点冲击波分 tick 方块破坏的单个空间分片。
    public static class EndShockwaveBlockDestroySlice {
        public Vec3 center; // 终点冲击波中心。
        public int minX; // 分片最小 X。
        public int maxX; // 分片最大 X。
        public int minY; // 分片最小 Y。
        public int maxY; // 分片最大 Y。
        public int minZ; // 分片最小 Z。
        public int maxZ; // 分片最大 Z。
        public double radius; // 终点冲击波破坏半径。
        public double heightUp; // 分片记录的向上破坏高度。
        public double heightDown; // 分片记录的向下破坏高度。

        public EndShockwaveBlockDestroySlice(Vec3 center, int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                             double radius, double heightUp, double heightDown) {
            this.center = center == null ? Vec3.ZERO : center;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.radius = Math.max(0.0001D, radius);
            this.heightUp = Math.max(0.0D, heightUp);
            this.heightDown = Math.max(0.0D, heightDown);
        }

        // 将整数分片范围转换为包含完整方块体积的 AABB。
        public AABB toAabb() {
            return new AABB(this.minX, this.minY, this.minZ, this.maxX + 0.999D, this.maxY + 0.999D, this.maxZ + 0.999D);
        }
    }
}
