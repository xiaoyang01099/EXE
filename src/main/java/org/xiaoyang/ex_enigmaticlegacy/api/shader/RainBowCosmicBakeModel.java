package org.xiaoyang.ex_enigmaticlegacy.api.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuo.endless.Client.Lib.PerspectiveModelState;
import com.yuo.endless.Client.Lib.TransformUtils;
import com.yuo.endless.Client.Model.IItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;

import java.util.ArrayList;
import java.util.List;

public final class RainBowCosmicBakeModel extends RainBowWrappedItemModel implements IItemRenderer {
    private final List<ResourceLocation> maskSprite;

    public RainBowCosmicBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType,
                           PoseStack pStack, MultiBufferSource source, int light, int overlay) {

        if (stack.getItem() == ModWeapons.AQUA_SWORD.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        }

        this.renderWrapped(stack, pStack, source, light, overlay, true);

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;

        if (RainbowAvaritiaShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else if (mc.player != null) {
            yaw   = (float)(mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float)(mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        RainbowAvaritiaShaders.cosmicTime.set(
                (float)(System.currentTimeMillis() - (long) RainbowAvaritiaShaders.renderTime) / 2000.0F
        );
        RainbowAvaritiaShaders.cosmicYaw.set(yaw);
        RainbowAvaritiaShaders.cosmicPitch.set(pitch);
        RainbowAvaritiaShaders.cosmicExternalScale.set(scale);

        if (RainbowAvaritiaShaders.FogColor != null) {
            float hue = ((float) System.currentTimeMillis() / 5000.0F) % 1.0F;
            int rgb = Mth.hsvToRgb(hue, 1.0F, 1.0F);

            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8)  & 0xFF) / 255.0F;
            float b = ( rgb        & 0xFF) / 255.0F;

            RainbowAvaritiaShaders.FogColor.set(r, g, b, 1.0F);
        }

        RainbowAvaritiaShaders.cosmicOpacity.set(1.0F);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(Exe.path("shader/cosmic_" + i));
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4    ] = sprite.getU0();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (RainbowAvaritiaShaders.cosmicUVs != null) {
            RainbowAvaritiaShaders.cosmicUVs.set(RainbowAvaritiaShaders.COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(RainbowAvaritiaShaders.RAINBOW_COSMIC_RENDER_TYPE);

        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : this.maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }

        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, light, overlay);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return this.parentState;
    }

    @Override
    public boolean isCosmic() {
        return true;
    }
}