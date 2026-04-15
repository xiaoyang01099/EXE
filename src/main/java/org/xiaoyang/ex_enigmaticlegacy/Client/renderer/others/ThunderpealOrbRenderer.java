package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.EntityThunderpealOrb;
import org.xiaoyang.ex_enigmaticlegacy.Event.RelicsEventHandler;
import vazkii.botania.client.fx.SparkleParticleData;

public class ThunderpealOrbRenderer extends EntityRenderer<EntityThunderpealOrb> {

    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");

    public ThunderpealOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(EntityThunderpealOrb entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        float age = entity.tickCount + partialTick;

        renderEnergyCore(poseStack, bufferSource, age, packedLight);

        if (entity.level().isClientSide) {
            renderLightningRings(entity, age);
            renderBotaniaLightning(entity, age);
            spawnTrailParticles(entity, age);
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderEnergyCore(PoseStack poseStack, MultiBufferSource bufferSource,
                                  float age, int packedLight) {
        poseStack.pushPose();

        float pulse = 0.8F + 0.2F * Mth.sin(age * 0.3F);
        poseStack.scale(pulse, pulse, pulse);

        VertexConsumer coreBuffer = bufferSource.getBuffer(
                RenderType.energySwirl(BEAM_TEXTURE, 0, 0));

        for (int layer = 0; layer < 3; layer++) {
            float layerSize = 0.3F + layer * 0.1F;
            float alpha = 1.0F - layer * 0.3F;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(age * (2.0F + layer)));
            poseStack.mulPose(Axis.XP.rotationDegrees(age * (1.5F + layer * 0.5F)));

            renderSphere(coreBuffer, poseStack.last().pose(), poseStack.last().normal(),
                    layerSize, alpha, 255, 200, 255, packedLight);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderLightningRings(EntityThunderpealOrb entity, float age) {
        Vec3 centerPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Level level = entity.level();

        for (int i = 0; i < 8; i++) {
            float angle1 = (age * 0.1F + i * 45.0F) * (float) Math.PI / 180.0F;
            float angle2 = (age * 0.15F + i * 30.0F) * (float) Math.PI / 180.0F;

            float radius = 0.8F;

            float x = radius * Mth.cos(angle1) * Mth.cos(angle2);
            float y = radius * Mth.sin(angle2);
            float z = radius * Mth.sin(angle1) * Mth.cos(angle2);

            Vec3 ringPos = centerPos.add(x, y, z);

            if (entity.tickCount % 4 == i % 4) {
                RelicsEventHandler.imposeLightning(
                        level,
                        centerPos.x, centerPos.y, centerPos.z,
                        ringPos.x, ringPos.y, ringPos.z,
                        5, 0.5F, 8,
                        entity.tickCount + i, 0.04F
                );
            }

            if (entity.tickCount % 12 == 0 && i % 2 == 0) {
                int nextI = (i + 1) % 8;
                float nextAngle1 = (age * 0.1F + nextI * 45.0F) * (float) Math.PI / 180.0F;
                float nextAngle2 = (age * 0.15F + nextI * 30.0F) * (float) Math.PI / 180.0F;

                float nextX = radius * Mth.cos(nextAngle1) * Mth.cos(nextAngle2);
                float nextY = radius * Mth.sin(nextAngle2);
                float nextZ = radius * Mth.sin(nextAngle1) * Mth.cos(nextAngle2);

                Vec3 nextRingPos = centerPos.add(nextX, nextY, nextZ);

                RelicsEventHandler.imposeLightning(
                        level,
                        ringPos.x, ringPos.y, ringPos.z,
                        nextRingPos.x, nextRingPos.y, nextRingPos.z,
                        4, 0.4F, 6,
                        entity.tickCount + i + 100, 0.03F
                );
            }
        }
    }

    private void renderSphere(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal,
                              float radius, float alpha, int red, int green, int blue,
                              int packedLight) {
        int segments = 12;

        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                float theta1 = (float) (i * Math.PI / segments);
                float theta2 = (float) ((i + 1) * Math.PI / segments);
                float phi1 = (float) (j * 2 * Math.PI / segments);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / segments);

                // FIX: QUADS 模式 — 每面4个顶点，不是6个
                addSphereVertex(buffer, matrix, normal, radius, theta1, phi1, red, green, blue, alpha, packedLight);
                addSphereVertex(buffer, matrix, normal, radius, theta2, phi1, red, green, blue, alpha, packedLight);
                addSphereVertex(buffer, matrix, normal, radius, theta2, phi2, red, green, blue, alpha, packedLight);
                addSphereVertex(buffer, matrix, normal, radius, theta1, phi2, red, green, blue, alpha, packedLight);
            }
        }
    }

