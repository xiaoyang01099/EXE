package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Enchantment.LavaWalkerEnchantment;
import net.xiaoyang010.ex_enigmaticlegacy.Enchantment.WitherEnchantment;
import vazkii.botania.common.lib.LibMisc;

import java.util.Map;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LibMisc.MOD_ID);

    public static final RegistryObject<Enchantment> WITHER = REGISTRY.register("wither", WitherEnchantment::new);
    public static final RegistryObject<Enchantment> LAVA_WALKER = REGISTRY.register("lava_walker", LavaWalkerEnchantment::new);

    public static void applyWitherEnchantment(ItemStack book) {
        EnchantmentHelper.setEnchantments(
                Map.of(WITHER.get(), 2),
                book
        );

        EnchantmentHelper.setEnchantments(
                Map.of(LAVA_WALKER.get(), 2),
                book
        );
    }
}