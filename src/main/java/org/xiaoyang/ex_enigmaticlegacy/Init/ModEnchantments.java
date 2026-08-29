package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.AutoShootEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.AutoTrackingEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.HeavenlyThunderEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.StarJudgementEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.LavaWalkerEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Enchantment.WitherEnchantment;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Map;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Exe.MODID);
    public static final RegistryObject<Enchantment> WITHER = REGISTRY.register("wither", WitherEnchantment::new);
    public static final RegistryObject<Enchantment> LAVA_WALKER = REGISTRY.register("lava_walker", LavaWalkerEnchantment::new);
    public static final RegistryObject<Enchantment> STAR_JUDGEMENT = REGISTRY.register("star_judgement", StarJudgementEnchantment::new); // 星辰裁决附魔。
    public static final RegistryObject<Enchantment> AUTO_SHOOT = REGISTRY.register("auto_shoot", AutoShootEnchantment::new); // 自动射击附魔。
    public static final RegistryObject<Enchantment> AUTO_TRACKING = REGISTRY.register("auto_tracking", AutoTrackingEnchantment::new); // 自动追踪附魔。
    public static final RegistryObject<Enchantment> HEAVENLY_THUNDER = REGISTRY.register("heavenly_thunder", HeavenlyThunderEnchantment::new); // 天雷战戟天雷附魔。

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

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