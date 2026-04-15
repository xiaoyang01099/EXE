package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Block.MagicTableBlock;
import org.xiaoyang.ex_enigmaticlegacy.Client.screen.MagicTableScreen;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.*;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModRecipes;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableConvertPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableCustomAmountPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableGearPacket;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.MagicTableRecipe;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;


public class ProjecteFactory {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Exe.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Exe.MODID);

    public static RegistryObject<Block> MAGIC_TABLE;
    public static RegistryObject<Item> MAGIC_TABLE_ITEM;
    public static RegistryObject<Item> EMC_WAND;

    public static void init(IEventBus bus) {
        BlockRegister();
        ItemRegister();

        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private static void BlockRegister() {
        MAGIC_TABLE = BLOCKS.register("magic_table",
                () -> new MagicTableBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.WOOD)
                                .strength(2.5F)
                                .sound(SoundType.WOOD)
                                .noOcclusion()));
    }

    private static void ItemRegister() {
        MAGIC_TABLE_ITEM = ITEMS.register("magic_table", () -> new BlockItem(MAGIC_TABLE.get(), new Item.Properties()));
        EMC_WAND = ITEMS.register("emc_wand", EMCWand::new);
    }

    public static void RecipeRegister() {
        ModRecipes.MAGIC_TABLE_SERIALIZER = ModRecipes.SERIALIZERS.register(
                "magic_table", MagicTableRecipe.Serializer::new);

        ModRecipes.MAGIC_TABLE_TYPE = ModRecipes.RECIPE_TYPES.register(
                "magic_table", () -> new RecipeType<>() {
                    @Override
                    public String toString() {
                        return new ResourceLocation(Exe.MODID, "magic_table").toString();
                    }
                });
    }

    public static void MenuRegister() {
        ModMenus.registerConditional();
    }

    public static void BlockEntityRegister(
            DeferredRegister<BlockEntityType<?>> blockEntities) {

        ModBlockEntities.MAGIC_TABLE_TILE =
                blockEntities.register("magic_table_tile",
                        () -> BlockEntityType.Builder.of(
                                (pos, state) -> new TileMagicTable(
                                        ModBlockEntities.MAGIC_TABLE_TILE.get(),
                                        pos, state),
                                MAGIC_TABLE.get()
                        ).build(null));
    }

    public static void ScreenRegister() {
        if (ModMenus.MAGIC_TABLE_MENU != null && ModMenus.MAGIC_TABLE_MENU.isPresent()) {
            MenuScreens.register(
                    ModMenus.MAGIC_TABLE_MENU.get(),
                    MagicTableScreen::new);
        }
        if (ModMenus.EMC_MENU != null && ModMenus.EMC_MENU.isPresent()) {
            MenuScreens.register(
                    ModMenus.EMC_MENU.get(),
                    EMCWandScreen::new);
        }
    }

    public static void NetworkRegister(SimpleChannel channel, int startId) {
        channel.registerMessage(
                startId,
                MagicTableConvertPacket.class,
                MagicTableConvertPacket::encode,
                MagicTableConvertPacket::decode,
                MagicTableConvertPacket::handle
        );
        channel.registerMessage(
                startId + 1,
                MagicTableGearPacket.class,
                MagicTableGearPacket::encode,
                MagicTableGearPacket::decode,
                MagicTableGearPacket::handle
        );
        channel.registerMessage(
                startId + 2,
                MagicTableCustomAmountPacket.class,
                MagicTableCustomAmountPacket::encode,
                MagicTableCustomAmountPacket::decode,
                MagicTableCustomAmountPacket::handle
        );
        channel.registerMessage(
                startId + 3,
                EMCWandRequestPacket.class,
                EMCWandRequestPacket::encode,
                EMCWandRequestPacket::decode,
                EMCWandRequestPacket::handle
        );
        channel.registerMessage(
                startId + 4,
                EMCWandSetValuePacket.class,
                EMCWandSetValuePacket::encode,
                EMCWandSetValuePacket::decode,
                EMCWandSetValuePacket::handle
        );
        channel.registerMessage(
                startId + 5,
                EMCWandItemListPacket.class,
                EMCWandItemListPacket::encode,
                EMCWandItemListPacket::decode,
                EMCWandItemListPacket::handle
        );
        channel.registerMessage(
                startId + 6,
                EMCWandResultPacket.class,
                EMCWandResultPacket::encode,
                EMCWandResultPacket::decode,
                EMCWandResultPacket::handle
        );
    }
}