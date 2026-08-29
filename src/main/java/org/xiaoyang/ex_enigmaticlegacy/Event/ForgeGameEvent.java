package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Item.MagicBowItem;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeGameEvent {
    private static final int MAGIC_BOW_MAX_QUICK_CHARGE_LEVEL = 5;

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (tryApplyMagicBowQuickCharge(event, left, right)) return;

        if (!left.is(ModWeapons.COLORFUL_COIN.get())) return;
        if (!right.is(Items.GOLD_INGOT)) return;
        if (!left.isDamaged()) return;

        ItemStack output = left.copy();
        output.setDamageValue(0);
        event.setOutput(output);
        event.setCost(1);
        event.setMaterialCost(1);
    }

    public static boolean tryApplyMagicBowQuickCharge(AnvilUpdateEvent event, ItemStack left, ItemStack right) {
        if (!left.is(ModWeapons.MAGIC_BOW.get())) return false;

        int incomingLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.QUICK_CHARGE, right);
        if (incomingLevel <= 0) return false;

        int currentLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.QUICK_CHARGE, left);
        int targetLevel = Math.max(currentLevel, incomingLevel);
        if (currentLevel == incomingLevel && currentLevel < MAGIC_BOW_MAX_QUICK_CHARGE_LEVEL) {
            targetLevel = currentLevel + 1;
        }
        if (targetLevel <= currentLevel) return false;

        ItemStack output = left.copy();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(output);
        enchantments.put(Enchantments.QUICK_CHARGE, Math.min(targetLevel, MAGIC_BOW_MAX_QUICK_CHARGE_LEVEL));
        EnchantmentHelper.setEnchantments(enchantments, output);

        event.setOutput(output);
        event.setCost(targetLevel);
        event.setMaterialCost(1);
        return true;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        rebuildEntityDamageWhitelistMap();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MagicBowItem.clearSuppressNextAutoTrackingVanillaShot(event.getEntity());
    }

    public static HashMap<String, Boolean> rebuildEntityDamageWhitelistMap() {
        HashMap<String, Boolean> whitelistMap = new HashMap<>();
        for (String id : ConfigFile.entityDamageWhitelist()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null || !ForgeRegistries.ENTITY_TYPES.containsKey(location)) {
                Exe.LOGGER.warn("Invalid entity damage whitelist entry: {}", id);
                continue;
            }
            whitelistMap.put(location.toString(), true);
        }
        if (!ConfigFile.damagePlayers()) {
            whitelistMap.put("minecraft:player", true);
        }
        ConfigFile.setEntityDamageWhitelistMap(whitelistMap);
        return whitelistMap;
    }
}
