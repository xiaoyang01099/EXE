package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class ModelEngineerHopper extends Model {
    private final ModelPart hopperBase;
    private final ModelPart hopperTop;
    private final ModelPart hopperBottom;
    private final ModelPart root;

    public ModelEngineerHopper(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.hopperBase = root.getChild("hopper_base");
        this.hopperTop = root.getChild("hopper_top");
        this.hopperBottom = root.getChild("hopper_bottom");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition hopperBase = partdefinition.addOrReplaceChild("hopper_base", CubeListBuilder.create().texOffs(32, 45).addBox(5.0F, -5.0F, -7.0F, 2.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(12, 27).addBox(-5.0F, -2.8F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(52, 38).addBox(4.0F, 0.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-6.0F, 0.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-2.0F, -0.1716F, -6.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 49).addBox(-2.0F, 0.0F, 4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 45).addBox(-7.0F, -5.0F, -7.0F, 2.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(20, 50).addBox(-5.0F, -5.0F, 5.0F, 10.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 42).addBox(-5.0F, -5.0F, -7.0F, 10.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition hopperTop = partdefinition.addOrReplaceChild("hopper_top", CubeListBuilder.create().texOffs(20, 13).addBox(-3.0F, 3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition hopperBottom = partdefinition.addOrReplaceChild("hopper_bottom", CubeListBuilder.create().texOffs(24, 3).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 30.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void renderHopper(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, double time) {
        float offset = (float)Math.sin(time / 10.0F) * 0.05F + 0.01F;

        float alphaTop = (float)(Math.sin(time / 20.0F) * 0.2F + 0.6F);
        float alphaBottom = (float)(Math.cos(time / 25.0F) * 0.2F + 0.5F);

        this.hopperBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0.0F, offset, 0.0F);
        this.hopperTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alphaTop);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, -offset, 0.0F);
        this.hopperBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alphaBottom);
        poseStack.popPose();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.hopperBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.hopperTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha * 0.6F);
        this.hopperBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha * 0.6F);
    }
}