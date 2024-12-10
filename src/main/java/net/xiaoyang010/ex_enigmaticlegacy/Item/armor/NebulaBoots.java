package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.botania.api.BotaniaAPI;

import java.util.ArrayList;
import java.util.List;

public class NebulaBoots extends NebulaArmor {
    private static final float MAX_SPEED = 0.275F;
    public static final List<String> playersWithStepup = new ArrayList<>();

    public NebulaBoots(Properties properties) {
        super(EquipmentSlot.FEET, properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);

        if (level.isClientSide &&
                !player.getItemBySlot(EquipmentSlot.FEET).isEmpty() &&
                player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof NebulaBoots &&
                player.isSprinting()) {

            float w = 0.6F;
            float c = 1.0F - w;
            float r = w + (float)Math.random() * c;
            float g = w + (float)Math.random() * c;
            float b = w + (float)Math.random() * c;

            for(int i = 0; i < 2; ++i) {
                BotaniaAPI.instance().sparkleFX(
                        level,
                        player.getX() + (Math.random() - 0.5F),
                        player.getY() - 1.25F + (Math.random() / 4.0F - 0.125F),
                        player.getZ() + (Math.random() - 0.5F),
                        r, g, b,
                        0.7F + (float)Math.random() / 2.0F,
                        25
                );
            }
        }
    }

    @Override
    public float getEfficiency(ItemStack stack, Player player) {
        return 0;
    }

    @SubscribeEvent
    public void updatePlayerStepStatus(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack armor = player.getItemBySlot(EquipmentSlot.FEET);
            String playerStr = player.getGameProfile().getName();

            if (playersWithStepup.contains(playerStr)) {
                if (shouldPlayerHaveStepup(player)) {
                    if ((player.isOnGround() || player.getAbilities().flying) &&
                            player.zza > 0.0F) {

                        float speed = getSpeed(armor) * (player.isSprinting() ? 1.0F : 0.2F);

                        player.setDeltaMovement(
                                player.getDeltaMovement().add(
                                        0,
                                        0,
                                        player.getAbilities().flying ? speed * 0.6F : speed
                                )
                        );
                    }

                    player.maxUpStep = player.isCrouching() ? 0.50001F : 1.0F;
                } else {
                    player.maxUpStep = 0.5F;
                    playersWithStepup.remove(playerStr);
                }
            } else if (shouldPlayerHaveStepup(player)) {
                playersWithStepup.add(playerStr);
                player.maxUpStep = 1.0F;
            }
        }
    }

    private boolean shouldPlayerHaveStepup(Player player) {
        ItemStack armor = player.getItemBySlot(EquipmentSlot.FEET);
        return !armor.isEmpty() && armor.getItem() instanceof NebulaBoots;
    }

    private float getSpeed(ItemStack stack) {
        return MAX_SPEED * (1.0F - (float)getDamage(stack) / 1000.0F);
    }
}