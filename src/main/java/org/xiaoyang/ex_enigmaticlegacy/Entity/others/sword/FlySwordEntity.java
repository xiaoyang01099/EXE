package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;

import java.util.*;

// FlySwordEntity 负责飞剑跟随、索敌、攻击和拖尾记录。
public class FlySwordEntity extends Entity {
    public static final EntityDataAccessor<Integer> FLY_SWORD_MOVE_STATE = SynchedEntityData.defineId(FlySwordEntity.class, EntityDataSerializers.INT); // 飞剑同步移动状态。
    public static final int CLIENT_LERP_STEPS = 3; // 客户端每次收到同步包后的插值 tick 数。
    public static final double CLIENT_TELEPORT_DISTANCE = 10.0D; // 客户端超过该距离时直接定位，避免长距离慢速回拉。
    public static final double FOLLOW_RETURN_SPEED = 1.2D; // 距离编队点较远时的回归速度。
    public static final double FOLLOW_SETTLE_SPEED = 0.35D; // 靠近编队点后的贴合速度。
    public static final double FOLLOW_FAST_DISTANCE = 4.0D; // 超过该距离使用快速回归速度。
    public static final double FOLLOW_STOP_DISTANCE = 0.08D; // 小于该距离视为到位。
    public static final double TELEPORT_BACK_DISTANCE = 20.0D; // 超出该距离时直接回到编队点。
    public static final int POST_HIT_RANDOM_MOVE_TICKS = 2; // 命中后随机方向继续移动 tick。
    public static final double POST_HIT_RANDOM_UP_SCALE = 0.2D; // 随机方向允许的上抬幅度。
    public static final double POST_HIT_RANDOM_YAW_DEGREES = 18.0D; // 命中后随机飞出时允许的水平偏转角度。
    public UUID masterUUID; // 主人 UUID。
    public Entity master; // 飞剑主人实体。
    public int ID; // 飞剑序号。
    public MoveState moveState = MoveState.FOLLOW; // 当前移动状态。
    public int masterIsNullTick = 0; // 丢失主人后的累计 tick。
    public int attackNum = 0; // 当前累计命中次数。
    public int deFoodNum = 10; // 触发一次饱和度消耗所需命中次数。
    public final Random random = new Random(); // 随机目标选择器。
    public int attackDamage = 2; // 飞剑伤害。
    public int searchRange = 16; // 索敌范围。
    public static float speed = 3.0f; // 飞剑移动速度。
    public Vec3 targetMonsterPos; // 当前目标位置。
    public Vec3 targetDirection; // 当前目标方向。
    public int distance; // 到目标的距离。
    public int waitTick = 6; // 当前等待 tick。
    public List<Entity> entities; // 临时命中实体列表。
    public List<Entity> playerNearEntities; // 附近可攻击目标缓存。
    public LinkedList<Vec3> prePosList; // 客户端拖尾历史点。
    public int clientLerpSteps; // 客户端剩余插值 tick。
    public double clientLerpX; // 客户端插值目标 X。
    public double clientLerpY; // 客户端插值目标 Y。
    public double clientLerpZ; // 客户端插值目标 Z。
    public float clientLerpYRot; // 客户端插值目标水平朝向。
    public float clientLerpXRot; // 客户端插值目标俯仰朝向。
    public double attackYOffset; // 本轮攻击固定高度偏移。
    public boolean hasHitCurrentTarget; // 本轮攻击是否已经命中。
    public int postHitRandomMoveTicks; // 命中后随机方向继续移动的剩余 tick。
    public Vec3 postHitRandomDirection; // 命中后继续移动的随机方向。

    // 构造飞剑实体。
    public FlySwordEntity(EntityType<? extends FlySwordEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.attackDamage = ConfigFile.flySwordAttackDamage();
        this.searchRange = ConfigFile.flySwordSearchRange();
        this.prePosList = new LinkedList<>();
        this.playerNearEntities = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.hasImpulse = false;
        this.postHitRandomDirection = Vec3.ZERO;
    }

