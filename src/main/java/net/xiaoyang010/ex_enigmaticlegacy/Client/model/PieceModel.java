package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class PieceModel<T extends LivingEntity> extends HumanoidModel<T> {
	private final ModelPart ALL;
	private final ModelPart Q;
	private final ModelPart W;
	private final ModelPart E;
	private final ModelPart R;

	public PieceModel(ModelPart root) {
        super(root);
        this.ALL = root.getChild("ALL");
		this.Q = this.ALL.getChild("Q");
		this.W = this.ALL.getChild("W");
		this.E = this.ALL.getChild("E");
		this.R = this.ALL.getChild("R");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition Q = ALL.addOrReplaceChild("Q", CubeListBuilder.create().texOffs(50, 9).addBox(-3.15F, -0.525F, -0.625F, 4.75F, 0.5F, 1.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 5.0F, 8.0F, -1.5708F, -1.5708F, 0.0F));

		PartDefinition cube_r1 = Q.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 60).addBox(-2.2105F, 0.6F, 0.4986F, 2.2F, 0.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4783F, -1.075F, -2.1641F, 0.0F, 0.528F, 0.0F));

		PartDefinition cube_r2 = Q.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(14, 43).addBox(-1.95F, 0.5F, -0.5F, 3.525F, 0.5F, 1.35F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, -1.0F, -1.4F, 0.0F, 0.0873F, 0.0F));

		PartDefinition cube_r3 = Q.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(62, 57).addBox(0.5F, -0.5F, -0.6F, 2.7F, 0.5F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.981F, 0.0F, -1.1983F, 0.0F, 0.6545F, 0.0F));

		PartDefinition cube_r4 = Q.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.025F, -0.25F, -0.55F, 0.1F, 0.5F, 0.3F, new CubeDeformation(0.0F))
		.texOffs(66, 39).addBox(-0.1F, -0.249F, -0.35F, 0.175F, 0.5F, 0.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.789F, -0.25F, -3.0184F, 0.0F, -0.4974F, 0.0F));

		PartDefinition cube_r5 = Q.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(58, 64).addBox(-0.225F, 0.501F, -0.4F, 0.475F, 0.5F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(64, 28).addBox(-0.475F, 0.5F, 0.2F, 0.7F, 0.5F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4387F, -1.0F, -2.5827F, 0.0F, -0.4974F, 0.0F));

		PartDefinition cube_r6 = Q.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(64, 9).addBox(-1.25F, 0.5F, -0.3F, 1.95F, 0.5F, 0.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5325F, -0.975F, -2.6435F, 0.0F, 0.6545F, 0.0F));

		PartDefinition cube_r7 = Q.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(54, 40).addBox(0.725F, -0.5F, 0.05F, 1.075F, 0.5F, 3.55F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6779F, 0.025F, -2.5705F, 0.0F, -0.4363F, 0.0F));

		PartDefinition cube_r8 = Q.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(34, 26).addBox(0.0F, -0.225F, -2.9F, 0.1F, 0.475F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8365F, -0.25F, -1.0424F, 0.0F, -0.473F, 0.0F));

		PartDefinition cube_r9 = Q.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(60, 13).addBox(-0.1F, -0.25F, -3.0F, 0.4F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7094F, -0.2F, 1.8577F, 0.0F, -0.2967F, 0.0F));

		PartDefinition cube_r10 = Q.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(8, 64).addBox(-0.425F, -0.25F, -0.2125F, 1.85F, 0.5F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0662F, -0.25F, 1.0495F, 0.0F, -0.1571F, 0.0F));

		PartDefinition cube_r11 = Q.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(62, 60).addBox(-1.8F, 0.5F, -0.6F, 1.85F, 0.5F, 0.625F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9544F, -0.975F, 1.9084F, 0.0F, -0.2618F, 0.0F));

		PartDefinition W = ALL.addOrReplaceChild("W", CubeListBuilder.create().texOffs(58, 47).addBox(-5.375F, -0.5F, -3.0F, 4.275F, 0.5F, 0.3F, new CubeDeformation(0.0F))
		.texOffs(48, 6).addBox(-4.475F, -0.45F, -2.7F, 3.375F, 0.45F, 2.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 4.0F, 8.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r12 = W.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 64).addBox(-1.35F, 0.475F, -0.9375F, 1.35F, 0.525F, 0.875F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.95F, 0.525F, -0.4375F, 2.15F, 0.425F, 0.275F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0091F, -1.0F, 2.1203F, 0.0F, 0.3316F, 0.0F));

		PartDefinition cube_r13 = W.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-1.475F, 0.5F, -0.1375F, 1.675F, 0.475F, 0.275F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3543F, -1.0F, 1.9378F, 0.0F, 0.5978F, 0.0F));

		PartDefinition cube_r14 = W.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(60, 23).addBox(0.0625F, 0.5F, -2.825F, 0.875F, 0.475F, 2.475F, new CubeDeformation(0.0F))
		.texOffs(60, 0).addBox(-0.1375F, 0.5F, -3.125F, 0.275F, 0.5F, 3.275F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4224F, -1.0F, -0.0206F, 0.0F, 0.3534F, 0.0F));

		PartDefinition cube_r15 = W.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(20, 63).addBox(-2.0375F, 0.525F, 0.25F, 0.375F, 0.45F, 1.6F, new CubeDeformation(0.0F))
		.texOffs(36, 61).addBox(-2.2375F, 0.5F, -0.45F, 0.275F, 0.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4455F, -1.0F, 1.0262F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r16 = W.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(48, 23).addBox(-3.6F, 0.5F, -2.5375F, 4.225F, 0.525F, 2.175F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9746F, -1.0F, 2.7014F, 0.0F, -0.0873F, 0.0F));

		PartDefinition cube_r17 = W.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(60, 4).addBox(-0.15F, -0.25F, -0.4125F, 4.1F, 0.5F, 0.55F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7103F, -0.25F, 2.3745F, 0.0F, -0.096F, 0.0F));

		PartDefinition cube_r18 = W.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 0).addBox(-2.6F, 0.525F, 0.4125F, 2.7F, 0.475F, 0.225F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2439F, -1.0F, 2.3393F, 0.0F, -0.3665F, 0.0F));

		PartDefinition cube_r19 = W.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(12, 62).addBox(-2.9F, 0.5F, -0.5875F, 2.7F, 0.5F, 0.575F, new CubeDeformation(0.0F))
		.texOffs(60, 25).addBox(-3.3F, 0.5F, -0.0875F, 3.4F, 0.5F, 0.275F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.282F, -1.0F, 2.0453F, 0.0F, 0.2269F, 0.0F));

		PartDefinition cube_r20 = W.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(54, 26).addBox(-0.15F, 0.5F, -0.2F, 0.3F, 0.5F, 4.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2493F, -1.0F, -1.1781F, 0.0F, 0.7505F, 0.0F));

		PartDefinition cube_r21 = W.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(58, 22).addBox(-0.3F, 0.5F, 0.05F, 4.6F, 0.5F, 0.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.801F, -1.0F, -2.9444F, 0.0F, -0.3491F, 0.0F));

		PartDefinition cube_r22 = W.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(40, 65).addBox(0.5125F, -0.25F, 0.35F, 0.475F, 0.5F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(60, 6).addBox(-0.9375F, -0.25F, -0.45F, 1.525F, 0.5F, 2.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4429F, -0.25F, 1.1041F, 0.0F, -0.3491F, 0.0F));

		PartDefinition cube_r23 = W.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(54, 32).addBox(-0.05F, -0.2F, -1.8875F, 1.4F, 0.4F, 3.775F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4429F, -0.25F, 1.1041F, 0.0F, 0.7505F, 0.0F));

		PartDefinition cube_r24 = W.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(32, 9).addBox(-1.4375F, -0.225F, -2.6F, 4.675F, 0.45F, 3.775F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6275F, -0.25F, 0.1784F, 0.0F, -0.3316F, 0.0F));

		PartDefinition E = ALL.addOrReplaceChild("E", CubeListBuilder.create(), PartPose.offsetAndRotation(6.0F, -6.0F, -13.0F, -1.5708F, -1.5708F, 0.0F));

		PartDefinition cube_r25 = E.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(66, 16).addBox(3.0F, -0.25F, 0.1F, 1.0F, 0.5F, 0.1F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.5F, -0.225F, -1.0F, 0.9F, 0.45F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.7F, -0.2F, -0.6F, 4.6F, 0.45F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.6F, -0.225F, -0.2F, 5.0F, 0.475F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3744F, -0.25F, 5.5145F, 0.0F, -0.7156F, 0.0F));

		PartDefinition cube_r26 = E.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(22, 59).addBox(-0.2F, -0.25F, -1.15F, 0.4F, 0.5F, 3.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9449F, -0.25F, 2.8043F, 0.0F, 0.5934F, 0.0F));

		PartDefinition cube_r27 = E.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(44, 61).addBox(-0.2F, -0.25F, -0.75F, 0.4F, 0.5F, 2.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2015F, -0.25F, 0.4654F, 0.0F, 0.4189F, 0.0F));

		PartDefinition cube_r28 = E.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(54, 62).addBox(-0.2F, -0.25F, -0.55F, 0.4F, 0.5F, 1.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8444F, -0.25F, -1.2953F, 0.0F, 0.3054F, 0.0F));

		PartDefinition cube_r29 = E.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(40, 61).addBox(-0.2F, -0.25F, -1.2F, 0.4F, 0.5F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1801F, -0.25F, -2.9761F, 0.0F, 0.1484F, 0.0F));

		PartDefinition cube_r30 = E.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(4, 64).addBox(-0.2F, -0.25F, 0.65F, 0.4F, 0.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0763F, -0.25F, -6.2388F, 0.0F, -0.1309F, 0.0F));

		PartDefinition cube_r31 = E.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(34, 63).addBox(0.2F, -0.25F, 0.15F, 0.4F, 0.5F, 0.9F, new CubeDeformation(0.0F))
		.texOffs(32, 13).addBox(-0.2F, -0.25F, -0.05F, 0.4F, 0.5F, 1.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5112F, -0.25F, -6.6896F, 0.0F, -0.5236F, 0.0F));

		PartDefinition cube_r32 = E.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(38, 51).addBox(-0.2F, -0.25F, -0.45F, 0.4F, 0.5F, 1.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0584F, -0.25F, -7.1511F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r33 = E.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(42, 64).addBox(-0.45F, -0.25F, -0.2F, 0.9F, 0.5F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3928F, -0.225F, -7.5511F, 0.0F, 0.2967F, 0.0F));

		PartDefinition cube_r34 = E.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(64, 42).addBox(-0.65F, -0.25F, -0.2F, 0.9F, 0.5F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4858F, -0.25F, -7.4542F, 0.0F, -0.3665F, 0.0F));

		PartDefinition cube_r35 = E.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 0).addBox(-0.2F, -0.5F, 7.075F, 0.1F, 0.5F, 0.2F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.3F, -0.5F, 6.975F, 0.2F, 0.5F, 0.15F, new CubeDeformation(0.0F))
		.texOffs(64, 40).addBox(-0.1F, -0.5F, 6.5F, 0.1F, 0.5F, 0.95F, new CubeDeformation(0.0F))
		.texOffs(62, 38).addBox(-3.2F, -0.5F, 1.3F, 1.2F, 0.5F, 1.175F, new CubeDeformation(0.0F))
		.texOffs(24, 63).addBox(-1.2F, -0.475F, 4.4F, 0.8F, 0.5F, 1.275F, new CubeDeformation(0.0F))
		.texOffs(28, 56).mirror().addBox(-2.0F, -0.5F, 1.3F, 1.6F, 0.5F, 3.175F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(32, 64).addBox(-1.8F, -0.5F, -9.2F, 1.2F, 0.5F, 0.275F, new CubeDeformation(0.0F))
		.texOffs(48, 61).addBox(-2.5F, -0.475F, -9.0F, 2.1F, 0.5F, 1.075F, new CubeDeformation(0.0F))
		.texOffs(20, 52).addBox(-4.1F, -0.5F, -5.8F, 0.4F, 0.5F, 4.475F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-3.9F, -0.5F, -7.3F, 0.4F, 0.5F, 7.675F, new CubeDeformation(0.0F))
		.texOffs(0, 29).addBox(-3.5F, -0.45F, -8.0F, 3.1F, 0.45F, 9.375F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.4F, -0.5F, -9.0F, 0.4F, 0.5F, 15.975F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, 0.0F, 1.1F, 0.0F, 0.2793F, 0.0F));

		PartDefinition R = ALL.addOrReplaceChild("R", CubeListBuilder.create().texOffs(0, 17).addBox(-2.1F, -0.45F, -5.8F, 5.3F, 0.4F, 11.6F, new CubeDeformation(0.0F))
		.texOffs(40, 47).addBox(-3.0F, -0.4F, 5.7F, 7.7F, 0.4F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(60, 17).addBox(0.4092F, -0.425F, -6.7084F, 2.875F, 0.4F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 2.0F, -7.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r36 = R.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(60, 48).addBox(-0.95F, -0.2F, -0.1875F, 0.6F, 0.4F, 2.375F, new CubeDeformation(0.0F))
		.texOffs(54, 36).addBox(-1.25F, -0.2F, -2.2875F, 0.3F, 0.4F, 4.475F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2264F, -0.225F, 3.9106F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r37 = R.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(48, 13).addBox(-0.5F, -0.2F, -1.4875F, 1.0F, 0.4F, 4.775F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.305F, -0.2F, -0.9581F, 0.0F, 0.2094F, 0.0F));

		PartDefinition cube_r38 = R.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(38, 58).addBox(-0.5F, -0.2F, -2.0875F, 1.0F, 0.4F, 3.375F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0963F, -0.225F, -3.3278F, 0.0F, 0.5061F, 0.0F));

		PartDefinition cube_r39 = R.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(12, 59).addBox(-2.6875F, -0.2F, 0.4F, 1.525F, 0.4F, 2.9F, new CubeDeformation(0.0F))
		.texOffs(50, 12).addBox(-3.7176F, -0.2F, -0.5F, 5.825F, 0.4F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9324F, -0.2F, -5.9897F, 0.0F, 0.1571F, 0.0F));

		PartDefinition cube_r40 = R.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(32, 15).addBox(-0.825F, -0.2F, 1.95F, 0.3F, 0.4F, 1.3F, new CubeDeformation(0.0F))
		.texOffs(34, 13).addBox(-0.525F, -0.15F, -2.05F, 0.9F, 0.4F, 5.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.29F, -0.225F, 2.491F, 0.0F, 0.2182F, 0.0F));

		PartDefinition cube_r41 = R.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(48, 53).addBox(-0.5F, -0.2F, -1.0F, 0.8F, 0.4F, 4.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5008F, -0.2F, -2.6967F, 0.0F, -0.1658F, 0.0F));

		PartDefinition cube_r42 = R.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(62, 36).addBox(0.0F, -0.175F, -0.2F, 0.6F, 0.35F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3239F, -0.2F, -5.259F, 0.0F, -0.5585F, 0.0F));

		PartDefinition cube_r43 = R.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(46, 59).addBox(-1.3F, -0.175F, -0.3F, 2.3F, 0.4F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0109F, -0.225F, -5.5895F, 0.0F, -0.7679F, 0.0F));

		PartDefinition cube_r44 = R.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(54, 59).addBox(-0.3F, -0.2F, -2.85F, 0.6F, 0.4F, 3.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8089F, -0.2F, 5.925F, 0.0F, 0.5061F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		ALL.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}