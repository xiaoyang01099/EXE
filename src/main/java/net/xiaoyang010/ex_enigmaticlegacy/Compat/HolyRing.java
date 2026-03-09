package net.xiaoyang010.ex_enigmaticlegacy.Compat;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HolyRing extends Item implements ICurioItem {

    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("457d0ac3-69e4-482f-b636-22e0802da6bd");
    private static final UUID DAMAGE_REDUCTION_UUID = UUID.fromString("1f9b1c56-67e3-4f2b-9d56-7e1b4b5b5f30");
    private static final UUID DAMAGE_INCREASE_UUID = UUID.fromString("c7f1a9a0-cf14-4720-9a77-431bc89d3ddf");

    public HolyRing() {
        super(new Properties().rarity(ModRarities.MIRACLE).stacksTo(1).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level.isClientSide)
            return;

        LivingEntity entity = (LivingEntity) context.entity();

        // 添加药水效果
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, true));

        // 确保在每次tick时只添加一次增益
        if (Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).getModifier(ARMOR_BOOST_UUID) == null) {
            Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).addPermanentModifier(
                    new AttributeModifier(ARMOR_BOOST_UUID, "holy_ring_armor_boost", 3000.0, AttributeModifier.Operation.ADDITION));
        }

        if (Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).getModifier(DAMAGE_INCREASE_UUID) == null) {
            Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(
                    new AttributeModifier(DAMAGE_INCREASE_UUID, "holy_ring_damage_increase", 3000.0, AttributeModifier.Operation.ADDITION));
        }

        if (Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).getModifier(DAMAGE_REDUCTION_UUID) == null) {
            Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).addPermanentModifier(
                    new AttributeModifier(DAMAGE_REDUCTION_UUID, "holy_ring_damage_reduction", 30000, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        // 添加飞行能力
        if (entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = (LivingEntity) context.entity();

        // 移除属性修饰符
        Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).removeModifier(ARMOR_BOOST_UUID);
        Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).removeModifier(DAMAGE_INCREASE_UUID);
        Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)).removeModifier(DAMAGE_REDUCTION_UUID);

        // 移除夜视效果
        entity.removeEffect(MobEffects.NIGHT_VISION);

        // 移除飞行能力
        if (entity instanceof Player player) {
            // 只有在玩家不是创造模式时才移除飞行能力
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        list.add(new TranslatableComponent("至高无上的圣契戒指").withStyle(ChatFormatting.GOLD));
        list.add(new TranslatableComponent("赐予佩戴者飞行的能力").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return CuriosApi.getCuriosHelper().findEquippedCurio(this, context.entity()).isEmpty()
                && context.identifier().equals("ring");
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "speed_bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "ring", uuid, 6.0, AttributeModifier.Operation.ADDITION);
        return atts;
    }
}