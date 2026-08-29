package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Mixin.RenderStateShardEmptyTextureAccessor;
import org.xiaoyang.ex_enigmaticlegacy.Mixin.RenderTypeCompositeRenderTypeAccessor;
import org.xiaoyang.ex_enigmaticlegacy.Mixin.RenderTypeCompositeStateAccessor;

import java.util.Optional;

public class MiaoOutlineRenderTypeTextureResolver {
    public static Optional<ResourceLocation> resolve(RenderType renderType) {
        if (!(renderType instanceof RenderTypeCompositeRenderTypeAccessor compositeAccessor)) {
            return Optional.empty();
        }
        RenderType.CompositeState state = compositeAccessor.ex_enigmaticlegacy$getState();
        if (!((Object) state instanceof RenderTypeCompositeStateAccessor stateAccessor)) {
            return Optional.empty();
        }
        RenderStateShard.EmptyTextureStateShard textureState = stateAccessor.ex_enigmaticlegacy$getTextureState();
        if (!(textureState instanceof RenderStateShardEmptyTextureAccessor textureAccessor)) {
            return Optional.empty();
        }
        return textureAccessor.ex_enigmaticlegacy$cutoutTexture();
    }
}
