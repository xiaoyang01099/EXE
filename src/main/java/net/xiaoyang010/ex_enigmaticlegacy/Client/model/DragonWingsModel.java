package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class DragonWingsModel extends EntityModel<LivingEntity> {
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;

    public DragonWingsModel(ModelPart root) {
        this.rightWing = root.getChild("right_wing");
        this.rightWingTip = rightWing.getChild("right_wingtip");
        this.leftWing = root.getChild("left_wing");
        this.leftWingTip = leftWing.getChild("left_wingtip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 右翼 - 只修改position
        PartDefinition rightWing = partdefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(112, 88)
                        .addBox(-56.0F, -4.0F, -4.0F, 56.0F, 8.0F, 8.0F)
                        .texOffs(-56, 88)
                        .addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                // 调整基础位置到玩家背后
                PartPose.offset(-8.0F, 5.0F, 8.0F));
        // 右翼尖保持不变
        rightWing.addOrReplaceChild("right_wingtip",
                CubeListBuilder.create()
                        .texOffs(112, 136)
                        .addBox(-56.0F, -2.0F, -2.0F, 56.0F, 4.0F, 4.0F)
                        .texOffs(-56, 144)
                        .addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(-56.0F, 0.0F, 0.0F));

        // 左翼 - 只修改position
        PartDefinition leftWing = partdefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(112, 88)
                        .addBox(0.0F, -4.0F, -4.0F, 56.0F, 8.0F, 8.0F)
                        .texOffs(-56, 88)
                        .addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                // 调整基础位置到玩家背后
                PartPose.offset(8.0F, 5.0F, 8.0F));

        // 左翼尖保持不变
        leftWing.addOrReplaceChild("left_wingtip",
                CubeListBuilder.create()
                        .texOffs(112, 136)
                        .addBox(0.0F, -2.0F, -2.0F, 56.0F, 4.0F, 4.0F)
                        .texOffs(-56, 144)
                        .addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(56.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float f = ((float)Math.PI * 2F);
        float wingAnimation = (float)Math.cos(ageInTicks * 0.1F) * 0.1F;

        // 右翼动画
        this.rightWing.zRot = 0.125F - wingAnimation;
        this.rightWingTip.zRot = -((float)(Math.sin(ageInTicks * 0.15F) + 0.5F)) * 0.75F;

        // 左翼动画 (镜像右翼)
        this.leftWing.zRot = -0.125F + wingAnimation;
        this.leftWingTip.zRot = ((float)(Math.sin(ageInTicks * 0.15F) + 0.5F)) * 0.75F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        // 在渲染时稍微调整整体位置
        poseStack.pushPose();
        // 向后移动一点点以确保翅膀从背部延伸
        poseStack.translate(0, 0, 0.2);

        rightWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }
}