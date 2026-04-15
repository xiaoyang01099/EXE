package org.xiaoyang.ex_enigmaticlegacy.api.shader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class RainbowCosmicModelLoader implements IGeometryLoader<RainbowCosmicModelLoader.RainbowCosmicGeometry> {

    public static final RainbowCosmicModelLoader INSTANCE = new RainbowCosmicModelLoader();

    @NotNull
    @Override
    public RainbowCosmicGeometry read(@NotNull JsonObject modelContents,
                                      @NotNull JsonDeserializationContext deserializationContext) {
        JsonObject rainbowCosmicObj = modelContents.getAsJsonObject("rainbow_cosmic");
        if (rainbowCosmicObj == null) {
            throw new IllegalStateException("Missing 'rainbow_cosmic' object in model JSON");
        }

        List<String> maskTextures = new ArrayList<>();

        if (rainbowCosmicObj.has("masks") && rainbowCosmicObj.get("masks").isJsonArray()) {
            JsonArray masksArray = rainbowCosmicObj.getAsJsonArray("masks");
            for (int i = 0; i < masksArray.size(); i++) {
                maskTextures.add(masksArray.get(i).getAsString());
            }
        } else if (rainbowCosmicObj.has("mask")) {
            if (rainbowCosmicObj.get("mask").isJsonArray()) {
                JsonArray masksArray = rainbowCosmicObj.getAsJsonArray("mask");
                for (int i = 0; i < masksArray.size(); i++) {
                    maskTextures.add(masksArray.get(i).getAsString());
                }
            } else {
                maskTextures.add(GsonHelper.getAsString(rainbowCosmicObj, "mask"));
            }
        }

        if (maskTextures.isEmpty()) {
            throw new IllegalStateException("No mask textures specified in rainbow_cosmic model");
        }

        JsonObject clean = modelContents.deepCopy();
        clean.remove("rainbow_cosmic");
        clean.remove("cosmic");
        clean.remove("loader");

        BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);

        return new RainbowCosmicGeometry(baseModel, maskTextures);
    }

    public static class RainbowCosmicGeometry implements IUnbakedGeometry<RainbowCosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextureNames;

        public RainbowCosmicGeometry(BlockModel baseModel, List<String> maskTextureNames) {
            this.baseModel = baseModel;
            this.maskTextureNames = maskTextureNames;
        }

        @SuppressWarnings("removal")  //禁用过期警告
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                               Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelState, ItemOverrides overrides,
                               ResourceLocation modelLocation) {

            BakedModel baseBakedModel = this.baseModel.bake(
                    baker, this.baseModel, spriteGetter,
                    modelState, modelLocation, true
            );

            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextureNames.forEach(mask ->
                    textures.add(new ResourceLocation(mask))
            );

            return new RainBowCosmicBakeModel(baseBakedModel, textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                                   IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}