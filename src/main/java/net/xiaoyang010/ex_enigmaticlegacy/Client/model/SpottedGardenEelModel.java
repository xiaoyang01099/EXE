package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpottedGardenEelModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;

    public SpottedGardenEelModel(ModelPart root) {
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
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offset(0.0F, 23.0F, -8.0F));

        body.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 14.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(8, 0)
                        .addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 38);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ImmutableList.of(this.body).forEach(modelPart -> {
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        });
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = 2.5f;
        float degree = 2.5f;
        this.body.yRot = Mth.cos(limbSwing * speed * 0.4F) * degree * 0.3F * limbSwingAmount;
        this.head.yRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * 0.3F * limbSwingAmount;
        this.tail.yRot = Mth.cos(1.0F + limbSwing * speed * 0.4F) * degree * 0.3F * limbSwingAmount;
    }
}