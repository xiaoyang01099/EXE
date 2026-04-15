package org.xiaoyang.ex_enigmaticlegacy.api.shader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

@SuppressWarnings("removal")
public class EndPortalHaloLoader implements IGeometryLoader<EndPortalHaloLoader.EndPortalHaloGeometry> {
    @Override
    public EndPortalHaloGeometry read(JsonObject json, JsonDeserializationContext ctx) {
        JsonObject portalObj = GsonHelper.getAsJsonObject(json, "portal_halo");

        int size       = GsonHelper.getAsInt(portalObj,     "size",     1);
        boolean pulse  = GsonHelper.getAsBoolean(portalObj, "pulse",    false);
        boolean animated = GsonHelper.getAsBoolean(portalObj, "animated", false);
        String styleStr  = GsonHelper.getAsString(portalObj, "style",   "halo");

        EndPortalHaloBakedModel.HaloStyle style = EndPortalHaloBakedModel.HaloStyle.valueOf(styleStr.toUpperCase());

        String maskModelLoc    = GsonHelper.getAsString(portalObj, "mask_model", "");
        ResourceLocation maskLocation = maskModelLoc.isEmpty() ? null : new ResourceLocation(maskModelLoc);

        JsonObject clean = json.deepCopy();
        clean.remove("portal_halo");
        clean.remove("loader");
        BlockModel baseModel = ctx.deserialize(clean, BlockModel.class);

        return new EndPortalHaloGeometry(baseModel, size, pulse, animated, style, maskLocation);
    }

    public static class EndPortalHaloGeometry implements IUnbakedGeometry<EndPortalHaloGeometry> {
        private final BlockModel baseModel;
        private final int size;
        private final boolean pulse;
        private final boolean animated;
        private final EndPortalHaloBakedModel.HaloStyle style;
        private final ResourceLocation maskLocation;

        public EndPortalHaloGeometry(BlockModel baseModel, int size, boolean pulse,
                                     boolean animated,
                                     EndPortalHaloBakedModel.HaloStyle style,
                                     ResourceLocation maskLocation) {
            this.baseModel    = baseModel;
            this.size         = size;
            this.pulse        = pulse;
            this.animated     = animated;
            this.style        = style;
            this.maskLocation = maskLocation;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel bakedBaseModel = this.baseModel.bake(
                    baker, this.baseModel, spriteGetter, modelState, modelLocation, false
            );

            BakedModel bakedPortalModel = null;
            if (this.maskLocation != null) {
                UnbakedModel unbakedMask = baker.getModel(this.maskLocation);
                bakedPortalModel = unbakedMask.bake(
                        baker, spriteGetter, modelState, this.maskLocation
                );
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
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
            if (this.maskLocation != null) {
                modelGetter.apply(this.maskLocation).resolveParents(modelGetter);
            }
        }
    }
}