package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.SphereNavigation;

@OnlyIn(Dist.CLIENT)
public class SphereNavigationItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "items/res/sphere_navigation_0");
    private static final ResourceLocation BLOCK_FOUND_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "items/res/sphere_navigation_1");
    private static final ResourceLocation BLOCK_NOT_FOUND_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "items/res/sphere_navigation_2");
    private static final ResourceLocation ENABLED_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "items/res/sphere_navigation_3");
    private static final ResourceLocation DISABLED_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "items/res/sphere_navigation_4");

    public SphereNavigationItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform, PoseStack poseStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        poseStack.pushPose();

        switch (transform) {
            case GUI -> {
                poseStack.scale(16.0F, 16.0F, 16.0F);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                poseStack.translate(-1.0F, -1.0F, 0.0F);
            }
            case GROUND, FIXED -> {
                poseStack.translate(-0.5F, 0.0F, 0.0F);
                if (stack.hasFoil()) {
                    poseStack.translate(0.0F, -0.3F, 0.01F);
                }
            }
        }

        var textureAtlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

        TextureAtlasSprite baseSprite = textureAtlas.apply(BASE_TEXTURE);
        renderSprite(poseStack, buffer, baseSprite, combinedLight, combinedOverlay, 0.0F);

        boolean isEnabled = stack.getDamageValue() == 0;
        ResourceLocation enableTexture = isEnabled ? ENABLED_TEXTURE : DISABLED_TEXTURE;
        TextureAtlasSprite enableSprite = textureAtlas.apply(enableTexture);
        renderSprite(poseStack, buffer, enableSprite, combinedLight, combinedOverlay, 0.001F);

        double time = System.currentTimeMillis();

        if (transform != ItemTransforms.TransformType.GUI) {
            poseStack.scale(1.0F, 1.0F, 1.05F);
            poseStack.translate(0.0F, 0.0F, 0.0015625F);
        }

        boolean hasFoundBlock = SphereNavigation.getFindBlock(stack) != null;
        ResourceLocation blockTexture = hasFoundBlock ? BLOCK_FOUND_TEXTURE : BLOCK_NOT_FOUND_TEXTURE;

        if (hasFoundBlock) {
            double offsetY = Math.cos(time / 650.0) * 0.0075 - 0.015;
            poseStack.translate(0.0F, offsetY, 0.0F);
        }

        TextureAtlasSprite blockSprite = textureAtlas.apply(blockTexture);
        renderSprite(poseStack, buffer, blockSprite, combinedLight, combinedOverlay, 0.002F);

        poseStack.popPose();
    }

    private void renderSprite(PoseStack poseStack, MultiBufferSource buffer, TextureAtlasSprite sprite,
                              int combinedLight, int combinedOverlay, float zOffset) {

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        var pose = poseStack.last();

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        float size = 0.0625F;

        addVertex(vertexConsumer, pose, 0, 0, zOffset, minU, maxV, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, size, 0, zOffset, maxU, maxV, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, size, size, zOffset, maxU, minV, combinedLight, combinedOverlay);
        addVertex(vertexConsumer, pose, 0, size, zOffset, minU, minV, combinedLight, combinedOverlay);
    }

    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                           float u, float v, int combinedLight, int combinedOverlay) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}