    // 设置飞剑主人和编号。
    public void setOwner(Entity player, int entityID) {
        this.master = player;
        this.masterUUID = player.getUUID();
        this.ID = entityID;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource == level().damageSources().genericKill()) {
            return super.hurt(pSource, pAmount);
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            this.moveState = MoveState.values()[this.entityData.get(FLY_SWORD_MOVE_STATE)];
            tickClientLerp();
            addPrePosList(position());
            return;
        }

//        if (this.master == null && this.masterUUID != null && level() instanceof ServerLevel serverLevel) {
//            this.master = serverLevel.getEntity(this.masterUUID);
//        }

        if (master != null && !master.isRemoved()) {
            this.masterIsNullTick = 0;
            updatePos();
        } else {
            if (masterIsNullTick > 60) {
                masterIsNullTick = 0;
                this.remove(RemovalReason.DISCARDED);
                return;
            }
            masterIsNullTick++;
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    // 客户端按最近一次服务端同步目标推进短插值，尽量还原原来 Mob 版的平滑观感。
    public void tickClientLerp() {
        if (this.clientLerpSteps <= 0) {
            return;
        }

        // 每 tick 都向缓存目标逼近一小段，让本体位置和拖尾点都基于平滑后的坐标。
        double nextX = this.getX() + (this.clientLerpX - this.getX()) / this.clientLerpSteps;
        double nextY = this.getY() + (this.clientLerpY - this.getY()) / this.clientLerpSteps;
        double nextZ = this.getZ() + (this.clientLerpZ - this.getZ()) / this.clientLerpSteps;
        float nextYRot = Mth.rotLerp(1.0F / this.clientLerpSteps, this.getYRot(), this.clientLerpYRot);
        float nextXRot = Mth.lerp(1.0F / this.clientLerpSteps, this.getXRot(), this.clientLerpXRot);
        this.setPos(nextX, nextY, nextZ);
        this.setRot(nextYRot, nextXRot);
        this.clientLerpSteps--;
    }

    // 按当前状态推进飞剑逻辑。
    public void updatePos() {
        switch (moveState) {
            case FOLLOW -> findTarget();
            case MOVING -> moveToEntity();
            case WAIT -> waitState();
            case BACK -> backToPlayer();
            default -> findTarget();
        }
    }

    // 搜索目标，没有目标时回到跟随状态。
    public void findTarget() {
        playerNearEntities = getSearchRangeTargets(master, searchRange);
        if (playerNearEntities.isEmpty()) {
            followPlayer();
            return;
        }
        this.targetMonsterPos = getRandomPosition(playerNearEntities);
        setMovingState();
    }

    // 随机选择一个怪物位置作为目标。
    public Vec3 getRandomPosition(List<Entity> entities) {
        if (entities.size() == 1) {
            return entities.get(0).position();
        }
        int index = random.nextInt(entities.size());
        return entities.get(index).position();
    }

    // 进入飞行攻击状态。
    public void setMovingState() {
        // 每轮攻击只随机一次高度偏移，避免每 tick 改变目标点导致打转。
        this.attackYOffset = random.nextBoolean() ? 0.1D : 0.0D;
        this.hasHitCurrentTarget = false;
        this.postHitRandomMoveTicks = 0;
        this.postHitRandomDirection = Vec3.ZERO;
        this.targetMonsterPos = this.targetMonsterPos.add(0.0D, this.attackYOffset, 0.0D);
        this.targetDirection = targetMonsterPos.subtract(position()).normalize();
        this.distance = (int) targetMonsterPos.distanceTo(position());
        this.setWaitTick((int) (distance / speed) + 3);
        this.setYRot(calculateYAngle(targetDirection));
        setFlySwordMoveState(MoveState.MOVING);
    }

    // 根据与编队点的距离切换回归速度。
    public double getFollowSpeed(double distanceToTarget) {
        if (distanceToTarget > FOLLOW_FAST_DISTANCE) {
            return FOLLOW_RETURN_SPEED;
        }
        return FOLLOW_SETTLE_SPEED;
    }

    // 跟随主人待机。
    public void followPlayer() {
        if (this.master == null) return;
        Vec3 eye = master.position();
        Vec3 directionVec3 = Vec3.atLowerCornerOf(master.getDirection().getNormal().multiply(-1));
        Vec3 playerBack = eye.add(directionVec3);
        Vec3 targetPos = playerBack.add(calcSpawnOffset(directionVec3));
        Vec3 offset = targetPos.subtract(this.position());
        this.setYRot(calculateYAngle(offset));
        double distanceToTarget = offset.length();

        // 飞剑和编队点距离过大时直接拉回，避免跨区域追随时间过长。
        if (distanceToTarget <= FOLLOW_STOP_DISTANCE) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (distanceToTarget > TELEPORT_BACK_DISTANCE) {
            setPos(targetPos);
            setDeltaMovement(Vec3.ZERO);
        } else {
            moveTowardClamped(targetPos, getFollowSpeed(distanceToTarget), FOLLOW_STOP_DISTANCE);
        }
    }

    // 计算飞剑在主人身后的编队偏移。
    public Vec3 calcSpawnOffset(Vec3 direction) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize().add(0, 0.2, 0);
        if (right.lengthSqr() < 0.001) {
            right = new Vec3(1, 0, 0);
        }

        float spacing = 0.2f;
        Vec3 pos = Vec3.ZERO;
        if (this.ID < 4) {
            switch (this.ID) {
                case 1 -> pos = pos.add(right.scale(-0.5).add(0, 0.2, 0));
                case 2 -> pos = pos.add(right.scale(0.5));
                case 3 -> pos = pos.add(0, 0.3, 0);
                default -> {
                }
            }
        } else if (this.ID % 2 == 0) {
            pos = pos.add(right.scale(-1 * this.ID * spacing)).add(0, 0.2, 0);
        } else {
            pos = pos.add(right.scale(this.ID * spacing)).add(0, -0.2, 0);
        }
        return pos;
    }

