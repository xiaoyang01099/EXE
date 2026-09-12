package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 污染防护护符
 * 佩戴后免疫污染效果
 */
public class ItemCorruptionProtectionAmulet extends Item implements ICurioItem {

    private static final UUID ARMOR_UUID = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");

    public ItemCorruptionProtectionAmulet(Properties properties) {
        super(properties.stacksTo(1).durability(1000));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity.level.isClientSide) return;
        entity.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
        if (entity.level.getGameTime() % 100 == 0) {
            stack.hurtAndBreak(1, entity, e -> {});
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();

        attributes.put(Attributes.ARMOR,
                new AttributeModifier(ARMOR_UUID, "Corruption Protection", 2.0, AttributeModifier.Operation.ADDITION));

        return attributes;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.corruption_protection_amulet.tooltip.1")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.corruption_protection_amulet.tooltip.2")
                .withStyle(ChatFormatting.GRAY));

        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.corruption_protection_amulet.durability", remaining)
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // 青色
    }
}
