package net.xiaoyang010.ex_enigmaticlegacy.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.covers1624.quack.gson.JsonUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class EndPortalHaloLoader implements IModelLoader<EndPortalHaloLoader.EndPortalHaloGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public EndPortalHaloGeometry read(JsonDeserializationContext ctx, JsonObject json) {
        JsonObject portalObj = json.getAsJsonObject("portal_halo");
        if (portalObj == null) {
            throw new IllegalStateException("Missing 'portal_halo' object.");
        }

        int size = JsonUtils.getInt(portalObj, "size");
        boolean pulse = JsonUtils.getAsPrimitive(portalObj, "pulse").getAsBoolean();
        boolean animated = JsonUtils.getAsPrimitive(portalObj, "animated").getAsBoolean();

        // ✅ 新增：读取遮罩纹理
        String maskTexture = JsonUtils.getString(portalObj, "mask");

        JsonObject clean = json.deepCopy();
        clean.remove("portal_halo");
        clean.remove("loader");
        BlockModel baseModel = ctx.deserialize(clean, BlockModel.class);

        return new EndPortalHaloGeometry(baseModel, size, pulse, animated, maskTexture);
    }

    public static class EndPortalHaloGeometry implements IModelGeometry<EndPortalHaloGeometry> {
        private final BlockModel baseModel;
        private final int size;
        private final boolean pulse;
        private final boolean animated;
        private final String maskTexture;

        private Material portalMaterial;
        private Material maskMaterial;  // ✅ 新增

        public EndPortalHaloGeometry(BlockModel baseModel, int size, boolean pulse,
                                     boolean animated, String maskTexture) {
            this.baseModel = baseModel;
            this.size = size;
            this.pulse = pulse;
            this.animated = animated;
            this.maskTexture = maskTexture;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner,
                               ModelBakery bakery,
                               Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelTransform,
                               ItemOverrides overrides,
                               ResourceLocation modelLocation) {

            BakedModel bakedBaseModel = this.baseModel.bake(
                    bakery, this.baseModel, spriteGetter,
                    modelTransform, modelLocation, false
            );

            TextureAtlasSprite portalSprite = spriteGetter.apply(this.portalMaterial);
            TextureAtlasSprite maskSprite = spriteGetter.apply(this.maskMaterial);  // ✅ 获取遮罩

            return new EndPortalHaloBakedModel(
                    bakedBaseModel,
                    portalSprite,
                    maskSprite,  // ✅ 传递遮罩
                    this.size,
                    this.pulse,
                    this.animated
            );
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner,
                                                Function<ResourceLocation, UnbakedModel> modelGetter,
                                                Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> materials = new HashSet<>();

            // 末地传送门纹理（原版）
            this.portalMaterial = new Material(
                    net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS,
                    new ResourceLocation("minecraft:block/end_portal")
            );
            materials.add(this.portalMaterial);

            // ✅ 遮罩纹理
            this.maskMaterial = owner.resolveTexture(this.maskTexture);
            if (Objects.equals(this.maskMaterial.texture(),
                    MissingTextureAtlasSprite.getLocation())) {
                missingTextureErrors.add(Pair.of(this.maskTexture, owner.getModelName()));
            }
            materials.add(this.maskMaterial);

            // 基础模型纹理
            materials.addAll(this.baseModel.getMaterials(modelGetter, missingTextureErrors));

            return materials;
        }
    }
}