    // 朝目标移动并尝试命中。
    public void moveToEntity() {
        if (this.postHitRandomMoveTicks > 0) {
            setDeltaMovement(this.postHitRandomDirection.scale(speed));
            this.setYRot(calculateYAngle(this.getDeltaMovement()));
            this.postHitRandomMoveTicks--;
            if (this.postHitRandomMoveTicks <= 0) {
                this.hasHitCurrentTarget = false;
                setFlySwordMoveState(MoveState.WAIT);
                setWaitTick(0);
            }
            return;
        }

        if (this.targetMonsterPos == null) {
            setFlySwordMoveState(MoveState.WAIT);
            return;
        }

        moveToward(this.targetMonsterPos, speed);
        this.setYRot(calculateYAngle(this.getDeltaMovement()));

        if (findAndHurtTarget()) {
            this.hasHitCurrentTarget = true;
            this.postHitRandomDirection = createPostHitRandomDirection();
            this.postHitRandomMoveTicks = POST_HIT_RANDOM_MOVE_TICKS;
            return;
        }
        if (waitTick <= 1) {
            setFlySwordMoveState(MoveState.WAIT);
        }
        setWaitTick();
    }

    // 朝任意目标点移动。
    public void moveToVec3(Vec3 target) {
        moveToward(target, speed);
    }

    // 跟随玩家时使用防过冲移动，避免在编队点附近反复抖动。
    public void moveTowardClamped(Vec3 target, double maxStep, double stopDistance) {
        Vec3 offset = target.subtract(position());
        double distanceToTarget = offset.length();
        if (distanceToTarget <= stopDistance) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        double stepLength = Math.min(distanceToTarget - stopDistance, maxStep);
        setDeltaMovement(offset.normalize().scale(stepLength));
    }