    private void addSphereVertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal,
                                 float radius, float theta, float phi,
                                 int red, int green, int blue, float alpha, int packedLight) {
        float x = radius * Mth.sin(theta) * Mth.cos(phi);
        float y = radius * Mth.cos(theta);
        float z = radius * Mth.sin(theta) * Mth.sin(phi);

        float nx = Mth.sin(theta) * Mth.cos(phi);
        float ny = Mth.cos(theta);
        float nz = Mth.sin(theta) * Mth.sin(phi);

        float u = phi / (2.0F * (float) Math.PI);
        float v = theta / (float) Math.PI;

        buffer.vertex(matrix, x, y, z)
                .color(red, green, blue, (int) (255 * alpha))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    private void renderBotaniaLightning(EntityThunderpealOrb entity, float age) {
        if (entity.tickCount % 8 == 0) {
            Vec3 centerPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            Level level = entity.level();
            RandomSource random = RandomSource.create(entity.tickCount);

            for (int i = 0; i < 4; i++) {
                double angle1 = random.nextDouble() * Math.PI * 2;
                double angle2 = (random.nextDouble() - 0.5) * Math.PI;
                double length = 1.5 + random.nextDouble() * 1.0;

                double x = Math.cos(angle1) * Math.cos(angle2) * length;
                double y = Math.sin(angle2) * length;
                double z = Math.sin(angle1) * Math.cos(angle2) * length;

                Vec3 endPos = centerPos.add(x, y, z);

                RelicsEventHandler.imposeLightning(
                        level,
                        centerPos.x, centerPos.y, centerPos.z,
                        endPos.x, endPos.y, endPos.z,
                        3, 0.5F, (int)(length * 1.6),
                        entity.tickCount + i, 0.04F
                );
            }

            if (entity.tickCount % 20 == 0) {
                for (int i = 0; i < 2; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double length = 2.5 + random.nextDouble() * 1.5;

                    Vec3 endPos = centerPos.add(
                            Math.cos(angle) * length,
                            (random.nextDouble() - 0.5) * length,
                            Math.sin(angle) * length
                    );

                    RelicsEventHandler.imposeLightning(
                            level,
                            centerPos.x, centerPos.y, centerPos.z,
                            endPos.x, endPos.y, endPos.z,
                            2, 0.4F, (int)(length * 1.6),
                            entity.tickCount + i + 50, 0.03F
                    );
                }
            }
        }
    }

    private void spawnTrailParticles(EntityThunderpealOrb entity, float age) {
        if (entity.tickCount % 2 == 0) {
            Level level = entity.level();

            SparkleParticleData purpleSparkle = SparkleParticleData.sparkle(
                    0.3F, 0.8F, 0.4F, 1.0F, 15);
            level.addParticle(purpleSparkle,
                    entity.getX(), entity.getY(), entity.getZ(),
                    0, 0, 0);

            SparkleParticleData whiteSparkle = SparkleParticleData.sparkle(
                    0.2F, 1.0F, 1.0F, 1.0F, 10);
            level.addParticle(whiteSparkle,
                    entity.getX(), entity.getY(), entity.getZ(),
                    0, 0, 0);

            RandomSource random = entity.random;
            for (int i = 0; i < 3; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.8;
                double offsetY = (random.nextDouble() - 0.5) * 0.8;
                double offsetZ = (random.nextDouble() - 0.5) * 0.8;

                SparkleParticleData lightPurpleSparkle = SparkleParticleData.sparkle(
                        0.15F, 0.9F, 0.7F, 1.0F, 8);
                level.addParticle(lightPurpleSparkle,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        0, 0, 0);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThunderpealOrb entity) {
        return BEAM_TEXTURE;
    }
}