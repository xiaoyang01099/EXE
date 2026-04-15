package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.InfinityPotato;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.FullAltarTile;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating.BelieverTile;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.AntigravityCharm;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.ManaBucket;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.ContainerOverpowered;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.SlimeNecklace;
import org.xiaoyang.ex_enigmaticlegacy.Container.CelestialHTMenu;
import org.xiaoyang.ex_enigmaticlegacy.Effect.Drowning;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.*;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.ManaitaArmor;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.NebulaArmor;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.NebulaArmorHelper;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.WildHuntArmor;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.BedrockBreaker;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.InfinityTotemLevel;
import org.xiaoyang.ex_enigmaticlegacy.Item.weapon.Wastelayer;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage.StepHeightMessage;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.JumpPacket;
import org.xiaoyang.ex_enigmaticlegacy.SpawnControlConfig;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.decor.TinyPotatoBlock;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.patchouli.api.PatchouliAPI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.AdminController.shouldKeepInventory;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public class ModEventHandler {
    private static boolean wasJumpPressed = false;
    private static boolean totemJustTriggered = false;
    private static int invulnerableTimer = 0;
    private static final int INVULNERABLE_DURATION = 30;
    private static final int REPAIR_COST = 1500;

    @SubscribeEvent
    public static void onMobSpawnCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntityType());
        if (entityId != null && SpawnControlConfig.isDisabled(entityId)) {
            if (event.getSpawnType() == MobSpawnType.NATURAL
                    || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION
                    || event.getSpawnType() == MobSpawnType.PATROL) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void SlimeNecklaceHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (SlimeNecklace.tryReflectProjectile(player, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void SlimeNecklaceAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (SlimeNecklace.shouldBlockSlimeDamage(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void SlimeNecklaceDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        SlimeNecklace.tryTimeFreeze(player, event.getSource(), event.getAmount());
        if (SlimeNecklace.trySlimeShield(player, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        CompoundTag persistentData = serverPlayer.getPersistentData();

        CompoundTag forgeData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);

        String key = Exe.MODID + ":bible_book_given";

        if (!forgeData.getBoolean(key)) {
            ItemStack book = PatchouliAPI.get().getBookStack(
                    new ResourceLocation(Exe.MODID, "bible")
            );
            serverPlayer.getInventory().add(book);

            forgeData.putBoolean(key, true);
            persistentData.put(Player.PERSISTED_NBT_TAG, forgeData);
        }
    }

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player killer) {
            ItemStack weapon = killer.getMainHandItem();

            if (weapon.getItem() instanceof Wastelayer wastelayer) {
                wastelayer.onEntityKilled(weapon, event.getEntity(), killer);
            }
        }
    }

    @SubscribeEvent
    public static void ManaOnPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

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

        boolean canMove = player.onGround()
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

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
                Component message = Component.translatable("item.ex_enigmaticlegacy.admin_controller.inventory_restored")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
                serverPlayer.sendSystemMessage(message);

                ServerLevel serverLevel = serverPlayer.serverLevel();
                serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                serverLevel.sendParticles(
                        ParticleTypes.TOTEM_OF_UNDYING,
                        serverPlayer.getX(),
                        serverPlayer.getY() + 1.0,
                        serverPlayer.getZ(),
                        30,
                        1.0, 2.0, 1.0,
                        0.1
                );
            }
        }
    }

    @SubscribeEvent
    public static void onTotemLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        InfinityTotemLevel totemItem = (InfinityTotemLevel) ModItems.INFINITY_TOTEM_LEVEL.get();
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
        if (!(event.getEntity() instanceof Player)) {
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

            if (player != null && !player.onGround() && !player.isInWater() && !player.isInLava()) {
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
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Wolf)) {
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();

        if (wolf.getPersistentData().contains("TemporaryWolf")) {
            int timer = wolf.getPersistentData().getInt("DespawnTimer");

            if (timer <= 0) {
                if (wolf.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            wolf.getX(), wolf.getY() + 1.0, wolf.getZ(),
                            20, 1.0, 1.0, 1.0, 0.0);
                }

                wolf.remove(Wolf.RemovalReason.DISCARDED);
            } else {
                wolf.getPersistentData().putInt("DespawnTimer", timer - 1);

                if (timer <= 200) {
                    if (wolf.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                wolf.getX(), wolf.getY() + 0.5, wolf.getZ(),
                                1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Creeper) {
            LivingEntity target = event.getNewTarget();
            if (target instanceof Player player &&
                    player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity && !(attacker instanceof Creeper)) {
                    List<Creeper> nearbyCreepers = player.level().getEntitiesOfClass(
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
        if (event.getExplosion().getDirectSourceEntity() instanceof Creeper) {
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
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!(itemStack.getItem() instanceof ManaBucket manaBucket)) {
            return;
        }

        if (level.getBlockState(pos).getBlock() != BotaniaBlocks.manaPool) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ManaPool pool)) {
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
        } else if (manaBucket.isFilled() && pool.getCurrentMana() == 0) {
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
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState block = event.getPlacedBlock();
        LevelAccessor world = event.getLevel();
        if (block.getBlock() instanceof FallingBlock && entity instanceof Player player) {
            boolean air = world.getBlockState(pos.below()).isAir();
            boolean b = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() == ModItems.ANTIGRAVITY_CHARM.get()) {
                    boolean active = item.getOrCreateTag().getBoolean(AntigravityCharm.ACTIVE_KEY);
                    if (active) b = true;
                }
            }
            if (air && b) {
                world.setBlock(pos.below(), ModBlocks.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 2);
            }
        }
    }

    @SubscribeEvent
    public static void onCharm(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        LevelAccessor world = event.getLevel();
        BlockState blockState = world.getBlockState(pos.above());
        if (blockState.getBlock() instanceof FallingBlock fallingBlock) {
            boolean b = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() == ModItems.ANTIGRAVITY_CHARM.get()) {
                    boolean active = item.getOrCreateTag().getBoolean(AntigravityCharm.ACTIVE_KEY);
                    if (active) b = true;
                }
            }

            if (b) {
                world.setBlock(pos, ModBlocks.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 2);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBedrock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof BedrockBreaker && state.is(Blocks.BEDROCK)) {
            event.getLevel().destroyBlock(pos, false);
            if (!event.getLevel().isClientSide && event.getLevel() instanceof ServerLevel serverWorld) {
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
        Level world = dragon.level();

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
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = entity.getEffect(ModEffects.DAMAGE_REDUCTION.get());

        if (effect != null) {
            event.setAmount(event.getAmount() * 0.01F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
            event.getToolTip().add(Component.translatable("item.ex_enigmaticlegacy.prismaticradianceingot.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void rightBlock(RightClickBlock event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player player) {
            BlockHitResult hitVec = event.getHitVec();
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) {
                BlockPos pos = hitVec.getBlockPos();
                Level world = player.level();
                BlockState state = world.getBlockState(pos);

                boolean isInfinityPotato = state.getBlock() instanceof InfinityPotato;
                boolean isTinyPotato = state.getBlock() instanceof TinyPotatoBlock;

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
        if (event.getEntity() instanceof Player player) {
            float amount = event.getAmount();
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
            int mana = (int) Math.ceil(amount * 1000.f / 4.f);
            int headMana = NebulaArmor.getManaInternal(head);
            int chestMana = NebulaArmor.getManaInternal(chest);
            int legsMana = NebulaArmor.getManaInternal(legs);
            int feetMana = NebulaArmor.getManaInternal(feet);

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
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() instanceof Drowning) {
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
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        Player player = event.player;
        AbstractContainerMenu container = player.containerMenu;
        if (container == null || container.containerId == 0) return;
        for (Slot slot : container.slots) {
            if (slot.container instanceof Inventory) continue;
            if (isCuriosSlot(slot)) continue;
            ItemStack slotItem = slot.getItem();
            if (!slotItem.is(ModTags.Items.SPECTRITE_ITEMS)) continue;
            boolean isAllowed = isAllowedSpectriteContainer(slot.container, player.level());
            if (container instanceof ContainerOverpowered) isAllowed = true;
            if (!isAllowed) {
                ItemStack itemToReturn = slotItem.copy();
                slot.set(ItemStack.EMPTY);
                returnItemToPlayerInventory(player, itemToReturn);
                player.displayClientMessage(
                        Component.nullToEmpty(
                                ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))
                        ),
                        true
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemPlaceInContainer(RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(ModTags.Items.SPECTRITE_ITEMS)) return;
        BlockState state = world.getBlockState(pos);
        boolean isContainer = world.getBlockEntity(pos) instanceof Container
                || state.getMenuProvider(world, pos) != null;
        if (!isContainer) return;
        if (state.is(ModTags.Blocks.SPECTRITE_CONTAINER)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        if (!world.isClientSide) {
            player.displayClientMessage(
                    Component.nullToEmpty(
                            ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))
                    ),
                    true
            );
        }
    }

    private static boolean isAllowedSpectriteContainer(Container container, Level level) {
        if (container instanceof BlockEntity be) {
            return be.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
        }
        if (container instanceof CompoundContainer cc) {
            if (cc.container1 instanceof BlockEntity be1
                    && be1.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER)) return true;
            if (cc.container2 instanceof BlockEntity be2
                    && be2.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER)) return true;
        }
        if (container instanceof CraftingContainer) return false;
        if (container instanceof ResultContainer) return false;
        return false;
    }

    private static void returnItemToPlayerInventory(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack invStack = player.getInventory().items.get(i);
            if (ItemStack.isSameItemSameTags(invStack, stack) && invStack.getCount() < invStack.getMaxStackSize()) {
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
        Player player = event.getEntity();

        if (player.hasEffect(ModEffects.EMESIS.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
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

            if (!PlayerHelper.hasHeldItem(mc.player, vazkii.botania.common.item.BotaniaItems.lexicon)) {
                if (tile instanceof FullAltarTile altar) {
                    FullAltarTile.Hud.render(altar, event.getGuiGraphics(), mc);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof InfinityTotemLevel) {
            InfinityTotemLevel totem = (InfinityTotemLevel) left.getItem();

            if (totem.isValidRepairItem(left, right)) {
                ItemStack output = left.copy();
                output.setDamageValue(0);

                event.setCost(REPAIR_COST / 50);
                event.setMaterialCost(1);
                event.setOutput(output);
            }
        }
    }
}