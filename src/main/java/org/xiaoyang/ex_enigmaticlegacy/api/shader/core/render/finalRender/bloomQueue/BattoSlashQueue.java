package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.BattoSlashEntity;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.BattoSlashRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.BattoSlashShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.util.List;

public class BattoSlashQueue extends EntityQueue<BattoSlashEntity> {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 世界上方向。
    public static final int POSITION_X_OFFSET = 0; // baked quad 顶点 X 偏移。
    public static final int POSITION_Y_OFFSET = 1; // baked quad 顶点 Y 偏移。
    public static final int POSITION_Z_OFFSET = 2; // baked quad 顶点 Z 偏移。
    public static final int UV_U_OFFSET = 4; // baked quad 顶点 U 偏移。
    public static final int UV_V_OFFSET = 5; // baked quad 顶点 V 偏移。
    public static final double OBJ_BASE_SCALE = 0.012D; // OBJ 原始坐标基础缩放，避免几百单位的模型坐标直接进世界。
    public static final double OBJ_CENTER_X = 31.8D; // sword1 OBJ 横向中心，用于把刀光放回实体中心附近。
    public static final double OBJ_CENTER_Y = 0.0D; // sword1 OBJ 厚度中心，用于保留薄的竖向厚度。
    public static final double OBJ_CENTER_Z = 0.0D; // sword1 OBJ 前后中心，用于把弧面铺在水平面。
    public static final double SIDE_SCALE = 1.0D; // 横向调试倍率，先不做 20 格视觉放大。
    public static final double UP_SCALE = 1.0D; // 竖向调试倍率，先不做额外竖向放大。
    public static final double FORWARD_SCALE = 1.0D; // 前后调试倍率，先不做额外前后放大。
    public static final double VISUAL_RANGE_SCALE = 10.0D; // 拔刀斩视觉水平范围调试倍率。
    public static final double FRONT_OFFSET = 2.2D; // 拔刀斩整体向释放者前方偏移，避免渲染到玩家背后。
    public static final double HEIGHT_OFFSET = 0.65D; // 视觉中心高度，调低可让拔刀斩更贴近地面。
    public static final float BLOOM_STRENGTH = 1.6F; // 拔刀斩 bloom 强度。
    public static final float NOISE_STRENGTH = 0.08F; // daoguang 噪声扰动强度。
    public static final float MATERIAL_INTENSITY = 1.15F; // daoguang 自发光整体强度。
    public TextureAtlasSprite mainSprite; // sword1 主纹理 sprite。
    public TextureAtlasSprite texBSprite; // daoguang 暖色自发光 sprite。
    public TextureAtlasSprite maskSprite; // daoguang 透明遮罩 sprite。

    @Override
    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        if (!BattoSlashShader.isLoaded()) return;
        if (entities.isEmpty()) return;
        if (!SwordAuraObjModel.isLoaded()) return;

        TextureAtlasSprite main = getMainSprite();
        TextureAtlasSprite texB = getTexBSprite();
        TextureAtlasSprite mask = getMaskSprite();
        BakedModel model = SwordAuraObjModel.getModel();
        if (main == null || texB == null || mask == null || model == null) return;

        BattoSlashShader.setMaterialParams(MathUtil.getClientTime(partialTick), BLOOM_STRENGTH, NOISE_STRENGTH, MATERIAL_INTENSITY);
        BattoSlashShader.setPannerParams(0.10F, 0.0F, 0.15F, 0.0F);
        BattoSlashShader.setSpriteUVs(
                main.getU0(), main.getV0(), main.getU1(), main.getV1(),
                texB.getU0(), texB.getV0(), texB.getU1(), texB.getV1(),
                mask.getU0(), mask.getV0(), mask.getU1(), mask.getV1()
        );
        BattoSlashShader.setView(viewMatrix);
        BattoSlashShader.setSamplers(EXETextureAtlas.EXE_TOOL_ATLAS.getId());

