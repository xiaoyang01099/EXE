package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityRainBowLightningBlot;

import java.util.Random;

public class RainLightningRenderer extends EntityRenderer<EntityRainBowLightningBlot> {
    public RainLightningRenderer(EntityRendererProvider.Context p_174286_) {
        super(p_174286_);
    }

    private static final Vector3f[] RAINBOW_COLORS = {
            new Vector3f(1.0F, 0.0F, 0.0F),
            new Vector3f(1.0F, 0.5F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(0.0F, 1.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, 1.0F),
            new Vector3f(0.5F, 0.0F, 1.0F)
    };

    public void render(EntityRainBowLightningBlot p_115266_, float p_115267_, float p_115268_, PoseStack p_115269_, MultiBufferSource p_115270_, int p_115271_) {
        float[] afloat = new float[8];
        float[] afloat1 = new float[8];
        float f = 0.0F;
        float f1 = 0.0F;
        Random random = new Random(p_115266_.seed);

        /*for(int j = 0; j < 4; ++j) {
            for(int y = 0; y < 8; ++y) {
                if(random.nextFloat() < 0.3F) {
                    float progress = y / 7.0F;
                    int colorIndex = (int)(progress * (RAINBOW_COLORS.length - 1));
                    Vector3f color1 = RAINBOW_COLORS[colorIndex];
                    Vector3f color2 = RAINBOW_COLORS[Math.min(colorIndex + 1, RAINBOW_COLORS.length - 1)];
                    float blend = progress * (RAINBOW_COLORS.length - 1) - colorIndex;

                    float r = color1.x() + (color2.x() - color1.x()) * blend;
                    float g = color1.y() + (color2.y() - color1.y()) * blend;
                    float b = color1.z() + (color2.z() - color1.z()) * blend;

                    Vector3f finalColor = new Vector3f(r, g, b);

                    float spread = 0.5F;
                    double x = p_115266_.getX() + (random.nextDouble() - 0.5D) * spread;
                    double z = p_115266_.getZ() + (random.nextDouble() - 0.5D) * spread;

                    p_115266_.level.addParticle(
                            new DustParticleOptions(finalColor, 1.0F),
                            x, p_115266_.getY() + y, z,
                            0, 0, 0
                    );
                }
            }
        }*/

        for(int i = 7; i >= 0; --i) {
            afloat[i] = f;
            afloat1[i] = f1;
            f += (float)(random.nextInt(11) - 5);
            f1 += (float)(random.nextInt(11) - 5);
        }

        VertexConsumer vertexconsumer = p_115270_.getBuffer(RenderType.lightning());
        Matrix4f matrix4f = p_115269_.last().pose();

        for(int j = 0; j < 4; ++j) {
            Random random1 = new Random(p_115266_.seed);

            for(int k = 0; k < 3; ++k) {
                int l = 7;
                int i1 = 0;
                if (k > 0) {
                    l = 7 - k;
                }

                if (k > 0) {
                    i1 = l - 2;
                }

                float f2 = afloat[l] - f;
                float f3 = afloat1[l] - f1;

                for(int j1 = l; j1 >= i1; --j1) {
                    float f4 = f2;
                    float f5 = f3;
                    if (k == 0) {
                        f2 += (float)(random1.nextInt(11) - 5);
                        f3 += (float)(random1.nextInt(11) - 5);
                    } else {
                        f2 += (float)(random1.nextInt(31) - 15);
                        f3 += (float)(random1.nextInt(31) - 15);
                    }

                    float f6 = 0.5F;
                    float f7 = 0.45F;
                    float f8 = 0.45F;
                    float f9 = 0.5F;
                    float f10 = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        f10 *= (float)j1 * 0.1F + 1.0F;
                    }

                    float f11 = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        f11 *= ((float)j1 - 1.0F) * 0.1F + 1.0F;
                    }
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, false, false, true, false);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, true, false, true, true);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, true, true, false, true);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, false, true, false, false);
                }
            }
        }
    }

    private static void quad(Matrix4f p_115273_, VertexConsumer p_115274_, float p_115275_, float p_115276_, int p_115277_, float p_115278_, float p_115279_, float p_115280_, float p_115281_, float p_115282_, float p_115283_, float p_115284_, boolean p_115285_, boolean p_115286_, boolean p_115287_, boolean p_115288_) {
        Random random = new Random();
        p_115274_.vertex(p_115273_, p_115275_ + (p_115285_ ? p_115284_ : -p_115284_), (float)(p_115277_ * 16), p_115276_ + (p_115286_ ? p_115284_ : -p_115284_)).color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.3F).endVertex();
        p_115274_.vertex(p_115273_, p_115278_ + (p_115285_ ? p_115283_ : -p_115283_), (float)((p_115277_ + 1) * 16), p_115279_ + (p_115286_ ? p_115283_ : -p_115283_)).color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.3F).endVertex();
        p_115274_.vertex(p_115273_, p_115278_ + (p_115287_ ? p_115283_ : -p_115283_), (float)((p_115277_ + 1) * 16), p_115279_ + (p_115288_ ? p_115283_ : -p_115283_)).color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.3F).endVertex();
        p_115274_.vertex(p_115273_, p_115275_ + (p_115287_ ? p_115284_ : -p_115284_), (float)(p_115277_ * 16), p_115276_ + (p_115288_ ? p_115284_ : -p_115284_)).color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.3F).endVertex();
    }

    public ResourceLocation getTextureLocation(EntityRainBowLightningBlot p_115264_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
