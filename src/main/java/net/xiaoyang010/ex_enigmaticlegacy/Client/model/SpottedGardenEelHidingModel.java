package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpottedGardenEelEntity;


@OnlyIn(Dist.CLIENT)
public class SpottedGardenEelHidingModel extends EntityModel<SpottedGardenEelEntity> {
    public final ModelPart body;
    public final ModelPart tail;
    public final ModelPart head;

    public SpottedGardenEelHidingModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = body.getChild("tail");
        this.head = body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 6)
                        .addBox(-1.0F, -1.0F, -12.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 25.0F, 1.0F, -1.5707963267948966F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 2.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(8, 0)
                        .addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -11.0F, 1.5707963267948966F, 0.0F, 0.0F));;

        return LayerDefinition.create(meshdefinition, 32, 38);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        ImmutableList.of(this.body).forEach(modelPart -> {
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        });
    }

    @Override
    public void setupAnim(SpottedGardenEelEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void prepareMobModel(SpottedGardenEelEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        if(entity.isInWater()) {
            float speed = 1.0f;
            float degree = 1.0f;
            this.body.zRot = Mth.cos(entity.tickCount * speed * 0.1F) * degree * 0.3F;
            this.tail.visible = false;
        } else {
            this.tail.visible = true;
            this.body.zRot = 0.0F;
        }
    }
}