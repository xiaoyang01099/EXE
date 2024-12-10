package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.model;

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

    // 创建模型层，确保尺寸与 JSON 中定义相匹配
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // lock部分，从 [7, 7, 0] 到 [9, 11, 1]，大小为 2x4x1
        partdefinition.addOrReplaceChild("lock",
                CubeListBuilder.create().texOffs(32, 0).addBox(7.0F, 7.0F, 0.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F)); // 偏移量为 0，直接使用原始位置

        // lid部分，从 [1, 10, 1] 到 [15, 15, 15]，大小为 14x5x14
        partdefinition.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 10.0F, 1.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F)); // 偏移量为 0

        // base部分，从 [1, 0, 1] 到 [15, 10, 15]，大小为 14x10x14
        partdefinition.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F)); // 偏移量为 0

        return LayerDefinition.create(meshdefinition, 64, 64); // 确保纹理大小足够
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lock.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
