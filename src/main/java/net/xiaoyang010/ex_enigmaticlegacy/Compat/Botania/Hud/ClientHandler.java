package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
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
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.IRankItem;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.mixin.client.AccessorAbstractContainerScreen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            renderTooltip();
        }
    }

    @SubscribeEvent
    public void clientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ItemsRemainingRender.tick();
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.DrawScreenEvent.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen) {
            renderTooltip();
        }
    }

    public static void renderTooltip() {
        Minecraft mc = Minecraft.getInstance();
        Screen gui = mc.screen;

        if (gui instanceof AbstractContainerScreen && mc.player != null &&
                mc.player.containerMenu.getCarried().isEmpty()) {

            AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) gui;
            Slot slot = getHoveredSlot(container);

            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) {
                    int mouseX = (int)(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
                    int mouseY = (int)(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

                    Font font = mc.font;

                    List<Component> tooltip;
                    try {
                        tooltip = stack.getTooltipLines(mc.player,
                                mc.options.advancedItemTooltips ?
                                        net.minecraft.world.item.TooltipFlag.Default.ADVANCED :
                                        net.minecraft.world.item.TooltipFlag.Default.NORMAL);
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
                        PoseStack poseStack = new PoseStack();
                        drawRankItemBar(stack, mouseX, mouseY, offx, offy, width, height, font, poseStack);
                    }
                }
            }
        }
    }

    private static Slot getHoveredSlot(AbstractContainerScreen<?> container) {
        return ((AccessorAbstractContainerScreen) container).getHoveredSlot();
    }

    private static void drawRankItemBar(ItemStack stack, int mouseX, int mouseY, int offx, int offy,
                                        int width, int height, Font font, PoseStack poseStack) {
        IRankItem item = (IRankItem) stack.getItem();
        int level = item.getLevel(stack);
        int[] levels = item.getLevels();
        int max = levels[Math.min(levels.length - 1, level + 1)];
        boolean isMaxLevel = level >= levels.length - 1;
        int curr = item.getMana();
        float percent = level == 0 ? 0.0F : (float) curr / (float) max;
        int rainbowWidth = Math.min(width - (isMaxLevel ? 0 : 1), (int) ((float) width * percent));
        float huePer = width == 0 ? 0.0F : 1.0F / (float) width;
        float hueOff = ((float) ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) * 0.01F;

        RenderSystem.disableDepthTest();

        GuiComponent.fill(poseStack, mouseX + offx - 1, mouseY - offy - height - 1,
                mouseX + offx + width + 1, mouseY - offy, 0xFF000000);

        for (int i = 0; i < rainbowWidth; ++i) {
            int color = Color.HSBtoRGB(hueOff + huePer * (float) i, 1.0F, 1.0F);
            GuiComponent.fill(poseStack, mouseX + offx + i, mouseY - offy - height,
                    mouseX + offx + i + 1, mouseY - offy, 0xFF000000 | color);
        }

        GuiComponent.fill(poseStack, mouseX + offx + rainbowWidth, mouseY - offy - height,
                mouseX + offx + width, mouseY - offy, 0xFF505050);

        String currentRank = getRankName(level);

        font.draw(poseStack, currentRank, mouseX + offx, mouseY - offy - 12, 0xFFFFFF);

        if (!isMaxLevel) {
            String nextRank = getRankName(level + 1);
            font.draw(poseStack, nextRank,
                    mouseX + offx + width - font.width(nextRank),
                    mouseY - offy - 12, 0xFFFFFF);
        }

        RenderSystem.enableDepthTest();
    }

    private static String getRankName(int level) {
        switch (level) {
            case 0: return "§7Novice";
            case 1: return "§fApprentice";
            case 2: return "§9Adept";
            case 3: return "§5Expert";
            case 4: return "§6Master";
            case 5: return "§cGrandmaster";
            default: return "§dTranscendent";
        }
    }
}