package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.Const;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.GuiButtonOpenInventory;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.DragonCrystalArmor;


@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (!(screen instanceof InventoryScreen)) {
            return;
        }
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        int x = screenWidth / 2 + Const.VWIDTH / 2 - GuiButtonOpenInventory.WIDTH - 1;
        int y = screenHeight / 2 - Const.VHEIGHT / 2 - GuiButtonOpenInventory.HEIGHT + 1;

        boolean hasPotions = Minecraft.getInstance().player != null &&
                !Minecraft.getInstance().player.getActiveEffects().isEmpty();
        if (hasPotions) {
            x += 60;
        }

        event.addListener(new GuiButtonOpenInventory(x, y));

        Exe.LOGGER.debug("Added open inventory button to vanilla inventory GUI");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ConfigHandler.enableDragonArmorOverlayConfig.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ItemStack helmet = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof DragonCrystalArmor armor) {
                armor.renderHelmetOverlay(event.getGuiGraphics(), event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

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