package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.damagesource.DamageSource;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.IPreventBreakInCreative;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.EntityDoppleganger;

import java.lang.reflect.Field;
import java.util.UUID;

public class GaiaSlayer extends Item implements IPreventBreakInCreative {
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final double ATTACK_DAMAGE = 8.0;
    private static Field mobSpawnTicksField;
    private static Field tpDelayField;

    static {
        try {

            mobSpawnTicksField = EntityDoppleganger.class.getDeclaredField("mobSpawnTicks");
            mobSpawnTicksField.setAccessible(true);

            tpDelayField = EntityDoppleganger.class.getDeclaredField("tpDelay");
            tpDelayField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            System.err.println("Failed to initialize reflection fields for EntityDoppleganger");
            e.printStackTrace();
        }
    }

    public GaiaSlayer() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (target instanceof EntityDoppleganger doppleganger) {
            try {
                if (doppleganger.getHealth() >= 0.5f) {
                    doppleganger.setHealth(0.5f);
                }

                if (mobSpawnTicksField != null) {
                    mobSpawnTicksField.setInt(doppleganger, 0);
                }

                if (tpDelayField != null) {
                    tpDelayField.setInt(doppleganger, 10000);
                }

                doppleganger.die(DamageSource.OUT_OF_WORLD);
                doppleganger.remove(RemovalReason.KILLED);

            } catch (IllegalAccessException e) {
                System.err.println("Failed to access EntityDoppleganger fields");
                e.printStackTrace();

                doppleganger.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
                doppleganger.die(DamageSource.OUT_OF_WORLD);
                doppleganger.remove(RemovalReason.KILLED);
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();

        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER,
                            "Weapon modifier",
                            ATTACK_DAMAGE,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        return multimap;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}
