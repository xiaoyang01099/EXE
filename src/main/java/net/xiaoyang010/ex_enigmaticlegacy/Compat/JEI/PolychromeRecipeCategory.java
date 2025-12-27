package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.IFE.IPolychromeRecipe;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.*;

public class PolychromeRecipeCategory implements IRecipeCategory<IPolychromeRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");

    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable icon;
    private final IDrawable platformStructure;

    // 完整的结构数据
    private static final String[][] STRUCTURE = {
            {
                    "_______________",
                    "_______________",
                    "_______________",
                    "_______________",
                    "____X_____X____",
                    "_______Z_______",
                    "_______________",
                    "_____Z___Z_____",
                    "_______________",
                    "_______Z_______",
                    "____X_____X____",
                    "_______________",
                    "_______________",
                    "_______________",
                    "_______________"
            },
            {
                    "_______________",
                    "_______________",
                    "_______________",
                    "______J_J______",
                    "____H_____H____",
                    "_______K_______",
                    "___J_______J___",
                    "_____K_P_K_____",
                    "___J_______J___",
                    "_______K_______",
                    "____H_____H____",
                    "______J_J______",
                    "_______________",
                    "_______________",
                    "_______________"
            },
            {
                    "_______G_______",
                    "______OIO______",
                    "_____FIUIF_____",
                    "____FIYTYIF____",
                    "___FIYTETYIF___",
                    "__FIYEWQWEYIF__",
                    "_OIYTWRLRWTYIO_",
                    "GIUTEQL0LQETUIG",
                    "_OIYTWRLRWTYIO_",
                    "__FIYEWQWEYIF__",
                    "___FIYTETYIF___",
                    "____FIYTYIF____",
                    "_____FIUIF_____",
                    "______OIO______",
                    "_______G_______"
            }
    };

    public PolychromeRecipeCategory(IGuiHelper guiHelper) {
        // 更大的背景以容纳完整结构
        background = guiHelper.createBlankDrawable(220, 200);

        // 自定义覆盖层
        ResourceLocation overlayTexture = new ResourceLocation(ExEnigmaticlegacyMod.MODID,
                "textures/gui/polychrome_jei_overlay.png");
//        try {
//            overlay = guiHelper.createDrawable(overlayTexture, 0, 0, 128, 128);
//        } catch (Exception e) {
            overlay = guiHelper.createBlankDrawable(128, 128);
//        }

        // 图标
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));

        // 本地化名称
        localizedName = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome");

        // 创建完整的平台结构绘制器
        Map<Character, IDrawable> blockMap = createBlockMap(guiHelper);
        platformStructure = new PolychromeCollapsePrismDrawable(blockMap, STRUCTURE);
    }

    /**
     * 创建方块字符到可绘制对象的映射
     */
    private Map<Character, IDrawable> createBlockMap(IGuiHelper guiHelper) {
        Map<Character, IDrawable> map = new HashMap<>();

        // 所有方块映射
        map.put('P', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get())));
        map.put('R', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.MITHRILL_BLOCK.get())));
        map.put('0', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.BLOCKNATURE.get())));
        map.put('L', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.GAIA_BLOCK.get())));
        map.put('Q', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.ARCANE_ICE_CHUNK.get())));
        map.put('W', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get())));
        map.put('E', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.DRAGON_CRYSTALS_BLOCK.get())));
        map.put('T', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get())));
        map.put('Y', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.livingrock)));
        map.put('U', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(Blocks.GLOWSTONE)));
        map.put('I', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.INFINITYGlASS.get())));
        map.put('O', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.terrasteelBlock)));
        map.put('G', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.dragonstoneBlock)));
        map.put('F', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.DECAY_BLOCK.get())));
        map.put('H', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.MANA_CONTAINER.get())));
        map.put('J', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.INFINITY_POTATO.get())));
        map.put('K', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.fabulousPool)));
        map.put('Z', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.manaPylon)));
        map.put('X', guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.naturaPylon)));

        return map;
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public Class<? extends IPolychromeRecipe> getRecipeClass() {
        return IPolychromeRecipe.class;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(@Nonnull IPolychromeRecipe recipe, @Nonnull IRecipeSlotsView view,
                     @Nonnull PoseStack ms, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font font = Minecraft.getInstance().font;

        // 绘制标题区域背景
        GuiComponent.fill(ms, 5, 5, 215, 25, 0x80000000);

        // 绘制标题
        Component title = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome.title")
                .withStyle(style -> style.withColor(0x9400D3).withBold(true));
        font.draw(ms, title, 10, 10, 0xFFFFFF);

        // 绘制装饰性覆盖层（如果存在）
        if (overlay.getWidth() > 0) {
            ms.pushPose();
            ms.translate(0, 0, -50);
            overlay.draw(ms, 46, 30);
            ms.popPose();
        }

        // 绘制完整的多方块结构
        ms.pushPose();
        ms.translate(0, 0, -100);
        platformStructure.draw(ms, 10, 30);
        ms.popPose();

        // 绘制魔力信息区域
        int mana = recipe.getManaUsage();
        int maxMana = getMaxManaForDisplay(mana);

        // 魔力条背景
        GuiComponent.fill(ms, 5, 180, 215, 195, 0x80000000);

        // 魔力进度条
        HUDHandler.renderManaBar(ms, 10, 185, 0x9400D3, 0.75F, mana, maxMana);

        // 魔力数值文本
        String manaText = formatMana(mana);
        Component manaLabel = EComponent.translatable("jei.ex_enigmaticlegacy.mana_required", manaText);
        int textWidth = font.width(manaLabel);
        font.draw(ms, manaLabel, 110 - textWidth / 2, 172, 0xFFD700);

        // 绘制结构统计信息
        drawStructureStats(ms, font, 5, 155);

        // 绘制使用提示
        Component hint = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome.hint")
                .withStyle(style -> style.withItalic(true));
        font.draw(ms, hint, 10, 165, 0xAAAAAA);

        RenderSystem.disableBlend();
    }

    /**
     * 绘制结构统计信息
     */
    private void drawStructureStats(PoseStack ms, Font font, int x, int y) {
        Map<Character, Integer> blockCount = countBlocks();

        Component statsTitle = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome.structure_stats")
                .withStyle(style -> style.withBold(true));
        font.draw(ms, statsTitle, x, y, 0xFFAA00);

        // 显示总方块数
        int totalBlocks = blockCount.values().stream().mapToInt(Integer::intValue).sum();
        Component total = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome.total_blocks", totalBlocks);
        font.draw(ms, total, x, y + 10, 0xCCCCCC);
    }

    /**
     * 统计结构中每种方块的数量
     */
    private Map<Character, Integer> countBlocks() {
        Map<Character, Integer> count = new HashMap<>();

        for (String[] layer : STRUCTURE) {
            for (String row : layer) {
                for (char c : row.toCharArray()) {
                    if (c != '_') {
                        count.put(c, count.getOrDefault(c, 0) + 1);
                    }
                }
            }
        }

        return count;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull IPolychromeRecipe recipe,
                          @Nonnull IFocusGroup focusGroup) {
        // 输出槽（屏幕中心偏上）
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 65)
                .addItemStack(recipe.getResultItem());

        // 输入材料排列
        List<Ingredient> ingredients = recipe.getIngredients();
        int ingredientCount = ingredients.size();

        if (ingredientCount == 0) {
            return;
        }

        // 根据材料数量选择布局策略
        if (ingredientCount <= 8) {
            // 单环布局
            arrangeSingleCircle(builder, recipe, 102, 65, 40);
        } else if (ingredientCount <= 16) {
            // 双环布局
            int innerCount = ingredientCount / 2;
            arrangeDoubleCircle(builder, recipe, 102, 65, 30, 50, innerCount);
        } else {
            // 三环布局
            int innerCount = ingredientCount / 3;
            int middleCount = innerCount * 2;
            arrangeTripleCircle(builder, recipe, 102, 65, 25, 40, 55, innerCount, middleCount);
        }

        // 催化剂槽（底部）
        builder.addSlot(RecipeIngredientRole.CATALYST, 102, 140)
                .addItemStack(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
    }

    /**
     * 单环布局
     */
    private void arrangeSingleCircle(IRecipeLayoutBuilder builder, IPolychromeRecipe recipe,
                                     int centerX, int centerY, int radius) {
        arrangeInCircle(builder, recipe.getIngredients(), centerX, centerY, radius, 0,
                recipe.getIngredients().size());
    }

    /**
     * 双环布局
     */
    private void arrangeDoubleCircle(IRecipeLayoutBuilder builder, IPolychromeRecipe recipe,
                                     int centerX, int centerY, int innerRadius, int outerRadius,
                                     int innerCount) {
        List<Ingredient> ingredients = recipe.getIngredients();

        // 内环
        arrangeInCircle(builder, ingredients, centerX, centerY, innerRadius, 0, innerCount);

        // 外环
        arrangeInCircle(builder, ingredients, centerX, centerY, outerRadius, innerCount,
                ingredients.size());
    }

    /**
     * 三环布局
     */
    private void arrangeTripleCircle(IRecipeLayoutBuilder builder, IPolychromeRecipe recipe,
                                     int centerX, int centerY, int innerRadius, int middleRadius,
                                     int outerRadius, int innerCount, int middleCount) {
        List<Ingredient> ingredients = recipe.getIngredients();

        // 内环
        arrangeInCircle(builder, ingredients, centerX, centerY, innerRadius, 0, innerCount);

        // 中环
        arrangeInCircle(builder, ingredients, centerX, centerY, middleRadius, innerCount, middleCount);

        // 外环
        arrangeInCircle(builder, ingredients, centerX, centerY, outerRadius, middleCount,
                ingredients.size());
    }

    /**
     * 在圆形路径上排列物品槽
     */
    private void arrangeInCircle(IRecipeLayoutBuilder builder, List<Ingredient> ingredients,
                                 int centerX, int centerY, int radius, int startIndex, int endIndex) {
        int count = endIndex - startIndex;
        if (count <= 0) return;

        double angleBetweenEach = 360.0 / count;
        double startAngle = -90; // 从顶部开始

        for (int i = 0; i < count && (startIndex + i) < ingredients.size(); i++) {
            double angle = Math.toRadians(startAngle + i * angleBetweenEach);
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(ingredients.get(startIndex + i));
        }
    }

    /**
     * 格式化魔力数值
     */
    private String formatMana(int mana) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(mana);
    }

    /**
     * 获取用于显示的最大魔力值
     */
    private int getMaxManaForDisplay(int mana) {
        if (mana <= 100000) return 100000;
        if (mana <= 500000) return 500000;
        if (mana <= 1000000) return 1000000;
        if (mana <= 5000000) return 5000000;
        if (mana <= 10000000) return 10000000;
        if (mana <= 50000000) return 50000000;
        if (mana <= 100000000) return 100000000;
        return ((mana / 100000000) + 1) * 100000000;
    }
}