package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.covers1624.quack.gson.JsonUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model.EndPortalHaloBakedModel;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class EndPortalHaloLoader implements IModelLoader<EndPortalHaloLoader.EndPortalHaloGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public EndPortalHaloGeometry read(JsonDeserializationContext ctx, JsonObject json) {
        JsonObject portalObj = json.getAsJsonObject("portal_halo");
        if (portalObj == null) throw new IllegalStateException("Missing 'portal_halo' object.");

        int size = JsonUtils.getInt(portalObj, "size");
        boolean pulse = JsonUtils.getAsPrimitive(portalObj, "pulse").getAsBoolean();
        boolean animated = JsonUtils.getAsPrimitive(portalObj, "animated").getAsBoolean();
        String styleStr = JsonUtils.getString(portalObj, "style", "halo");
        EndPortalHaloBakedModel.HaloStyle style = EndPortalHaloBakedModel.HaloStyle.valueOf(styleStr.toUpperCase());

        String maskModelLoc = JsonUtils.getString(portalObj, "mask_model", "");
        ResourceLocation maskLocation = maskModelLoc.isEmpty() ? null : new ResourceLocation(maskModelLoc);

        JsonObject clean = json.deepCopy();
        clean.remove("portal_halo");
        clean.remove("loader");
        BlockModel baseModel = ctx.deserialize(clean, BlockModel.class);

        return new EndPortalHaloGeometry(baseModel, size, pulse, animated, style, maskLocation);
    }

    public static class EndPortalHaloGeometry implements IModelGeometry<EndPortalHaloGeometry> {
        private final BlockModel baseModel;
        private final int size;
        private final boolean pulse;
        private final boolean animated;
        private final EndPortalHaloBakedModel.HaloStyle style;
        private final ResourceLocation maskLocation;

        public EndPortalHaloGeometry(BlockModel baseModel, int size, boolean pulse, boolean animated,
                                     EndPortalHaloBakedModel.HaloStyle style, ResourceLocation maskLocation) {
            this.baseModel = baseModel;
            this.size = size;
            this.pulse = pulse;
            this.animated = animated;
            this.style = style;
            this.maskLocation = maskLocation;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel bakedBaseModel = this.baseModel.bake(bakery, this.baseModel, spriteGetter, modelTransform, modelLocation, false);

            BakedModel bakedPortalModel = null;
            if (this.maskLocation != null) {
                UnbakedModel unbakedMask = bakery.getModel(this.maskLocation);
                bakedPortalModel = unbakedMask.bake(bakery, (m) -> spriteGetter.apply(m), modelTransform, this.maskLocation);
            }

            return new EndPortalHaloBakedModel(
                    bakedBaseModel,
                    bakedPortalModel,
                    this.size,
                    this.pulse,
                    this.animated,
                    this.style
            );
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> materials = (Set<Material>) this.baseModel.getMaterials(modelGetter, missingTextureErrors);
            if (this.maskLocation != null) {
                UnbakedModel maskModel = modelGetter.apply(this.maskLocation);
                materials.addAll(maskModel.getMaterials(modelGetter, missingTextureErrors));
            }
            return materials;
        }
    }
}