package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class ModelArmorWildHunt extends HumanoidModel<LivingEntity> {
    private final ModelPart head;
    public final ModelPart chest;
    private final ModelPart cloak;
    public final ModelPart leftArm;
    public final ModelPart rightArm;
    public final ModelPart leftLeg;
    public final ModelPart rightLeg;
    public final ModelPart leftBoot;
    public final ModelPart rightBoot;
    private final int slot;

    public ModelArmorWildHunt(ModelPart root, int slot) {
        super(root);
        this.slot = slot;
        this.head = root.getChild("head");
        this.chest = root.getChild("body");
        this.cloak = root.getChild("cloak");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftBoot = root.getChild("left_boot");
        this.rightBoot = root.getChild("right_boot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 7).addBox(-4.0F, -8.0F, -4.0F, 8, 2, 8, new CubeDeformation(0.25F))
                        .texOffs(38, 18).addBox(-4.0F, -8.6F, -4.0F, 8, 1, 5, new CubeDeformation(0.0F))
                        .texOffs(21, 20).addBox(-3.0F, -8.6F, 1.0F, 6, 1, 2, new CubeDeformation(0.0F))
                        .texOffs(46, 86).addBox(-3.5F, -6.0F, 3.0F, 7, 1, 1, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Adding rotated cube parts for head
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(31, 25).addBox(1.7F, -6.7F, -3.0F, 2, 3, 2, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.8F, -2.7F, -0.1745F, 0.0F, 0.1309F));

        // Chest
        PartDefinition chest = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 93).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.2F))
                        .texOffs(0, 84).addBox(-4.0F, 0.9F, -3.1F, 8, 7, 1, new CubeDeformation(0.0F))
                        .texOffs(19, 85).addBox(-2.5F, 4.7F, 1.9F, 5, 6, 1, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Skeleton) && !(entity instanceof Zombie)) {
            prepareForRender(entity);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            setRotationAnglesMonster(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private void prepareForRender(LivingEntity entity) {
        this.crouching = entity.isCrouching();

        if (entity instanceof Player player) {
            ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);

            this.rightArmPose = ArmPose.EMPTY;
            if (!itemstack.isEmpty()) {
                this.rightArmPose = ArmPose.ITEM;

                if (player.getUseItemRemainingTicks() > 0) {
                    UseAnim useAnim = itemstack.getUseAnimation();
                    if (useAnim == UseAnim.BLOCK) {
                        this.rightArmPose = ArmPose.BLOCK;
                    } else if (useAnim == UseAnim.BOW) {
                        this.rightArmPose = ArmPose.BOW_AND_ARROW;
                    }
                }
            }
        }
    }

    private void setRotationAnglesMonster(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim((LivingEntity)entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float f = Mth.sin(this.attackTime * (float)Math.PI);
        float f1 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float)Math.PI);

        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.yRot = -(0.1F - f * 0.6F);
        this.leftArm.yRot = 0.1F - f * 0.6F;

        float f2 = -(float)Math.PI / 2F;
        this.rightArm.xRot = f2;
        this.leftArm.xRot = f2;
        this.rightArm.xRot -= f * 1.2F - f1 * 0.4F;
        this.leftArm.xRot -= f * 1.2F - f1 * 0.4F;

        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        head.visible = slot == 0;
        chest.visible = slot == 1;
        leftArm.visible = slot == 1;
        rightArm.visible = slot == 1;
        leftLeg.visible = slot == 2;
        rightLeg.visible = slot == 2;
        leftBoot.visible = slot == 3;
        rightBoot.visible = slot == 3;

        if (young) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}