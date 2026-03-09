package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
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

public class AncientAegis extends Item implements ICurioItem, INoEMCItem {
    private static final float ANCIENT_AEGIS_DAMAGE_REDUCTION = 0.25F;
    private static final float HEALTH_MODIFIER = 10.0F;

    public AncientAegis(Properties properties) {
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        RelicImpl.addDefaultTooltip(stack, tooltipComponents);

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltipComponents.add(new TranslatableComponent("item.ItemAncientAegis1_1.lore")
                    .append(" " + (int)(ANCIENT_AEGIS_DAMAGE_REDUCTION * 100.0F))
                    .append(new TranslatableComponent("item.ItemAncientAegis1_2.lore"))
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemAncientAegis2.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemAncientAegis3.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.nullToEmpty(""));
            tooltipComponents.add(new TranslatableComponent("item.ItemAncientAegis4.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemAncientAegis5.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.nullToEmpty(""));
        } else {
            tooltipComponents.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (!entity.level.isClientSide && entity.tickCount % 20 == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return HashMultimap.create();
                }
            }
        }

        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.MAX_HEALTH,
                new AttributeModifier(uuid, "Ancient Aegis Health Modifier", HEALTH_MODIFIER, AttributeModifier.Operation.ADDITION));
        return attributes;
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

        return CuriosApi.getCuriosHelper().findEquippedCurio(this, slotContext.entity()).isEmpty()
                && slotContext.identifier().equals("belt");
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
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
    }

    public static float getDamageReduction() {
        return ANCIENT_AEGIS_DAMAGE_REDUCTION;
    }

    public static boolean isWearing(Player player) {
        var curioResult = CuriosApi.getCuriosHelper().findEquippedCurio(ModItems.ANCIENT_AEGIS.get(), player);
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
}