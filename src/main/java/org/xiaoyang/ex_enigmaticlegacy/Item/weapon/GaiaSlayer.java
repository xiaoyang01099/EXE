package org.xiaoyang.ex_enigmaticlegacy.Item.weapon;

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
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IPreventBreakInCreative;
import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.lang.reflect.Field;
import java.util.UUID;

public class GaiaSlayer extends Item implements IPreventBreakInCreative {
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final double ATTACK_DAMAGE = 8.0;
    private static Field mobSpawnTicksField;
    private static Field tpDelayField;

    static {
        try {

            mobSpawnTicksField = GaiaGuardianEntity.class.getDeclaredField("mobSpawnTicks");
            mobSpawnTicksField.setAccessible(true);

            tpDelayField = GaiaGuardianEntity.class.getDeclaredField("tpDelay");
            tpDelayField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            System.err.println("Failed to initialize reflection fields for EntityDoppleganger");
            e.printStackTrace();
        }
    }

    public GaiaSlayer() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (target instanceof GaiaGuardianEntity doppleganger) {
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

                doppleganger.die(doppleganger.damageSources().fellOutOfWorld());
                doppleganger.remove(RemovalReason.KILLED);

            } catch (IllegalAccessException e) {
                System.err.println("Failed to access EntityDoppleganger fields");
                e.printStackTrace();

                doppleganger.hurt(doppleganger.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                doppleganger.hurt(doppleganger.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
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
