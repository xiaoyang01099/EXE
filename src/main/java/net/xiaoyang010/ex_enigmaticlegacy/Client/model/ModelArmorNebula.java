package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

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
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class ModelArmorNebula<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "armor_nebula"), "main");

    public final ModelPart kuijia;
    public final ModelPart toukui;
    public final ModelPart xiongjia;
    public final ModelPart jia;
    public final ModelPart rightarm2;
    public final ModelPart leftarm2;
    public final ModelPart kuitui;
    public final ModelPart RightLeg2;
    public final ModelPart LeftLeg2;
    public final ModelPart Rightjiao;
    public final ModelPart Leftjiao;

    public ModelArmorNebula(ModelPart root) {
        super(root);

        this.kuijia = root.getChild("kuijia");
        this.toukui = this.kuijia.getChild("toukui");
        this.xiongjia = this.kuijia.getChild("xiongjia");
        this.jia = this.xiongjia.getChild("jia");
        this.rightarm2 = this.xiongjia.getChild("rightarm2");
        this.leftarm2 = this.xiongjia.getChild("leftarm2");
        this.kuitui = this.kuijia.getChild("kuitui");
        this.RightLeg2 = this.kuitui.getChild("RightLeg2");
        this.LeftLeg2 = this.kuitui.getChild("LeftLeg2");
        this.Rightjiao = this.kuitui.getChild("Rightjiao");
        this.Leftjiao = this.kuitui.getChild("Leftjiao");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition kuijia = partdefinition.addOrReplaceChild("kuijia", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition toukui = kuijia.addOrReplaceChild("toukui", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -34.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.3F))
                .texOffs(0, 10).addBox(-4.0F, -36.0F, -4.1F, 8.0F, 3.0F, 7.0F, new CubeDeformation(-0.9F))
                .texOffs(30, 10).addBox(-4.0F, -33.0F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.4F))
                .texOffs(38, 35).addBox(-4.0F, -30.4F, -3.0F, 0.0F, 1.0F, 6.0F, new CubeDeformation(0.3F))
                .texOffs(26, 43).addBox(-4.0F, -29.8F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.3F))
                .texOffs(38, 35).mirror().addBox(4.0F, -30.4F, -3.0F, 0.0F, 1.0F, 6.0F, new CubeDeformation(0.3F)).mirror(false)
                .texOffs(30, 10).mirror().addBox(4.0F, -33.0F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.4F)).mirror(false)
                .texOffs(26, 43).mirror().addBox(4.0F, -29.8F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = toukui.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 7).addBox(-0.6F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(4.0F, -33.7F, 0.5F, -0.9617F, -0.0706F, 0.0514F));

        PartDefinition cube_r2 = toukui.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(42, 43).addBox(-0.4F, -1.8F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-0.1F, -34.7F, 2.6F, -1.7628F, 0.0F, 0.0F));

        PartDefinition cube_r3 = toukui.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -33.7F, 0.5F, -0.9617F, 0.0706F, -0.0514F));

        PartDefinition cube_r4 = toukui.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-0.1F, -34.2F, -3.8F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r5 = toukui.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(34, 7).mirror().addBox(-0.4F, -0.8F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(-3.9F, -34.6F, -4.5F, -0.3791F, 0.3922F, -0.1511F));

        PartDefinition cube_r6 = toukui.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(34, 7).addBox(-0.6F, -0.8F, 0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.9F, -34.6F, -4.5F, -0.3791F, -0.3922F, 0.1511F));

        PartDefinition xiongjia = kuijia.addOrReplaceChild("xiongjia", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition jia = xiongjia.addOrReplaceChild("jia", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(24, 35).addBox(-3.0F, -24.9F, -4.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.5F))
                .texOffs(34, 43).addBox(-2.0F, -22.4F, -3.6F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.5F))
                .texOffs(0, 44).addBox(-1.0F, -21.2F, -3.6F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r7 = jia.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(46, 7).addBox(-0.6F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-3.1F, -22.6F, -3.9F, 0.305F, -0.1308F, -0.2363F));

        PartDefinition cube_r8 = jia.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(3.1F, -22.6F, -3.9F, 0.305F, 0.1308F, 0.2363F));

        PartDefinition cube_r9 = jia.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(46, 7).mirror().addBox(-0.4F, -1.8F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-0.1F, -19.7F, -4.8F, -0.2618F, 0.0F, 0.0F));

        PartDefinition rightarm2 = xiongjia.addOrReplaceChild("rightarm2", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0F, -24.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(0, 36).addBox(-8.0F, -14.8F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.45F))
                .texOffs(40, 20).addBox(-8.0F, -15.7F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r10 = rightarm2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(14, 36).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(-5.0F, -24.2F, -1.4F, 0.5323F, 0.0F, 0.0698F));

        PartDefinition cube_r11 = rightarm2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(40, 28).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(-8.0F, -24.7F, -0.5F, 0.0F, 0.0F, 0.0698F));

        PartDefinition cube_r12 = rightarm2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(14, 43).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-7.8F, -23.8F, -0.5F, 0.0F, 0.0F, 0.0698F));

        PartDefinition leftarm2 = xiongjia.addOrReplaceChild("leftarm2", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(5.0F, -24.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                .texOffs(0, 36).mirror().addBox(5.0F, -14.8F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
                .texOffs(40, 20).mirror().addBox(6.0F, -15.7F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r13 = leftarm2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(14, 36).mirror().addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.7F)).mirror(false), PartPose.offsetAndRotation(5.0F, -24.2F, -1.4F, 0.5323F, 0.0F, -0.0698F));

        PartDefinition cube_r14 = leftarm2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(40, 28).mirror().addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.7F)).mirror(false), PartPose.offsetAndRotation(8.0F, -24.7F, -0.5F, 0.0F, 0.0F, -0.0698F));

        PartDefinition cube_r15 = leftarm2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(14, 43).mirror().addBox(-2.0F, -1.0F, -1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(7.8F, -23.8F, -0.5F, 0.0F, 0.0F, -0.0698F));

        PartDefinition kuitui = kuijia.addOrReplaceChild("kuitui", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightLeg2 = kuitui.addOrReplaceChild("RightLeg2", CubeListBuilder.create().texOffs(24, 20).addBox(-4.1F, -10.7F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-0.1F, -0.1F, 0.0F));

        PartDefinition cube_r16 = RightLeg2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(4, 44).addBox(-0.1F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.4F, -6.3F, -2.6F, -0.2794F, 0.0026F, 0.0273F));

        PartDefinition cube_r17 = RightLeg2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(8, 44).addBox(-0.1F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-4.4F, -6.7F, 1.2F, 0.093F, 0.0232F, -0.1899F));

        PartDefinition LeftLeg2 = kuitui.addOrReplaceChild("LeftLeg2", CubeListBuilder.create().texOffs(24, 20).mirror().addBox(0.1F, -10.7F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.1F, -0.1F, 0.0F));

        PartDefinition cube_r18 = LeftLeg2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(8, 44).mirror().addBox(-0.9F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(4.4F, -6.7F, 1.2F, 0.093F, -0.0232F, 0.1899F));

        PartDefinition cube_r19 = LeftLeg2.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(4, 44).mirror().addBox(-0.9F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(2.4F, -6.3F, -2.6F, -0.2794F, -0.0026F, -0.0273F));

        PartDefinition Rightjiao = kuitui.addOrReplaceChild("Rightjiao", CubeListBuilder.create().texOffs(32, 0).addBox(-4.1F, -2.6F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.offset(-0.4F, 0.0F, 0.0F));

        PartDefinition cube_r20 = Rightjiao.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(4, 44).addBox(-0.1F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.4F, -0.7F, -2.1F, 0.2794F, 0.0026F, 0.0273F));

        PartDefinition cube_r21 = Rightjiao.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(32, 7).addBox(-0.6131F, -1.5F, 0.0194F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-4.5F, -2.5F, -2.7F, 1.6871F, -0.6573F, -1.752F));

        PartDefinition Leftjiao = kuitui.addOrReplaceChild("Leftjiao", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(0.1F, -2.6F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false), PartPose.offset(0.4F, 0.0F, 0.0F));

        PartDefinition cube_r22 = Leftjiao.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(4, 44).mirror().addBox(-0.9F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(2.4F, -0.7F, -2.1F, 0.2794F, -0.0026F, -0.0273F));

        PartDefinition cube_r23 = Leftjiao.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(32, 7).mirror().addBox(-0.3869F, -1.5F, 0.0194F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(4.5F, -2.5F, -2.7F, 1.6871F, 0.6573F, 1.752F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.toukui.xRot = this.head.xRot;
        this.toukui.yRot = this.head.yRot;
        this.toukui.zRot = this.head.zRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        kuijia.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
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

    private HumanoidArm getAttackArm(T pEntity) {
        HumanoidArm $$1 = pEntity.getMainArm();
        return pEntity.swingingArm == InteractionHand.MAIN_HAND ? $$1 : $$1.getOpposite();
    }

    private float quadraticArmUpdate(float pLimbSwing) {
        return -65.0F * pLimbSwing + pLimbSwing * pLimbSwing;
    }
}