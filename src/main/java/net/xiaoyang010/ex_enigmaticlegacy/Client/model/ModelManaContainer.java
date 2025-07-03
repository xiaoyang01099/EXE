package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;



public class ModelManaContainer extends Model {
    private final ModelPart bbMain;

    public ModelManaContainer(ModelPart root) {
        super(RenderType::entitySolid);
        this.bbMain = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bbMain = partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(6, 14).addBox(-7.0F, -15.0F, -7.0F, 14.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 0).addBox(-5.0F, -10.2F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(49, 37).addBox(-6.0F, -10.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 26).addBox(4.0F, -10.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(13, 23).addBox(-2.0F, -10.0F, -6.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 23).addBox(-2.0F, -10.0F, 4.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(27, 23).addBox(-5.0F, -18.0F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 14).addBox(-3.0F, -6.2F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(55, 0).addBox(-6.0F, -17.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 28).addBox(4.0F, -17.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(55, 5).addBox(4.0F, -17.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 23).addBox(-6.0F, -17.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 0).addBox(4.0F, -18.0F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 5).addBox(-4.0F, -18.0F, 4.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 0).addBox(-4.0F, -18.0F, -5.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(14, 38).addBox(-7.0F, -15.0F, 4.0F, 14.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(41, 48).addBox(4.0F, -15.0F, -4.0F, 3.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 48).addBox(-7.0F, -15.0F, -4.0F, 3.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bbMain.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart modelPart, float x, float y, float z) {
        modelPart.xRot = x;
        modelPart.yRot = y;
        modelPart.zRot = z;
    }
}