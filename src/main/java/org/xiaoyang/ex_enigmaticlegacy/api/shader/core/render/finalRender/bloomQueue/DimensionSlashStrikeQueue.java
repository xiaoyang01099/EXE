package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashStrikeEntity;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.DimensionSlashStrikeRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.DimensionSlashStrikeShader;

import java.util.Random;

public class DimensionSlashStrikeQueue extends EntityQueue<DimensionSlashStrikeEntity> {
    public DimensionSlashStrikeQueue() {
        super();
    }

    @Override
    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        if (!DimensionSlashStrikeShader.isLoaded()) return;
        float time = MathUtil.getClientTime(partialTick);
        DimensionSlashStrikeShader.setEffectParams(time, DimensionSlashConfig.BLOOM_STRENGTH);
        DimensionSlashStrikeShader.setView(viewMatrix);
        VertexConsumer consumer = fboBuffer.getBuffer(DimensionSlashStrikeRenderType.getRenderType());
        for (DimensionSlashStrikeEntity entity : entities) {
            renderStrikes(consumer, entity, camera, partialTick);
        }
        fboBuffer.endBatch(DimensionSlashStrikeRenderType.getRenderType());
    }

    public void renderStrikes(VertexConsumer consumer, DimensionSlashStrikeEntity entity, Camera camera, float partialTick) {
        float age = entity.getAge() + partialTick;
        int total = DimensionSlashConfig.STRIKE_COUNT;
        for (int i = 0; i < total; i++) {
            float spawnAge = i * (DimensionSlashConfig.STRIKE_SPAWN_TICKS / (float) total);
            float localAge = age - spawnAge;
            if (localAge < 0.0F) continue;
            float alpha = getStrikeAlpha(age, localAge);
            if (alpha <= 0.001F) continue;
            writeStrikeQuad(consumer, entity, camera, i, alpha);
        }
    }

    public float getStrikeAlpha(float age, float localAge) {
        float fadeIn = Mth.clamp(localAge / 0.55F, 0.0F, 1.0F);
        float globalFade = 1.0F - Mth.clamp((age - DimensionSlashConfig.STRIKE_HOLD_END_TICK) / Math.max(1.0F, (float) DimensionSlashConfig.STRIKE_FADE_OUT_TICKS), 0.0F, 1.0F);
        return fadeIn * globalFade;
    }

    public void writeStrikeQuad(VertexConsumer consumer, DimensionSlashStrikeEntity entity, Camera camera, int index, float alpha) {
        Random random = new Random(entity.getVisualSeed() * 734287L + index * 9127L);
        double radius = DimensionSlashConfig.RADIUS * (0.06D + random.nextDouble() * 0.86D);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double height = 0.35D + random.nextDouble() * 4.7D;
        Vec3 center = entity.position().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
        Vec3 toCamera = camera.getPosition().subtract(center);
        Vec3 cameraRight = getSafeNormalize(new Vec3(toCamera.z, 0.0D, -toCamera.x), new Vec3(1.0D, 0.0D, 0.0D));
        Vec3 cameraUp = getSafeNormalize(cameraRight.cross(toCamera).normalize(), new Vec3(0.0D, 1.0D, 0.0D));
        float roll = (float) (random.nextDouble() * Math.PI);
        double cos = Math.cos(roll);
        double sin = Math.sin(roll);
        Vec3 axisLong = getSafeNormalize(cameraRight.scale(cos).add(cameraUp.scale(sin)), cameraRight);
        Vec3 axisWide = getSafeNormalize(axisLong.cross(toCamera).normalize(), cameraUp);
        double length = 4.8D + random.nextDouble() * 8.4D;
        double width = 0.045D + random.nextDouble() * 0.105D;
        Vec3 p0 = center.subtract(axisLong.scale(length)).subtract(axisWide.scale(width));
        Vec3 p1 = center.add(axisLong.scale(length)).subtract(axisWide.scale(width));
        Vec3 p2 = center.add(axisLong.scale(length)).add(axisWide.scale(width));
        Vec3 p3 = center.subtract(axisLong.scale(length)).add(axisWide.scale(width));
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        writeVertex(consumer, p0, 0.0F, 0.0F, a);
        writeVertex(consumer, p1, 1.0F, 0.0F, a);
        writeVertex(consumer, p2, 1.0F, 1.0F, a);
        writeVertex(consumer, p3, 0.0F, 1.0F, a);
    }

    public void writeVertex(VertexConsumer consumer, Vec3 pos, float u, float v, int alpha) {
        consumer.vertex(pos.x, pos.y, pos.z)
                .color(205, 240, 255, alpha)
                .uv(u, v)
                .endVertex();
    }

    public Vec3 getSafeNormalize(Vec3 value, Vec3 fallback) {
        if (value == null || value.lengthSqr() < 1.0E-8D) return fallback;
        return value.normalize();
    }
}
