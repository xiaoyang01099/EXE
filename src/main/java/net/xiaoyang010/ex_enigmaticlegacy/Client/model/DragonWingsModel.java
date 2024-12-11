package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class DragonWingsModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("ex_enigmaticlegacy", "dragon_wings"), "main");

    private final ModelPart wing;
    private final ModelPart wingTip;
    private final float scale;
    private LivingEntity currentEntity;

    public DragonWingsModel(ModelPart root, float scale) {
        super(root);
        this.scale = scale;
        this.wing = root.getChild("body").getChild("wing");
        this.wingTip = this.wing.getChild("wingtip");
    }

    public void setEntity(LivingEntity entity) {
        this.currentEntity = entity;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.getChild("body");

        PartDefinition wing = body.addOrReplaceChild("wing", CubeListBuilder.create()
                        .texOffs(112, 88).addBox(-56.0F, -4.0F, -4.0F, 56.0F, 8.0F, 8.0F)
                        .texOffs(-56, 88).addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(-12.0F, 5.0F, 2.0F));

        wing.addOrReplaceChild("wingtip", CubeListBuilder.create()
                        .texOffs(112, 136).addBox(-56.0F, -2.0F, -2.0F, 56.0F, 4.0F, 4.0F)
                        .texOffs(-56, 144).addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 56.0F),
                PartPose.offset(-56.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.0F, 0.0F);

        boolean isGliding = false;
        if (this.currentEntity != null) {
            isGliding = currentEntity.getFallFlyingTicks() > 4;
        }

        poseStack.translate(0.0F, -1.0F, 0.0F);

        for (int j = 0; j < 2; ++j) {
            poseStack.pushPose();
            if (isGliding) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(j == 0 ? 270.0F : 0.0F));
            }

            float f11 = this.swimAmount * ((float)Math.PI / 10F);
            this.wing.xRot = 0.125F - (float)Math.cos(f11) * 0.2F;
            this.wing.yRot = 0.25F;
            this.wing.zRot = (float)(Math.sin(f11) + 0.125F) * 0.8F;
            this.wingTip.zRot = -((float)(Math.sin(f11 + 2.0F) + 0.5F)) * 0.75F;

            if (j == 0) {
                this.wing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                poseStack.scale(-1.0F, 1.0F, 1.0F);
            } else {
                this.wing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}