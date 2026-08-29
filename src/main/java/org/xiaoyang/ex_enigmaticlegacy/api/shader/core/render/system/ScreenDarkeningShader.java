package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.ShaderProgram;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class ScreenDarkeningShader extends ShaderProgram {
    public static final ResourceLocation VERTEX_FILE = new ResourceLocation(Exe.MODID, "shaders/post/screen_darken.vsh"); // 屏幕暗化顶点 shader。
    public static final ResourceLocation FRAGMENT_FILE = new ResourceLocation(Exe.MODID, "shaders/post/screen_darken.fsh"); // 屏幕暗化片元 shader。

    public int locationSceneTexture; // 原版场景纹理 uniform 位置。
    public int locationDarkenStrength; // 当前暗化强度 uniform 位置。

    public ScreenDarkeningShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    public void getAllUniformLocations() {
        locationSceneTexture = super.getUniformLocation("SceneTexture");
        locationDarkenStrength = super.getUniformLocation("DarkenStrength");
    }

    // 写入固定纹理槽位和本帧暗化强度。
    public void loadUniforms(float darkenStrength) {
        super.loadInt(locationSceneTexture, 0);
        super.loadFloat(locationDarkenStrength, Math.max(0.0F, Math.min(0.95F, darkenStrength)));
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
