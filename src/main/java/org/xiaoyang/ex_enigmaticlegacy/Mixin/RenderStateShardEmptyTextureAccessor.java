package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(RenderStateShard.EmptyTextureStateShard.class)
public interface RenderStateShardEmptyTextureAccessor {
    @Invoker("cutoutTexture")
    Optional<ResourceLocation> ex_enigmaticlegacy$cutoutTexture();
}