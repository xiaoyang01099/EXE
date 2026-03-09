package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import java.util.UUID;

public class ManaitaArmor extends ArmorItem {

    public static final Properties MANAITA_ARMOR = new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE);
    private static final UUID ARMOR_UUID = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");

    public ManaitaArmor(EquipmentSlot pSlot) {
        super(new ZMaterial(), pSlot, MANAITA_ARMOR);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) return;
        initNBT(stack);
        if (!isEquipped(player, stack)) return;
        switch (this.slot) {
            case HEAD -> tickHelmet(stack, player, level);
            case CHEST -> tickChestplate(stack, player, level);
            case LEGS -> tickLeggings(stack, player, level);
            case FEET -> tickBoots(stack, player, level);
        }
    }

    private void tickHelmet(ItemStack stack, Player player, Level level) {
        player.setAirSupply(300);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0f);
        if (stack.getOrCreateTag().getBoolean("NightVision")) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }
    }

    private void tickChestplate(ItemStack stack, Player player, Level level) {
        player.getActiveEffects().stream()
                .filter(effect -> !effect.getEffect().isBeneficial())
                .map(MobEffectInstance::getEffect)
                .toList()
                .forEach(player::removeEffect);
    }

    private void tickLeggings(ItemStack stack, Player player, Level level) {
        if (player.isOnFire()) {
            player.clearFire();
        }
        if (stack.getOrCreateTag().getBoolean("Invisibility")) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 11, 0, false, false));
            player.setInvisible(true);
        } else {
            player.setInvisible(false);
        }
    }

    public static boolean isManaitaArmorPart(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof ManaitaArmor) {
                return true;
            }
        }
        return false;
    }

    private void tickBoots(ItemStack stack, Player player, Level level) {
    }

    private void initNBT(ItemStack stack) {
        var tag = stack.getOrCreateTag();

        switch (this.slot) {
            case HEAD -> {
                if (!tag.contains("NightVision")) {
                    tag.putBoolean("NightVision", false);
                }
            }
            case LEGS -> {
                if (!tag.contains("Invisibility")) {
                    tag.putBoolean("Invisibility", false);
                }
            }
            case FEET -> {
                if (!tag.contains("Speed")) {
                    tag.putInt("Speed", 1);
                }
            }
        }
    }

    private boolean isEquipped(Player player, ItemStack stack) {
        return player.getItemBySlot(this.slot) == stack;
    }
    public static int getSpeed(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Speed");
    }
    public static void setSpeed(ItemStack stack, int speed) {
        stack.getOrCreateTag().putInt("Speed", speed);
    }

    public void onArmorKeyPress(ItemStack stack, Player player) {
        var tag = stack.getOrCreateTag();
        switch (this.slot) {
            case HEAD -> {
                boolean newValue = !tag.getBoolean("NightVision");
                tag.putBoolean("NightVision", newValue);
                player.displayClientMessage(
                        Component.literal("[")
                                .append(stack.getHoverName())
                                .append("] NightVision: " + (newValue ? "ON" : "OFF"))
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
            }
            case LEGS -> {
                boolean newValue = !tag.getBoolean("Invisibility");
                tag.putBoolean("Invisibility", newValue);
                player.displayClientMessage(
                        Component.literal("[")
                                .append(stack.getHoverName())
                                .append("] Invisibility: " + (newValue ? "ON" : "OFF"))
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
            }
            case FEET -> {
                int currentSpeed = getSpeed(stack);
                int newSpeed = currentSpeed >= 8 ? 1 : currentSpeed + 1;
                setSpeed(stack, newSpeed);
                player.displayClientMessage(
                        Component.literal("[")
                                .append(stack.getHoverName())
                                .append("] Speed Level: " + newSpeed)
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == this.slot) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

            builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_UUID,
                    "Armor modifier", 1000.0D, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(TOUGHNESS_UUID,
                    "Armor toughness", 1000.0D, AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    public static boolean isManaitaArmor(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        return helmet.getItem() instanceof ManaitaArmor &&
                chestplate.getItem() instanceof ManaitaArmor &&
                leggings.getItem() instanceof ManaitaArmor &&
                boots.getItem() instanceof ManaitaArmor;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (isManaitaArmor(player)) {
            player.clearFire();

            player.getActiveEffects().stream()
                    .filter(effect -> !effect.getEffect().isBeneficial())
                    .map(effect -> effect.getEffect())
                    .forEach(player::removeEffect);

            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            if (player.getFoodData().getFoodLevel() < 20) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
            }
        }
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return false;
    }
}