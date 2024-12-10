package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaItem;

import java.util.UUID;

public class NebulaHelmReveal extends NebulaArmor implements IManaProficiencyArmor {
    private static final String TAG_COSMIC_FACE = "enableCosmicFace";

    public NebulaHelmReveal(Item.Properties props) {
        super(EquipmentSlot.HEAD, props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setCosmicFace(stack, !enableCosmicFace(stack));
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
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
        if (hasArmorSetItem(player, EquipmentSlot.HEAD)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 4, false, false));
            if (player.hasEffect(MobEffects.WITHER)) {
                player.removeEffect(MobEffects.WITHER);
            }
            int food = player.getFoodData().getFoodLevel();
            if (food > 0 && food < 18 && player.canEat(false) && player.tickCount % 80 == 0) {
                player.heal(1.0F);
            }

            dispatchManaExact(stack, player, 2, true);
        }
    }

    public static boolean dispatchManaExact(ItemStack stack, Player player, int manaToSend, boolean add) {
        if (stack.isEmpty()) {
            return false;
        }

        // 检查玩家物品栏
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (checkAndTransferMana(stack, stackInSlot, manaToSend, add)) {
                return true;
            }
        }

        return CuriosApi.getCuriosHelper().findCurios(player, itemStack ->
                        itemStack.getItem() instanceof IManaItem)
                .stream()
                .map(SlotResult::stack)
                .anyMatch(curioStack -> checkAndTransferMana(stack, curioStack, manaToSend, add));
    }

    private static boolean checkAndTransferMana(ItemStack source, ItemStack target, int manaToSend, boolean add) {
        if (!target.isEmpty() && !target.is(source.getItem()) &&
                target.getItem() instanceof IManaItem manaItem) {

            if (manaItem.getMana() + manaToSend <= manaItem.getMaxMana()
                    && manaItem.canReceiveManaFromItem(target)) {

                if (add) {
                    manaItem.addMana(manaToSend);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public float getEfficiency(ItemStack stack, Player player) {
        return hasArmorSetItem(player, EquipmentSlot.HEAD) ? 0.3F : 0.0F;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();

        if (slot == EquipmentSlot.HEAD) {
            attributes.put(
                    Attributes.MAX_HEALTH,
                    new AttributeModifier(UUID.fromString("8-4-4-4-12"),
                            "Nebula Helm modifier",
                            20.0F * (1.0F - (float)getDamage(stack) / 1000.0F),
                            AttributeModifier.Operation.ADDITION)
            );
        }

        return attributes;
    }
}