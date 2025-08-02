package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.client.core.handler.ClientTickHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {

    public static final ResourceLocation miscHuds = new ResourceLocation("ex_enigmaticlegacy:textures/misc/misc_huds.png");
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    public static Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();
    private static final FloatBuffer buffer = createFloatBuffer(16);

    private static FloatBuffer createFloatBuffer(int capacity) {
        return FloatBuffer.allocate(capacity);
    }

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

        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);

        Matrix4f projMatrix = RenderSystem.getProjectionMatrix().copy();

        float[] planeS = {1.0F, 0.0F, 0.0F, 0.0F};
        float[] planeT = {0.0F, 1.0F, 0.0F, 0.0F};
        setFloatBuffer(planeS).rewind();
        setFloatBuffer(planeT).rewind();

        poseStack.scale(12.0F, 12.0F, 1.0F);

        float gameTime = mc.level.getGameTime() + partialTicks;
        poseStack.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(gameTime * 0.5F));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < 64; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));

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

    private static FloatBuffer setFloatBuffer(float... values) {
        buffer.clear();
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public static void renderCosmicBackground(PoseStack poseStack, MultiBufferSource buffers) {
        RANDOM.setSeed(31100L);

        VertexConsumer buffer = buffers.getBuffer(RenderHelper.STARFIELD);
        Matrix4f matrix = poseStack.last().pose();

        float time = (float)(Minecraft.getInstance().level.getGameTime() % 20000L) / 20000.0F;
        Color color = Color.getHSBColor(time % 1.0F, 1.0F, 1.0F);
        float r = color.getRed() / 255.0F;
        float g = color.getGreen() / 255.0F;
        float b = color.getBlue() / 255.0F;

        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 1.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, 1.0F, 0.0F, 0.0F).endVertex();
    }

    public static void drawArrow(PoseStack poseStack, int x, int y, boolean side) {
        RenderSystem.setShaderTexture(0, miscHuds);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderHelper.drawTexturedModalRect(poseStack, x, y, 0, side ? 0 : 22, 38, 10);
    }

    public static void drawChanceBar(PoseStack poseStack, int x, int y, int chance) {
        RenderSystem.setShaderTexture(0, miscHuds);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 绘制背景
        RenderHelper.drawTexturedModalRect(poseStack, x, y, 0, 0, 57, 6);

        // 计算百分比
        int chancePercentage = Math.max(0, (int)((double)((float)chance / 100.0F) * 55.0));

        // 绘制填充条
        RenderHelper.drawTexturedModalRect(poseStack, x + 1, y + 1, 0, 6, 55, 4);

        // 计算颜色
        Color color = new Color(Color.HSBtoRGB((float)chance / 360.0F,
                ((float)Math.sin((double)((float)ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) * 0.2) + 1.0F) * 0.3F + 0.4F, 1.0F));

        // 设置颜色并绘制进度条
        RenderSystem.setShaderColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1.0F);
        RenderHelper.drawTexturedModalRect(poseStack, x + 1, y + 1, 0, 6, Math.min(55, chancePercentage), 4);

        // 重置颜色
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderPoolManaBar(PoseStack poseStack, int x, int y, int color, float alpha, int mana) {
        Minecraft mc = Minecraft.getInstance();
        int poolCount = (int)Math.floor((double)mana / 1000000.0);
        if (poolCount < 0) {
            poolCount = 0;
        }

        int onePoolMana = mana - poolCount * 1000000;
        String strPool = poolCount + "x";
        int xc = x - mc.font.width(strPool) / 2;

        poseStack.pushPose();
        poseStack.translate((float)xc + 42.0F, (float)y + 5.0F, 0.0F);

        // 渲染物品
        mc.getItemRenderer().renderAndDecorateItem(new ItemStack(ModBlocks.manaPool), 0, 0);

        poseStack.translate(18.0F, 5.0F, 300.0F);
        mc.font.drawShadow(poseStack, strPool, 0, 0, color);
        poseStack.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }

        HUDHandler.renderManaBar(poseStack, x, y, color, alpha, onePoolMana, 1000000);
    }

    public static void drawPoolManaHUD(PoseStack poseStack, String name, int mana, int maxMana, int color) {
        Minecraft mc = Minecraft.getInstance();
        int poolCount = (int)Math.floor((double)mana / 1000000.0);
        int maxPoolCount = (int)Math.floor((double)maxMana / 1000000.0);

        if (poolCount < 0) poolCount = 0;
        if (maxPoolCount < 0) maxPoolCount = 0;

        int onePoolMana = mana - poolCount * 1000000;
        String strPool = poolCount + "x / " + maxPoolCount + "x";

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int xc = screenWidth / 2 - mc.font.width(strPool) / 2 - 3;
        int yc = screenHeight / 2;

        poseStack.pushPose();
        poseStack.translate((float)xc - 6.0F, (float)yc + 30.0F, 0.0F);

        mc.getItemRenderer().renderAndDecorateItem(new ItemStack(ModBlocks.manaPool), 0, 0);

        poseStack.translate(18.0F, 4.5F, 300.0F);
        mc.font.drawShadow(poseStack, strPool, 0, 0, color);
        poseStack.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }

        HUDHandler.drawSimpleManaHUD(poseStack, color, onePoolMana, 1000000, name);
    }

    public static void drawSimpleManaHUD(PoseStack poseStack, int color, int mana, int maxMana,
                                         String name, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        int x = screenWidth / 2 - mc.font.width(name) / 2;
        int y = screenHeight / 2 + 10;

        mc.font.drawShadow(poseStack, name, x, y, color);

        x = screenWidth / 2 - 51;
        y += 10;

        renderManaBar(poseStack, x, y, 1.0F, mana, maxMana, color);
    }

    private static void renderManaBar(PoseStack poseStack, int x, int y, float alpha,
                                      int mana, int maxMana, int color) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, miscHuds);

        GuiComponent.blit(poseStack, x, y, 0, 0, 102, 5, 256, 256);

        int manaPercentage = Math.max(0, (int) ((double) mana / (double) maxMana * 100));
        if (manaPercentage == 0 && mana > 0) {
            manaPercentage = 1;
        }

        GuiComponent.fill(poseStack, x + 1, y + 1, x + 1 + manaPercentage, y + 4, color);

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
        double time = (double)((float)ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks);
        time += (double)(new Random((long)(posX ^ posY ^ posZ))).nextInt(360);
        float sin = (float)(Math.sin(time / 20.0) * 0.15) - 0.15F;
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

    public static void renderIcon(PoseStack poseStack, int x, int y, ResourceLocation texture,
                                  int size, float alpha) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GuiComponent.blit(poseStack, x, y, 0, 0, size, size, size, size);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}