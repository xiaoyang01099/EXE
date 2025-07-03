package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {

    // 资源位置
    public static final ResourceLocation miscHuds = new ResourceLocation("ex_enigmaticlegacy", "textures/misc/misc_huds.png");
    public static final ResourceLocation endSky = new ResourceLocation("minecraft", "textures/environment/end_sky.png");
    public static final ResourceLocation endPortal = new ResourceLocation("minecraft", "textures/entity/end_portal.png");

    // 客户端实例和工具
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();
    private static FloatBuffer buffer;

    static {
        buffer = FloatBuffer.allocate(16);
    }


    public static void renderCosmicBackground() {
        random.setSeed(0x1F2F3F4F5F6F7F8FL);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        // 设置渲染状态
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, endSky);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // 设置纹理生成
        RenderSystem.texParameter(3553, 10242, 10497); // GL_TEXTURE_WRAP_S, GL_REPEAT
        RenderSystem.texParameter(3553, 10243, 10497); // GL_TEXTURE_WRAP_T, GL_REPEAT
        RenderSystem.texParameter(3553, 34624, 34625); // GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE

        long time = System.currentTimeMillis();
        float timeF = time * 0.001F;

        poseStack.translate(0.0F, 0.0F, 0.0F);
        poseStack.scale(1.0F, 1.0F, 1.0F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(timeF * 2.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(timeF * 3.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(timeF * 1.0F));

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();

            if (i == 1) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            } else if (i == 2) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            } else if (i == 3) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            } else if (i == 4) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            } else if (i == 5) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
            }

            Matrix4f faceMatrix = poseStack.last().pose();

            float f11 = (timeF * 0.1F + i * 0.2F) % 1.0F;
            float f12 = (timeF * 0.15F + i * 0.3F) % 1.0F;
            float f13 = (timeF * 0.05F + i * 0.1F) % 1.0F;
            Color color = Color.getHSBColor(f11, f12 * 0.5F + 0.5F, f13 * 0.3F + 0.7F);

            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();

            builder.vertex(faceMatrix, -1.0F, -1.0F, -1.0F).uv(0.0F, 0.0F).color(red, green, blue, 255).endVertex();
            builder.vertex(faceMatrix, -1.0F, -1.0F, 1.0F).uv(0.0F, 1.0F).color(red, green, blue, 255).endVertex();
            builder.vertex(faceMatrix, 1.0F, -1.0F, 1.0F).uv(1.0F, 1.0F).color(red, green, blue, 255).endVertex();
            builder.vertex(faceMatrix, 1.0F, -1.0F, -1.0F).uv(1.0F, 0.0F).color(red, green, blue, 255).endVertex();

            poseStack.popPose();
        }

        tessellator.end();

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void drawArrow(PoseStack poseStack, int x, int y, boolean side) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, miscHuds);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int u = side ? 16 : 0;
        GuiComponent.blit(poseStack, x, y, u, 0, 16, 16, 256, 256);
    }

    public static void drawChanceBar(PoseStack poseStack, int x, int y, float chance) {
        chance = Math.max(0.0F, Math.min(1.0F, chance));

        float ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
        float chancePercentage = chance + (float) Math.sin(ticks * 0.1) * 0.02F;
        chancePercentage = Math.max(0.0F, Math.min(1.0F, chancePercentage));

        float hue = chancePercentage * 0.33F;
        int color = Color.HSBtoRGB(hue, 0.8F, 0.9F);
        Color colorObj = new Color(color);

        RenderSystem.setShaderColor(colorObj.getRed() / 255.0F, colorObj.getGreen() / 255.0F,
                colorObj.getBlue() / 255.0F, 1.0F);

        // 绘制条形图
        int width = Math.round(100 * chancePercentage);
        GuiComponent.fill(poseStack, x, y, x + width, y + 4, color);

        // 绘制边框
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.fill(poseStack, x - 1, y - 1, x + 101, y, 0xFF000000); // 上
        GuiComponent.fill(poseStack, x - 1, y + 4, x + 101, y + 5, 0xFF000000); // 下
        GuiComponent.fill(poseStack, x - 1, y - 1, x, y + 5, 0xFF000000); // 左
        GuiComponent.fill(poseStack, x + 100, y - 1, x + 101, y + 5, 0xFF000000); // 右
    }

    /**
     * 渲染魔力池魔力条
     */
    public static void renderPoolManaBar(PoseStack poseStack, int x, int y, int mana, float alpha, int poolCount) {
        Minecraft mc = Minecraft.getInstance();

        int manaToShow = (int) ((float) mana / 1000.0F);
        String poolStr = poolCount + ":" + manaToShow;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        RenderSystem.enableBlend();
        GuiComponent.fill(poseStack, x - 3, y - 3, x + 100 + 3, y + 12 + 3, 0x44000000);

        // 绘制魔力池图标
        ItemRenderer itemRenderer = mc.getItemRenderer();
        ItemStack poolStack = new ItemStack(ModBlocks.manaPool);
        itemRenderer.renderGuiItem(poolStack, x, y);

        Font font = mc.font;
        font.draw(poseStack, poolStr, x + 20, y + 2, 0xFFFFFF);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawPoolManaHUD(PoseStack poseStack, String name, int mana, int maxMana, int color) {
        String text = name + " " + mana + "/" + maxMana;
        int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2;
        int y =  mc.getWindow().getGuiScaledHeight() - 30;

        Font font = mc.font;
        font.draw(poseStack, text, x, y, 0xFFFFFF);

        HUDHandler.renderManaBar(poseStack, x, y + 12, 0x0080FF, 1.0F, mana, maxMana);
    }

    /**
     * 绘制简单魔力HUD
     */
    public static void drawSimpleManaHUD(PoseStack poseStack, int mana, int maxMana, int maxPoolCount, String name) {
        drawPoolManaHUD(poseStack, name, mana, maxMana, maxPoolCount);
    }

    /**
     * 设置光照贴图纹理坐标
     */

    public static void setLightmapTextureCoords(int lightmapX, int lightmapY) {
        // 在1.18.2中，使用打包的光照值
        int packedLight = LightTexture.pack(lightmapX, lightmapY);
        // 光照贴图通过着色器系统处理，这个方法主要用于兼容性
    }

    /**
     * 设置旋转
     */
    public static Vec3 setRotation(float angel, float vX, float vY, float vZ, Vec3 v3) {
        Vec3 rVec = new Vec3(vX, vY, vZ).normalize();

        double rAngel = Math.toRadians(angel);
        double cos = Math.cos(rAngel);
        double sin = Math.sin(rAngel);

        // 罗德里格旋转公式
        double d1 = cos + rVec.x * rVec.x * (1.0 - cos);
        double d2 = rVec.x * rVec.y * (1.0 - cos) - rVec.z * sin;
        double d3 = rVec.x * rVec.z * (1.0 - cos) + rVec.y * sin;

        return new Vec3(
                d1 * v3.x + d2 * v3.y + d3 * v3.z,
                (rVec.y * rVec.x * (1.0 - cos) + rVec.z * sin) * v3.x + (cos + rVec.y * rVec.y * (1.0 - cos)) * v3.y + (rVec.y * rVec.z * (1.0 - cos) - rVec.x * sin) * v3.z,
                (rVec.z * rVec.x * (1.0 - cos) - rVec.y * sin) * v3.x + (rVec.z * rVec.y * (1.0 - cos) + rVec.x * sin) * v3.y + (cos + rVec.z * rVec.z * (1.0 - cos)) * v3.z
        );
    }

    public static Color getCorporeaRuneColor(int posX, int posY, int posZ, int meta, int time) {
        random.setSeed(posX ^ posY ^ posZ ^ meta);
        float hue = random.nextFloat();

        // 基于时间的颜色变化
        float timeOffset = (time % 80) / 80.0F;
        hue = (hue + timeOffset * 0.1F) % 1.0F;

        // 多种颜色主题
        float saturation, brightness;
        switch (meta % 6) {
            case 0: // 蓝色主题
                saturation = 0.8F + random.nextFloat() * 0.2F;
                brightness = 0.6F + random.nextFloat() * 0.4F;
                hue = 0.6F + random.nextFloat() * 0.1F;
                break;
            case 1: // 绿色主题
                saturation = 0.7F + random.nextFloat() * 0.3F;
                brightness = 0.5F + random.nextFloat() * 0.5F;
                hue = 0.3F + random.nextFloat() * 0.1F;
                break;
            case 2: // 紫色主题
                saturation = 0.9F + random.nextFloat() * 0.1F;
                brightness = 0.4F + random.nextFloat() * 0.6F;
                hue = 0.8F + random.nextFloat() * 0.1F;
                break;
            case 3: // 红色主题
                saturation = 0.8F + random.nextFloat() * 0.2F;
                brightness = 0.7F + random.nextFloat() * 0.3F;
                hue = random.nextFloat() * 0.1F;
                break;
            case 4: // 橙色主题
                saturation = 0.9F + random.nextFloat() * 0.1F;
                brightness = 0.8F + random.nextFloat() * 0.2F;
                hue = 0.1F + random.nextFloat() * 0.1F;
                break;
            default: // 黄色主题
                saturation = 0.6F + random.nextFloat() * 0.4F;
                brightness = 0.9F + random.nextFloat() * 0.1F;
                hue = 0.15F + random.nextFloat() * 0.05F;
                break;
        }

        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static void renderIcon(PoseStack poseStack, TextureAtlasSprite icon, int size) {
        if (icon == null) return;

        float minU = icon.getU0();
        float maxU = icon.getU1();
        float minV = icon.getV0();
        float maxV = icon.getV1();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = poseStack.last().pose();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, 0, size, 0).uv(minU, maxV).endVertex();
        builder.vertex(matrix, size, size, 0).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, size, 0, 0).uv(maxU, minV).endVertex();
        builder.vertex(matrix, 0, 0, 0).uv(minU, minV).endVertex();
        tessellator.end();
    }

    private static FloatBuffer createBuffer(float p1, float p2, float p3, float p4) {
        buffer.clear();
        buffer.put(p1).put(p2).put(p3).put(p4);
        buffer.flip();
        return buffer;
    }
}