package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.SpottedGardenEelHidingModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.SpottedGardenEelModel;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpottedGardenEelEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SpottedGardenEelRenderer extends MobRenderer<SpottedGardenEelEntity, EntityModel<SpottedGardenEelEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spottedgarden/spotted_garden_eel.png");

    public static final ModelLayerLocation EEL_MODEL_LAYER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "spotted_garden_eel"), "main");
    public static final ModelLayerLocation EEL_HIDING_MODEL_LAYER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "spotted_garden_eel_hiding"), "main");

    private final EntityModel<SpottedGardenEelEntity> normalModel;
    private final EntityModel<SpottedGardenEelEntity> hidingModel;

    public SpottedGardenEelRenderer(EntityRendererProvider.Context context) {
        super(context, null, 0.1F);


        this.normalModel = new SpottedGardenEelModel<>(context.bakeLayer(EEL_MODEL_LAYER));
        this.hidingModel = new SpottedGardenEelHidingModel(context.bakeLayer(EEL_HIDING_MODEL_LAYER));
        this.model = this.normalModel; // 设置默认模型
    }

    @Override
    public ResourceLocation getTextureLocation(SpottedGardenEelEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SpottedGardenEelEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        model = entity.isHidden() ? hidingModel : normalModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}