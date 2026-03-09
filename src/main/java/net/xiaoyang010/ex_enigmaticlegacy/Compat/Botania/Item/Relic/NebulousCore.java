package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.network.chat.TranslatableComponent;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class NebulousCore extends Item implements ICurioItem, INoEMCItem {
    private static final double TELEPORT_CHANCE = 2.08E-4;  // 0.0208%
    private static final double DORMANT_CHANCE = 2.7E-5;    // 0.0027%
    private static final int TELEPORT_RANGE = 32;
    private static final int MIN_DORMANT_TIME = 1200;       // 1分钟
    private static final int MAX_DORMANT_TIME = 7200;       // 6分钟

    public NebulousCore(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(itemStack, tooltip);

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.effect")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(new TranslatableComponent(""));

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore3")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore4")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore6")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore7")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.lore8")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent(""));

            tooltip.add(new TranslatableComponent("Teleport Chance: " + String.format("%.4f%%", TELEPORT_CHANCE * 100))
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("Dormant Chance: " + String.format("%.5f%%", DORMANT_CHANCE * 100))
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent(""));
        }

        tooltip.add(new TranslatableComponent(""));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level.isClientSide) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (Math.random() <= TELEPORT_CHANCE) {
                for (int counter = 0; counter <= 32 && !RelicsEventHandler.validTeleportRandomly(player, player.level, TELEPORT_RANGE); ++counter) {
                }
            }

            else if (Math.random() <= DORMANT_CHANCE) {
                transformToDormantArcanum(player, stack, slotContext);
            }
        }
    }

    private void transformToDormantArcanum(Player player, ItemStack stack, SlotContext slotContext) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return;
            }
        }

        ItemStack dormantStack = new ItemStack(ModItems.DORMANT_ARCANUM.get());

        int randomTime = (int)(Math.random() * (MAX_DORMANT_TIME - MIN_DORMANT_TIME));
        int lifetime = MIN_DORMANT_TIME + randomTime;
        ItemNBTHelper.setInt(dormantStack, "ILifetime", lifetime);

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getStacksHandler(slotContext.identifier()).ifPresent(stacksHandler -> {
                stacksHandler.getStacks().setStackInSlot(slotContext.index(), dormantStack);
            });
        });

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.8F);

        player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.dormant")
                .append(new TranslatableComponent(" (" + (lifetime / 20) + "s)"))
                .withStyle(ChatFormatting.DARK_PURPLE), player.getUUID());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.equipped")
                    .withStyle(ChatFormatting.BLUE), player.getUUID());
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.arcanum.unequipped")
                    .withStyle(ChatFormatting.GRAY), player.getUUID());
        }
    }
}