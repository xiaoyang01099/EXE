package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.jetbrains.annotations.NotNull;

// Curios API imports
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class AdminController extends Item {
    private static final GameModeWeight[] GAME_MODE_WEIGHTS = {
            new GameModeWeight(GameType.SURVIVAL, 8),
            new GameModeWeight(GameType.CREATIVE, 1),
            new GameModeWeight(GameType.ADVENTURE, 90),
            new GameModeWeight(GameType.SPECTATOR, 1)
    };

    public static final String CHARM_INV_TAG = "CharmInventory";
    public static final String CURIOS_INV_TAG = "CuriosInventory";
    private static final int TOTAL_WEIGHT = 100;
    private static final String NBT_KEEP_INVENTORY = "KeepInventoryEnabled";
    private static final String NBT_OWNER_UUID = "OwnerUUID";
    private static final double RANGE = 10.0;

    public AdminController(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static CompoundTag getPlayerData(Player player) {
        if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void keepWholeList(NonNullList<ItemStack> transferTo, NonNullList<ItemStack> transferFrom) {
        for (int i = 0; i < transferFrom.size(); i++) {
            transferTo.set(i, transferFrom.get(i).copy());
        }
        transferFrom.clear();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player var2 = event.getPlayer();
        if (var2 instanceof ServerPlayer serverPlayer) {
            returnStoredItems(serverPlayer);
            returnCuriosItems(serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void applyDeathItems(LivingDeathEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof Player player && !player.isSpectator()) {
            if (!living.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                charmOfKeeping(player);
            }
        }
    }

    private static ItemStack charmUsed;
    public static int damage = 0;

    public static boolean consumeInventoryItem(Player player, Item item) {
        return consumeInventoryItem(player.getInventory().armor, item) ||
                consumeInventoryItem(player.getInventory().items, item) ||
                consumeInventoryItem(player.getInventory().offhand, item);
    }

    public static boolean consumeInventoryItem(NonNullList<ItemStack> stacks, Item item) {
        Iterator<ItemStack> var2 = stacks.iterator();

        ItemStack stack;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            stack = (ItemStack)var2.next();
        } while(stack.getItem() != item);

        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains("BlockStateTag")) {
            CompoundTag damageNbt = nbt.getCompound("BlockStateTag");
            if (damageNbt.contains("damage")) {
                damage = damageNbt.getInt("damage");
            }
        }

        return true;
    }

    private static void charmOfKeeping(Player player) {
        boolean tier3 = consumeInventoryItem(player, ModItems.ADMIN_CONTROLLER.get());

        if (!tier3) return;

        Inventory keepInventory = new Inventory(null);
        ListTag tagList = new ListTag();

        keepWholeList(keepInventory.items, player.getInventory().items);

        keepWholeList(keepInventory.armor, player.getInventory().armor);

        keepWholeList(keepInventory.offhand, player.getInventory().offhand);

        charmUsed = new ItemStack(ModItems.ADMIN_CONTROLLER.get());

        if (!keepInventory.isEmpty()) {
            keepInventory.save(tagList);
            getPlayerData(player).put(CHARM_INV_TAG, tagList);
        }

        if (ModList.get().isLoaded("curios")) {
            saveCuriosItems(player);
        }
    }

    private static void saveCuriosItems(Player player) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            CompoundTag curiosData = new CompoundTag();

            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                String identifier = entry.getKey();
                ICurioStacksHandler stacksHandler = entry.getValue();

                ListTag slotData = new ListTag();

                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        CompoundTag itemData = new CompoundTag();
                        itemData.putInt("Slot", i);
                        stack.save(itemData);
                        slotData.add(itemData);

                        stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                if (!slotData.isEmpty()) {
                    curiosData.put(identifier, slotData);
                }
            }

            if (!curiosData.isEmpty()) {
                getPlayerData(player).put(CURIOS_INV_TAG, curiosData);
            }
        });
    }

    private static void returnCuriosItems(Player player) {
        if (!ModList.get().isLoaded("curios")) return;

        CompoundTag playerData = getPlayerData(player);
        if (!player.level.isClientSide && playerData.contains(CURIOS_INV_TAG)) {
            CompoundTag curiosData = playerData.getCompound(CURIOS_INV_TAG);

            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                for (String identifier : curiosData.getAllKeys()) {
                    if (curios.containsKey(identifier)) {
                        ICurioStacksHandler stacksHandler = curios.get(identifier);
                        ListTag slotData = curiosData.getList(identifier, 10);

                        for (int i = 0; i < slotData.size(); i++) {
                            CompoundTag itemData = slotData.getCompound(i);
                            int slot = itemData.getInt("Slot");
                            ItemStack stack = ItemStack.of(itemData);

                            if (slot < stacksHandler.getSlots() && !stack.isEmpty()) {
                                if (stacksHandler.getStacks().getStackInSlot(slot).isEmpty()) {
                                    stacksHandler.getStacks().setStackInSlot(slot, stack);
                                } else {
                                    boolean placed = false;
                                    for (int j = 0; j < stacksHandler.getSlots(); j++) {
                                        if (stacksHandler.getStacks().getStackInSlot(j).isEmpty()) {
                                            stacksHandler.getStacks().setStackInSlot(j, stack);
                                            placed = true;
                                            break;
                                        }
                                    }
                                    if (!placed) {
                                        player.getInventory().add(stack);
                                    }
                                }
                            }
                        }
                    }
                }
            });

            playerData.remove(CURIOS_INV_TAG);
        }
    }

    private static void returnStoredItems(Player player) {
        CompoundTag playerData = getPlayerData(player);
        if (!player.level.isClientSide && playerData.contains(CHARM_INV_TAG)) {
            ListTag tagList = playerData.getList(CHARM_INV_TAG, 10);
            loadNoClear(tagList, player.getInventory());
            getPlayerData(player).getList(CHARM_INV_TAG, 10).clear();
            getPlayerData(player).remove(CHARM_INV_TAG);
        }
    }

    public static void loadNoClear(ListTag tag, Inventory inventory) {
        List<ItemStack> blockedItems = new ArrayList<>();

        for (int i = 0; i < tag.size(); ++i) {
            CompoundTag compoundtag = tag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.of(compoundtag);
            if (!itemstack.isEmpty()) {
                if (j < inventory.items.size()) {
                    if (inventory.items.get(j).isEmpty()) {
                        inventory.items.set(j, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                } else if (j >= 100 && j < inventory.armor.size() + 100) {
                    if (inventory.armor.get(j - 100).isEmpty()) {
                        inventory.armor.set(j - 100, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                } else if (j >= 150 && j < inventory.offhand.size() + 150) {
                    if (inventory.offhand.get(j - 150).isEmpty()) {
                        inventory.offhand.set(j - 150, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                }
            }
        }

        if(!blockedItems.isEmpty()) blockedItems.forEach(inventory::add);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = itemStack.getOrCreateTag();
            if (!tag.contains(NBT_OWNER_UUID)) {
                tag.putString(NBT_OWNER_UUID, player.getUUID().toString());
            }

            if (player.isShiftKeyDown()) {
                enableKeepInventoryForSelf(itemStack, serverPlayer);
            } else {
                changeTargetGameMode(level, serverPlayer, itemStack);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    private void enableKeepInventoryForSelf(ItemStack itemStack, ServerPlayer player) {
        CompoundTag tag = itemStack.getOrCreateTag();

        tag.putBoolean(NBT_KEEP_INVENTORY, true);
        tag.putString(NBT_OWNER_UUID, player.getUUID().toString());

        Component message = new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.keep_inventory.enabled_self")
                .withStyle(ChatFormatting.GREEN);

        player.sendMessage(message, player.getUUID());

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);

        for (int i = 0; i < 15; i++) {
            double offsetX = (player.level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = player.level.random.nextDouble() * 1.5;
            double offsetZ = (player.level.random.nextDouble() - 0.5) * 1.5;

            player.level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    player.getX() + offsetX,
                    player.getY() + offsetY + 1.0,
                    player.getZ() + offsetZ,
                    0.0, 0.1, 0.0
            );
        }
    }

    private void changeTargetGameMode(Level level, ServerPlayer player, ItemStack itemStack) {
        Player targetPlayer = getTargetPlayer(level, player);

        if (targetPlayer == null) {
            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.no_target")
                    .withStyle(ChatFormatting.RED), player.getUUID());
            return;
        }

        if (!(targetPlayer instanceof ServerPlayer serverTarget)) {
            return;
        }

        GameType newGameMode = getRandomGameMode();
        GameType oldGameMode = serverTarget.gameMode.getGameModeForPlayer();

        if (newGameMode == oldGameMode) {
            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.same_mode")
                    .withStyle(ChatFormatting.YELLOW), player.getUUID());
            return;
        }

        serverTarget.gameMode.changeGameModeForPlayer(newGameMode);

        Component executorMessage = new TranslatableComponent(
                "item.ex_enigmaticlegacy.admin_controller.mode_changed",
                targetPlayer.getDisplayName(),
                getGameModeDisplayName(newGameMode)
        ).withStyle(ChatFormatting.GREEN);
        player.sendMessage(executorMessage, player.getUUID());

        Component targetMessage = new TranslatableComponent(
                "item.ex_enigmaticlegacy.admin_controller.mode_changed_target",
                getGameModeDisplayName(newGameMode),
                player.getDisplayName()
        ).withStyle(ChatFormatting.GOLD);
        serverTarget.sendMessage(targetMessage, serverTarget.getUUID());

        level.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        addParticleEffect(level, targetPlayer);
    }

    private void addParticleEffect(Level level, Player targetPlayer) {
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    targetPlayer.getX() + offsetX,
                    targetPlayer.getY() + offsetY,
                    targetPlayer.getZ() + offsetZ,
                    0.0, 0.1, 0.0
            );
        }
    }

    private Player getTargetPlayer(Level level, Player player) {
        HitResult hitResult = player.pick(RANGE, 0.0F, false);
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer;
        }

        AABB searchBox = player.getBoundingBox().inflate(RANGE);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox,
                p -> p != player && p.distanceTo(player) <= RANGE);

        if (!nearbyPlayers.isEmpty()) {
            return nearbyPlayers.stream()
                    .min((p1, p2) -> Double.compare(p1.distanceTo(player), p2.distanceTo(player)))
                    .orElse(null);
        }

        return null;
    }

    private GameType getRandomGameMode() {
        Random random = new Random();
        int randomValue = random.nextInt(TOTAL_WEIGHT);
        int currentWeight = 0;

        for (GameModeWeight weight : GAME_MODE_WEIGHTS) {
            currentWeight += weight.weight;
            if (randomValue < currentWeight) {
                return weight.gameType;
            }
        }

        return GameType.SURVIVAL;
    }

    private Component getGameModeDisplayName(GameType gameType) {
        switch (gameType) {
            case SURVIVAL:
                return new TranslatableComponent("gameMode.survival").withStyle(ChatFormatting.GREEN);
            case CREATIVE:
                return new TranslatableComponent("gameMode.creative").withStyle(ChatFormatting.BLUE);
            case ADVENTURE:
                return new TranslatableComponent("gameMode.adventure").withStyle(ChatFormatting.RED);
            case SPECTATOR:
                return new TranslatableComponent("gameMode.spectator").withStyle(ChatFormatting.GRAY);
            default:
                return new TextComponent("Unknown");
        }
    }

    public static boolean shouldKeepInventory(Player player) {
        UUID playerUUID = player.getUUID();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof AdminController) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.getBoolean(NBT_KEEP_INVENTORY)) {
                    String ownerUUIDString = tag.getString(NBT_OWNER_UUID);
                    if (ownerUUIDString.isEmpty() || ownerUUIDString.equals(playerUUID.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(itemStack, level, tooltip, flag);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.tooltip.1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.tooltip.2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.nullToEmpty(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.probabilities")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TextComponent("• ").append(new TranslatableComponent("gameMode.survival"))
                    .append(": 8%").withStyle(ChatFormatting.GREEN));
            tooltip.add(new TextComponent("• ").append(new TranslatableComponent("gameMode.creative"))
                    .append(": 1%").withStyle(ChatFormatting.BLUE));
            tooltip.add(new TextComponent("• ").append(new TranslatableComponent("gameMode.adventure"))
                    .append(": 90%").withStyle(ChatFormatting.RED));
            tooltip.add(new TextComponent("• ").append(new TranslatableComponent("gameMode.spectator"))
                    .append(": 1%").withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.nullToEmpty(""));

        CompoundTag tag = itemStack.getTag();
        boolean keepInventoryEnabled = tag != null && tag.getBoolean(NBT_KEEP_INVENTORY);

        if (keepInventoryEnabled) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.keep_inventory.status_enabled")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

            String ownerUUID = tag.getString(NBT_OWNER_UUID);
            if (!ownerUUID.isEmpty()) {
                tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.owner_only")
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.keep_inventory.status_disabled")
                    .withStyle(ChatFormatting.RED));
        }

        if (Screen.hasControlDown()) {
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.usage.1")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.usage.3")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.admin_controller.no_permission_needed")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static class GameModeWeight {
        final GameType gameType;
        final int weight;

        GameModeWeight(GameType gameType, int weight) {
            this.gameType = gameType;
            this.weight = weight;
        }
    }
}