package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.PolychromeCollapsePrismTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import vazkii.botania.client.core.handler.ClientTickHandler;
import javax.annotation.Nonnull;
import java.util.List;

public class PolychromeCollapsePrismRenderer implements BlockEntityRenderer<PolychromeCollapsePrismTile> {

    public PolychromeCollapsePrismRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull PolychromeCollapsePrismTile prism, float partialTicks, PoseStack ms,
                       MultiBufferSource buffers, int light, int overlay) {

        float completion = prism.getCompletion();
        Level level = prism.getLevel();

        if (level == null) return;

        renderBaseTerraPlateEffect(prism, partialTicks, ms, buffers, light, overlay);

        if (completion > 0) {
            // 渲染物品浮动特效
            renderFloatingItemEffects(prism, partialTicks, ms, buffers, level, light, overlay);

            // 渲染进度环特效
            if (completion > 0.1f) {
                renderProgressRing(prism, partialTicks, ms, buffers, completion, light, overlay);
            }

            // 渲染中心光芒特效
            if (completion > 0.3f) {
                renderCentralGlow(prism, partialTicks, ms, buffers, completion, light, overlay);
            }
        }
    }

    private void renderBaseTerraPlateEffect(PolychromeCollapsePrismTile prism, float partialTicks,
                                            PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        float completion = prism.getCompletion();
        float alphaMod = Math.min(1.0F, completion / 0.1F);

        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(90F));
        ms.translate(0F, 0F, -3F / 16F - 0.001F);

        float baseAlpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 8D) + 1D) / 5D + 0.6D) * alphaMod;

        float time = (ClientTickHandler.ticksInGame + partialTicks) / 20.0F;
        float hue = (time % 120) / 120.0F;

        VertexConsumer buffer = buffers.getBuffer(SpecialRenderHelper.POLYCHROME_COLLAPSE_PRISM);

        ms.pushPose();
//        ms.mulPose(Vector3f.ZP.rotationDegrees(time * 0.5F % 360F));
        SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.sprite(), 1, 1, baseAlpha);
        ms.popPose();

        if (completion > 0.2f) {
            ms.pushPose();
            ms.scale(1.2F, 1.2F, 1.2F);
//            ms.mulPose(Vector3f.ZP.rotationDegrees(-time * 0.3F % 360F));
            SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.sprite(), 1, 1, baseAlpha * 0.6F);
            ms.popPose();
        }

        ms.popPose();
    }

    private void renderFloatingItemEffects(PolychromeCollapsePrismTile prism, float partialTicks,
                                           PoseStack ms, MultiBufferSource buffers, Level level, int light, int overlay) {
        AABB searchBox = new AABB(prism.getBlockPos()).inflate(0.5, 0.5, 0.5);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox,
                entity -> entity.isAlive() && !entity.getItem().isEmpty());

        if (items.isEmpty()) return;

        for (ItemEntity item : items) {
            ms.pushPose();

            ms.translate(
                    item.getX() - prism.getBlockPos().getX(),
                    item.getY() - prism.getBlockPos().getY() + 0.1,
                    item.getZ() - prism.getBlockPos().getZ()
            );

            float time = ClientTickHandler.ticksInGame + partialTicks;

            renderItemAura(ms, buffers, time, light, overlay);

            ms.popPose();
        }
    }

    private void renderItemAura(PoseStack ms, MultiBufferSource buffers, float time, int light, int overlay) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.translucent());

        ms.pushPose();
        ms.mulPose(Vector3f.YP.rotationDegrees(time * 2.0F));

        float hue = (time / 60.0F) % 1.0F;
        float[] rgb = hsvToRgb(hue, 0.8F, 1.0F);
        float alpha = 0.3F + 0.2F * Mth.sin(time / 10.0F);

        renderColoredSquare(ms, buffer, 0.3F, rgb[0], rgb[1], rgb[2], alpha, light, overlay);

        ms.popPose();
    }

    private void renderProgressRing(PolychromeCollapsePrismTile prism, float partialTicks,
                                    PoseStack ms, MultiBufferSource buffers, float completion, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5F, 0.05F, 0.5F);

        float time = ClientTickHandler.ticksInGame + partialTicks;

        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(90F));
        ms.mulPose(Vector3f.ZP.rotationDegrees(time * 1.0F));

        float ringScale = 1.0F + 0.1F * Mth.sin(time / 15.0F);
        ms.scale(ringScale, ringScale, 1.0F);

        VertexConsumer buffer = buffers.getBuffer(SpecialRenderHelper.POLYCHROME_COLLAPSE_PRISM);

        float hue = (time / 40.0F) % 1.0F;
        float alpha = 0.6F * completion;

        SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.sprite(), 1, 1, alpha);

        ms.popPose();

        if (completion > 0.5f) {
            ms.pushPose();
            ms.mulPose(Vector3f.XP.rotationDegrees(90F));
            ms.mulPose(Vector3f.ZP.rotationDegrees(-time * 1.5F));

            float innerScale = 0.6F + 0.1F * Mth.sin(time / 10.0F);
            ms.scale(innerScale, innerScale, 1.0F);

            float innerAlpha = 0.4F * completion;
            SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.sprite(), 1, 1, innerAlpha);

            ms.popPose();
        }

        ms.popPose();
    }

    private void renderCentralGlow(PolychromeCollapsePrismTile prism, float partialTicks,
                                   PoseStack ms, MultiBufferSource buffers, float completion, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5F, 0.1F, 0.5F);

        float time = ClientTickHandler.ticksInGame + partialTicks;

        for (int i = 0; i < 3; i++) {
            ms.pushPose();

            float layerScale = 0.8F + i * 0.2F + 0.1F * Mth.sin(time / (10.0F + i * 5.0F));
            float layerRotation = time * (0.5F + i * 0.3F);
            float layerHeight = 0.02F * i;

            ms.translate(0, layerHeight, 0);
            ms.mulPose(Vector3f.XP.rotationDegrees(90F));
            ms.mulPose(Vector3f.ZP.rotationDegrees(layerRotation));
            ms.scale(layerScale, layerScale, 1.0F);

            float hue = ((time / 30.0F) + i * 0.33F) % 1.0F;
            float alpha = (0.3F - i * 0.08F) * completion;

            VertexConsumer buffer = buffers.getBuffer(SpecialRenderHelper.POLYCHROME_COLLAPSE_PRISM);
            SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.sprite(), 1, 1, alpha);

            ms.popPose();
        }

        ms.popPose();
    }

    private void renderColoredSquare(PoseStack ms, VertexConsumer buffer, float size,
                                     float r, float g, float b, float alpha, int light, int overlay) {
        float halfSize = size / 2;

        buffer.vertex(ms.last().pose(), -halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 0)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(ms.last().pose(), -halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 1)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(ms.last().pose(), halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 1)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(ms.last().pose(), halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 0)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();
    }

    private float[] hsvToRgb(float h, float s, float v) {
        float[] rgb = new float[3];

        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch (i % 6) {
            case 0: rgb[0] = v; rgb[1] = t; rgb[2] = p; break;
            case 1: rgb[0] = q; rgb[1] = v; rgb[2] = p; break;
            case 2: rgb[0] = p; rgb[1] = v; rgb[2] = t; break;
            case 3: rgb[0] = p; rgb[1] = q; rgb[2] = v; break;
            case 4: rgb[0] = t; rgb[1] = p; rgb[2] = v; break;
            case 5: rgb[0] = v; rgb[1] = p; rgb[2] = q; break;
        }

        return rgb;
    }
}