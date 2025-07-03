package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class IvyRegen extends Item {
    public static final String TAG_REGEN = "EXE_Ivyregen";

    public IvyRegen(Properties properties) {
        super(properties);
    }

    public static boolean hasIvy(ItemStack itemStack) {
        return (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getOrCreateTag().contains(TAG_REGEN))
                ? itemStack.getOrCreateTag().getBoolean(TAG_REGEN)
                : false;
    }

    public static ItemStack setIvy(ItemStack itemStack, boolean regen) {
        itemStack.getOrCreateTag().putBoolean(TAG_REGEN, true);
        return itemStack;
    }
}