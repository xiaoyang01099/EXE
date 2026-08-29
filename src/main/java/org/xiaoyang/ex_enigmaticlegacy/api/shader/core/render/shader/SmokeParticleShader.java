package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class SmokeParticleShader {
    private static ShaderInstance shader;
    private static AbstractUniform globalParams;
    private static AbstractUniform cameraRight;
    private static AbstractUniform cameraUp;
    private static AbstractUniform uView;
    private static AbstractUniform projectionMat;
    private static AbstractUniform screenSize;
    private static AbstractUniform smokeMaskParams;
    private static AbstractUniform softParticleParams;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "smoke_particle"),
                DefaultVertexFormat.POSITION_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        globalParams = shaderInstance.safeGetUniform("GlobalParams");
        cameraRight = shaderInstance.safeGetUniform("CameraRight");
        cameraUp = shaderInstance.safeGetUniform("CameraUp");
        uView = shaderInstance.safeGetUniform("uView");
        projectionMat = shaderInstance.safeGetUniform("ProjMat");
        screenSize = shaderInstance.safeGetUniform("ScreenSize");
        smokeMaskParams = shaderInstance.safeGetUniform("SmokeMaskParams");
        softParticleParams = shaderInstance.safeGetUniform("SoftParticleParams");
    }

    public static void setGlobalParams(float time, float playableFrames, float columns, float rows) {
        globalParams.set(time, playableFrames, columns, rows);
    }

    public static void setCameraBasis(float rightX, float rightY, float rightZ, float upX, float upY, float upZ) {
        cameraRight.set(rightX, rightY, rightZ, 0.0F);
        cameraUp.set(upX, upY, upZ, 0.0F);
    }

    public static void setView(Matrix4f view) {
        uView.set(view);
    }

    public static void setProjection(Matrix4f projection) {
        projectionMat.set(projection);
    }

    public static void setScreenSize(float width, float height) {
        screenSize.set(width, height, 0.0F, 0.0F);
    }

    public static void setSmokeMaskParams(float alphaCutoff, float smokeSoftness, float smokeGamma, float bottomFadeEnd) {
        smokeMaskParams.set(alphaCutoff, smokeSoftness, smokeGamma, bottomFadeEnd);
    }

    public static void setSoftParticleParams(boolean enabled, float nearDistance, float farDistance) {
        softParticleParams.set(enabled ? 1.0F : 0.0F, nearDistance, farDistance, 0.0F);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
