package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class WorldShardRenderer {
    private static final int SHARD_BUFFER_SIZE = Math.max(32768, SlashCofig.WorldSlash.WORLD_SHARD_COUNT * 96 * 48);
    private static final FullscreenBufferBuilder SHARD_BUFFER = new FullscreenBufferBuilder(SHARD_BUFFER_SIZE);
    private static ShaderInstance shader;
    private static Uniform screenSizeUniform;
    private static Uniform texelSizeUniform;
    private static Uniform refractionUniform;
    private static Uniform edgeUniform;
    private static Uniform mirrorUniform;
    private static Uniform timeUniform;
    private static TextureTarget sceneTarget;

    private WorldShardRenderer() {
    }

    public static void shaderLoaded(ShaderInstance instance) {
        shader = instance;
        screenSizeUniform = instance.getUniform("ScreenSize");
        texelSizeUniform = instance.getUniform("TexelSize");
        refractionUniform = instance.getUniform("RefractionStrength");
        edgeUniform = instance.getUniform("EdgeStrength");
        mirrorUniform = instance.getUniform("MirrorStrength");
        timeUniform = instance.getUniform("Time");
    }

    public static void render(PoseStack poseStack, List<SlashEffect> effects, Vec3 camera, float partialTick, RenderTarget main) {
        if (shader == null || !SlashCofig.WorldSlash.WORLD_SHARDS_ENABLED
                || effects.stream().noneMatch(effect -> effect.hasWorldShards() && effect.worldVisibility(partialTick) > 0.001f)) return;

        ensureSceneTarget(main.width, main.height);
        if (sceneTarget == null) return;

        if (!FramebufferBlitter.blitColor(main, sceneTarget)) return;
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.enableDefault();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.setShader(() -> shader);
        shader.setSampler("SceneTex", sceneTarget.getColorTextureId());
        if (screenSizeUniform != null) screenSizeUniform.set((float) main.width, (float) main.height);
        if (texelSizeUniform != null) texelSizeUniform.set(1.0f / Math.max(1.0f, main.width), 1.0f / Math.max(1.0f, main.height));
        if (refractionUniform != null) refractionUniform.set(SlashCofig.WorldSlash.WORLD_SHARD_REFRACTION);
        if (edgeUniform != null) edgeUniform.set(SlashCofig.WorldSlash.WORLD_SHARD_EDGE_HIGHLIGHT);
        if (mirrorUniform != null) mirrorUniform.set(SlashCofig.WorldSlash.WORLD_SHARD_MIRROR_STRENGTH);
        if (timeUniform != null) timeUniform.set((Minecraft.getInstance().level == null ? 0.0f : Minecraft.getInstance().level.getGameTime()) + partialTick);

        BufferBuilder builder = SHARD_BUFFER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);

        for (SlashEffect effect : effects) {
            effect.renderWorldShards(poseStack, builder, camera, partialTick, effect.worldVisibility(partialTick));
        }
        FullscreenBufferBuilder.drawWithShaderOrDiscard(builder);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        DriverBlendState.enableDefault();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
    }

    public static void clear() {
        if (sceneTarget != null) {
            sceneTarget.destroyBuffers();
            sceneTarget = null;
        }
    }

    private static void ensureSceneTarget(int width, int height) {
        if (sceneTarget == null) {
            sceneTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            sceneTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (sceneTarget.width != width || sceneTarget.height != height) {
            sceneTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }

}
