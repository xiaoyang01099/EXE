package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmorHelper;
import vazkii.botania.api.item.IPhantomInkable;

public class ModelArmorNebula <T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("ex_enigmaticlegacy", "nebula_armor"), "main");

    private final ModelPart hat;
    private final ModelPart head;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart cube_r12;
    private final ModelPart cube_r13;
    private final ModelPart cube_r14;
    private final ModelPart cube_r15;
    public final ModelPart body;
    private final ModelPart cube_r16;
    private final ModelPart cube_r4;
    private final ModelPart cube_r4_r1;
    private final ModelPart cube_r5;
    private final ModelPart cube_r5_r1;
    private final ModelPart cube_r6;
    private final ModelPart cube_r7;
    private final ModelPart cube_r7_r1;
    private final ModelPart cube_r7_r2;
    public final ModelPart leftArm;
    private final ModelPart cube_r17;
    private final ModelPart cube_r8;
    private final ModelPart cube_r9;
    public final ModelPart rightArm;
    private final ModelPart cube_r18;
    private final ModelPart cube_r10;
    private final ModelPart cube_r11;
    public final ModelPart leftLeg;
    private final ModelPart cube_r19;
    private final ModelPart cube_r20;
    private final ModelPart cube_r21;
    public final ModelPart rightLeg;
    private final ModelPart cube_r22;
    private final ModelPart cube_r23;
    private final ModelPart cube_r24;
    public final ModelPart leftBoot;
    private final ModelPart cube_r25;
    private final ModelPart cube_r26;
    public final ModelPart rightBoot;
    private final ModelPart cube_r27;
    private final ModelPart cube_r28;
    private final EquipmentSlot slot;

    public ModelArmorNebula(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;

        this.hat = root.getChild("hat");
        this.head = root.getChild("head");
        this.cube_r1 = head.getChild("cube_r1");
        this.cube_r2 = head.getChild("cube_r2");
        this.cube_r3 = head.getChild("cube_r3");
        this.cube_r12 = head.getChild("cube_r12");
        this.cube_r13 = head.getChild("cube_r13");
        this.cube_r14 = head.getChild("cube_r14");
        this.cube_r15 = head.getChild("cube_r15");

        this.body = root.getChild("body");
        this.cube_r16 = body.getChild("cube_r16");
        this.cube_r4 = body.getChild("cube_r4");
        this.cube_r4_r1 = cube_r4.getChild("cube_r4_r1");
        this.cube_r5 = body.getChild("cube_r5");
        this.cube_r5_r1 = cube_r5.getChild("cube_r5_r1");
        this.cube_r6 = body.getChild("cube_r6");
        this.cube_r7 = body.getChild("cube_r7");
        this.cube_r7_r1 = cube_r7.getChild("cube_r7_r1");
        this.cube_r7_r2 = cube_r7.getChild("cube_r7_r2");

        this.leftArm = root.getChild("right_arm");
        this.cube_r17 = leftArm.getChild("cube_r17");
        this.cube_r8 = leftArm.getChild("cube_r8");
        this.cube_r9 = leftArm.getChild("cube_r9");

        this.rightArm = root.getChild("left_arm");
        this.cube_r18 = rightArm.getChild("cube_r18");
        this.cube_r10 = rightArm.getChild("cube_r10");
        this.cube_r11 = rightArm.getChild("cube_r11");

        this.leftLeg = root.getChild("right_leg");
        this.cube_r19 = leftLeg.getChild("cube_r19");
        this.cube_r20 = leftLeg.getChild("cube_r20");
        this.cube_r21 = leftLeg.getChild("cube_r21");

        this.rightLeg = root.getChild("left_leg");
        this.cube_r22 = rightLeg.getChild("cube_r22");
        this.cube_r23 = rightLeg.getChild("cube_r23");
        this.cube_r24 = rightLeg.getChild("cube_r24");

        this.leftBoot = root.getChild("left_boot");
        this.cube_r25 = leftBoot.getChild("cube_r25");
        this.cube_r26 = leftBoot.getChild("cube_r26");

        this.rightBoot = root.getChild("right_boot");
        this.cube_r27 = rightBoot.getChild("cube_r27");
        this.cube_r28 = rightBoot.getChild("cube_r28");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);


        // Head
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 66).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.21F))
                        .texOffs(36, 90).addBox(-3.0F, -8.75F, -3.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.21F))
                        .texOffs(0, 90).addBox(-4.0F, -6.0F, -4.0F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.2085F))
                        .texOffs(19, 86).addBox(3.0F, -6.0F, -4.0F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.2085F))
                        .texOffs(28, 80).addBox(-3.0F, -6.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.2075F))
                        .texOffs(23, 81).addBox(-4.0F, -6.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2075F))
                        .texOffs(23, 78).addBox(3.0F, -6.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2075F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(16, 78).addBox(0.6F, -8.4F, -6.8F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.3F, -0.1745F, -0.3491F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(9, 78).addBox(-2.6F, -8.4F, -6.8F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.3F, -0.1745F, 0.3491F, 0.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(5, 83).addBox(-1.0F, -9.0F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                        .texOffs(47, 83).addBox(-4.6F, -7.0F, 3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7418F, 0.0F, -0.0873F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                        .texOffs(45, 76).addBox(2.6F, -7.0F, 3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7418F, 0.0F, 0.0873F));

        PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create()
                        .texOffs(31, 84).addBox(-1.0F, -5.0F, -4.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create()
                        .texOffs(0, 78).addBox(-1.0F, -7.9F, -8.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.21F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

        // Body
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -0.2F, -3.0F, 9.0F, 11.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(31, 10).addBox(-2.5F, 5.7F, 1.9F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(31, 0).addBox(-3.5F, -0.6F, 2.6F, 7.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r16 = body.addOrReplaceChild("cube_r16", CubeListBuilder.create()
                        .texOffs(57, 16).addBox(-1.0F, -20.7F, 7.7F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 24.0F, -1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.1F, 25.2F, 3.35F, 0.2618F, 0.0F, 0.1745F));

        PartDefinition cube_r4_r1 = cube_r4.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create()
                        .texOffs(50, 8).addBox(-2.0F, -25.0F, -3.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -1.2F, -3.35F, -0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.1F, 25.2F, 3.35F, 0.2618F, 0.0F, -0.1745F));

        PartDefinition cube_r5_r1 = cube_r5.addOrReplaceChild("cube_r5_r1", CubeListBuilder.create()
                        .texOffs(50, 0).addBox(0.0F, -25.0F, -3.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -1.2F, -3.35F, -0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 23.75F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r7_r1 = cube_r7.addOrReplaceChild("cube_r7_r1", CubeListBuilder.create()
                        .texOffs(34, 68).addBox(-1.5F, -23.1842F, -1.1615F, 3.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -0.3F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r7_r2 = cube_r7.addOrReplaceChild("cube_r7_r2", CubeListBuilder.create()
                        .texOffs(34, 17).addBox(1.5F, -21.1842F, -0.8615F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(54, 69).addBox(-3.5F, -21.1842F, -0.8615F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -0.3F, 0.0F, 0.2618F, 0.0F, 0.0F));

        // Left Arm
        PartDefinition leftArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-4.0F, -2.4F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(21, 27).addBox(-3.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition cube_r17 = leftArm.addOrReplaceChild("cube_r17", CubeListBuilder.create()
                        .texOffs(45, 63).addBox(-1.7F, 1.3F, -2.3F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.8727F, 0.0F, 0.0F));

        PartDefinition cube_r8 = leftArm.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                        .texOffs(54, 25).addBox(-14.0918F, -24.4119F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.2F, 22.3F, 0.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition cube_r9 = leftArm.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                        .texOffs(16, 19).addBox(-11.8564F, -24.7019F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.2F, 22.3F, 0.0F, 0.0F, 0.0F, 0.0873F));

        // Right Arm
        PartDefinition rightArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(38, 26).addBox(-1.0F, -2.4F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(0, 38).addBox(-1.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(4.0F, 2.0F, 0.0F));

        PartDefinition cube_r18 = rightArm.addOrReplaceChild("cube_r18", CubeListBuilder.create()
                        .texOffs(45, 63).addBox(-0.3F, 1.3F, -2.3F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.8727F, 0.0F, 0.0F));

        PartDefinition cube_r10 = rightArm.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                        .texOffs(19, 36).addBox(11.6918F, -24.4119F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.8F, 22.3F, 0.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition cube_r11 = rightArm.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                        .texOffs(24, 38).addBox(7.4564F, -24.7019F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.8F, 22.3F, 0.0F, 0.0F, 0.0F, -0.0873F));

        // Left Leg
        PartDefinition leftLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 50).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(-1.0F, 12.0F, 0.0F));

        PartDefinition cube_r19 = leftLeg.addOrReplaceChild("cube_r19", CubeListBuilder.create()
                        .texOffs(46, 17).addBox(-1.0F, 0.6F, -3.8F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r20 = leftLeg.addOrReplaceChild("cube_r20", CubeListBuilder.create()
                        .texOffs(7, 20).addBox(-3.0F, 2.0F, 0.7F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r21 = leftLeg.addOrReplaceChild("cube_r21", CubeListBuilder.create()
                        .texOffs(17, 48).addBox(-0.5F, -10.7F, -1.9F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 1.0F, 0.1745F, 0.0F, -0.45F));

        // Right Leg
        PartDefinition rightLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(42, 38).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(1.0F, 12.0F, 0.0F));

        PartDefinition cube_r22 = rightLeg.addOrReplaceChild("cube_r22", CubeListBuilder.create()
                        .texOffs(35, 46).addBox(-1.0F, 0.6F, -3.8F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r23 = rightLeg.addOrReplaceChild("cube_r23", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(1.0F, 2.0F, 0.7F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r24 = rightLeg.addOrReplaceChild("cube_r24", CubeListBuilder.create()
                        .texOffs(26, 48).addBox(-1.5F, -10.7F, -1.9F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 1.0F, 0.1745F, 0.0F, 0.45F));

        // Left Boot
        PartDefinition leftBoot = partdefinition.addOrReplaceChild("left_boot", CubeListBuilder.create()
                        .texOffs(18, 56).addBox(-2.0F, 9.0F, -2.8F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition cube_r25 = leftBoot.addOrReplaceChild("cube_r25", CubeListBuilder.create()
                        .texOffs(40, 52).addBox(-0.1F, -5.7F, 1.9F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, -1.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r26 = leftBoot.addOrReplaceChild("cube_r26", CubeListBuilder.create()
                        .texOffs(33, 54).addBox(1.5F, -1.8F, 1.2F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, -1.0F, 0.7418F, 0.0F, 0.35F));

        // Right Boot
        PartDefinition rightBoot = partdefinition.addOrReplaceChild("right_boot", CubeListBuilder.create()
                        .texOffs(44, 53).addBox(-2.0F, 9.0F, -2.8F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition cube_r27 = rightBoot.addOrReplaceChild("cube_r27", CubeListBuilder.create()
                        .texOffs(27, 65).addBox(-1.9F, -5.7F, 1.9F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, -1.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r28 = rightBoot.addOrReplaceChild("cube_r28", CubeListBuilder.create()
                        .texOffs(37, 61).addBox(-2.5F, -1.8F, 1.2F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, -1.0F, 0.7418F, 0.0F, -0.35F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Skeleton) && !(entity instanceof Zombie)) {
            this.prepareForRender(entity);
            super.setupAnim((T) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.setRotationAnglesMonster(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, entity);
        }
    }

    public void prepareForRender(LivingEntity entity) {
        this.crouching = entity.isCrouching();

        if (entity instanceof Player player) {
            ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (itemstack.isEmpty()) {
                this.rightArmPose = ArmPose.EMPTY;
                this.leftArmPose = ArmPose.EMPTY;
            } else {
                if (player.getUseItemRemainingTicks() > 0) {
                    UseAnim useaction = itemstack.getUseAnimation();
                    if (useaction == UseAnim.BLOCK) {
                        this.rightArmPose = ArmPose.BLOCK;
                    } else if (useaction == UseAnim.BOW) {
                        this.rightArmPose = ArmPose.BOW_AND_ARROW;
                    }
                }
            }
        }
    }

    private void setRotationAnglesMonster(float limbSwing, float limbSwingAmount, float ageInTicks,
                                          float netHeadYaw, float headPitch, LivingEntity entity) {
        super.setupAnim((T) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

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

        hat.visible = false;
        head.visible = slot == EquipmentSlot.HEAD;
        body.visible = slot == EquipmentSlot.CHEST;
        leftArm.visible = slot == EquipmentSlot.CHEST;
        rightArm.visible = slot == EquipmentSlot.CHEST;
        rightLeg.visible = slot == EquipmentSlot.LEGS;
        leftLeg.visible = slot == EquipmentSlot.LEGS;
        leftBoot.visible = slot == EquipmentSlot.FEET;
        rightBoot.visible = slot == EquipmentSlot.FEET;

        if (young) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0F, 1.5F, 0.0F);
            renderBodyParts(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            renderBodyParts(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    private void renderBodyParts(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                 int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftBoot.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightBoot.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private LivingEntity currentEntity;

    public void setCurrentEntity(LivingEntity entity) {
        this.currentEntity = entity;
    }

    public LivingEntity getCurrentEntity() {
        return this.currentEntity;
    }
}
