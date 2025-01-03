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
import net.minecraft.world.entity.player.Player;

public class DragonWingsModel extends EntityModel<LivingEntity> {
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;

    public DragonWingsModel(ModelPart root) {
        this.rightWing = root.getChild("right_wing");
        this.rightWingTip = rightWing.getChild("right_wing_tip");
        this.leftWing = root.getChild("left_wing");
        this.leftWingTip = leftWing.getChild("left_wing_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition left = partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().addBox("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), PartPose.offset(12.0F, 5.0F, 2.0F));
        left.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror().addBox("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), PartPose.offset(56.0F, 0.0F, 0.0F));

        PartDefinition right = partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), PartPose.offset(-12.0F, 5.0F, 2.0F));
        right.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), PartPose.offset(-56.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof Player player){
//            boolean flying = player.getAbilities().flying;
//            boolean fallFlying = player.isFallFlying();
//            if (flying || fallFlying){
                float wingAnimation = (float)Math.cos(ageInTicks * 0.1F) * 0.1F;
                // 右翼动画
                this.rightWing.zRot = 0.125F - wingAnimation;
                this.rightWingTip.zRot = -((float)(Math.sin(ageInTicks * 0.15F) + 0.5F)) * 0.75F;

                // 左翼动画 (镜像右翼)
                this.leftWing.zRot = -0.125F + wingAnimation;
                this.leftWingTip.zRot = ((float)(Math.sin(ageInTicks * 0.15F) + 0.5F)) * 0.75F;
//            }
        }

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