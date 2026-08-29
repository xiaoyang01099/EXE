package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.SwordAuraEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.SwordAuraVisualConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.SwordAuraShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class SwordAuraQueue extends EntityQueue<SwordAuraEntity> {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D);
    public static final int COLOR_FULL = 255;
    public float animationTime;
    public TextureAtlasSprite auraSprite;
    public TextureAtlasSprite gradientSprite;
    public TextureAtlasSprite blueGradientSprite;
    public final SwordAuraInstancedRenderer instancedRenderer;

    public SwordAuraQueue() {
        super();
        instancedRenderer = new SwordAuraInstancedRenderer();
    }

    @Override
    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        render(fboBuffer, camera, partialTick, viewMatrix, 0.0F);
    }

    @Override
    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix, float frameDeltaSeconds) {
        if (!SwordAuraShader.isLoaded()) return;
        if (entities.isEmpty()) return;
        if (!SwordAuraObjModel.isLoaded()) return;

        animationTime += frameDeltaSeconds;
        SwordAuraShader.setGlobalParams(animationTime, (float) SwordAuraVisualConfig.BLOOM_STRENGTH_SCALE);
        SwordAuraShader.setView(viewMatrix);

        TextureAtlasSprite sprite = getAuraSprite();
        TextureAtlasSprite gradient = getGradientSprite();
        TextureAtlasSprite blueGradient = getBlueGradientSprite();
        if (sprite == null || gradient == null || blueGradient == null) return;
        SwordAuraShader.setSpriteUVs(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), gradient.getU0(), gradient.getV0(), gradient.getU1(), gradient.getV1(), blueGradient.getU0(), blueGradient.getV0(), blueGradient.getU1(), blueGradient.getV1());
        fboBuffer.endBatch();
        BakedModel model = SwordAuraObjModel.getModel();
        boolean rendered = instancedRenderer.render(entities, this, model, sprite, partialTick);
        if (!rendered) {
            return;
        }
        for (SwordAuraEntity aura : entities) {
            emitTrailParticlesIfVisible(aura, partialTick);
        }
    }

    public float getFade(SwordAuraEntity aura, float partialTick) {
        if (aura.isPreviewStatic()) {
            return 1.0F;
        }
        float ageProgress = aura.getAgeProgress(partialTick);
        return 1.0F - Mth.clamp((ageProgress - 0.78F) / 0.22F, 0.0F, 1.0F);
    }

    public int getGradientSelect(SwordAuraEntity aura) {
        return (aura.getVisualSeed() & 1) == 0 ? 0 : COLOR_FULL;
    }

    public float getRevealProgress(SwordAuraEntity aura, float partialTick) {
        if (aura.isPreviewStatic()) {
            return 1.0F;
        }
        float ageProgress = aura.getAgeProgress(partialTick);
        float revealProgress = ageProgress / Math.max(0.01F, (float) SwordAuraVisualConfig.REVEAL_COMPLETE_PROGRESS);
        return Mth.clamp(revealProgress, 0.0F, 1.0F);
    }

    public AuraBasis buildAuraBasis(SwordAuraEntity aura, float partialTick, float visualProgress) {
        Vec3 center = aura.getPosition(partialTick);
        Vec3 direction = aura.getAuraDirection();
        Vec3 side = getRolledSide(direction, aura.getRollRadians());
        Vec3 up = SwordAuraEntity.safeNormalize(side.cross(direction), WORLD_UP);
        float grow = Mth.lerp(Mth.clamp(visualProgress, 0.0F, 1.0F),
                (float) SwordAuraVisualConfig.OBJ_START_SCALE,
                (float) SwordAuraVisualConfig.OBJ_END_SCALE);
        double scale = SwordAuraVisualConfig.OBJ_BASE_SCALE * grow;
        return new AuraBasis(center, side, up, direction, scale);
    }

    public Vec3 getRolledSide(Vec3 direction, float rollRadians) {
        Vec3 side = SwordAuraEntity.safeNormalize(direction.cross(WORLD_UP), WORLD_RIGHT);
        Vec3 up = SwordAuraEntity.safeNormalize(side.cross(direction), WORLD_UP);
        double cos = Math.cos(rollRadians);
        double sin = Math.sin(rollRadians);
        return SwordAuraEntity.safeNormalize(side.scale(cos).add(up.scale(sin)), side);
    }

    public TextureAtlasSprite getAuraSprite() {
        if (auraSprite == null) {
            auraSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.SWORD_AURA_TEXTURE);
        }
        return auraSprite;
    }

    public TextureAtlasSprite getGradientSprite() {
        if (gradientSprite == null) {
            gradientSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.multi_gradient);
        }
        return gradientSprite;
    }

    public TextureAtlasSprite getBlueGradientSprite() {
        if (blueGradientSprite == null) {
            blueGradientSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.BLUE_GRADIENT_TEXTURE);
        }
        return blueGradientSprite;
    }

    public void emitTrailParticles(SwordAuraEntity aura, AuraBasis basis) {
        if (aura.isPreviewStatic()) {
            return;
        }
        boolean blueGradient = (aura.getVisualSeed() & 1) != 0;
        Vec3 tailCenter = basis.center.subtract(basis.forward.scale(SwordAuraVisualConfig.TRAIL_BACK_OFFSET));
        Vec3 backward = basis.forward.scale(-1.0D);
        float particleRotation = aura.getRollRadians();
        emitTrailGroupAtSide(aura, tailCenter, backward, particleRotation, basis, blueGradient, -1.0D);
        emitTrailGroupAtSide(aura, tailCenter, backward, particleRotation, basis, blueGradient, -2.5D);
        emitTrailGroupAtSide(aura, tailCenter, backward, particleRotation, basis, blueGradient, 2.5D);
        emitTrailGroupAtSide(aura, tailCenter, backward, particleRotation, basis, blueGradient, 0.7D);
    }

    public void emitTrailGroupAtSide(SwordAuraEntity aura, Vec3 tailCenter, Vec3 backward, float particleRotation,
                                     AuraBasis basis, boolean blueGradient, double sideFactor) {
        Vec3 offset = basis.side.scale(SwordAuraVisualConfig.TRAIL_SIDE_OFFSET * sideFactor);
        Vec3 sideSpread = basis.side.scale(SwordAuraVisualConfig.TRAIL_SIDE_DIRECTION_SCALE * sideFactor);
        RandomSource shapeRandom = RandomSource.create(aura.getVisualSeed() * 31L + Math.round((sideFactor + 2.0D) * 1000.0D));
        emitTrailGroup(tailCenter.add(offset), backward.add(sideSpread), particleRotation + (float) sideFactor * 0.35F,
                basis, blueGradient, shapeRandom);
    }

    public void emitTrailGroup(Vec3 position, Vec3 direction, float particleRotation, AuraBasis basis,
                               boolean blueGradient, RandomSource shapeRandom) {
        Vec3 finalDirection = SwordAuraEntity.safeNormalize(direction, basis.forward.scale(-1.0D));
        float startR = blueGradient ? 0.46F : 0.78F;
        float startG = blueGradient ? 0.86F : 0.48F;
        float startB = 1.0F;
        float speed = 1.55F;
        float spread = 2.34F;
        float life = 0.42F;
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(position)
                .direction((float) finalDirection.x, (float) finalDirection.y, (float) finalDirection.z)
                .speed(speed)
                .spread(spread)
                .life(life)
                .gravity(0.0F)
                .size(0.05f,0.05f, particleRotation)
                .color(startR, startG, startB, 1.0f)
                .endColor(0.16F, 0.46F, 1.0F, 1.0F)
                .randomShape(shapeRandom)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(SwordAuraVisualConfig.TRAIL_SIDE_RATE)
                .duration(0.1f));
    }

    public void emitTrailParticlesIfVisible(SwordAuraEntity aura, float partialTick) {
        float visualProgress = aura.isPreviewStatic() ? 1.0F : aura.getAgeProgress(partialTick);
        float fade = getFade(aura, partialTick);
        if (fade <= 0.01F) {
            return;
        }
        emitTrailParticles(aura, buildAuraBasis(aura, partialTick, visualProgress));
    }

    public void cleanUp() {
        instancedRenderer.cleanup();
    }

    public static class AuraBasis {
        public final Vec3 center; // 剑气实体中心位置。
        public final Vec3 side; // 剑气局部 X 轴对应的世界方向。
        public final Vec3 up; // 剑气局部 Y 轴对应的世界方向。
        public final Vec3 forward; // 剑气局部 Z 轴对应的世界方向。
        public final double scale; // OBJ 局部坐标整体缩放。

        public AuraBasis(Vec3 center, Vec3 side, Vec3 up, Vec3 forward, double scale) {
            this.center = center;
            this.side = side;
            this.up = up;
            this.forward = forward;
            this.scale = scale;
        }

    }
}
