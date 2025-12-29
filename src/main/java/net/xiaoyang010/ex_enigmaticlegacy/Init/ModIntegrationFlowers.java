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
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating.EMCFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.AlchemyAzaleaTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.AlchemySunflowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.CelestialBlueHyacinthTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.EMCFlowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.GearFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.GearFlowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.TrinarySynthesisTile;
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
    public static RegistryObject<Block> GEAR_FLOWER;
    public static RegistryObject<Block> TRINARY_SYNTHESIS;
    public static RegistryObject<Block> EMC_FLOWER;

    //物品
    public static RegistryObject<Item> CELESTIAL_BLUE_HYACINTH_ITEM;
    public static RegistryObject<Item> ALCHEMY_AZALEA_ITEM;
    public static RegistryObject<Item> ALCHEMY_SUNFLOWER_ITEM;
    public static RegistryObject<Item> GEAR_FLOWER_ITEM;
    public static RegistryObject<Item> TRINARY_SYNTHESIS_ITEM;
    public static RegistryObject<Item> EMC_FLOWER_ITEM;

    //方块实体
    public static RegistryObject<BlockEntityType<CelestialBlueHyacinthTile>> CELESTIAL_BLUE_HYACINTH_TILE;
    public static RegistryObject<BlockEntityType<AlchemyAzaleaTile>> ALCHEMY_AZALEA_TILE;
    public static RegistryObject<BlockEntityType<AlchemySunflowerTile>> ALCHEMY_SUNFLOWER_TILE;
    public static RegistryObject<BlockEntityType<GearFlowerTile>> GEAR_FLOWER_TILE;
    public static RegistryObject<BlockEntityType<TrinarySynthesisTile>> TRINARY_SYNTHESIS_TILE;
    public static RegistryObject<BlockEntityType<EMCFlowerTile>> EMC_FLOWER_TILE;

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

            EMC_FLOWER = BLOCK_REGISTRY.register("emc_flower",
                    () -> new EMCFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> EMC_FLOWER_TILE.get())
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

            EMC_FLOWER_TILE = BLOCK_ENTITY_REGISTRY.register("emc_flower_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                                    new EMCFlowerTile(ModIntegrationFlowers.EMC_FLOWER_TILE.get(), pos, state),
                            ModIntegrationFlowers.EMC_FLOWER.get()).build(null));

            //联动花朵物品
            EMC_FLOWER_ITEM = blockFlowerLinkage(ModIntegrationFlowers.EMC_FLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            CELESTIAL_BLUE_HYACINTH_ITEM = blockFlowerLinkage(ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            ALCHEMY_AZALEA_ITEM = blockFlowerLinkage(ModIntegrationFlowers.ALCHEMY_AZALEA, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            ALCHEMY_SUNFLOWER_ITEM = blockFlowerLinkage(ModIntegrationFlowers.ALCHEMY_SUNFLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
        }

        if (ModList.get().isLoaded("create")) {

            TRINARY_SYNTHESIS = BLOCK_REGISTRY.register("trinary_synthesis",
                    () -> new GearFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> GEAR_FLOWER_TILE.get())
            );

            GEAR_FLOWER = BLOCK_REGISTRY.register("gear_flower",
                    () -> new GearFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS,
                            () -> GEAR_FLOWER_TILE.get())
            );

            GEAR_FLOWER_TILE = BLOCK_ENTITY_REGISTRY.register("gear_flower_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                            new GearFlowerTile(ModIntegrationFlowers.GEAR_FLOWER_TILE.get(), pos, state),
                            ModIntegrationFlowers.GEAR_FLOWER.get()).build(null)
            );

            TRINARY_SYNTHESIS_TILE = BLOCK_ENTITY_REGISTRY.register("trinary_synthesis_tile",
                    () -> BlockEntityType.Builder.of((pos, state) ->
                            new TrinarySynthesisTile(ModIntegrationFlowers.TRINARY_SYNTHESIS_TILE.get(), pos, state),
                            ModIntegrationFlowers.TRINARY_SYNTHESIS.get()).build(null)
            );

            GEAR_FLOWER_ITEM = blockFlowerLinkage(ModIntegrationFlowers.GEAR_FLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
            TRINARY_SYNTHESIS_ITEM = blockFlowerLinkage(ModIntegrationFlowers.TRINARY_SYNTHESIS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
        }
    }

    public static RegistryObject<Item> blockFlowerLinkage(RegistryObject<Block> block, CreativeModeTab tab) {
            return BLOCK_ITEM_REGISTRY.register(block.getId().getPath(), () -> new ItemBlockFlower(block.get(), new Item.Properties().tab(tab)));
    }
}