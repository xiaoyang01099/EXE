package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Container.DeconTableMenu;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.PacketIndex;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.Manager.DeconstructionManager;

import java.util.List;

public class DeconTableScreen extends AbstractContainerScreen<DeconTableMenu> {
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/deconstruction.png");
    private DeconButton next;
    private DeconButton back;
    public int recipeIndex;
    private Level level;
    private Inventory inventory;

    public DeconTableScreen(DeconTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.recipeIndex = 0;
        this.level = pPlayerInventory.player.level();
        this.inventory = pPlayerInventory;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(this.next = new DeconButton(x + 160, y + 53, true,
                () -> this.handleButtonClick(this.next)));
        this.addRenderableWidget(this.back = new DeconButton(x + 92, y + 53, false,
                () -> this.handleButtonClick(this.back)));
        this.next.active = false;
        this.back.active = false;
        this.next.text = Component.translatable("button.recipe.next").getString();
        this.back.text = Component.translatable("button.recipe.previous").getString();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        guiGraphics.drawString(this.font, Component.translatable("container.deconstruction"), 28, 6, 4210752);
        guiGraphics.drawString(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 4210752);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CRAFTING_TABLE_GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        List recipeList = DeconstructionManager.instance.getRecipes(this.menu.slots.get(0).getItem());
        if (recipeList.size() == 0) {
            this.recipeIndex = 0;
        }

        if (this.recipeIndex <= recipeList.size()) {
            this.next.active = this.recipeIndex < recipeList.size() - 1;
            this.back.active = this.recipeIndex > 0;
        } else {
            this.next.active = false;
            this.back.active = false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);
        this.drawToolTips(guiGraphics, pMouseX, pMouseY);
    }

    protected void drawToolTips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.isHovering(162, 56, 10, 14, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font,
                    List.of(Component.translatable(this.next.text)), mouseX, mouseY);
        }

        if (this.isHovering(94, 56, 10, 14, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font,
                    List.of(Component.translatable(this.back.text)), mouseX, mouseY);
        }
    }

    protected void handleButtonClick(DeconButton button) {
        boolean changed = false;
        if (button == this.next) {
            ++this.recipeIndex;
            changed = true;
        } else if (button == this.back) {
            --this.recipeIndex;
            changed = true;
        }

        if (changed && this.level.isClientSide) {
            NetworkHandler.CHANNEL.sendToServer(new PacketIndex(this.getRecipeIndex()));
            this.menu.setRecipeIndex(this.getRecipeIndex());
        }
    }

    public int getRecipeIndex() {
        return this.recipeIndex;
    }

    public class DeconButton extends AbstractWidget {
        private final boolean mirrored;
        private final Runnable onPress;
        public String text = "";
        private ResourceLocation guiTexture = new ResourceLocation("ex_enigmaticlegacy",
                "textures/gui/container/deconstruction.png");

        public DeconButton(int x, int y, boolean mirrored, Runnable onPress) {
            super(x, y, 12, 19, Component.empty());
            this.mirrored = mirrored;
            this.onPress = onPress;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.run();
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            boolean mouseHovering = mouseX >= this.getX() && mouseY >= this.getY() &&
                    mouseX < this.getX() + this.width &&
                    mouseY < this.getY() + this.height;

            int textureY = 0;
            int textureX = 176;

            if (!this.active) {
                textureX += 2 * this.width;
            } else if (mouseHovering) {
                textureX += this.width;
            }

            if (!this.mirrored) {
                textureY += this.height;
            }

            guiGraphics.blit(this.guiTexture, this.getX(), this.getY(),
                    textureX, textureY, this.width, this.height);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}