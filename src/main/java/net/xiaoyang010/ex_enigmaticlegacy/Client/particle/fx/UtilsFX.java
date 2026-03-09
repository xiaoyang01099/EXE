package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class UtilsFX {
    public static final String[] colorNames = new String[]{"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
    public static final String[] colorCodes = new String[]{"§f", "§6", "§d", "§9", "§e", "§a", "§d", "§8", "§7", "§b", "§5", "§9", "§4", "§2", "§c", "§8"};
    public static final int[] colors = new int[]{15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019};
    public static int[] connectedTextureRefByID = new int[]{0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11};
    public static float[] lightBrightnessTable = null;
    private static Map<Object, Integer> textureSizeCache = new HashMap<>();
    static Map<String, ResourceLocation> boundTextures = new HashMap<>();
    static DecimalFormat myFormatter = new DecimalFormat("#######.##");

    public static float getBrightnessFromLight(int light) {
        if (lightBrightnessTable == null) {
            lightBrightnessTable = new float[16];
            float f = 0.0F;

            for(int i = 0; i <= 15; ++i) {
                float f1 = 1.0F - (float)i / 15.0F;
                lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
            }
        }

        return lightBrightnessTable[light];
    }

    public static void infusedStoneSparkle(Level world, int x, int y, int z, int md) {
        if (world.isClientSide) {
            int color = 0;
            switch (md) {
                case 1:
                    color = 1;
                    break;
                case 2:
                    color = 4;
                    break;
                case 3:
                    color = 2;
                    break;
                case 4:
                    color = 3;
                    break;
                case 5:
                    color = 6;
                    break;
                case 6:
                    color = 5;
            }

            for(int a = 0; a < 3; ++a) {
                FXSparkle fx = new FXSparkle((ClientLevel)world, (double)((float)x + world.random.nextFloat()), (double)((float)y + world.random.nextFloat()), (double)((float)z + world.random.nextFloat()), 1.75F, color == -1 ? world.random.nextInt(5) : color, 3 + world.random.nextInt(3));
                fx.setGravity(0.1F);
                ParticleEngine.instance.addEffect(world, fx);
            }
        }
    }

    public static void shootFire(Level world, Player p, boolean offset, int range, boolean lance) {
        Vec3 vec3d = p.getViewVector(1.0F);
        double px = p.getX() - (double)(Mth.cos(p.getYRot() / 180.0F * (float)Math.PI) * 0.1F);
        double py = p.getY() - (double)0.08F;
        double pz = p.getZ() - (double)(Mth.sin(p.getYRot() / 180.0F * (float)Math.PI) * 0.1F);
        if (p.getId() != Minecraft.getInstance().player.getId()) {
            py = p.getBoundingBox().minY + (double)(p.getBbHeight() / 2.0F) + (double)0.25F;
        }

        for(int q = 0; q < 3; ++q) {
            FXScorch ef = new FXScorch((ClientLevel)world, px, py, pz, vec3d, (float)range, lance);
            ParticleEngine.instance.addEffect(world, ef);
        }
    }

    public static void renderFacingQuad(double px, double py, double pz, float angle, float scale, float alpha, int frames, int cframe, float partialTicks, int color) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Player player = (Player)Minecraft.getInstance().getCameraEntity();
            double iPX = Mth.lerp(partialTicks, player.xOld, player.getX());
            double iPY = Mth.lerp(partialTicks, player.yOld, player.getY());
            double iPZ = Mth.lerp(partialTicks, player.zOld, player.getZ());

            RenderSystem.disableDepthTest();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            Vec3 cameraPos = camera.getPosition();
            Vec3 look = new Vec3(px - cameraPos.x, py - cameraPos.y, pz - cameraPos.z).normalize();
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = look.cross(up).normalize();
            up = right.cross(look).normalize();

            Vec3 v1 = new Vec3(-scale, -scale, 0);
            Vec3 v2 = new Vec3(-scale, scale, 0);
            Vec3 v3 = new Vec3(scale, scale, 0);
            Vec3 v4 = new Vec3(scale, -scale, 0);

            float f2 = (float)cframe / (float)frames;
            float f3 = (float)(cframe + 1) / (float)frames;
            float f4 = 0.0F;
            float f5 = 1.0F;

            Color co = new Color(color);
            float r = co.getRed() / 255.0F;
            float g = co.getGreen() / 255.0F;
            float b = co.getBlue() / 255.0F;

            buffer.vertex(px + v1.x, py + v1.y, pz + v1.z).uv(f2, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v2.x, py + v2.y, pz + v2.z).uv(f3, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v3.x, py + v3.y, pz + v3.z).uv(f3, f4).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v4.x, py + v4.y, pz + v4.z).uv(f2, f4).color(r, g, b, alpha).endVertex();
            tessellator.end();
        }
    }

    public static void renderFacingStrip(double px, double py, double pz, float angle, float scale, float alpha, int frames, int strip, int frame, float partialTicks, int color) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Player player = (Player)Minecraft.getInstance().getCameraEntity();
            double iPX = Mth.lerp(partialTicks, player.xOld, player.getX());
            double iPY = Mth.lerp(partialTicks, player.yOld, player.getY());
            double iPZ = Mth.lerp(partialTicks, player.zOld, player.getZ());

            RenderSystem.disableDepthTest();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            Vec3 cameraPos = camera.getPosition();
            Vec3 look = new Vec3(px - cameraPos.x, py - cameraPos.y, pz - cameraPos.z).normalize();
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = look.cross(up).normalize();
            up = right.cross(look).normalize();

            Vec3 v1 = new Vec3(-scale, -scale, 0);
            Vec3 v2 = new Vec3(-scale, scale, 0);
            Vec3 v3 = new Vec3(scale, scale, 0);
            Vec3 v4 = new Vec3(scale, -scale, 0);

            float f2 = (float)frame / (float)frames;
            float f3 = (float)(frame + 1) / (float)frames;
            float f4 = (float)strip / (float)frames;
            float f5 = ((float)strip + 1.0F) / (float)frames;

            Color co = new Color(color);
            float r = co.getRed() / 255.0F;
            float g = co.getGreen() / 255.0F;
            float b = co.getBlue() / 255.0F;

            buffer.vertex(px + v1.x, py + v1.y, pz + v1.z).uv(f3, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v2.x, py + v2.y, pz + v2.z).uv(f3, f4).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v3.x, py + v3.y, pz + v3.z).uv(f2, f4).color(r, g, b, alpha).endVertex();
            buffer.vertex(px + v4.x, py + v4.y, pz + v4.z).uv(f2, f5).color(r, g, b, alpha).endVertex();
            tessellator.end();
        }
    }

    public static void renderAnimatedQuad(float scale, float alpha, int frames, int cframe, float partialTicks, int color) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            float f2 = (float)cframe / (float)frames;
            float f3 = (float)(cframe + 1) / (float)frames;
            float f4 = 0.0F;
            float f5 = 1.0F;

            Color co = new Color(color);
            float r = co.getRed() / 255.0F;
            float g = co.getGreen() / 255.0F;
            float b = co.getBlue() / 255.0F;

            buffer.vertex(-0.5F * scale, 0.5F * scale, 0.0F).uv(f2, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(0.5F * scale, 0.5F * scale, 0.0F).uv(f3, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(0.5F * scale, -0.5F * scale, 0.0F).uv(f3, f4).color(r, g, b, alpha).endVertex();
            buffer.vertex(-0.5F * scale, -0.5F * scale, 0.0F).uv(f2, f4).color(r, g, b, alpha).endVertex();
            tessellator.end();
        }
    }

    public static void renderAnimatedQuadStrip(float scale, float alpha, int frames, int strip, int cframe, float partialTicks, int color) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            float f2 = (float)cframe / (float)frames;
            float f3 = (float)(cframe + 1) / (float)frames;
            float f4 = (float)strip / (float)frames;
            float f5 = (float)(strip + 1) / (float)frames;

            Color co = new Color(color);
            float r = co.getRed() / 255.0F;
            float g = co.getGreen() / 255.0F;
            float b = co.getBlue() / 255.0F;

            buffer.vertex(-0.5F * scale, 0.5F * scale, 0.0F).uv(f2, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(0.5F * scale, 0.5F * scale, 0.0F).uv(f3, f5).color(r, g, b, alpha).endVertex();
            buffer.vertex(0.5F * scale, -0.5F * scale, 0.0F).uv(f3, f4).color(r, g, b, alpha).endVertex();
            buffer.vertex(-0.5F * scale, -0.5F * scale, 0.0F).uv(f2, f4).color(r, g, b, alpha).endVertex();
            tessellator.end();
        }
    }

    public static Vec3 perpendicular(Vec3 v) {
        return v.z == 0.0D ? zCrossProduct(v) : xCrossProduct(v);
    }

    public static Vec3 xCrossProduct(Vec3 v) {
        double d = v.z;
        double d1 = -v.y;
        return new Vec3(0.0D, d, d1);
    }

    public static Vec3 zCrossProduct(Vec3 v) {
        double d = v.y;
        double d1 = -v.x;
        return new Vec3(d, d1, 0.0D);
    }

    public static void drawTexturedQuad(int par1, int par2, int par3, int par4, int par5, int par6, double zLevel) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tesselator var9 = Tesselator.getInstance();
        BufferBuilder buffer = var9.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(par1 + 0, par2 + par6, zLevel).uv((par3 + 0) * var7, (par4 + par6) * var8).endVertex();
        buffer.vertex(par1 + par5, par2 + par6, zLevel).uv((par3 + par5) * var7, (par4 + par6) * var8).endVertex();
        buffer.vertex(par1 + par5, par2 + 0, zLevel).uv((par3 + par5) * var7, (par4 + 0) * var8).endVertex();
        buffer.vertex(par1 + 0, par2 + 0, zLevel).uv((par3 + 0) * var7, (par4 + 0) * var8).endVertex();
        var9.end();
    }

    public static void drawTexturedQuadFull(int par1, int par2, double zLevel) {
        Tesselator var9 = Tesselator.getInstance();
        BufferBuilder buffer = var9.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(par1 + 0, par2 + 16, zLevel).uv(0.0F, 1.0F).endVertex();
        buffer.vertex(par1 + 16, par2 + 16, zLevel).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(par1 + 16, par2 + 0, zLevel).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(par1 + 0, par2 + 0, zLevel).uv(0.0F, 0.0F).endVertex();
        var9.end();
    }

    public static void renderQuad(String texture) {
        renderQuad(texture, 1, 0.66F);
    }

    public static void renderQuad(String texture, int blend, float trans) {
        renderQuad(texture, blend, trans, 1.0F, 1.0F, 1.0F);
    }

    public static void renderQuad(String texture, int blend, float trans, float r, float g, float b) {
        bindTexture(texture);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, blend);
        RenderSystem.setShaderColor(r, g, b, trans);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(0.0D, 1.0D, 0.0D).uv(0.0F, 1.0F).color(r, g, b, trans).endVertex();
        buffer.vertex(1.0D, 1.0D, 0.0D).uv(1.0F, 1.0F).color(r, g, b, trans).endVertex();
        buffer.vertex(1.0D, 0.0D, 0.0D).uv(1.0F, 0.0F).color(r, g, b, trans).endVertex();
        buffer.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, 0.0F).color(r, g, b, trans).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();
    }

    public static int getTextureAnimationSize(String s) {
        if (textureSizeCache.get(s) != null) {
            return (Integer)textureSizeCache.get(s);
        } else {
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("ex_enigmaticlegacy", s)).getInputStream();
                if (inputstream == null) {
                    throw new Exception("Image not found: " + s);
                } else {
                    BufferedImage bi = ImageIO.read(inputstream);
                    int size = bi.getWidth() / bi.getHeight();
                    textureSizeCache.put(s, size);
                    return size;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 16;
            }
        }
    }

    public static int getTextureSize(String s, int dv) {
        if (textureSizeCache.get(Arrays.asList(s, dv)) != null) {
            return (Integer)textureSizeCache.get(Arrays.asList(s, dv));
        } else {
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("ex_enigmaticlegacy", s)).getInputStream();
                if (inputstream == null) {
                    throw new Exception("Image not found: " + s);
                } else {
                    BufferedImage bi = ImageIO.read(inputstream);
                    int size = bi.getWidth() / dv;
                    textureSizeCache.put(Arrays.asList(s, dv), size);
                    return size;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 16;
            }
        }
    }

    public static int getBrightnessForRender(Entity entity, double x, double z) {
        int var2 = Mth.floor(x);
        int var3 = Mth.floor(z);
        if (entity.level.hasChunkAt(new BlockPos(var2, 0, var3))) {
            double var4 = (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.66;
            int var6 = Mth.floor(entity.getY() + var4);
            return entity.level.getLightEmission(new BlockPos(var2, var6, var3));
        } else {
            return 0;
        }
    }

    public static void bindTexture(String texture) {
        ResourceLocation rl = null;
        if (boundTextures.containsKey(texture)) {
            rl = boundTextures.get(texture);
        } else {
            rl = new ResourceLocation("ex_enigmaticlegacy", texture);
            boundTextures.put(texture, rl);
        }
        RenderSystem.setShaderTexture(0, rl);
    }

    public static void bindTexture(String mod, String texture) {
        ResourceLocation rl = null;
        String key = mod + ":" + texture;
        if (boundTextures.containsKey(key)) {
            rl = boundTextures.get(key);
        } else {
            rl = new ResourceLocation(mod, texture);
            boundTextures.put(key, rl);
        }
        RenderSystem.setShaderTexture(0, rl);
    }

    public static void bindTexture(ResourceLocation resource) {
        RenderSystem.setShaderTexture(0, resource);
    }

    public static void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float var7 = (float)(par5 >> 24 & 255) / 255.0F;
        float var8 = (float)(par5 >> 16 & 255) / 255.0F;
        float var9 = (float)(par5 >> 8 & 255) / 255.0F;
        float var10 = (float)(par5 & 255) / 255.0F;
        float var11 = (float)(par6 >> 24 & 255) / 255.0F;
        float var12 = (float)(par6 >> 16 & 255) / 255.0F;
        float var13 = (float)(par6 >> 8 & 255) / 255.0F;
        float var14 = (float)(par6 & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator var15 = Tesselator.getInstance();
        BufferBuilder buffer = var15.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(par3, par2, 300.0D).color(var8, var9, var10, var7).endVertex();
        buffer.vertex(par1, par2, 300.0D).color(var8, var9, var10, var7).endVertex();
        buffer.vertex(par1, par4, 300.0D).color(var12, var13, var14, var11).endVertex();
        buffer.vertex(par3, par4, 300.0D).color(var12, var13, var14, var11).endVertex();
        var15.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static boolean isVisibleTo(float fov, Entity ent, double x, double y, double z) {
        double dist = ent.distanceToSqr(x, y, z);
        if (dist < 4.0D) {
            return true;
        } else {
            Minecraft mc = Minecraft.getInstance();
            double vT = (double)(fov + mc.options.fov / 2.0F);
            int j = 512;
            if (j > 400) {
                j = 400;
            }

            double rD = (double)j;
            float f1 = Mth.cos(-ent.getYRot() * 0.017453292F - (float)Math.PI);
            float f3 = Mth.sin(-ent.getYRot() * 0.017453292F - (float)Math.PI);
            float f5 = -Mth.cos(-ent.getXRot() * 0.017453292F);
            float f7 = Mth.sin(-ent.getXRot() * 0.017453292F);
            double lx = (double)(f3 * f5);
            double ly = (double)f7;
            double lz = (double)(f1 * f5);
            double dx = x + 0.5D - ent.getX();
            double dy = y + 0.5D - ent.getY() - (double)ent.getEyeHeight();
            double dz = z + 0.5D - ent.getZ();
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double dot = dx / len * lx + dy / len * ly + dz / len * lz;
            double angle = Math.acos(dot);
            return angle < vT && mc.options.getCameraType().isFirstPerson() && dist < rD || !mc.options.getCameraType().isFirstPerson() && dist < rD;
        }
    }

    public static void drawCustomTooltip(Screen gui, ItemRenderer itemRenderer, Font fr, List<String> var4, int par2, int par3, int subTipColor) {
        RenderSystem.disableDepthTest();
        if (!var4.isEmpty()) {
            int var5 = 0;

            for(String var7 : var4) {
                int var8 = fr.width(var7);
                if (var8 > var5) {
                    var5 = var8;
                }
            }

            int var15 = par2 + 12;
            int var16 = par3 - 12;
            int var9 = 8;
            if (var4.size() > 1) {
                var9 += 2 + (var4.size() - 1) * 10;
            }

            int var10 = -267386864;
            drawGradientRect(var15 - 3, var16 - 4, var15 + var5 + 3, var16 - 3, var10, var10);
            drawGradientRect(var15 - 3, var16 + var9 + 3, var15 + var5 + 3, var16 + var9 + 4, var10, var10);
            drawGradientRect(var15 - 3, var16 - 3, var15 + var5 + 3, var16 + var9 + 3, var10, var10);
            drawGradientRect(var15 - 4, var16 - 3, var15 - 3, var16 + var9 + 3, var10, var10);
            drawGradientRect(var15 + var5 + 3, var16 - 3, var15 + var5 + 4, var16 + var9 + 3, var10, var10);
            int var11 = 1347420415;
            int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
            drawGradientRect(var15 - 3, var16 - 3 + 1, var15 - 3 + 1, var16 + var9 + 3 - 1, var11, var12);
            drawGradientRect(var15 + var5 + 2, var16 - 3 + 1, var15 + var5 + 3, var16 + var9 + 3 - 1, var11, var12);
            drawGradientRect(var15 - 3, var16 - 3, var15 + var5 + 3, var16 - 3 + 1, var11, var11);
            drawGradientRect(var15 - 3, var16 + var9 + 2, var15 + var5 + 3, var16 + var9 + 3, var12, var12);

            for(int var13 = 0; var13 < var4.size(); ++var13) {
                String var14 = var4.get(var13);
                if (var13 == 0) {
                    var14 = "§" + Integer.toHexString(subTipColor) + var14;
                } else {
                    var14 = "§7" + var14;
                }

                fr.draw(new PoseStack(), var14, var15, var16, -1);
                if (var13 == 0) {
                    var16 += 2;
                }

                var16 += 10;
            }
        }

        RenderSystem.enableDepthTest();
    }

    public static void drawFloatyLine(double x, double y, double z, double x2, double y2, double z2, float partialTicks, int color, String texture, float speed, float distance) {
        drawFloatyLine(x, y, z, x2, y2, z2, partialTicks, color, texture, speed, distance, 0.15F);
    }

    public static void drawFloatyLine(double x, double y, double z, double x2, double y2, double z2, float partialTicks, int color, String texture, float speed, float distance, float width) {
        LivingEntity player = (LivingEntity) Minecraft.getInstance().getCameraEntity();
        double iPX = Mth.lerp(partialTicks, player.xOld, player.getX());
        double iPY = Mth.lerp(partialTicks, player.yOld, player.getY());
        double iPZ = Mth.lerp(partialTicks, player.zOld, player.getZ());

        PoseStack poseStack = new PoseStack();
        poseStack.translate(-iPX + x2, -iPY + y2, -iPZ + z2);

        float time = (float)(System.nanoTime() / 30000000L);
        Color co = new Color(color);
        float r = (float)co.getRed() / 255.0F;
        float g = (float)co.getGreen() / 255.0F;
        float b = (float)co.getBlue() / 255.0F;

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        double dc1x = (double)((float)(x - x2));
        double dc1y = (double)((float)(y - y2));
        double dc1z = (double)((float)(z - z2));
        bindTexture(texture);
        RenderSystem.disableCull();

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        double d3 = x - x2;
        double d4 = y - y2;
        double d5 = z - z2;
        float dist = Mth.sqrt((float)(d3 * d3 + d4 * d4 + d5 * d5));
        float blocks = (float)Math.round(dist);
        float length = blocks * 2.0F;
        float f9 = 0.0F;
        float f10 = 1.0F;

        for(int i = 0; (float)i <= length * distance; ++i) {
            float f2 = (float)i / length;
            float f3 = 1.0F - Math.abs((float)i - length / 2.0F) / (length / 2.0F);
            double dx = dc1x + (double)(Mth.sin((float)((z % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 4.0D)) * 0.5F * f3);
            double dy = dc1y + (double)(Mth.sin((float)((x % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 3.0D)) * 0.5F * f3);
            double dz = dc1z + (double)(Mth.sin((float)((y % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 2.0D)) * 0.5F * f3);

            float f13 = (1.0F - f2) * dist - time * speed;
            buffer.vertex(dx * (double)f2, dy * (double)f2 - (double)width, dz * (double)f2).uv(f13, f10).color(r, g, b, f3).endVertex();
            buffer.vertex(dx * (double)f2, dy * (double)f2 + (double)width, dz * (double)f2).uv(f13, f9).color(r, g, b, f3).endVertex();
        }

        tessellator.end();

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        for(int var84 = 0; (float)var84 <= length * distance; ++var84) {
            float f2 = (float)var84 / length;
            float f3 = 1.0F - Math.abs((float)var84 - length / 2.0F) / (length / 2.0F);
            double dx = dc1x + (double)(Mth.sin((float)((z % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 4.0D)) * 0.5F * f3);
            double dy = dc1y + (double)(Mth.sin((float)((x % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 3.0D)) * 0.5F * f3);
            double dz = dc1z + (double)(Mth.sin((float)((y % 16.0D + (double)(dist * (1.0F - f2) * 2.0F) - (double)(time % 32767.0F / 5.0F)) / 2.0D)) * 0.5F * f3);

            float f13 = (1.0F - f2) * dist - time * speed;
            buffer.vertex(dx * (double)f2 - (double)width, dy * (double)f2, dz * (double)f2).uv(f13, f10).color(r, g, b, f3).endVertex();
            buffer.vertex(dx * (double)f2 + (double)width, dy * (double)f2, dz * (double)f2).uv(f13, f9).color(r, g, b, f3).endVertex();
        }

        tessellator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    public static void drawFloatyGUILine(double x, double y, double x2, double y2, float partialTicks, int color, String texture, float speed, float distance) {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(x2, y2, 0.0D);

        float time = (float)(System.nanoTime() / 30000000L);
        Color co = new Color(color);
        float r = (float)co.getRed() / 255.0F;
        float g = (float)co.getGreen() / 255.0F;
        float b = (float)co.getBlue() / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        double dc1x = (double)((float)(x - x2));
        double dc1y = (double)((float)(y - y2));
        bindTexture(texture);

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        double d3 = x - x2;
        double d4 = y - y2;
        float dist = Mth.sqrt((float)(d3 * d3 + d4 * d4));
        double dx = d3 / (double)dist;

        poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)(-(Math.atan2(d3, d4) * 180.0D / Math.PI)) + 90.0F));

        float blocks = (float)Math.round(dist);
        float length = blocks * distance;
        float f9 = 0.0F;
        float f10 = 1.0F;
        float sec = 1.0F / length;

        for(int i = 0; (float)i <= length; ++i) {
            float f2 = (float)i / length;
            float f13 = (1.0F - f2) * length;
            float f14 = (1.0F - f2) * length + sec;
            float width = 1.0F;
            buffer.vertex(dx * (double)i, 0.0F - width, 0.0D).uv(f13 / width, f10).color(r, g, b, 1.0F).endVertex();
            buffer.vertex(dx * (double)i, 0.0F + width, 0.0D).uv(f14 / width, f9).color(r, g, b, 1.0F).endVertex();
        }

        tessellator.end();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static int getGuiXSize(AbstractContainerScreen<?> gui) {
        return gui.getXSize();
    }

    public static int getGuiYSize(AbstractContainerScreen<?> gui) {
        return gui.getYSize();
    }
}