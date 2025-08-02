package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityThunderpealOrb;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.proxy.IProxy;

import java.util.Random;

public class ThunderpealOrbRenderer extends EntityRenderer<EntityThunderpealOrb> {

    // 使用原版的末影水晶光束纹理
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");

    public ThunderpealOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(EntityThunderpealOrb entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        // 获取实体年龄用于动画
        float age = entity.tickCount + partialTick;

        // 渲染核心球体
        renderEnergyCore(poseStack, bufferSource, age, packedLight);

        // 渲染环绕的雷电效果 - 改为植物魔法闪电
        if (entity.level.isClientSide) {
            renderLightningRings(entity, age);
        }

        // 添加植物魔法闪电效果
        if (entity.level.isClientSide) {
            renderBotaniaLightning(entity, age);
        }

        // 添加粒子效果
        if (entity.level.isClientSide) {
            spawnTrailParticles(entity, age);
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderEnergyCore(PoseStack poseStack, MultiBufferSource bufferSource,
                                  float age, int packedLight) {
        poseStack.pushPose();

        // 核心球体的脉动效果
        float pulse = 0.8F + 0.2F * Mth.sin(age * 0.3F);
        poseStack.scale(pulse, pulse, pulse);

        // 使用半透明的发光渲染类型
        VertexConsumer coreBuffer = bufferSource.getBuffer(RenderType.energySwirl(BEAM_TEXTURE, 0, 0));

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // 渲染多层球体
        for (int layer = 0; layer < 3; layer++) {
            float layerSize = 0.3F + layer * 0.1F;
            float alpha = 1.0F - layer * 0.3F;

            // 每层有不同的旋转
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(age * (2.0F + layer)));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(age * (1.5F + layer * 0.5F)));

            renderSphere(coreBuffer, poseStack.last().pose(), poseStack.last().normal(),
                    layerSize, alpha, 255, 200, 255, packedLight); // 紫白色

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    // 改为使用植物魔法闪电的环绕效果
    private void renderLightningRings(EntityThunderpealOrb entity, float age) {
        Vec3 centerPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());

        // 环绕球体的闪电效果
        for (int i = 0; i < 8; i++) {
            // 计算环绕位置
            float angle1 = (age * 0.1F + i * 45.0F) * (float)Math.PI / 180.0F;
            float angle2 = (age * 0.15F + i * 30.0F) * (float)Math.PI / 180.0F;

            // 环绕半径
            float radius = 0.8F;

            // 计算环绕点位置
            float x = radius * Mth.cos(angle1) * Mth.cos(angle2);
            float y = radius * Mth.sin(angle2);
            float z = radius * Mth.sin(angle1) * Mth.cos(angle2);

            Vec3 ringPos = centerPos.add(x, y, z);

            // 从中心到环绕点的闪电
            if (entity.tickCount % 4 == i % 4) { // 错开时间，避免同时闪烁
                int outerColor = 0xFF4400AA; // 深紫色
                int innerColor = 0xFFBBBBFF; // 淡蓝白色

                IProxy.INSTANCE.lightningFX(centerPos, ringPos, 5.0F, outerColor, innerColor);
            }

            // 偶尔在环绕点之间产生闪电
            if (entity.tickCount % 12 == 0 && i % 2 == 0) {
                // 下一个环绕点
                int nextI = (i + 1) % 8;
                float nextAngle1 = (age * 0.1F + nextI * 45.0F) * (float)Math.PI / 180.0F;
                float nextAngle2 = (age * 0.15F + nextI * 30.0F) * (float)Math.PI / 180.0F;

                float nextX = radius * Mth.cos(nextAngle1) * Mth.cos(nextAngle2);
                float nextY = radius * Mth.sin(nextAngle2);
                float nextZ = radius * Mth.sin(nextAngle1) * Mth.cos(nextAngle2);

                Vec3 nextRingPos = centerPos.add(nextX, nextY, nextZ);

                // 环绕点之间的闪电
                int outerColor = 0xFF6600CC; // 中紫色
                int innerColor = 0xFFFFFFFF; // 白色

                IProxy.INSTANCE.lightningFX(ringPos, nextRingPos, 4.0F, outerColor, innerColor);
            }
        }
    }

    private void renderSphere(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal,
                              float radius, float alpha, int red, int green, int blue, int packedLight) {

        int segments = 12; // 球体精度

        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                float theta1 = (float) (i * Math.PI / segments);
                float theta2 = (float) ((i + 1) * Math.PI / segments);
                float phi1 = (float) (j * 2 * Math.PI / segments);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / segments);

                // 四个顶点构成一个四边形
                addSphereVertex(buffer, matrix, normal, radius, theta1, phi1, red, green, blue, alpha, packedLight);
                addSphereVertex(buffer, matrix, normal, radius, theta2, phi1, red, green, blue, alpha, packedLight);
                addSphereVertex(buffer, matrix, normal, radius, theta2, phi2, red, green, blue, alpha, packedLight);

                addSphereVertex(buffer, matrix, normal, radius, theta1, phi1, red, green, blue, alpha, packedLight);
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

        // 法向量就是位置向量归一化
        float nx = Mth.sin(theta) * Mth.cos(phi);
        float ny = Mth.cos(theta);
        float nz = Mth.sin(theta) * Mth.sin(phi);

        // UV坐标
        float u = phi / (2.0F * (float)Math.PI);
        float v = theta / (float)Math.PI;

        buffer.vertex(matrix, x, y, z)
                .color(red, green, blue, (int)(255 * alpha))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    // 外射闪电效果
    private void renderBotaniaLightning(EntityThunderpealOrb entity, float age) {
        // 每隔几tick生成植物魔法闪电效果
        if (entity.tickCount % 8 == 0) {
            Vec3 centerPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            Random random = new Random(entity.tickCount);

            // 生成多道从中心向外的闪电
            for (int i = 0; i < 4; i++) {
                // 随机方向
                double angle1 = random.nextDouble() * Math.PI * 2;
                double angle2 = (random.nextDouble() - 0.5) * Math.PI;

                // 闪电长度
                double length = 1.5 + random.nextDouble() * 1.0;

                // 计算终点位置
                double x = Math.cos(angle1) * Math.cos(angle2) * length;
                double y = Math.sin(angle2) * length;
                double z = Math.sin(angle1) * Math.cos(angle2) * length;

                Vec3 endPos = centerPos.add(x, y, z);

                // 颜色配置：外层紫色，内层白色
                int outerColor = 0xFF6600FF; // 紫色
                int innerColor = 0xFFFFFFFF; // 白色

                // 使用植物魔法的闪电效果
                IProxy.INSTANCE.lightningFX(centerPos, endPos, 3.0F, outerColor, innerColor);
            }

            // 偶尔生成更长的闪电
            if (entity.tickCount % 20 == 0) {
                for (int i = 0; i < 2; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double length = 2.5 + random.nextDouble() * 1.5;

                    Vec3 endPos = centerPos.add(
                            Math.cos(angle) * length,
                            (random.nextDouble() - 0.5) * length,
                            Math.sin(angle) * length
                    );

                    // 更亮的颜色用于长闪电
                    int outerColor = 0xFF9900FF; // 亮紫色
                    int innerColor = 0xFFFFFFFF; // 白色

                    IProxy.INSTANCE.lightningFX(centerPos, endPos, 2.0F, outerColor, innerColor);
                }
            }
        }
    }

    private void spawnTrailParticles(EntityThunderpealOrb entity, float age) {
        // 使用植物魔法的粒子效果创建轨迹
        if (entity.tickCount % 2 == 0) { // 每2tick生成一次粒子
            BotaniaAPI api = BotaniaAPI.instance();

            // 主要的紫色粒子
            api.sparkleFX(entity.level,
                    entity.getX(), entity.getY(), entity.getZ(),
                    0.8F, 0.4F, 1.0F, // 紫色
                    0.3F, 15);

            // 白色核心粒子
            api.sparkleFX(entity.level,
                    entity.getX(), entity.getY(), entity.getZ(),
                    1.0F, 1.0F, 1.0F, // 白色
                    0.2F, 10);

            // 额外的雷电效果粒子
            for (int i = 0; i < 3; i++) {
                double offsetX = (entity.random.nextDouble() - 0.5) * 0.8;
                double offsetY = (entity.random.nextDouble() - 0.5) * 0.8;
                double offsetZ = (entity.random.nextDouble() - 0.5) * 0.8;

                api.sparkleFX(entity.level,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        0.9F, 0.7F, 1.0F, // 淡紫色
                        0.15F, 8);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThunderpealOrb entity) {
        return BEAM_TEXTURE;
    }
}