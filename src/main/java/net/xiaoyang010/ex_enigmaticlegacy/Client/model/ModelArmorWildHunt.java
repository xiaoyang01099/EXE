package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;

public class ModelArmorWildHunt<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("ex_enigmaticlegacy", "wild_hunt_armor"), "main");

    public final ModelPart head;
    public final ModelPart chest;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public final ModelPart rightBoot;
    public final ModelPart leftBoot;
    public final EquipmentSlot slot;

    public ModelArmorWildHunt(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;

        this.head = root.getChild("head");
        this.chest = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightBoot = root.getChild("right_boot");
        this.leftBoot = root.getChild("left_boot");

        this.hat.visible = false;
        this.head.visible = slot == EquipmentSlot.HEAD;
        this.body.visible = slot == EquipmentSlot.CHEST;
        this.rightArm.visible = slot == EquipmentSlot.CHEST;
        this.leftArm.visible = slot == EquipmentSlot.CHEST;
        this.rightLeg.visible = slot == EquipmentSlot.LEGS;
        this.leftLeg.visible = slot == EquipmentSlot.LEGS;
        this.rightBoot.visible = slot == EquipmentSlot.FEET;
        this.leftBoot.visible = slot == EquipmentSlot.FEET;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 7).addBox(-4.0F, -8.0F, -4.0F, 8, 2, 8, new CubeDeformation(0.25F))
                        .texOffs(38, 18).addBox(-4.0F, -8.6F, -4.0F, 8, 1, 5, new CubeDeformation(0.0F))
                        .texOffs(21, 20).addBox(-3.0F, -8.6F, 1.0F, 6, 1, 2, new CubeDeformation(0.0F))
                        .texOffs(46, 86).addBox(-3.5F, -6.0F, 3.0F, 7, 1, 1, new CubeDeformation(0.1F))
                        .texOffs(40, 25).addBox(3.0F, -6.2F, -1.8F, 1, 4, 5, new CubeDeformation(0.1F))
                        .texOffs(52, 25).addBox(-4.0F, -6.2F, -1.8F, 1, 4, 5, new CubeDeformation(0.1F))
                        .texOffs(28, 0).addBox(-4.0F, -6.3F, -4.0F, 1, 5, 2, new CubeDeformation(0.1F))
                        .texOffs(33, 7).addBox(3.0F, -6.3F, -4.0F, 1, 5, 2, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(31, 25).addBox(1.7F, -6.7F, -3.0F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.8F, -2.7F, -0.1745F, 0.0F, 0.1309F));

        PartDefinition chest = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(49, 25).addBox(-1.0F, 7.85F, -2.9F, 2, 2, 1, new CubeDeformation(0.0F))
                        .texOffs(0, 84).addBox(-4.0F, 0.9F, -3.1F, 8, 7, 1, new CubeDeformation(0.0F))
                        .texOffs(19, 85).addBox(-2.5F, 4.7F, 1.9F, 5, 6, 1, new CubeDeformation(0.0F))
                        .texOffs(44, 89).addBox(-3.5F, 0.0F, 1.1F, 7, 7, 2, new CubeDeformation(0.0F))
                        .texOffs(0, 93).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(19, 72).addBox(-1.0F, 2.0F, -2.0F, 4, 8, 4, new CubeDeformation(0.1F))
                        .texOffs(31, 105).addBox(-1.0F, -2.0F, -2.0F, 4, 4, 4, new CubeDeformation(0.3F))
                        .texOffs(19, 43).addBox(-0.1F, -2.7F, -2.0F, 4, 3, 4, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition leftArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(19, 59).addBox(-3.0F, 2.0F, -2.0F, 4, 8, 4, new CubeDeformation(0.1F))
                        .texOffs(31, 95).addBox(-3.0F, -2.0F, -2.0F, 4, 4, 4, new CubeDeformation(0.3F))
                        .texOffs(19, 51).addBox(-3.9F, -2.7F, -2.0F, 4, 3, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition rightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(48, 116).addBox(-2.0F, 0.4F, -2.0F, 4, 8, 4, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition leftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(48, 103).addBox(-2.0F, 0.4F, -2.0F, 4, 8, 4, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition rightBoot = partdefinition.addOrReplaceChild("right_boot", CubeListBuilder.create()
                        .texOffs(0, 120).addBox(-2.0F, 9.0F, -2.8F, 4, 3, 5, new CubeDeformation(0.25F)), // 把 -4.0F 改成 -2.0F
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition leftBoot = partdefinition.addOrReplaceChild("left_boot", CubeListBuilder.create()
                        .texOffs(0, 111).addBox(-2.0F, 9.0F, -2.8F, 4, 3, 5, new CubeDeformation(0.25F)), // 把 0.0F 改成 -2.0F
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(22, 25).addBox(-3.7F, -6.7F, -3.0F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.8F, -2.7F, -0.1745F, 0.0F, -0.1309F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(21, 12).addBox(-1.0F, -13.1F, -2.8F, 2, 1, 6, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 4.0F, -1.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                        .texOffs(47, 61).addBox(1.0F, -15.2F, -3.4F, 2, 3, 2, new CubeDeformation(-0.3F))
                        .texOffs(47, 55).addBox(-3.6F, -15.2F, -3.4F, 2, 3, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(0.3F, 4.0F, -1.2F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(47, 71).addBox(-3.0F, -13.0F, 1.2F, 2, 1, 2, new CubeDeformation(-0.1F))
                        .texOffs(47, 67).addBox(1.6F, -13.0F, 1.2F, 2, 1, 2, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(-0.3F, 4.0F, -1.2F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                        .texOffs(24, 8).addBox(-1.0F, -11.2F, -2.0F, 2, 2, 1, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 4.0F, -1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                        .texOffs(56, 43).addBox(-1.0F, -15.7F, -6.4F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(-0.8F, 5.5F, -0.1F, -0.1309F, 1.5708F, 0.0F));

        PartDefinition cube_r8 = head.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                        .texOffs(56, 68).addBox(-1.0F, -13.7F, -1.7F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.8F, 4.5F, -0.1F, 0.2182F, -1.5708F, 0.0F));

        PartDefinition cube_r9 = head.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                        .texOffs(56, 49).addBox(-1.0F, -15.7F, -6.4F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(0.8F, 5.5F, -0.1F, -0.1309F, -1.5708F, 0.0F));

        PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                        .texOffs(56, 74).addBox(-1.0F, -13.7F, -1.7F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(-0.8F, 4.5F, -0.1F, 0.2182F, 1.5708F, 0.0F));

        PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                        .texOffs(56, 62).addBox(-1.0F, -15.7F, -6.6F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(0.0F, 3.9F, -0.7F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                        .texOffs(56, 55).addBox(-1.0F, -14.7F, -1.7F, 2, 4, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.9F, -0.7F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                        .texOffs(47, 43).addBox(-1.0F, -13.7F, -1.7F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(-1.6F, 3.8F, -1.6F, 0.2182F, 0.7854F, 0.0F));

        PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -14.7F, -6.3F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(1.6F, 3.8F, -1.6F, -0.1309F, -0.7854F, 0.0F));

        PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create()
                        .texOffs(47, 49).addBox(-1.0F, -13.7F, -1.7F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(1.6F, 3.8F, -1.6F, 0.2182F, -0.7854F, 0.0F));

        PartDefinition cube_r16 = head.addOrReplaceChild("cube_r16", CubeListBuilder.create()
                        .texOffs(0, 6).addBox(-1.0F, -14.7F, -6.3F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(-1.6F, 3.8F, -1.6F, -0.1309F, 0.7854F, 0.0F));

        PartDefinition cube_r17 = chest.addOrReplaceChild("cube_r17", CubeListBuilder.create()
                        .texOffs(36, 1).addBox(-4.0F, 10.1F, -5.1F, 8, 4, 1, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, 1.2F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r18 = chest.addOrReplaceChild("cube_r18", CubeListBuilder.create()
                        .texOffs(51, 35).addBox(0.9F, -14.0F, -3.9F, 2, 6, 1, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, 20.5F, 0.8F, 0.0F, 0.0F, 0.0436F));

        PartDefinition cube_r19 = chest.addOrReplaceChild("cube_r19", CubeListBuilder.create()
                        .texOffs(58, 35).addBox(-2.9F, -14.0F, -3.9F, 2, 6, 1, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, 20.5F, 0.8F, 0.0F, 0.0F, -0.0436F));

        PartDefinition cube_r21 = leftArm.addOrReplaceChild("cube_r21", CubeListBuilder.create()
                        .texOffs(0, 29).addBox(-1.0F, -13.7F, -6.3F, 2, 2, 2, new CubeDeformation(-0.45F)),
                PartPose.offsetAndRotation(-1.1F, 10.3F, 0.5F, -0.1309F, 0.7854F, 0.0F));

        PartDefinition cube_r22 = leftArm.addOrReplaceChild("cube_r22", CubeListBuilder.create()
                        .texOffs(9, 51).addBox(-3.0F, -14.7F, -3.4F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(0.8F, 9.9F, 1.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r23 = leftArm.addOrReplaceChild("cube_r23", CubeListBuilder.create()
                        .texOffs(9, 63).addBox(-3.0F, -13.0F, 1.2F, 2, 1, 2, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.8F, 9.9F, 1.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r24 = leftArm.addOrReplaceChild("cube_r24", CubeListBuilder.create()
                        .texOffs(0, 47).addBox(-1.0F, -13.7F, -1.7F, 2, 2, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(-1.1F, 9.7F, -0.8F, 0.2182F, 2.3562F, 0.0F));

        PartDefinition cube_r25 = leftArm.addOrReplaceChild("cube_r25", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-1.0F, -13.7F, -6.3F, 2, 2, 2, new CubeDeformation(-0.45F)),
                PartPose.offsetAndRotation(-1.1F, 9.9F, -0.8F, -0.1309F, 2.3562F, 0.0F));

        PartDefinition cube_r26 = leftArm.addOrReplaceChild("cube_r26", CubeListBuilder.create()
                        .texOffs(0, 53).addBox(-1.0F, -13.7F, -1.7F, 2, 2, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(-1.1F, 10.1F, 0.5F, 0.2182F, 0.7854F, 0.0F));

        PartDefinition cube_r27 = rightArm.addOrReplaceChild("cube_r27", CubeListBuilder.create()
                        .texOffs(0, 59).addBox(-1.0F, -13.7F, -1.7F, 2, 2, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(1.1F, 10.1F, 0.5F, 0.2182F, -0.7854F, 0.0F));

        PartDefinition cube_r28 = rightArm.addOrReplaceChild("cube_r28", CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-1.0F, -13.7F, -6.3F, 2, 2, 2, new CubeDeformation(-0.45F)),
                PartPose.offsetAndRotation(1.1F, 10.3F, 0.5F, -0.1309F, -0.7854F, 0.0F));

        PartDefinition cube_r29 = rightArm.addOrReplaceChild("cube_r29", CubeListBuilder.create()
                        .texOffs(0, 41).addBox(-1.0F, -13.7F, -6.3F, 2, 2, 2, new CubeDeformation(-0.45F)),
                PartPose.offsetAndRotation(1.2F, 10.0F, -0.8F, -0.1309F, -2.3562F, 0.0F));

        PartDefinition cube_r30 = rightArm.addOrReplaceChild("cube_r30", CubeListBuilder.create()
                        .texOffs(0, 65).addBox(-1.0F, -13.7F, -1.7F, 2, 2, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(1.2F, 9.8F, -0.8F, 0.2182F, -2.3562F, 0.0F));

        PartDefinition cube_r31 = rightArm.addOrReplaceChild("cube_r31", CubeListBuilder.create()
                        .texOffs(9, 57).addBox(-3.0F, -14.7F, -3.4F, 2, 3, 2, new CubeDeformation(-0.35F)),
                PartPose.offsetAndRotation(3.3F, 10.0F, 1.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r32 = rightArm.addOrReplaceChild("cube_r32", CubeListBuilder.create()
                        .texOffs(9, 67).addBox(-3.0F, -13.0F, 1.2F, 2, 1, 2, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(3.3F, 10.0F, 1.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r33 = rightLeg.addOrReplaceChild("cube_r33", CubeListBuilder.create()
                        .texOffs(37, 115).addBox(-4.5F, -10.1F, -3.1F, 3, 4, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(0.0F, 11.9F, 1.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r34 = leftLeg.addOrReplaceChild("cube_r34", CubeListBuilder.create()
                        .texOffs(37, 122).addBox(1.5F, -10.1F, -3.1F, 3, 4, 2, new CubeDeformation(-0.3F)),
                PartPose.offsetAndRotation(0.0F, 11.9F, 1.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cloak = partdefinition.addOrReplaceChild("cloak", CubeListBuilder.create(),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition cube_r20 = cloak.addOrReplaceChild("cube_r20", CubeListBuilder.create()
                        .texOffs(0, 71).addBox(-3.0F, 4.6F, 1.4F, 6, 10, 0, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -6.0F, 1.0F, 0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        switch (slot) {
            case HEAD:
                head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;
            case CHEST:
                chest.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;
            case LEGS:
                rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;
            case FEET:
                rightBoot.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                leftBoot.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;
            default:
                break;
        }
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {

        super.setupAnim((T) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity instanceof Zombie || entity instanceof Skeleton) {
            setupMonsterAnimation(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private void setupMonsterAnimation(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                       float ageInTicks, float netHeadYaw, float headPitch) {
        // 特定于怪物的动画
        rightArm.xRot = -(float) Math.PI / 2F;
        leftArm.xRot = -(float) Math.PI / 2F;

        float f = (float) Math.PI;
        float swingProgress = this.attackTime;
        float f1 = (float) Math.sin(swingProgress * f);
        float f2 = (float) Math.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * f);

        rightArm.zRot = 0;
        leftArm.zRot = 0;
        rightArm.yRot = -(0.1F - f1 * 0.6F);
        leftArm.yRot = 0.1F - f1 * 0.6F;

        // 应用摆动动画
        rightArm.xRot -= f1 * 1.2F - f2 * 0.4F;
        leftArm.xRot -= f1 * 1.2F - f2 * 0.4F;
        rightArm.zRot += (float) (Math.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        leftArm.zRot -= (float) (Math.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        rightArm.xRot += (float) (Math.sin(ageInTicks * 0.067F) * 0.05F);
        leftArm.xRot -= (float) (Math.sin(ageInTicks * 0.067F) * 0.05F);
    }
}