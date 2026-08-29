package org.xiaoyang.ex_enigmaticlegacy.Client.particle.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.SparklingFlightParticles;

import java.util.*;

// SparklingFlightClientState 保存客户端加速玩家状态和有界历史位置，并驱动金黄色 GPU 拖尾。
public class SparklingFlightClientState {
    public static final Set<Integer> ACTIVE_ENTITY_IDS = new HashSet<>(); // 当前客户端收到的加速飞行玩家实体 ID。
    public static final Map<Integer, FlightTrailState> TRAIL_STATES = new HashMap<>(); // 每个活动玩家的历史轨迹状态。
    public static final int MAX_HISTORY_POINTS = 20; // 每个玩家最多保留的历史位置数量。
    public static final int MAX_SAMPLES_PER_EMISSION = 8; // 单玩家单次最多提交的轨迹采样数量。
    public static final int EMISSION_INTERVAL_TICKS = 1; // 相邻两次 GPU 粒子发射的最少客户端 tick。
    public static final int MAX_MISSING_TICKS = 40; // 状态包先于实体到达时允许等待的客户端 tick 数。
    public static final double SAMPLE_SPACING = 0.75D; // 已完成历史段中相邻粒子采样点的目标间距。
    public static final double MIN_EMISSION_DISTANCE = 0.35D; // 累计移动达到该距离后才允许发射轨迹粒子。
    public static final double MIN_CONTINUOUS_DISTANCE = 24.0D; // 异常跳变阈值的最低值。
    public static ClientLevel currentLevel; // 当前缓存所属客户端世界，用于切换世界时清理实体 ID。

    // 应用本地预测或服务端同步的加速开关。
    public static void apply(int entityId, boolean boosting, boolean horizontalPose, double maxSpeed) {
        if (entityId < 0) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (currentLevel != null && currentLevel != level) {
            clear();
        }
        currentLevel = level;
        if (boosting) {
            ACTIVE_ENTITY_IDS.add(entityId);
            FlightTrailState trail = TRAIL_STATES.computeIfAbsent(entityId, id -> new FlightTrailState());
            trail.horizontalPoseRequested = horizontalPose;
            trail.maxContinuousDistance = calculateMaxContinuousDistance(maxSpeed);
            if (level != null && level.getEntity(entityId) instanceof Player player) {
                updateHorizontalPose(player, trail);
            }
            return;
        }
        deactivate(entityId);
    }

    // 每客户端 tick 更新所有活动玩家的历史位置和 GPU 粒子。
    public static void tick(ClientLevel level) {
        if (level == null) {
            clear();
            return;
        }
        if (currentLevel == null) {
            currentLevel = level;
        } else if (currentLevel != level) {
            clear();
            currentLevel = level;
        }
        if (ACTIVE_ENTITY_IDS.isEmpty()) return;

        // 使用实体 ID 快照，避免网络包在遍历期间修改活动集合。
        List<Integer> activeIds = new ArrayList<>(ACTIVE_ENTITY_IDS);
        for (int entityId : activeIds) {
            tickEntity(level, entityId);
        }
    }

