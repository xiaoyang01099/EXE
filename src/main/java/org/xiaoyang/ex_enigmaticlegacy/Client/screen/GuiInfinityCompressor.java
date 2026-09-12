package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.yuo.endless.recipe.EndlessRecipes;
import com.yuo.endless.recipe.NeutroniumRecipe;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.InfinityCompressorControlPacket;

public class GuiInfinityCompressor extends AbstractContainerScreen<ContainerInfinityCompressor> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            "ex_enigmaticlegacy", "textures/gui/infinity_compressor.png");
    private static final ResourceLocation WANION_GUI = new ResourceLocation(
            "ex_enigmaticlegacy", "textures/gui/gui_textures.png");

    public GuiInfinityCompressor(ContainerInfinityCompressor menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable(
                "container.ex_enigmaticlegacy.infinity_compressor.name"));
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);
        boolean recipeHover = isInside(mouseX, mouseY, x + 151, y + 7);
        graphics.blit(WANION_GUI, x + 151, y + 7,
                recipeHover ? 54 : 36, 72, 18, 18, 128, 128);
        graphics.drawString(font, Component.literal("R").withStyle(ChatFormatting.BOLD),
                x + 156, y + 12, 0xFFFFFF, true);

        TileEntityInfinityCompressor tile = menu.getTile();
        boolean redstoneHover = isInside(mouseX, mouseY, x + 151, y + 29);
        graphics.blit(WANION_GUI, x + 151, y + 29,
                redstoneHover ? 18 : 0, 54 + 18 * Math.max(0, Math.min(2, tile.getRedstoneMode())),
                18, 18, 128, 128);
        boolean trashHover = isInside(mouseX, mouseY, x + 151, y + 51);
        graphics.blit(WANION_GUI, x + 151, y + 51,
                trashHover ? 54 : 36, tile.isTrashInvalid() ? 90 : 72,
                18, 18, 128, 128);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        titleLabelX = imageWidth / 2 - font.width(title) / 2;
        super.renderLabels(graphics, mouseX, mouseY);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            if (isInside(mouseX, mouseY, leftPos + 151, topPos + 29)) {
                NetworkHandler.sendToServer(new InfinityCompressorControlPacket(menu.getTile().getBlockPos(), button == 0 ? 2 : 3));
                return true;
            }
            if (button == 0 && isInside(mouseX, mouseY, leftPos + 151, topPos + 51)) {
                NetworkHandler.sendToServer(new InfinityCompressorControlPacket(menu.getTile().getBlockPos(), 1));
                return true;
            }
            if (isInside(mouseX, mouseY, leftPos + 80, topPos + 33)) {
                ItemStack preview = menu.getTile().getPreviewResult();
                if (hasShiftDown() && !preview.isEmpty()) {
                    NetworkHandler.sendToServer(new InfinityCompressorControlPacket(menu.getTile().getBlockPos(), 0));
                    return true;
                }
                if (preview.isEmpty() && minecraft != null && minecraft.player != null) {
                    ItemStack held = minecraft.player.getMainHandItem();
                    if (!held.isEmpty() && minecraft.level != null) {
                        for (NeutroniumRecipe recipe : minecraft.level.getRecipeManager()
                                .getAllRecipesFor(EndlessRecipes.NEUTRONIUM_RECIPE.get())) {
                            if (recipe.getInput().test(held)) {
                                NetworkHandler.sendToServer(new org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.InfinityCompressorRecipePacket(recipe.getId()));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        var displayed = menu.getTile().getPreviewResult();
        if (displayed.isEmpty()) displayed = menu.getTile().getItem(TileEntityInfinityCompressor.OUTPUT_SLOT);
        if (!displayed.isEmpty()) graphics.renderItem(displayed, leftPos + 80, topPos + 33);
        renderTooltip(graphics, mouseX, mouseY);
        if (isInside(mouseX, mouseY, leftPos + 151, topPos + 29)) {
            String mode = switch (menu.getTile().getRedstoneMode()) {
                case 1 -> "OFF";
                case 2 -> "ON";
                default -> "IGNORED";
            };
            graphics.renderTooltip(font, Component.translatable("gui.ex_enigmaticlegacy.infinity_compressor.redstone", mode), mouseX, mouseY);
        }
        if (isInside(mouseX, mouseY, leftPos + 151, topPos + 51)) {
            graphics.renderTooltip(font, Component.translatable("gui.ex_enigmaticlegacy.infinity_compressor.trash"), mouseX, mouseY);
        }
        if (isInside(mouseX, mouseY, leftPos + 80, topPos + 33)) {
            graphics.renderTooltip(font, Component.translatable("gui.ex_enigmaticlegacy.infinity_compressor.clear"), mouseX, mouseY);
        }
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
    }
}