    // 按最大步长平滑靠近目标点，避免整段位移导致瞬移观感。
    public void moveToward(Vec3 target, double maxStep) {
        Vec3 offset = target.subtract(position());
        double distanceToTarget = offset.length();
        if (distanceToTarget <= 0.001D) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        Vec3 step = offset.normalize().scale(maxStep);
        setDeltaMovement(step);
    }

    // 命中后生成一个随机飞出方向，避免单怪场景在两个点之间来回打转。
    public Vec3 createPostHitRandomDirection() {
        // 优先沿当前攻击前进方向做小角度偏转，避免随机方向像蛇一样左右乱摆。
        Vec3 baseDirection = this.getDeltaMovement();
        if (baseDirection.lengthSqr() <= 0.001D && this.targetMonsterPos != null) {
            baseDirection = this.targetMonsterPos.subtract(this.position());
        }
        if (baseDirection.lengthSqr() <= 0.001D) {
            baseDirection = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 flatDirection = new Vec3(baseDirection.x, 0.0D, baseDirection.z);
        if (flatDirection.lengthSqr() <= 0.001D) {
            flatDirection = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 normalizedFlatDirection = flatDirection.normalize();
        double yawRadians = Math.toRadians((random.nextDouble() * 2.0D - 1.0D) * POST_HIT_RANDOM_YAW_DEGREES);
        double cos = Math.cos(yawRadians);
        double sin = Math.sin(yawRadians);
        Vec3 rotatedFlatDirection = new Vec3(
                normalizedFlatDirection.x * cos - normalizedFlatDirection.z * sin,
                0.0D,
                normalizedFlatDirection.x * sin + normalizedFlatDirection.z * cos
        );
        double upward = random.nextDouble() * POST_HIT_RANDOM_UP_SCALE;
        return rotatedFlatDirection.add(0.0D, upward, 0.0D).normalize();
    }

    // 检测附近可攻击目标并结算伤害。
    public boolean findAndHurtTarget() {
        entities = getSearchRangeTargets(this, 1.8);
        if (entities.isEmpty()) return false;

        entities.forEach(entity -> {
            if (this.master instanceof LivingEntity livingMaster) {
                entity.hurt(livingMaster.damageSources().mobAttack(livingMaster), this.attackDamage);
            } else {
                entity.hurt(this.damageSources().magic(), this.attackDamage);
            }
            setPlayerFood();
        });
        return true;
    }

    // 等待后再次寻找目标。
    public void waitState() {
        setWaitTick();
        playerNearEntities = getSearchRangeTargets(master, searchRange);
        if (playerNearEntities.isEmpty()) {
            setFlySwordMoveState(MoveState.BACK);
            return;
        }
        this.targetMonsterPos = getRandomPosition(playerNearEntities);
        setMovingState();
        setFlySwordMoveState(MoveState.MOVING);
    }

    // 回到玩家附近重新进入索敌。
    public void backToPlayer() {
        setFlySwordMoveState(MoveState.FOLLOW);
        findTarget();
    }

    // 获取范围内符合当前飞剑攻击配置的目标实体。
    public List<Entity> getSearchRangeTargets(Entity e, double searchRange) {
        if (e == null) return List.of();
        return level().getEntities(e, e.getBoundingBox().inflate(searchRange), this::canAttackTarget);
    }

    // 判断实体是否符合当前飞剑的索敌和伤害条件。
    public boolean canAttackTarget(Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return false;
        if (target == master || masterUUID != null && masterUUID.equals(target.getUUID())) return false;
        if (!ConfigFile.flySwordAttackAllEntities()) {
            return target instanceof Monster || target.getClassification(true) == MobCategory.MONSTER;
        }
        return !EntityUtil.isInDamageWhitelist(target);
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(FLY_SWORD_MOVE_STATE, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.entityData.set(FLY_SWORD_MOVE_STATE, pCompound.getInt("fly_sword_move_state"));
        if (pCompound.hasUUID("master_uuid")) {
            this.masterUUID = pCompound.getUUID("master_uuid");
        }
        this.ID = pCompound.getInt("fly_sword_entity_id");
        this.attackNum = pCompound.getInt("attack_num");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("fly_sword_move_state", this.entityData.get(FLY_SWORD_MOVE_STATE));
        if (this.masterUUID != null) {
            pCompound.putUUID("master_uuid", this.masterUUID);
        }
        pCompound.putInt("fly_sword_entity_id", this.ID);
        pCompound.putInt("attack_num", this.attackNum);
    }

    // 每累计命中一定次数后消耗主人饱和度。
    public void setPlayerFood() {
        attackNum++;
        if (attackNum >= deFoodNum) {
            attackNum = 0;
            if (this.master instanceof ServerPlayer player) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() - 1);
                }
            }
        }
    }

