package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.ShaderProgram;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class MiaoOutlineShader extends ShaderProgram {
    public static final ResourceLocation VERTEX_FILE = new ResourceLocation(Exe.MODID, "shaders/post/miao_outline.vsh"); // 全屏 quad 顶点 shader。
    public static final ResourceLocation FRAGMENT_FILE = new ResourceLocation(Exe.MODID, "shaders/post/miao_outline.fsh"); // Miao 径向深度描边片元 shader。
    public int location_targetDepthMaskTexture; // CA2 深度和目标 mask 纹理采样器。
    public int location_atlasTexture; // AkatZuma 自定义 atlas 纹理采样器。
    public int location_screenSize; // 当前 mainFBO 尺寸。
    public int location_time; // 客户端运行时间。
    public int location_outlineParams; // 描边宽度、采样数、边缘强度和样式类型。
    public int location_depthParams; // 深度阈值、深度范围、mask 阈值。
    public int location_noiseParams; // 噪声流速、强度和平铺。
    public int location_fireColorParams; // 火焰渐变颜色流动和强度参数。
    public int location_fireEdgeParams; // 火焰边缘后置扰动参数。
    public int location_fireWarpParams; // 火焰边缘采样位移参数。
    public int location_distanceFadeParams; // 描边随目标深度缩小的距离衰减参数。
    public int location_noiseSpriteUv; // 噪声 sprite 在 atlas 中的 UV 范围。
    public int location_gradientSpriteUv; // 火焰渐变 sprite 在 atlas 中的 UV 范围。
    public int location_visibleColor; // 可见描边颜色。
    public int location_bloomColor; // bloom 描边颜色。

    public MiaoOutlineShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    public void getAllUniformLocations() {
        location_targetDepthMaskTexture = super.getUniformLocation("targetDepthMaskTexture");
        location_atlasTexture = super.getUniformLocation("atlasTexture");
        location_screenSize = super.getUniformLocation("screenSize");
        location_time = super.getUniformLocation("time");
        location_outlineParams = super.getUniformLocation("outlineParams");
        location_depthParams = super.getUniformLocation("depthParams");
        location_noiseParams = super.getUniformLocation("noiseParams");
        location_fireColorParams = super.getUniformLocation("fireColorParams");
        location_fireEdgeParams = super.getUniformLocation("fireEdgeParams");
        location_fireWarpParams = super.getUniformLocation("fireWarpParams");
        location_distanceFadeParams = super.getUniformLocation("distanceFadeParams");
        location_noiseSpriteUv = super.getUniformLocation("noiseSpriteUv");
        location_gradientSpriteUv = super.getUniformLocation("gradientSpriteUv");
        location_visibleColor = super.getUniformLocation("visibleColor");
        location_bloomColor = super.getUniformLocation("bloomColor");
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    public void loadTextureUnits() {
        super.loadInt(location_targetDepthMaskTexture, 0);
        super.loadInt(location_atlasTexture, 1);
    }

    public void loadUniforms(int width, int height, float time, MiaoOutlineStyle style) {
        MiaoOutlineStyle safeStyle = style == null ? MiaoOutlineStyle.AUTO_TRACKING_RED : style;
        float kindFlag = safeStyle.kind == MiaoOutlineStyle.Kind.SPARKLING_FRUIT_FIRE ? 1.0f : 0.0f;
        float distanceFadeFlag = safeStyle.distanceFadeEnabled ? 1.0f : 0.0f;
        super.loadVector(location_screenSize, new Vector2f(width, height));
        super.loadFloat(location_time, time);
        super.loadVector(location_outlineParams, new Vector4f(safeStyle.outlinePixels, safeStyle.radialSamples, safeStyle.edgeSoftness, kindFlag));
        super.loadVector(location_depthParams, new Vector4f(safeStyle.depthThreshold, safeStyle.depthRange, safeStyle.maskThreshold, 0.0f));
        super.loadVector(location_noiseParams, new Vector4f(safeStyle.noiseSpeedX, safeStyle.noiseSpeedY, safeStyle.noiseIntensity, safeStyle.noiseTiling));
        super.loadVector(location_fireColorParams, new Vector4f(safeStyle.fireColorSpeed, safeStyle.fireGradientEdgeScale, safeStyle.fireGradientNoiseScale, safeStyle.fireColorIntensity));
        super.loadVector(location_fireEdgeParams, new Vector4f(safeStyle.fireEdgeNoiseStrength, safeStyle.fireEdgeCutoff, safeStyle.fireEdgeBreakup, safeStyle.fireEdgeSoftness));
        super.loadVector(location_fireWarpParams, new Vector4f(safeStyle.fireWarpIntensity, safeStyle.fireWarpMix, safeStyle.fireWarpDirectionScale, 0.0f));
        super.loadVector(location_distanceFadeParams, new Vector4f(safeStyle.distanceFadeStart, safeStyle.distanceFadeEnd, safeStyle.distanceMinScale, distanceFadeFlag));
        super.loadVector(location_visibleColor, safeStyle.visibleColor);
        super.loadVector(location_bloomColor, safeStyle.bloomColor);
    }

    public void loadNoiseSpriteUv(float u0, float v0, float u1, float v1) {
        super.loadVector(location_noiseSpriteUv, new Vector4f(u0, v0, u1, v1));
    }

    public void loadGradientSpriteUv(float u0, float v0, float u1, float v1) {
        super.loadVector(location_gradientSpriteUv, new Vector4f(u0, v0, u1, v1));
    }
}
