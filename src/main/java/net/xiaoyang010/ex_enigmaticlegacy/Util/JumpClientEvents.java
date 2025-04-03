package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.JumpPacket;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT)
public class JumpClientEvents {

    private static boolean wasJumpPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;

            if (player != null && !player.isOnGround() && !player.isInWater() && !player.isInLava()) {
                boolean isJumpPressed = mc.options.keyJump.isDown();

                if (isJumpPressed && !wasJumpPressed) {
                    if (WildHuntArmor.isWearingFullSet(player)) {
                        NetworkHandler.sendToServer(new JumpPacket());
                        player.jumpFromGround();
                    }
                }
                wasJumpPressed = isJumpPressed;
            } else {
                wasJumpPressed = mc.options.keyJump.isDown();
            }
        }
    }
}