        VertexConsumer consumer = fboBuffer.getBuffer(BattoSlashRenderType.getRenderType());
        for (BattoSlashEntity entity : entities) {
            renderSlash(consumer, entity, model, main, partialTick);
        }
        fboBuffer.endBatch(BattoSlashRenderType.getRenderType());
    }

    @Override
    public PostRenderPhase getPhase() {
        return PostRenderPhase.ALWAYS_VISIBLE_WORLD;
    }

    public void renderSlash(VertexConsumer consumer, BattoSlashEntity entity, BakedModel model, TextureAtlasSprite targetSprite, float partialTick) {
        float progress = entity.getProgress(partialTick);
        float alpha = getAlpha(progress);
        if (alpha <= 0.003F) return;
        List<BakedQuad> quads = model.getQuads(null, null, RandomSource.create(0L));
        for (BakedQuad quad : quads) {
            writeModelQuad(consumer, entity, quad, targetSprite, progress, alpha);
        }
    }

    public float getAlpha(float progress) {
        return Mth.clamp(progress / 0.05F, 0.0F, 1.0F);
    }

    public void writeModelQuad(VertexConsumer consumer, BattoSlashEntity entity, BakedQuad quad, TextureAtlasSprite targetSprite, float progress, float alpha) {
        int[] vertices = quad.getVertices();
        int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
        TextureAtlasSprite sourceSprite = quad.getSprite();
        Vec3 forward = entity.getForward();
        Vec3 side = entity.getTiltedSide();
        Vec3 up = entity.getTiltedUp();
        Vec3 center = entity.position().add(forward.scale(FRONT_OFFSET)).add(WORLD_UP.scale(HEIGHT_OFFSET));
        for (int index = 0; index < 4; index++) {
            int base = index * stride;
            float localX = Float.intBitsToFloat(vertices[base + POSITION_X_OFFSET]);
            float localY = Float.intBitsToFloat(vertices[base + POSITION_Y_OFFSET]);
            float localZ = Float.intBitsToFloat(vertices[base + POSITION_Z_OFFSET]);
            float sourceU = Float.intBitsToFloat(vertices[base + UV_U_OFFSET]);
            float sourceV = Float.intBitsToFloat(vertices[base + UV_V_OFFSET]);
            Vec3 local = buildBattoSlashLocal(localX, localY, localZ);
            Vec3 world = center
                    .add(side.scale(local.x * SIDE_SCALE * VISUAL_RANGE_SCALE))
                    .add(up.scale(local.y * UP_SCALE))
                    .add(forward.scale(local.z * FORWARD_SCALE * VISUAL_RANGE_SCALE));
            float u = remapToLocalU(sourceSprite, targetSprite, sourceU);
            float v = remapToLocalV(sourceSprite, targetSprite, sourceV);
            writeVertex(consumer, world, u, v, progress, alpha);
        }
    }

    public void writeVertex(VertexConsumer consumer, Vec3 world, float u, float v, float progress, float alpha) {
        int progressByte = Mth.clamp((int) (progress * 255.0F), 0, 255);
        int alphaByte = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        consumer.vertex(world.x, world.y, world.z)
                .color(progressByte, 255, 255, alphaByte)
                .uv(u, v)
                .endVertex();
    }

    public Vec3 buildBattoSlashLocal(float localX, float localY, float localZ) {
        double side = (localX - OBJ_CENTER_X) * OBJ_BASE_SCALE;
        double up = (localY - OBJ_CENTER_Y) * OBJ_BASE_SCALE;
        double forward = -(localZ - OBJ_CENTER_Z) * OBJ_BASE_SCALE;
        return new Vec3(side, up, forward);
    }

    public float remapToLocalU(TextureAtlasSprite sourceSprite, TextureAtlasSprite targetSprite, float sourceU) {
        if (sourceSprite == null) {
            return Mth.clamp(targetSprite.getUOffset(sourceU) / 16.0F, 0.0F, 1.0F);
        }
        return Mth.clamp(sourceSprite.getUOffset(sourceU) / 16.0F, 0.0F, 1.0F);
    }

    public float remapToLocalV(TextureAtlasSprite sourceSprite, TextureAtlasSprite targetSprite, float sourceV) {
        if (sourceSprite == null) {
            return Mth.clamp(targetSprite.getVOffset(sourceV) / 16.0F, 0.0F, 1.0F);
        }
        return Mth.clamp(sourceSprite.getVOffset(sourceV) / 16.0F, 0.0F, 1.0F);
    }

    public TextureAtlasSprite getMainSprite() {
        if (mainSprite == null) {
            mainSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.SWORD_AURA_TEXTURE);
        }
        return mainSprite;
    }

    public TextureAtlasSprite getTexBSprite() {
        if (texBSprite == null) {
            texBSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.BATTO_TEX_B_TEXTURE);
        }
        return texBSprite;
    }

    public TextureAtlasSprite getMaskSprite() {
        if (maskSprite == null) {
            maskSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.BATTO_MASK_TEXTURE);
        }
        return maskSprite;
    }

}
