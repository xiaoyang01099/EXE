package net.xiaoyang010.ex_enigmaticlegacy.Event;

import morph.avaritia.container.MachineMenu;
import morph.avaritia.container.slot.ScrollingFakeSlot;
import morph.avaritia.container.slot.StaticFakeSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityPotato;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.FullAltarTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.BelieverTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.AntigravityCharm;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.IvyRegen;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.ManaBucket;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.Drowning;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.BedrockBreaker;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityTotem;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.ManaitaArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmorHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.StepHeightMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.JumpPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.ContainerOverpowered;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.decor.BlockTinyPotato;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.helper.PlayerHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.xiaoyang010.ex_enigmaticlegacy.Item.AdminController.shouldKeepInventory;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ModEventHandler {
    private static boolean wasJumpPressed = false;
    private static boolean totemJustTriggered = false;
    private static int invulnerableTimer = 0;
    private static final int INVULNERABLE_DURATION = 30;
    private static final int REPAIR_COST = 1500;

    @SubscribeEvent
    public static void ManaOnPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level.isClientSide) return;

        Player player = event.player;

        if (ManaitaArmor.isManaitaArmor(player)) {
            player.setHealth(player.getMaxHealth());
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof ManaitaArmor) {
            handleBootsMovement(player, boots);
        }

        if (ManaitaArmor.isManaitaArmorPart(player)) {
            if (player.maxUpStep < 1.0f) {
                player.maxUpStep = 1.0f;

                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new StepHeightMessage(1.0f)
                );
            }
        } else {
            if (player.maxUpStep > 0.6f) {
                player.maxUpStep = 0.6f;
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new StepHeightMessage(0.6f)
                );
            }
        }
    }

    private static void handleBootsMovement(Player player, ItemStack boots) {
        if (player.zza == 0 && player.xxa == 0) return;

        boolean canMove = player.isOnGround()
                || player.getAbilities().flying
                || player.isInWater()
                || player.isInLava();

        if (!canMove) return;

        float speed = ManaitaArmor.getSpeed(boots) * 0.1f;

        if (player.getAbilities().flying) speed *= 1.1f;
        if (player.isCrouching()) speed *= 0.1f;

        if (player.zza > 0) {
            player.moveRelative(speed, new Vec3(0, 0, 1));
        } else if (player.zza < 0) {
            player.moveRelative(-speed * 0.3f, new Vec3(0, 0, 1));
        }

        if (player.xxa != 0) {
            player.moveRelative(speed * 0.5f * Math.signum(player.xxa), new Vec3(1, 0, 0));
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof ManaitaArmor) {
            float jumpBoost = ManaitaArmor.getSpeed(boots) * 0.1f;
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + jumpBoost, motion.z);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ManaitaArmor.isManaitaArmorPart(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ManaitaArmor.isManaitaArmor(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ManaitaArmor.isManaitaArmor(player)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (ManaitaArmor.isManaitaArmor(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = (Player) event.getEntity();
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!(itemStack.getItem() instanceof ManaBucket manaBucket)) {
            return;
        }

        if (level.getBlockState(pos).getBlock() != ModBlocks.manaPool) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TilePool pool)) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        if (player.isShiftKeyDown()) {
            return;
        }

        boolean changed = false;

        if (!manaBucket.isFilled() && pool.isFull()) {
            pool.receiveMana(-pool.getCurrentMana());

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
                ItemStack filledBucket = new ItemStack(ModItems.FILLED_MANA_BUCKET.get());

                if (itemStack.isEmpty()) {
                    player.setItemInHand(hand, filledBucket);
                } else {
                    if (!player.getInventory().add(filledBucket)) {
                        player.drop(filledBucket, false);
                    }
                }
            }
            changed = true;
        }

        else if (manaBucket.isFilled() && pool.getCurrentMana() == 0) {
            pool.receiveMana(1000000);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
                ItemStack emptyBucket = new ItemStack(ModItems.EMPTY_MANA_BUCKET.get());

                if (itemStack.isEmpty()) {
                    player.setItemInHand(hand, emptyBucket);
                } else {
                    if (!player.getInventory().add(emptyBucket)) {
                        player.drop(emptyBucket, false);
                    }
                }
            }
            changed = true;
        }

        if (changed) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onPlace(EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState block = event.getPlacedBlock();
        LevelAccessor world = event.getWorld();
        if (block.getBlock() instanceof FallingBlock && entity instanceof Player player){
            boolean air = world.getBlockState(pos.below()).isAir();
            boolean b = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() == ModItems.ANTIGRAVITY_CHARM.get()) {
                    boolean active = item.getOrCreateTag().getBoolean(AntigravityCharm.ACTIVE_KEY);
                    if (active) b = true;
                }
            }
            if (air && b){
                world.setBlock(pos.below(), ModBlockss.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 2);  //设置空方块
            }
        }
    }

    @SubscribeEvent
    public static void onCharm(BreakEvent event){
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        LevelAccessor world = event.getWorld();
        BlockState blockState = world.getBlockState(pos.above()); //上方方块
        if (blockState.getBlock() instanceof FallingBlock fallingBlock) {
            boolean b = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() == ModItems.ANTIGRAVITY_CHARM.get()) {
                    boolean active = item.getOrCreateTag().getBoolean(AntigravityCharm.ACTIVE_KEY);
                    if (active) b = true;
                }
            }

            if (b){
                world.setBlock(pos, ModBlockss.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 2);  //设置空方块
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        Player original = event.getOriginal();
        Player newPlayer = event.getPlayer();

        if (shouldKeepInventory(original)) {
            for (int i = 0; i < original.getInventory().getContainerSize(); i++) {
                ItemStack stack = original.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    newPlayer.getInventory().setItem(i, stack.copy());
                }
            }

            newPlayer.experienceLevel = original.experienceLevel;
            newPlayer.experienceProgress = original.experienceProgress;
            newPlayer.totalExperience = original.totalExperience;

            if (newPlayer instanceof ServerPlayer serverPlayer) {
                Component message = new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.inventory_restored")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
                serverPlayer.sendMessage(message, serverPlayer.getUUID());

                serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                for (int i = 0; i < 30; i++) {
                    double offsetX = (serverPlayer.level.random.nextDouble() - 0.5) * 2.0;
                    double offsetY = serverPlayer.level.random.nextDouble() * 2.0;
                    double offsetZ = (serverPlayer.level.random.nextDouble() - 0.5) * 2.0;

                    serverPlayer.level.addParticle(
                            net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                            serverPlayer.getX() + offsetX,
                            serverPlayer.getY() + offsetY + 1.0,
                            serverPlayer.getZ() + offsetZ,
                            0.0, 0.1, 0.0
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTotemLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof Player player)) {
            return;
        }

        if (player.level.isClientSide) {
            return;
        }

        InfinityTotem totemItem = (InfinityTotem) ModItems.INFINITY_TOTEM.get();
        if (totemItem.hasTotemInInventory(player)) {
            ItemStack totem = totemItem.getTotemFromInventory(player);
            if (!totem.isEmpty()) {
                event.setCanceled(true);
                totemJustTriggered = true;
                invulnerableTimer = INVULNERABLE_DURATION;

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING), 1);
                    CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, totem);
                }
                totemItem.triggerTotemEffect(player, totem, event.getSource());
            }
        }
    }

    @SubscribeEvent
    public static void onTotemPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && totemJustTriggered) {
            if (invulnerableTimer > 0) {
                invulnerableTimer--;
            } else {
                totemJustTriggered = false;
            }
        }
    }

    @SubscribeEvent
    public static void onTotemLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof Player)) {
            return;
        }

        if (totemJustTriggered && invulnerableTimer > 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;

            if (player != null && !player.isOnGround() && !player.isInWater() && !player.isInLava()) {
                boolean isJumpPressed = mc.options.keyJump.isDown();

                if (isJumpPressed && !wasJumpPressed) {
                    if (WildHuntArmor.isWearingFullSet(player)) {
                        NetworkHandler.sendToServer(new JumpPacket());
                        player.jumpFromGround();
                    }
                }
                wasJumpPressed = isJumpPressed;
            } else {
                wasJumpPressed = mc.options.keyJump.isDown();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Wolf)) {
            return;
        }

        Wolf wolf = (Wolf) event.getEntityLiving();

        if (wolf.getPersistentData().contains("TemporaryWolf")) {
            int timer = wolf.getPersistentData().getInt("DespawnTimer");

            if (timer <= 0) {
                for (int i = 0; i < 20; i++) {
                    wolf.level.addParticle(ParticleTypes.PORTAL,
                            wolf.getX() + (wolf.getRandom().nextDouble() - 0.5D) * 2.0D,
                            wolf.getY() + wolf.getRandom().nextDouble() * 2.0D,
                            wolf.getZ() + (wolf.getRandom().nextDouble() - 0.5D) * 2.0D,
                            0, 0, 0);
                }

                wolf.remove(Wolf.RemovalReason.DISCARDED);
            } else {
                wolf.getPersistentData().putInt("DespawnTimer", timer - 1);

                if (timer <= 200) {
                    wolf.level.addParticle(ParticleTypes.SMOKE,
                            wolf.getX(),
                            wolf.getY() + 0.5D,
                            wolf.getZ(),
                            0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntityLiving() instanceof Creeper) {
            LivingEntity target = event.getNewTarget();
            if (target instanceof Player player &&
                    player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity && !(attacker instanceof Creeper)) {
                    List<Creeper> nearbyCreepers = player.level.getEntitiesOfClass(
                            Creeper.class,
                            player.getBoundingBox().inflate(20),
                            creeper -> !creeper.isIgnited() &&
                                    creeper.hasLineOfSight(attacker)
                    );

                    for (Creeper creeper : nearbyCreepers) {
                        creeper.setLastHurtByMob((LivingEntity) attacker);
                        creeper.getNavigation().moveTo(attacker.getX(), attacker.getY(), attacker.getZ(), 3D);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCreeperExplosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getSourceMob() instanceof Creeper) {
            List<Entity> affectedEntities = event.getAffectedEntities();
            List<Entity> entitiesToRemove = new ArrayList<>();
            boolean hasFriendlyPlayer = false;

            for (Entity entity : affectedEntities) {
                if (entity instanceof Player player &&
                        player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                    entitiesToRemove.add(player);
                    hasFriendlyPlayer = true;
                }
            }

            if (hasFriendlyPlayer) {
                for (Entity entity : affectedEntities) {
                    if (!(entity instanceof Player player &&
                            player.hasEffect(ModEffects.CREEPER_FRIENDLY.get()))) {
                        double dx = entity.getX() - event.getExplosion().getPosition().x;
                        double dy = entity.getY() - event.getExplosion().getPosition().y;
                        double dz = entity.getZ() - event.getExplosion().getPosition().z;
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (distance != 0) {
                            dx /= distance;
                            dy /= distance;
                            dz /= distance;
                            entity.setDeltaMovement(entity.getDeltaMovement().add(
                                    dx * 50.0,
                                    dy * 50.0,
                                    dz * 50.0
                            ));
                            if (entity instanceof LivingEntity living) {
                                living.hurt(event.getExplosion().getDamageSource(), 50.0F);
                            }
                        }
                    }
                }
                affectedEntities.removeAll(entitiesToRemove);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBedrock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        BlockState state = event.getWorld().getBlockState(pos);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof BedrockBreaker && state.is(Blocks.BEDROCK)) {
            event.getWorld().destroyBlock(pos, false);
            if (!event.getWorld().isClientSide && event.getWorld() instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) event.getWorld();
                ItemStack bedrockStack = new ItemStack(Blocks.BEDROCK);
                ItemEntity bedrockItemEntity = new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bedrockStack);
                serverWorld.addFreshEntity(bedrockItemEntity);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void getDragonWings(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        EnderDragon dragon = (EnderDragon) event.getEntity();
        Level world = dragon.level;

        if (world.isClientSide) {
            return;
        }

        ItemStack dragonWings = new ItemStack(ModArmors.DRAGON_WINGS.get());
        Vec3 dragonPos = dragon.position();
        ItemEntity itemEntity = new ItemEntity(world, dragonPos.x, dragonPos.y + 1.0, dragonPos.z, dragonWings);
        itemEntity.setUnlimitedLifetime();
        world.addFreshEntity(itemEntity);
    }

    @SubscribeEvent
    public void onReduction(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        MobEffectInstance effect = entity.getEffect(ModEffects.DAMAGE_REDUCTION.get());

        if (effect != null) {
            event.setAmount(event.getAmount() * 0.01F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltipEvent(ItemTooltipEvent event) {
        ItemStack item = event.getItemStack();
        if (IvyRegen.hasIvy(item)) {
            event.getToolTip().add(new TranslatableComponent("tooltips.ex_enigmaticlegacy_has_timeless_ivy"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
            event.getToolTip().add(new TranslatableComponent("item.ex_enigmaticlegacy.prismaticradianceingot.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void rightBlock(RightClickBlock event) {
        LivingEntity living = event.getEntityLiving();
        if (living instanceof Player player) {
            BlockHitResult hitVec = event.getHitVec();
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) {
                BlockPos pos = hitVec.getBlockPos();
                Level world = player.getLevel();
                BlockState state = world.getBlockState(pos);

                boolean isInfinityPotato = state.getBlock() instanceof InfinityPotato;
                boolean isTinyPotato = state.getBlock() instanceof BlockTinyPotato;

                if (isInfinityPotato || isTinyPotato) {
                    int range = 4;
                    BlockPos pos1 = pos.offset(-range, 0, -range);
                    BlockPos pos2 = pos.offset(range, 1, range);
                    for (BlockPos blockPos : BlockPos.betweenClosed(pos1, pos2)) {
                        BlockEntity entity = world.getBlockEntity(blockPos);
                        if (entity instanceof BelieverTile believer) {
                            believer.addRightMana(isInfinityPotato);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            float amount = event.getAmount();
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
            int mana = (int) Math.ceil(amount * 1000.f / 4.f); //应消耗的魔力 = 伤害值 * 1000mana
            int headMana = NebulaArmor.getManaInternal(head);
            int chestMana = NebulaArmor.getManaInternal(chest);
            int legsMana = NebulaArmor.getManaInternal(legs);
            int feetMana = NebulaArmor.getManaInternal(feet);
            //第二版 单件各减25%
            if (NebulaArmorHelper.isNebulaArmor(head) && headMana > mana) {
                NebulaArmor.setManaInternal(head, headMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(chest) && chestMana > mana) {
                NebulaArmor.setManaInternal(chest, chestMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(legs) && legsMana > mana) {
                NebulaArmor.setManaInternal(legs, legsMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(feet) && feetMana > mana) {
                NebulaArmor.setManaInternal(feet, feetMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.hasNebulaArmor(player) && headMana > mana && chestMana > mana && legsMana > mana && feetMana > mana) {
                event.setAmount(0);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() instanceof Drowning) {
            event.setCanceled(true);
        }
    }

    private static boolean isCuriosSlot(Slot slot) {
        String slotClassName = slot.getClass().getName().toLowerCase();
        if (slotClassName.contains("curios") || slotClassName.contains("cosmetic")) {
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            Player player = event.player;
            AbstractContainerMenu container = player.containerMenu;

            if (container != null) {
                boolean isAllowedContainer = false;
                BlockPos containerPos = null;
                BlockEntity blockEntity = null;
                boolean isPowerInventory = container instanceof ContainerOverpowered;
                // 1. 特定容器检查
                if (container instanceof MachineMenu<?> machineMenu) {
                    blockEntity = machineMenu.machineTile;
                    if (blockEntity != null) {
                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                    }
                } else if (container instanceof CelestialHTMenu celestialMenu) {
                    containerPos = new BlockPos(celestialMenu.x, celestialMenu.y, celestialMenu.z);
                    blockEntity = player.level.getBlockEntity(containerPos);
                    if (blockEntity != null) {
                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                    }
                }
                // 铁砧检查
                else if (container instanceof ItemCombinerMenu itemCombiner) {
                    try {
                        Field accessField = ItemCombinerMenu.class.getDeclaredField("access");
                        accessField.setAccessible(true);
                        ContainerLevelAccess access = (ContainerLevelAccess) accessField.get(itemCombiner);

                        Optional<Boolean> allowed = access.evaluate((level, pos) -> {
                            BlockState state = level.getBlockState(pos);
                            return state.is(ModTags.Blocks.SPECTRITE_CONTAINER);
                        });
                        isAllowedContainer = allowed.orElse(false);
                    } catch (Exception ignored) {
                    }
                }
                // 其他通用容器检查
                else {
                    try {
                        // 尝试获取 ContainerLevelAccess
                        for (Field field : container.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            Object value = field.get(container);
                            if (value instanceof ContainerLevelAccess) {
                                ContainerLevelAccess access = (ContainerLevelAccess) value;
                                Optional<BlockState> state = access.evaluate((level, pos) -> level.getBlockState(pos));
                                if (state.isPresent()) {
                                    isAllowedContainer = state.get().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                    break;
                                }
                            }
                        }

                        // 如果没找到 ContainerLevelAccess，尝试其他方式
                        if (!isAllowedContainer) {
                            for (Field field : container.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                Object value = field.get(container);
                                if (value instanceof BlockPos) {
                                    containerPos = (BlockPos) value;
                                    break;
                                } else if (value instanceof BlockEntity) {
                                    blockEntity = (BlockEntity) value;
                                    containerPos = blockEntity.getBlockPos();
                                    break;
                                }
                            }

                            // 如果找到了位置，检查方块
                            if (containerPos != null) {
                                if (blockEntity == null) {
                                    blockEntity = player.level.getBlockEntity(containerPos);
                                }
                                if (blockEntity != null) {
                                    isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                } else {
                                    BlockState state = player.level.getBlockState(containerPos);
                                    isAllowedContainer = state.is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 2. 检查槽位
                for (Slot slot : container.slots) {
                    // 跳过玩家物品栏和特殊槽位
                    if (slot.container instanceof Inventory ||
                            slot instanceof StaticFakeSlot ||
                            slot instanceof ScrollingFakeSlot || isCuriosSlot(slot)) {
                        continue;
                    }

                    ItemStack slotItem = slot.getItem();
                    if (slotItem.is(ModTags.Items.SPECTRITE_ITEMS)) {
                        boolean isAllowed = isAllowedContainer || isPowerInventory;

                        // 处理特殊槽位
                        if (container instanceof ItemCombinerMenu && slot.container instanceof ResultContainer) {
                            isAllowed = isAllowedContainer;
                        } else if (slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer) {
                            isAllowed = isAllowedContainer;
                        }
                        // 检查其他容器槽位
                        else if (!isAllowed && slot.container instanceof Container) {
                            BlockEntity slotBlockEntity = null;

                            if (slot.container instanceof BlockEntity) {
                                slotBlockEntity = (BlockEntity) slot.container;
                            } else if (slot.container instanceof CompoundContainer) {
                                CompoundContainer compoundContainer = (CompoundContainer) slot.container;
                                if (compoundContainer.container1 instanceof BlockEntity) {
                                    slotBlockEntity = (BlockEntity) compoundContainer.container1;
                                } else if (compoundContainer.container2 instanceof BlockEntity) {
                                    slotBlockEntity = (BlockEntity) compoundContainer.container2;
                                }
                            }

                            if (slotBlockEntity != null) {
                                isAllowed = slotBlockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                            }
                        }

                        // 如果不允许，返回物品
                        if (!isAllowed) {
                            ItemStack itemToReturn = slotItem.copy();
                            slot.set(ItemStack.EMPTY);
                            returnItemToPlayerInventory(player, itemToReturn);
                            player.displayClientMessage(
                                    Component.nullToEmpty(ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))),
                                    true
                            );
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemPlaceInContainer(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        Level world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.is(ModTags.Items.SPECTRITE_ITEMS)) {
            boolean isContainer = false;
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof Container) {
                isContainer = true;
            }
            else if (state.hasBlockEntity() && blockEntity != null) {
                isContainer = state.getMenuProvider(world, pos) != null;
            }

            if (isContainer) {
                boolean isAllowed = state.is(ModTags.Blocks.SPECTRITE_CONTAINER);

                if (!isAllowed) {
                    if (!world.isClientSide) {
                        player.displayClientMessage(
                                Component.nullToEmpty(ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))),
                                true
                        );
                    }
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    private static void returnItemToPlayerInventory(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack invStack = player.getInventory().items.get(i);
            if (ItemStack.isSame(invStack, stack) && invStack.getCount() < invStack.getMaxStackSize()) {
                int spaceLeft = invStack.getMaxStackSize() - invStack.getCount();
                int amountToAdd = Math.min(spaceLeft, stack.getCount());
                invStack.grow(amountToAdd);
                stack.shrink(amountToAdd);
                if (stack.isEmpty()) return;
            }
        }

        if (!stack.isEmpty()) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                if (player.getInventory().items.get(i).isEmpty()) {
                    player.getInventory().items.set(i, stack.copy());
                    stack.setCount(0);
                    return;
                }
            }
        }

        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ModEffects.EMESIS.get())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.hasEffect(ModEffects.EMESIS.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) {
            return;
        }

        HitResult pos = mc.hitResult;
        if (pos instanceof BlockHitResult result) {
            BlockPos bpos = result.getBlockPos();
            BlockEntity tile = mc.level.getBlockEntity(bpos);

            if (!PlayerHelper.hasHeldItem(mc.player, vazkii.botania.common.item.ModItems.lexicon)) {
                if (tile instanceof FullAltarTile altar) {
                    FullAltarTile.Hud.render(altar, event.getMatrixStack(), mc);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof InfinityTotem) {
            InfinityTotem totem = (InfinityTotem) left.getItem();

            if (totem.isValidRepairItem(left, right)) {
                ItemStack output = left.copy();
                output.setDamageValue(0);

                event.setCost(REPAIR_COST / 50);
                event.setMaterialCost(1);
                event.setOutput(output);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        ItemStack base = event.getLeft();
        ItemStack material = event.getRight();

        if (base.isEmpty())
            return;
        if (!base.isRepairable())
            return;
        if (material.isEmpty())
            return;
        if (!(material.getItem() instanceof IvyRegen))
            return;
        if (IvyRegen.hasIvy(base))
            return;

        ItemStack result = base.copy();
        IvyRegen.setIvy(result, true);

        event.setMaterialCost(1);
        event.setCost(ConfigHandler.TIMELESS_IVY_EXP_COST.get());
        event.setOutput(result);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide
                && player.level.getGameTime() % ConfigHandler.REPAIR_TICK.get() == 0) {
            if (!ConfigHandler.ONLY_REPAIR_EQUIPMENTS.get()) {
                player.getInventory().items.forEach((ItemStack itemstack) -> {
                    if (IvyRegen.hasIvy(itemstack) && itemstack.getDamageValue() > 0)
                        processItemRepair(player, itemstack);
                });
            }
            List<EquipmentSlot> relevantSlots = Arrays.asList(
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

            for (EquipmentSlot slot : relevantSlots) {
                ItemStack itemstack = player.getItemBySlot(slot);
                if (IvyRegen.hasIvy(itemstack))
                    processItemRepair(player, itemstack);
            }
        }
    }

    protected static void processItemRepair(Player player, ItemStack itemstack) {
        if (itemstack.getDamageValue() == 0 || !itemstack.isRepairable()) {
            return;
        }

        int manaCostPerDamage = ConfigHandler.MANA_COST_PER_DAMAGE.get();
        int maxRepair = ConfigHandler.TRY_REPAIR_TO_FULL.get()
                ? calculateMaxRepair(player, itemstack, manaCostPerDamage)
                : 1;

        if (maxRepair > 0) {
            int totalCost = maxRepair * manaCostPerDamage;
            boolean success = ManaItemHandler.instance().requestManaExactForTool(
                    itemstack, player, totalCost, true);

            if (success) {
                int newDamage = Math.max(0, itemstack.getDamageValue() - maxRepair);
                itemstack.setDamageValue(newDamage);
            }
        }
    }

    private static int calculateMaxRepair(Player player, ItemStack tool, int manaPerDamage) {
        int availableMana = ManaItemHandler.instance().requestManaForTool(
                tool, player, Integer.MAX_VALUE, false);
        return Math.min(tool.getDamageValue(), availableMana / manaPerDamage);
    }
}