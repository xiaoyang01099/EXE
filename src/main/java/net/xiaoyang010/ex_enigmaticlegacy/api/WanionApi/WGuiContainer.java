package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public abstract class WGuiContainer<T extends WTileEntity> extends AbstractContainerScreen<WContainer<T>> implements INBTMessage {
    private final WContainer<T> wContainer;
    private final ResourceLocation guiTextureLocation;
    private final List<WElement<?>> wElements = new ArrayList<>();
    protected final String tileName;
    protected final String inventory;
    protected final Slot firstPlayerSlot;
    protected final TextElement tileNameTextElement;
    protected final TextElement inventoryTextElement;
    private WInteraction latestInteraction;

    public WGuiContainer(@Nonnull final WContainer<T> wContainer, @Nonnull final ResourceLocation guiTextureLocation, @Nonnull final Component title) {
        this(wContainer, guiTextureLocation, title, 0, 0);
    }

    public WGuiContainer(@Nonnull final WContainer<T> wContainer, @Nonnull final ResourceLocation guiTextureLocation, @Nonnull final Component title, final int width, final int height) {
        super(wContainer, Minecraft.getInstance().player.getInventory(), title);
        this.wContainer = wContainer;
        this.guiTextureLocation = guiTextureLocation;
        this.tileName = I18n.get(getMenu().getTileName());
        this.inventory = I18n.get("container.inventory");

        if (width > 0 && height > 0) {
            this.imageWidth = width;
            this.imageHeight = height;
        }

        int totalSlots = menu.slots.size();
        this.firstPlayerSlot = totalSlots >= 36 ? menu.slots.get(totalSlots - 36) : null;

        this.latestInteraction = new WInteraction(this, 0, 0);
        addElement((tileNameTextElement = getTileNameTextElement()));
        addElement((inventoryTextElement = getInventoryTextElement()));
    }

    public final void addElement(@Nonnull final WElement<?> element)
    {
        wElements.add(element);
    }

    protected TextElement getTileNameTextElement()
    {
        return new TextElement(tileName, this, 7, 7);
    }

    protected TextElement getInventoryTextElement() {
        if (firstPlayerSlot != null) {
            return new TextElement(inventory, this, firstPlayerSlot.x - 1, firstPlayerSlot.y - 11);
        }
        return new TextElement(inventory, this, 7, imageHeight - 85); // Default position
    }

    @Nonnull
    public WContainer<T> getContainer()
    {
        return wContainer;
    }

    @Nonnull
    public T getTile()
    {
        return wContainer.getTile();
    }

    public final <C extends IController<?, ?>> C getController(@Nonnull final Class<C> aClass) {
        return getTile().getController(aClass);
    }

    @Nonnull
    public <C extends IControl<C>> C getControl(@Nonnull final Class<C> cClass) {
        return getController(ControlController.class).get(cClass);
    }

    @Nonnull
    public IField<?> getField(@Nonnull final String fieldName) {
        return getController(FieldController.class).getField(fieldName);
    }

    @Nonnull
    public AbstractMatching<?> getMatching(final int matchingNumber) {
        return getController(MatchingController.class).getMatching(matchingNumber);
    }

    @Nonnull
    public Player getEntityPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Nonnull
    public WInteraction getLatestInteraction()
    {
        return latestInteraction;
    }

    @Override
    protected void renderBg(@Nonnull PoseStack poseStack, final float partialTicks, final int mouseX, final int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, guiTextureLocation);

        final boolean smallGui = imageWidth < 256 && imageHeight < 256;
        int texWidth = smallGui ? 256 : Math.max(imageWidth, imageHeight);
        int texHeight = smallGui ? 256 : Math.max(imageWidth, imageHeight);

        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight, texWidth, texHeight);

        this.latestInteraction = new WInteraction(this, mouseX, mouseY);
        getEnabledElements().forEach(element -> element.draw(this.latestInteraction));
        getUpdatableElements().forEach(IUpdatable::update);
    }

    @Override
    protected void renderLabels(@Nonnull PoseStack poseStack, final int mouseX, final int mouseY) {
        this.latestInteraction = new WInteraction(this, mouseX, mouseY);
        getEnabledElements().forEach(element -> element.drawForeground(this.latestInteraction, poseStack));
        for (final Button button : this.renderables.stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .toList()) {
            if (button.isHoveredOrFocused()) {
                button.renderToolTip(poseStack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final WKeyInteraction keyInteraction = new WKeyInteraction(this);
        if (interact(keyInteraction)) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        final WMouseInteraction mouseInteraction = new WMouseInteraction(this);
        if (interact(mouseInteraction)) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
    }

    private boolean interact(@Nonnull final WInteraction wInteraction) {
        for (final WElement<?> element : getInteractableElements(wInteraction)) {
            if (wInteraction.shouldProceed()) {
                element.interaction(wInteraction);
            }
        }
        return wInteraction.shouldProceed();
    }

    private Collection<WElement<?>> getEnabledElements() {
        return wElements.stream().filter(WElement::isEnabled).collect(Collectors.toList());
    }

    private Collection<WElement<?>> getInteractableElements(@Nonnull final WInteraction wInteraction) {
        return wElements.stream()
                .filter(element -> element.isEnabled() && element.canInteractWith(wInteraction))
                .collect(Collectors.toList());
    }

    private Collection<IUpdatable> getUpdatableElements() {
        return wElements.stream()
                .filter(IUpdatable.class::isInstance)
                .map(IUpdatable.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void receiveNBT(@Nonnull final CompoundTag compoundTag) {
        wElements.stream()
                .filter(INBTMessage.class::isInstance)
                .forEach(element -> ((INBTMessage) element).receiveNBT(compoundTag));
    }

    public final int leftPos() {
        return this.leftPos;
    }

    public final int topPos() {
        return this.topPos;
    }
}