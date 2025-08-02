package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.RedstoneControl.GUI_TEXTURES;

@OnlyIn(Dist.CLIENT)
public class ItemBoxElement extends ItemElement
{
    protected static final ResourceLocation DEFAULT_RESOURCE_LOCATION = GUI_TEXTURES;

    public ItemBoxElement(@Nonnull final Supplier<ItemStack> stackSupplier, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y)
    {
        super(stackSupplier, wGuiContainer, x, y, 18, 18);
    }

    @Override
    public void draw(@Nonnull final WInteraction wInteraction)
    {
        final ItemStack stack = this.stackSupplier.get();
        final PoseStack poseStack = wInteraction.getPoseStack();

        RenderSystem.setShaderTexture(0, DEFAULT_RESOURCE_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(poseStack, getUsableX(), getUsableY(), 90, 0, width, height, 128, 128);

        if (!stack.isEmpty()) {
            try {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableDepthTest();

                wGuiContainer.getMinecraft().getItemRenderer().renderGuiItem(stack, getUsableX() + 1, getUsableY() + 1);

            } catch (Exception ignored) {}
        }

        if (wInteraction.isHovering(this)) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            wGuiContainer.fillGradient(poseStack, getUsableX() + 1, getUsableY() + 1, getUsableX() + 17, getUsableY() + 17, 0x80FFFFFF, 0x80FFFFFF);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
        }
    }
}