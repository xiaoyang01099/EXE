package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.mixin.client.AccessorRenderType;

public final class SpecialRenderHelper extends RenderType {

    public static final RenderType RAINBOW_MANA_WATER;

    private SpecialRenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode,
                                int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        throw new UnsupportedOperationException("Should not be instantiated");
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, boolean hasCrumbling, boolean sortOnUpload, CompositeState glState) {
        return AccessorRenderType.create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, CompositeState glState) {
        return makeLayer(name, format, mode, bufSize, false, false, glState);
    }

    static {
        RenderType.CompositeState glState = RenderType.CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::rainbowManaWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);

        RAINBOW_MANA_WATER = makeLayer(ExEnigmaticlegacyMod.MODID + ":rainbow_mana_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);
    }

    /**
     * 渲染图标到缓冲区
     */
    public static void renderIcon(PoseStack ms, VertexConsumer buffer, int x, int y,
                                  TextureAtlasSprite icon, int width, int height, float alpha) {
        Matrix4f mat = ms.last().pose();
        int fullbright = 0xF000F0;
        buffer.vertex(mat, x, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV1()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x + width, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV1()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x + width, y, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV0()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x, y, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV0()).uv2(fullbright).endVertex();
    }
}