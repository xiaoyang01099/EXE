package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import net.xiaoyang010.ex_enigmaticlegacy.Tile.CelestialHolinessTransmuterBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumOfMystiqueBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.InfinityPotatoEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<BlockEntityType<InfinityChestEntity>> INFINITY_CHEST = register("infinity_chest_entity", ModBlockss.INFINITYCHEST, InfinityChestEntity::new);
    public static final RegistryObject<BlockEntityType<StarlitSanctumOfMystiqueBlockEntity>> STARLIT_SANCTUM_OF_MYSTIQUE = register("starlit_sanctum_of_mystique", ModBlockss.STARLITSANCTUM, StarlitSanctumOfMystiqueBlockEntity::new);
    public static final RegistryObject<BlockEntityType<CelestialHolinessTransmuterBlockEntity>> CELESTIAL_HOLINESS_TRANSMUTER = register("celestial_holiness_transmuter", ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER, CelestialHolinessTransmuterBlockEntity::new);




    //botania
    public static final RegistryObject<BlockEntityType<InfinityPotatoEntity>> INFINITY_POTATO = register("infinity_potato", ModBlockss.INFINITY_POTATO, InfinityPotatoEntity::new);
    public static final RegistryObject<BlockEntityType<NightshadeTile>> NIGHTSHADE_TILE = register("nightshade_tile", ModBlockss.NIGHTSHADE, NightshadeTile::new);


    public static final RegistryObject<BlockEntityType<AstralKillopTile>> ASTRAL_KILLOP_TILE = register("astral_killop_tile", ModBlockss.ASTRAL_KILLOP, AstralKillopTile::new);
    public static final RegistryObject<BlockEntityType<DaybloomBlockTile>> DAYBLOOM_TILE = register("daybloom_tile",ModBlockss.DAYBLOOM, DaybloomBlockTile::new);
    public static final RegistryObject<BlockEntityType<BelieverTile>> BELIEVERTILE = register("believertile",ModBlockss.BELIEVER, BelieverTile::new);
    public static final RegistryObject<BlockEntityType<OrechidEndiumTile>> ORECHIDENDIUMTILE = register("orechidendiumtile", ModBlockss.ORECHIDENDIUM, OrechidEndiumTile::new);
    public static final RegistryObject<BlockEntityType<SoarleanderTile>> SOARLEANDERTILE = register("soarleander", ModBlockss.SOARLEANDER, SoarleanderTile::new);
    public static final RegistryObject<BlockEntityType<AsgardFlowerTile>> ASGARDANDELIONTILE = register("asgardandeliontile", ModBlockss.ASGARDANDELION, AsgardFlowerTile::new);
    public static final RegistryObject<BlockEntityType<FloweyTile>> FLOWEYTILE = register("floweytile",ModBlockss.FLOWEY, FloweyTile::new);
    public static final RegistryObject<BlockEntityType<WitchOpoodTile>> WITCH_OPOOD_TILE = register("witch_opood_tile",ModBlockss.WITCH_OPOOD, WitchOpoodTile::new);

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryName, RegistryObject<Block> block,
                                                                                       BlockEntityType.BlockEntitySupplier<T> supplier) {
        return REGISTRY.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

    @Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEventSubscriber {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
