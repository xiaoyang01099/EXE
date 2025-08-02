package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemsRemainingRender {
    private static final int maxTicks = 30;
    private static final int leaveTicks = 20;
    private static ItemStack stack = ItemStack.EMPTY;
    private static int ticks;
    private static String text = "";

    @OnlyIn(Dist.CLIENT)
    public static void render(PoseStack poseStack, float partialTicks) {
        if (ticks > 0 && !stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int pos = maxTicks - ticks;
            int x = screenWidth / 2 + 10 + Math.max(0, pos - leaveTicks);
            int y = screenHeight / 2;
            int start = maxTicks - leaveTicks;
            float alpha = (float)ticks + partialTicks > (float)start ? 1.0F : ((float)ticks + partialTicks) / (float)start;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            int xp = x + (int)(16.0F * (1.0F - alpha));

            poseStack.pushPose();
            poseStack.translate(xp, y, 0.0F);
            poseStack.scale(alpha, 1.0F, 1.0F);

            try {
                ItemRenderer itemRenderer = mc.getItemRenderer();
                itemRenderer.renderAndDecorateItem(stack, 0, 0);
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.warn("物品渲染失败: " + e.getMessage());
            }

            poseStack.popPose();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (text != null && !text.isEmpty()) {
                Font font = mc.font;
                int color = 0xFFFFFF | (int)(alpha * 255.0F) << 24;

                poseStack.pushPose();
                font.draw(poseStack, text, x + 20, y + 6, color);
                poseStack.popPose();
            }

            RenderSystem.disableBlend();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.ClientTickEvent.Phase.END) {
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
        ItemsRemainingRender.stack = itemStack != null ? itemStack : ItemStack.EMPTY;
        ItemsRemainingRender.text = displayText != null ? displayText : "";
        ticks = stack.isEmpty() ? 0 : maxTicks;
    }
}