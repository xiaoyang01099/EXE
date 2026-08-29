package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom;

import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.ShaderProgram;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class BloomDownsampleShader extends ShaderProgram {
    private static final ResourceLocation VERTEX_FILE = new ResourceLocation(Exe.MODID, "shaders/post/bloom_downsample.vsh"); // 降采样顶点 shader 资源。
    private static final ResourceLocation FRAGMENT_FILE = new ResourceLocation(Exe.MODID, "shaders/post/bloom_downsample.fsh"); // Bloom 重采样片元 shader 资源。

    private int location_inputTexture;

    public BloomDownsampleShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    public void getAllUniformLocations() {
        location_inputTexture = super.getUniformLocation("inputTexture");
    }

    public void loadTextureUnit() {
        super.loadInt(location_inputTexture, 0);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
