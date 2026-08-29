package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.SpecialCoreShaders;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXEParticleLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.slash.ParticleOwnership;

public class TrueDemonStarParticle extends TextureSheetParticle {
    private static final int STAR_BUFFER_SIZE = 65536;
    private static final StarBuffer STAR_BUFFER = new StarBuffer(STAR_BUFFER_SIZE);
    private final float baseSize;
    private final float maxAlpha;
    private final float pulsePhase;
    private final float pulseSpeed;
    private final float flickerPhase;
    private final float flickerSpeed;
    private final float secondaryPhase;
    private final long dimensionalSlashRoundId;

    protected TrueDemonStarParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z);
        this.dimensionalSlashRoundId = ParticleOwnership.claimSpawnOwner();

        this.lifetime = 44 + this.random.nextInt(29);

        float sizeMix = this.random.nextFloat();
        this.baseSize = Mth.lerp(sizeMix * sizeMix, 0.070f, 0.24f) * (this.random.nextFloat() < 0.22f ? 1.65f : 1.0f);
        this.maxAlpha = 0.72f + this.random.nextFloat() * 0.28f;
        this.pulsePhase = this.random.nextFloat() * Mth.TWO_PI;
        this.pulseSpeed = 0.28f + this.random.nextFloat() * 0.28f;
        this.flickerPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.flickerSpeed = 1.20f + this.random.nextFloat() * 1.55f;
        this.secondaryPhase = this.random.nextFloat() * Mth.TWO_PI;

        this.gravity = 0.0f;
        this.friction = 0.94f;
        this.hasPhysics = false;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        applyVisualState(0.0f);
    }

    @Override
    public void tick() {
        if (ParticleOwnership.isRenderFrozen(this.dimensionalSlashRoundId)) return;
        super.tick();
        applyVisualState(0.0f);
    }

    private void applyVisualState(float partialTicks) {
        this.quadSize = visualQuadSize(partialTicks);
        this.alpha = visualAlpha(partialTicks);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
    }

    private float visualQuadSize(float partialTicks) {
        float envelope = lifeEnvelope(partialTicks);
        float pulse = visualPulse(partialTicks);
        float flicker = visualFlicker(partialTicks);
        return this.baseSize * (0.76f + pulse * 0.30f + flicker * 0.22f) * (0.30f + envelope * 0.70f);
    }

    private float visualAlpha(float partialTicks) {
        float envelope = lifeEnvelope(partialTicks);
        float brightness = visualBrightness(partialTicks);
        return Mth.clamp(envelope * (0.12f + brightness * 0.88f) * this.maxAlpha, 0.0f, 1.0f);
    }

    private float visualCoreAlpha(float partialTicks) {
        float envelope = lifeEnvelope(partialTicks);
        float brightness = visualBrightness(partialTicks);
        return Mth.clamp(envelope * (0.70f + brightness * 0.30f) * this.maxAlpha, 0.0f, 1.0f);
    }

    private float visualBrightness(float partialTicks) {
        float pulse = visualPulse(partialTicks);
        float flicker = visualFlicker(partialTicks);
        return Mth.clamp(0.08f + pulse * 0.62f + flicker * 0.42f, 0.08f, 1.0f);
    }

    private float visualPulse(float partialTicks) {
        float visualAge = visualAge(partialTicks);
        return 0.5f + 0.5f * Mth.sin(visualAge * this.pulseSpeed + this.pulsePhase);
    }

    private float visualFlicker(float partialTicks) {
        float visualAge = visualAge(partialTicks);
        float main = Mth.sin(visualAge * this.flickerSpeed + this.flickerPhase) * 0.62f;
        float secondary = Mth.sin(visualAge * (this.flickerSpeed * 2.45f) + this.secondaryPhase) * 0.38f;
        return Mth.clamp(0.5f + main * 0.5f + secondary * 0.5f, 0.0f, 1.0f);
    }

    private float lifeEnvelope(float partialTicks) {
        float progress = visualAge(partialTicks) / Math.max(1.0f, (float) this.lifetime);
        float fadeIn = Mth.clamp(progress / 0.12f, 0.0f, 1.0f);
        float fadeOut = Mth.clamp((1.0f - progress) / 0.42f, 0.0f, 1.0f);
        return fadeIn * fadeOut;
    }

    private float visualAge(float partialTicks) {
        return Mth.clamp(this.age + partialTicks, 0.0f, (float) this.lifetime);
    }

    @Override
    protected int getLightColor(float partialTicks) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        partialTicks = ParticleOwnership.partialTick(this.dimensionalSlashRoundId, partialTicks);
        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z());

        float scale = visualQuadSize(partialTicks);
        float worldVisibility = ParticleOwnership.visibility(this.dimensionalSlashRoundId, partialTicks);
        float renderAlpha = visualAlpha(partialTicks) * worldVisibility;
        float coreAlpha = visualCoreAlpha(partialTicks) * worldVisibility;
        if (scale <= 1.0e-5f || Math.max(renderAlpha, coreAlpha) <= 1.0e-4f) {
            return;
        }

        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F,  1.0F, 0.0F), new Vector3f( 1.0F,  1.0F, 0.0F), new Vector3f( 1.0F, -1.0F, 0.0F)};
        for (int i = 0; i < 4; ++i) {
            corners[i].rotate(rotation).mul(scale).add(x, y, z);
        }

        int light = this.getLightColor(partialTicks);
        if (EXEParticleLateRenderQueue.shouldDefer()) {
            EXEParticleLateRenderQueue.enqueueStar(this, corners, renderAlpha, coreAlpha, light);
            return;
        }

        emitQuad(STAR_BUFFER.builder(), corners, renderAlpha, coreAlpha, light);
    }

    private static void emitQuad(BufferBuilder buffer, Vector3f[] corners, float alpha, float coreAlpha, int light) {
        if (buffer == null) return;
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(0.0F, 1.0F).color(coreAlpha, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(0.0F, 0.0F).color(coreAlpha, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(1.0F, 0.0F).color(coreAlpha, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(1.0F, 1.0F).color(coreAlpha, 1.0F, 1.0F, alpha).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return TRUE_DEMON_STAR_PARTICLE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider() {}

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new TrueDemonStarParticle(level, x, y, z, vx, vy, vz);
        }
    }

    private static final ParticleRenderType TRUE_DEMON_STAR_PARTICLE = new ParticleRenderType() {
        @Override
        public void begin(@NotNull BufferBuilder builder, @NotNull TextureManager textureManager) {
            if (EXEParticleLateRenderQueue.shouldDefer()) return;

            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);

            RenderSystem.applyModelViewMatrix();
            STAR_BUFFER.begin();
        }

        @Override
        public void end(Tesselator tessellator) {
            if (EXEParticleLateRenderQueue.shouldDefer()) return;

            STAR_BUFFER.draw(SpecialCoreShaders.getTrueDemonStarParticleShader());

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.applyModelViewMatrix();
        }

        @Override
        public String toString() {
            return "true_demon_star_type";
        }
    };

    private static final class StarBuffer {
        private final int initialSize;
        private BufferBuilder builder;

        private StarBuffer(int initialSize) {
            this.initialSize = initialSize;
        }

        private void begin() {
            if (builder == null || builder.building()) {
                builder = new BufferBuilder(initialSize);
            }
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        private BufferBuilder builder() {
            return builder != null && builder.building() ? builder : null;
        }

        private void draw(ShaderInstance shader) {
            if (builder == null || !builder.building()) return;

            BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
            if (rendered == null) return;
            if (shader == null) {
                Exe.LOGGER.warn("True Demon Star Particle shader is null!");
                return;
            }

            RenderSystem.setShader(() -> shader);
            BufferUploader.drawWithShader(rendered);
        }
    }
}
