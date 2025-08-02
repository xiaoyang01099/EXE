package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CapybaraEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.Collections;

public class CapybaraModel extends AgeableListModel<CapybaraEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "capybara"), "main");

    public final ModelPart body;
    public final ModelPart rightBackLeg;
    public final ModelPart leftBackLeg;
    public final ModelPart rightFrontLeg;
    public final ModelPart leftFrontLeg;
    public final ModelPart head;
    public final ModelPart chestLeft;
    public final ModelPart chestRight;
    public final ModelPart earRight;
    public final ModelPart earLeft;
    public final ModelPart hat;
    public final ModelPart hatBrim;

    public CapybaraModel(ModelPart root) {
        this.body = root.getChild("body");
        this.rightBackLeg = root.getChild("right_back_leg");
        this.leftBackLeg = root.getChild("left_back_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.head = root.getChild("head");
        this.chestLeft = this.body.getChild("chest_left");
        this.chestRight = this.body.getChild("chest_right");
        this.earRight = this.head.getChild("ear_right");
        this.earLeft = this.head.getChild("ear_left");
        this.hat = this.head.getChild("hat");
        this.hatBrim = this.hat.getChild("hat_brim");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, -7.0F, -11.0F, 14.0F, 14.0F, 22.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        body.addOrReplaceChild("chest_right",
                CubeListBuilder.create()
                        .texOffs(40, 58)
                        .addBox(-2.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F),
                PartPose.offset(-7.0F, -3.0F, 4.0F));

        body.addOrReplaceChild("chest_left",
                CubeListBuilder.create()
                        .texOffs(60, 58)
                        .mirror()
                        .addBox(0.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F),
                PartPose.offset(7.0F, -3.0F, 4.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 36)
                        .addBox(-4.0F, -5.5F, -11.0F, 8.0F, 10.0F, 14.0F),
                PartPose.offset(0.0F, 4.5F, -10.0F));

        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.0F, -2.0F, -1.0F, 1.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-3.5F, -5.0F, 1.5F, -0.3927F, -0.3927F, 0.0F));

        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(3.5F, -5.0F, 1.5F, -0.3927F, 0.3927F, 0.0F));

        PartDefinition hat = head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(30, 36)
                        .addBox(-3.5F, -4.0F, -1.5F, 7.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -5.1F, -1.5F, -0.3128F, 0.0F, 0.0F));

        hat.addOrReplaceChild("hat_brim",
                CubeListBuilder.create()
                        .texOffs(45, 17)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 0.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, -3.5F));

        partdefinition.addOrReplaceChild("right_back_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(-3.5F, 13.3F, 10.0F));

        partdefinition.addOrReplaceChild("left_back_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(3.5F, 13.3F, 10.0F));

        partdefinition.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create()
                        .texOffs(50, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(-4.0F, 13.3F, -8.0F));

        partdefinition.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create()
                        .texOffs(50, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(4.0F, 13.3F, -8.0F));

        return LayerDefinition.create(meshdefinition, 80, 74);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return Collections.emptyList();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, leftBackLeg, leftFrontLeg, rightBackLeg, rightFrontLeg, head);
    }

    @Override
    public void setupAnim(CapybaraEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = 1.0f;
        float degree = 1.0f;

        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.body.yRot = 0;
        this.body.zRot = Mth.cos(limbSwing * speed * 0.4F) * degree * 0.15F * limbSwingAmount;

        if (entityIn.isInWater()) {
            this.body.yRot = Mth.cos(ageInTicks * speed * 0.4F) * degree * 0.05F * 1;
            this.body.zRot = 0;
            this.leftBackLeg.xRot = Mth.cos(1.0F + ageInTicks * speed * 0.4F) * degree * 1.2F * 0.2F + 0.45F;
            this.rightBackLeg.xRot = Mth.cos(1.0F + ageInTicks * speed * 0.4F) * degree * -1.2F * 0.2F + 0.45F;
            this.rightFrontLeg.xRot = Mth.cos(1.0F + ageInTicks * speed * 0.4F) * degree * 0.8F * 0.2F + 0.45F;
            this.leftFrontLeg.xRot = Mth.cos(1.0F + ageInTicks * speed * 0.4F) * degree * -0.8F * 0.2F + 0.45F;
            this.head.xRot += Mth.cos(ageInTicks * speed * 0.4F) * degree * 0.2F * 0.2F - 0.25F;
        } else {
            if (entityIn.isInSittingPose()) {
                this.body.y = 17.0F;
                this.body.yRot = 0.0F;
                this.rightBackLeg.y = 21.3F;
                this.rightBackLeg.yRot = -0.3490658503988659F;
                this.rightBackLeg.xRot = 1.5708F;
                this.leftBackLeg.y = 21.3F;
                this.leftBackLeg.yRot = 0.3490658503988659F;
                this.leftBackLeg.xRot = 1.5708F;
                this.rightFrontLeg.y = 22.3F;
                this.rightFrontLeg.yRot = 0.3490658503988659F;
                this.rightFrontLeg.xRot = -1.5708F;
                this.leftFrontLeg.y = 22.3F;
                this.leftFrontLeg.yRot = -0.3490658503988659F;
                this.leftFrontLeg.xRot = -1.5708F;
                this.head.y = 10.5F;
            } else {
                this.body.y = 11.0F;
                this.body.yRot = 0.0F;
                this.rightBackLeg.y = 13.3F;
                this.rightBackLeg.yRot = 0.0F;
                this.leftBackLeg.y = 13.3F;
                this.leftBackLeg.yRot = 0.0F;
                this.rightFrontLeg.y = 13.3F;
                this.rightFrontLeg.yRot = 0.0F;
                this.leftFrontLeg.y = 13.3F;
                this.leftFrontLeg.yRot = 0.0F;
                this.head.y = 4.5F;

                this.leftBackLeg.xRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * 0.8F * limbSwingAmount;
                this.rightBackLeg.xRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * -0.8F * limbSwingAmount;
                this.rightFrontLeg.xRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * 0.8F * limbSwingAmount;
                this.leftFrontLeg.xRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * -0.8F * limbSwingAmount;
            }
        }
    }
}