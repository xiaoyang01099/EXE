package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.KindMiaoModel;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;

public class MiaoMiaoRenderer extends MobRenderer<CatMewEntity, KindMiaoModel<CatMewEntity>> {
    public MiaoMiaoRenderer(EntityRendererProvider.Context context) {
        super(context, new KindMiaoModel<>(context.bakeLayer(ModModelLayers.MIAOMIAO_LAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this,
                new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
    }

    @Override
    public ResourceLocation getTextureLocation(CatMewEntity entity) {
        return new ResourceLocation("ex_enigmaticlegacy:textures/entity/miaomiao/kind_miao.png");
    }
}