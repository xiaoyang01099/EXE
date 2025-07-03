package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.AlchemyAzaleaTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.AlchemySunflowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.CelestialBlueHyacinthTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.ItemBlockFlower;
import vazkii.botania.common.block.BlockSpecialFlower;


public class ModIntegrationFlowers {
    public static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, "ex_enigmaticlegacy");
    public static final DeferredRegister<Item> BLOCK_ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, "ex_enigmaticlegacy");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, "ex_enigmaticlegacy");
    private static final BlockBehaviour.Properties FLOWER_PROPS = BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_CYAN).noCollission().instabreak().sound(SoundType.GRASS);

    //方块
    public static RegistryObject<Block> CELESTIAL_BLUE_HYACINTH;
    public static RegistryObject<Block> ALCHEMY_AZALEA;
    public static RegistryObject<Block> ALCHEMY_SUNFLOWER;

    //物品
    public static RegistryObject<Item> CELESTIAL_BLUE_HYACINTH_ITEM;
    public static RegistryObject<Item> ALCHEMY_AZALEA_ITEM;
    public static RegistryObject<Item> ALCHEMY_SUNFLOWER_ITEM;

    //方块实体
    public static RegistryObject<BlockEntityType<CelestialBlueHyacinthTile>> CELESTIAL_BLUE_HYACINTH_TILE;
    public static RegistryObject<BlockEntityType<AlchemyAzaleaTile>> ALCHEMY_AZALEA_TILE;
    public static RegistryObject<BlockEntityType<AlchemySunflowerTile>> ALCHEMY_SUNFLOWER_TILE;

    static {
        if (ModList.get().isLoaded("projecte")) {
            //联动花朵方块
            CELESTIAL_BLUE_HYACINTH = BLOCK_REGISTRY.register("celestial_blue_hyacinth",
                    () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> CELESTIAL_BLUE_HYACINTH_TILE.get())
            );

            ALCHEMY_AZALEA = BLOCK_REGISTRY.register("alchemy_azalea",
                    () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> ALCHEMY_AZALEA_TILE.get())
            );

            ALCHEMY_SUNFLOWER = BLOCK_REGISTRY.register("alchemy_sunflower",
                    () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> ALCHEMY_SUNFLOWER_TILE.get())
            );

            //联动花朵实体
            CELESTIAL_BLUE_HYACINTH_TILE = BLOCK_ENTITY_REGISTRY.register("celestial_blue_hyacinth_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                                    new CelestialBlueHyacinthTile(ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH_TILE.get(), pos, state),
                            ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH.get()).build(null));

            ALCHEMY_AZALEA_TILE = BLOCK_ENTITY_REGISTRY.register("alchemy_azalea_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                                    new AlchemyAzaleaTile(ModIntegrationFlowers.ALCHEMY_AZALEA_TILE.get(), pos, state),
                            ModIntegrationFlowers.ALCHEMY_AZALEA.get()).build(null));

            ALCHEMY_SUNFLOWER_TILE = BLOCK_ENTITY_REGISTRY.register("alchemy_sunflower_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                                    new AlchemySunflowerTile(ModIntegrationFlowers.ALCHEMY_SUNFLOWER_TILE.get(), pos, state),
                            ModIntegrationFlowers.ALCHEMY_SUNFLOWER.get()).build(null));
        }

            //联动花朵物品
            CELESTIAL_BLUE_HYACINTH_ITEM = blockFlowerLinkage(ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            ALCHEMY_AZALEA_ITEM = blockFlowerLinkage(ModIntegrationFlowers.ALCHEMY_AZALEA, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            ALCHEMY_SUNFLOWER_ITEM = blockFlowerLinkage(ModIntegrationFlowers.ALCHEMY_SUNFLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
    }

    public static RegistryObject<Item> blockFlowerLinkage(RegistryObject<Block> block, CreativeModeTab tab) {
            return BLOCK_ITEM_REGISTRY.register(block.getId().getPath(), () -> new ItemBlockFlower(block.get(), new Item.Properties().tab(tab)));
    }
}