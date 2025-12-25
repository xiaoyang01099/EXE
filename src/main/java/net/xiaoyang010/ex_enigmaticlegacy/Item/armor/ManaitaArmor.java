package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import java.util.UUID;

public class ManaitaArmor extends ArmorItem {

    public static final Properties MANAITA_ARMOR = new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE);
    private static final UUID ARMOR_UUID = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");

    public ManaitaArmor(EquipmentSlot pSlot) {
        super(new ZMaterial(), pSlot, MANAITA_ARMOR);
        MinecraftForge.EVENT_BUS.register(this);
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

    public static boolean hasFullSet(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        return helmet.getItem() instanceof ManaitaArmor &&
                chestplate.getItem() instanceof ManaitaArmor &&
                leggings.getItem() instanceof ManaitaArmor &&
                boots.getItem() instanceof ManaitaArmor;
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            Player player = (Player) event.getEntityLiving();
            if (hasFullSet(player)) {
                event.setCanceled(true);
                event.setAmount(0);
            }
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (hasFullSet(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;

            if (hasFullSet(player)) {
                player.noPhysics = true;
                player.setInvulnerable(true);

                if (player.isInWater() || player.isInLava()) {
                    player.setAirSupply(player.getMaxAirSupply());
                }

                if (player.getBoundingBox().getSize() > 0.1) {
                    player.refreshDimensions();
                }
            } else {
                if (player.noPhysics && !player.isSpectator()) {
                    player.noPhysics = false;
                }
                if (player.isInvulnerable() && !player.isCreative()) {
                    player.setInvulnerable(false);
                }
            }
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (hasFullSet(player)) {
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