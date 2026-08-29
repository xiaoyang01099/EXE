package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingFlightState;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
public class SparklingBoostC2SPacket {
    public static final double SEARCH_STEP = 0.5D;
    public static final double MIN_TELEPORT_DISTANCE = 1.0D;
    public static final Map<UUID, Long> NEXT_ALLOWED_TICK = new HashMap<>();

    public SparklingBoostC2SPacket() {
    }

    public SparklingBoostC2SPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!player.hasEffect(ModEffects.SPARKLING_EFFECT.get())) return;
            if (!canBoostNow(player)) return;

            tryTeleport(player);
        });
        context.setPacketHandled(true);
    }

    public static boolean tryTeleport(ServerPlayer player) {
        Vec3 origin = player.position();
        float height = player.getBbHeight();
        float width = player.getBbWidth();
        Vec3 target = findSafeTeleportTarget(player, ConfigFile.sparklingTeleportDistance());
        if (target == null) return false;

        player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;
        player.level().playSound(null, target.x, target.y, target.z,
                ModSounds.AAAAA.get(), player.getSoundSource(), 1.0F, 1.0F);

        SparklingFlightState.resetBoostTrails(player);
        sendTeleportParticles(player, origin, target, height, width);
        return true;
    }

    public static void sendTeleportParticles(ServerPlayer player, Vec3 origin, Vec3 target, float height, float width) {
        if (player == null || origin == null || target == null) return;
        NetworkHandler.sendToPlayer(new SparklingTeleportParticlesS2CPacket(origin, target, height, width), player);
    }

    public static Vec3 findSafeTeleportTarget(ServerPlayer player, double distance) {
        if (player == null || distance < MIN_TELEPORT_DISTANCE) return null;

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.position();
        for (double currentDistance = distance; currentDistance >= MIN_TELEPORT_DISTANCE; currentDistance -= SEARCH_STEP) {
            Vec3 position = origin.add(look.scale(currentDistance));
            if (isSafeTeleportPosition(player, position)) {
                return position;
            }
        }
        return null;
    }

    public static boolean isSafeTeleportPosition(ServerPlayer player, Vec3 position) {
        ServerLevel level = player.serverLevel();
        AABB targetBox = player.getBoundingBox().move(position.subtract(player.position()));
        if (!level.noCollision(player, targetBox)) return false;

        BlockPos feet = BlockPos.containing(position);
        BlockPos head = feet.above();
        return isSafeBodyBlock(level, feet) && isSafeBodyBlock(level, head);
    }

    public static boolean isSafeBodyBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!level.getFluidState(pos).isEmpty()) return false;
        return !isHazardBlock(state);
    }

    public static boolean isHazardBlock(BlockState state) {
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.LAVA)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.POWDER_SNOW);
    }

    public static boolean canBoostNow(ServerPlayer player) {
        long now = player.level().getGameTime();
        long nextAllowedTick = NEXT_ALLOWED_TICK.getOrDefault(player.getUUID(), 0L);
        if (now < nextAllowedTick) return false;
        int serverCooldown = Math.max(1, ConfigFile.sparklingTeleportCooldownTicks() - 1);
        NEXT_ALLOWED_TICK.put(player.getUUID(), now + serverCooldown);
        return true;
    }

    public static void clearCooldown(Player player) {
        if (player == null) return;
        NEXT_ALLOWED_TICK.remove(player.getUUID());
    }
}
