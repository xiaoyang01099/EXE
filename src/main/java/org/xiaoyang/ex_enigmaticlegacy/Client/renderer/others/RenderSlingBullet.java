package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.EntitySlingBullet;

@OnlyIn(Dist.CLIENT)
public class RenderSlingBullet extends EntityRenderer<EntitySlingBullet> {
    private final ItemRenderer itemRenderer;

    public RenderSlingBullet(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EntitySlingBullet entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getEntityItem();

        if (stack.isEmpty()) {
            stack = new ItemStack(Items.APPLE);
        }

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        this.itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySlingBullet entity) {
        return new ResourceLocation("textures/misc/white.png");
    }
}