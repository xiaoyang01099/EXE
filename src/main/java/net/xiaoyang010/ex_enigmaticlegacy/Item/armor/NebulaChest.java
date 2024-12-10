package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NebulaChest extends NebulaArmor {
    public static final List<String> playersWithFlight = new ArrayList<>();

    public NebulaChest(Properties properties) {
        super(EquipmentSlot.CHEST, properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();

        if (slot == EquipmentSlot.CHEST) {
            attributes.put(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            UUID.fromString("8-4-4-4-12"),
                            "Nebula Chest modifier",
                            1.0F * (1.0F - (float)getDamage(stack) / 1000.0F),
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        return attributes;
    }

    @SubscribeEvent
    public void updatePlayerFlyStatus(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof Player player) {
            String playerStr = player.getGameProfile().getName();

            if (playersWithFlight.contains(playerStr)) {
                if (shouldPlayerHaveFlight(player)) {
                    player.getAbilities().mayfly = true;
                } else {
                    if (!player.getAbilities().instabuild) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.getAbilities().invulnerable = false;
                    }
                    playersWithFlight.remove(playerStr);
                }
            } else if (shouldPlayerHaveFlight(player)) {
                playersWithFlight.add(playerStr);
                player.getAbilities().mayfly = true;
            }
            player.onUpdateAbilities();
        }
    }

    private static boolean shouldPlayerHaveFlight(Player player) {
        ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
        return !armor.isEmpty() && armor.getItem() instanceof NebulaChest;
    }

    private static String playerStr(Player player) {
        return player.getGameProfile().getName();
    }

    @Override
    public float getEfficiency(ItemStack stack, Player player) {
        return 0;
    }
}