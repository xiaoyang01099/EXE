package net.xiaoyang010.ex_enigmaticlegacy.Util;

import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Random;

public class ClientHelper {
    public static final ResourceLocation MISC_HUDS = new ResourceLocation("ex_enigmaticlegacy", "textures/misc/misc_huds.png");
    private static final ResourceLocation END_SKY = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    public static final Minecraft mc = Minecraft.getInstance();
    private static final FloatBuffer FLOAT_BUFFER = BufferUtils.createFloatBuffer(16);

    public static void renderCosmicBackground() {
        RANDOM.setSeed(31100L);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < 16; i++) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();

            float layer = 16 - i;
            float scale = 0.0625F;
            float alpha = 1.0F / (layer + 1.0F);

            if (i == 0) {
                RenderSystem.setShaderTexture(0, END_SKY);
                alpha = 0.1F;
                layer = 65.0F;
                scale = 0.125F;
            }

            if (i == 1) {
                RenderSystem.setShaderTexture(0, END_PORTAL);
                scale = 0.5F;
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            poseStack.translate(0.0F, 1.5F, 0.0F);

            Matrix4f matrix = poseStack.last().pose();
            setupPortalRendering(matrix, scale, layer, i);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            Color color = Color.getHSBColor(
                    (float) Minecraft.getInstance().level.getGameTime() / 20.0F % 360.0F / 360.0F,
                    1.0F,
                    1.0F
            );

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0.0F, 0.24F, 0.0F).uv(0.0F, 0.0F)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)).endVertex();
            buffer.vertex(matrix, 0.0F, 0.24F, 1.0F).uv(0.0F, 1.0F)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)).endVertex();
            buffer.vertex(matrix, 1.0F, 0.24F, 1.0F).uv(1.0F, 1.0F)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)).endVertex();
            buffer.vertex(matrix, 1.0F, 0.24F, 0.0F).uv(1.0F, 0.0F)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)).endVertex();
            tesselator.end();

            poseStack.popPose();
        }

        RenderSystem.disableBlend();
    }

    private static void setupPortalRendering(Matrix4f matrix, float scale, float layer, int index) {
        matrix.setIdentity();
        matrix.multiply(Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.0F));
        matrix.multiply(Matrix4f.createScaleMatrix(scale, scale, scale));
        matrix.multiply(new Matrix4f(Vector3f.ZP.rotationDegrees(
                (index * index * 4321 + index * 9) * 2.0F
        )));
        matrix.multiply(Matrix4f.createTranslateMatrix(-0.5F, -0.5F, 0.0F));
    }

    public static void drawArrow(PoseStack ms, int x, int y, boolean side) {
        RenderSystem.setShaderTexture(0, MISC_HUDS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(ms, x, y, 0, side ? 0 : 22, 10, 38, 256, 256);
    }

    public static void drawChanceBar(PoseStack ms, int x, int y, int chance) {
        RenderSystem.setShaderTexture(0, MISC_HUDS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(ms, x, y, 0, 0, 57, 6, 256, 256);

        int chancePercentage = Math.max(0, (int)((float)chance / 100.0F * 55.0F));
        GuiComponent.blit(ms, x + 1, y + 1, 0, 6, Math.min(55, chancePercentage), 4, 256, 256);

        Color color = new Color(Color.HSBtoRGB(
                chance / 360.0F,
                (float)(Math.sin(ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks * 0.2) + 1.0F) * 0.3F + 0.4F,
                1.0F
        ));
        RenderSystem.setShaderColor(
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F,
                1.0F
        );
    }

    public static void renderPoolManaBar(PoseStack ms, int x, int y, int color, float alpha, int mana) {
        int poolCount = Math.max(0, (int) Math.floor(mana / 1000000F));
        int onePoolMana = mana - poolCount * 1000000;
        String strPool = poolCount + "x";

        ms.pushPose();

        ms.translate(x + 42F - mc.font.width(strPool) / 2F, y + 5F, 0F);
        renderItemAndEffectIntoGUI(new ItemStack(ModBlocks.manaPool), 0, 0);

        ms.translate(18F, 5F, 300F);
        mc.font.draw(ms, strPool, 0, 0, color);

        ms.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }
        HUDHandler.renderManaBar(ms, x, y, color, alpha, onePoolMana, 1000000);
    }

    public static void drawPoolManaHUD(PoseStack ms, String name, int mana, int maxMana, int color) {
        int poolCount = Math.max(0, (int) Math.floor(mana / 1000000F));
        int maxPoolCount = Math.max(0, (int) Math.floor(maxMana / 1000000F));
        int onePoolMana = mana - poolCount * 1000000;

        String strPool = poolCount + "x / " + maxPoolCount + "x";
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int xc = width / 2 - mc.font.width(strPool) / 2 - 3;
        int yc = height / 2;

        ms.pushPose();

        ms.translate(xc - 6F, yc + 30F, 0F);
        renderItemAndEffectIntoGUI(new ItemStack(ModBlocks.manaPool), 0, 0);

        ms.translate(18F, 4.5F, 300F);
        mc.font.drawShadow(ms, strPool, 0, 0, color);

        ms.popPose();

        if (poolCount * 1000000 == mana) {
            onePoolMana = poolCount * 1000000;
        }

        HUDHandler.drawSimpleManaHUD(ms, color, onePoolMana, 1000000, name);
    }

    public static void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
        mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
    }

    public static Vector3 setRotation(float angle, float vX, float vY, float vZ, Vector3 v3) {
        Vector3 rotationVector = new Vector3(vX, vY, vZ);
        Vector3 normalizedVector = v3.copy().normalize();

        double radianAngle = Math.toRadians(angle) * 0.5F;
        double sin = Math.sin(radianAngle);

        double x = rotationVector.x * sin;
        double y = rotationVector.y * sin;
        double z = rotationVector.z * sin;
        double cosAngle = Math.cos(radianAngle);

        double d = -x * normalizedVector.x - y * normalizedVector.y - z * normalizedVector.z;
        double d1 = cosAngle * normalizedVector.x + y * normalizedVector.z - z * normalizedVector.y;
        double d2 = cosAngle * normalizedVector.y - x * normalizedVector.z + z * normalizedVector.x;
        double d3 = cosAngle * normalizedVector.z + x * normalizedVector.y - y * normalizedVector.x;

        v3.x = d1 * cosAngle - d * x - d2 * z + d3 * y;
        v3.y = d2 * cosAngle - d * y + d1 * z - d3 * x;
        v3.z = d3 * cosAngle - d * z - d1 * y + d2 * x;

        return v3;
    }

    public static Color getCorporeaRuneColor(int posX, int posY, int posZ, int meta) {
        double time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
        time += new Random(posX ^ posY ^ posZ).nextInt(360);
        float sin = (float) (Math.sin(time / 20.0F) * 0.15F) - 0.15F;

        return switch (meta) {
            case 0 -> Color.getHSBColor(0.0F, 0.0F, 0.54F + sin / 1.2F);
            case 1 -> Color.getHSBColor(0.688F, 0.93F, 0.96F + sin - 0.15F);
            case 2 -> Color.getHSBColor(0.983F, 0.99F, 1.0F + sin - 0.15F);
            case 3 -> Color.getHSBColor(0.319F, 0.92F, 0.95F + sin - 0.15F);
            case 4 -> Color.getHSBColor(0.536F, 0.53F, 0.92F + sin - 0.15F);
            default -> Color.WHITE;
        };
    }

    public static void renderTextureAtlas(PoseStack ms, TextureAtlasSprite sprite, int light) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        Matrix4f matrix = ms.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, -0.5F, -0.25F, 0.0F).uv(minU, maxV).endVertex();
        buffer.vertex(matrix, 0.5F, -0.25F, 0.0F).uv(maxU, maxV).endVertex();
        buffer.vertex(matrix, 0.5F, 0.75F, 0.0F).uv(maxU, minV).endVertex();
        buffer.vertex(matrix, -0.5F, 0.75F, 0.0F).uv(minU, minV).endVertex();
        tesselator.end();
    }
}