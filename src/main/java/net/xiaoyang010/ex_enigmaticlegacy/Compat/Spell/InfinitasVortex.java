package net.xiaoyang010.ex_enigmaticlegacy.Compat.Spell;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.curios.SimpleAttributeCurio;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;



public class InfinitasVortex extends SimpleAttributeCurio {

    public static final String TRANSLATION_KEY = "item.ex_enigmaticlegacy.infinitas_vortex";

    public InfinitasVortex() {
        super((new Properties()).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1), AttributeRegistry.MAX_MANA, new AttributeModifier("mana", (double)10000000.0F, AttributeModifier.Operation.ADDITION));
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TranslatableComponent(TRANSLATION_KEY + ".tooltip")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withItalic(true).withObfuscated(true)));

        tooltip.add(new TranslatableComponent(TRANSLATION_KEY + ".tooltip_detailed")
                .withStyle(ChatFormatting.GOLD));
    }


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(uuid, "max_mana", 10000000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(uuid, "cooldown_reduction", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(uuid, "spell_power", 2.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.SPELL_RESIST.get(), new AttributeModifier(uuid, "spell_resist", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.CAST_TIME_REDUCTION.get(), new AttributeModifier(uuid, "cast_time_reduction", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.SUMMON_DAMAGE.get(), new AttributeModifier(uuid, "summon_damage", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier(uuid, "fire", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.ICE_SPELL_POWER.get(), new AttributeModifier(uuid, "ice", 2.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier(uuid, "lightning", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.HOLY_SPELL_POWER.get(), new AttributeModifier(uuid, "holy", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(uuid, "ender", 5.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier(uuid, "blood", 10.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.EVOCATION_SPELL_POWER.get(), new AttributeModifier(uuid, "evocation", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(AttributeRegistry.POISON_SPELL_POWER.get(), new AttributeModifier(uuid, "poison", 10000.0F, AttributeModifier.Operation.ADDITION));
        atts.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "speed_bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "ring", uuid, 8.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "necklace", uuid, 8.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "head", uuid, 5.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "back", uuid, 5.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "body", uuid, 5.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "bracelet", uuid, 6.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "hands", uuid, 6.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "belt", uuid, 4.0, AttributeModifier.Operation.ADDITION);
        CuriosApi.getCuriosHelper().addSlotModifier(atts, "charm", uuid, 4.0, AttributeModifier.Operation.ADDITION);
        return atts;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof InfinitasVortex) {
            List<Component> tooltip = event.getToolTip();
        }
    }
}
