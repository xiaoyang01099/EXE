package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.InstanceLayout;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.InstanceVBO;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.SmokeParticleShader;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.nio.FloatBuffer;
import java.util.List;

public class SmokeParticleInstancedRenderer {
    public static final int POSITION_LOCATION = 0; // shader 中局部位置 attribute 的 location。
    public static final int UV_LOCATION = 1; // shader 中局部 UV attribute 的 location。
    public static final int CENTER_SIZE_LOCATION = 2; // 实例中心和尺寸 attribute 的 location。
    public static final int COLOR_LOCATION = 3; // 实例可见颜色 attribute 的 location。
    public static final int BLOOM_LOCATION = 4; // 实例 bloom 颜色 attribute 的 location。
    public static final int ANIM_LOCATION = 5; // 实例动画参数 attribute 的 location。
    public static final int MOTION_LOCATION = 6; // 实例旋转和随机参数 attribute 的 location。
    public static final int INSTANCE_STRIDE_FLOATS = 20; // 每个烟雾实例包含 5 个 vec4。
    public static final int DEFAULT_MAX_INSTANCES = 10000; // 烟雾粒子默认实例容量，给多云团和大体积烟雾预留空间。
    public static final int QUAD_VERTEX_COUNT = 6; // 一个 billboard quad 展开为两个三角形。
    public static final ResourceLocation SMOKE_TEXTURE = new ResourceLocation(Exe.MODID, "textures/entity/smoke.png");
    public static final float SMOKE_ALPHA_CUTOFF = 0.01F; // smoke.png alpha 透明度抠除阈值。
    public static final float SMOKE_SOFTNESS = 0.08F; // smoke.png alpha 软边过渡范围。
    public static final float SMOKE_GAMMA = 1.00F; // smoke.png alpha 曲线。
    public static final float SMOKE_BOTTOM_FADE_END = 0.22F; // 单粒子贴地时底部局部 UV 软淡出高度。
    public static final float SOFT_PARTICLE_NEAR_DISTANCE = 0.00003F; // 深度差低于该值时开始软化。
    public static final float SOFT_PARTICLE_FAR_DISTANCE = 0.00085F; // 深度差高于该值时完全显示。

    public final InstanceLayout instanceLayout; // 烟雾实例数据布局。
    public final InstanceVBO instanceVBO; // 每帧上传烟雾实例参数的 VBO。
    public int vaoID; // 静态 billboard VAO。
    public int positionVboID; // 静态 billboard 局部位置 VBO。
    public int uvVboID; // 静态 billboard 局部 UV VBO。

    public SmokeParticleInstancedRenderer() {
        instanceLayout = InstanceLayout.create(INSTANCE_STRIDE_FLOATS)
                .attr(CENTER_SIZE_LOCATION, 4, 0)
                .attr(COLOR_LOCATION, 4, 4)
                .attr(BLOOM_LOCATION, 4, 8)
                .attr(ANIM_LOCATION, 4, 12)
                .attr(MOTION_LOCATION, 4, 16);
        instanceVBO = new InstanceVBO(instanceLayout, DEFAULT_MAX_INSTANCES);
        buildQuadMesh();
        if (vaoID != 0) {
            instanceVBO.attachTo(vaoID);
        }
    }

    public boolean render(List<SmokeParticleQueue.SmokeParticleData> particles, float time, Camera camera, org.joml.Matrix4f viewMatrix,
                          int sceneDepthTextureId, int screenWidth, int screenHeight) {
        if (particles == null || particles.isEmpty()) return true;
        if (vaoID == 0 || !SmokeParticleShader.isLoaded()) return false;

        FloatBuffer buffer = instanceVBO.getBuffer();
        buffer.clear();
        int instanceCount = 0;
        for (SmokeParticleQueue.SmokeParticleData particle : particles) {
            if (instanceCount >= instanceVBO.maxInstances) break;
            if (particle.writeInstance(buffer, time)) {
                instanceCount++;
            }
        }
        if (instanceCount <= 0) return true;

        instanceVBO.update(buffer, instanceCount);
        drawInstanced(instanceCount, time, camera, viewMatrix, sceneDepthTextureId, screenWidth, screenHeight);
        return true;
    }

