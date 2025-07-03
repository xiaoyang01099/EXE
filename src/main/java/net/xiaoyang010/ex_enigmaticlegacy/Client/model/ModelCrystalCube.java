package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class ModelCrystalCube extends Model {
    public final ModelPart cube;
    public final ModelPart base1;
    public final ModelPart base2;

    public ModelCrystalCube() {
        super(RenderType::entityTranslucentCull);

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition base1Def = partdefinition.addOrReplaceChild("base1", CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-3.0F, 7.0F, -3.0F, 6.0F, 1.0F, 6.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        base1Def.addOrReplaceChild("base2", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-5.0F, 3.0F, -5.0F, 10.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        ModelPart root = LayerDefinition.create(meshdefinition, 48, 32)
                .bakeRoot();

        this.cube = root.getChild("cube");
        this.base1 = root.getChild("base1");
        this.base2 = this.base1.getChild("base2");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.base1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 1.0F);
        this.cube.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void renderBase(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.base1.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderCube(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float alpha) {
        this.cube.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
    }

    public void setRotationAngle(ModelPart modelPart, float x, float y, float z) {
        modelPart.xRot = x;
        modelPart.yRot = y;
        modelPart.zRot = z;
    }
}