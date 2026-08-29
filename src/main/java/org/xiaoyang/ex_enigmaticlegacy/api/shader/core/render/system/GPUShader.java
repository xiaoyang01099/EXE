package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import static org.lwjgl.opengl.GL43.*;

public class GPUShader extends ShaderProgram {
    private static final String VERTEX_FILE = "shaders/gpu/gpushader.vsh";
    private static final String FRAGMENT_FILE = "shaders/gpu/gpushader.fsh";
    private static final String COMPUTE_FILE = "shaders/gpu/gpushader.comp";
    private int computeProgram;
    private int comp_uDeltaTime;
    private int comp_uMaxParticles;
    private int comp_uEmitJobCount;
    private int comp_uTime;
    private int comp_uRenderPipelineCount;
    public int render_uProjection;
    public int render_uView;
    public int render_uTime;
    public int render_uRenderPipelineId;
    public int render_uMaxParticles;

    public GPUShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
        loadComputeShader();
    }

    @Override
    protected void getAllUniformLocations() {
        render_uProjection = glGetUniformLocation(programID, "uProjection");
        render_uView = glGetUniformLocation(programID, "uView");
        render_uTime = glGetUniformLocation(programID, "uTime");
        render_uRenderPipelineId = glGetUniformLocation(programID, "uRenderPipelineId");
        render_uMaxParticles = glGetUniformLocation(programID, "uMaxParticles");
    }

    private void loadComputeShader() {
        int computeShaderId = loadShader(COMPUTE_FILE, GL_COMPUTE_SHADER);

        computeProgram = glCreateProgram();
        glAttachShader(computeProgram, computeShaderId);
        glLinkProgram(computeProgram);

        if (glGetProgrami(computeProgram, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(computeProgram);
            System.err.println("[Compute Shader 链接错误]\n" + log);
        }

        glDeleteShader(computeShaderId);
        getComputeShaderUniformLocation();
    }

    private void getComputeShaderUniformLocation() {
        comp_uDeltaTime = glGetUniformLocation(computeProgram, "uDeltaTime");
        comp_uMaxParticles = glGetUniformLocation(computeProgram, "uMaxParticles");
        comp_uEmitJobCount = glGetUniformLocation(computeProgram, "uEmitJobCount");
        comp_uTime = glGetUniformLocation(computeProgram, "uTime");
        comp_uRenderPipelineCount = glGetUniformLocation(computeProgram, "uRenderPipelineCount");
    }

    public void updateComputeUniforms(float dt, int maxParticles, int emitJobCount, float totalTime, int renderPipelineCount) {
        glUniform1f(comp_uDeltaTime, dt);
        glUniform1i(comp_uMaxParticles, maxParticles);
        glUniform1i(comp_uEmitJobCount, emitJobCount);
        glUniform1f(comp_uTime, totalTime);
        glUniform1i(comp_uRenderPipelineCount, renderPipelineCount);
    }

    public void updateRenderUniforms(float totalTime, int pipelineId, int maxParticles) {
        glUniform1f(render_uTime, totalTime);
        glUniform1i(render_uRenderPipelineId, pipelineId);
        glUniform1i(render_uMaxParticles, maxParticles);
    }

    public int getComputeProgram() {
        return computeProgram;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (computeProgram != 0) {
            glDeleteProgram(computeProgram);
        }
    }
}
