package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.shaders.Uniform;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXEParticleLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.slash.ParticleOwnership;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.slash.SlashCofig;

import javax.annotation.Nullable;

public class TrueDemonParticle extends TextureSheetParticle {

    private static final int LAYER_BUFFER_SIZE = 262144;
    private static final LayerBuffer BLACK_LAYER_BUFFER = new LayerBuffer(LAYER_BUFFER_SIZE);
    private static final LayerBuffer WHITE_LAYER_BUFFER = new LayerBuffer(LAYER_BUFFER_SIZE);
    private static final LayerBuffer MAGENTA_LAYER_BUFFER = new LayerBuffer(LAYER_BUFFER_SIZE);
    private static final float BASE_RED = 1.0f;
    private static final float BASE_GREEN = 45.0f / 255.0f;
    private static final float BASE_BLUE = 242.0f / 255.0f;
    private static final float MIN_BRIGHTNESS = 0.42f;
    private static final float MAX_BRIGHTNESS = 1.55f;
    private final float baseSize;
    private final float pulsePhase;
    private final float pulseSpeed;
    private final float flickerPhase;
    private final float secondaryPhase;
    private final float colorBias;
    private final boolean weaponMote;
    private final long dimensionalSlashRoundId;
    @Nullable private final TrueDemonWeaponParticleVisuals.Profile weaponVisual;

