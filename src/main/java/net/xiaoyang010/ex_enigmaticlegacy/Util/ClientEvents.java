package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.DragonCrystalArmor;


@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (!ConfigHandler.enableDragonArmorOverlayConfig.get()) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                ItemStack helmet = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof DragonCrystalArmor) {
                    ((DragonCrystalArmor) helmet.getItem()).renderHelmetOverlay(event.getMatrixStack(), event.getPartialTicks());
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.hasEffect(ModEffects.EMESIS.get())) {
            float originalForward = event.getInput().forwardImpulse;
            float originalStrafe = event.getInput().leftImpulse;

            event.getInput().forwardImpulse = -originalForward;
            event.getInput().leftImpulse = -originalStrafe;

            if (event.getInput().jumping) {
                event.getInput().jumping = false;
            }
        }
    }
}