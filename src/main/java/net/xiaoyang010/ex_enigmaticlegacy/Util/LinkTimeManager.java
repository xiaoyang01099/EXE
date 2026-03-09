package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.EntityCursedManaBurst;
import vazkii.botania.common.entity.EntityManaBurst;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class LinkTimeManager {
    private static final Map<UUID, LinkTimeData> ACTIVE_PLAYERS = new ConcurrentHashMap<>();
    private static final Map<UUID, TickCounter> ENTITY_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<UUID, EntityFreezeData> FROZEN_ENTITIES = new ConcurrentHashMap<>();
    private static final Set<BlockEntity> FROZEN_BLOCK_ENTITIES = ConcurrentHashMap.newKeySet();
    private static final int TICK_DIVISOR = 4;
    private static final int EFFECT_RADIUS = 64;

    private static class EntityFreezeData {
        final Vec3 motion;
        final float yRot;
        final float xRot;
        final float yRotO;
        final float xRotO;

        EntityFreezeData(Entity entity) {
            this.motion = entity.getDeltaMovement();
            this.yRot = entity.getYRot();
            this.xRot = entity.getXRot();
            this.yRotO = entity.yRotO;
            this.xRotO = entity.xRotO;
        }

        void restore(Entity entity) {
            if (entity.isAlive() && !entity.isRemoved()) {
                entity.setDeltaMovement(this.motion);
                entity.setYRot(this.yRot);
                entity.setXRot(this.xRot);
                entity.yRotO = this.yRotO;
                entity.xRotO = this.xRotO;
            }
        }
    }

    private static class LinkTimeData {
        final UUID playerId;
        final ServerLevel level;
        long startTime;

        LinkTimeData(UUID playerId, ServerLevel level) {
            this.playerId = playerId;
            this.level = level;
            this.startTime = level.getGameTime();
        }
    }

    private static class TickCounter {
        int count = 0;

        boolean shouldTick() {
            if (count >= TICK_DIVISOR - 1) {
                count = 0;
                return true;
            } else {
                count++;
                return false;
            }
        }

        void reset() {
            count = 0;
        }
    }

    public static void activate(Player player) {
        if (player.level.isClientSide) return;

        ServerLevel level = (ServerLevel) player.level;
        ACTIVE_PLAYERS.put(player.getUUID(), new LinkTimeData(player.getUUID(), level));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.5F, 2.0F);
    }

    public static void deactivate(Player player) {
        LinkTimeData removed = ACTIVE_PLAYERS.remove(player.getUUID());

        if (removed != null) {
            ServerLevel level = removed.level;
            for (Map.Entry<UUID, EntityFreezeData> entry : FROZEN_ENTITIES.entrySet()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity != null) {
                    entry.getValue().restore(entity);
                }
            }

            ENTITY_COUNTERS.clear();
            FROZEN_ENTITIES.clear();
            FROZEN_BLOCK_ENTITIES.clear();

            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    public static boolean isActive(UUID playerId) {
        return ACTIVE_PLAYERS.containsKey(playerId);
    }

    public static boolean hasAnyActive() {
        return !ACTIVE_PLAYERS.isEmpty();
    }

    private static boolean shouldEntityUpdate(Entity entity, Player linkPlayer) {
        if (entity.getUUID().equals(linkPlayer.getUUID())) {
            return true;
        }

        if (entity instanceof AbstractArrow arrow) {
            if (arrow.getOwner() != null && arrow.getOwner().getUUID().equals(linkPlayer.getUUID())) {
                return true;
            }
        }

        if (entity instanceof EntityCursedManaBurst) {
            return true;
        }

        if (entity instanceof EntityManaBurst) {
            return true;
        }

        if (entity.distanceToSqr(linkPlayer) > EFFECT_RADIUS * EFFECT_RADIUS) {
            return true;
        }

        TickCounter counter = ENTITY_COUNTERS.computeIfAbsent(entity.getUUID(), k -> new TickCounter());
        return counter.shouldTick();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return;
        if (!hasAnyActive()) return;

        BlockEntity blockEntity = event.getWorld().getBlockEntity(event.getPos());
        if (blockEntity != null) {
            FROZEN_BLOCK_ENTITIES.remove(blockEntity);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;
        if (!(event.world instanceof ServerLevel level)) return;

        if (!hasAnyActive()) return;

        FROZEN_BLOCK_ENTITIES.removeIf(BlockEntity::isRemoved);

        for (LinkTimeData data : ACTIVE_PLAYERS.values()) {
            if (data.level != level) continue;

            Player player = level.getServer().getPlayerList().getPlayer(data.playerId);
            if (player == null) {
                ACTIVE_PLAYERS.remove(data.playerId);
                continue;
            }

            AABB area = new AABB(player.blockPosition()).inflate(EFFECT_RADIUS);
            List<Entity> entities = level.getEntities(player, area);

            for (Entity entity : entities) {
                if (entity == player) continue;

                if (!shouldEntityUpdate(entity, player)) {
                    freezeEntity(entity);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide) return;

        if (!hasAnyActive()) return;

        for (LinkTimeData data : ACTIVE_PLAYERS.values()) {
            if (data.level != entity.level) continue;

            Player player = data.level.getServer().getPlayerList().getPlayer(data.playerId);
            if (player == null) continue;

            if (!shouldEntityUpdate(entity, player)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (!isActive(player.getUUID())) return;

        if (player.getUseItem().isEmpty()) {
            deactivate(player);
        }
    }

    private static void freezeEntity(Entity entity) {
        FROZEN_ENTITIES.computeIfAbsent(entity.getUUID(), k -> new EntityFreezeData(entity));

        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.0001) {
            entity.setDeltaMovement(Vec3.ZERO);
        }

        entity.setYRot(entity.yRotO);
        entity.setXRot(entity.xRotO);

        if (entity instanceof AbstractArrow arrow) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(0.01));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!hasAnyActive()) {
            if (!ENTITY_COUNTERS.isEmpty()) {
                ENTITY_COUNTERS.clear();
            }
            if (!FROZEN_ENTITIES.isEmpty()) {
                FROZEN_ENTITIES.clear();
            }
            if (!FROZEN_BLOCK_ENTITIES.isEmpty()) {
                FROZEN_BLOCK_ENTITIES.clear();
            }
        } else {
            FROZEN_ENTITIES.entrySet().removeIf(entry -> {
                for (LinkTimeData data : ACTIVE_PLAYERS.values()) {
                    Entity entity = data.level.getEntity(entry.getKey());
                    if (entity == null || entity.isRemoved()) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) return;

        if (event.getWorld() instanceof ServerLevel level) {
            ACTIVE_PLAYERS.entrySet().removeIf(entry -> {
                LinkTimeData data = entry.getValue();
                if (data.level == level) {
                    Player player = level.getServer().getPlayerList().getPlayer(data.playerId);
                    if (player != null) {
                        for (Map.Entry<UUID, EntityFreezeData> freezeEntry : FROZEN_ENTITIES.entrySet()) {
                            Entity entity = level.getEntity(freezeEntry.getKey());
                            if (entity != null) {
                                freezeEntry.getValue().restore(entity);
                            }
                        }
                    }
                    return true;
                }
                return false;
            });
        }

        if (!hasAnyActive()) {
            cleanup();
        }
    }

    public static void cleanup() {
        ACTIVE_PLAYERS.clear();
        ENTITY_COUNTERS.clear();
        FROZEN_ENTITIES.clear();
        FROZEN_BLOCK_ENTITIES.clear();
    }
}