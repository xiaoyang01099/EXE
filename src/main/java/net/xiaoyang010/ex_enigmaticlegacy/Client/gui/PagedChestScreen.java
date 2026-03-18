package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Container.PagedChestContainer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PagedChestScreen extends AbstractContainerScreen<PagedChestContainer> {
    private static final ResourceLocation CHEST_GUI = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/multipage_chest_gui.png");
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 256;
    private Button nextButton;
    private Button prevButton;

    public PagedChestScreen(PagedChestContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = GUI_HEIGHT;
        this.imageWidth = GUI_WIDTH;
        this.inventoryLabelY = this.imageHeight - 84;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 20;
        int buttonHeight = 20;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.prevButton = this.addRenderableWidget(new Button(
                x - buttonWidth - 4, y + 20,
                buttonWidth, buttonHeight,
                new TextComponent("<"),
                button -> this.menu.previousPage()
        ));

        this.nextButton = this.addRenderableWidget(new Button(
                x + this.imageWidth + 4, y + 20,
                buttonWidth, buttonHeight,
                new TextComponent(">"),
                button -> this.menu.nextPage()
        ));

        updateButtonStates();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);

        String pageText = String.format("Page %d/%d",
                this.menu.getCurrentPage() + 1,
                this.menu.getTotalPages()
        );

        this.font.draw(
                poseStack,
                pageText,
                (this.width - this.font.width(pageText)) / 2f,
                this.topPos - 10,
                0x404040
        );

        Slot trashSlot = this.menu.slots.get(PagedChestContainer.TRASH_SLOT_INDEX);
        int trashScreenX = this.leftPos + trashSlot.x;
        int trashScreenY = this.topPos + trashSlot.y;
        if (mouseX >= trashScreenX && mouseX < trashScreenX + 16
                && mouseY >= trashScreenY && mouseY < trashScreenY + 16
                && !this.menu.getCarried().isEmpty()) {
            this.renderComponentTooltip(poseStack, List.of(
                    new TranslatableComponent("gui.ex_enigmaticlegacy.paged_chest.trash.hint1")
                            .withStyle(ChatFormatting.RED),
                    new TranslatableComponent("gui.ex_enigmaticlegacy.paged_chest.trash.hint2")
                            .withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        this.prevButton.active = this.menu.getCurrentPage() > 0;
        this.nextButton.active = this.menu.getCurrentPage() < this.menu.getTotalPages() - 1;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHEST_GUI);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderTrashSlotOverlay(poseStack, x, y);
    }

    private void renderTrashSlotOverlay(PoseStack poseStack, int guiX, int guiY) {
        Slot trashSlot = this.menu.slots.get(PagedChestContainer.TRASH_SLOT_INDEX);
        int slotX = guiX + trashSlot.x - 1;
        int slotY = guiY + trashSlot.y - 1;

        fill(poseStack, slotX, slotY, slotX + 18, slotY + 1, 0xFFFF4444);
        fill(poseStack, slotX, slotY + 17, slotX + 18, slotY + 18, 0xFFFF4444);
        fill(poseStack, slotX, slotY, slotX + 1, slotY + 18, 0xFFFF4444);
        fill(poseStack, slotX + 17, slotY, slotX + 18, slotY + 18, 0xFFFF4444);
        fill(poseStack, slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x44FF0000);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
    }
}