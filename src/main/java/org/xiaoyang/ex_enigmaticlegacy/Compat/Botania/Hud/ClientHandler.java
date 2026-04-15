package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IRankItem;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.mixin.client.AbstractContainerScreenAccessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public static void clientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ItemsRemainingRender.tick();
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen) {
            renderTooltip(event.getGuiGraphics());
        }
    }

    public static void renderTooltip(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Screen gui = mc.screen;

        if (!(gui instanceof AbstractContainerScreen)) return;
        if (mc.player == null) return;
        if (!mc.player.containerMenu.getCarried().isEmpty()) return;

        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) gui;
        Slot slot = getHoveredSlot(container);

        if (slot == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        int mouseX = (int)(mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());
        int mouseY = (int)(mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());

        Font font = mc.font;

        List<Component> tooltip;
        try {
            tooltip = stack.getTooltipLines(
                    mc.player,
                    mc.options.advancedItemTooltips
                            ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED
                            : net.minecraft.world.item.TooltipFlag.Default.NORMAL
            );
        } catch (Exception e) {
            tooltip = new ArrayList<>();
        }

        int width = 0;
        for (Component component : tooltip) {
            FormattedCharSequence sequence = component.getVisualOrderText();
            width = Math.max(width, font.width(sequence) + 2);
        }

        int tooltipHeight = (tooltip.size() - 1) * 10 + 5;
        int height = 3;
        int offx = 11;
        int offy = 17;
        boolean offscreen = mouseX + width + 19 >= mc.getWindow().getGuiScaledWidth();
        int fixY = mc.getWindow().getGuiScaledHeight() - mouseY + tooltipHeight;

        if (fixY < 0) {
            offy -= fixY;
        }
        if (offscreen) {
            offx = -13 - width;
        }

        if (stack.getItem() instanceof IRankItem) {
            drawRankItemBar(stack, mouseX, mouseY, offx, offy, width, height, font, guiGraphics);
        }
    }

    private static Slot getHoveredSlot(AbstractContainerScreen<?> container) {
        return ((AbstractContainerScreenAccessor) container).getHoveredSlot();
    }

    private static void drawRankItemBar(ItemStack stack, int mouseX, int mouseY,
                                        int offx, int offy, int width, int height,
                                        Font font, GuiGraphics guiGraphics) {
        IRankItem item = (IRankItem) stack.getItem();
        int level = item.getLevel(stack);
        int[] levels = item.getLevels();
        int max = levels[Math.min(levels.length - 1, level + 1)];
        boolean isMaxLevel = level >= levels.length - 1;
        int curr = item.getMana();
        float percent = level == 0 ? 0.0F : (float) curr / (float) max;
        int rainbowWidth = Math.min(width - (isMaxLevel ? 0 : 1), (int)((float) width * percent));
        float huePer = width == 0 ? 0.0F : 1.0F / (float) width;
        float hueOff = ((float) ClientTickHandler.ticksInGame
                + ClientTickHandler.partialTicks) * 0.01F;

        RenderSystem.disableDepthTest();

        guiGraphics.fill(
                mouseX + offx - 1, mouseY - offy - height - 1,
                mouseX + offx + width + 1, mouseY - offy,
                0xFF000000
        );

        for (int i = 0; i < rainbowWidth; ++i) {
            int color = Color.HSBtoRGB(hueOff + huePer * (float) i, 1.0F, 1.0F);
            guiGraphics.fill(
                    mouseX + offx + i, mouseY - offy - height,
                    mouseX + offx + i + 1, mouseY - offy,
                    0xFF000000 | color
            );
        }

        guiGraphics.fill(
                mouseX + offx + rainbowWidth, mouseY - offy - height,
                mouseX + offx + width, mouseY - offy,
                0xFF505050
        );

        String currentRank = getRankName(level);

        guiGraphics.drawString(font, currentRank,
                mouseX + offx,
                mouseY - offy - 12,
                0xFFFFFF
        );

        if (!isMaxLevel) {
            String nextRank = getRankName(level + 1);
            guiGraphics.drawString(font, nextRank,
                    mouseX + offx + width - font.width(nextRank),
                    mouseY - offy - 12,
                    0xFFFFFF
            );
        }

        RenderSystem.enableDepthTest();
    }

    private static String getRankName(int level) {
        return switch (level) {
            case 0 -> "§7Novice";
            case 1 -> "§fApprentice";
            case 2 -> "§9Adept";
            case 3 -> "§5Expert";
            case 4 -> "§6Master";
            case 5 -> "§cGrandmaster";
            default -> "§dTranscendent";
        };
    }
}