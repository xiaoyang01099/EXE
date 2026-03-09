package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.common.block.tile.mana.TileSpreader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@Mixin(TileSpreader.WandHud.class)
public class SpreaderWandHudMixin {

    @Shadow(remap = false)
    @Final
    private TileSpreader spreader;

    @Inject(method = "renderHUD", at = @At("HEAD"), cancellable = true, remap = false)
    public void mana_display$renderHUD(PoseStack ms, Minecraft mc, CallbackInfo ci) {

        if(spreader instanceof ManaSpreaderBlockEntityAccessor mtechlab_spreader){
            String spreaderName = (new ItemStack(this.spreader.getBlockState().getBlock())).getHoverName().getString();
            ItemStack lensStack = this.spreader.getItemHandler().getItem(0);
            ItemStack receiverStack = mtechlab_spreader.getReceiver() == null ? ItemStack.EMPTY : new ItemStack(Objects.requireNonNull(this.spreader.getLevel()).getBlockState(mtechlab_spreader.getReceiver().getManaReceiverPos()).getBlock());

            int lensWidth = mana_display$itemWithNameWidth(mc, lensStack);
            int receiverWidth = mana_display$itemWithNameWidth(mc, receiverStack);
            int width = 4 + Collections.max(Arrays.asList(102, mc.font.width(spreaderName), lensWidth, receiverWidth));
            int height = 22 + (lensStack.isEmpty() ? 0 : 18) + (receiverStack.isEmpty() ? 0 : 18);
            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int centerY = mc.getWindow().getGuiScaledHeight() / 2;

            mana_display$renderHUDBox(ms, centerX - width / 2, centerY - 5, centerX + width / 2, centerY + 8 + height);

            int color = this.spreader.getVariant().hudColor;
            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, this.spreader.getCurrentMana(), this.spreader.getVariant().manaCapacity, spreaderName);

            mana_display$renderItemWithNameCentered(ms, mc, receiverStack, centerY + 30, color);
            mana_display$renderItemWithNameCentered(ms, mc, lensStack, centerY + (receiverStack.isEmpty() ? 30 : 48), color);
        }

        ci.cancel();
    }

    @Unique
    private void mana_display$renderHUDBox(PoseStack ms, int x1, int y1, int x2, int y2) {
        GuiComponent.fill(ms, x1, y1, x2, y2, 0x55000000);
        GuiComponent.fill(ms, x1, y1, x2, y1 + 1, 0x55FFFFFF);
        GuiComponent.fill(ms, x1, y1, x1 + 1, y2, 0x55FFFFFF);
        GuiComponent.fill(ms, x2 - 1, y1, x2, y2, 0x55000000);
        GuiComponent.fill(ms, x1, y2 - 1, x2, y2, 0x55000000);
    }

    @Unique
    private int mana_display$itemWithNameWidth(Minecraft mc, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return 16 + mc.font.width(stack.getHoverName()) / 2;
    }

    @Unique
    private void mana_display$renderItemWithNameCentered(PoseStack ms, Minecraft mc, ItemStack stack, int y, int color) {
        if (!stack.isEmpty()) {
            String stackName = stack.getHoverName().getString();
            int width = 16 + mc.font.width(stackName) / 2;
            int x = mc.getWindow().getGuiScaledWidth() / 2 - width;

            mc.font.drawShadow(ms, stackName, (float)(x + 20), (float)(y + 5), color);
            mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
        }
    }
}