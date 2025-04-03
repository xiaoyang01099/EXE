package net.xiaoyang010.ex_enigmaticlegacy.Client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelNidavellirForge extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("ex_enigmaticlegacy", "nidavellir_forge"), "main");
    private final ModelPart bottomAnvil;
    private final ModelPart topAnvil;

    public ModelNidavellirForge(ModelPart root) {
        super(RenderType::entitySolid);
        this.bottomAnvil = root.getChild("bottom_anvil");
        this.topAnvil = root.getChild("top_anvil");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bottomAnvil = partdefinition.addOrReplaceChild("bottom_anvil", CubeListBuilder.create()
                        .texOffs(32, 26).addBox(-3.0F, -1.0F, -4.0F, 6.0F, 1.0F, 1.0F)
                        .texOffs(0, 31).addBox(-5.0F, -1.0F, -3.0F, 12.0F, 1.0F, 6.0F)
                        .texOffs(32, 17).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                        .texOffs(0, 8).addBox(-4.0F, -2.0F, -3.0F, 8.0F, 1.0F, 6.0F)
                        .texOffs(32, 23).addBox(-3.0F, -1.0F, 3.0F, 6.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition topAnvil = partdefinition.addOrReplaceChild("top_anvil", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-6.5F, -11.0F, -3.0F, 12.0F, 2.0F, 6.0F)
                        .texOffs(0, 38).addBox(-5.5F, -12.0F, -4.0F, 13.0F, 2.0F, 8.0F)
                        .texOffs(0, 15).addBox(-5.5F, -9.0F, -3.0F, 9.0F, 2.0F, 6.0F)
                        .texOffs(17, 0).addBox(-4.5F, -11.0F, 2.5F, 7.0F, 3.0F, 1.0F)
                        .texOffs(0, 0).addBox(-4.5F, -11.0F, -3.5F, 7.0F, 3.0F, 1.0F)
                        .texOffs(30, 12).addBox(-2.5F, -7.0F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 26.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bottomAnvil.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        topAnvil.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}