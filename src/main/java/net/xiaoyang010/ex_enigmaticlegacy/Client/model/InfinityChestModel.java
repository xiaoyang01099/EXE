package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class InfinityChestModel extends Model {
    private final ModelPart base;
    public final ModelPart lid;
    private final ModelPart lock;

    public InfinityChestModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.base = root.getChild("base");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition baseGroup = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        baseGroup.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-7.0F, -2.0F, -7.0F, 14.0F, 10.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        baseGroup.addOrReplaceChild("lid",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, -7.0F, -7.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        baseGroup.addOrReplaceChild("lock",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -3.0F, -8.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lock.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}