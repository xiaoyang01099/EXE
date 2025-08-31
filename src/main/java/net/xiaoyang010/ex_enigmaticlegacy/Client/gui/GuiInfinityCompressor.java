package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.CompressorRecipeField;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.*;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ITooltipSupplier;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import static net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod.MODID;

public class GuiInfinityCompressor extends WGuiContainer<TileEntityInfinityCompressor> {
    private static final ResourceLocation guiTexture = new ResourceLocation(MODID, "textures/gui/infinity_compressor.png");
    private static final boolean JEI_PRESENT = ModList.get().isLoaded("jei");

    public GuiInfinityCompressor(@Nonnull final WContainer<TileEntityInfinityCompressor> wContainer,
                                 @Nonnull final Inventory playerInventory,
                                 @Nonnull final Component title) {
        super(wContainer, guiTexture, title, 176, 166);
        addElement(new RecipeResultItemElement(() -> getTile().compressorRecipeField.getCompressorRecipeOutput(),
                this, imageWidth / 2 - 8, 33).setTooltipSupplier(new SingularityTooltipSupplier()));

        final LetterBoxElement showRecipesElement = new LetterBoxElement('R', this, imageWidth - 25, 7);
        if (!JEI_PRESENT)
            showRecipesElement.setDefaultForegroundCheck().setTooltipSupplier((interaction, stackSupplier) ->
                    Lists.newArrayList(EComponent.literal(I18n.get("ex_enigmaticlegacy.no.jei"))));
        addElement(showRecipesElement);
//        addElement(new RedstoneControlWButton(getControl(RedstoneControl.class), this, imageWidth - 25, 29, true));
//        addElement(new CheckBoxWElement((CheckBox) getField("ex_enigmaticlegacy.compressor.trashcan"),
//                this, imageWidth - 25, 51));
    }

    private final class SingularityTooltipSupplier implements ITooltipSupplier {
        @Override
        public List<Component> getTooltip(@Nonnull final WInteraction wInteraction, @Nonnull final Supplier<ItemStack> supplier)
        {
            final TileEntityInfinityCompressor tile = getTile();
            final CompressorRecipeField compressorRecipeField = tile.compressorRecipeField;
            final ItemStack outputStack = compressorRecipeField.getCompressorRecipeOutput();
            final List<Component> tooltip = new ArrayList<>();
            if (compressorRecipeField.isNull() || outputStack.isEmpty())
                return tooltip;

//            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.compressor.making")).withStyle(ChatFormatting.GOLD));
//            tooltip.add(outputStack.getHoverName().copy().withStyle(outputStack.getRarity().color));
//            tooltip.add(EComponent.literal(compressorRecipeField.getProgress()));
//            tooltip.add(Component.nullToEmpty(""));
//            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.clear.recipe")).withStyle(ChatFormatting.GOLD));
//            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.compressor.clear.note") + " " + I18n.get("ex_enigmaticlegacy.compressor.clear.warning"))
//                    .withStyle(ChatFormatting.RED).append(EComponent.literal("").withStyle(ChatFormatting.WHITE)));
            return tooltip;
        }
    }
}