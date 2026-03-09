package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@OnlyIn(Dist.CLIENT)
public class NebulaArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "armor_nebula"), "main");
	public static final float OVERLAY_SCALE = 0.25F;
	public static final float HAT_OVERLAY_SCALE = 0.5F;
	private static final float SPYGLASS_ARM_ROT_Y = 0.2617994F;
	private static final float SPYGLASS_ARM_ROT_X = 1.9198622F;
	private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994F;
	private final ModelPart head;
	public final ModelPart hat;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	public ArmPose leftArmPose;
	public ArmPose rightArmPose;
	public boolean crouching;
	public float swimAmount;

	public NebulaArmorModel(ModelPart root) {
		super(root);
		this.leftArmPose = ArmPose.EMPTY;
		this.rightArmPose = ArmPose.EMPTY;
		this.head = root.getChild("head");
		this.hat = root.getChild("hat");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
				.texOffs(32, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F + 0, 0.0F));


		PartDefinition Head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.3F))
		.texOffs(0, 10).addBox(-4.0F, -12.0F, -4.1F, 8.0F, 3.0F, 7.0F, new CubeDeformation(-0.9F))
		.texOffs(30, 10).addBox(-4.0F, -9.0F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.4F))
		.texOffs(38, 35).addBox(-4.0F, -6.4F, -3.0F, 0.0F, 1.0F, 6.0F, new CubeDeformation(0.3F))
		.texOffs(26, 43).addBox(-4.0F, -5.8F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.3F))
		.texOffs(38, 35).mirror().addBox(4.0F, -6.4F, -3.0F, 0.0F, 1.0F, 6.0F, new CubeDeformation(0.3F)).mirror(false)
		.texOffs(30, 10).mirror().addBox(4.0F, -9.0F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.4F)).mirror(false)
		.texOffs(26, 43).mirror().addBox(4.0F, -5.8F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 7).addBox(-0.6F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(4.0F, -9.7F, 0.5F, -0.9617F, -0.0706F, 0.0514F));

		PartDefinition cube_r2 = Head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(42, 43).addBox(-0.4F, -1.8F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-0.1F, -10.7F, 2.6F, -1.7628F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -9.7F, 0.5F, -0.9617F, 0.0706F, -0.0514F));

		PartDefinition cube_r4 = Head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-0.1F, -10.2F, -3.8F, -0.3491F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(34, 7).mirror().addBox(-0.4F, -0.8F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(-3.9F, -10.6F, -4.5F, -0.3791F, 0.3922F, -0.1511F));

		PartDefinition cube_r6 = Head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(34, 7).addBox(-0.6F, -0.8F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.9F, -10.6F, -4.5F, -0.3791F, -0.3922F, 0.1511F));

		PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(24, 35).addBox(-3.0F, -0.9F, -4.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.5F))
		.texOffs(34, 43).addBox(-2.0F, 1.6F, -3.6F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.5F))
		.texOffs(0, 44).addBox(-1.0F, 2.8F, -3.6F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = Body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(46, 7).addBox(-0.6F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-3.1F, 1.4F, -3.9F, 0.305F, -0.1308F, -0.2363F));

		PartDefinition cube_r8 = Body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(3.1F, 1.4F, -3.9F, 0.305F, 0.1308F, 0.2363F));

		PartDefinition cube_r9 = Body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-0.1F, 4.3F, -4.8F, -0.2618F, 0.0F, 0.0F));

		PartDefinition RightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(0, 36).addBox(-3.0F, 7.2F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.45F))
		.texOffs(40, 20).addBox(-3.0F, 6.3F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition cube_r10 = RightArm.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(14, 36).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(0.0F, -2.2F, -1.4F, 0.5323F, 0.0F, 0.0698F));

		PartDefinition cube_r11 = RightArm.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(40, 28).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(-3.0F, -2.7F, -0.5F, 0.0F, 0.0F, 0.0698F));

		PartDefinition cube_r12 = RightArm.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(14, 43).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.8F, -1.8F, -0.5F, 0.0F, 0.0F, 0.0698F));

		PartDefinition LeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(0.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
		.texOffs(0, 36).mirror().addBox(0.0F, 7.2F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
		.texOffs(40, 20).mirror().addBox(1.0F, 6.3F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition cube_r13 = LeftArm.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(14, 36).mirror().addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.7F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.2F, -1.4F, 0.5323F, 0.0F, -0.0698F));

		PartDefinition cube_r14 = LeftArm.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(40, 28).mirror().addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.7F)).mirror(false), PartPose.offsetAndRotation(3.0F, -2.7F, -0.5F, 0.0F, 0.0F, -0.0698F));

		PartDefinition cube_r15 = LeftArm.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(14, 43).mirror().addBox(-2.0F, -1.0F, -1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(2.8F, -1.8F, -0.5F, 0.0F, 0.0F, -0.0698F));

		PartDefinition RightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 20).addBox(-2.3F, 1.2F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.2F))
		.texOffs(32, 0).addBox(-2.6F, 9.4F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition cube_r16 = RightLeg.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(32, 7).addBox(-0.6131F, -1.5F, 0.0194F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-3.0F, 9.5F, -2.7F, 1.6871F, -0.6573F, -1.752F));

		PartDefinition cube_r17 = RightLeg.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(4, 44).addBox(-0.1F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.9F, 11.3F, -2.1F, 0.2794F, 0.0026F, 0.0273F));

		PartDefinition cube_r18 = RightLeg.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(4, 44).addBox(-0.1F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.6F, 5.6F, -2.6F, -0.2794F, 0.0026F, 0.0273F));

		PartDefinition cube_r19 = RightLeg.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(8, 44).addBox(-0.1F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.6F, 5.2F, 1.2F, 0.093F, 0.0232F, -0.1899F));

		PartDefinition LeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 20).mirror().addBox(-1.7F, 1.2F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false)
		.texOffs(32, 0).mirror().addBox(-1.4F, 9.4F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition cube_r20 = LeftLeg.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(32, 7).mirror().addBox(-0.3869F, -1.5F, 0.0194F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(3.0F, 9.5F, -2.7F, 1.6871F, 0.6573F, 1.752F));

		PartDefinition cube_r21 = LeftLeg.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(4, 44).mirror().addBox(-0.9F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(0.9F, 11.3F, -2.1F, 0.2794F, -0.0026F, -0.0273F));

		PartDefinition cube_r22 = LeftLeg.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(8, 44).mirror().addBox(-0.9F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(2.6F, 5.2F, 1.2F, 0.093F, -0.0232F, 0.1899F));

		PartDefinition cube_r23 = LeftLeg.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(4, 44).mirror().addBox(-0.9F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(0.6F, 5.6F, -2.6F, -0.2794F, -0.0026F, -0.0273F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
	}

	public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
		this.swimAmount = pEntity.getSwimAmount(pPartialTick);
		super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
	}

	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		boolean $$6 = pEntity.getFallFlyingTicks() > 4;
		boolean $$7 = pEntity.isVisuallySwimming();
		this.head.yRot = pNetHeadYaw * 0.017453292F;
		if ($$6) {
			this.head.xRot = -0.7853982F;
		} else if (this.swimAmount > 0.0F) {
			if ($$7) {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, -0.7853982F);
			} else {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, pHeadPitch * 0.017453292F);
			}
		} else {
			this.head.xRot = pHeadPitch * 0.017453292F;
		}

		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;
		float $$8 = 1.0F;
		if ($$6) {
			$$8 = (float)pEntity.getDeltaMovement().lengthSqr();
			$$8 /= 0.2F;
			$$8 *= $$8 * $$8;
		}

		if ($$8 < 1.0F) {
			$$8 = 1.0F;
		}

		this.rightArm.xRot = Mth.cos(pLimbSwing * 0.6662F + 3.1415927F) * 2.0F * pLimbSwingAmount * 0.5F / $$8;
		this.leftArm.xRot = Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / $$8;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / $$8;
		this.leftLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + 3.1415927F) * 1.4F * pLimbSwingAmount / $$8;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
		this.rightLeg.zRot = 0.0F;
		this.leftLeg.zRot = 0.0F;
		ModelPart var10000;
		if (this.riding) {
			var10000 = this.rightArm;
			var10000.xRot += -0.62831855F;
			var10000 = this.leftArm;
			var10000.xRot += -0.62831855F;
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = 0.31415927F;
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = -0.31415927F;
			this.leftLeg.zRot = -0.07853982F;
		}

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;
		boolean $$9 = pEntity.getMainArm() == HumanoidArm.RIGHT;
		boolean $$10;
		if (pEntity.isUsingItem()) {
			$$10 = pEntity.getUsedItemHand() == InteractionHand.MAIN_HAND;
			if ($$10 == $$9) {
				this.poseRightArm(pEntity);
			} else {
				this.poseLeftArm(pEntity);
			}
		} else {
			$$10 = $$9 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
			if ($$9 != $$10) {
				this.poseLeftArm(pEntity);
				this.poseRightArm(pEntity);
			} else {
				this.poseRightArm(pEntity);
				this.poseLeftArm(pEntity);
			}
		}

		this.setupAttackAnimation(pEntity, pAgeInTicks);
		if (this.crouching) {
			this.body.xRot = 0.5F;
			var10000 = this.rightArm;
			var10000.xRot += 0.4F;
			var10000 = this.leftArm;
			var10000.xRot += 0.4F;
			this.rightLeg.z = 4.0F;
			this.leftLeg.z = 4.0F;
			this.rightLeg.y = 12.2F;
			this.leftLeg.y = 12.2F;
			this.head.y = 4.2F;
			this.body.y = 3.2F;
			this.leftArm.y = 5.2F;
			this.rightArm.y = 5.2F;
		} else {
			this.body.xRot = 0.0F;
			this.rightLeg.z = 0.1F;
			this.leftLeg.z = 0.1F;
			this.rightLeg.y = 12.0F;
			this.leftLeg.y = 12.0F;
			this.head.y = 0.0F;
			this.body.y = 0.0F;
			this.leftArm.y = 2.0F;
			this.rightArm.y = 2.0F;
		}

		if (this.rightArmPose != ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.rightArm, pAgeInTicks, 1.0F);
		}

		if (this.leftArmPose != ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.leftArm, pAgeInTicks, -1.0F);
		}

		if (this.swimAmount > 0.0F) {
			float $$12 = pLimbSwing % 26.0F;
			HumanoidArm $$13 = this.getAttackArm(pEntity);
			float $$14 = $$13 == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float $$15 = $$13 == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float $$17;
			if (!pEntity.isUsingItem()) {
				if ($$12 < 14.0F) {
					this.leftArm.xRot = this.rotlerpRad($$15, this.leftArm.xRot, 0.0F);
					this.rightArm.xRot = Mth.lerp($$14, this.rightArm.xRot, 0.0F);
					this.leftArm.yRot = this.rotlerpRad($$15, this.leftArm.yRot, 3.1415927F);
					this.rightArm.yRot = Mth.lerp($$14, this.rightArm.yRot, 3.1415927F);
					this.leftArm.zRot = this.rotlerpRad($$15, this.leftArm.zRot, 3.1415927F + 1.8707964F * this.quadraticArmUpdate($$12) / this.quadraticArmUpdate(14.0F));
					this.rightArm.zRot = Mth.lerp($$14, this.rightArm.zRot, 3.1415927F - 1.8707964F * this.quadraticArmUpdate($$12) / this.quadraticArmUpdate(14.0F));
				} else if ($$12 >= 14.0F && $$12 < 22.0F) {
					$$17 = ($$12 - 14.0F) / 8.0F;
					this.leftArm.xRot = this.rotlerpRad($$15, this.leftArm.xRot, 1.5707964F * $$17);
					this.rightArm.xRot = Mth.lerp($$14, this.rightArm.xRot, 1.5707964F * $$17);
					this.leftArm.yRot = this.rotlerpRad($$15, this.leftArm.yRot, 3.1415927F);
					this.rightArm.yRot = Mth.lerp($$14, this.rightArm.yRot, 3.1415927F);
					this.leftArm.zRot = this.rotlerpRad($$15, this.leftArm.zRot, 5.012389F - 1.8707964F * $$17);
					this.rightArm.zRot = Mth.lerp($$14, this.rightArm.zRot, 1.2707963F + 1.8707964F * $$17);
				} else if ($$12 >= 22.0F && $$12 < 26.0F) {
					$$17 = ($$12 - 22.0F) / 4.0F;
					this.leftArm.xRot = this.rotlerpRad($$15, this.leftArm.xRot, 1.5707964F - 1.5707964F * $$17);
					this.rightArm.xRot = Mth.lerp($$14, this.rightArm.xRot, 1.5707964F - 1.5707964F * $$17);
					this.leftArm.yRot = this.rotlerpRad($$15, this.leftArm.yRot, 3.1415927F);
					this.rightArm.yRot = Mth.lerp($$14, this.rightArm.yRot, 3.1415927F);
					this.leftArm.zRot = this.rotlerpRad($$15, this.leftArm.zRot, 3.1415927F);
					this.rightArm.zRot = Mth.lerp($$14, this.rightArm.zRot, 3.1415927F);
				}
			}

			$$17 = 0.3F;
			float $$19 = 0.33333334F;
			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(pLimbSwing * 0.33333334F + 3.1415927F));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(pLimbSwing * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}

	private void poseRightArm(T pLivingEntity) {
		switch (this.rightArmPose) {
			case EMPTY:
				this.rightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
				this.rightArm.yRot = -0.5235988F;
				break;
			case ITEM:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.31415927F;
				this.rightArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 3.1415927F;
				this.rightArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot;
				this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
				this.rightArm.xRot = -1.5707964F + this.head.xRot;
				this.leftArm.xRot = -1.5707964F + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, pLivingEntity, true);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
				break;
			case SPYGLASS:
				this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (pLivingEntity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.rightArm.yRot = this.head.yRot - 0.2617994F;
		}

	}

	private void poseLeftArm(T pLivingEntity) {
		switch (this.leftArmPose) {
			case EMPTY:
				this.leftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
				this.leftArm.yRot = 0.5235988F;
				break;
			case ITEM:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.31415927F;
				this.leftArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 3.1415927F;
				this.leftArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
				this.leftArm.yRot = 0.1F + this.head.yRot;
				this.rightArm.xRot = -1.5707964F + this.head.xRot;
				this.leftArm.xRot = -1.5707964F + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, pLivingEntity, false);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
				break;
			case SPYGLASS:
				this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (pLivingEntity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.leftArm.yRot = this.head.yRot + 0.2617994F;
		}

	}

	protected void setupAttackAnimation(T pLivingEntity, float pAgeInTicks) {
		if (!(this.attackTime <= 0.0F)) {
			HumanoidArm $$2 = this.getAttackArm(pLivingEntity);
			ModelPart $$3 = this.getArm($$2);
			float $$4 = this.attackTime;
			this.body.yRot = Mth.sin(Mth.sqrt($$4) * 6.2831855F) * 0.2F;
			ModelPart var10000;
			if ($$2 == HumanoidArm.LEFT) {
				var10000 = this.body;
				var10000.yRot *= -1.0F;
			}

			this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
			this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
			this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
			this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
			var10000 = this.rightArm;
			var10000.yRot += this.body.yRot;
			var10000 = this.leftArm;
			var10000.yRot += this.body.yRot;
			var10000 = this.leftArm;
			var10000.xRot += this.body.yRot;
			$$4 = 1.0F - this.attackTime;
			$$4 *= $$4;
			$$4 *= $$4;
			$$4 = 1.0F - $$4;
			float $$5 = Mth.sin($$4 * 3.1415927F);
			float $$6 = Mth.sin(this.attackTime * 3.1415927F) * -(this.head.xRot - 0.7F) * 0.75F;
			$$3.xRot -= $$5 * 1.2F + $$6;
			$$3.yRot += this.body.yRot * 2.0F;
			$$3.zRot += Mth.sin(this.attackTime * 3.1415927F) * -0.4F;
		}
	}

	protected float rotlerpRad(float pAngle, float pMaxAngle, float pMul) {
		float $$3 = (pMul - pMaxAngle) % 6.2831855F;
		if ($$3 < -3.1415927F) {
			$$3 += 6.2831855F;
		}

		if ($$3 >= 3.1415927F) {
			$$3 -= 6.2831855F;
		}

		return pMaxAngle + pAngle * $$3;
	}

	private float quadraticArmUpdate(float pLimbSwing) {
		return -65.0F * pLimbSwing + pLimbSwing * pLimbSwing;
	}

	public void copyPropertiesTo(HumanoidModel<T> pModel) {
		super.copyPropertiesTo(pModel);
		pModel.leftArmPose = this.leftArmPose;
		pModel.rightArmPose = this.rightArmPose;
		pModel.crouching = this.crouching;
		pModel.head.copyFrom(this.head);
		pModel.hat.copyFrom(this.hat);
		pModel.body.copyFrom(this.body);
		pModel.rightArm.copyFrom(this.rightArm);
		pModel.leftArm.copyFrom(this.leftArm);
		pModel.rightLeg.copyFrom(this.rightLeg);
		pModel.leftLeg.copyFrom(this.leftLeg);
	}

	public void setAllVisible(boolean pVisible) {
		this.head.visible = pVisible;
		this.hat.visible = pVisible;
		this.body.visible = pVisible;
		this.rightArm.visible = pVisible;
		this.leftArm.visible = pVisible;
		this.rightLeg.visible = pVisible;
		this.leftLeg.visible = pVisible;
	}

	public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
		this.getArm(pSide).translateAndRotate(pPoseStack);
	}

	protected ModelPart getArm(HumanoidArm pSide) {
		return pSide == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	public ModelPart getHead() {
		return this.head;
	}

	private HumanoidArm getAttackArm(T pEntity) {
		HumanoidArm $$1 = pEntity.getMainArm();
		return pEntity.swingingArm == InteractionHand.MAIN_HAND ? $$1 : $$1.getOpposite();
	}
}