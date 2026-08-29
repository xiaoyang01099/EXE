package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface RenderTypeCompositeRenderTypeAccessor {
    @Accessor("state")
    RenderType.CompositeState ex_enigmaticlegacy$getState();
}