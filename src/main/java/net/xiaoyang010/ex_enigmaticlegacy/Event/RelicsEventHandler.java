package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.*;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.block.subtile.functional.SubTileHeiseiDream;
import vazkii.botania.common.entity.EntityDoppleganger;
import vazkii.botania.common.helper.ItemNBTHelper;

import java.util.*;

public class RelicsEventHandler {
    public static List<String> darkRingDamageNegations = new ArrayList<>();
    public static HashMap<ServerPlayer, Integer> castingCooldowns = new HashMap<>();

    static {
        darkRingDamageNegations.add("inFire");          // 站在火里
        darkRingDamageNegations.add("onFire");          // 着火状态
        darkRingDamageNegations.add("lava");            // 岩浆伤害
        darkRingDamageNegations.add("hotFloor");        // 岩浆块
        darkRingDamageNegations.add("fireball");        // 火球伤害
        darkRingDamageNegations.add("witherSkull");     // 凋离骷髅头
        darkRingDamageNegations.add("fire");            // 通用火焰
        darkRingDamageNegations.add("magma");           // 岩浆相关
        darkRingDamageNegations.add("magmaBlock");      // 岩浆块
    }

    public static boolean canEntityBeSeen(Entity entity, double x, double y, double z) {
        Vec3 start = new Vec3(x, y, z);
        Vec3 end = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        ClipContext context = new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null);
        BlockHitResult result = entity.level.clip(context);
        return result == null || result.getType() == BlockHitResult.Type.MISS;
    }

    public static boolean validatePosition(Level world, int x, int y, int z) {
        BlockPos belowPos = new BlockPos(x, y - 1, z);
        BlockPos currentPos = new BlockPos(x, y, z);
        BlockPos abovePos = new BlockPos(x, y + 1, z);

        BlockState belowState = world.getBlockState(belowPos);
        BlockState currentState = world.getBlockState(currentPos);
        BlockState aboveState = world.getBlockState(abovePos);

        return !belowState.isAir() &&
                belowState.getCollisionShape(world, belowPos).isEmpty() == false &&
                currentState.isAir() &&
                aboveState.isAir();
    }

    public static BlockHitResult getPointedBlock(Player player, Level world, float range) {
        double d0 = player.getX();
        double d1 = player.getY() + player.getEyeHeight();
        double d2 = player.getZ();
        Vec3 position = new Vec3(d0, d1, d2);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 finalvec = position.add(look.x * (double)range, look.y * (double)range, look.z * (double)range);
        BlockHitResult mop = world.clip(new ClipContext(position, finalvec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return mop;
    }

    public static void imposeBurst(Level world, double x, double y, double z, float size) {
        if (!world.isClientSide) {
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            x, y, z, 128.0, world.dimension())),
                    new BurstMessage(x, y, z, size)
            );
        }
    }

    public static ArrayList<Entity> getEntitiesInRange(Level world, double x, double y, double z, Entity entity, Class<? extends Entity> clazz, double range) {
        ArrayList<Entity> out = new ArrayList<>();

        AABB searchArea = new AABB(
                x - range, y - range, z - range,
                x + range, y + range, z + range
        );

        List<? extends Entity> entities = world.getEntitiesOfClass(clazz, searchArea);

        if (!entities.isEmpty()) {
            for (Entity ent : entities) {
                if (entity == null || !entity.getUUID().equals(ent.getUUID())) {
                    out.add(ent);
                }
            }
        }

        return out;
    }

    public static Entity getPointedEntity(Level world, Entity entityplayer, double minrange, double range, float padding) {
        return getPointedEntity(world, entityplayer, minrange, range, padding, false);
    }

    public static Entity getPointedEntity(Level world, Entity entityplayer, double minrange, double range, float padding, boolean nonCollide) {
        Entity pointedEntity = null;
        Vec3 vec3d = new Vec3(entityplayer.getX(), entityplayer.getY() + (double)entityplayer.getEyeHeight(), entityplayer.getZ());
        Vec3 vec3d1 = entityplayer.getViewVector(1.0F);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range);

        AABB searchArea = entityplayer.getBoundingBox()
                .expandTowards(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range)
                .inflate((double)padding, (double)padding, (double)padding);

        List<Entity> list = world.getEntities(entityplayer, searchArea);
        double d2 = 0.0;

        for (Entity entity : list) {
            if (!(entity.distanceTo(entityplayer) < minrange) &&
                    (entity.isPickable() || nonCollide) &&
                    world.clip(new ClipContext(
                            new Vec3(entityplayer.getX(), entityplayer.getY() + (double)entityplayer.getEyeHeight(), entityplayer.getZ()),
                            new Vec3(entity.getX(), entity.getY() + (double)entity.getEyeHeight(), entity.getZ()),
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            entityplayer
                    )).getType() == HitResult.Type.MISS) {

                float f2 = Math.max(0.8F, entity.getPickRadius());
                AABB axisalignedbb = entity.getBoundingBox().inflate((double)f2, (double)f2, (double)f2);

                if (axisalignedbb.contains(vec3d)) {
                    if (0.0 < d2 || d2 == 0.0) {
                        pointedEntity = entity;
                        d2 = 0.0;
                    }
                } else {
                    Vec3 clipResult = axisalignedbb.clip(vec3d, vec3d2).orElse(null);
                    if (clipResult != null) {
                        double d3 = vec3d.distanceTo(clipResult);
                        if (d3 < d2 || d2 == 0.0) {
                            pointedEntity = entity;
                            d2 = d3;
                        }
                    }
                }
            }
        }

        return pointedEntity;
    }

    public static List<ItemStack> itemSearch(Player player, Item searchItem) {
        List<ItemStack> itemStackList = new LinkedList<>();

        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack stackInSlot = player.getInventory().items.get(slot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == searchItem) {
                itemStackList.add(stackInSlot);
            }
        }

        return itemStackList;
    }

    public static void insanelyDisastrousConsequences(Player player) {
        while (player.getInventory().contains(new ItemStack(ModItems.FATE_TOME.get()))) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == ModItems.FATE_TOME.get()) {
                    player.getInventory().removeItem(i, stack.getCount());
                    break;
                }
            }
        }

        List<LivingEntity> entityList = player.level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        player.getX() - 64.0, player.getY() - 64.0, player.getZ() - 64.0,
                        player.getX() + 64.0, player.getY() + 64.0, player.getZ() + 64.0
                )
        );

        if (!entityList.isEmpty()) {
            for (LivingEntity entity : entityList) {
                entity.hurt(DamageSource.MAGIC, 40000.0F);
                player.level.explode(
                        player,
                        entity.getX(), entity.getY(), entity.getZ(),
                        16.0F,
                        Explosion.BlockInteraction.BREAK
                );
            }
        }

        player.level.explode(
                player,
                player.getX(), player.getY(), player.getZ(),
                100.0F,
                Explosion.BlockInteraction.BREAK
        );
    }

    public static boolean isOnCoodown(Player player) {
        if (player.level.isClientSide) {
            return false;
        } else {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return false;
            }

            int cooldown;
            try {
                cooldown = castingCooldowns.get(serverPlayer);
            } catch (NullPointerException var3) {
                castingCooldowns.put(serverPlayer, 0);
                cooldown = 0;
            }

            return cooldown != 0;
        }
    }

    public static boolean isEntityBlacklistedFromTelekinesis(LivingEntity entity) {
        return entity instanceof EntityDoppleganger;
    }

    public static void imposeLightning(Level world, double x, double y, double z, double destX, double destY, double destZ, int duration, float curve, int speed, int type, float width) {
        if (!world.isClientSide) {
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            x, y, z, 128.0, world.dimension()
                    )),
                    new LightningMessage(x, y, z, destX, destY, destZ, duration, curve, speed, type, width)
            );
        }
    }

    public static void setCasted(Player player, int cooldown, boolean swing) {
        if (!player.level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            castingCooldowns.put(serverPlayer, cooldown);
            if (swing) {
                player.swing(player.getUsedItemHand());
                 NetworkHandler.sendToPlayer(serverPlayer, new ICanSwingMySwordMessage());
            }
        }
    }

    public static boolean hasBauble(Player player, Item item) {
        var curioResult = CuriosApi.getCuriosHelper().findEquippedCurio(item, player);
        if (curioResult.isPresent()) {
            ItemStack stack = curioResult.get().getRight();
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                return relic != null && relic.isRightPlayer(player);
            }
            return true;
        }
        return false;
    }

    public static List<Player> getBaubleOwnersList(Level world, Item baubleItem) {
        List<Player> returnList = new LinkedList<>();

        if (!world.isClientSide) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                    var curioResult = CuriosApi.getCuriosHelper().findEquippedCurio(baubleItem, serverPlayer);
                    if (curioResult.isPresent()) {
                        ItemStack stack = curioResult.get().getRight();
                        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
                        if (relicCap.isPresent()) {
                            IRelic relic = relicCap.orElse(null);
                            if (relic != null && relic.isRightPlayer(serverPlayer)) {
                                returnList.add(serverPlayer);
                            }
                        } else {
                            returnList.add(serverPlayer);
                        }
                    }
                }
            }
        }

        return returnList;
    }

    public static boolean isDamageTypeAbsolute(DamageSource source) {
        return source.isBypassInvul() || source.isBypassArmor() ||
                source instanceof ModDamageSources.DamageSourceSuperposition ||
                source instanceof ModDamageSources.DamageSourceSuperpositionDefined ||
                source instanceof ModDamageSources.DamageSourceTrueDamage ||
                source instanceof ModDamageSources.DamageSourceTrueDamageUndef ||
                source instanceof ModDamageSources.DamageSourceFate;
    }

    public static ItemStack findFirst(Player player, Item searchItem) {
        for(int slot = 0; slot < player.getInventory().items.size(); ++slot) {
            if (!player.getInventory().items.get(slot).isEmpty() && player.getInventory().items.get(slot).getItem() == searchItem) {
                return player.getInventory().items.get(slot);
            }
        }
        return null;
    }

    public static void cryHavoc(Level world, Player player, int RANGE) {
        List<Mob> hostileMobs = world.getEntitiesOfClass(Mob.class,
                        new AABB(
                                player.getX() - RANGE,
                                player.getY() - RANGE,
                                player.getZ() - RANGE,
                                player.getX() + RANGE + 1.0,
                                player.getY() + RANGE + 1.0,
                                player.getZ() + RANGE + 1.0
                        )
                ).stream()
                .filter(mob -> mob instanceof Enemy)
                .toList();

        if (hostileMobs.size() > 1) {
            List<Enemy> enemies = hostileMobs.stream()
                    .map(mob -> (Enemy)mob)
                    .collect(java.util.stream.Collectors.toList());

            for(Mob mob : hostileMobs) {
                if (SubTileHeiseiDream.brainwashEntity(mob, enemies)) {
                    break;
                }
            }
        }
    }

    public static boolean validTeleportRandomly(Entity entity, Level level, int range) {
        for (int attempts = 0; attempts < 32; attempts++) {
            double x = entity.getX() + (Math.random() - 0.5) * range * 2;
            double y = entity.getY() + (Math.random() - 0.5) * 8;
            double z = entity.getZ() + (Math.random() - 0.5) * range * 2;

            BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
            BlockPos posAbove = pos.above();

            if (level.isEmptyBlock(pos) && level.isEmptyBlock(posAbove) &&
                    !level.isEmptyBlock(pos.below())) {

                Vec3 start = entity.position();
                Vec3 end = new Vec3(x, y, z);
                BlockHitResult hitResult = level.clip(new ClipContext(start, end,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

                if (hitResult.getType() == HitResult.Type.MISS) {
                    entity.teleportTo(x, y, z);
                    return true;
                }
            }
        }
        return false;
    }

    public static Player findPlayerWithBauble(Level world, int range, Item baubleItem, Player excludePlayer) {
        if (world.isClientSide) {
            return null;
        }

        List<Player> players = world.getEntitiesOfClass(Player.class,
                new AABB(
                        excludePlayer.getX() - range, excludePlayer.getY() - range, excludePlayer.getZ() - range,
                        excludePlayer.getX() + range, excludePlayer.getY() + range, excludePlayer.getZ() + range
                ));

        for (Player player : players) {
            if (player != excludePlayer && hasBauble(player, baubleItem)) {
                return player;
            }
        }
        return null;
    }

    public static void sendNotification(Player player, int type) {

        switch (type) {
            case 1:
                break;
            case 2:
                if (player instanceof ServerPlayer serverPlayer) {
                }
                break;
            default:
                break;
        }
    }

    @SubscribeEvent
    public void livingTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.getEntity().level.isClientSide) {

            if (castingCooldowns.containsKey(player)) {
                int cooldown = castingCooldowns.get(player);
                if (cooldown > 0) {
                    cooldown--;
                    castingCooldowns.put(player, cooldown);
                    return;
                }
            } else {
                castingCooldowns.put(player, 0);
            }
        }

        if (16.0F > 0.0F
                && event.getEntity() instanceof EntityDoppleganger guardian
                && !event.getEntity().level.isClientSide
                && event.getEntity().tickCount > 100) {

            Vector3 pos = Vector3.fromEntityCenter(guardian);
            double range = 16.0F;
            AABB boundingBox = new AABB(
                    guardian.getX() - range, guardian.getY() - range, guardian.getZ() - range,
                    guardian.getX() + range, guardian.getY() + range, guardian.getZ() + range
            );

            if (guardian.level.containsAnyLiquid(boundingBox)) {
                NetworkHandler.sendToPlayer(
                        new PacketVoidMessage(pos.x, pos.y, pos.z, true),
                        guardian.level,
                        new BlockPos(guardian.getX(), guardian.getY(), guardian.getZ()),
                        64.0
                );

                if (64.0F != 0.0F) {
                    if (64.0F > 0.0F) {
                        NetworkHandler.sendToPlayer(
                                new GuardianVanishMessage(),
                                guardian.level,
                                new BlockPos(guardian.getX(), guardian.getY(), guardian.getZ()),
                                64.0F
                        );
                    } else {
                        NetworkHandler.sendToPlayer(new GuardianVanishMessage());
                    }
                }

                ClientProxy proxy = new ClientProxy();
                ClientProxy.spawnSuperParticle(
                        guardian.level, "explosion",
                        pos.x, pos.y, pos.z,
                        0, 0, 0, 2.0F, 64.0F
                );

                guardian.level.playSound(null, guardian.getX(), guardian.getY(), guardian.getZ(),
                        SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 4.0F,
                        (float)(0.9F + Math.random() * 0.1F));

                if (false) {
                }

                guardian.discard();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityAttacked(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attackerPlayer && !event.isCanceled()) {
            if (attackerPlayer.getInventory().contains(new ItemStack(ModItems.CHAOS_CORE.get()))
                    && Math.random() < 0.45) {

                List<Entity> entityList = event.getEntity().level.getEntitiesOfClass(
                        Entity.class,
                        new AABB(
                                event.getEntity().getX() - 16.0F, event.getEntity().getY() - 16.0F, event.getEntity().getZ() - 16.0F,
                                event.getEntity().getX() + 16.0F, event.getEntity().getY() + 16.0F, event.getEntity().getZ() + 16.0F
                        ),
                        entity -> entity != event.getEntity()
                );

                if (!entityList.isEmpty()) {
                    Entity randomEntity = entityList.get((int)(Math.random() * entityList.size()));
                    float redirectedAmount = event.getAmount() * (float)(Math.random() * 2.0F);

                    if (Math.random() < 0.15) {
                        attackerPlayer.hurt(event.getSource(), redirectedAmount);
                    } else {
                        randomEntity.hurt(event.getSource(), redirectedAmount);
                    }
                    event.setCanceled(true);
                }
            }
        }

        if (event.getEntity() instanceof Player player) {
            if (!event.isCanceled()
                    && player.getInventory().contains(new ItemStack(ModItems.CHAOS_CORE.get()))
                    && Math.random() < 0.42) {

                List<Entity> entityList = event.getEntity().level.getEntitiesOfClass(
                        Entity.class,
                        new AABB(
                                event.getEntity().getX() - 16.0F, event.getEntity().getY() - 16.0F, event.getEntity().getZ() - 16.0F,
                                event.getEntity().getX() + 16.0F, event.getEntity().getY() + 16.0F, event.getEntity().getZ() + 16.0F
                        ),
                        entity -> entity != event.getEntity()
                );

                if (!entityList.isEmpty()) {
                    Entity randomEntity = entityList.get((int)(Math.random() * entityList.size()));
                    float redirectedAmount = event.getAmount() * (float)(Math.random() * 2.0F);
                    randomEntity.hurt(event.getSource(), redirectedAmount);
                    event.setCanceled(true);
                }
            }

            // 星云核心闪避
            if (!event.isCanceled()
                    && hasBauble(player, ModItems.NEBULOUS_CORE.get())
                    && Math.random() < 0.4F
                    && !isDamageTypeAbsolute(event.getSource())) {

                for (int counter = 0; counter <= 32; ++counter) {
                    if (validTeleportRandomly(event.getEntity(), event.getEntity().level, 16)) {
                        event.getEntity().invulnerableTime = 20;
                        event.setCanceled(true);
                        break;
                    }
                }
            }

            // 暗日之戒特定伤害免疫 - 添加装备检查
            if (!event.isCanceled()
                    && hasBauble(player, ModItems.DARK_SUN_RING.get())
                    && darkRingDamageNegations.contains(event.getSource().getMsgId())) {

                if (true) {
                    if (event.getEntity().invulnerableTime == 0) {
                        player.heal(event.getAmount());
                        event.getEntity().invulnerableTime = 20;
                    }
                } else {
                    player.heal(event.getAmount());
                }
                event.setCanceled(true);
            }
            // 暗日之戒伤害反弹
            else if (!event.isCanceled()
                    && hasBauble(player, ModItems.DARK_SUN_RING.get())
                    && event.getSource().getEntity() != null
                    && Math.random() <= 0.2F
                    && player.invulnerableTime == 0) {

                player.invulnerableTime = 20;
                event.getSource().getEntity().hurt(event.getSource(), event.getAmount());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !event.isCanceled()) {

            // 混沌核心伤害随机化
            if (player.getInventory().contains(new ItemStack(ModItems.CHAOS_CORE.get()))) {
                event.setAmount(event.getAmount() * (float)(Math.random() * 2.0F));
            }

            // 古老圣盾伤害减免
            if (hasBauble(player, ModItems.ANCIENT_AEGIS.get())
                    && !event.isCanceled()
                    && !isDamageTypeAbsolute(event.getSource())) {
                event.setAmount(event.getAmount() * (1.0F - 0.25F));
            }

            // 古老圣盾伤害分担逻辑
            if (!event.getEntity().level.isClientSide
                    && !hasBauble(player, ModItems.ANCIENT_AEGIS.get())
                    && !event.isCanceled()) {

                Player aegisOwner = findPlayerWithBauble(event.getEntity().level, 32, ModItems.ANCIENT_AEGIS.get(), player);
                if (aegisOwner != null) {
                    aegisOwner.hurt(event.getSource(), event.getAmount() * 0.4F);
                    event.setAmount(event.getAmount() * 0.6F);
                }
            }

            // 暗日之戒超高伤害保护 - 添加装备检查
            if (event.getAmount() > 100.0F
                    && !isDamageTypeAbsolute(event.getSource())
                    && hasBauble(player, ModItems.DARK_SUN_RING.get())) {
                sendNotification(player, 2);
                event.setCanceled(true);
                return;
            }

            // 暗日之戒随机伤害增幅 - 添加装备检查
            if (Math.random() <= 0.25F
                    && !isDamageTypeAbsolute(event.getSource())
                    && hasBauble(player, ModItems.DARK_SUN_RING.get())) {
                float originalAmount = event.getAmount();
                float amplifiedAmount = originalAmount + originalAmount * (float)Math.random();
                event.setAmount(amplifiedAmount);
            }

            // 叠加态指环伤害分摊
            if (!(event.getSource() instanceof ModDamageSources.DamageSourceSuperposition)
                    && !(event.getSource() instanceof ModDamageSources.DamageSourceSuperpositionDefined)
                    && hasBauble(player, ModItems.SUPERPOSITION_RING.get())) {

                DamageSource altSource;
                if (event.getSource().getEntity() != null) {
                    altSource = new ModDamageSources.DamageSourceSuperpositionDefined(event.getSource().getEntity());
                } else {
                    altSource = new ModDamageSources.DamageSourceSuperposition();
                }

                if (event.getSource().isBypassArmor()) {
                    altSource.bypassArmor();
                }
                if (event.getSource().isBypassInvul()) {
                    altSource.bypassInvul();
                }

                List<Player> superpositioned = getBaubleOwnersList(
                        player.level,
                        ModItems.SUPERPOSITION_RING.get()
                );
                superpositioned.remove(player);

                if (!superpositioned.isEmpty()) {
                    double percent = 0.12 + Math.random() * 0.62;
                    float splitAmount = (float)(event.getAmount() * percent);

                    for (Player cPlayer : superpositioned) {
                        cPlayer.hurt(altSource, splitAmount / superpositioned.size());
                    }
                    event.setAmount(event.getAmount() - splitAmount);
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {

            if (player.getInventory().contains(new ItemStack(ModItems.OMEGA_CORE.get()))) {
                event.setCanceled(true);
                player.setHealth(1.0F);
                return;
            }

            if (!player.level.isClientSide) {
                ItemStack fateTomeStack = findFirst(player, ModItems.FATE_TOME.get());
                if (fateTomeStack != null && fateTomeStack.hasTag()
                        && ItemNBTHelper.verifyExistance(fateTomeStack, "IFateCooldown")
                        && ItemNBTHelper.getInt(fateTomeStack, "IFateCooldown", 0) == 0) {

                    int minCooldown = 30 * 20;
                    int bonusCooldown = 90 * 20 - minCooldown;
                    if (90 != 0) {
                        ItemNBTHelper.setInt(fateTomeStack, "IFateCooldown",
                                (int)(minCooldown + Math.random() * bonusCooldown));
                    }

                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());

                    if (Math.random() <= 0.75F) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0, false, false));
                    } else {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1, false, false));
                    }

                    ClientProxy proxy = new ClientProxy();
                    proxy.spawnSuperParticle(
                            player.level, "explosion",
                            player.getX(), player.getY() + 1.0F, player.getZ(),
                            0, 0, 0, 1.5F, 64.0F
                    );

                    player.level.playSound(null,
                            player.getX() + 0.5F, player.getY() + 1.5F, player.getZ() + 0.5F,
                            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }
}