package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.DimensionSlashScreenEffect;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.ShaderProgram;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class FinalShader extends ShaderProgram {
    private static final ResourceLocation VERTEX_FILE = new ResourceLocation(Exe.MODID, "shaders/post/final_shader.vsh");
    private static final ResourceLocation FRAGMENT_FILE = new ResourceLocation(Exe.MODID, "shaders/post/final_shader.fsh");
    private int location_colourTexture;
    private int location_mainTexture;
    private int location_bloomTexture;
    private int location_bloomStrength;
    private int location_dimensionEffect;
    private int location_dimensionGlass;
    private int location_dimensionField;

    public FinalShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_colourTexture = super.getUniformLocation("colourTexture");
        location_mainTexture = super.getUniformLocation("mainTexture");
        location_bloomTexture = super.getUniformLocation("bloomTexture");
        location_bloomStrength = super.getUniformLocation("bloomStrength");
        location_dimensionEffect = super.getUniformLocation("DimensionEffect");
        location_dimensionGlass = super.getUniformLocation("DimensionGlass");
        location_dimensionField = super.getUniformLocation("DimensionField");
    }

    public void loadUniforms(){
        super.loadInt(location_colourTexture, 0);
        super.loadInt(location_mainTexture, 1);
        super.loadInt(location_bloomTexture, 2);
        super.loadFloat(location_bloomStrength, 1.5f);
        super.loadVector(location_dimensionEffect, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
        super.loadVector(location_dimensionGlass, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
        super.loadVector(location_dimensionField, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
    }

    public void loadDimensionSlashEffect(DimensionSlashScreenEffect effect) {
        if (effect == null) {
            super.loadVector(location_dimensionEffect, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
            super.loadVector(location_dimensionGlass, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
            super.loadVector(location_dimensionField, new org.joml.Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
            return;
        }
        super.loadVector(location_dimensionEffect, new org.joml.Vector4f(effect.blueIntensity, effect.grayIntensity, effect.cutIntensity, effect.cutProgress));
        super.loadVector(location_dimensionGlass, new org.joml.Vector4f(effect.seed, effect.flashIntensity, effect.zoomBlurStrength, effect.contrastBoost));
        super.loadVector(location_dimensionField, new org.joml.Vector4f(effect.chromaticStrength, effect.vignetteStrength, effect.wallStrength, DimensionSlashConfig.CUT_STRENGTH));
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
