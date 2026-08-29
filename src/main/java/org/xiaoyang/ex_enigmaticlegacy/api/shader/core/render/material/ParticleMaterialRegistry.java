package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.nio.FloatBuffer;
import java.util.EnumMap;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43.glBindBufferBase;

// ParticleMaterialRegistry 统一注册 GPU 粒子材质，并把贴图 UV 与噪声参数上传到材质 SSBO。
public class ParticleMaterialRegistry {
    public static final int DEFAULT_SDF_ID = 0; // 旧粒子默认材质 ID。
    public static final int LIGHT_EFFECT_ID = 1; // 三噪声光效粒子材质 ID。
    public static final int DIRECTED_LIGHT_EFFECT_ID = 2; // 定向三噪声光效粒子材质 ID。
    public static final int MAGIC_CIRCLE_ENERGY_ID = 3; // 法阵能量基础粒子材质 ID。
    public static final int SHOCKWAVE_MAGIC_CIRCLE_ID = 4; // 冲击波法阵粒子材质 ID。
    public static final int EX_SWORD_WAVE_ID = 5; // EX 剑气粒子材质 ID。
    public static final int STAR_TEXTURE_ID = 6; // 星星贴图粒子材质 ID。
    public static final int RISING_SHOCKWAVE_ID = 7; // 上升冲击波圆台材质 ID。
    public static final int FLOATS_PER_MATERIAL = 28; // 每个材质 7 个 vec4。
    public static final int MATERIAL_BUFFER_BINDING = 2; // 材质表 SSBO 绑定点。

    public static final ParticleMaterial[] MATERIALS_BY_ID = new ParticleMaterial[] {
            new ParticleMaterial(DEFAULT_SDF_ID, ParticleMaterialKey.DEFAULT_SDF, ParticleRenderPipeline.SDF_BASIC,
                    null, null, null, null,
                    1.0F, 1.0F, 0.0F, 0.0F,
                    0.35F, 0.75F, 0.08F, 1.0F),
            new ParticleMaterial(LIGHT_EFFECT_ID, ParticleMaterialKey.LIGHT_EFFECT, ParticleRenderPipeline.LIGHT_EFFECT,
                    EXETextureAtlas.T_FX_TILE_0012_TEXTURE, EXETextureAtlas.fx_noise015, EXETextureAtlas.T_FX_TILE_0137_MOON_TEXTURE, EXETextureAtlas.noise_092_128x,
                    2.6F, 3.8F, 0.72F, 0.62F,
                    1.00F, 2.25F, 0.32F, 1.0F),
            new ParticleMaterial(DIRECTED_LIGHT_EFFECT_ID, ParticleMaterialKey.DIRECTED_LIGHT_EFFECT, ParticleRenderPipeline.DIRECTED_LIGHT_EFFECT,
                    EXETextureAtlas.T_FX_TILE_0012_TEXTURE, EXETextureAtlas.fx_noise015, EXETextureAtlas.T_FX_TILE_0137_MOON_TEXTURE, EXETextureAtlas.noise_092_128x,
                    2.6F, 3.8F, 0.72F, 0.62F,
                    1.00F, 2.25F, 0.32F, 1.0F),
            new ParticleMaterial(MAGIC_CIRCLE_ENERGY_ID, ParticleMaterialKey.MAGIC_CIRCLE_ENERGY, ParticleRenderPipeline.MAGIC_CIRCLE_ENERGY,
                    EXETextureAtlas.tex_pattern66, EXETextureAtlas.tex_pattern59, null, null,
                    2.0F, 2.0F, 0.35F, 0.50F,
                    0.65F, 0.15F, 0.18F, 1F),
            new ParticleMaterial(SHOCKWAVE_MAGIC_CIRCLE_ID, ParticleMaterialKey.SHOCKWAVE_MAGIC_CIRCLE, ParticleRenderPipeline.MAGIC_CIRCLE_ENERGY,
                    EXETextureAtlas.CIRCLE_SHOCKWAVE_TEXTURE, EXETextureAtlas.tex_pattern59, null, null,
                    5.0F, 3.0F, 0.8F, 0.1F,
                    0.55F, 1.15F, 0.18F, 1F),
            new ParticleMaterial(EX_SWORD_WAVE_ID, ParticleMaterialKey.EX_SWORD_WAVE, ParticleRenderPipeline.EX_SWORD_WAVE,
                    EXETextureAtlas.ex_wave1, EXETextureAtlas.ex_wave2, EXETextureAtlas.noise_054, null,
                    1.0F, 1.0F, 0.0F, 0.0F,
                    0.65F, 0.85F, 0.12F, 1.0F),
            new ParticleMaterial(STAR_TEXTURE_ID, ParticleMaterialKey.STAR_TEXTURE, ParticleRenderPipeline.STAR_TEXTURE,
                    EXETextureAtlas.AI_STAR_TEXTURE, null, null, null,
                    1.0F, 1.0F, 0.0F, 0.0F,
                    0.85F, 0.30F, 0.20F, 1.0F),
            new ParticleMaterial(RISING_SHOCKWAVE_ID, ParticleMaterialKey.RISING_SHOCKWAVE, ParticleRenderPipeline.RISING_SHOCKWAVE,
                    EXETextureAtlas.T_FX_TILE_0016_TEXTURE, null, null, null,
                    1.0F, 1.0F, 0.0F, 0.0F,
                    1.15F, 1.80F, 0.26F, 1.0F)
    }; // 固定材质表，数组下标就是 materialId。
    public static final EnumMap<ParticleMaterialKey, ParticleMaterial> MATERIALS_BY_KEY = buildMaterialsByKey(); // Java 侧按 key 快速查找材质。

