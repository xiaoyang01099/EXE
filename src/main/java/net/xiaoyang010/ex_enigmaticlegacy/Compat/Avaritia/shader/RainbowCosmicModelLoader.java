package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model.RainBowCosmicBakeModel;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class RainbowCosmicModelLoader implements IModelLoader<RainbowCosmicModelLoader.RainbowCosmicGeometry> {

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {}

    @NotNull
    @Override
    public RainbowCosmicGeometry read(@NotNull JsonDeserializationContext deserializationContext,
                                      JsonObject modelContents) {
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

    public static class RainbowCosmicGeometry implements IModelGeometry<RainbowCosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextureNames;
        private final List<Material> maskMaterials = new ArrayList<>();

        public RainbowCosmicGeometry(BlockModel baseModel, List<String> maskTextureNames) {
            this.baseModel = baseModel;
            this.maskTextureNames = maskTextureNames;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner,
                               ModelBakery bakery,
                               Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelTransform,
                               ItemOverrides overrides,
                               ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(
                    bakery,
                    this.baseModel,
                    spriteGetter,
                    modelTransform,
                    modelLocation,
                    true
            );

            List<ResourceLocation> maskResourceLocations = new ArrayList<>();
            for (Material material : this.maskMaterials) {
                maskResourceLocations.add(material.texture());
            }

            return new RainBowCosmicBakeModel(baseBakedModel, maskResourceLocations);
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner,
                                                Function<ResourceLocation, UnbakedModel> modelGetter,
                                                Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> materials = new HashSet<>();

            for (String textureName : this.maskTextureNames) {
                Material material = owner.resolveTexture(textureName);

                if (Objects.equals(material.atlasLocation(), MissingTextureAtlasSprite.getLocation())) {
                    missingTextureErrors.add(Pair.of(textureName, owner.getModelName()));
                }

                this.maskMaterials.add(material);
                materials.add(material);
            }

            materials.addAll(this.baseModel.getMaterials(modelGetter, missingTextureErrors));

            return materials;
        }
    }
}