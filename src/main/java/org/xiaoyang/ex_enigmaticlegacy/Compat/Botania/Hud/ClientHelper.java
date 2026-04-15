package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.BotaniaBlocks;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {
    public static final ResourceLocation miscHuds =
            new ResourceLocation(Exe.MODID, "textures/misc/engineer_hopper_hud.png");
    private static final ResourceLocation END_SKY_TEXTURE =
            new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE =
            new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    public static Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();

    private static final FloatBuffer buffer = FloatBuffer.allocate(16);
    public static void renderCosmicBackground(PoseStack poseStack, float partialTicks) {
        long time = System.currentTimeMillis();
        random.setSeed(time / 400L);

        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float alpha = (float) (Math.sin(ClientTickHandler.total() * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        Minecraft mc = Minecraft.getInstance();

        RenderSystem.setShaderTexture(0, END_SKY_TEXTURE);

        poseStack.scale(12.0F, 12.0F, 1.0F);

        float gameTime = mc.level.getGameTime() + partialTicks;
        poseStack.mulPose(Axis.XP.rotationDegrees(gameTime * 0.5F));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < 64; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));

            matrix = poseStack.last().pose();

            Color color = Color.getHSBColor(random.nextFloat(), 0.6F, 1.0F);
            float r = color.getRed() / 255.0F;
            float g = color.getGreen() / 255.0F;
            float b = color.getBlue() / 255.0F;

            float size = 0.15F + random.nextFloat() * 0.1F;

            bufferBuilder.vertex(matrix, -size, -size, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(matrix, size, -size, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(matrix, size, size, 0).color(r, g, b, alpha).endVertex();
            bufferBuilder.vertex(matrix, -size, size, 0).color(r, g, b, alpha).endVertex();

            poseStack.popPose();
        }

        tesselator.end();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void renderCosmicBackground(GuiGraphics guiGraphics, MultiBufferSource buffers) {
        RANDOM.setSeed(31100L);

        VertexConsumer buffer = buffers.getBuffer(RenderHelper.STARFIELD);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        float time = (float) (Minecraft.getInstance().level.getGameTime() % 20000L) / 20000.0F;
        Color color = Color.getHSBColor(time % 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderColor(
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                1.0F
        );

        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 1.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 1.0F, 0.0F, 0.0F).endVertex();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawArrow(GuiGraphics guiGraphics, int x, int y, boolean side) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(miscHuds, x, y, side ? 0 : 22, 10, 22, 15);
    }

    public static void drawChanceBar(GuiGraphics guiGraphics, int x, int y, int chance) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(miscHuds, x, y, 0, 0, 57, 6);

        guiGraphics.blit(miscHuds, x + 1, y + 1, 0, 6, 55, 4);

        int chancePercentage = Math.max(0,
                (int) ((double) ((float) chance / 100.0F) * 55.0));

        Color color = new Color(Color.HSBtoRGB(
                (float) chance / 360.0F,
                ((float) Math.sin((double) (ClientTickHandler.ticksInGame
                        + ClientTickHandler.partialTicks) * 0.2) + 1.0F) * 0.3F + 0.4F,
                1.0F
        ));

        RenderSystem.setShaderColor(
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                1.0F
        );

        guiGraphics.blit(miscHuds, x + 1, y + 1, 0, 6, Math.min(55, chancePercentage), 4);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderPoolManaBar(GuiGraphics guiGraphics, int x, int y, int color, float alpha, int mana) {
        Minecraft mc = Minecraft.getInstance();

        int poolCount = (int) Math.floor((double) mana / 1000000.0);
        if (poolCount < 0) poolCount = 0;

        int onePoolMana = mana - poolCount * 1000000;
        String strPool = poolCount + "x";
        int xc = x - mc.font.width(strPool) / 2;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float) xc + 42.0F, (float) y + 5.0F, 0.0F);

        guiGraphics.renderItem(new ItemStack(BotaniaBlocks.manaPool), 0, 0);

        poseStack.translate(18.0F, 5.0F, 300.0F);

        guiGraphics.drawString(mc.font, strPool, 0, 0, color);
        poseStack.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }

        HUDHandler.renderManaBar(guiGraphics, x, y, color, alpha, onePoolMana, 1000000);
    }

    public static void drawPoolManaHUD(GuiGraphics guiGraphics, String name,
                                       int mana, int maxMana, int color) {
        Minecraft mc = Minecraft.getInstance();

        int poolCount = (int) Math.floor((double) mana / 1000000.0);
        int maxPoolCount = (int) Math.floor((double) maxMana / 1000000.0);

        if (poolCount < 0) poolCount = 0;
        if (maxPoolCount < 0) maxPoolCount = 0;

        int onePoolMana = mana - poolCount * 1000000;
        String strPool = poolCount + "x / " + maxPoolCount + "x";

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int xc = screenWidth / 2 - mc.font.width(strPool) / 2 - 3;
        int yc = screenHeight / 2;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float) xc - 6.0F, (float) yc + 30.0F, 0.0F);

        guiGraphics.renderItem(new ItemStack(BotaniaBlocks.manaPool), 0, 0);

        poseStack.translate(18.0F, 4.5F, 300.0F);

        guiGraphics.drawString(mc.font, strPool, 0, 0, color);
        poseStack.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }

        HUDHandler.drawSimpleManaHUD(guiGraphics, color, onePoolMana, 1000000, name);
    }

    public static void drawSimpleManaHUD(GuiGraphics guiGraphics, int color,
                                         int mana, int maxMana, String name,
                                         int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();

        int x = screenWidth / 2 - mc.font.width(name) / 2;
        int y = screenHeight / 2 + 10;

        guiGraphics.drawString(mc.font, name, x, y, color);

        x = screenWidth / 2 - 51;
        y += 10;

        renderManaBar(guiGraphics, x, y, 1.0F, mana, maxMana, color);
    }

    private static void renderManaBar(GuiGraphics guiGraphics, int x, int y, float alpha, int mana, int maxMana, int color) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        guiGraphics.blit(miscHuds, x, y, 0, 0, 102, 5);

        int manaPercentage = Math.max(0, (int) ((double) mana / (double) maxMana * 100));
        if (manaPercentage == 0 && mana > 0) {
            manaPercentage = 1;
        }

        guiGraphics.fill(x + 1, y + 1, x + 1 + manaPercentage, y + 4, color);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void setLightmapTextureCoords(int light, float lightmapX, float lightmapY) {
        int lightX = LightTexture.block(light);
        int lightY = LightTexture.sky(light);

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            mc.gameRenderer.lightTexture().turnOnLightLayer();
        }
    }

    public static void setLightmapTextureCoords() {
        setLightmapTextureCoords(15728880, 240, 240);
    }

    public static Vector3 setRotation(float angel, float vX, float vY, float vZ, Vector3 v3) {
        Vector3 rVec = new Vector3(vX, vY, vZ);
        Vector3 rVec1 = v3.copy().normalize();

        double rAngel = Math.toRadians(angel) * 0.5;
        double sin = Math.sin(rAngel);

        double x = rVec.x * sin;
        double y = rVec.y * sin;
        double z = rVec.z * sin;

        rAngel = Math.cos(rAngel);

        double d = -x * rVec1.x - y * rVec1.y - z * rVec1.z;
        double d1 = rAngel * rVec1.x + y * rVec1.z - z * rVec1.y;
        double d2 = rAngel * rVec1.y - x * rVec1.z + z * rVec1.x;
        double d3 = rAngel * rVec1.z + x * rVec1.y - y * rVec1.x;

        v3.x = d1 * rAngel - d * x - d2 * z + d3 * y;
        v3.y = d2 * rAngel - d * y + d1 * z - d3 * x;
        v3.z = d3 * rAngel - d * z - d1 * y + d2 * x;

        return v3;
    }

    public static Color getCorporeaRuneColor(int posX, int posY, int posZ, int meta) {
        double time = (double) (ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks);
        time += (double) (new Random((long) (posX ^ posY ^ posZ))).nextInt(360);
        float sin = (float) (Math.sin(time / 20.0) * 0.15) - 0.15F;
        int color = 0;

        switch (meta) {
            case 0:
                color = Color.HSBtoRGB(0.0F, 0.0F, 0.54F + sin / 1.2F);
                break;
            case 1:
                color = Color.HSBtoRGB(0.688F, 0.93F, 0.96F + sin - 0.15F);
                break;
            case 2:
                color = Color.HSBtoRGB(0.983F, 0.99F, 1.0F + sin - 0.15F);
                break;
            case 3:
                color = Color.HSBtoRGB(0.319F, 0.92F, 0.95F + sin - 0.15F);
                break;
            case 4:
                color = Color.HSBtoRGB(0.536F, 0.53F, 0.92F + sin - 0.15F);
                break;
        }

        return new Color(color);
    }

    public static void renderIcon(GuiGraphics guiGraphics, int x, int y,
                                  ResourceLocation texture, int size, float alpha) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(texture, x, y, 0, 0, size, size, size, size);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}