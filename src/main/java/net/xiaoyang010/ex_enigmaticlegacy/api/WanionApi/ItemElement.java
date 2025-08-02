package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ITooltipSupplier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ItemElement extends WElement<ItemElement>
{
    public static final ITooltipSupplier DEFAULT_ITEMSTACK_TOOLTIP_SUPPLIER = ((interaction, stackSupplier) -> interaction.getWGuiContainer().getTooltipFromItem(stackSupplier.get()));
    protected final Supplier<ItemStack> stackSupplier;

    public ItemElement(@Nonnull final Supplier<ItemStack> stackSupplier, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y)
    {
        this(stackSupplier, wGuiContainer, x, y, 16, 16);
    }

    public ItemElement(@Nonnull final Supplier<ItemStack> getStack, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height)
    {
        super(wGuiContainer, x, y, width, height);
        this.stackSupplier = getStack;
        setTooltipSupplier(DEFAULT_ITEMSTACK_TOOLTIP_SUPPLIER);
        setForegroundCheck(interaction -> interaction.isHovering(this) && !stackSupplier.get().isEmpty());
        setItemStackSupplier(stackSupplier);
    }

    @Override
    public void draw(@Nonnull final WInteraction wInteraction)
    {
        final ItemStack stack = this.stackSupplier.get();
        if (stack.isEmpty())
            return;
        try {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();

            wGuiContainer.getMinecraft().getItemRenderer().renderGuiItem(stack, getUsableX(), getUsableY());

        } catch (Exception ignored) {}
    }
}