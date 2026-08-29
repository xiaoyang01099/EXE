package org.xiaoyang.ex_enigmaticlegacy.Client.particle.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SparklingFlightStateS2CPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SparklingFlightState {
    public static final String SNAPSHOT_PRESENT_TAG = "exeSparklingFlightSnapshot";
    public static final String SNAPSHOT_MAYFLY_TAG = "exeSparklingMayfly";
    public static final String SNAPSHOT_FLYING_TAG = "exeSparklingFlying";
    public static final String SNAPSHOT_SPEED_TAG = "exeSparklingFlyingSpeed";
    public static final Map<UUID, FlightSnapshot> SNAPSHOTS = new HashMap<>();
    public static final Map<UUID, BoostSession> BOOST_SESSIONS = new HashMap<>();

    public static void maintainFlight(Player player) {
        if (player == null || player.level().isClientSide()) return;
        Abilities abilities = player.getAbilities();
        if (!SNAPSHOTS.containsKey(player.getUUID())) {
            FlightSnapshot snapshot = readPersistedSnapshot(player);
            if (snapshot == null) {
                snapshot = new FlightSnapshot(abilities.mayfly, abilities.flying, abilities.getFlyingSpeed());
                writePersistedSnapshot(player, snapshot);
            }
            SNAPSHOTS.put(player.getUUID(), snapshot);
        }

        if (!abilities.mayfly || !abilities.flying) {
            abilities.mayfly = true;
            abilities.flying = true;
            player.onUpdateAbilities();
        }
        player.fallDistance = 0.0F;
    }

    public static boolean startBoost(ServerPlayer player) {
        if (!canBoost(player)) {
            sendBoostStateToPlayer(player, false);
            return false;
        }
        if (BOOST_SESSIONS.containsKey(player.getUUID())) return true;

        Vec3 look = getSafeLookDirection(player);
        double maxSpeed = ConfigFile.sparklingFlightBoostMaxSpeed();
        double startSpeed = Math.min(maxSpeed, Math.max(0.0D, player.getDeltaMovement().dot(look)));
        BOOST_SESSIONS.put(player.getUUID(), new BoostSession(player.level().getGameTime(), startSpeed));
        syncBoostState(player, true, false);
        return true;
    }

    public static void stopBoost(Player player) {
        if (player == null) return;
        BoostSession session = BOOST_SESSIONS.remove(player.getUUID());
        if (session == null) return;

        clearHorizontalPose(player, session);
        if (!player.level().isClientSide()) {
            syncBoostState(player, false, false);
        }
    }

    public static void tickBoost(Player player) {
        if (player == null || player.level().isClientSide()) return;
        BoostSession session = BOOST_SESSIONS.get(player.getUUID());
        if (session == null) return;
        if (!canBoost(player)) {
            stopBoost(player);
            return;
        }

        long elapsedTicks = Math.max(0L, player.level().getGameTime() - session.startTick);
        int accelerationTicks = Math.max(1, ConfigFile.sparklingFlightBoostAccelerationTicks());
        double progress = Math.min(1.0D, (double) elapsedTicks / (double) accelerationTicks);
        double maxSpeed = ConfigFile.sparklingFlightBoostMaxSpeed();
        double speed = Math.min(maxSpeed, session.startSpeed + (maxSpeed - session.startSpeed) * progress);
        Vec3 velocity = getSafeLookDirection(player).scale(speed);

        player.setDeltaMovement(velocity);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        updateMaxSpeedPose(player, session, progress >= 1.0D);
    }

    public static void updateMaxSpeedPose(Player player, BoostSession session, boolean atMaxSpeed) {
        if (session.maxSpeedReached == atMaxSpeed) return;
        session.maxSpeedReached = atMaxSpeed;
        if (atMaxSpeed) {
            Pose forcedPose = player.getForcedPose();
            if (forcedPose == null) {
                player.setForcedPose(Pose.FALL_FLYING);
                session.sparklingPoseApplied = true;
                session.horizontalPoseActive = true;
            } else if (forcedPose == Pose.FALL_FLYING) {
                session.horizontalPoseActive = true;
            }
            syncBoostState(player, true, session.horizontalPoseActive);
            return;
        }

        clearHorizontalPose(player, session);
        syncBoostState(player, true, false);
    }

    public static void clearHorizontalPose(Player player, BoostSession session) {
        if (player == null || session == null) return;
        if (session.sparklingPoseApplied && player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
        session.horizontalPoseActive = false;
        session.sparklingPoseApplied = false;
    }

    public static void restorePlayer(Player player) {
        if (player == null || player.level().isClientSide()) return;
        stopBoost(player);
        FlightSnapshot snapshot = SNAPSHOTS.remove(player.getUUID());
        if (snapshot == null) {
            snapshot = readPersistedSnapshot(player);
        }
        if (snapshot == null) return;

        Abilities abilities = player.getAbilities();
        if (!player.isCreative() && !player.isSpectator()) {
            abilities.mayfly = snapshot.mayfly;
            abilities.flying = snapshot.mayfly && snapshot.flying;
            abilities.setFlyingSpeed(snapshot.flyingSpeed);
        }
        clearPersistedSnapshot(player);
        player.fallDistance = 0.0F;
        player.onUpdateAbilities();
    }

    public static void restoreClonedPlayer(Player original, Player replacement) {
        if (original == null || replacement == null || replacement.level().isClientSide()) return;
        stopBoost(original);
        FlightSnapshot snapshot = SNAPSHOTS.remove(original.getUUID());
        if (snapshot == null) {
            snapshot = readPersistedSnapshot(original);
        }
        if (snapshot == null) return;

        Abilities abilities = replacement.getAbilities();
        if (!replacement.isCreative() && !replacement.isSpectator()) {
            abilities.mayfly = snapshot.mayfly;
            abilities.flying = snapshot.mayfly && snapshot.flying;
            abilities.setFlyingSpeed(snapshot.flyingSpeed);
        }
        clearPersistedSnapshot(original);
        clearPersistedSnapshot(replacement);
        replacement.fallDistance = 0.0F;
        replacement.onUpdateAbilities();
    }

    public static void stopBoostForDimensionChange(Player player) {
        stopBoost(player);
    }

    public static boolean canBoost(Player player) {
        return player != null
                && player.isAlive()
                && !player.isInWaterOrBubble()
                && player.hasEffect(ModEffects.SPARKLING_EFFECT.get());
    }

    public static boolean isBoosting(Player player) {
        return player != null && BOOST_SESSIONS.containsKey(player.getUUID());
    }

    public static void syncBoostState(Player player, boolean active, boolean horizontalPose) {
        if (player == null || player.level().isClientSide()) return;
        NetworkHandler.sendToTrackingEntityAndSelf(new SparklingFlightStateS2CPacket(
                player.getId(), active, horizontalPose, ConfigFile.sparklingFlightBoostMaxSpeed()), player);
    }

    public static void sendBoostStateToPlayer(Player target, ServerPlayer receiver) {
        if (target == null || receiver == null) return;
        BoostSession session = BOOST_SESSIONS.get(target.getUUID());
        boolean active = session != null;
        boolean horizontalPose = session != null && session.horizontalPoseActive;
        NetworkHandler.sendToPlayer(new SparklingFlightStateS2CPacket(
                target.getId(), active, horizontalPose, ConfigFile.sparklingFlightBoostMaxSpeed()), receiver);
    }

    public static void sendBoostStateToPlayer(ServerPlayer player, boolean active) {
        if (player == null) return;
        NetworkHandler.sendToPlayer(new SparklingFlightStateS2CPacket(
                player.getId(), active, false, ConfigFile.sparklingFlightBoostMaxSpeed()), player);
    }

    public static void resetBoostTrails(Player player) {
        BoostSession session = player == null ? null : BOOST_SESSIONS.get(player.getUUID());
        if (session == null) return;
        syncBoostState(player, false, false);
        syncBoostState(player, true, session.horizontalPoseActive);
    }

    public static Vec3 getSafeLookDirection(Player player) {
        Vec3 look = player == null ? Vec3.ZERO : player.getLookAngle();
        return look.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
    }

    public static FlightSnapshot readPersistedSnapshot(Player player) {
        if (player == null) return null;
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(SNAPSHOT_PRESENT_TAG)) return null;
        return new FlightSnapshot(
                data.getBoolean(SNAPSHOT_MAYFLY_TAG),
                data.getBoolean(SNAPSHOT_FLYING_TAG),
                data.getFloat(SNAPSHOT_SPEED_TAG));
    }

    public static void writePersistedSnapshot(Player player, FlightSnapshot snapshot) {
        if (player == null || snapshot == null) return;
        CompoundTag data = player.getPersistentData();
        data.putBoolean(SNAPSHOT_PRESENT_TAG, true);
        data.putBoolean(SNAPSHOT_MAYFLY_TAG, snapshot.mayfly);
        data.putBoolean(SNAPSHOT_FLYING_TAG, snapshot.flying);
        data.putFloat(SNAPSHOT_SPEED_TAG, snapshot.flyingSpeed);
    }

    public static void clearPersistedSnapshot(Player player) {
        if (player == null) return;
        CompoundTag data = player.getPersistentData();
        data.remove(SNAPSHOT_PRESENT_TAG);
        data.remove(SNAPSHOT_MAYFLY_TAG);
        data.remove(SNAPSHOT_FLYING_TAG);
        data.remove(SNAPSHOT_SPEED_TAG);
    }

    public static class FlightSnapshot {
        public final boolean mayfly;
        public final boolean flying;
        public final float flyingSpeed;

        public FlightSnapshot(boolean mayfly, boolean flying, float flyingSpeed) {
            this.mayfly = mayfly;
            this.flying = flying;
            this.flyingSpeed = flyingSpeed;
        }
    }

    public static class BoostSession {
        public final long startTick;
        public final double startSpeed;
        public boolean maxSpeedReached;
        public boolean horizontalPoseActive;
        public boolean sparklingPoseApplied;

        public BoostSession(long startTick, double startSpeed) {
            this.startTick = startTick;
            this.startSpeed = startSpeed;
        }
    }
}
