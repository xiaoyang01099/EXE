package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DeconTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.PacketIndex;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DeconstructionManager;

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
        this.level = pPlayerInventory.player.level;
        this.inventory = pPlayerInventory;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(this.next = new DeconButton(x + 160, y + 53, true,
                button -> this.handleButtonClick(button)));
        this.addRenderableWidget(this.back = new DeconButton(x + 92, y + 53, false,
                button -> this.handleButtonClick(button)));
        this.next.active = false;
        this.back.active = false;
        this.next.text = new TranslatableComponent("button.recipe.next").getString();
        this.back.text = new TranslatableComponent("button.recipe.previous").getString();
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        this.font.draw(pPoseStack, new TranslatableComponent("container.deconstruction"), 28, 6, 4210752);
        this.font.draw(pPoseStack, new TranslatableComponent("container.inventory"), 8, this.imageHeight - 96 + 2, 4210752);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
        this.drawToolTips(pPoseStack, pMouseX, pMouseY);
    }

    protected void drawToolTips(PoseStack pPoseStack, int mouseX, int mouseY) {
        if (this.isHovering(162, 56, 10, 14, mouseX, mouseY)) {
            this.renderTooltip(pPoseStack, new TextComponent(this.next.text), mouseX, mouseY);
        }

        if (this.isHovering(94, 56, 10, 14, mouseX, mouseY)) {
            this.renderTooltip(pPoseStack, new TextComponent(this.back.text), mouseX, mouseY);
        }
    }

    protected void handleButtonClick(Button button) {
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

    public class DeconButton extends Button {
        private final boolean mirrored;
        public String text = "";
        private ResourceLocation guiTexture = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/deconstruction.png");

        public DeconButton(int x, int y, boolean mirrored, OnPress pressable) {
            super(x, y, 12, 19, new TextComponent(""), pressable);
            this.mirrored = mirrored;
        }

        @Override
        public void renderButton(PoseStack pPoseStack, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderTexture(0, this.guiTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            boolean mouseHovering = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width &&
                    mouseY < this.y + this.height;

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

            blit(pPoseStack, this.x, this.y, textureX, textureY, this.width, this.height);
        }
    }
}