package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import vazkii.botania.common.block.ModBlocks;

import java.util.HashMap;
import java.util.Map;

public class PolychromeCollapsePrismDrawable implements IDrawable {
    private final Map<Character, IDrawable> blockDrawables;
    private final int blockSize = 8; // 每个方块的显示大小
    private final int layerSpacing = 80; // 层间距

    // 完整的多方块结构定义
    private final String[][] LAYER_0 = {
            {"_______________"},
            {"_______________"},
            {"_______________"},
            {"_______________"},
            {"____X_____X____"},
            {"_______Z_______"},
            {"_______________"},
            {"_____Z___Z_____"},
            {"_______________"},
            {"_______Z_______"},
            {"____X_____X____"},
            {"_______________"},
            {"_______________"},
            {"_______________"},
            {"_______________"}
    };

    private final String[][] LAYER_1 = {
            {"_______________"},
            {"_______________"},
            {"_______________"},
            {"______J_J______"},
            {"____H_____H____"},
            {"_______K_______"},
            {"___J_______J___"},
            {"_____K_P_K_____"},
            {"___J_______J___"},
            {"_______K_______"},
            {"____H_____H____"},
            {"______J_J______"},
            {"_______________"},
            {"_______________"},
            {"_______________"}
    };

    private final String[][] LAYER_2 = {
            {"_______G_______"},
            {"______OIO______"},
            {"_____FIUIF_____"},
            {"____FIYTYIF____"},
            {"___FIYTETYIF___"},
            {"__FIYEWQWEYIF__"},
            {"_OIYTWRLRWTYIO_"},
            {"GIUTEQL0LQETUIG"},
            {"_OIYTWRLRWTYIO_"},
            {"__FIYEWQWEYIF__"},
            {"___FIYTETYIF___"},
            {"____FIYTYIF____"},
            {"_____FIUIF_____"},
            {"______OIO______"},
            {"_______G_______"}
    };

    public PolychromeCollapsePrismDrawable(IGuiHelper guiHelper) {
        blockDrawables = new HashMap<>();

        // 创建所有方块的drawable
        blockDrawables.put('P', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get())));
        blockDrawables.put('R', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.MITHRILL_BLOCK.get())));
        blockDrawables.put('0', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.BLOCKNATURE.get())));
        blockDrawables.put('L', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.GAIA_BLOCK.get())));
        blockDrawables.put('Q', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.ARCANE_ICE_CHUNK.get())));
        blockDrawables.put('W', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get())));
        blockDrawables.put('E', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.DRAGON_CRYSTALS_BLOCK.get())));
        blockDrawables.put('T', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get())));
        blockDrawables.put('Y', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.livingrock)));
        blockDrawables.put('U', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.GLOWSTONE)));
        blockDrawables.put('I', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.INFINITYGlASS.get())));
        blockDrawables.put('O', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.terrasteelBlock)));
        blockDrawables.put('G', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.dragonstoneBlock)));
        blockDrawables.put('F', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.DECAY_BLOCK.get())));
        blockDrawables.put('H', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.MANA_CONTAINER.get())));
        blockDrawables.put('J', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.INFINITY_POTATO.get())));
        blockDrawables.put('K', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.fabulousPool)));
        blockDrawables.put('Z', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.manaPylon)));
        blockDrawables.put('X', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.naturaPylon)));
    }


    @Override
    public int getWidth() {
        return 15 * blockSize + 2 * layerSpacing; // 3层并排显示
    }

    @Override
    public int getHeight() {
        return 15 * blockSize + 40; // 高度加上标签空间
    }

    @Override
    public void draw(PoseStack ms, int xOffset, int yOffset) {
        ms.pushPose();

        // 绘制层级0 (底层)
        drawLayer(ms, LAYER_0, xOffset, yOffset, "Layer 1 (Bottom)");

        // 绘制层级1 (中层)
        drawLayer(ms, LAYER_1, xOffset + layerSpacing, yOffset, "Layer 2 (Middle)");

        // 绘制层级2 (顶层)
        drawLayer(ms, LAYER_2, xOffset + 2 * layerSpacing, yOffset, "Layer 3 (Top)");

        ms.popPose();
    }

    private void drawLayer(PoseStack ms, String[][] layer, int startX, int startY, String layerName) {
        ms.pushPose();

        for (int row = 0; row < 15; row++) {
            String rowData = layer[row][0];
            for (int col = 0; col < 15; col++) {
                char blockChar = rowData.charAt(col);

                if (blockChar != '_') {
                    IDrawable blockDrawable = blockDrawables.get(blockChar);
                    if (blockDrawable != null) {
                        int x = startX + col * blockSize;
                        int y = startY + 20 + row * blockSize;

                        ms.pushPose();
                        ms.scale(0.5f, 0.5f, 1.0f);
                        blockDrawable.draw(ms, x * 2, y * 2);
                        ms.popPose();
                    }
                }
            }
        }

        ms.popPose();
    }
}