package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface RenderTypeCompositeStateAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard ex_enigmaticlegacy$getTextureState();
}