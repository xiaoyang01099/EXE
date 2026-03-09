package net.xiaoyang010.ex_enigmaticlegacy.Compat.Jade;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mcp.mobius.waila.api.ui.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class WaveNameElement extends Element {
    private static final Random GLITCH_RAND = new Random();
    private static final WeakHashMap<String, ShardData[][]> SHATTER_CACHE = new WeakHashMap<>();
    private static final int SHATTER_SEED = 0xDEAD1234;

    private static final long CYCLE     = 4500L;
    private static final long T_HOLD    =  900L;
    private static final long T_CRACK   =  600L;
    private static final long T_SCATTER = 1000L;
    private static final long T_FADE    =  500L;
    private static final long T_REFORM  =  900L;

    private final ItemStack stack;
    private final IWaveName.WaveStyle style;
    private final String rawText;

    public WaveNameElement(ItemStack stack, IWaveName.WaveStyle style, String rawText) {
        this.stack = stack;
        this.style = style;
        this.rawText = rawText != null ? rawText : "";
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(font.width(rawText), font.lineHeight);
    }

    @Override
    public void render(PoseStack poseStack, float x, float y, float maxX, float maxY) {
        Font font = Minecraft.getInstance().font;
        int alpha = 255;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        switch (style) {
            case HOLY:
            case FALLEN:
            case MIRACLE:
            case RAINBOW:
            case GLOW_STAR:
                ModRarities.drawWaveNameWithStyle(poseStack, font, stack, rawText, style,
                        (int) x, (int) y, alpha);
                break;
            case GLITCH:
                drawGlitchInJade(poseStack, font, rawText, x, y, alpha);
                break;
            case TEAR:
                drawTearInJade(poseStack, font, rawText, x, y, alpha);
                break;
            case DISSOLVE:
                drawDissolveInJade(poseStack, font, rawText, x, y, alpha);
                break;
            case SHATTER:
                drawShatterInJade(poseStack, font, rawText, x, y, alpha);
                break;
            default:
                font.drawShadow(poseStack, rawText, x, y, 0xFFFFFFFF);
                break;
        }

        RenderSystem.disableBlend();
    }

    @Override
    public @Nullable Component getMessage() {
        return new TextComponent(rawText);
    }

    private static float[] localToScreen(PoseStack poseStack, float localX, float localY) {
        Matrix4f mat = poseStack.last().pose();
        Vector4f vec = new Vector4f(localX, localY, 0, 1);
        vec.transform(mat);
        return new float[]{vec.x(), vec.y()};
    }

    private static void enableScissorFromLocal(PoseStack poseStack,
                                               float left, float top,
                                               float right, float bottom) {
        Window window = Minecraft.getInstance().getWindow();
        double guiScale = window.getGuiScale();
        int winH = window.getHeight();

        float[] screenLT = localToScreen(poseStack, left, top);
        float[] screenRB = localToScreen(poseStack, right, bottom);

        float screenLeft   = Math.min(screenLT[0], screenRB[0]);
        float screenTop    = Math.min(screenLT[1], screenRB[1]);
        float screenRight  = Math.max(screenLT[0], screenRB[0]);
        float screenBottom = Math.max(screenLT[1], screenRB[1]);

        int physX = (int) (screenLeft * guiScale);
        int physW = (int) ((screenRight - screenLeft) * guiScale);
        int physH = (int) ((screenBottom - screenTop) * guiScale);
        int physY = (int) (winH - screenBottom * guiScale);

        if (physW <= 0 || physH <= 0) {
            RenderSystem.enableScissor(0, 0, 0, 0);
            return;
        }
        RenderSystem.enableScissor(physX, physY, physW, physH);
    }

    private static void drawChars(PoseStack poseStack, Font font, String raw, float startX, float startY, float offsetX, float offsetY, int color) {
        float cx = startX;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') { cx += font.width(" "); continue; }
            String ch = String.valueOf(c);
            poseStack.pushPose();
            poseStack.translate(cx + offsetX, startY + offsetY, 0.0);
            font.drawShadow(poseStack, ch, 0, 0, color);
            poseStack.popPose();
            cx += font.width(ch);
        }
    }

    private static int lerpColor(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        return ((int)(r1 + (r2 - r1) * t) << 16)
                | ((int)(g1 + (g2 - g1) * t) << 8)
                |  (int)(b1 + (b2 - b1) * t);
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3 - 2 * t);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static void drawGlitchInJade(PoseStack poseStack, Font font, String raw, float startX, float startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        int charHeight = font.lineHeight;
        long ms = System.currentTimeMillis();
        long macroFrame = ms / 500;
        long microFrame = ms / 50;

        GLITCH_RAND.setSeed(macroFrame * 0xDEADBEEFL);
        boolean isBurst = GLITCH_RAND.nextFloat() < 0.35f;
        float burstPower = isBurst ? 0.4f + GLITCH_RAND.nextFloat() * 0.6f : 0.15f;

        int totalWidth = font.width(raw);

        GLITCH_RAND.setSeed(microFrame * 0xCAFEBABEL);
        int sliceCount = isBurst ? 3 + GLITCH_RAND.nextInt(5) : 1 + GLITCH_RAND.nextInt(2);
        int[] sliceTopY = new int[sliceCount];
        int[] sliceHeight = new int[sliceCount];
        int[] sliceOffsetX = new int[sliceCount];

        for (int s = 0; s < sliceCount; s++) {
            sliceTopY[s] = -2 + GLITCH_RAND.nextInt(charHeight + 4);
            sliceHeight[s] = 1 + GLITCH_RAND.nextInt(4);
            float maxOff = isBurst ? 12f * burstPower : 3f;
            sliceOffsetX[s] = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * maxOff);
        }

        GLITCH_RAND.setSeed(microFrame * 0xFEEDFACEL);
        float chromaRange = isBurst ? 6f * burstPower : 1f;
        int rOffX = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange);
        int rOffY = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.4f);
        int gOffX = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.3f);
        int gOffY = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.2f);
        int bOffX = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange);
        int bOffY = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * chromaRange * 0.4f);

        GLITCH_RAND.setSeed(microFrame * 0x1234ABCDL);
        boolean isFlash = isBurst && GLITCH_RAND.nextFloat() < 0.05f;

        GLITCH_RAND.setSeed(microFrame * 0x9E3779B9L);
        int globalJitterY = isBurst
                ? (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * 2 * burstPower) : 0;

        float drawX = startX;
        float drawY = startY + globalJitterY;

        int baseColor = isFlash ? (alpha << 24) | 0xFFFFFF : (alpha << 24) | 0xDDDDDD;
        drawChars(poseStack, font, raw, drawX, drawY, 0, 0, baseColor);

        if (!isFlash) {
            float chanAlpha = isBurst ? 0.60f : 0.25f;
            int[] chanOffX = {rOffX, gOffX, bOffX};
            int[] chanOffY = {rOffY, gOffY, bOffY};
            int[] chanColor = {0xFF0000, 0x00FF00, 0x0000FF};

            for (int ch = 0; ch < 3; ch++) {
                int cColor = ((int) (alpha * chanAlpha) << 24) | chanColor[ch];
                enableScissorFromLocal(poseStack,
                        drawX - chromaRange - 2, drawY - 2,
                        drawX + totalWidth + chromaRange + 4, drawY + charHeight + 2);
                drawChars(poseStack, font, raw, drawX, drawY, chanOffX[ch], chanOffY[ch], cColor);
                RenderSystem.disableScissor();
            }
        }

        for (int s = 0; s < sliceCount; s++) {
            float scTop = drawY + sliceTopY[s];
            float scBottom = scTop + sliceHeight[s];

            enableScissorFromLocal(poseStack,
                    drawX - Math.abs(sliceOffsetX[s]) - 2, scTop,
                    drawX + totalWidth + Math.abs(sliceOffsetX[s]) + 2, scBottom);

            int sliceColor;
            if (isBurst) {
                GLITCH_RAND.setSeed(microFrame * 7919L + s * 0xABCDEFL);
                if (GLITCH_RAND.nextFloat() < 0.70f) {
                    sliceColor = ((int) (alpha * (0.7f + GLITCH_RAND.nextFloat() * 0.3f)) << 24) | 0xFFFFFF;
                } else {
                    int[] cyberColors = {0x00FFFF, 0xFF00FF, 0xFFFF00, 0xFF0044, 0x00FF88};
                    sliceColor = ((int) (alpha * 0.85f) << 24) | cyberColors[GLITCH_RAND.nextInt(cyberColors.length)];
                }
            } else {
                sliceColor = ((int) (alpha * 0.40f) << 24) | 0xFFFFFF;
            }
            drawChars(poseStack, font, raw, drawX, drawY, sliceOffsetX[s], 0, sliceColor);
            RenderSystem.disableScissor();
        }

        if (isBurst) {
            for (int s = 0; s < sliceCount; s++) {
                if (Math.abs(sliceOffsetX[s]) < 2) continue;
                GLITCH_RAND.setSeed(microFrame * 31337L + s);
                if (GLITCH_RAND.nextFloat() > 0.65f) continue;

                float lineY = drawY + sliceTopY[s];
                int glowCol = ((int) (alpha * 0.8f) << 24) | 0x00FFFF;
                enableScissorFromLocal(poseStack, drawX, lineY, drawX + totalWidth, lineY + 1);
                drawChars(poseStack, font, raw, drawX, drawY, sliceOffsetX[s], 0, glowCol);
                RenderSystem.disableScissor();
            }
        }
    }

    private static void drawTearInJade(PoseStack poseStack, Font font, String raw, float startX, float startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        long ms = System.currentTimeMillis();
        long frame = ms / 120;
        int charHeight = font.lineHeight;

        GLITCH_RAND.setSeed(frame * 0xDEADBEEFL);
        float tearIntensity = 0.3f + GLITCH_RAND.nextFloat() * 0.7f;

        GLITCH_RAND.setSeed(frame * 0xCAFEBABEL);
        int sliceCount = 1 + GLITCH_RAND.nextInt(3);
        int[] sliceY = new int[sliceCount];
        for (int s = 0; s < sliceCount; s++) {
            int base = (charHeight * (s + 1)) / (sliceCount + 1);
            sliceY[s] = Math.max(1, Math.min(charHeight - 1, base + GLITCH_RAND.nextInt(3) - 1));
        }

        GLITCH_RAND.setSeed(frame * 0x1234ABCDL);
        int[] segmentShift = new int[sliceCount + 1];
        for (int s = 0; s <= sliceCount; s++) {
            segmentShift[s] = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * 8 * tearIntensity);
        }

        GLITCH_RAND.setSeed(frame * 0xFEEDFACEL);
        int chromaX = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * 4 * tearIntensity);
        int chromaY = (int) ((GLITCH_RAND.nextFloat() * 2 - 1) * 2 * tearIntensity);

        GLITCH_RAND.setSeed(frame * 0x9E3779B9L);
        int scanShift = (GLITCH_RAND.nextFloat() < 0.20f)
                ? (int) (GLITCH_RAND.nextGaussian() * 4 * tearIntensity) : 0;

        float curX = startX;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') { curX += font.width(" "); continue; }
            String ch = String.valueOf(c);
            int charW = font.width(ch);

            GLITCH_RAND.setSeed(frame * 53L + i * 0x7654321L);
            if (GLITCH_RAND.nextFloat() < 0.08f * tearIntensity) {
                curX += charW;
                continue;
            }

            int selfJitterX = 0;
            if (GLITCH_RAND.nextFloat() < 0.20f) {
                selfJitterX = (int) (GLITCH_RAND.nextGaussian() * 1.5 * tearIntensity);
            }

            for (int seg = 0; seg <= sliceCount; seg++) {
                int segTop    = (seg == 0) ? 0 : sliceY[seg - 1];
                int segBottom = (seg == sliceCount) ? charHeight : sliceY[seg];
                if (segTop >= segBottom) continue;

                int shift = segmentShift[seg] + selfJitterX + scanShift;

                int segColor;
                if (seg == 0) {
                    segColor = (alpha << 24) | 0xFFEEEE;
                } else if (seg == sliceCount) {
                    int botAlpha = (int) (alpha * (0.80f + GLITCH_RAND.nextFloat() * 0.20f));
                    segColor = (botAlpha << 24) | 0xAABBFF;
                } else {
                    int midAlpha = (int) (alpha * (0.75f + GLITCH_RAND.nextFloat() * 0.25f));
                    segColor = (midAlpha << 24) | 0x88FFEE;
                }

                enableScissorFromLocal(poseStack,
                        curX + shift - 1, startY + segTop,
                        curX + shift + charW + 1, startY + segBottom);

                poseStack.pushPose();
                poseStack.translate(curX + shift, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, segColor);
                poseStack.popPose();

                if (seg == 0) {
                    int topColor = ((int) (alpha * 0.55f) << 24) | 0xFF4444;
                    poseStack.pushPose();
                    poseStack.translate(curX + shift - chromaX, startY - chromaY, 0.0);
                    font.drawShadow(poseStack, ch, 0, 0, topColor);
                    poseStack.popPose();
                }

                if (seg == sliceCount) {
                    int botColor = ((int) (alpha * 0.55f) << 24) | 0x4466FF;
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
                    int glowColor = ((int) (alpha * 0.7f) << 24) | 0x00FFFF;

                    enableScissorFromLocal(poseStack,
                            curX, startY + sliceY[s] - 0.5f,
                            curX + charW + 2, startY + sliceY[s] + 1.0f);

                    poseStack.pushPose();
                    poseStack.translate(curX + glowShift, startY, 0.0);
                    font.drawShadow(poseStack, ch, 0, 0, glowColor);
                    poseStack.popPose();
                    RenderSystem.disableScissor();
                }
            }

            GLITCH_RAND.setSeed(frame * 199L + i * 0xABCDEFL);
            if (GLITCH_RAND.nextFloat() < 0.05f * tearIntensity) {
                float noiseY = startY + GLITCH_RAND.nextInt(charHeight);
                int noiseShift = (int) (GLITCH_RAND.nextGaussian() * 6 * tearIntensity);
                int noiseColor = ((int) (alpha * 0.5f) << 24) | 0x00FFFF;

                enableScissorFromLocal(poseStack,
                        curX + noiseShift, noiseY,
                        curX + noiseShift + charW, noiseY + 1);

                poseStack.pushPose();
                poseStack.translate(curX + noiseShift, startY, 0.0);
                font.drawShadow(poseStack, ch, 0, 0, noiseColor);
                poseStack.popPose();
                RenderSystem.disableScissor();
            }

            curX += charW;
        }
    }

    private static IWaveName.WaveStyle getStyle(ItemStack stack) {
        if (stack.getItem() instanceof IWaveName wni) {
            return wni.getWaveStyle(stack);
        }
        net.minecraft.world.item.Rarity r = stack.getRarity();
        if (r == ModRarities.HOLY)    return IWaveName.WaveStyle.HOLY;
        if (r == ModRarities.FALLEN)  return IWaveName.WaveStyle.FALLEN;
        if (r == ModRarities.MIRACLE) return IWaveName.WaveStyle.MIRACLE;
        return IWaveName.WaveStyle.RAINBOW;
    }

    private void drawDissolveInJade(PoseStack poseStack, Font font, String raw, float startX, float startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;

        int charH = font.lineHeight;
        long ms = System.currentTimeMillis();
        int totalW = font.width(raw);

        double cycleT = (ms % 4000) / 4000.0;
        double dissolveT;
        if (cycleT < 0.5) {
            double t = cycleT * 2.0;
            dissolveT = t * t * (3 - 2 * t);
        } else {
            double t = (cycleT - 0.5) * 2.0;
            dissolveT = 1.0 - t * t * (3 - 2 * t);
        }

        int dissolveY = (int) (dissolveT * (charH + 4));

        final int columnWidth = 2;
        int columnCount = (totalW + columnWidth - 1) / columnWidth;
        int[] columnDrift = new int[columnCount];
        GLITCH_RAND.setSeed(0xDEADC0DEL + raw.hashCode());
        for (int col = 0; col < columnCount; col++) {
            columnDrift[col] = (int) (GLITCH_RAND.nextGaussian() * 1.8);
        }

        long microFrame = ms / 80;
        int[] columnStretch = new int[columnCount];
        for (int col = 0; col < columnCount; col++) {
            GLITCH_RAND.setSeed(microFrame * 0x9E3779B9L + col * 0x45D9F3BL);
            columnStretch[col] = (int) (2 + GLITCH_RAND.nextFloat() * 6 * dissolveT);
        }

        IWaveName.WaveStyle wStyle = getStyle(stack);
        int baseRGB, glowRGB;
        switch (wStyle) {
            case HOLY   -> { baseRGB = 0xAADDFF; glowRGB = 0xFFFFFF; }
            case FALLEN -> { baseRGB = 0xAA00FF; glowRGB = 0xFF44AA; }
            default     -> { baseRGB = 0x44FFAA; glowRGB = 0xAAFFEE; }
        }

        float normalTop = startY + dissolveY;
        float normalBottom = startY + charH;
        if (normalTop < normalBottom) {
            enableScissorFromLocal(poseStack, startX, normalTop, startX + totalW, normalBottom);
            float cx = startX;
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
            float colX = startX + col * columnWidth;
            if (colX >= startX + totalW) break;
            int colW = Math.min(columnWidth, (int)(startX + totalW - colX));
            int colDissolveY = dissolveY + columnDrift[col];
            colDissolveY = Math.max(0, Math.min(charH + 2, colDissolveY));
            if (colDissolveY <= 0) continue;

            float dropTop = startY + colDissolveY - 1;
            float dropBottom = dropTop + columnStretch[col] + 1;
            if (dropTop >= dropBottom) continue;

            int stretchLen = columnStretch[col] + 1;
            for (int dy = 0; dy < stretchLen; dy++) {
                float alphaRatio = 1.0f - (float) dy / stretchLen;
                alphaRatio = alphaRatio * alphaRatio;
                int dropAlpha = (int) (alpha * alphaRatio * 0.9f);
                if (dropAlpha <= 0) continue;

                int dropRGB = lerpColor(baseRGB, glowRGB, alphaRatio);
                int dropColor = (dropAlpha << 24) | (dropRGB & 0x00FFFFFF);

                float sliceYtop = dropTop + dy;
                float sliceYbot = sliceYtop + 1;

                enableScissorFromLocal(poseStack, colX, sliceYtop, colX + colW, sliceYbot);

                float cx = startX;
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
        }

        if (dissolveY > 0 && dissolveY <= charH + 2) {
            int glowAlpha = (int) (alpha * Math.min(1.0, dissolveT * 1.5) * 0.9);
            int glowColor = (glowAlpha << 24) | (glowRGB & 0x00FFFFFF);

            float surfY = startY + dissolveY;
            enableScissorFromLocal(poseStack, startX, surfY, startX + totalW, surfY + 1.5f);

            float cx = startX;
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
                float colX = startX + col * columnWidth;
                if (colX >= startX + totalW) break;
                int colW = Math.min(columnWidth, (int)(startX + totalW - colX));
                float colSurfY = startY + dissolveY + columnDrift[col];

                GLITCH_RAND.setSeed(microFrame * 0xBEEFL + col * 137L);
                if (GLITCH_RAND.nextFloat() > 0.80f) continue;

                int ptAlpha = (int) (alpha * 0.95f * Math.min(1.0, dissolveT * 2));
                int ptColor = (ptAlpha << 24) | 0xFFFFFF;

                enableScissorFromLocal(poseStack, colX, colSurfY, colX + colW, colSurfY + 1);

                float cx2 = startX;
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

        final int dropCount = 6;
        final int dropLifeMs = 800;
        for (int d = 0; d < dropCount; d++) {
            if (dissolveT < 0.1) continue;
            long dropOffset = (long) (d * (dropLifeMs / (double) dropCount));
            long dropAge = (ms + dropOffset) % dropLifeMs;
            float dropProgress = (float) dropAge / dropLifeMs;

            GLITCH_RAND.setSeed(d * 0x12345678L + raw.hashCode());
            int dropColX = (int)(startX + GLITCH_RAND.nextInt(Math.max(1, totalW - 1)));
            float maxFall = charH * 1.5f;
            float dropY = (startY + dissolveY) + dropProgress * maxFall;
            int dropH = Math.max(1, (int) (4 * (1.0f - dropProgress)));
            float dropAlphaRatio = (1.0f - dropProgress) * (1.0f - dropProgress);
            int dropAlpha = (int) (alpha * dropAlphaRatio * dissolveT);
            if (dropAlpha <= 0) continue;

            int dropRGB_ = lerpColor(baseRGB, glowRGB, 1.0f - dropProgress);
            int dropColor = (dropAlpha << 24) | (dropRGB_ & 0x00FFFFFF);

            enableScissorFromLocal(poseStack,
                    dropColX, dropY,
                    dropColX + columnWidth, dropY + dropH);

            float cx = startX;
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

    private static class ShardData {
        float[] lx = new float[3];
        float[] ly = new float[3];
        float cx, cy;
        float vx, vy;
        float rotSpeed;
        float phase;
        float area;
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
            pts.add(new float[]{0, 0});
            pts.add(new float[]{W, 0});
            pts.add(new float[]{W, H});
            pts.add(new float[]{0, H});
            int edgePts = 4 + rng.nextInt(4);
            for (int k = 0; k < edgePts; k++) {
                int edge = k % 4;
                switch (edge) {
                    case 0 -> pts.add(new float[]{rng.nextFloat() * W, 0});
                    case 1 -> pts.add(new float[]{W, rng.nextFloat() * H});
                    case 2 -> pts.add(new float[]{rng.nextFloat() * W, H});
                    default -> pts.add(new float[]{0, rng.nextFloat() * H});
                }
            }
            int innerPts = 5 + rng.nextInt(6);
            for (int k = 0; k < innerPts; k++) {
                float px = 0.5f + rng.nextFloat() * (W - 1f);
                float py = 0.5f + rng.nextFloat() * (H - 1f);
                pts.add(new float[]{px, py});
            }
            List<int[]> triangles = bowyer_watson(pts);
            List<ShardData> shards = new ArrayList<>();
            float mcxAll = W / 2f, mcyAll = H / 2f;
            for (int[] tri : triangles) {
                float ax = pts.get(tri[0])[0], ay = pts.get(tri[0])[1];
                float bx = pts.get(tri[1])[0], by = pts.get(tri[1])[1];
                float cx2 = pts.get(tri[2])[0], cy2 = pts.get(tri[2])[1];
                float area = Math.abs((bx - ax) * (cy2 - ay) - (cx2 - ax) * (by - ay)) * 0.5f;
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
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len < 0.5f) {
                    float angle = rng.nextFloat() * (float) (Math.PI * 2);
                    dx = (float) Math.cos(angle);
                    dy = (float) Math.sin(angle);
                } else { dx /= len; dy /= len; }
                float speedBase = 6f + rng.nextFloat() * 16f;
                float speedMod = (float) Math.sqrt(20f / Math.max(1f, area));
                float speed = speedBase * Math.min(2.5f, speedMod);
                float deviation = (rng.nextFloat() - 0.5f) * 1.2f;
                sd.vx = dx * speed + dy * deviation;
                sd.vy = dy * speed - dx * deviation;
                sd.rotSpeed = (rng.nextFloat() - 0.5f) * 8f;
                sd.phase = rng.nextFloat();
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
            if (p[0] < minX) minX = p[0]; if (p[1] < minY) minY = p[1];
            if (p[0] > maxX) maxX = p[0]; if (p[1] > maxY) maxY = p[1];
        }
        float dx = maxX - minX, dy = maxY - minY;
        float delta = Math.max(dx, dy) * 10f;
        int n = points.size();
        points.add(new float[]{minX - delta, minY - delta * 3});
        points.add(new float[]{minX + delta * 3, minY - delta});
        points.add(new float[]{minX - delta, minY + delta * 3});
        List<int[]> triangulation = new ArrayList<>();
        triangulation.add(new int[]{n, n + 1, n + 2});
        for (int pi = 0; pi < n; pi++) {
            float px = points.get(pi)[0], py = points.get(pi)[1];
            List<int[]> badTris = new ArrayList<>();
            for (int[] tri : triangulation) {
                if (inCircumcircle(points, tri, px, py)) badTris.add(tri);
            }
            List<int[]> polygon = new ArrayList<>();
            for (int[] tri : badTris) {
                int[][] edges = {{tri[0], tri[1]}, {tri[1], tri[2]}, {tri[2], tri[0]}};
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
            for (int[] edge : polygon) triangulation.add(new int[]{edge[0], edge[1], pi});
        }
        triangulation.removeIf(tri -> tri[0] >= n || tri[1] >= n || tri[2] >= n);
        points.subList(n, points.size()).clear();
        return triangulation;
    }

    private static boolean inCircumcircle(List<float[]> pts, int[] tri, float px, float py) {
        float ax = pts.get(tri[0])[0] - px, ay = pts.get(tri[0])[1] - py;
        float bx = pts.get(tri[1])[0] - px, by = pts.get(tri[1])[1] - py;
        float cx = pts.get(tri[2])[0] - px, cy = pts.get(tri[2])[1] - py;
        float d = (ax * ax + ay * ay) * (bx * cy - by * cx)
                - (bx * bx + by * by) * (ax * cy - ay * cx)
                + (cx * cx + cy * cy) * (ax * by - ay * bx);
        return d > 0;
    }

    private static boolean edgeShared(int[] edge, int[] tri) {
        return (tri[0] == edge[0] && tri[1] == edge[1]) || (tri[0] == edge[1] && tri[1] == edge[0])
                || (tri[1] == edge[0] && tri[2] == edge[1]) || (tri[1] == edge[1] && tri[2] == edge[0])
                || (tri[2] == edge[0] && tri[0] == edge[1]) || (tri[2] == edge[1] && tri[0] == edge[0]);
    }

    private static void drawShatterInJade(PoseStack poseStack, Font font, String raw, float startX, float startY, int alpha) {
        if (raw == null || raw.isEmpty()) return;
        String cacheKey = raw + "@" + font.lineHeight;
        ShardData[][] allShards = SHATTER_CACHE.computeIfAbsent(cacheKey, k -> buildShards(font, raw));

        long ms = System.currentTimeMillis();
        long phase = ms % CYCLE;

        boolean intact = true;
        boolean isReform = false;
        float crackT = 0f;
        float scatterT = 0f;
        float globalAlphaScale = 1f;

        long e = phase;
        if (e < T_HOLD) {
            intact = true;
        } else if ((e -= T_HOLD) < T_CRACK) {
            intact = false;
            crackT = smoothstep((float) e / T_CRACK);
        } else if ((e -= T_CRACK) < T_SCATTER) {
            intact = false;
            crackT = 1f;
            scatterT = smoothstep((float) e / T_SCATTER);
        } else if ((e -= T_SCATTER) < T_FADE) {
            intact = false;
            crackT = 1f;
            scatterT = 1f;
            globalAlphaScale = 1f - smoothstep((float) e / T_FADE);
        } else if ((e -= T_FADE) < T_REFORM) {
            intact = false;
            isReform = true;
            float reformProgress = smoothstep((float) e / T_REFORM);
            scatterT = 1f - reformProgress;
            crackT = 1f - reformProgress;
            globalAlphaScale = smoothstep(Math.min(1f, reformProgress / 0.25f));
        }

        float curX = startX;
        for (int ci = 0; ci < raw.length(); ci++) {
            char c = raw.charAt(ci);
            int charW = font.width(String.valueOf(c));
            if (c == ' ') { curX += charW; continue; }

            ShardData[] shards = (ci < allShards.length) ? allShards[ci] : null;
            if (shards == null || shards.length == 0) { curX += charW; continue; }

            if (intact) {
                drawIntactChar(poseStack, font, c, curX, startY, alpha, ms, ci);
            } else {
                int shardAlpha = Math.max(0, (int) (alpha * globalAlphaScale));
                if (shardAlpha < 3) { curX += charW; continue; }

                for (ShardData sd : shards) {
                    drawShardTriangleJade(poseStack, font, String.valueOf(c),
                            curX, startY, sd, crackT, scatterT, shardAlpha, ms, ci, isReform);
                }
                if (!isReform && crackT > 0.05f && scatterT < 0.4f) {
                    drawCrackLinesJade(poseStack, font, String.valueOf(c),
                            curX, startY, shards, crackT, shardAlpha, ms);
                }
            }
            curX += charW;
        }
    }

    private static void drawIntactChar(PoseStack poseStack, Font font, char c, float x, float y, int alpha, long ms, int idx) {
        float breathe = (float) (Math.sin(ms / 1800.0 + idx * 0.5) * 0.5 + 0.5);
        int rgb = lerpColor(0x99DDFF, 0xEEF8FF, breathe);
        float jitterY = (float) (Math.sin(ms / 180.0 + idx * 1.3) * 0.35);
        int argb = (alpha << 24) | rgb;
        poseStack.pushPose();
        poseStack.translate(x, y + jitterY, 0);
        font.drawShadow(poseStack, String.valueOf(c), 0, 0, argb);
        poseStack.popPose();
    }

    private static void drawShardTriangleJade(PoseStack poseStack, Font font, String ch, float charX, float charY, ShardData sd, float crackT, float scatterT, int alpha, long ms, int charIdx, boolean isReform) {
        float crackDisp = crackT * crackT * 2.5f;
        float scatterDisp = scatterT * scatterT * 22f;
        float sizeFactor = (float) Math.sqrt(15f / Math.max(1f, sd.area));
        sizeFactor = Math.min(2.2f, sizeFactor);
        float disp = (crackDisp + scatterDisp) * sizeFactor;
        float dispX = sd.vx * disp / 20f;
        float dispY = sd.vy * disp / 20f;
        dispY += scatterT * scatterT * 4f * sizeFactor;
        float rot = sd.rotSpeed * (crackT * 0.08f + scatterT * 0.9f) * sizeFactor;

        int shardColor;
        if (isReform) {
            float pulse = (float) (Math.sin(ms / 500.0 + sd.phase * Math.PI * 2) * 0.1 + 0.9);
            int rgb = lerpColor(0xCCEEFF, 0xFFFFFF, Math.min(1f, (1f - scatterT) * 1.2f));
            int r = clamp((int) (((rgb >> 16) & 0xFF) * pulse));
            int g = clamp((int) (((rgb >> 8) & 0xFF) * pulse));
            int b = clamp((int) ((rgb & 0xFF) * pulse));
            shardColor = (alpha << 24) | (r << 16) | (g << 8) | b;
        } else if (scatterT > 0.35f) {
            float hue = (float) (((ms / 2500.0) + charIdx * 0.13 + sd.phase * 0.6) % 1.0);
            int rgb = Color.HSBtoRGB(hue, 0.55f, 1.0f) & 0x00FFFFFF;
            shardColor = (alpha << 24) | rgb;
        } else if (crackT > 0f) {
            float t = crackT + scatterT * 0.35f;
            int rgb = lerpColor(0xCCEEFF, 0xFFFFFF, Math.min(1f, t * 1.2f));
            float pulse = (float) (Math.sin(ms / 500.0 + sd.phase * Math.PI * 2) * 0.1 + 0.9);
            int r = clamp((int) (((rgb >> 16) & 0xFF) * pulse));
            int g = clamp((int) (((rgb >> 8) & 0xFF) * pulse));
            int b = clamp((int) ((rgb & 0xFF) * pulse));
            shardColor = (alpha << 24) | (r << 16) | (g << 8) | b;
        } else {
            shardColor = (alpha << 24) | 0xCCEEFF;
        }

        if ((shardColor >>> 24) < 3) return;

        float cosR = (float) Math.cos(rot);
        float sinR = (float) Math.sin(rot);
        float aabbMinX = Float.MAX_VALUE, aabbMinY = Float.MAX_VALUE;
        float aabbMaxX = -Float.MAX_VALUE, aabbMaxY = -Float.MAX_VALUE;

        for (int v = 0; v < 3; v++) {
            float lx = sd.lx[v] - sd.cx;
            float ly = sd.ly[v] - sd.cy;
            float wx = charX + sd.cx + lx * cosR - ly * sinR + dispX;
            float wy = charY + sd.cy + lx * sinR + ly * cosR + dispY;
            if (wx < aabbMinX) aabbMinX = wx;
            if (wy < aabbMinY) aabbMinY = wy;
            if (wx > aabbMaxX) aabbMaxX = wx;
            if (wy > aabbMaxY) aabbMaxY = wy;
        }

        enableScissorFromLocal(poseStack,
                aabbMinX - 0.5f, aabbMinY - 0.5f,
                aabbMaxX + 0.5f, aabbMaxY + 0.5f);

        poseStack.pushPose();
        poseStack.translate(charX + sd.cx + dispX, charY + sd.cy + dispY, 0);
        if (Math.abs(rot) > 0.001f) {
            poseStack.mulPose(new Quaternion(Vector3f.ZP, (float) Math.toDegrees(rot), true));
        }
        poseStack.translate(-sd.cx, -sd.cy, 0);
        font.drawShadow(poseStack, ch, 0, 0, shardColor);
        poseStack.popPose();
        RenderSystem.disableScissor();

        if (!isReform && scatterT > 0.1f && scatterT < 0.9f) {
            drawShardTrailJade(poseStack, font, charX, charY, sd,
                    scatterT, alpha, ms);
        }
    }

    private static void drawShardTrailJade(PoseStack poseStack, Font font, float charX, float charY, ShardData sd, float scatterT, int alpha, long ms) {
        int trailCount = 3;
        for (int t = 1; t <= trailCount; t++) {
            float lag = t * 0.12f;
            float tScatter = Math.max(0f, scatterT - lag);
            float sizeFactor = (float) Math.sqrt(15f / Math.max(1f, sd.area));
            sizeFactor = Math.min(2.2f, sizeFactor);
            float tDispX = sd.vx * tScatter * tScatter * 22f * sizeFactor / 20f;
            float tDispY = sd.vy * tScatter * tScatter * 22f * sizeFactor / 20f
                    + tScatter * tScatter * 4f * sizeFactor;
            float trailAlpha = (1f - (float) t / (trailCount + 1)) * 0.35f * scatterT;
            int ta = Math.max(0, Math.min(255, (int) (alpha * trailAlpha)));
            if (ta < 4) continue;
            float hue = (float) (((ms / 3000.0) + sd.phase) % 1.0);
            int rgb = Color.HSBtoRGB(hue, 0.6f, 0.9f) & 0x00FFFFFF;
            int color = (ta << 24) | rgb;
            float tx = charX + sd.cx + tDispX;
            float ty = charY + sd.cy + tDispY;
            poseStack.pushPose();
            poseStack.translate(tx - 1f, ty - 1f, 0);
            font.drawShadow(poseStack, "·", 0, 0, color);
            poseStack.popPose();
        }
    }

    private static void drawCrackLinesJade(PoseStack poseStack, Font font, String ch, float charX, float charY, ShardData[] shards, float crackT, int alpha, long ms) {
        float glowT = crackT * (1f - crackT * 0.3f);
        int glowAlpha = (int) (alpha * glowT * 0.7f);
        if (glowAlpha < 5) return;
        for (ShardData sd : shards) {
            float pulse = (float) (Math.sin(ms / 300.0 + sd.phase * Math.PI * 4) * 0.3 + 0.7);
            int ptAlpha = (int) (glowAlpha * pulse);
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
}