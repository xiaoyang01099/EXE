package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;
import java.util.UUID;

public class Manaita extends Item implements INoEMCItem {

    private static final String LAST_POSITION_KEY = "lastPosition";
    private static final String LAST_POS_X = "lastPosX";
    private static final String LAST_POS_Y = "lastPosY";
    private static final String LAST_POS_Z = "lastPosZ";

    public Manaita(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);

                if (!isAuthorizedPlayer(stack, player)) {
                    punishUnauthorizedPlayer(stack, level, player, slot);
                }
            }
        }
    }


    private boolean isAuthorizedPlayer(ItemStack stack, Player player) {
        var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic == null) return true;


        UUID boundPlayer = relic.getSoulbindUUID();
        if (boundPlayer == null) {
            return true;
        }

        return boundPlayer.equals(player.getUUID());
    }


    private void punishUnauthorizedPlayer(ItemStack stack, Level level, Player player, int slot) {
        Vec3 currentPos = player.position();
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putDouble(LAST_POS_X, currentPos.x);
        nbt.putDouble(LAST_POS_Y, currentPos.y);
        nbt.putDouble(LAST_POS_Z, currentPos.z);

        player.getInventory().setItem(slot, ItemStack.EMPTY);

        ItemEntity itemEntity = new ItemEntity(level, currentPos.x, currentPos.y, currentPos.z, stack.copy());
        itemEntity.setNoPickUpDelay();
        itemEntity.setThrower(player.getUUID());

        CompoundTag entityNbt = itemEntity.getPersistentData();
        entityNbt.putString("originalOwner", getRelic(stack) != null && getRelic(stack).getSoulbindUUID() != null ?
                getRelic(stack).getSoulbindUUID().toString() : "none");

        level.addFreshEntity(itemEntity);

        player.teleportTo(player.getX(), -100, player.getZ());

        player.displayClientMessage(EComponent.translatable("message.manaita.unauthorized")
                .withStyle(ChatFormatting.DARK_RED),false);
    }

    private IRelic getRelic(ItemStack stack) {
        return IXplatAbstractions.INSTANCE.findRelic(stack);
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        if (!(stack.getItem() instanceof Manaita)) return;

        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getItem();

        CompoundTag entityNbt = itemEntity.getPersistentData();
        if (entityNbt.contains("originalOwner")) {
            String originalOwnerUuid = entityNbt.getString("originalOwner");

            if (!"none".equals(originalOwnerUuid) && !originalOwnerUuid.equals(player.getUUID().toString())) {
                event.setResult(Event.Result.DENY);

                player.displayClientMessage(EComponent.translatable("message.manaita.cannot_pickup")
                        .withStyle(ChatFormatting.RED),false);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemstack) {
        return itemstack.copy();
    }
}