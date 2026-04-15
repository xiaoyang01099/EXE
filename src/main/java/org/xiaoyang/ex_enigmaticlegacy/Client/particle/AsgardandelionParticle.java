package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AsgardandelionParticle extends TextureSheetParticle {
    private final SpriteSet animatedSprite;
    private double angleOffset;
    private double windFactor;

    public AsgardandelionParticle(SpriteSet animatedSprite, ClientLevel world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        super(world, posX, posY, posZ, motionX, motionY, motionZ);

        this.xd = motionX + (random.nextDouble() - 0.5D) * 0.1D;
        this.yd = motionY + (random.nextDouble() * 0.1D) + 0.05D;
        this.zd = motionZ + (random.nextDouble() - 0.5D) * 0.1D;

        this.animatedSprite = animatedSprite;
        this.lifetime = random.nextInt(100) + 100;
        this.quadSize = 0.2F * (this.random.nextFloat() * 0.5F + 0.5F);
        this.pickSprite(animatedSprite);

        this.angleOffset = random.nextDouble() * Math.PI * 2;
        this.windFactor = 0.01 + random.nextDouble() * 0.03;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;

        float lifeFraction = (float) this.age / (float) this.lifetime;
        this.setAlpha(1.0F - lifeFraction);

        if (this.age >= this.lifetime) {
            this.remove();
        } else {
            this.angleOffset += 0.15;
            double sway = Math.sin(this.angleOffset) * 0.1;

            this.xd += sway * this.windFactor;
            this.zd += sway * this.windFactor;

            this.yd = (0.05D + random.nextDouble() * 0.05D) * (1 - lifeFraction);

            this.move(this.xd, this.yd, this.zd);

            this.oRoll = this.roll;
            this.roll += (float) (0.1 * (Math.sin(this.angleOffset) * 0.5));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

//    private static final ParticleRenderType RAINBOW_COSMIC_RENDER = new ParticleRenderType() {
//        @Override
//        public void begin(BufferBuilder builder, TextureManager textureManager) {
//            RenderSystem.depthMask(true);
//
//            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
//
//            Minecraft mc = Minecraft.getInstance();
//            if (mc.gameRenderer != null && mc.gameRenderer.lightTexture() != null) {
//                mc.gameRenderer.lightTexture().turnOnLightLayer();
//            }
//
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
//
//            if (RainbowAvaritiaShaders.particleCosmicShader != null) {
//                RenderSystem.setShader(() -> RainbowAvaritiaShaders.particleCosmicShader);
//            }
//
//            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
//        }
//
//        @Override
//        public void end(Tesselator tesselator) {
//            tesselator.end();
//        }
//
//        @Override
//        public String toString() {
//            return "RAINBOW_COSMIC_PARTICLE";
//        }
//    };
//
//    @Override
//    public void render(@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
//        super.render(buffer, camera, partialTicks);
//    }
//
//    @Override
//    public ParticleRenderType getRenderType() {
//        return RAINBOW_COSMIC_RENDER;
//    }

    @Override
    public int getLightColor(float partialTick) {
        float f = this.lifetime / (((this.age + (this.lifetime * 0.5F)) + partialTick));
        f = Mth.clamp(f, 0F, 0.5F);
        int i = super.getLightColor(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15f * 16f);
        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet animatedSprite;

        public Factory(SpriteSet animatedSprite) {
            this.animatedSprite = animatedSprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new AsgardandelionParticle(this.animatedSprite, world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
