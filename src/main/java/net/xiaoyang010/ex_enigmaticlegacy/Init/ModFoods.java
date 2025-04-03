package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Item.food.EndlessCake;
import net.xiaoyang010.ex_enigmaticlegacy.Item.food.EnigmaticViscousSubstance;
import net.xiaoyang010.ex_enigmaticlegacy.Item.food.TabooApex;


public class ModFoods {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "ex_enigmaticlegacy");

    public static final RegistryObject<Item> ENIGMATICVISCOUSSUBSTANCE = ITEMS.register("enigmatic_viscous_substance", () -> EnigmaticViscousSubstance.ENIGMATICVISCOUSSUBSTANCE);
    public static final RegistryObject<Item> TABOOAPEX = ITEMS.register("tabooapex", () -> TabooApex.TabooApex);



    public static final RegistryObject<Item> ENDLESS_CAKE = ITEMS.register("endless_cake", () ->
            new EndlessCake(
                    ModBlockss.ENDLESS_CAKE.get(),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(ModRarities.MIRACLE)
                    .tab(ModTabs.TAB_EXENIGMATICLEGACY_FOOD)));


    public static void registerItems() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

