package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.IManaDiscountArmor;
import vazkii.botania.api.mana.IManaItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemNebulaHelm extends NebulaArmor implements IManaDiscountArmor {
    private static final String TAG_COSMIC_FACE = "enableCosmicFace";
    private static final UUID HELM_UUID = UUID.fromString("cfb111e4-9caa-12bf-6a67-01bccaabe34d");

    public ItemNebulaHelm(EquipmentSlot equipmentSlot) {
        this("nebulaHelm");
    }

    public ItemNebulaHelm(String str) {
        super(EquipmentSlot.HEAD);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setCosmicFace(stack, !enableCosmicFace(stack));
            return InteractionResultHolder.success(stack);
        } else {
            return super.use(level, player, hand);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!enableCosmicFace(stack)) {
            tooltip.add(new TranslatableComponent("").withStyle(ChatFormatting.GREEN));
        }
    }

    public boolean enableCosmicFace(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_COSMIC_FACE);
    }

    public void setCosmicFace(ItemStack stack, boolean enable) {
        stack.getOrCreateTag().putBoolean(TAG_COSMIC_FACE, enable);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);

        if (hasArmorSet(player)) {
            int food = player.getFoodData().getFoodLevel();
            if (food > 0 && food < 18 && player.isHurt() && player.tickCount % 80 == 0) {
                player.heal(1.0F);
            }

            dispatchManaExact(stack, player, 2, true);
        }
    }

    public static boolean dispatchManaExact(ItemStack stack, Player player, int manaToSend, boolean add) {
        if (stack == null) {
            return false;
        }

        Inventory inventoryPlayer = player.getInventory();

        int invSize = inventoryPlayer.getContainerSize();

        for (int i = 0; i < invSize; ++i) {
            ItemStack stackInSlot = inventoryPlayer.getItem(i);
            if (stackInSlot != stack && !stackInSlot.isEmpty() && stackInSlot.getItem() instanceof IManaItem) {
                IManaItem manaItemSlot = (IManaItem) stackInSlot.getItem();
                if (manaItemSlot.getMana() + manaToSend <= manaItemSlot.getMaxMana() &&
                        manaItemSlot.canReceiveManaFromItem(stack)) {
                    if (add) {
                        manaItemSlot.addMana(manaToSend);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
        return hasArmorSet(player) ? 0.3F : 0.0F;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = super.getAttributeModifiers(slot, stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.putAll(attributes);

        if (slot == EquipmentSlot.HEAD) {
            double healthBonus = 20.0D * (1.0D - (double) getDamage(stack) / 1000.0D);
            builder.put(Attributes.MAX_HEALTH,
                    new AttributeModifier(HELM_UUID, "NebulaHelm modifier", healthBonus, AttributeModifier.Operation.ADDITION));
        }

        return builder.build();
    }

    public boolean hasArmorSet(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        return helmet.getItem() instanceof NebulaArmor &&
                chestplate.getItem() instanceof NebulaArmor &&
                leggings.getItem() instanceof NebulaArmor &&
                boots.getItem() instanceof NebulaArmor;
    }

    public int getDamage(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Damage");
    }
}