    // 更新一个活动玩家，实体暂未生成时保留短暂等待窗口。
    public static void tickEntity(ClientLevel level, int entityId) {
        FlightTrailState trail = TRAIL_STATES.computeIfAbsent(entityId, id -> new FlightTrailState());
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof Player player) || !entity.isAlive()) {
            trail.missingTicks++;
            if (trail.missingTicks > MAX_MISSING_TICKS) {
                apply(entityId, false, false, 0.0D);
            }
            return;
        }

        trail.missingTicks = 0;
        updateHorizontalPose(player, trail);
        Vec3 currentPosition = getBodyCenter(player);
        if (trail.segmentEnd == null) {
            trail.segmentEnd = currentPosition;
            return;
        }
        if (trail.segmentStart == null) {
            trail.segmentStart = trail.segmentEnd;
            trail.segmentEnd = currentPosition;
            return;
        }

        // 只消费上一 tick 已经完成的轨迹段，当前最新位置留到下一 tick，避免粒子领先插值模型。
        Vec3 segment = trail.segmentEnd.subtract(trail.segmentStart);
        double distance = segment.length();
        if (distance > trail.maxContinuousDistance) {
            resetTrailPositions(trail, currentPosition);
            return;
        }

        if (distance >= 0.01D) {
            Vec3 movementDirection = segment.normalize();
            double backwardOffset = getBackwardOffset(player);
            Vec3 anchorStart = trail.segmentStart.subtract(movementDirection.scale(backwardOffset));
            Vec3 anchorEnd = trail.segmentEnd.subtract(movementDirection.scale(backwardOffset));
            appendPendingSegment(trail, anchorStart, anchorEnd);
        }
        trail.segmentStart = trail.segmentEnd;
        trail.segmentEnd = currentPosition;
    }

    // 把一条已完成历史段加入待发射路径，并按时间和累计距离限制发射频率。
    public static void appendPendingSegment(FlightTrailState trail, Vec3 start, Vec3 end) {
        if (trail.pendingPath.isEmpty()) {
            trail.pendingPath.addLast(start);
        } else if (trail.pendingPath.peekLast().distanceTo(start) >= 0.01D) {
            trail.pendingPath.addLast(start);
        }
        trail.pendingPath.addLast(end);
        trail.pendingDistance += start.distanceTo(end);
        trail.emissionCooldown++;
        if (trail.emissionCooldown < EMISSION_INTERVAL_TICKS || trail.pendingDistance < MIN_EMISSION_DISTANCE) return;
        emitPendingPath(trail);
    }

    // 沿待发射折线路径最多取四个内部采样点，不生成最新端点粒子。
    public static void emitPendingPath(FlightTrailState trail) {
        if (trail.pendingPath.size() < 2) return;
        List<Vec3> path = new ArrayList<>(trail.pendingPath);
        int remainingSamples = MAX_SAMPLES_PER_EMISSION;
        for (int pathIndex = 0; pathIndex < path.size() - 1 && remainingSamples > 0; pathIndex++) {
            Vec3 start = path.get(pathIndex);
            Vec3 end = path.get(pathIndex + 1);
            Vec3 segment = end.subtract(start);
            double distance = segment.length();
            if (distance < 0.01D) continue;

            int sampleCount = Math.min(remainingSamples, Math.max(1, (int) Math.ceil(distance / SAMPLE_SPACING)));
            Vec3 backwardDirection = segment.scale(-1.0D).normalize();
            for (int i = 1; i <= sampleCount; i++) {
                double ratio = (double) i / (double) (sampleCount + 1);
                Vec3 sample = start.lerp(end, ratio);
                addHistoryPoint(trail, sample);
                SparklingFlightParticles.emit(sample, backwardDirection);
            }
            remainingSamples -= sampleCount;
        }

        Vec3 lastPoint = trail.pendingPath.peekLast();
        trail.pendingPath.clear();
        if (lastPoint != null) {
            trail.pendingPath.addLast(lastPoint);
        }
        trail.pendingDistance = 0.0D;
        trail.emissionCooldown = 0;
    }

    // 读取玩家当前身体中心，位置先进入延迟历史段，不在当前 tick 直接发射。
    public static Vec3 getBodyCenter(Player player) {
        return player.position().add(0.0D, player.getBbHeight() * 0.45D, 0.0D);
    }

    // 第一人称轨迹多后移一些以避开镜头，其他视角保持贴近玩家后方。
    public static double getBackwardOffset(Player player) {
        return isFirstPersonLocalPlayer(player) ? 0.65D : 0.35D;
    }

    // 按服务端同步的最大速度动态放宽合法连续移动阈值。
    public static double calculateMaxContinuousDistance(double configuredMaxSpeed) {
        double maxSpeed = Math.max(0.0D, configuredMaxSpeed);
        double scaledSpeed = maxSpeed > Double.MAX_VALUE / 1.25D ? Double.MAX_VALUE : maxSpeed * 1.25D;
        return Math.max(MIN_CONTINUOUS_DISTANCE, scaledSpeed);
    }

    // 判断当前轨迹是否属于第一人称本地玩家。
    public static boolean isFirstPersonLocalPlayer(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && minecraft.player.getId() == player.getId()
                && minecraft.options.getCameraType().isFirstPerson();
    }

    // 写入有界历史队列，超过上限时移除最旧点。
    public static void addHistoryPoint(FlightTrailState trail, Vec3 point) {
        trail.history.addFirst(point);
        while (trail.history.size() > MAX_HISTORY_POINTS) {
            trail.history.removeLast();
        }
    }

    // 判断指定实体当前是否被服务端标记为加速飞行。
    public static boolean isActive(int entityId) {
        return ACTIVE_ENTITY_IDS.contains(entityId);
    }

    // 判断指定实体当前是否处于服务端要求的满速横向飞行姿态。
    public static boolean isHorizontalPoseActive(int entityId) {
        if (!ACTIVE_ENTITY_IDS.contains(entityId)) return false;
        FlightTrailState trail = TRAIL_STATES.get(entityId);
        return trail != null && trail.horizontalPoseRequested;
    }

    // 根据服务端满速状态设置或清理客户端玩家强制横飞姿态。
    public static void updateHorizontalPose(Player player, FlightTrailState trail) {
        if (player == null || trail == null) return;
        if (trail.horizontalPoseRequested) {
            Pose forcedPose = player.getForcedPose();
            if (forcedPose == null) {
                player.setForcedPose(Pose.FALL_FLYING);
                trail.sparklingPoseApplied = true;
            }
            return;
        }
        clearHorizontalPose(player, trail);
    }

    // 只解除当前客户端确实由闪闪果实设置的强制姿态。
    public static void clearHorizontalPose(Player player, FlightTrailState trail) {
        if (player == null || trail == null) return;
        if (trail.sparklingPoseApplied && player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
        trail.sparklingPoseApplied = false;
    }

    // 停止一个客户端加速状态，并在删除缓存前解除对应强制姿态。
    public static void deactivate(int entityId) {
        FlightTrailState trail = TRAIL_STATES.remove(entityId);
        if (trail != null && currentLevel != null && currentLevel.getEntity(entityId) instanceof Player player) {
            clearHorizontalPose(player, trail);
        }
        ACTIVE_ENTITY_IDS.remove(entityId);
    }

    // 异常跳变时清空所有未消费段，并以当前位置重新建立延迟轨迹。
    public static void resetTrailPositions(FlightTrailState trail, Vec3 currentPosition) {
        trail.history.clear();
        trail.pendingPath.clear();
        trail.pendingDistance = 0.0D;
        trail.emissionCooldown = 0;
        trail.segmentStart = null;
        trail.segmentEnd = currentPosition;
    }

    // 清空世界相关的活动实体和历史轨迹，防止实体 ID 在新世界中复用。
    public static void clear() {
        if (currentLevel != null) {
            for (Map.Entry<Integer, FlightTrailState> entry : TRAIL_STATES.entrySet()) {
                if (currentLevel.getEntity(entry.getKey()) instanceof Player player) {
                    clearHorizontalPose(player, entry.getValue());
                }
            }
        }
        ACTIVE_ENTITY_IDS.clear();
        TRAIL_STATES.clear();
        currentLevel = null;
    }

    // FlightTrailState 保存单个玩家最近的客户端飞行位置。
    public static class FlightTrailState {
        public final Deque<Vec3> history = new ArrayDeque<>(); // 最近的历史位置，队首为最新点。
        public final Deque<Vec3> pendingPath = new ArrayDeque<>(); // 等待低频发射的已完成轨迹折线。
        public Vec3 segmentStart; // 延迟轨迹段的较旧身体中心。
        public Vec3 segmentEnd; // 延迟轨迹段的最新已记录身体中心。
        public double pendingDistance; // 距离上次发射后累计的已完成路径长度。
        public int emissionCooldown; // 距离上次 GPU 粒子发射的客户端 tick 数。
        public int missingTicks; // 状态已激活但客户端暂时找不到实体的 tick 数。
        public boolean horizontalPoseRequested; // 服务端是否要求该玩家显示满速横向姿态。
        public boolean sparklingPoseApplied; // 客户端强制姿态是否确实由闪闪果实设置。
        public double maxContinuousDistance = MIN_CONTINUOUS_DISTANCE; // 按服务端最大速度计算的合法单 tick 距离阈值。
    }
}
