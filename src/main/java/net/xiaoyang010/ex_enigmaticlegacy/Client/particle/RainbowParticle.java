package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainbowParticle extends Particle {
    private float rotationX = 0.0F;
    private float rotationY = 0.0F;
    private float rotationZ = 0.0F;
    private final float rotationSpeedX;
    private final float rotationSpeedY;
    private final float rotationSpeedZ;
    private float cubeSize;
    private final float hueOffset;
    private static final float SATURATION = 0.75F;
    private static final float BRIGHTNESS = 1.0F;

    protected RainbowParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 60 + this.random.nextInt(40);
        this.gravity = -0.02F;
        this.friction = 0.98F;
        this.xd = xSpeed + (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = ySpeed + 0.05 + this.random.nextDouble() * 0.03;
        this.zd = zSpeed + (this.random.nextDouble() - 0.5) * 0.02;
        this.cubeSize = 0.15F + this.random.nextFloat() * 0.3F;
        this.hueOffset = this.random.nextFloat() * 360.0F;
        this.alpha = 0.8F;
        this.rotationSpeedX = this.random.nextFloat() - 0.5F;
        this.rotationSpeedY = this.random.nextFloat() - 0.5F;
        this.rotationSpeedZ = this.random.nextFloat() - 0.5F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float lifeProgress = (float) this.age / (float) this.lifetime;

            if (lifeProgress > 0.7F) {
                float fadeProgress = (lifeProgress - 0.7F) / 0.3F;
                this.alpha = 0.8F * (1.0F - fadeProgress);
            }

            float currentHue = (this.hueOffset + lifeProgress * 360.0F) % 360.0F;
            this.updateColorFromHSB(currentHue, SATURATION, BRIGHTNESS);

            this.rotationX = this.rotationSpeedX;
            this.rotationY = this.rotationSpeedY;
            this.rotationZ = this.rotationSpeedZ;

            this.cubeSize = 0.1F;

            this.xd *= this.friction;
            this.yd += this.gravity;
            this.yd *= this.friction;
            this.zd *= this.friction;
            this.move(this.xd, this.yd, this.zd);

            if (this.alpha <= 0.001F) {
                this.remove();
            }
        }
    }

    private void updateColorFromHSB(float hue, float saturation, float brightness) {
        float h = hue / 360.0F;
        int rgb = hsbToRgb(h, saturation, brightness);
        this.rCol = (float) (rgb >> 16 & 255) / 255.0F;
        this.gCol = (float) (rgb >> 8 & 255) / 255.0F;
        this.bCol = (float) (rgb & 255) / 255.0F;
    }

    private static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0.0F) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 1:
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 2:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                    break;
                case 3:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 4:
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 5:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
                    break;
            }
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Quaternion rotation = Quaternion.ONE.copy();
        rotation.mul(Vector3f.XP.rotation(this.rotationX));
        rotation.mul(Vector3f.YP.rotation(this.rotationY));
        rotation.mul(Vector3f.ZP.rotation(this.rotationZ));

        this.renderCube(buffer, x, y, z, this.cubeSize, rotation);
    }

    private void renderCube(VertexConsumer buffer, float x, float y, float z,
                            float size, Quaternion rotation) {
        float halfSize = size / 2.0F;

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-halfSize, -halfSize, -halfSize),
                new Vector3f(halfSize, -halfSize, -halfSize),
                new Vector3f(halfSize, halfSize, -halfSize),
                new Vector3f(-halfSize, halfSize, -halfSize),
                new Vector3f(-halfSize, -halfSize, halfSize),
                new Vector3f(halfSize, -halfSize, halfSize),
                new Vector3f(halfSize, halfSize, halfSize),
                new Vector3f(-halfSize, halfSize, halfSize)
        };

        for (int i = 0; i < 8; ++i) {
            vertices[i].transform(rotation);
            vertices[i].add(x, y, z);
        }

        int light = 15728880;

        this.addQuad(buffer, vertices[4], vertices[5], vertices[6], vertices[7], light);
        this.addQuad(buffer, vertices[1], vertices[0], vertices[3], vertices[2], light);
        this.addQuad(buffer, vertices[5], vertices[1], vertices[2], vertices[6], light);
        this.addQuad(buffer, vertices[0], vertices[4], vertices[7], vertices[3], light);
        this.addQuad(buffer, vertices[3], vertices[7], vertices[6], vertices[2], light);
        this.addQuad(buffer, vertices[4], vertices[0], vertices[1], vertices[5], light);
    }

    private void addQuad(VertexConsumer buffer,
                         Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4,
                         int light) {
        buffer.vertex(v1.x(), v1.y(), v1.z())
                .uv(0.0F, 0.0F)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(v2.x(), v2.y(), v2.z())
                .uv(1.0F, 0.0F)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(v3.x(), v3.y(), v3.z())
                .uv(1.0F, 1.0F)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(v4.x(), v4.y(), v4.z())
                .uv(0.0F, 1.0F)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    public static class GlowParticleRenderTypes {
        public static final ParticleRenderType CUBE_PARTICLE = new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
                RenderSystem.enableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE
                );
                RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP);
            }

            @Override
            public void end(Tesselator tesselator) {
                tesselator.end();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                );
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }

            @Override
            public String toString() {
                return "CUBE_PARTICLE";
            }
        };
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GlowParticleRenderTypes.CUBE_PARTICLE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RainbowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}