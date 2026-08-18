package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface GaiaGuardianRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {

    @Accessor("model")
    M exe$getModel();

    @Accessor("layers")
    List<RenderLayer<T, M>> exe$getLayers();
}