package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialRegistry;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleRenderPipeline;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL40.glDrawArraysIndirect;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;

public class GPUParticleSystem {

    public static final int MAX_PARTICLES = 150000;
    private static final int LOCAL_SIZE_X = 256;
    private static final int QUAD_VERTEX_COUNT = 4;
    private static final int RISING_SHOCKWAVE_SEGMENTS = 32; // 上升冲击波圆台圆周分段数。
    private static final int RISING_SHOCKWAVE_VERTEX_COUNT = RISING_SHOCKWAVE_SEGMENTS * 6; // 每段两个三角形。
    private static final int ACTIVE_INDEX_BUFFER_BINDING = 3; // active index SSBO 绑定点。
    private static final int ACTIVE_COUNT_BUFFER_BINDING = 4; // active count SSBO 绑定点。
    private static final int INDIRECT_COMMAND_UINTS = 4; // DrawArraysIndirectCommand 包含 count/instanceCount/first/baseInstance。
    private static final int INDIRECT_COMMAND_STRIDE_BYTES = INDIRECT_COMMAND_UINTS * Integer.BYTES; // 单个 indirect command 字节跨度。
    private static final int INDIRECT_INSTANCE_COUNT_OFFSET_BYTES = Integer.BYTES; // instanceCount 位于 command 的第二个 uint。
    private static final int FLOATS_PER_PARTICLE = 52;
    private static final int FLOATS_PER_EMIT_JOB = 52;
    private static final int MAX_EMIT_JOBS = 768;   // 发射任务数量上限
    private int particleSsbo;
    private int emitJobSsbo;
    private int activeIndexSsbo; // 按 pipeline 压缩后的活跃粒子下标表。
    private int activeCountSsbo; // 每个 pipeline 本帧实际活跃粒子数量。
    private int indirectCommandBuffer; // GPU indirect draw 命令缓冲，避免 CPU 读回 active count。
    private int vao;
    private int vbo;
    private int nextEmitIndex;
    private int emitJobCount;
    private float totalTime;
    private final FloatBuffer particleBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * FLOATS_PER_PARTICLE);
    private final FloatBuffer emitJobBuffer = BufferUtils.createFloatBuffer(MAX_EMIT_JOBS * FLOATS_PER_EMIT_JOB);
    private final IntBuffer activeCountResetBuffer = BufferUtils.createIntBuffer(ParticleRenderPipeline.COUNT);
    private final IntBuffer indirectCommandInitBuffer = BufferUtils.createIntBuffer(ParticleRenderPipeline.COUNT * INDIRECT_COMMAND_UINTS);
    private final float[] activePipelineTimeLeft = new float[ParticleRenderPipeline.COUNT];
    public GPUShader gpushader;
    public GPUParticleRenderShader lightEffectShader; // 三噪声光效粒子渲染 Shader。
    public GPUParticleRenderShader directedLightEffectShader; // 世界空间定向三噪声光效粒子渲染 Shader。
    public GPUParticleRenderShader magicCircleEnergyShader; // 水平法阵能量粒子渲染 Shader。
    public GPUParticleRenderShader exSwordWaveShader; // 世界竖直平面的 EX 剑气粒子渲染 Shader。
    public GPUParticleRenderShader starTextureShader; // 始终朝向相机的星星贴图粒子渲染 Shader。
    public GPUParticleRenderShader risingShockwaveShader; // 程序化圆台上升冲击波渲染 Shader。

    public GPUParticleSystem() {
        initParticleSSBO();
        initEmitJobSSBO();
        initActiveIndexSSBO();
        initActiveCountSSBO();
        initIndirectCommandBuffer();
        initQuadVAO();
        gpushader = new GPUShader();
        lightEffectShader = new GPUParticleRenderShader("shaders/gpu/particle_light_effect.vsh", "shaders/gpu/particle_light_effect.fsh");
        directedLightEffectShader = new GPUParticleRenderShader("shaders/gpu/particle_directed_light_effect.vsh", "shaders/gpu/particle_directed_light_effect.fsh");
        magicCircleEnergyShader = new GPUParticleRenderShader("shaders/gpu/particle_magic_circle_energy.vsh", "shaders/gpu/particle_magic_circle_energy.fsh");
        exSwordWaveShader = new GPUParticleRenderShader("shaders/gpu/particle_ex_sword_wave.vsh", "shaders/gpu/particle_ex_sword_wave.fsh");
        starTextureShader = new GPUParticleRenderShader("shaders/gpu/particle_star_texture.vsh", "shaders/gpu/particle_star_texture.fsh");
        risingShockwaveShader = new GPUParticleRenderShader("shaders/gpu/particle_rising_shockwave.vsh", "shaders/gpu/particle_rising_shockwave.fsh");
    }

    private void initParticleSSBO() {
        particleSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, particleSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) MAX_PARTICLES * FLOATS_PER_PARTICLE * Float.BYTES,
                GL_DYNAMIC_COPY);

        particleBuffer.clear();
        for (int i = 0; i < MAX_PARTICLES * FLOATS_PER_PARTICLE; i++) {
            particleBuffer.put(0f);
        }
        particleBuffer.flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, particleBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void initEmitJobSSBO() {
        emitJobSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, emitJobSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) MAX_EMIT_JOBS * FLOATS_PER_EMIT_JOB * Float.BYTES,
                GL_STREAM_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void initIndirectCommandBuffer() {
        indirectCommandBuffer = glGenBuffers();
        indirectCommandInitBuffer.clear();
        for (int i = 0; i < ParticleRenderPipeline.COUNT; i++) {
            indirectCommandInitBuffer.put(vertexCountOfPipeline(i));
            indirectCommandInitBuffer.put(0);
            indirectCommandInitBuffer.put(0);
            indirectCommandInitBuffer.put(0);
        }
        indirectCommandInitBuffer.flip();

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, indirectCommandInitBuffer, GL_DYNAMIC_COPY);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
    }

    public void initActiveIndexSSBO() {
        activeIndexSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, activeIndexSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) MAX_PARTICLES * ParticleRenderPipeline.COUNT * Integer.BYTES,
                GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void initActiveCountSSBO() {
        activeCountSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, activeCountSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) ParticleRenderPipeline.COUNT * Integer.BYTES,
                GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private void initQuadVAO() {
        float[] vertices = {
                -0.5f,  0.5f,
                -0.5f, -0.5f,
                 0.5f,  0.5f,
                 0.5f, -0.5f
        };

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer buf = BufferUtils.createFloatBuffer(vertices.length);
        buf.put(vertices);
        buf.flip();
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void beginEmitJobs() {
        emitJobCount = 0;
        emitJobBuffer.clear();
    }

    public void addEmitJob(ParticleEmitTask task, int emitCount) {
        if (task == null || emitCount <= 0 || emitJobCount >= MAX_EMIT_JOBS) {
            return;
        }

        int count = Math.min(emitCount, MAX_PARTICLES);
        int startIndex = nextEmitIndex;
        nextEmitIndex = (nextEmitIndex + count) % MAX_PARTICLES;

        writeEmitJobPositionVec4(task, startIndex);
        writeEmitJobDirectionVec4(task, count, startIndex);
        writeEmitJobColorVec4s(task);
        writeEmitJobPhysicsVec4(task);
        writeEmitJobRenderVec4(task);
        writeEmitJobMotionVec4(task);
        writeEmitJobRandomVec4(task);
        writeEmitJobSpeedParamsVec4(task);
        writeEmitJobRenderParamsVec4(task);
        writeEmitJobSizeParamsVec4(task);
        writeEmitJobSizeControlVec4(task);

        int pipelineId = ParticleMaterialRegistry.pipelineIdOf(task.materialId());
        if (pipelineId >= 0 && pipelineId < activePipelineTimeLeft.length) {
            activePipelineTimeLeft[pipelineId] = Math.max(activePipelineTimeLeft[pipelineId], task.life);
        }

        emitJobCount++;
    }

    // 写入 EmitJob.position：xyz 为发射位置，w 为环形粒子缓冲写入起点。
    public void writeEmitJobPositionVec4(ParticleEmitTask task, int startIndex) {
        emitJobBuffer.put(task.posX).put(task.posY).put(task.posZ).put((float) startIndex);
    }

    // 写入 EmitJob.direction：普通模式保存方向，圆形和径向扩散模式保存轨道平面欧拉角。
    public void writeEmitJobDirectionVec4(ParticleEmitTask task, int count, int startIndex) {
        if (task.motionType == ParticleEmitTask.MOTION_CIRCULAR
                || task.motionType == ParticleEmitTask.MOTION_RADIAL_DIFFUSION) {
            float planeSeed = totalTime * 17.0f + emitJobCount * 31.0f + startIndex * 0.013f;
            float planePitch = task.orbitPlanePitch + signedRandom(planeSeed + 1.0f) * task.orbitPlanePitchRange;
            float planeYaw = task.orbitPlaneYaw + signedRandom(planeSeed + 2.0f) * task.orbitPlaneYawRange;
            float planeRoll = task.orbitPlaneRoll + signedRandom(planeSeed + 3.0f) * task.orbitPlaneRollRange;
            emitJobBuffer.put(planePitch).put(planeYaw).put(planeRoll).put((float) count);
            return;
        }
        emitJobBuffer.put(task.dirX).put(task.dirY).put(task.dirZ).put((float) count);
    }

    // 连续写入 EmitJob.startColor/midColor/endColor 三个颜色槽位。
    public void writeEmitJobColorVec4s(ParticleEmitTask task) {
        emitJobBuffer.put(task.startR).put(task.startG).put(task.startB).put(task.startA);
        emitJobBuffer.put(task.midR).put(task.midG).put(task.midB).put(task.midA);
        emitJobBuffer.put(task.endR).put(task.endG).put(task.endB).put(task.endA);
    }

    // 写入 EmitJob.physics：不同运动模式复用 xyzw，但仍保持固定槽位顺序。
    public void writeEmitJobPhysicsVec4(ParticleEmitTask task) {
        switch (task.motionType) {
            case ParticleEmitTask.MOTION_CIRCULAR:
                emitJobBuffer.put(task.orbitPhase).put(task.spread).put(task.life).put(task.orbitPhaseRange);
                break;
            case ParticleEmitTask.MOTION_TURBULENT_RISE:
                emitJobBuffer.put(task.startSpeed).put(task.turbulentSpawnRadius).put(task.life).put(task.turbulentRadialExpansion);
                break;
            default:
                emitJobBuffer.put(task.startSpeed).put(task.spread).put(task.life).put(task.gravity);
                break;
        }
    }

    // 写入 EmitJob.render：xy 为出生尺寸，z 为基础旋转，w 为 SDF 形状类型。
    public void writeEmitJobRenderVec4(ParticleEmitTask task) {
        emitJobBuffer.put(task.sizeX).put(task.sizeY).put(task.rotation).put((float) task.shapeType);
    }

    // 写入 EmitJob.motion：按运动模式集中解释 yzw 的复用语义。
    public void writeEmitJobMotionVec4(ParticleEmitTask task) {
        if (task.materialId() == ParticleMaterialRegistry.RISING_SHOCKWAVE_ID) {
            emitJobBuffer.put((float) task.motionType).put(task.risingShockwaveDissolvePower)
                    .put(0.0F).put(0.0F);
            return;
        }
        switch (task.motionType) {
            case ParticleEmitTask.MOTION_RADIAL_DIFFUSION:
                emitJobBuffer.put((float) task.motionType).put(task.radialSpawnRadiusJitter)
                        .put(task.radialVerticalSpeed).put(task.radialVerticalSpeedJitter);
                break;
            case ParticleEmitTask.MOTION_TURBULENT_RISE:
                emitJobBuffer.put((float) task.motionType).put(task.turbulentCurlStrength)
                        .put(task.turbulentNoiseScale).put(task.turbulentNoiseSpeed);
                break;
            default:
                emitJobBuffer.put((float) task.motionType).put(task.orbitRadius)
                        .put(task.angularSpeed).put(task.verticalSpeed);
                break;
        }
    }

    // 写入 EmitJob.random：时间种子、任务序号、圆形出生模式和材质 ID。
    public void writeEmitJobRandomVec4(ParticleEmitTask task) {
        emitJobBuffer.put(totalTime).put((float) emitJobCount)
                .put((float) task.orbitSpawnMode).put((float) task.materialId());
    }

    // 写入 EmitJob.speedParams：速度曲线和方向符号，弧面方向模式会复用这些槽位。
    public void writeEmitJobSpeedParamsVec4(ParticleEmitTask task) {
        emitJobBuffer.put(task.startSpeed).put(task.endSpeed).put(task.speedCurvePower).put(task.directionSign);
    }

    // 写入 EmitJob.renderParams：z 对普通 billboard 是自旋速度，对噪声上升是出生高度下限，对上升冲击波是 UV 流速。
    public void writeEmitJobRenderParamsVec4(ParticleEmitTask task) {
        float renderParamZ;
        float renderParamW;
        if (task.materialId() == ParticleMaterialRegistry.RISING_SHOCKWAVE_ID) {
            renderParamZ = task.risingShockwaveUvFlowSpeed;
            renderParamW = task.risingShockwavePower;
        } else if (task.motionType == ParticleEmitTask.MOTION_TURBULENT_RISE) {
            renderParamZ = task.turbulentSpawnHeightMin;
            renderParamW = task.turbulentSpawnHeightMax;
        } else {
            renderParamZ = task.rotationSpeed;
            renderParamW = task.turbulentSpawnHeightMax;
        }
        emitJobBuffer.put(task.midColorTime).put(task.randomRotation ? 1.0F : 0.0F)
                .put(renderParamZ).put(renderParamW);
    }

    // 写入 EmitJob.sizeParams：xy 为中间尺寸，zw 为结束尺寸。
    public void writeEmitJobSizeParamsVec4(ParticleEmitTask task) {
        emitJobBuffer.put(task.midSizeX).put(task.midSizeY).put(task.endSizeX).put(task.endSizeY);
    }

    // 写入 EmitJob.sizeControl：x 为中间尺寸时间，y 为固定尺寸开关，zw 为 LIGHT_EFFECT 遮罩或上升冲击波 UV 平铺参数。
    public void writeEmitJobSizeControlVec4(ParticleEmitTask task) {
        if (task.materialId() == ParticleMaterialRegistry.RISING_SHOCKWAVE_ID) {
            emitJobBuffer.put(task.midSizeTime).put(task.fixedSizeScale ? 1.0F : 0.0F)
                    .put(task.risingShockwaveUvTileX).put(task.risingShockwaveUvTileY);
            return;
        }
        emitJobBuffer.put(task.midSizeTime).put(task.fixedSizeScale ? 1.0F : 0.0F)
                .put(task.lightEffectMaskRadius).put(task.lightEffectMaskSoftness);
    }

    // 返回每个 pipeline 的间接绘制顶点数，普通粒子为四顶点 quad，上升冲击波为程序化圆台三角形。
    public int vertexCountOfPipeline(int pipelineId) {
        return pipelineId == ParticleRenderPipeline.RISING_SHOCKWAVE
                ? RISING_SHOCKWAVE_VERTEX_COUNT
                : QUAD_VERTEX_COUNT;
    }

    private static float signedRandom(float seed) {
        double value = Math.sin(seed * 12.9898 + 78.233) * 43758.5453;
        return (float) ((value - Math.floor(value)) * 2.0 - 1.0);
    }

    private void uploadEmitJobs() {
        emitJobBuffer.flip();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, emitJobSsbo);
        if (emitJobBuffer.hasRemaining()) {
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, emitJobBuffer);
        }
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void resetActiveCounts() {
        activeCountResetBuffer.clear();
        for (int i = 0; i < ParticleRenderPipeline.COUNT; i++) {
            activeCountResetBuffer.put(0);
        }
        activeCountResetBuffer.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, activeCountSsbo);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, activeCountResetBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void updateAndRender(float dt, Matrix4f projMatrix, Camera camera) {
        totalTime += dt;
        uploadEmitJobs();
        resetActiveCounts();

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleSsbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, emitJobSsbo);
        ParticleMaterialRegistry.bindMaterialBuffer();
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ACTIVE_INDEX_BUFFER_BINDING, activeIndexSsbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ACTIVE_COUNT_BUFFER_BINDING, activeCountSsbo);

        glUseProgram(gpushader.getComputeProgram());
        gpushader.updateComputeUniforms(dt, MAX_PARTICLES, emitJobCount, totalTime, ParticleRenderPipeline.COUNT);

        int numGroups = (MAX_PARTICLES + LOCAL_SIZE_X - 1) / LOCAL_SIZE_X;
        glDispatchCompute(numGroups, 1, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_BUFFER_UPDATE_BARRIER_BIT);
        updateIndirectCommandsFromActiveCounts();
        glMemoryBarrier(GL_COMMAND_BARRIER_BIT);
        Matrix4f viewMatrix = MathUtil.createViewMatrix(camera);

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        boolean depthWriteEnabled = glGetBoolean(GL_DEPTH_WRITEMASK);
        glDepthMask(false);

        renderSdfPipeline(projMatrix, viewMatrix);
        renderLightEffectPipeline(projMatrix, viewMatrix);
        renderDirectedLightEffectPipeline(projMatrix, viewMatrix);
        renderMagicCircleEnergyPipeline(projMatrix, viewMatrix);
        renderExSwordWavePipeline(projMatrix, viewMatrix);
        renderStarTexturePipeline(projMatrix, viewMatrix);
        renderRisingShockwavePipeline(projMatrix, viewMatrix, camera);
        ParticleMaterialRegistry.unbindMaterialBuffer();

        glDepthMask(depthWriteEnabled);
        glDisable(GL_BLEND);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glUseProgram(0);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ACTIVE_COUNT_BUFFER_BINDING, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ACTIVE_INDEX_BUFFER_BINDING, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        for (int i = 0; i < activePipelineTimeLeft.length; i++) {
            activePipelineTimeLeft[i] = Math.max(0.0F, activePipelineTimeLeft[i] - dt);
        }
    }

    public void renderSdfPipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.SDF_BASIC)) return;
        gpushader.start();
        gpushader.loadMatrix(gpushader.render_uProjection, projMatrix);
        gpushader.loadMatrix(gpushader.render_uView, viewMatrix);
        gpushader.updateRenderUniforms(totalTime, ParticleRenderPipeline.SDF_BASIC, MAX_PARTICLES);
        drawPipelineIndirect(ParticleRenderPipeline.SDF_BASIC);
    }

    public void renderLightEffectPipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.LIGHT_EFFECT)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        lightEffectShader.start();
        lightEffectShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime, ParticleRenderPipeline.LIGHT_EFFECT, MAX_PARTICLES);
        drawPipelineIndirect(ParticleRenderPipeline.LIGHT_EFFECT);
    }

    public void renderDirectedLightEffectPipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.DIRECTED_LIGHT_EFFECT)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        directedLightEffectShader.start();
        directedLightEffectShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime,
                ParticleRenderPipeline.DIRECTED_LIGHT_EFFECT, MAX_PARTICLES);

        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            drawPipelineIndirect(ParticleRenderPipeline.DIRECTED_LIGHT_EFFECT);
        } finally {
            if (cullEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
        }
    }

    public void renderMagicCircleEnergyPipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.MAGIC_CIRCLE_ENERGY)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        magicCircleEnergyShader.start();
        magicCircleEnergyShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime,
                ParticleRenderPipeline.MAGIC_CIRCLE_ENERGY, MAX_PARTICLES);

        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            drawPipelineIndirect(ParticleRenderPipeline.MAGIC_CIRCLE_ENERGY);
        } finally {
            if (cullEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
        }
    }

    public void renderExSwordWavePipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.EX_SWORD_WAVE)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        exSwordWaveShader.start();
        exSwordWaveShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime,
                ParticleRenderPipeline.EX_SWORD_WAVE, MAX_PARTICLES);

        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            drawPipelineIndirect(ParticleRenderPipeline.EX_SWORD_WAVE);
        } finally {
            if (cullEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
        }
    }

    public void renderStarTexturePipeline(Matrix4f projMatrix, Matrix4f viewMatrix) {
        if (!isPipelineActive(ParticleRenderPipeline.STAR_TEXTURE)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        starTextureShader.start();
        starTextureShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime,
                ParticleRenderPipeline.STAR_TEXTURE, MAX_PARTICLES);
        drawPipelineIndirect(ParticleRenderPipeline.STAR_TEXTURE);
    }

    public void renderRisingShockwavePipeline(Matrix4f projMatrix, Matrix4f viewMatrix, Camera camera) {
        if (!isPipelineActive(ParticleRenderPipeline.RISING_SHOCKWAVE)) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        if (atlasTextureId <= 0) return;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTextureId);
        risingShockwaveShader.start();
        risingShockwaveShader.updateRenderUniforms(projMatrix, viewMatrix, totalTime,
                ParticleRenderPipeline.RISING_SHOCKWAVE, MAX_PARTICLES);
        if (camera != null) {
            risingShockwaveShader.loadCameraPosition(camera.getPosition());
        }

        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        try {
            GL11.glDisable(GL11.GL_CULL_FACE);
            drawPipelineIndirect(ParticleRenderPipeline.RISING_SHOCKWAVE, GL_TRIANGLES);
        } finally {
            if (cullEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
        }
    }

    public void updateIndirectCommandsFromActiveCounts() {
        glBindBuffer(GL_COPY_READ_BUFFER, activeCountSsbo);
        glBindBuffer(GL_COPY_WRITE_BUFFER, indirectCommandBuffer);
        for (int pipelineId = 0; pipelineId < ParticleRenderPipeline.COUNT; pipelineId++) {
            long readOffset = (long) pipelineId * Integer.BYTES;
            long writeOffset = (long) pipelineId * INDIRECT_COMMAND_STRIDE_BYTES + INDIRECT_INSTANCE_COUNT_OFFSET_BYTES;
            glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, readOffset, writeOffset, Integer.BYTES);
        }
        glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
        glBindBuffer(GL_COPY_READ_BUFFER, 0);
    }

    public void drawPipelineIndirect(int pipelineId) {
        drawPipelineIndirect(pipelineId, GL_TRIANGLE_STRIP);
    }

    public void drawPipelineIndirect(int pipelineId, int drawMode) {
        if (pipelineId < 0 || pipelineId >= ParticleRenderPipeline.COUNT) return;
        long commandOffset = (long) pipelineId * INDIRECT_COMMAND_STRIDE_BYTES;
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer);
        glDrawArraysIndirect(drawMode, commandOffset);
    }

    public boolean isPipelineActive(int pipelineId) {
        return pipelineId >= 0 && pipelineId < activePipelineTimeLeft.length && activePipelineTimeLeft[pipelineId] > 0.0F;
    }

    public void cleanUp() {
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (particleSsbo != 0) {
            glDeleteBuffers(particleSsbo);
        }
        if (emitJobSsbo != 0) {
            glDeleteBuffers(emitJobSsbo);
        }
        if (activeIndexSsbo != 0) {
            glDeleteBuffers(activeIndexSsbo);
        }
        if (activeCountSsbo != 0) {
            glDeleteBuffers(activeCountSsbo);
        }
        if (indirectCommandBuffer != 0) {
            glDeleteBuffers(indirectCommandBuffer);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (gpushader != null) {
            gpushader.cleanUp();
        }
        if (lightEffectShader != null) {
            lightEffectShader.cleanUp();
        }
        if (directedLightEffectShader != null) {
            directedLightEffectShader.cleanUp();
        }
        if (magicCircleEnergyShader != null) {
            magicCircleEnergyShader.cleanUp();
        }
        if (exSwordWaveShader != null) {
            exSwordWaveShader.cleanUp();
        }
        if (starTextureShader != null) {
            starTextureShader.cleanUp();
        }
        if (risingShockwaveShader != null) {
            risingShockwaveShader.cleanUp();
        }
        ParticleMaterialRegistry.cleanUp();
    }
}
