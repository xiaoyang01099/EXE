package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.EntityAdvancedSpark;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.render.entity.RenderSparkBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.helper.ColorHelper;

import javax.annotation.Nonnull;
import java.util.Random;

public class RenderAdvencedSpark extends RenderSparkBase<EntityAdvancedSpark> {

    public RenderAdvencedSpark(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected TextureAtlasSprite getBaseIcon(EntityAdvancedSpark entity) {
        return SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIcon.sprite();
    }

    @Override
    protected TextureAtlasSprite getSpinningIcon(EntityAdvancedSpark entity) {
        int upgrade = entity.getUpgrade().ordinal() - 1;
        return upgrade >= 0 && upgrade < MiscellaneousModels.INSTANCE.sparkUpgradeIcons.length
                ? MiscellaneousModels.INSTANCE.sparkUpgradeIcons[upgrade].sprite()
                : null;
    }

    @Override
    public void render(@Nonnull EntityAdvancedSpark tEntity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
        TextureAtlasSprite iicon = getBaseIcon(tEntity);

        ms.pushPose();

        double time = (tEntity.level.getGameTime() % 24000) + partialTicks + new Random(tEntity.getId()).nextInt(200);
        float a = 0.1F + (tEntity.isInvisible() ? 0 : 1) * 0.8F;

        int alpha = (int) ((0.7 + 0.3 * (Math.sin(time / 5.0) + 0.5) * 2) * a * 255.0);
        int iconColor = 0xFFFFFF | (alpha << 24);

        float scale = 0.75F + 0.1F * (float) Math.sin(time / 10);
        ms.scale(scale, scale, scale);

        VertexConsumer buffer = buffers.getBuffer(RenderHelper.SPARK);
        ms.pushPose();
        ms.mulPose(entityRenderDispatcher.cameraOrientation());
        ms.mulPose(Vector3f.YP.rotationDegrees(180));
        renderIcon(ms, buffer, iicon, iconColor);

        ms.pushPose();
        ms.translate(-0.02 + Math.sin(time / 20) * 0.2, 0.24 + Math.cos(time / 20) * 0.2, 0.005);
        ms.scale(0.2F, 0.2F, 0.2F);
        int starColor = ColorHelper.getColorValue(tEntity.getNetwork()) | ((int) (a * 255.0F) << 24);

        renderIcon(ms, buffer, SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIconStar.sprite(), starColor);
        ms.popPose();

        TextureAtlasSprite spinningIcon = getSpinningIcon(tEntity);
        if (spinningIcon != null) {
            ms.translate(-0.02 + Math.sin(time / 20) * -0.2, 0.24 + Math.cos(time / 20) * -0.2, 0.005);
            ms.scale(0.2F, 0.2F, 0.2F);
            renderIcon(ms, buffer, spinningIcon, iconColor);
        }
        ms.popPose();

        ms.popPose();
    }

    // 从基类复制的 renderIcon 方法，因为它是 private 的
    private void renderIcon(PoseStack ms, VertexConsumer buffer, TextureAtlasSprite icon, int color) {
        float f = icon.getU0();
        float f1 = icon.getU1();
        float f2 = icon.getV0();
        float f3 = icon.getV1();
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        int fullbright = 0xF000F0;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        Matrix4f mat = ms.last().pose();
        buffer.vertex(mat, 0.0F - f5, 0.0F - f6, 0.0F).color(r, g, b, a).uv(f, f3).uv2(fullbright).endVertex();
        buffer.vertex(mat, f4 - f5, 0.0F - f6, 0.0F).color(r, g, b, a).uv(f1, f3).uv2(fullbright).endVertex();
        buffer.vertex(mat, f4 - f5, f4 - f6, 0.0F).color(r, g, b, a).uv(f1, f2).uv2(fullbright).endVertex();
        buffer.vertex(mat, 0.0F - f5, f4 - f6, 0.0F).color(r, g, b, a).uv(f, f2).uv2(fullbright).endVertex();
    }
}