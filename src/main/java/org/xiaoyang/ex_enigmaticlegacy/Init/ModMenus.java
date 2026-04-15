package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud.TChest.ContainerItemChest;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.ContainerOverpowered;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.EMCWandMenu;
import org.xiaoyang.ex_enigmaticlegacy.Container.*;
import org.xiaoyang.ex_enigmaticlegacy.Tile.PagedChestBlockTile;
import org.xiaoyang.ex_enigmaticlegacy.Tile.SpectriteChestTile;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "ex_enigmaticlegacy");

    public static final RegistryObject<MenuType<NeutroniumDecompressorMenu>> NEUTRONIUM_DECOMPRESSOR_MENU =
            register("neutronium_decompressor", NeutroniumDecompressorMenu::new);

    public static final RegistryObject<MenuType<ExtremeDisassemblyMenu>> EXTREME_CRAFTING_DISASSEMBLY_MENU =
            register("extreme_crafting_disassembly_table", ExtremeDisassemblyMenu::new);

    public static final RegistryObject<MenuType<StarlitSanctumMenu>> STARLIT_SANCTUM_SCREEN =
            register("starlit_sanctum_screen", StarlitSanctumMenu::new);

    public static final RegistryObject<MenuType<CelestialHTMenu>> CELESTIAL_HOLINESS_TRANSMUTE =
            register("celestial_holiness_transmute", CelestialHTMenu::new);

    public static final RegistryObject<MenuType<DeconTableMenu>> DECON_TABLE_MENU =
            register("deconstruction_table", DeconTableMenu::new);

    public static final RegistryObject<MenuType<ContainerOverpowered>> OVERPOWERED_CONTAINER =
            register("overpowered_container",
                    (windowId, inv, data) -> new ContainerOverpowered(windowId, inv));

    public static final RegistryObject<MenuType<ContainerItemChest>> TALISMAN_CHEST =
            register("talisman_chest",
                    (windowId, inv, data) -> new ContainerItemChest(windowId, inv, inv.player));

    public static final RegistryObject<MenuType<DimensionalMirrorContainer>> DIMENSIONAL_MIRROR =
            register("dimensional_mirror",
                    (windowId, inv, data) -> new DimensionalMirrorContainer(windowId, inv, inv.player));

    public static final RegistryObject<MenuType<RainbowTableContainer>> RAINBOW_TABLE_CONTAINER =
            register("rainbow_table",
                    (windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity blockEntity = inv.player.level.getBlockEntity(pos);
                        return new RainbowTableContainer(null, windowId, inv, blockEntity, pos);
                    });

    public static final RegistryObject<MenuType<PagedChestContainer>> PAGED_CHEST =
            register("paged_chest",
                    (windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        Level level = inv.player.level;
                        return new PagedChestContainer(windowId, inv,
                                (PagedChestBlockTile) level.getBlockEntity(pos));
                    });

    public static final RegistryObject<MenuType<SpectriteChestContainer>> SPECTRITE_CHEST_MENU =
            MENU_TYPES.register("spectrite_chest_menu", () ->
                    IForgeMenuType.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
                        if (be instanceof SpectriteChestTile tile) {
                            return new SpectriteChestContainer(windowId, inv, tile);
                        }
                        return null;
                    })
            );

    public static final RegistryObject<MenuType<ContainerExtremeAutoCrafter>> EXTREME_AUTO_CRAFTER_MENU =
            register("extreme_auto_crafter_menu",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level world = inv.player.level;
                TileEntityExtremeAutoCrafter tileEntity = (TileEntityExtremeAutoCrafter) world.getBlockEntity(pos);
                return new ContainerExtremeAutoCrafter(windowId, inv, tileEntity);
            });

    public static RegistryObject<MenuType<MagicTableMenu>> MAGIC_TABLE_MENU = null;
    public static RegistryObject<MenuType<EMCWandMenu>> EMC_MENU = null;

    public static void registerConditional() {
        if (ModList.get().isLoaded("projecte")) {
            MAGIC_TABLE_MENU = MENU_TYPES.register("magic_table",
                    () -> IForgeMenuType.create(MagicTableMenu::new));
            EMC_MENU = MENU_TYPES.register("emc",
                    () -> IForgeMenuType.create(EMCWandMenu::new));
        }
    }

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(
            String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
    }
}