package org.xiaoyang.ex_enigmaticlegacy.Util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;
import java.io.InputStream;

@OnlyIn(Dist.CLIENT)
public class AnimatedChestTexture {
    private static final int TOTAL_FRAMES = 36;
    private static final int FRAME_TIME   = 2;
    private static final int FRAME_SIZE   = 64;

    public static final ResourceLocation DYNAMIC_LOCATION =
            new ResourceLocation(Exe.MODID, "dynamic/spectrite_chest");

    private static final ResourceLocation SOURCE_TEXTURE =
            new ResourceLocation(Exe.MODID,
                    "textures/item/entity/chest/spectrite_chest.png");

    private static DynamicTexture dynamicTexture;
    private static NativeImage    fullImage;
    private static boolean        initialized = false;
    private static int            lastFrame   = -1;

    public static void init() {
        if (initialized) return;
        try {
            InputStream is = Minecraft.getInstance()
                    .getResourceManager()
                    .open(SOURCE_TEXTURE);
            fullImage = NativeImage.read(is);
            is.close();

            NativeImage firstFrame = extractFrame(0);
            dynamicTexture = new DynamicTexture(firstFrame);

            Minecraft.getInstance()
                    .getTextureManager()
                    .register(DYNAMIC_LOCATION, dynamicTexture);

            initialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tick() {
        if (!initialized) return;

        long gameTick = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;

        int currentFrame = (int) ((gameTick / FRAME_TIME) % TOTAL_FRAMES);

        if (currentFrame == lastFrame) return;
        lastFrame = currentFrame;

        NativeImage target = dynamicTexture.getPixels();
        if (target == null) return;

        int yOffset = currentFrame * FRAME_SIZE;
        for (int x = 0; x < FRAME_SIZE; x++) {
            for (int y = 0; y < FRAME_SIZE; y++) {
                target.setPixelRGBA(x, y, fullImage.getPixelRGBA(x, y + yOffset));
            }
        }

        dynamicTexture.upload();
    }

    private static NativeImage extractFrame(int frameIndex) {
        NativeImage frame = new NativeImage(FRAME_SIZE, FRAME_SIZE, false);
        int yOffset = frameIndex * FRAME_SIZE;
        for (int x = 0; x < FRAME_SIZE; x++) {
            for (int y = 0; y < FRAME_SIZE; y++) {
                frame.setPixelRGBA(x, y, fullImage.getPixelRGBA(x, y + yOffset));
            }
        }
        return frame;
    }

    public static void close() {
        if (fullImage != null) {
            fullImage.close();
            fullImage = null;
        }
        initialized = false;
    }
}