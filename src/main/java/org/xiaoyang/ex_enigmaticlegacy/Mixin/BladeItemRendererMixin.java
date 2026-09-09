package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash.HeldItemTrails;

@Mixin(ItemRenderer.class)
public abstract class BladeItemRendererMixin {
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"))
    private void bladeflash$begin(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                 PoseStack pose, MultiBufferSource buffers, Level level, int light, int overlay,
                                 int seed, CallbackInfo ci) {
        HeldItemTrails.beginItem(entity, stack, context, leftHand);
    }

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("RETURN"))
    private void bladeflash$end(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                               PoseStack pose, MultiBufferSource buffers, Level level, int light, int overlay,
                               int seed, CallbackInfo ci) {
        HeldItemTrails.endItem();
    }

    @Inject(method = "renderModelLists", at = @At("HEAD"))
    private void bladeflash$sample(BakedModel model, ItemStack stack, int light, int overlay, PoseStack pose,
                                  VertexConsumer consumer, CallbackInfo ci) {
        HeldItemTrails.sampleModel(pose);
    }
}