    public void buildQuadMesh() {
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(QUAD_VERTEX_COUNT * 3);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(QUAD_VERTEX_COUNT * 2);
        putVertex(positionBuffer, uvBuffer, -0.5F, -0.5F, 0.0F, 0.0F);
        putVertex(positionBuffer, uvBuffer, 0.5F, -0.5F, 1.0F, 0.0F);
        putVertex(positionBuffer, uvBuffer, 0.5F, 0.5F, 1.0F, 1.0F);
        putVertex(positionBuffer, uvBuffer, 0.5F, 0.5F, 1.0F, 1.0F);
        putVertex(positionBuffer, uvBuffer, -0.5F, 0.5F, 0.0F, 1.0F);
        putVertex(positionBuffer, uvBuffer, -0.5F, -0.5F, 0.0F, 0.0F);
        positionBuffer.flip();
        uvBuffer.flip();
        createMeshBuffers(positionBuffer, uvBuffer);
    }

    public void putVertex(FloatBuffer positionBuffer, FloatBuffer uvBuffer, float x, float y, float u, float v) {
        positionBuffer.put(x).put(y).put(0.0F);
        uvBuffer.put(u).put(v);
    }

    public void createMeshBuffers(FloatBuffer positionBuffer, FloatBuffer uvBuffer) {
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        positionVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(POSITION_LOCATION, 3, GL11.GL_FLOAT, false, 0, 0L);

        uvVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(UV_LOCATION, 2, GL11.GL_FLOAT, false, 0, 0L);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void drawInstanced(int instanceCount, float time, Camera camera,
                              org.joml.Matrix4f viewMatrix, int sceneDepthTextureId, int screenWidth, int screenHeight) {
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        int previousVao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int previousProgram = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        int previousTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        int previousTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL13.glActiveTexture(previousActiveTexture);

        try {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_CULL_FACE);
            RenderSystem.setShaderTexture(0, SMOKE_TEXTURE);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            int smokeTextureId = Minecraft.getInstance().getTextureManager().getTexture(SMOKE_TEXTURE).getId();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, smokeTextureId);
            applySmokeTextureFilter();
            if (sceneDepthTextureId > 0) {
                GL13.glActiveTexture(GL13.GL_TEXTURE1);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneDepthTextureId);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
            }

            GL30.glBindVertexArray(vaoID);
            SmokeParticleShader.setProjection(RenderSystem.getProjectionMatrix());
            SmokeParticleShader.setView(viewMatrix);
            SmokeParticleShader.setScreenSize(screenWidth, screenHeight);
            SmokeParticleShader.setSmokeMaskParams(SMOKE_ALPHA_CUTOFF, SMOKE_SOFTNESS, SMOKE_GAMMA, SMOKE_BOTTOM_FADE_END);
            SmokeParticleShader.setSoftParticleParams(sceneDepthTextureId > 0, SOFT_PARTICLE_NEAR_DISTANCE, SOFT_PARTICLE_FAR_DISTANCE);
            SmokeParticleShader.setGlobalParams(time, SmokeParticleQueue.SMOKE_TEXTURE_PLAYABLE_FRAMES,
                    SmokeParticleQueue.SMOKE_TEXTURE_COLUMNS, SmokeParticleQueue.SMOKE_TEXTURE_ROWS);
            writeCameraBasis(camera);
            SmokeParticleShader.getShader().setSampler("Sampler0", smokeTextureId);
            if (sceneDepthTextureId > 0) {
                SmokeParticleShader.getShader().setSampler("SceneDepthSampler", sceneDepthTextureId);
            }
            SmokeParticleShader.getShader().apply();
            instanceVBO.enable(POSITION_LOCATION, UV_LOCATION);
            GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, QUAD_VERTEX_COUNT, instanceCount);
            instanceVBO.disable(POSITION_LOCATION, UV_LOCATION);
        } finally {
            SmokeParticleShader.getShader().clear();
            GL20.glUseProgram(previousProgram);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture1);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture0);
            GL13.glActiveTexture(previousActiveTexture);
            GL30.glBindVertexArray(previousVao);
            if (blendEnabled) {
                GL11.glEnable(GL11.GL_BLEND);
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }
            if (cullEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
            GL11.glDepthFunc(depthFunc);
            GL11.glDepthMask(depthMask);
        }
    }

    public void applySmokeTextureFilter() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    }

    public void writeCameraBasis(Camera camera) {
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(camera.rotation());
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(camera.rotation());
        SmokeParticleShader.setCameraBasis(right.x(), right.y(), right.z(), up.x(), up.y(), up.z());
    }

    public void cleanup() {
        if (positionVboID != 0) {
            GL15.glDeleteBuffers(positionVboID);
            positionVboID = 0;
        }
        if (uvVboID != 0) {
            GL15.glDeleteBuffers(uvVboID);
            uvVboID = 0;
        }
        if (vaoID != 0) {
            GL30.glDeleteVertexArrays(vaoID);
            vaoID = 0;
        }
        instanceVBO.cleanup();
    }
}
