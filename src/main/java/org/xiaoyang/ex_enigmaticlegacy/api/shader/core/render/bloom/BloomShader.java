package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom;

import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.ShaderProgram;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class BloomShader extends ShaderProgram {
    private static final ResourceLocation VERTEX_FILE = new ResourceLocation(Exe.MODID, "shaders/post/bloom_blur.vsh");
    private static final ResourceLocation FRAGMENT_FILE = new ResourceLocation(Exe.MODID, "shaders/post/bloom_blur.fsh");

    private int location_inputTexture;
    private int location_direction;
    private int location_blurRadius;

    public BloomShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    public void getAllUniformLocations() {
        location_inputTexture = super.getUniformLocation("inputTexture");
        location_direction = super.getUniformLocation("direction");
        location_blurRadius = super.getUniformLocation("BlurRadius");
    }

    public void loadTextureUnit() {
        super.loadInt(location_inputTexture, 0);
    }

    public void loadDirection(float x, float y) {
        super.loadVector(location_direction, new Vector2f(x, y));
    }

    public void loadBlurRadius(float blurRadius) {
        super.loadFloat(location_blurRadius, blurRadius);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
