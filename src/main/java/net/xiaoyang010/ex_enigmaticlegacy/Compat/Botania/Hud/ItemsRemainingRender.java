package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemsRemainingRender {
    private static final int maxTicks = 30;
    private static final int leaveTicks = 20;
    private static ItemStack stack = ItemStack.EMPTY;
    private static int ticks = 0;
    private static String text = "";

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (ticks > 0 && !stack.isEmpty()) {
                render(event.getMatrixStack(), event.getPartialTicks());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void render(PoseStack poseStack, float partialTicks) {
        if (ticks > 0 && !stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int pos = maxTicks - ticks;
            int x = screenWidth / 2 + 10 + Math.max(0, pos - leaveTicks);
            int y = screenHeight - 50;

            int start = maxTicks - leaveTicks;
            float alpha = (float)ticks + partialTicks > (float)start
                    ? 1.0F
                    : ((float)ticks + partialTicks) / (float)start;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            ItemRenderer itemRenderer = mc.getItemRenderer();
            poseStack.pushPose();
            itemRenderer.renderAndDecorateItem(stack, x, y);
            poseStack.popPose();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int color = 0xFFFFFF | (int)(alpha * 255.0F) << 24;
            Font font = mc.font;
            font.draw(poseStack, text, x + 20, y + 6, color);

            RenderSystem.disableBlend();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        if (ticks > 0) {
            --ticks;
        }
    }

    public static void set(ItemStack itemStack, String displayText) {
        ItemsRemainingRender.stack = itemStack;
        ItemsRemainingRender.text = displayText;
        ticks = itemStack == null || itemStack.isEmpty() ? 0 : maxTicks;
    }
}
