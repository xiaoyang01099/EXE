package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class ModelDiceFate extends Model {
    private final ModelPart bb_main;

    public ModelDiceFate(ModelPart root) {
        super(RenderType::entitySolid);
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float rotX, float rotY, float rotZ) {
        this.setRotationAngles(rotX, rotY, rotZ);
        this.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void setRotationAngles(float rotX, float rotY, float rotZ) {
        this.bb_main.xRot = this.getAngle(rotX);
        this.bb_main.yRot = this.getAngle(rotY);
        this.bb_main.zRot = this.getAngle(rotZ);
    }

    private float getAngle(float rot) {
        return rot / 180.0F * (float) Math.PI;
    }
}