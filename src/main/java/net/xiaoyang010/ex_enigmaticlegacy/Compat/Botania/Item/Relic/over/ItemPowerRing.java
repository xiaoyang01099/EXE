package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemPowerRing extends Item implements ICurioItem {

    public ItemPowerRing(Properties properties) {
        super(properties.stacksTo(1));
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

    private boolean isRightPlayer(ItemStack stack, Player player) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relicInstance = relicCap.orElse(null);
            if (relicInstance != null && !relicInstance.isRightPlayer(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }

        if (player.level.isClientSide) {
            return;
        }

        var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic != null) {
            relic.tickBinding(player);
        }

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relicInstance = relicCap.orElse(null);
            if (relicInstance != null && !relicInstance.isRightPlayer(player)) {
                return;
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            return isRightPlayer(stack, player);
        }
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            return isRightPlayer(stack, player);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();

        if (slotContext.entity() instanceof Player player) {
            if (!isRightPlayer(stack, player)) {
                return atts;
            }
        }

        atts.put(Attributes.JUMP_STRENGTH, new AttributeModifier(uuid, "jump_strength_bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "ring", uuid, 3.0, AttributeModifier.Operation.ADDITION);
        return atts;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(EComponent.translatable("tooltip.powerring.desc1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.translatable("tooltip.powerring.desc2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.literal(""));
            tooltip.add(EComponent.translatable("tooltip.powerring.feature1")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(EComponent.translatable("tooltip.powerring.feature2")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(EComponent.translatable("tooltip.powerring.feature3")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(EComponent.translatable("tooltip.powerring.brief")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.translatable("tooltip.powerring.shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        tooltip.add(new TextComponent(""));
    }

    public static boolean isRingEquipped(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> {
                    if (!(stack.getItem() instanceof ItemPowerRing)) {
                        return false;
                    }
                    var relicCap = stack.getCapability(
                            BotaniaForgeCapabilities.RELIC);
                    if (relicCap.isPresent()) {
                        var relic = relicCap.orElse(null);
                        if (relic != null && !relic.isRightPlayer(player)) {
                            return false;
                        }
                    }
                    return true;
                })
                .isPresent();
    }
}
