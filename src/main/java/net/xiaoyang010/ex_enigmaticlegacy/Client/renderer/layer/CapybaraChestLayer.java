package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.CapybaraModel;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CapybaraEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

;

public class CapybaraChestLayer extends RenderLayer<CapybaraEntity, CapybaraModel> {
    private static final ResourceLocation SINGLE_CHEST = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/capybara/single_chest.png");
    private static final ResourceLocation DOUBLE_CHEST = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/capybara/double_chest.png");

    public CapybaraChestLayer(RenderLayerParent<CapybaraEntity, CapybaraModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CapybaraEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        final int chestCount = entity.getChestCount();
        if (chestCount > 0) {
            CapybaraModel model = getParentModel();
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(
                    poseStack,
                    buffer.getBuffer(RenderType.entityCutoutNoCull(chestCount > 1 ? DOUBLE_CHEST : SINGLE_CHEST)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F
            );
        }
    }
}