    public UUID getMasterUUID() {
        return masterUUID;
    }

    public void setMasterUUID(UUID ownerUUID) {
        this.masterUUID = ownerUUID;
    }

    public Entity getMaster() {
        return master;
    }

    public int getEntityID() {
        return ID;
    }

    public void setEntityID(int entityID) {
        this.ID = entityID;
    }

    public MoveState getMoveState() {
        return moveState;
    }

    public void setMoveState(MoveState moveState) {
        this.moveState = moveState;
    }

    // 同步并更新当前移动状态。
    public void setFlySwordMoveState(MoveState moveState) {
        setMoveState(moveState);
        this.entityData.set(FLY_SWORD_MOVE_STATE, moveState.getID());
    }

    public int getWaitTick() {
        return waitTick;
    }

    public void setWaitTick(int freeTick) {
        this.waitTick = freeTick;
    }

    public void setWaitTick() {
        this.waitTick--;
        if (waitTick < 0) {
            waitTick = 0;
        }
    }

    public List<Vec3> getPrePosList() {
        return prePosList;
    }

    // 记录拖尾历史点位。
    public void addPrePosList(Vec3 pos) {
        prePosList.addFirst(pos);
        if (prePosList.size() > 6) {
            prePosList.removeLast();
        }
    }

    // 计算飞剑朝向角度。
    public float calculateYAngle(Vec3 vec3) {
        double angleRadians = Math.atan2(vec3.z, vec3.x);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }
        return (float) (angleDegrees - 90);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        if (!this.level().isClientSide()) {
            super.lerpTo(x, y, z, yRot, xRot, steps, teleport);
            return;
        }

        // 远距离同步和召回直接落点，近距离同步走短插值，避免普通 Entity 的视觉跳点。
        double distanceToTargetSqr = this.position().distanceToSqr(x, y, z);
        if (teleport || distanceToTargetSqr > CLIENT_TELEPORT_DISTANCE * CLIENT_TELEPORT_DISTANCE) {
            this.clientLerpSteps = 0;
            this.setPos(x, y, z);
            this.setRot(yRot, xRot);
            return;
        }

        this.clientLerpX = x;
        this.clientLerpY = y;
        this.clientLerpZ = z;
        this.clientLerpYRot = yRot;
        this.clientLerpXRot = xRot;
        this.clientLerpSteps = Math.max(CLIENT_LERP_STEPS, steps);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean canAddPassenger(Entity pPassenger) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void positionRider(Entity pPassenger, MoveFunction pMoveFunction) {
    }

    public enum MoveState {
        FOLLOW(0),
        LIFTOFF(1),
        MOVING(2),
        ATTACK(3),
        WAIT(4),
        BACK(5);

        private final int id; // 状态编号。

        MoveState(int i) {
            this.id = i;
        }

        public int getID() {
            return this.id;
        }
    }
}
