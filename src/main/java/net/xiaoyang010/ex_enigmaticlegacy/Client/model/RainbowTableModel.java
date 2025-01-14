package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RainbowTableModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("ex_enigmaticlegacy", "rainbow_table"), "main");
	private final ModelPart part;
	private final ModelPart main;

	public RainbowTableModel(ModelPart root) {
		this.part = root.getChild("part");
		this.main = root.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition part = partdefinition.addOrReplaceChild("part", CubeListBuilder.create().texOffs(36, 37).addBox(0.0F, -5.0F, 10.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(36, 22).addBox(0.0F, -5.0F, 0.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 46).addBox(-1.0F, -7.0F, 0.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 54).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 54).addBox(-2.0F, -9.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 54).addBox(9.0F, -9.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 54).addBox(9.0F, -9.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 54).addBox(-2.0F, -9.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 46).addBox(-2.0F, -8.0F, 0.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(36, 0).addBox(8.0F, -5.0F, 2.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 46).addBox(9.0F, -8.0F, 0.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 51).addBox(8.0F, -7.0F, 0.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 53).addBox(8.0F, -8.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 46).addBox(9.0F, -8.0F, 10.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(6, 51).addBox(8.0F, -7.0F, 10.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 46).addBox(8.0F, -8.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 9).addBox(-2.0F, -5.0F, 2.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(16, 50).addBox(-1.0F, -7.0F, 10.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 54).addBox(-1.0F, -8.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 46).addBox(-2.0F, -8.0F, 10.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 51).addBox(4.0F, -5.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 51).addBox(3.0F, -9.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 44).addBox(3.0F, -10.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 53).addBox(3.0F, -11.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 42).addBox(3.0F, -8.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 52).addBox(3.0F, -7.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 40).addBox(3.0F, -6.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 52).addBox(3.0F, -5.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 52).addBox(4.0F, -6.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 52).addBox(4.0F, -7.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(22, 52).addBox(4.0F, -8.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 51).addBox(4.0F, -9.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 52).addBox(4.0F, -10.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 53).addBox(4.0F, -11.0F, 6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 12.0F, -6.0F));

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 26).addBox(-5.0F, -3.0F, -3.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 26).addBox(-4.0F, -5.0F, -2.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(32, 26).addBox(-4.0F, -8.0F, -2.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 46).addBox(-3.0F, -9.0F, -1.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.25F))
		.texOffs(36, 40).addBox(-3.0F, -11.25F, -1.0F, 4.0F, 2.25F, 4.0F, new CubeDeformation(-0.25F))
		.texOffs(16, 46).addBox(-2.0F, -13.0F, 0.0F, 2.0F, 2.25F, 2.0F, new CubeDeformation(0.25F))
		.texOffs(0, 0).addBox(-6.0F, -14.0F, -3.0F, 10.0F, 1.0F, 8.0F, new CubeDeformation(0.35F))
		.texOffs(0, 37).addBox(4.0F, -15.0F, -3.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(36, 18).addBox(-6.0F, -15.0F, -4.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 9).addBox(-6.0F, -16.0F, -3.0F, 10.0F, 1.0F, 8.0F, new CubeDeformation(0.1F))
		.texOffs(0, 18).addBox(-6.0F, -15.0F, -3.0F, 10.0F, 0.0F, 8.0F, new CubeDeformation(0.25F))
		.texOffs(36, 20).addBox(-6.0F, -15.0F, 5.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 37).addBox(-7.0F, -15.0F, -3.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 24.0F, -1.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		part.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}