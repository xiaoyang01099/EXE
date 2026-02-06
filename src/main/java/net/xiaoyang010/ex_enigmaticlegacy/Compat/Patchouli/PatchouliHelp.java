package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.patchouli.api.PatchouliAPI;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class PatchouliHelp {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getLevel().isClientSide) return;
        var player = event.getPlayer();

        var data = player.getPersistentData();
        var key = "bible_book";

        if (!data.getBoolean(key)) {
            ItemStack book = PatchouliAPI.get().getBookStack(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "bible"));
            player.getInventory().add(book);
            data.putBoolean(key, true);
        }
    }
}