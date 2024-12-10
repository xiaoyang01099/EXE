package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class ModelArmorNebula extends HumanoidModel<LivingEntity> {
    private final EquipmentSlot slot;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart lArm;
    private final ModelPart rArm;
    private final ModelPart lLeg;
    private final ModelPart rLeg;
    private final ModelPart hat;

    public ModelArmorNebula(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.lArm = root.getChild("left_arm");
        this.rArm = root.getChild("right_arm");
        this.lLeg = root.getChild("left_leg");
        this.rLeg = root.getChild("right_leg");
        this.hat = root.getChild("hat");
    }

    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.1F), 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 头部
        PartDefinition headPart = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 66).addBox(-4.0F, -8.0F, -4.0F, 8, 2, 8, new CubeDeformation(0.21F))
                        .texOffs(36, 90).addBox(-3.0F, -8.75F, -3.0F, 6, 1, 4, new CubeDeformation(0.21F))
                        , PartPose.offset(0.0F, 0.0F, 0.0F));
        addHeadRotations(headPart);

        // 胸甲
        partdefinition.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -0.2F, -3.0F, 9, 11, 6)
                        .texOffs(31, 10).addBox(-2.5F, 5.7F, 1.9F, 5, 4, 2)
                        .texOffs(31, 0).addBox(-3.5F, -0.6F, 2.6F, 7, 7, 2)
                , PartPose.offset(0.0F, 0.0F, 0.0F));

        // 左臂
        partdefinition.addOrReplaceChild("lArm", CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-4.0F, -2.4F, -2.5F, 5, 5, 5, new CubeDeformation(0.05F))
                        .texOffs(21, 27).addBox(-3.0F, 6.0F, -2.0F, 4, 4, 4, new CubeDeformation(0.2F))
                , PartPose.offset(-5.0F, 2.0F, 0.0F));

        // 右臂
        partdefinition.addOrReplaceChild("rArm", CubeListBuilder.create()
                        .texOffs(38, 26).addBox(-1.0F, -2.4F, -2.5F, 5, 5, 5, new CubeDeformation(0.05F))
                        .texOffs(0, 38).addBox(-1.0F, 6.0F, -2.0F, 4, 4, 4, new CubeDeformation(0.2F))
                , PartPose.offset(5.0F, 2.0F, 0.0F));

        // 左腿
        partdefinition.addOrReplaceChild("lLeg", CubeListBuilder.create()
                        .texOffs(0, 50).addBox(-4.0F, 0.0F, -2.0F, 4, 8, 4, new CubeDeformation(0.2F))
                , PartPose.offset(0.0F, 12.0F, 0.0F));

        // 右腿
        partdefinition.addOrReplaceChild("rLeg", CubeListBuilder.create()
                        .texOffs(42, 38).addBox(0.0F, 0.0F, -2.0F, 4, 8, 4, new CubeDeformation(0.2F))
                , PartPose.offset(0.0F, 12.0F, 0.0F));

        // 左靴
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(18, 56).addBox(0.0F, 9.0F, -2.8F, 4, 3, 5, new CubeDeformation(0.2F))
                , PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    private static void addHeadRotations(PartDefinition headPart) {
        // 为头部添加旋转的立方体部分
        headPart.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(16, 78).addBox(0.6F, -8.4F, -6.8F, 2, 3, 1, new CubeDeformation(0.21F))
                , PartPose.rotation(-0.1745F, -0.3491F, 0.0F));
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        //基于插槽的自定义渲染逻辑
        this.head.visible = slot == EquipmentSlot.HEAD;
        this.body.visible = slot == EquipmentSlot.CHEST;
        this.lArm.visible = slot == EquipmentSlot.CHEST;
        this.rArm.visible = slot == EquipmentSlot.CHEST;
        this.lLeg.visible = slot == EquipmentSlot.LEGS;
        this.rLeg.visible = slot == EquipmentSlot.LEGS;
        this.hat.visible = slot == EquipmentSlot.FEET;

        // 针对特定场景的姿势调整
        if (entity instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            this.rightArmPose = heldItem.isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;

            // 其他姿势处理
            UseAnim useAnim = heldItem.getUseAnimation();
            switch (useAnim) {
                case BLOCK -> this.rightArmPose = ArmPose.BLOCK;
                case BOW -> this.rightArmPose = ArmPose.BOW_AND_ARROW;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        if (young) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0F, 1.5F, 0.0F);
            head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0F, 1.5F, 0.0F);
            body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
