package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SacabambaspisModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "sacabambaspis"), "main");

    private final ModelPart head;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart cube_r4;
    private final ModelPart cube_r5;
    private final ModelPart cube_r6;
    private final ModelPart cube_r7;
    private final ModelPart cube_r8;
    private final ModelPart cube_r9;
    private final ModelPart mouth_r1;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart tail1;
    private final ModelPart tail2;

    public SacabambaspisModel(ModelPart root) {
        this.head = root.getChild("head");
        this.cube_r1 = head.getChild("cube_r1");
        this.cube_r2 = head.getChild("cube_r2");
        this.cube_r3 = head.getChild("cube_r3");
        this.cube_r4 = head.getChild("cube_r4");
        this.cube_r5 = head.getChild("cube_r5");
        this.cube_r6 = head.getChild("cube_r6");
        this.cube_r7 = head.getChild("cube_r7");
        this.cube_r8 = head.getChild("cube_r8");
        this.cube_r9 = head.getChild("cube_r9");
        this.mouth_r1 = head.getChild("mouth_r1");
        this.body1 = head.getChild("body1");
        this.body2 = body1.getChild("body2");
        this.body3 = body2.getChild("body3");
        this.tail1 = body3.getChild("tail1");
        this.tail2 = tail1.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(14, 34).addBox(-1.99F, -5.75F, -7.0F, 4.0F, 1.0F, 1.0F)
                        .texOffs(38, 26).addBox(-2.5F, -7.0F, 6.25F, 5.0F, 3.0F, 2.0F)
                        .texOffs(0, 11).addBox(-3.0F, -6.5F, -3.75F, 6.0F, 2.0F, 10.0F)
                        .texOffs(0, 23).addBox(-3.5F, -4.85F, -2.75F, 7.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(34, 36).addBox(-3.51F, -4.35F, -4.1F, 7.0F, 3.0F, 1.0F)
                        .texOffs(24, 5).addBox(-3.01F, -4.35F, -6.1F, 6.0F, 3.0F, 2.0F)
                        .texOffs(22, 11).addBox(-3.49F, -5.451F, 0.85F, 7.0F, 1.0F, 5.0F)
                        .texOffs(9, 38).addBox(-2.99F, -5.451F, 5.85F, 6.0F, 1.0F, 2.0F),
                PartPose.rotation(-0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(34, 40).addBox(-2.49F, -6.7F, -4.3F, 5.0F, 2.0F, 2.0F),
                PartPose.rotation(0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(32, 17).addBox(-2.5F, -7.0F, -5.75F, 5.0F, 2.0F, 3.0F)
                        .texOffs(34, 22).addBox(-2.49F, -2.75F, 6.0F, 5.0F, 1.0F, 3.0F)
                        .texOffs(24, 0).addBox(-2.99F, -3.65F, 3.5F, 6.0F, 2.0F, 3.0F)
                        .texOffs(28, 31).addBox(-2.99F, -4.6F, 1.5F, 6.0F, 3.0F, 2.0F),
                PartPose.rotation(0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                        .texOffs(35, 5).addBox(-3.8F, -5.25F, -3.45F, 2.0F, 1.0F, 5.0F)
                        .texOffs(0, 38).addBox(1.8F, -5.25F, -3.45F, 2.0F, 1.0F, 5.0F),
                PartPose.rotation(-0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(0, 11).addBox(-4.71F, -5.4F, -5.55F, 1.0F, 1.0F, 4.0F),
                PartPose.rotation(0.0F, -0.3491F, 0.0F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(3.71F, -5.4F, -5.55F, 1.0F, 1.0F, 4.0F),
                PartPose.rotation(0.0F, 0.3491F, 0.0F));

        PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                        .texOffs(14, 30).addBox(2.5F, -6.65F, -5.0F, 2.0F, 1.0F, 3.0F),
                PartPose.rotation(0.0436F, 0.3491F, 0.0F));

        PartDefinition cube_r8 = head.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                        .texOffs(22, 17).addBox(-4.5F, -6.65F, -5.0F, 2.0F, 1.0F, 3.0F),
                PartPose.rotation(0.0436F, -0.3491F, 0.0F));

        PartDefinition cube_r9 = head.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -6.75F, -3.4F, 7.0F, 1.0F, 10.0F),
                PartPose.rotation(0.0436F, 0.0F, 0.0F));

        PartDefinition mouth_r1 = head.addOrReplaceChild("mouth_r1", CubeListBuilder.create()
                        .texOffs(39, 0).addBox(-1.99F, -6.85F, -5.0F, 4.0F, 2.0F, 1.0F),
                PartPose.rotation(0.3491F, 0.0F, 0.0F));

        PartDefinition body1 = head.addOrReplaceChild("body1", CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-2.0F, -2.01F, 0.25F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -5.0F, 8.0F));

        PartDefinition body2 = body1.addOrReplaceChild("body2", CubeListBuilder.create()
                        .texOffs(22, 38).addBox(-1.5F, -1.25F, 0.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -0.5F, 4.25F));

        PartDefinition body3 = body2.addOrReplaceChild("body3", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -1.26F, 0.0F, 2.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition tail1 = body3.addOrReplaceChild("tail1", CubeListBuilder.create()
                        .texOffs(6, 30).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 6.0F)
                        .texOffs(0, 24).addBox(0.0F, -3.5F, 0.0F, 0.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                        .texOffs(20, 31).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F)
                        .texOffs(0, 2).addBox(0.0F, -1.5F, 6.0F, 0.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -0.25F, 6.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = 0.6F;
        float degree = 1.0F;

        boolean inWater = entity.isInWater() ||
                entity.level.getBlockState(entity.blockPosition())
                        .getFluidState()
                        .isSource();

        if (!inWater) {
            speed = 0.8F;
        }

        if (entity instanceof LivingEntity livingEntity) {
            this.head.y = 19.0F;
            float swimAnimation = (float) Math.sin(ageInTicks * 0.1F) * 0.3F;
            this.head.y += swimAnimation;

            float tailWave = speed * 0.3F;
            float tailDegree = degree * 0.4F;
            this.body1.yRot = (float) Math.sin(ageInTicks * tailWave) * tailDegree;
            this.body2.yRot = (float) Math.sin(ageInTicks * tailWave - 0.4F) * tailDegree;
            this.body3.yRot = (float) Math.sin(ageInTicks * tailWave - 0.8F) * tailDegree;
            this.tail1.yRot = (float) Math.sin(ageInTicks * tailWave - 1.2F) * tailDegree;
            this.tail2.yRot = (float) Math.sin(ageInTicks * tailWave - 1.6F) * tailDegree;

            if (!inWater) {
                this.head.zRot = (float) Math.toRadians(90);
                this.head.y = 19.0F + (float) Math.sin(ageInTicks * tailWave) * 2.0F;
            } else {
                this.head.zRot = 0;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.0F, 1.5F, 0.0F);

        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}