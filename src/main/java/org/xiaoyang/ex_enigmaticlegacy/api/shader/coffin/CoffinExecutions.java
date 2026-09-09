package org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;
import org.xiaoyang.ex_enigmaticlegacy.Mixin.LivingEntityAccess;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.CoffinPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public final class CoffinExecutions {
    public static final int DURATION = 180;
    public static int duration(int form) { return form==1?128:DURATION; }
    public static int hideAt(int form) { return form==1?106:150; }
    private static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Exe.MODID, "black_coffin"));
    private static final TicketType<UUID> TICKET = TicketType.create("bladeflash_coffin", UUID::compareTo);
    private static final ThreadLocal<Set<UUID>> EXECUTING = ThreadLocal.withInitial(HashSet::new);

    public static CoffinData data(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CoffinData::load, CoffinData::new, "bladeflash_coffins");
    }

    public static CoffinData.Entry entry(Entity entity) {
        if (entity.level().isClientSide || entity.getServer() == null) return null;
        if (entity.getServer().overworld() == null || !entity.getServer().isSameThread()) return null;
        return data(entity.getServer()).entries.get(entity.getUUID());
    }

    public static boolean locked(Entity entity) {
        return entry(entity) != null && !executing(entity);
    }

    public static boolean executing(Entity entity) {
        return EXECUTING.get().contains(entity.getUUID());
    }

    public static float damage(LivingEntity victim, DamageSource source, float amount) {
        if (victim.level().isClientSide || !(amount > 0) || executing(victim)) return amount;
        if (locked(victim)) return 0;
        if (!(source.getDirectEntity() instanceof LivingEntity attacker) || source.getEntity() != attacker
            || attacker == victim) return amount;
        var form=org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.coffin(attacker.getMainHandItem());
        if(form==org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Coffin.NONE) return amount;
        float health = victim.getHealth();
        if (!(health > 0) || !(health - amount < victim.getMaxHealth() * 0.4f)) return amount;
        CoffinData.Entry entry = new CoffinData.Entry();
        entry.form=form==org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Coffin.SECOND?1:0;
        entry.target = victim.getUUID(); entry.attacker = attacker.getUUID();
        entry.dimension = victim.level().dimension().location(); entry.anchor = victim.position();
        entry.yaw = victim.getYRot(); entry.pitch = victim.getXRot();
        entry.width = Math.max(1, victim.getBbWidth() / 1.4f);
        entry.height = Math.max(1, victim.getBbHeight() / 3.4f);
        entry.health = Math.max(0.01f, health - amount); entry.player = victim instanceof ServerPlayer;
        victim.stopRiding(); victim.ejectPassengers(); victim.stopUsingItem();
        CoffinData data = data(victim.getServer());
        data.entries.put(entry.target, entry); data.setDirty();
        hold(victim);
        CoffinPacket.broadcast(entry, (ServerLevel) victim.level(), false);
        return Math.min(amount, Math.max(0, health - entry.health));
    }

    public static float constrainedHealth(LivingEntity entity, float proposed) {
        if (executing(entity)) return 0;
        CoffinData.Entry entry = entry(entity);
        return entry == null ? proposed : entry.health;
    }

    public static void hold(Entity entity) {
        CoffinData.Entry entry = entry(entity);
        if (entry == null) return;
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPos(entry.anchor.x, entry.anchor.y, entry.anchor.z);
        entity.setYRot(entry.yaw); entity.setXRot(entry.pitch);
        entity.fallDistance = 0;
        if (entity instanceof LivingEntity living) {
            living.setYHeadRot(entry.yaw); living.setYBodyRot(entry.yaw);
            living.stopUsingItem(); living.walkAnimation.setSpeed(0);
        }
        if (entity instanceof Mob mob) mob.getNavigation().stop();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        CoffinData data = data(server);
        for (CoffinData.Entry entry : new ArrayList<>(data.entries.values())) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, entry.dimension));
            if (level == null) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(entry.target);
            if (!entry.player || player != null)
                level.getChunkSource().addRegionTicket(TICKET, new ChunkPos(BlockPos.containing(entry.anchor)), 2, entry.target);
            Entity entity = entry.player ? player : level.getEntity(entry.target);
            if (entry.age < duration(entry.form)) entry.age++;
            if (entity instanceof LivingEntity living && !living.isRemoved()) {
                entry.missingTicks = 0;
                hold(living);
                if (player != null && entry.age % 10 == 1)
                    player.connection.teleport(entry.anchor.x, entry.anchor.y, entry.anchor.z, entry.yaw, entry.pitch);
                if (entry.age >= duration(entry.form)) {
                    execute(living, entry);
                    data.entries.remove(entry.target);
                    level.getChunkSource().removeRegionTicket(TICKET, new ChunkPos(BlockPos.containing(entry.anchor)), 2, entry.target);
                    CoffinPacket.broadcast(entry, level, true);
                }
            } else if (entry.player) {
                level.getChunkSource().removeRegionTicket(TICKET, new ChunkPos(BlockPos.containing(entry.anchor)), 2, entry.target);
            } else if (++entry.missingTicks > 200) {
                data.entries.remove(entry.target);
                level.getChunkSource().removeRegionTicket(TICKET, new ChunkPos(BlockPos.containing(entry.anchor)), 2, entry.target);
                CoffinPacket.broadcast(entry, level, true);
            }
            data.setDirty();
        }
    }

    private static void execute(LivingEntity victim, CoffinData.Entry entry) {
        EXECUTING.get().add(entry.target);
        try {
            ServerLevel level = (ServerLevel) victim.level();
            Entity attacker = level.getEntity(entry.attacker);
            if (attacker == null) attacker = level.getServer().getPlayerList().getPlayer(entry.attacker);
            DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DAMAGE), attacker);
            victim.getCombatTracker().recordDamage(source, Math.max(victim.getHealth(), 1));
            victim.setHealth(0);
            victim.getEntityData().set(LivingEntityAccess.bladeflash$healthKey(), 0f);
            victim.die(source);
            victim.getEntityData().set(LivingEntityAccess.bladeflash$healthKey(), 0f);
            if (!(victim instanceof ServerPlayer)) victim.remove(Entity.RemovalReason.KILLED);
        } finally {
            EXECUTING.get().remove(entry.target);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(LivingAttackEvent event) {
        if (locked(event.getEntity()) || (event.getSource().getEntity() != null && locked(event.getSource().getEntity())))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void heal(LivingHealEvent event) { if (locked(event.getEntity())) event.setCanceled(true); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void death(LivingDeathEvent event) { if (locked(event.getEntity())) event.setCanceled(true); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void teleport(EntityTeleportEvent event) {
        if (event.isCancelable() && locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void travel(EntityTravelToDimensionEvent event) { if (locked(event.getEntity())) event.setCanceled(true); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void explode(ExplosionEvent.Start event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        if (source != null && locked(source)) event.setCanceled(true);
    }

    private static void sync(ServerPlayer player) {
        for (CoffinData.Entry entry : data(player.server).entries.values()) CoffinPacket.send(entry, player);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CoffinData.Entry entry = entry(event.getTarget());
            if (entry != null) CoffinPacket.send(entry, player);
        }
    }

    private CoffinExecutions() {}
}
