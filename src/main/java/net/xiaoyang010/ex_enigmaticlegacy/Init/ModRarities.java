package net.xiaoyang010.ex_enigmaticlegacy.Init;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class ModRarities {
    public static final Rarity MIRACLE = Rarity.create("miracle", ChatFormatting.DARK_RED);
    public static final Rarity HOLY    = Rarity.create("holy",    ChatFormatting.WHITE);
    public static final Rarity FALLEN  = Rarity.create("fallen",  ChatFormatting.WHITE);
    private static final WeakHashMap<String, ShardData[][]> SHATTER_CACHE = new WeakHashMap<>();
    private static final Random GLITCH_RAND = new Random();
    private static final int SHATTER_SEED = 0xDEAD1234;

    private static final long CYCLE    = 4500L;
    private static final long T_HOLD   =  900L;
    private static final long T_CRACK  =  600L;
    private static final long T_SCATTER= 1000L;
    private static final long T_FADE   =  500L;
    private static final long T_REFORM =  900L;

    public static final int MARK_WAVE_HOLY    = 0x010001;
    public static final int MARK_WAVE_FALLEN  = 0x010002;
    public static final int MARK_WAVE_MIRACLE = 0x010003;
    public static final int MARK_GLITCH       = 0x010004;
    public static final int MARK_TEAR         = 0x010005;
    public static final int MARK_DISSOLVE     = 0x010006;
    public static final int MARK_GLOW_STAR    = 0x010007;
    public static final int MARK_RAINBOW      = 0x010008;
    public static final int MARK_SHATTER      = 0x010009;

    public static void register() {}

    private static class ShardData {
        float[] lx = new float[3];
        float[] ly = new float[3];
        float cx, cy;
        float vx, vy;
        float rotSpeed;
        float phase;
        float area;
    }

    public static void drawWaveNameWithStyle(PoseStack poseStack, Font font, ItemStack stack, String rawText, IWaveName.WaveStyle style, int startX, int startY, int alpha) {
        switch (style) {
            case GLITCH   -> drawGlitchName  (poseStack, font, rawText, startX, startY, alpha);
            case TEAR     -> drawTearName    (poseStack, font, rawText, startX, startY, alpha);
            case DISSOLVE -> drawDissolveName(poseStack, font, stack, rawText, startX, startY, alpha);
            case GLOW_STAR-> drawGlowStarName(poseStack, font, rawText, startX, startY, alpha);
            case SHATTER  -> drawShatterName(poseStack, font, rawText, startX, startY, alpha);
            default       -> drawWaveLoop   (poseStack, font, rawText, style, startX, startY, alpha);
        }
    }

    private static final int[] HOLY_COLORS = {
            0xFFD700, // 金
            0xFFEC80, // 浅金
            0xFFFFC0, // 奶白
            0xFFFFFF, // 纯白
            0xE0FFFF, // 极浅蓝白
            0xAAF0FF, // 冰蓝
            0x80DFFF, // 天蓝
            0xAAF0FF, // 冰蓝
            0xE0FFFF, // 极浅蓝白
            0xFFFFFF, // 纯白
            0xFFFFC0, // 奶白
            0xFFEC80, // 浅金
    };

    private static final int[] FALLEN_COLORS = {
            0xFF2200, // 鲜红
            0xCC0000, // 血红
            0x880000, // 暗红
            0x550033, // 暗紫红
            0x440055, // 深紫
            0x6600AA, // 紫
            0x9900DD, // 亮紫
            0xCC00FF, // 紫红亮
            0x9900DD, // 亮紫
            0x6600AA, // 紫
            0x440055, // 深紫
            0x550033, // 暗紫红
            0x880000, // 暗红
            0xCC0000, // 血红
    };

    private static final int[] MIRACLE_COLORS = {
            0xFF0000, // 红
            0xFF7700, // 橙
            0xFFFF00, // 黄
            0x00FF00, // 绿
            0x00FFFF, // 青
            0x0077FF, // 蓝
            0x8800FF, // 紫
            0xFF00FF, // 品红
            0xFF0077, // 玫红
    };

    public static boolean shouldAnimate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IWaveName) return true;
        Rarity r = stack.getRarity();
        return r == HOLY || r == FALLEN || r == MIRACLE;
    }

    private static IWaveName.WaveStyle getStyle(ItemStack stack) {
        if (stack.getItem() instanceof IWaveName wni) {
            return wni.getWaveStyle(stack);
        }
        Rarity r = stack.getRarity();
        if (r == HOLY)    return IWaveName.WaveStyle.HOLY;
        if (r == FALLEN)  return IWaveName.WaveStyle.FALLEN;
        if (r == MIRACLE) return IWaveName.WaveStyle.MIRACLE;
        return IWaveName.WaveStyle.RAINBOW;
    }

    private static int lerpColor(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        return ((int)(r1 + (r2 - r1) * t) << 16)
                | ((int)(g1 + (g2 - g1) * t) << 8)
                |  (int)(b1 + (b2 - b1) * t);
    }

    private static int getAnimColor(int[] colors, double timeSec,
                                    int charIdx, float speed, float waveOffset) {
        double phase = (timeSec * speed - charIdx * waveOffset) % colors.length;
        if (phase < 0) phase += colors.length;
        int i0 = (int) phase;
        int i1 = (i0 + 1) % colors.length;
        return lerpColor(colors[i0], colors[i1], (float)(phase - i0));
    }

    public static void drawWaveName(PoseStack poseStack, Font font, ItemStack stack, int startX, int startY, int alpha) {
        String raw = ChatFormatting.stripFormatting(
                stack.getHoverName().getString());
        if (raw == null || raw.isEmpty()) return;

        IWaveName.WaveStyle style = getStyle(stack);

        switch (style) {
            case GLITCH    -> drawGlitchName  (poseStack, font, raw, startX, startY, alpha);
            case TEAR      -> drawTearName    (poseStack, font, raw, startX, startY, alpha);
            case DISSOLVE  -> drawDissolveName(poseStack, font, stack, raw, startX, startY, alpha);
            case GLOW_STAR -> drawGlowStarName(poseStack, font, raw, startX, startY, alpha);
            case SHATTER  -> drawShatterName(poseStack, font, raw, startX, startY, alpha);
            default        -> drawWaveLoop   (poseStack, font, raw, style,  startX, startY, alpha);
        }
    }

    private static void drawWaveLoop(PoseStack poseStack, Font font, String raw, IWaveName.WaveStyle style, int startX, int startY, int alpha) {
        long ms = System.currentTimeMillis();
        double timeSec = ms / 1000.0;
        int curX = startX;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') {
                curX += font.width(" ");
                continue;
            }
            double waveY = Math.sin(ms / 300.0 + i * 0.4) * 2.0;
            int rgb;
            switch (style) {
                case HOLY -> rgb = getAnimColor(HOLY_COLORS, timeSec, i, 1.5F, 0.35F);
                case FALLEN -> rgb = getAnimColor(FALLEN_COLORS, timeSec, i, 1.2F, 0.40F);
                case MIRACLE -> rgb = getAnimColor(MIRACLE_COLORS, timeSec, i, 2.0F, 0.30F);
                default -> {
                    float hue = (float) ((timeSec / 3.0 + i * 0.08) % 1.0);
                    rgb = Color.HSBtoRGB(hue, 0.85f, 1.0f) & 0x00FFFFFF;
                }
            }
            int argb = (alpha << 24) | (rgb & 0x00FFFFFF);
            poseStack.pushPose();
            poseStack.translate(curX, startY + waveY, 0.0);
            font.drawShadow(poseStack, String.valueOf(c), 0, 0, argb);
            poseStack.popPose();
            curX += font.width(String.valueOf(c));
        }
    }

    private static void drawGlitchName(PoseStack poseStack, Font font, String raw, int startX, int startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        Minecraft mc         = Minecraft.getInstance();
        double    guiScale   = mc.getWindow().getGuiScale();
        int       winH       = mc.getWindow().getHeight();
        int       charHeight = font.lineHeight;
        long      ms         = System.currentTimeMillis();
        long macroFrame = ms / 500;
        long microFrame = ms / 50;

        GLITCH_RAND.setSeed(macroFrame * 0xDEADBEEFL);
        boolean isBurst     = GLITCH_RAND.nextFloat() < 0.35f;
        float   burstPower  = isBurst
                ? 0.4f + GLITCH_RAND.nextFloat() * 0.6f
                : 0.15f;

        int totalWidth = font.width(raw);
        GLITCH_RAND.setSeed(microFrame * 0xCAFEBABEL);
        int sliceCount = isBurst
                ? 3 + GLITCH_RAND.nextInt(5)
                : 1 + GLITCH_RAND.nextInt(2);
        int[] sliceTopY    = new int[sliceCount];
        int[] sliceHeight  = new int[sliceCount];
        int[] sliceOffsetX = new int[sliceCount];

        for (int s = 0; s < sliceCount; s++) {
            sliceTopY[s]   = -2 + GLITCH_RAND.nextInt(charHeight + 4);
            sliceHeight[s] = 1 + GLITCH_RAND.nextInt(4);
            float maxOff   = isBurst ? 12f * burstPower : 3f;
            sliceOffsetX[s] = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * maxOff);
        }

        GLITCH_RAND.setSeed(microFrame * 0xFEEDFACEL);
        float chromaRange = isBurst ? 6f * burstPower : 1f;
        int rOffX = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange);
        int rOffY = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.4f);
        int gOffX = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.3f);
        int gOffY = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.2f);
        int bOffX = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange);
        int bOffY = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.4f);

        GLITCH_RAND.setSeed(microFrame * 0x1234ABCDL);
        boolean isFlash = isBurst && GLITCH_RAND.nextFloat() < 0.05f;
        GLITCH_RAND.setSeed(microFrame * 0x9E3779B9L);
        int globalJitterY = isBurst
                ? (int)((GLITCH_RAND.nextFloat() * 2 - 1) * 2 * burstPower)
                : 0;
        int drawY = startY + globalJitterY;
        {
            int baseColor = isFlash
                    ? (alpha << 24) | 0xFFFFFF
                    : (alpha << 24) | 0xDDDDDD;
            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ' ') { cx += font.width(" "); continue; }
                String ch = String.valueOf(c);
                poseStack.pushPose();
                poseStack.translate(cx, drawY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, baseColor);
                poseStack.popPose();
                cx += font.width(ch);
            }
        }

        if (!isFlash) {
            float chanAlpha = isBurst ? 0.60f : 0.25f;

            int[]   chanOffX  = { rOffX, gOffX, bOffX };
            int[]   chanOffY  = { rOffY, gOffY, bOffY };
            int[]   chanColor = { 0xFF0000, 0x00FF00, 0x0000FF };

            for (int ch = 0; ch < 3; ch++) {
                int cAlpha = (int)(alpha * chanAlpha);
                int cColor = (cAlpha << 24) | chanColor[ch];
                int cx     = startX;
                int sx = (int)((startX - chromaRange - 2) * guiScale);
                int sw = (int)((totalWidth + chromaRange * 2 + 4) * guiScale);
                int sh = (int)((charHeight + Math.abs(rOffY) + Math.abs(bOffY) + 2) * guiScale);
                int sy = (int)(winH - (drawY + charHeight + 2) * guiScale);

                RenderSystem.enableScissor(sx, sy, sw, sh);

                for (int i = 0; i < raw.length(); i++) {
                    char chr = raw.charAt(i);
                    if (chr == ' ') { cx += font.width(" "); continue; }
                    String s = String.valueOf(chr);
                    poseStack.pushPose();
                    poseStack.translate(cx + chanOffX[ch], drawY + chanOffY[ch], 0.0);
                    font.drawShadow(poseStack, s, 0, 0, cColor);
                    poseStack.popPose();
                    cx += font.width(s);
                }

                RenderSystem.disableScissor();
            }
        }

        for (int s = 0; s < sliceCount; s++) {
            int scTop    = drawY + sliceTopY[s];
            int scBottom = scTop + sliceHeight[s];
            int scLeft   = startX - Math.abs(sliceOffsetX[s]) - 2;
            int scRight  = startX + totalWidth + Math.abs(sliceOffsetX[s]) + 2;
            int physX = (int)(scLeft   * guiScale);
            int physW = (int)((scRight - scLeft) * guiScale);
            int physH = Math.max(1, (int)((scBottom - scTop) * guiScale));
            int physY = (int)(winH - scBottom * guiScale);

            if (physH <= 0 || physW <= 0) continue;

            RenderSystem.enableScissor(physX, physY, physW, physH);
            int sliceColor;
            if (isBurst) {
                GLITCH_RAND.setSeed(microFrame * 7919L + s * 0xABCDEFL);
                if (GLITCH_RAND.nextFloat() < 0.70f) {
                    int sAlpha = (int)(alpha * (0.7f + GLITCH_RAND.nextFloat() * 0.3f));
                    sliceColor = (sAlpha << 24) | 0xFFFFFF;
                } else {
                    int[] cyberColors = { 0x00FFFF, 0xFF00FF, 0xFFFF00, 0xFF0044, 0x00FF88 };
                    int   sAlpha      = (int)(alpha * 0.85f);
                    sliceColor = (sAlpha << 24) | cyberColors[GLITCH_RAND.nextInt(cyberColors.length)];
                }
            } else {
                int sAlpha = (int)(alpha * 0.40f);
                sliceColor = (sAlpha << 24) | 0xFFFFFF;
            }
            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char chr = raw.charAt(i);
                if (chr == ' ') { cx += font.width(" "); continue; }
                String cs = String.valueOf(chr);
                poseStack.pushPose();
                poseStack.translate(cx + sliceOffsetX[s], drawY, 0.0);
                font.drawShadow(poseStack, cs, 0, 0, sliceColor);
                poseStack.popPose();
                cx += font.width(cs);
            }

            RenderSystem.disableScissor();
        }

        if (isBurst) {
            for (int s = 0; s < sliceCount; s++) {
                if (Math.abs(sliceOffsetX[s]) < 2) continue;

                GLITCH_RAND.setSeed(microFrame * 31337L + s);
                if (GLITCH_RAND.nextFloat() > 0.65f) continue;

                int lineY    = drawY + sliceTopY[s];
                int glowAlph = (int)(alpha * 0.8f);
                int glowCol  = (glowAlph << 24) | 0x00FFFF;

                int physX = (int)(startX * guiScale);
                int physW = (int)(totalWidth * guiScale);
                int physH = Math.max(1, (int)(guiScale));
                int physY = (int)(winH - (lineY + 1) * guiScale);

                RenderSystem.enableScissor(physX, physY, physW, physH);
                int cx = startX;
                for (int i = 0; i < raw.length(); i++) {
                    char chr = raw.charAt(i);
                    if (chr == ' ') { cx += font.width(" "); continue; }
                    String cs = String.valueOf(chr);
                    poseStack.pushPose();
                    poseStack.translate(cx + sliceOffsetX[s], drawY, 0.0);
                    font.drawShadow(poseStack, cs, 0, 0, glowCol);
                    poseStack.popPose();
                    cx += font.width(cs);
                }
                RenderSystem.disableScissor();
            }
        }
    }

    private static void drawTearName(PoseStack poseStack, Font font, String raw, int startX, int startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        long ms    = System.currentTimeMillis();
        long frame = ms / 120;
        Minecraft mc = Minecraft.getInstance();
        double guiScale = mc.getWindow().getGuiScale();
        int charHeight = font.lineHeight;
        GLITCH_RAND.setSeed(frame * 0xDEADBEEFL);
        float tearIntensity = 0.3f + GLITCH_RAND.nextFloat() * 0.7f;
        GLITCH_RAND.setSeed(frame * 0xCAFEBABEL);
        int sliceCount = 1 + GLITCH_RAND.nextInt(3);
        int[] sliceY = new int[sliceCount];
        for (int s = 0; s < sliceCount; s++) {
            int base = (charHeight * (s + 1)) / (sliceCount + 1);
            sliceY[s] = Math.max(1, Math.min(charHeight - 1,
                    base + GLITCH_RAND.nextInt(3) - 1));
        }

        GLITCH_RAND.setSeed(frame * 0x1234ABCDL);
        int[] segmentShift = new int[sliceCount + 1];
        for (int s = 0; s <= sliceCount; s++) {
            segmentShift[s] = (int)((GLITCH_RAND.nextFloat() * 2 - 1)
                    * 8 * tearIntensity);
        }
        GLITCH_RAND.setSeed(frame * 0xFEEDFACEL);
        int chromaX = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * 4 * tearIntensity);
        int chromaY = (int)((GLITCH_RAND.nextFloat() * 2 - 1) * 2 * tearIntensity);

        GLITCH_RAND.setSeed(frame * 0x9E3779B9L);
        int scanShift = (GLITCH_RAND.nextFloat() < 0.20f) ? (int)(GLITCH_RAND.nextGaussian() * 4 * tearIntensity) : 0;
        int windowHeight = mc.getWindow().getHeight();
        int curX = startX;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') { curX += font.width(" ");
                continue;
            }

            String ch = String.valueOf(c);
            int charW = font.width(ch);
            GLITCH_RAND.setSeed(frame * 53L + i * 0x7654321L);
            if (GLITCH_RAND.nextFloat() < 0.08f * tearIntensity) {
                curX += charW;
                continue;
            }

            int selfJitterX = 0;
            if (GLITCH_RAND.nextFloat() < 0.20f) {
                selfJitterX = (int)(GLITCH_RAND.nextGaussian() * 1.5 * tearIntensity);
            }
            for (int seg = 0; seg <= sliceCount; seg++) {
                int segTop    = (seg == 0)           ? 0          : sliceY[seg - 1];
                int segBottom = (seg == sliceCount)  ? charHeight : sliceY[seg];
                if (segTop >= segBottom) continue;
                int shift = segmentShift[seg] + selfJitterX + scanShift;
                int segColor;
                if (seg == 0) {
                    segColor = (alpha << 24) | 0xFFEEEE;
                } else if (seg == sliceCount) {
                    int botAlpha = (int)(alpha * (0.80f + GLITCH_RAND.nextFloat() * 0.20f));
                    segColor = (botAlpha << 24) | 0xAABBFF;
                } else {
                    int midAlpha = (int)(alpha * (0.75f + GLITCH_RAND.nextFloat() * 0.25f));
                    segColor = (midAlpha << 24) | 0x88FFEE;
                }
                int scissorX = (int)((curX + shift) * guiScale);
                int scissorW = (int)((charW + 2) * guiScale);
                int scissorH = (int)((segBottom - segTop) * guiScale);
                int scissorY = (int)(windowHeight - (startY + segBottom) * guiScale);

                RenderSystem.enableScissor(scissorX - (int)guiScale, scissorY, scissorW + (int)(guiScale * 2), scissorH);
                poseStack.pushPose();
                poseStack.translate(curX + shift, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, segColor);
                poseStack.popPose();

                if (seg == 0) {
                    int topAlpha = (int)(alpha * 0.55f);
                    int topColor = (topAlpha << 24) | 0xFF4444;
                    poseStack.pushPose();
                    poseStack.translate(curX + shift - chromaX, startY - chromaY, 0.0);
                    font.drawShadow(poseStack, ch, 0, 0, topColor);
                    poseStack.popPose();
                }

                if (seg == sliceCount) {
                    int botAlpha = (int)(alpha * 0.55f);
                    int botColor = (botAlpha << 24) | 0x4466FF;
                    poseStack.pushPose();
                    poseStack.translate(curX + shift + chromaX, startY + chromaY, 0.0);
                    font.drawShadow(poseStack, ch, 0, 0, botColor);
                    poseStack.popPose();
                }

                RenderSystem.disableScissor();
            }

            for (int s = 0; s < sliceCount; s++) {
                GLITCH_RAND.setSeed(frame * 317L + i * 0x11223344L + s);
                if (GLITCH_RAND.nextFloat() < 0.80f) {
                    int glowShift = segmentShift[s] + selfJitterX + scanShift;
                    int glowAlpha = (int)(alpha * 0.7f);
                    int glowColor = (glowAlpha << 24) | 0x00FFFF;
                    int scissorX  = (int)(curX * guiScale);
                    int scissorW  = (int)((charW + 2) * guiScale);
                    int scissorH  = Math.max(1, (int)(1.5 * guiScale));
                    int scissorY  = (int)(windowHeight - (startY + sliceY[s]) * guiScale);

                    RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
                    poseStack.pushPose();
                    poseStack.translate(curX + glowShift, startY, 0.0);
                    font.drawShadow(poseStack, ch, 0, 0, glowColor);
                    poseStack.popPose();
                    RenderSystem.disableScissor();
                }
            }

            GLITCH_RAND.setSeed(frame * 199L + i * 0xABCDEFL);
            if (GLITCH_RAND.nextFloat() < 0.05f * tearIntensity) {
                int noiseY     = startY + GLITCH_RAND.nextInt(charHeight);
                int noiseShift = (int)(GLITCH_RAND.nextGaussian() * 6 * tearIntensity);
                int noiseColor = ((int)(alpha * 0.5f) << 24) | 0x00FFFF;
                int sx = (int)((curX + noiseShift) * guiScale);
                int sy = (int)(windowHeight - (noiseY + 1) * guiScale);
                int sw = (int)(charW * guiScale);
                int sh = Math.max(1, (int)(guiScale));

                RenderSystem.enableScissor(sx, sy, sw, sh);
                poseStack.pushPose();
                poseStack.translate(curX + noiseShift, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, noiseColor);
                poseStack.popPose();
                RenderSystem.disableScissor();
            }
            curX += charW;
        }
    }

    private static void drawDissolveName(PoseStack poseStack, Font font, ItemStack stack, String raw, int startX, int startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        Minecraft mc       = Minecraft.getInstance();
        double    guiScale = mc.getWindow().getGuiScale();
        int       winH     = mc.getWindow().getHeight();
        int       charH    = font.lineHeight;
        long      ms       = System.currentTimeMillis();
        int       totalW   = font.width(raw);
        double cycleT = (ms % 4000) / 4000.0;
        double dissolveT;
        if (cycleT < 0.5) {
            double t = cycleT * 2.0;
            dissolveT = t * t * (3 - 2 * t);
        } else {
            double t = (cycleT - 0.5) * 2.0;
            dissolveT = 1.0 - t * t * (3 - 2 * t);
        }

        int dissolveY = (int)(dissolveT * (charH + 4));
        final int columnWidth = 2;
        int columnCount = (totalW + columnWidth - 1) / columnWidth;
        int[] columnDrift = new int[columnCount];
        GLITCH_RAND.setSeed(0xDEADC0DEL + raw.hashCode());
        for (int col = 0; col < columnCount; col++) {
            columnDrift[col] = (int)(GLITCH_RAND.nextGaussian() * 1.8);
        }

        long microFrame = ms / 80;
        int[] columnStretch = new int[columnCount];
        for (int col = 0; col < columnCount; col++) {
            GLITCH_RAND.setSeed(microFrame * 0x9E3779B9L + col * 0x45D9F3BL);
            columnStretch[col] = (int)(2 + GLITCH_RAND.nextFloat() * 6 * dissolveT);
        }

        IWaveName.WaveStyle style = getStyle(stack);
        int baseRGB, glowRGB;
        switch (style) {
            case HOLY   -> { baseRGB = 0xAADDFF; glowRGB = 0xFFFFFF; }
            case FALLEN -> { baseRGB = 0xAA00FF; glowRGB = 0xFF44AA; }
            default     -> { baseRGB = 0x44FFAA; glowRGB = 0xAAFFEE; }
        }

        int normalTop    = startY + dissolveY;
        int normalBottom = startY + charH;

        if (normalTop < normalBottom) {
            int sx = (int)(startX    * guiScale);
            int sw = (int)(totalW    * guiScale);
            int sh = (int)((normalBottom - normalTop) * guiScale);
            int sy = (int)(winH - normalBottom * guiScale);

            RenderSystem.enableScissor(sx, sy, sw, Math.max(1, sh));
            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ' ') { cx += font.width(" "); continue; }
                String ch = String.valueOf(c);
                poseStack.pushPose();
                poseStack.translate(cx, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, (alpha << 24) | 0xFFFFFF);
                poseStack.popPose();
                cx += font.width(ch);
            }
            RenderSystem.disableScissor();
        }

        if (dissolveT < 0.02) return;
        for (int col = 0; col < columnCount; col++) {
            int colX = startX + col * columnWidth;
            if (colX >= startX + totalW) break;
            int colW = Math.min(columnWidth, startX + totalW - colX);
            int colDissolveY = dissolveY + columnDrift[col];
            colDissolveY = Math.max(0, Math.min(charH + 2, colDissolveY));
            if (colDissolveY <= 0) continue;

            int dropTop    = startY + colDissolveY - 1;
            int dropBottom = dropTop + columnStretch[col] + 1;

            if (dropTop >= dropBottom) continue;
            int physX = (int)(colX      * guiScale);
            int physW = Math.max(1, (int)(colW * guiScale));
            int physH = Math.max(1, (int)((dropBottom - dropTop) * guiScale));
            int physY = (int)(winH - dropBottom * guiScale);

            RenderSystem.enableScissor(physX, physY, physW, physH);

            int stretchLen = columnStretch[col] + 1;
            for (int dy = 0; dy < stretchLen; dy++) {
                float alphaRatio = 1.0f - (float)dy / stretchLen;
                alphaRatio = alphaRatio * alphaRatio;
                int dropAlpha = (int)(alpha * alphaRatio * 0.9f);

                if (dropAlpha <= 0) continue;
                int dropRGB   = lerpColor(baseRGB, glowRGB, alphaRatio);
                int dropColor = (dropAlpha << 24) | (dropRGB & 0x00FFFFFF);
                int sliceY     = dropTop + dy;
                int slicePhysH = Math.max(1, (int)(guiScale));
                int slicePhysY = (int)(winH - (sliceY + 1) * guiScale);

                RenderSystem.enableScissor(physX, slicePhysY, physW, slicePhysH);

                int cx = startX;
                for (int i = 0; i < raw.length(); i++) {
                    char c = raw.charAt(i);
                    if (c == ' ') { cx += font.width(" "); continue; }
                    int cw = font.width(String.valueOf(c));
                    if (cx + cw > colX && cx < colX + colW) {
                        poseStack.pushPose();
                        poseStack.translate(cx, startY, 0.0);
                        font.drawShadow(poseStack, String.valueOf(c), 0, 0, dropColor);
                        poseStack.popPose();
                    }
                    cx += cw;
                }
                RenderSystem.disableScissor();
            }
            RenderSystem.disableScissor();
        }

        if (dissolveY > 0 && dissolveY <= charH + 2) {
            int glowAlpha = (int)(alpha * Math.min(1.0, dissolveT * 1.5) * 0.9);
            int glowColor = (glowAlpha << 24) | (glowRGB & 0x00FFFFFF);

            int surfY     = startY + dissolveY;
            int sfPhysX   = (int)(startX * guiScale);
            int sfPhysW   = (int)(totalW  * guiScale);
            int sfPhysH   = Math.max(1, (int)(1.5 * guiScale));
            int sfPhysY   = (int)(winH - (surfY + 1) * guiScale);

            RenderSystem.enableScissor(sfPhysX, sfPhysY, sfPhysW, sfPhysH);

            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ' ') { cx += font.width(" "); continue; }
                String ch = String.valueOf(c);
                poseStack.pushPose();
                poseStack.translate(cx, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, glowColor);
                poseStack.popPose();
                cx += font.width(ch);
            }
            RenderSystem.disableScissor();

            for (int col = 0; col < columnCount; col++) {
                int colX = startX + col * columnWidth;
                if (colX >= startX + totalW) break;
                int colW = Math.min(columnWidth, startX + totalW - colX);

                int colSurfY = startY + dissolveY + columnDrift[col];

                GLITCH_RAND.setSeed(microFrame * 0xBEEFL + col * 137L);
                if (GLITCH_RAND.nextFloat() > 0.80f) continue;

                int ptAlpha = (int)(alpha * 0.95f * Math.min(1.0, dissolveT * 2));
                int ptColor = (ptAlpha << 24) | 0xFFFFFF;

                int pPhysX = (int)(colX * guiScale);
                int pPhysW = Math.max(1, (int)(colW * guiScale));
                int pPhysH = Math.max(1, (int)(guiScale));
                int pPhysY = (int)(winH - (colSurfY + 1) * guiScale);

                RenderSystem.enableScissor(pPhysX, pPhysY, pPhysW, pPhysH);
                int cx2 = startX;
                for (int i = 0; i < raw.length(); i++) {
                    char c2 = raw.charAt(i);
                    if (c2 == ' ') { cx2 += font.width(" "); continue; }
                    int cw = font.width(String.valueOf(c2));
                    if (cx2 + cw > colX && cx2 < colX + colW) {
                        poseStack.pushPose();
                        poseStack.translate(cx2, startY, 0.0);
                        font.drawShadow(poseStack, String.valueOf(c2), 0, 0, ptColor);
                        poseStack.popPose();
                    }
                    cx2 += cw;
                }
                RenderSystem.disableScissor();
            }
        }

        final int dropCount  = 6;
        final int dropLifeMs = 800;

        for (int d = 0; d < dropCount; d++) {

            if (dissolveT < 0.1) continue;
            long  dropOffset   = (long)(d * (dropLifeMs / (double)dropCount));
            long  dropAge      = (ms + dropOffset) % dropLifeMs;
            float dropProgress = (float)dropAge / dropLifeMs; // [0,1]

            GLITCH_RAND.setSeed(d * 0x12345678L + raw.hashCode());
            int dropColX = startX + GLITCH_RAND.nextInt(Math.max(1, totalW - 1));
            float maxFall = charH * 1.5f;
            int dropY = (startY + dissolveY) + (int)(dropProgress * maxFall);
            int dropH = Math.max(1, (int)(4 * (1.0f - dropProgress)));
            float dropAlphaRatio = (1.0f - dropProgress) * (1.0f - dropProgress);
            int   dropAlpha      = (int)(alpha * dropAlphaRatio * dissolveT);
            if (dropAlpha <= 0) continue;

            int dropRGB_  = lerpColor(baseRGB, glowRGB, 1.0f - dropProgress);
            int dropColor = (dropAlpha << 24) | (dropRGB_ & 0x00FFFFFF);

            int dPhysX = (int)(dropColX       * guiScale);
            int dPhysW = Math.max(1, (int)(columnWidth * guiScale));
            int dPhysH = Math.max(1, (int)(dropH       * guiScale));
            int dPhysY = (int)(winH - (dropY + dropH)  * guiScale);

            RenderSystem.enableScissor(dPhysX, dPhysY, dPhysW, dPhysH);

            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ' ') { cx += font.width(" "); continue; }
                int cw = font.width(String.valueOf(c));
                if (cx + cw > dropColX && cx < dropColX + columnWidth) {
                    poseStack.pushPose();
                    poseStack.translate(cx, startY + (dropY - startY), 0.0);
                    font.drawShadow(poseStack, String.valueOf(c), 0, 0, dropColor);
                    poseStack.popPose();
                }
                cx += cw;
            }
            RenderSystem.disableScissor();
        }
    }

    private static void drawGlowStarName(PoseStack poseStack, Font font, String raw, int startX, int startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        long ms = System.currentTimeMillis();
        float hueShift = (float)((ms / 5000.0) % 1.0);
        int   glowRGB  = Color.HSBtoRGB(hueShift, 0.7f, 1.0f) & 0x00FFFFFF;

        final int   glowLayers    = 2;
        final float glowSpread    = 0.8f;
        final float glowAlphaBase = 0.20f;

        int[][] dirs = {
                {0,-1}, {0,1}, {-1,0}, {1,0}
        };

        for (int layer = 1; layer <= glowLayers; layer++) {
            float offset = layer * glowSpread;
            int gAlpha = Math.max(0, Math.min(255, (int)(alpha * (glowAlphaBase / layer))));
            if (gAlpha < 4) continue;
            int gColor = (gAlpha << 24) | glowRGB;
            for (int[] dir : dirs) {
                float ox = dir[0] * offset;
                float oy = dir[1] * offset;

                int cx = startX;
                for (int i = 0; i < raw.length(); i++) {
                    char c = raw.charAt(i);
                    if (c == ' ') { cx += font.width(" "); continue; }
                    String ch = String.valueOf(c);
                    font.drawShadow(poseStack, ch, cx + ox, startY + oy, gColor);
                    cx += font.width(ch);
                }
            }
        }

        float pulse          = (float)(Math.sin(ms / 2000.0 * Math.PI) * 0.08 + 0.92);
        int   textBrightness = Math.min(255, (int)(255 * pulse));
        int   textColor      = (alpha << 24)
                | (textBrightness << 16)
                | (textBrightness << 8)
                |  textBrightness;
        {
            int cx = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ' ') { cx += font.width(" "); continue; }
                String ch = String.valueOf(c);
                font.drawShadow(poseStack, ch, cx, startY, textColor);
                cx += font.width(ch);
            }
        }

        int lastCharRight;
        {
            int tx   = startX;
            int last = startX;
            for (int i = 0; i < raw.length(); i++) {
                char c  = raw.charAt(i);
                int  cw = font.width(String.valueOf(c));
                if (c != ' ') last = tx + cw;
                tx += cw;
            }
            lastCharRight = last;
        }
        float starCX = lastCharRight + 3f;
        float starCY = startY - 3f;

        drawCrossStar(poseStack, font, starCX, starCY,
                5.5f,
                (float)((ms % 1500) / 1500.0 * Math.PI * 2.0),
                1.0f, glowRGB, alpha);
    }

    private static void drawCrossStar(PoseStack poseStack, Font font, float x, float y, float size, float rotation, float brightness, int color, int alpha) {
        if (alpha <= 0) return;

        long ms = System.currentTimeMillis();

        final long T_APPEAR  = 360L;
        final long CYCLE     = 2400L;
        final long T_SHOW    = 1200L;
        final long T_FADE    = 2040L;

        long phase = ms % CYCLE;
        float lifeBrightness;
        float lifeScale;

        if (phase < T_APPEAR) {
            float t = (float)phase / T_APPEAR;
            float c1 = 1.70158f;
            float c3 = c1 + 1f;
            lifeScale      = 1f + c3 * (float)Math.pow(t - 1f, 3f) + c1 * (float)Math.pow(t - 1f, 2f);
            lifeScale      = Math.max(0f, Math.min(1.2f, lifeScale));
            lifeBrightness = t;
        } else if (phase < T_SHOW) {
            float t = (float)(phase - T_APPEAR) / (T_SHOW - T_APPEAR);
            lifeScale      = 1.0f;
            lifeBrightness = 0.85f + (float)(Math.sin(t * Math.PI * 3) * 0.075 + 0.075);
        } else if (phase < T_FADE) {
            float t = (float)(phase - T_SHOW) / (T_FADE - T_SHOW);
            float eased    = t * t * t * t;
            lifeBrightness = 1.0f - eased;
            lifeScale      = 1.0f - eased * 0.3f;
        } else {
            return;
        }

        lifeBrightness = Math.max(0f, Math.min(1f, lifeBrightness));
        if (lifeBrightness < 0.02f) return;

        float finalBrightness = lifeBrightness;
        float finalSize       = size * lifeScale;
        float finalRotation;

        if (phase < T_APPEAR) {
            float t = (float)phase / T_APPEAR;
            finalRotation = rotation + (float)Math.PI * (1f - t);
        } else if (phase < T_SHOW) {
            float t = (float)(phase - T_APPEAR) / (T_SHOW - T_APPEAR);
            finalRotation = rotation + t * (float)(Math.PI * 2.0);
        } else {
            float t = (float)(phase - T_SHOW) / (T_FADE - T_SHOW);
            finalRotation = rotation + (float)(Math.PI * 2.0) + t * (float)Math.PI;
        }

        for (int arm = 0; arm < 8; arm++) {
            float armAngle = finalRotation + arm * (float)(Math.PI / 4.0);
            float armLen   = ((arm % 2 == 0) ? finalSize : finalSize * 0.5f);
            float cosA     = (float)Math.cos(armAngle);
            float sinA     = (float)Math.sin(armAngle);

            int steps = Math.max(1, (int)armLen);

            for (int step = 1; step <= steps; step++) {
                float t     = (float)step / steps;
                float fade  = (1.0f - t) * (1.0f - t) * finalBrightness;
                int   ptA   = Math.max(0, Math.min(255, (int)(alpha * fade)));
                if (ptA < 4) continue;

                int   ptRGB   = lerpColor(color, 0xFFFFFF, (1.0f - t) * 0.4f);
                int   ptColor = (ptA << 24) | (ptRGB & 0x00FFFFFF);
                float px      = x + cosA * armLen * t;
                float py      = y + sinA * armLen * t;

                font.drawShadow(poseStack, "•", px - 1f, py - 1f, ptColor);
            }
        }

        float coreBurst = 0f;
        if (phase < T_APPEAR) {
            float t = (float)phase / T_APPEAR;
            if (t > 0.6f) {
                float bt = (t - 0.6f) / 0.2f;
                coreBurst = bt > 1f ? (2f - bt) / 1f : bt;
                coreBurst = Math.max(0f, Math.min(1f, coreBurst));
            }
        }

        float  coreB     = Math.min(1f, finalBrightness + coreBurst * 0.8f);
        int    coreAlpha = Math.max(0, Math.min(255, (int)(alpha * coreB * 0.95f)));
        if (coreAlpha >= 4) {
            int coreRGB   = lerpColor(color, 0xFFFFFF, coreB * 0.8f);
            int coreColor = (coreAlpha << 24) | (coreRGB & 0x00FFFFFF);
            font.drawShadow(poseStack, "✦", x - 3f, y - 4f, coreColor);
        }
    }

    private static ShardData[][] buildShards(Font font, String raw) {
        ShardData[][] result = new ShardData[raw.length()][];
        Random rng = new Random(SHATTER_SEED + raw.hashCode());
        int charH = font.lineHeight;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') { result[i] = new ShardData[0]; continue; }
            float W = font.width(String.valueOf(c));
            float H = charH;
            List<float[]> pts = new ArrayList<>();
            pts.add(new float[]{0,   0  });
            pts.add(new float[]{W,   0  });
            pts.add(new float[]{W,   H  });
            pts.add(new float[]{0,   H  });
            int edgePts = 4 + rng.nextInt(4);
            for (int k = 0; k < edgePts; k++) {
                int edge = k % 4;
                switch (edge) {
                    case 0 -> pts.add(new float[]{ rng.nextFloat()*W, 0 });
                    case 1 -> pts.add(new float[]{ W, rng.nextFloat()*H });
                    case 2 -> pts.add(new float[]{ rng.nextFloat()*W, H });
                    default-> pts.add(new float[]{ 0, rng.nextFloat()*H });
                }
            }
            int innerPts = 5 + rng.nextInt(6);
            for (int k = 0; k < innerPts; k++) {
                float px = 0.5f + rng.nextFloat() * (W - 1f);
                float py = 0.5f + rng.nextFloat() * (H - 1f);
                pts.add(new float[]{ px, py });
            }
            List<int[]> triangles = bowyer_watson(pts);
            List<ShardData> shards = new ArrayList<>();
            float mcxAll = W / 2f, mcyAll = H / 2f;
            for (int[] tri : triangles) {
                float ax = pts.get(tri[0])[0], ay = pts.get(tri[0])[1];
                float bx = pts.get(tri[1])[0], by = pts.get(tri[1])[1];
                float cx2 = pts.get(tri[2])[0], cy2 = pts.get(tri[2])[1];
                float area = Math.abs((bx-ax)*(cy2-ay) - (cx2-ax)*(by-ay)) * 0.5f;
                if (area < 0.3f) continue;
                ShardData sd = new ShardData();
                sd.lx[0] = ax; sd.ly[0] = ay;
                sd.lx[1] = bx; sd.ly[1] = by;
                sd.lx[2] = cx2; sd.ly[2] = cy2;
                sd.area = area;
                sd.cx = (ax + bx + cx2) / 3f;
                sd.cy = (ay + by + cy2) / 3f;
                float dx = sd.cx - mcxAll;
                float dy = sd.cy - mcyAll;
                float len = (float) Math.sqrt(dx*dx + dy*dy);
                if (len < 0.5f) {
                    float angle = rng.nextFloat() * (float)(Math.PI * 2);
                    dx = (float)Math.cos(angle);
                    dy = (float)Math.sin(angle);
                } else {
                    dx /= len;
                    dy /= len;
                }
                float speedBase = 6f + rng.nextFloat() * 16f;
                float speedMod  = (float)Math.sqrt(20f / Math.max(1f, area));
                float speed     = speedBase * Math.min(2.5f, speedMod);
                float deviation = (rng.nextFloat() - 0.5f) * 1.2f;
                sd.vx = dx * speed + dy  * deviation;
                sd.vy = dy * speed - dx * deviation;
                sd.rotSpeed = (rng.nextFloat() - 0.5f) * 8f;
                sd.phase    = rng.nextFloat();
                shards.add(sd);
            }
            result[i] = shards.toArray(new ShardData[0]);
        }
        return result;
    }

    private static List<int[]> bowyer_watson(List<float[]> points) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (float[] p : points) {
            if (p[0] < minX) minX = p[0];
            if (p[1] < minY) minY = p[1];
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
        }

        float dx = maxX - minX, dy = maxY - minY;
        float delta = Math.max(dx, dy) * 10f;
        int n = points.size();

        points.add(new float[]{
                minX - delta,     minY - delta * 3
        });
        points.add(new float[]{
                minX + delta * 3, minY - delta
        });
        points.add(new float[]{
                minX - delta,     minY + delta * 3
        });

        List<int[]> triangulation = new ArrayList<>();
        triangulation.add(new int[]{
                n, n+1, n+2
        });

        for (int pi = 0; pi < n; pi++) {
            float px = points.get(pi)[0], py = points.get(pi)[1];
            List<int[]> badTris = new ArrayList<>();
            for (int[] tri : triangulation) {
                if (inCircumcircle(points, tri, px, py)) {
                    badTris.add(tri);
                }
            }

            List<int[]> polygon = new ArrayList<>();
            for (int[] tri : badTris) {
                int[][] edges = {
                        {tri[0], tri[1]}, {tri[1], tri[2]}, {tri[2], tri[0]}
                };

                for (int[] edge : edges) {
                    boolean shared = false;
                    for (int[] other : badTris) {
                        if (other == tri) continue;
                        if (edgeShared(edge, other)) { shared = true; break; }
                    }
                    if (!shared) polygon.add(edge);
                }
            }

            triangulation.removeAll(badTris);

            for (int[] edge : polygon) {
                triangulation.add(new int[]{ edge[0], edge[1], pi });
            }
        }
        triangulation.removeIf(tri ->
                tri[0] >= n || tri[1] >= n || tri[2] >= n);

        points.subList(n, points.size()).clear();
        return triangulation;
    }

    private static boolean inCircumcircle(List<float[]> pts, int[] tri, float px, float py) {
        float ax = pts.get(tri[0])[0] - px, ay = pts.get(tri[0])[1] - py;
        float bx = pts.get(tri[1])[0] - px, by = pts.get(tri[1])[1] - py;
        float cx = pts.get(tri[2])[0] - px, cy = pts.get(tri[2])[1] - py;

        float d = (ax*ax + ay*ay) * (bx*cy - by*cx)
                - (bx*bx + by*by) * (ax*cy - ay*cx)
                + (cx*cx + cy*cy) * (ax*by - ay*bx);
        return d > 0;
    }

    private static boolean edgeShared(int[] edge, int[] tri) {
        return (tri[0] == edge[0] && tri[1] == edge[1]) || (tri[0] == edge[1] && tri[1] == edge[0])
                || (tri[1] == edge[0] && tri[2] == edge[1]) || (tri[1] == edge[1] && tri[2] == edge[0])
                || (tri[2] == edge[0] && tri[0] == edge[1]) || (tri[2] == edge[1] && tri[0] == edge[0]);
    }

    public static void drawShatterName(PoseStack poseStack, Font font, String raw, int startX, int startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;
        String cacheKey = raw + "@" + font.lineHeight;
        ShardData[][] allShards = SHATTER_CACHE.computeIfAbsent(cacheKey, k -> buildShards(font, raw));

        long ms    = System.currentTimeMillis();
        long phase = ms % CYCLE;

        boolean intact   = true;
        boolean isReform = false;

        float crackT   = 0f;
        float scatterT = 0f;
        float globalAlphaScale = 1f;

        long e = phase;

        if (e < T_HOLD) {
            intact = true;

        } else if ((e -= T_HOLD) < T_CRACK) {
            intact = false;
            crackT = smoothstep((float) e / T_CRACK);
            globalAlphaScale = 1f;

        } else if ((e -= T_CRACK) < T_SCATTER) {
            intact = false;
            crackT   = 1f;
            scatterT = smoothstep((float) e / T_SCATTER);
            globalAlphaScale = 1f;

        } else if ((e -= T_SCATTER) < T_FADE) {
            intact = false;
            crackT   = 1f;
            scatterT = 1f;
            float fadeProgress = smoothstep((float) e / T_FADE);
            globalAlphaScale = 1f - fadeProgress;

        } else if ((e -= T_FADE) < T_REFORM) {
            intact   = false;
            isReform = true;
            float reformProgress = smoothstep((float) e / T_REFORM);
            scatterT = 1f - reformProgress;
            crackT   = 1f - reformProgress;
            globalAlphaScale = smoothstep(Math.min(1f, reformProgress / 0.25f));

        }

        int curX = startX;
        for (int ci = 0; ci < raw.length(); ci++) {
            char c     = raw.charAt(ci);
            int  charW = font.width(String.valueOf(c));
            if (c == ' ') { curX += charW; continue; }

            ShardData[] shards = (ci < allShards.length) ? allShards[ci] : null;
            if (shards == null || shards.length == 0) { curX += charW; continue; }

            if (intact) {
                drawIntactChar(poseStack, font, c, curX, startY, alpha, ms, ci);
            } else {
                int shardAlpha = Math.max(0, (int)(alpha * globalAlphaScale));
                if (shardAlpha < 3) { curX += charW; continue; }

                for (ShardData sd : shards) {
                    drawShardTriangle(poseStack, font, String.valueOf(c),
                            curX, startY, sd,
                            crackT, scatterT, shardAlpha, ms, ci, isReform);
                }
                if (!isReform && crackT > 0.05f && scatterT < 0.4f) {
                    drawCrackLines(poseStack, font, String.valueOf(c),
                            curX, startY, shards, crackT, shardAlpha, ms);
                }
            }
            curX += charW;
        }
    }

    private static void drawShardTriangle(PoseStack poseStack, Font font, String ch, int charX, int charY, ShardData sd, float crackT, float scatterT, int alpha, long ms, int charIdx, boolean isReform) {
        float crackDisp   = crackT   * crackT   * 2.5f;
        float scatterDisp = scatterT * scatterT * 22f;
        float sizeFactor  = (float)Math.sqrt(15f / Math.max(1f, sd.area));
        sizeFactor = Math.min(2.2f, sizeFactor);
        float disp  = (crackDisp + scatterDisp) * sizeFactor;
        float dispX = sd.vx * disp / 20f;
        float dispY = sd.vy * disp / 20f;
        dispY += scatterT * scatterT * 4f * sizeFactor;
        float rot = sd.rotSpeed * (crackT * 0.08f + scatterT * 0.9f) * sizeFactor;

        int shardColor;

        if (isReform) {
            float pulse = (float)(Math.sin(ms / 500.0 + sd.phase * Math.PI * 2) * 0.1 + 0.9);
            int rgb = lerpColor(0xCCEEFF, 0xFFFFFF, Math.min(1f, (1f - scatterT) * 1.2f));
            int r = clamp((int)(((rgb >> 16) & 0xFF) * pulse));
            int g = clamp((int)(((rgb >>  8) & 0xFF) * pulse));
            int b = clamp((int)( (rgb & 0xFF)        * pulse));
            shardColor = (alpha << 24) | (r << 16) | (g << 8) | b;

        } else if (scatterT > 0.35f) {
            float hue = (float)(((ms / 2500.0) + charIdx * 0.13 + sd.phase * 0.6) % 1.0);
            int   rgb = Color.HSBtoRGB(hue, 0.55f, 1.0f) & 0x00FFFFFF;
            shardColor = (alpha << 24) | rgb;

        } else if (crackT > 0f) {
            float t   = crackT + scatterT * 0.35f;
            int   rgb = lerpColor(0xCCEEFF, 0xFFFFFF, Math.min(1f, t * 1.2f));
            float pulse = (float)(Math.sin(ms / 500.0 + sd.phase * Math.PI * 2) * 0.1 + 0.9);
            int r = clamp((int)(((rgb >> 16) & 0xFF) * pulse));
            int g = clamp((int)(((rgb >>  8) & 0xFF) * pulse));
            int b = clamp((int)( (rgb & 0xFF)        * pulse));
            shardColor = (alpha << 24) | (r << 16) | (g << 8) | b;

        } else {
            shardColor = (alpha << 24) | 0xCCEEFF;
        }

        if ((shardColor >>> 24) < 3) return;

        float cosR = (float)Math.cos(rot);
        float sinR = (float)Math.sin(rot);
        float aabbMinX = Float.MAX_VALUE,  aabbMinY = Float.MAX_VALUE;
        float aabbMaxX = -Float.MAX_VALUE, aabbMaxY = -Float.MAX_VALUE;
        float[] wx = new float[3], wy = new float[3];

        for (int v = 0; v < 3; v++) {
            float lx = sd.lx[v] - sd.cx;
            float ly = sd.ly[v] - sd.cy;
            wx[v] = charX + sd.cx + lx * cosR - ly * sinR + dispX;
            wy[v] = charY + sd.cy + lx * sinR + ly * cosR + dispY;
            if (wx[v] < aabbMinX) aabbMinX = wx[v];
            if (wy[v] < aabbMinY) aabbMinY = wy[v];
            if (wx[v] > aabbMaxX) aabbMaxX = wx[v];
            if (wy[v] > aabbMaxY) aabbMaxY = wy[v];
        }

        Minecraft mc      = Minecraft.getInstance();
        double   guiScale = mc.getWindow().getGuiScale();
        int      winH     = mc.getWindow().getHeight();
        int sx = (int)Math.floor((aabbMinX - 0.5f) * guiScale);
        int sy = (int)Math.floor(winH - (aabbMaxY + 0.5f) * guiScale);
        int sw = Math.max(1, (int)Math.ceil((aabbMaxX - aabbMinX + 1f) * guiScale));
        int sh = Math.max(1, (int)Math.ceil((aabbMaxY - aabbMinY + 1f) * guiScale));

        RenderSystem.enableScissor(sx, sy, sw, sh);
        poseStack.pushPose();
        poseStack.translate(charX + sd.cx + dispX, charY + sd.cy + dispY, 0);
        if (Math.abs(rot) > 0.001f) {
            poseStack.mulPose(new Quaternion(Vector3f.ZP, (float)Math.toDegrees(rot), true));
        }
        poseStack.translate(-sd.cx, -sd.cy, 0);
        font.drawShadow(poseStack, ch, 0, 0, shardColor);
        poseStack.popPose();
        RenderSystem.disableScissor();

        if (!isReform && scatterT > 0.1f && scatterT < 0.9f) {
            drawShardTrail(poseStack, font, charX, charY, sd,
                    dispX, dispY, scatterT, alpha, ms);
        }
    }

    private static void drawIntactChar(PoseStack poseStack, Font font, char c, int x, int y, int alpha, long ms, int idx) {
        float breathe = (float)(Math.sin(ms / 1800.0 + idx * 0.5) * 0.5 + 0.5);
        int   rgb     = lerpColor(0x99DDFF, 0xEEF8FF, breathe);
        float jitterY = (float)(Math.sin(ms / 180.0 + idx * 1.3) * 0.35);
        int   argb    = (alpha << 24) | rgb;
        poseStack.pushPose();
        poseStack.translate(x, y + jitterY, 0);
        font.drawShadow(poseStack, String.valueOf(c), 0, 0, argb);
        poseStack.popPose();
    }

    private static void drawShardTrail(PoseStack poseStack, Font font, int charX, int charY, ShardData sd, float dispX, float dispY, float scatterT, int alpha, long ms) {
        int trailCount = 3;

        for (int t = 1; t <= trailCount; t++) {
            float lag     = t * 0.12f;
            float tScatter = Math.max(0f, scatterT - lag);
            float sizeFactor = (float)Math.sqrt(15f / Math.max(1f, sd.area));
            sizeFactor = Math.min(2.2f, sizeFactor);
            float tDispX = sd.vx * tScatter * tScatter * 22f * sizeFactor / 20f;
            float tDispY = sd.vy * tScatter * tScatter * 22f * sizeFactor / 20f + tScatter * tScatter * 4f * sizeFactor;
            float trailAlpha = (1f - (float)t / (trailCount + 1)) * 0.35f * scatterT;
            int   ta         = Math.max(0, Math.min(255, (int)(alpha * trailAlpha)));
            if (ta < 4) continue;
            float hue   = (float)(((ms / 3000.0) + sd.phase) % 1.0);
            int   rgb   = Color.HSBtoRGB(hue, 0.6f, 0.9f) & 0x00FFFFFF;
            int   color = (ta << 24) | rgb;
            float tx = charX + sd.cx + tDispX;
            float ty = charY + sd.cy + tDispY;

            poseStack.pushPose();
            poseStack.translate(tx - 1f, ty - 1f, 0);
            font.drawShadow(poseStack, "·", 0, 0, color);
            poseStack.popPose();
        }
    }

    private static void drawCrackLines(PoseStack poseStack, Font font, String ch, int charX, int charY, ShardData[] shards, float crackT, int alpha, long ms) {
        float glowT    = crackT * (1f - crackT * 0.3f);
        int   glowAlpha = (int)(alpha * glowT * 0.7f);
        if (glowAlpha < 5) return;

        for (ShardData sd : shards) {
            float pulse    = (float)(Math.sin(ms / 300.0 + sd.phase * Math.PI * 4) * 0.3 + 0.7);
            int   ptAlpha  = (int)(glowAlpha * pulse);
            if (ptAlpha < 4) continue;
            int ptColor = (ptAlpha << 24) | lerpColor(0x88CCFF, 0xFFFFFF, crackT);
            float micro = crackT * 0.8f;
            float px = charX + sd.cx + sd.vx * micro / 20f;
            float py = charY + sd.cy + sd.vy * micro / 20f;

            poseStack.pushPose();
            poseStack.translate(px - 1f, py - 1f, 0);
            font.drawShadow(poseStack, "✦", 0, 0, ptColor);
            poseStack.popPose();
        }
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3 - 2 * t);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}