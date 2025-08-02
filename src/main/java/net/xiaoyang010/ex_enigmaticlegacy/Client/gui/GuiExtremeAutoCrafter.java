package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.*;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ITooltipSupplier;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.Supplier;
import static net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod.MODID;

public final class GuiExtremeAutoCrafter extends WGuiContainer<TileEntityExtremeAutoCrafter> {
    private static final ResourceLocation guiTexture = new ResourceLocation(MODID, "textures/gui/extreme_auto_crafter.png");
    private static final ITooltipSupplier extremeTooltipSupplier = new ExtremeTooltipSupplier();

    public GuiExtremeAutoCrafter(@NotNull final WContainer<TileEntityExtremeAutoCrafter> container,
                                 @NotNull final Inventory playerInventory,
                                 @NotNull final Component title) {
        super(container, guiTexture, title, 343, 276);

        final TileEntityExtremeAutoCrafter tileEntity = getContainer().getTile();
        final Slot slot = menu.slots.get(tileEntity.full);
        addElement(new RecipeResultItemBox(() -> getTile().getExtremeRecipeField().getExtremeRecipeOutput(),
                this, slot.x - 1, slot.y - 29).setTooltipSupplier(extremeTooltipSupplier));
//        addElement(new RedstoneControlWButton(getControl(RedstoneControl.class),
//                this, imageWidth - 25, imageHeight - 25));
//        addElement(new EnergyElement(getControl(EnergyControl.class),
//                this, imageWidth - 25, imageHeight - 83));
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        super.renderBg(poseStack, partialTick, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private static final class ExtremeTooltipSupplier implements ITooltipSupplier {
        @Override
        public List<Component> getTooltip(@NotNull final WInteraction wInteraction, @NotNull final Supplier<ItemStack> supplier) {
            final List<Component> tooltip = ItemElement.DEFAULT_ITEMSTACK_TOOLTIP_SUPPLIER.getTooltip(wInteraction, supplier);
            tooltip.add(EComponent.empty());
            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.clear.recipe")).withStyle(ChatFormatting.GOLD));
            return tooltip;
        }
    }
}