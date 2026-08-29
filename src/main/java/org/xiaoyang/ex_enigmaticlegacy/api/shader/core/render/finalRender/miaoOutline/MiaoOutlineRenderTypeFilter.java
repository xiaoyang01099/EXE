package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Optional;

public class MiaoOutlineRenderTypeFilter {
    public static boolean shouldCapture(RenderType renderType, Optional<ResourceLocation> texture) {
        if (renderType == null) return false;
        if (isGlintRenderType(renderType)) return false;
        return texture.isEmpty() || !isGlintTexture(texture.get());
    }

    public static boolean isGlintRenderType(RenderType renderType) {
        if (renderType == null) return false;
        String renderTypeName = renderType.toString().toLowerCase(Locale.ROOT);
        return renderTypeName.contains("glint") || renderTypeName.contains("foil");
    }

    public static boolean isGlintTexture(ResourceLocation texture) {
        if (texture == null) return false;
        String namespace = texture.getNamespace().toLowerCase(Locale.ROOT);
        String path = texture.getPath().toLowerCase(Locale.ROOT);
        if (!"minecraft".equals(namespace)) return path.contains("enchanted_glint") || path.contains("glint");
        return path.contains("textures/misc/enchanted_glint") || path.contains("glint");
    }
}