    public static int materialSsbo; // GPU 材质表 SSBO。
    public static boolean dirty = true; // atlas 重载后重新上传 UV。

    public ParticleMaterialRegistry() {}

    // 构建 key 到材质的映射，GPU 上传顺序仍由 MATERIALS_BY_ID 保证。
    public static EnumMap<ParticleMaterialKey, ParticleMaterial> buildMaterialsByKey() {
        EnumMap<ParticleMaterialKey, ParticleMaterial> map = new EnumMap<>(ParticleMaterialKey.class);
        for (ParticleMaterial material : MATERIALS_BY_ID) {
            map.put(material.key, material);
        }
        return map;
    }

    // 根据材质 key 返回稳定材质 ID，未知值回退到默认 SDF。
    public static int idOf(ParticleMaterialKey key) {
        if (key == null) return DEFAULT_SDF_ID;
        ParticleMaterial material = MATERIALS_BY_KEY.get(key);
        return material == null ? DEFAULT_SDF_ID : material.id;
    }

    // 根据材质 ID 返回渲染批次 ID，越界时回退到 SDF 批次。
    public static int pipelineIdOf(int materialId) {
        if (materialId < 0 || materialId >= MATERIALS_BY_ID.length) {
            return ParticleRenderPipeline.SDF_BASIC;
        }
        return MATERIALS_BY_ID[materialId].pipelineId;
    }

    // 返回材质 SSBO，渲染前会绑定到固定 binding。
    public static int getMaterialSsbo() {
        return materialSsbo;
    }

    // 标记材质表需要重新上传，资源重载或 atlas 重建后调用。
    public static void markDirty() {
        dirty = true;
    }

    // 确保材质 SSBO 已经上传，材质表很小，dirty 时整表重传。
    public static void uploadIfNeeded() {
        if (!dirty && materialSsbo != 0) return;
        if (materialSsbo == 0) {
            materialSsbo = glGenBuffers();
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(MATERIALS_BY_ID.length * FLOATS_PER_MATERIAL);
        for (ParticleMaterial material : MATERIALS_BY_ID) {
            putSpriteUv(buffer, material.baseTexture);
            putSpriteUv(buffer, material.noiseTexture0);
            putSpriteUv(buffer, material.noiseTexture1);
            putSpriteUv(buffer, material.topDissolveTexture);
            buffer.put(material.noiseTileX).put(material.noiseTileY).put(material.noiseSpeed).put(material.noiseStrength);
            buffer.put(material.bloomCore).put(material.bloomHalo).put(material.bloomEdgeWidth).put(material.alphaScale);
            buffer.put((float) material.pipelineId).put(0.0F).put(0.0F).put(0.0F);
        }
        buffer.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, materialSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        dirty = false;
    }

    // 将材质表绑定到 Shader 固定读取位置。
    public static void bindMaterialBuffer() {
        uploadIfNeeded();
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, MATERIAL_BUFFER_BINDING, materialSsbo);
    }

    // 解除材质 SSBO 绑定，避免影响后续自管 Shader。
    public static void unbindMaterialBuffer() {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, MATERIAL_BUFFER_BINDING, 0);
    }

    // 释放材质表 GPU 资源。
    public static void cleanUp() {
        if (materialSsbo != 0) {
            glDeleteBuffers(materialSsbo);
            materialSsbo = 0;
        }
        dirty = true;
    }

    // 写入 sprite 在 atlas 中的 UV 范围，资源未就绪时回退到完整 0..1。
    public static void putSpriteUv(FloatBuffer buffer, ResourceLocation location) {
        if (location == null || EXETextureAtlas.EXE_TOOL_ATLAS == null) {
            buffer.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
            return;
        }

        TextureAtlasSprite sprite = EXETextureAtlas.getTextureLocation(location);
        if (sprite == null) {
            buffer.put(0.0F).put(0.0F).put(1.0F).put(1.0F);
            return;
        }
        buffer.put(sprite.getU0()).put(sprite.getV0()).put(sprite.getU1()).put(sprite.getV1());
    }
}
