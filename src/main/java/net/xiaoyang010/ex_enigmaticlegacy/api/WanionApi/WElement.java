package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ITooltipSupplier;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public abstract class WElement<E extends WElement<E>> {
    public final static ITooltipSupplier DEFAULT_WELEMENT_TOOLTIP_SUPPLIER = ((interaction, stackSupplier) -> Collections.emptyList());
    public final static Supplier<ItemStack> DEFAULT_ITEMSTACK_SUPPLIER = () -> ItemStack.EMPTY;
    private final Predicate<WInteraction> defaultCheck = wInteraction -> wInteraction.isHovering(this);
    private final Class<E> elementClass;
    private Predicate<WInteraction> foregroundCheck = defaultCheck;
    private Predicate<WInteraction> interactionCheck = defaultCheck;
    private Supplier<ItemStack> stackSupplier = DEFAULT_ITEMSTACK_SUPPLIER;
    private ITooltipSupplier tooltipSupplier = DEFAULT_WELEMENT_TOOLTIP_SUPPLIER;
    private WKeyInteraction.IWKeyInteraction keyInteraction = this::interact;
    private WMouseInteraction.IWMouseInteraction mouseInteraction = this::interact;
    protected final WGuiContainer<?> wGuiContainer;

    protected final int width, height;
    protected int x, y;
    protected boolean enabled = true;

    public WElement(@Nonnull final WGuiContainer<?> wGuiContainer)
    {
        this(wGuiContainer, 0, 0);
    }

    public WElement(@Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y) {
        this(wGuiContainer, x, y, 0, 0);
    }

    @SuppressWarnings("unchecked")
    public WElement(@Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.wGuiContainer = wGuiContainer;
        this.elementClass = (Class<E>) getClass();
    }

    @Nonnull
    public Predicate<WInteraction> getForegroundCheck()
    {
        return foregroundCheck;
    }

    @Nonnull
    public final E setForegroundCheck(@Nonnull final Predicate<WInteraction> foregroundCheck) {
        this.foregroundCheck = foregroundCheck;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setDefaultChecks()
    {
        return setDefaultForegroundCheck().setDefaultInteractionCheck();
    }

    @Nonnull
    public final E setDefaultForegroundCheck() {
        this.foregroundCheck = defaultCheck;
        return elementClass.cast(this);
    }

    @Nonnull
    public ITooltipSupplier getTooltipSupplier()
    {
        return tooltipSupplier;
    }

    @Nonnull
    public final E setTooltipSupplier(@Nonnull final ITooltipSupplier tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setDefaultTooltipSupplier() {
        this.tooltipSupplier = DEFAULT_WELEMENT_TOOLTIP_SUPPLIER;
        return elementClass.cast(this);
    }

    @Nonnull
    public Supplier<ItemStack> getItemStackSupplier()
    {
        return stackSupplier;
    }

    @Nonnull
    public final E setItemStackSupplier(@Nonnull final Supplier<ItemStack> stackSupplier) {
        this.stackSupplier = stackSupplier;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setDefaultItemStackSupplier() {
        this.stackSupplier = DEFAULT_ITEMSTACK_SUPPLIER;
        return elementClass.cast(this);
    }

    @Nonnull
    public final List<Component> getTooltip(@Nonnull final WInteraction interaction) {
        return getTooltipSupplier().getTooltip(interaction, getItemStackSupplier());
    }

    public int getX()
    {
        return x;
    }

    public final void setX(final int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public final void setY(final int y)
    {
        this.y = y;
    }

    public final int getUsableX() {
        return wGuiContainer.leftPos() + getX();
    }

    public final int getUsableY()
    {
        return wGuiContainer.topPos() + getY();
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public final int getWindowID()
    {
        return getWContainer().containerId;
    }

    @Nonnull
    public final WGuiContainer<?> getWGuiContainer()
    {
        return wGuiContainer;
    }

    @Nonnull
    public final WContainer<?> getWContainer()
    {
        return wGuiContainer.getContainer();
    }

    @Nonnull
    public final Player getEntityPlayer()
    {
        return wGuiContainer.getEntityPlayer();
    }

    public final boolean isEnabled()
    {
        return enabled;
    }

    public final void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Nonnull
    public final Predicate<WInteraction> getInteractionCheck()
    {
        return this.interactionCheck;
    }

    @Nonnull
    public final E setInteractionCheck(@Nonnull final Predicate<WInteraction> wInteractionPredicate)
    {
        this.interactionCheck = wInteractionPredicate;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setDefaultInteractionCheck()
    {
        this.interactionCheck = defaultCheck;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setKeyInteraction(@Nonnull final WKeyInteraction.IWKeyInteraction IWKeyInteraction)
    {
        this.keyInteraction = IWKeyInteraction;
        return elementClass.cast(this);
    }

    @Nonnull
    public final E setMouseInteraction(@Nonnull final WMouseInteraction.IWMouseInteraction IWMouseInteraction)
    {
        this.mouseInteraction = IWMouseInteraction;
        return elementClass.cast(this);
    }

    public final boolean canInteractWith(@Nonnull final WInteraction wInteraction)
    {
        return interactionCheck.test(wInteraction);
    }

    public final void interaction(@Nonnull final WInteraction wInteraction)
    {
        if (wInteraction instanceof WKeyInteraction)
            keyInteraction.interact((WKeyInteraction) wInteraction);
        else if (wInteraction instanceof WMouseInteraction)
            mouseInteraction.interact((WMouseInteraction) wInteraction);
    }

    public void interact(@Nonnull final WKeyInteraction wKeyInteraction) {}

    public void interact(@Nonnull final WMouseInteraction wMouseInteraction) {}

    public abstract void draw(@Nonnull final WInteraction wInteraction);

    public void drawForeground(@Nonnull final WInteraction interaction, @Nonnull final PoseStack poseStack) {
        List<Component> tooltip;
        if (foregroundCheck.test(interaction) && !(tooltip = getTooltip(interaction)).isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            wGuiContainer.renderComponentTooltip(poseStack, tooltip, getTooltipX(interaction), getTooltipY(interaction));
        }
    }

    public int getTooltipX(@Nonnull final WInteraction wInteraction) {
        return wInteraction.getMouseX() - wGuiContainer.leftPos();
    }

    public int getTooltipY(@Nonnull final WInteraction wInteraction) {
        return wInteraction.getMouseY() - wGuiContainer.topPos();
    }

    public final SoundManager getSoundManager()
    {
        return Minecraft.getInstance().getSoundManager();
    }

    public final TextureManager getTextureManager()
    {
        return Minecraft.getInstance().getTextureManager();
    }

    public final Font getFontRenderer()
    {
        return Minecraft.getInstance().font;
    }

    public final void playPressSound() {
        getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public final void playPressSound(@Nonnull final SimpleSoundInstance sound)
    {
        getSoundManager().play(sound);
    }

    protected final void bindTexture(@Nonnull final ResourceLocation resourceLocation) {
        RenderSystem.setShaderTexture(0, resourceLocation);
    }
}