    protected TrueDemonParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        this(level, x, y, z, vx, vy, vz, false, 1.0f);
    }

    private TrueDemonParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, boolean weaponMote, float sizeScale) {
        this(level, x, y, z, vx, vy, vz, weaponMote, sizeScale, false);
    }

    private TrueDemonParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                              boolean weaponMote, float sizeScale, boolean corrosionMote) {
        super(level, x, y, z);
        this.dimensionalSlashRoundId = ParticleOwnership.claimSpawnOwner();
        this.weaponMote = weaponMote;
        this.weaponVisual = weaponMote
                ? (corrosionMote
                ? TrueDemonWeaponParticleVisuals.createCorrosion(this.random, sizeScale)
                : TrueDemonWeaponParticleVisuals.create(this.random, sizeScale))
                : null;
        if (this.weaponVisual != null) {
            this.lifetime = this.weaponVisual.lifetime();
            this.baseSize = this.weaponVisual.baseSize();
            this.pulsePhase = this.weaponVisual.pulsePhase();
            this.pulseSpeed = this.weaponVisual.pulseSpeed();
            this.flickerPhase = this.weaponVisual.flickerPhase();
            this.secondaryPhase = 0.0F;
            this.colorBias = this.weaponVisual.colorBias();
        } else {
            this.lifetime = 44 + this.random.nextInt(22);
            this.baseSize = (0.34F + this.random.nextFloat() * 0.22F) * Mth.clamp(sizeScale, 0.01f, 4.0f);
            this.pulsePhase = this.random.nextFloat() * Mth.TWO_PI;
            this.pulseSpeed = 0.38F + this.random.nextFloat() * 0.22F;
            this.flickerPhase = this.random.nextFloat() * Mth.TWO_PI;
            this.secondaryPhase = this.random.nextFloat() * Mth.TWO_PI;
            this.colorBias = this.random.nextFloat();
        }

        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.friction = weaponMote ? 1.0f : this.friction;
        if (corrosionMote) {
            this.friction = 0.94f;
            this.xd = (this.random.nextDouble() - 0.5D) * 0.010D;
            this.yd = 0.012D + this.random.nextDouble() * 0.010D;
            this.zd = (this.random.nextDouble() - 0.5D) * 0.010D;
        } else {
            this.xd = weaponMote ? 0.0D : vx * 0.056;
            this.yd = weaponMote ? 0.0D : vy * 0.056;
            this.zd = weaponMote ? 0.0D : vz * 0.056;
        }

        applyVisualState(0.0f);
    }

    @Override
    public void tick() {
        if (ParticleOwnership.isRenderFrozen(this.dimensionalSlashRoundId)) return;
        super.tick();
        applyVisualState(0.0f);
    }

    private void applyVisualState(float partialTicks) {
        if (this.weaponVisual != null) {
            TrueDemonWeaponParticleVisuals.Sample sample = weaponSample(partialTicks);
            this.quadSize = sample.size();
            this.alpha = sample.alpha();
            this.rCol = sample.red();
            this.gCol = sample.green();
            this.bCol = sample.blue();
            return;
        }

        float brightness = visualBrightness(partialTicks);
        float pulse = visualPulse(partialTicks);

        this.quadSize = visualQuadSize(partialTicks);
        this.alpha = visualAlpha(partialTicks);
        this.rCol = Mth.clamp(BASE_RED * (0.72f + brightness * 0.28f), 0.0f, 1.0f);
        this.gCol = Mth.clamp(BASE_GREEN * (0.55f + pulse * 1.10f) + colorBias * 0.030f, 0.0f, 0.32f);
        this.bCol = Mth.clamp(BASE_BLUE * (0.70f + brightness * 0.36f) + colorBias * 0.060f, 0.0f, 1.0f);
    }

    private float visualQuadSize(float partialTicks) {
        if (this.weaponVisual != null) return weaponSample(partialTicks).size();

        float envelope = lifeEnvelope(partialTicks);
        float pulse = visualPulse(partialTicks);
        float flicker = visualFlicker(partialTicks);
        return this.baseSize * envelope * (0.62f + pulse * 0.50f + flicker * 0.18f);
    }

    private float visualAlpha(float partialTicks) {
        if (this.weaponVisual != null) return weaponSample(partialTicks).alpha();

        float envelope = lifeEnvelope(partialTicks);
        float brightness = visualBrightness(partialTicks);
        return Mth.clamp(envelope * (0.38f + brightness * 0.58f), 0.0f, 1.0f);
    }

    private float visualBrightness(float partialTicks) {
        if (this.weaponVisual != null) return weaponSample(partialTicks).brightness();

        float pulse = visualPulse(partialTicks);
        float flicker = visualFlicker(partialTicks);
        return Mth.clamp(0.35f + pulse * 0.70f + flicker * 0.36f, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

    private float visualPulse(float partialTicks) {
        if (this.weaponVisual != null) return weaponSample(partialTicks).pulse();

        float visualAge = visualAge(partialTicks);
        return 0.5f + 0.5f * Mth.sin(visualAge * this.pulseSpeed + this.pulsePhase);
    }

    private float visualFlicker(float partialTicks) {
        if (this.weaponVisual != null) return weaponSample(partialTicks).flicker();

        float visualAge = visualAge(partialTicks);
        float coarse = Mth.sin(visualAge * 1.37f + this.flickerPhase) * 0.58f;
        float fine = Mth.sin(visualAge * 4.63f + this.secondaryPhase) * 0.42f;
        return Mth.clamp(0.5f + coarse * 0.5f + fine * 0.5f, 0.0f, 1.0f);
    }

    private float lifeEnvelope(float partialTicks) {
        float progress = visualAge(partialTicks) / Math.max(1.0f, (float) this.lifetime);
        if (this.weaponMote) {
            float fadeIn = smootherstep(0.0f, 0.22f, progress);
            float fadeOut = 1.0f - smootherstep(0.52f, 1.0f, progress);
            return fadeIn * fadeOut;
        }
        return burstScaleEnvelope(partialTicks);
    }

    private float burstScaleEnvelope(float partialTicks) {
        float progress = visualAge(partialTicks) / Math.max(1.0f, (float) this.lifetime);
        float sine = Mth.sin(Mth.PI * Mth.clamp(progress, 0.0f, 1.0f));
        return sine * sine;
    }

    private static float smootherstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / Math.max(1.0e-6f, edge1 - edge0), 0.0f, 1.0f);
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    private float visualAge(float partialTicks) {
        return Mth.clamp(this.age + partialTicks, 0.0f, (float) this.lifetime);
    }

    private TrueDemonWeaponParticleVisuals.Sample weaponSample(float partialTicks) {
        if (this.weaponVisual == null) {
            throw new IllegalStateException("Weapon particle visual requested for a non-weapon particle");
        }
        return this.weaponVisual.sample(visualAge(partialTicks));
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

        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F,  1.0F, 0.0F), new Vector3f( 1.0F,  1.0F, 0.0F), new Vector3f( 1.0F, -1.0F, 0.0F)};

        float scale = visualQuadSize(partialTicks);
        float worldVisibility = ParticleOwnership.visibility(this.dimensionalSlashRoundId, partialTicks);
        float renderAlpha = visualAlpha(partialTicks) * worldVisibility;
        if (scale <= 1.0e-5f || renderAlpha <= 1.0e-4f) {
            return;
        }

        float brightness = visualBrightness(partialTicks);
        float pulse = visualPulse(partialTicks);
        float renderR = Mth.clamp(BASE_RED * (0.72f + brightness * 0.28f), 0.0f, 1.0f);
        float renderG = Mth.clamp(BASE_GREEN * (0.55f + pulse * 1.10f) + colorBias * 0.030f, 0.0f, 0.32f);
        float renderB = Mth.clamp(BASE_BLUE * (0.70f + brightness * 0.36f) + colorBias * 0.060f, 0.0f, 1.0f);

        for (int i = 0; i < 4; ++i) {
            corners[i].rotate(rotation).mul(scale).add(x, y, z);
        }

        int light = this.getLightColor(partialTicks);

        if (EXEParticleLateRenderQueue.shouldDefer()) {
            EXEParticleLateRenderQueue.enqueueGlow(this, corners, renderR, renderG, renderB, renderAlpha, light);
            return;
        }

        emitLayerQuad(BLACK_LAYER_BUFFER.builder(), corners, renderR, renderG, renderB, renderAlpha, light);
        emitLayerQuad(WHITE_LAYER_BUFFER.builder(), corners, renderR, renderG, renderB, renderAlpha, light);
        emitLayerQuad(MAGENTA_LAYER_BUFFER.builder(), corners, renderR, renderG, renderB, renderAlpha, light);
    }

    private static void emitLayerQuad(BufferBuilder buffer, Vector3f[] corners, float red, float green, float blue, float alpha, int light) {
        if (buffer == null) return;
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(0.0F, 1.0F).color(red, green, blue, alpha).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(0.0F, 0.0F).color(red, green, blue, alpha).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(1.0F, 0.0F).color(red, green, blue, alpha).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(1.0F, 1.0F).color(red, green, blue, alpha).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return TRUE_DEMON_GLOW_PARTICLE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider() {}

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new TrueDemonParticle(level, x, y, z, vx, vy, vz);
        }
    }

    public static class SlashProvider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new TrueDemonParticle(level, x, y, z, vx, vy, vz, false, SlashCofig.WorldSlash.SlashParticles.SIZE_SCALE);
        }

        public SlashProvider() {}
    }

    public static class WeaponProvider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            float sizeScale = vx > 0.0D ? Mth.clamp((float) vx, 0.10f, 2.0f) : 1.0f;
            return new TrueDemonParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, true, sizeScale);
        }

        public WeaponProvider() {}
    }

    public static class CorrosionProvider implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            float sizeScale = vx > 0.0D ? Mth.clamp((float) vx, 0.10f, 2.0f) : 1.0f;
            return new TrueDemonParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, true, sizeScale, true);
        }

        public CorrosionProvider() {}
    }

    private static final ParticleRenderType TRUE_DEMON_GLOW_PARTICLE = new ParticleRenderType() {
        @Override
        public void begin(@NotNull BufferBuilder builder, @NotNull TextureManager textureManager) {
            if (EXEParticleLateRenderQueue.shouldDefer()) return;

            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);

            RenderSystem.applyModelViewMatrix();
            BLACK_LAYER_BUFFER.begin();
            WHITE_LAYER_BUFFER.begin();
            MAGENTA_LAYER_BUFFER.begin();
        }

        @Override
        public void end(@NonNull Tesselator tessellate) {
            if (EXEParticleLateRenderQueue.shouldDefer()) return;

            BLACK_LAYER_BUFFER.draw(EXECoreShaders.getTrueDemonParticleShader(), 0);
            WHITE_LAYER_BUFFER.draw(EXECoreShaders.getTrueDemonParticleWhiteShader(), 1);
            MAGENTA_LAYER_BUFFER.draw(EXECoreShaders.getTrueDemonParticleMagentaShader(), 2);

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.applyModelViewMatrix();
        }

        @Override
        public String toString() {
            return "true_demon_type";
        }
    };

    private static final class LayerBuffer {
        private final int initialSize;
        private BufferBuilder builder;

        private LayerBuffer(int initialSize) {
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

        private void draw(ShaderInstance shader, int layer) {
            if (builder == null || !builder.building()) return;

            BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
            if (rendered == null) return;
            if (shader == null) {
                Exe.LOGGER.warn("True Demon Particle layer shader {} is null!", layer);
                return;
            }

            Uniform layerUniform = shader.getUniform("Layer");
            if (layerUniform != null) layerUniform.set(layer);
            RenderSystem.setShader(() -> shader);
            BufferUploader.drawWithShader(rendered);
        }
    }

}
