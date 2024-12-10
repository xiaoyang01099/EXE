package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.Repair;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import morph.avaritia.client.AvaritiaShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterShadersEvent;
import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod(ExEnigmaticlegacyMod.MODID)
@EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AvaritiaShaderFix {
    public static final String MODID = "ex_enigmaticlegacy";
    private static boolean isOculusLoaded;

    public AvaritiaShaderFix() {
        isOculusLoaded = ModList.get().isLoaded("oculus");
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onShaderRegistry(RegisterShadersEvent event) {
        if (!isOculusLoaded) return;

        try {
            RenderType.CompositeState oculusState = RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> {
                        if (net.coderbot.iris.Iris.getIrisConfig().areShadersEnabled()) {
                            return net.minecraft.client.renderer.GameRenderer.getRendertypeEntityTranslucentShader();
                        }
                        return AvaritiaShaders.cosmicShader;
                    }))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation("avaritia", "textures/items/cosmic.png"),
                            false,
                            false
                    ))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(true);


            java.lang.reflect.Field renderTypeField = AvaritiaShaders.class.getDeclaredField("COSMIC_RENDER_TYPE");
            renderTypeField.setAccessible(true);

            RenderType newRenderType = RenderType.create(
                    "avaritia:cosmic_patched",
                    DefaultVertexFormat.BLOCK,
                    Mode.QUADS,
                    256,
                    true,
                    true,
                    oculusState
            );

            renderTypeField.set(null, newRenderType);

            // 使用create方法创建CCShaderInstance
            CCShaderInstance shader = CCShaderInstance.create(
                    event.getResourceManager(),
                    new ResourceLocation("avaritia", "cosmic"),
                    DefaultVertexFormat.BLOCK
            );

            if (shader != null) {
                event.registerShader(shader, shaderInstance -> {
                    AvaritiaShaders.cosmicShader = (CCShaderInstance) shaderInstance;

                    // 获取所有必要的uniform变量
                    AvaritiaShaders.cosmicTime = (CCUniform) shaderInstance.getUniform("time");
                    AvaritiaShaders.cosmicYaw = (CCUniform) shaderInstance.getUniform("yaw");
                    AvaritiaShaders.cosmicPitch = (CCUniform) shaderInstance.getUniform("pitch");
                    AvaritiaShaders.cosmicExternalScale = (CCUniform) shaderInstance.getUniform("externalScale");
                    AvaritiaShaders.cosmicOpacity = (CCUniform) shaderInstance.getUniform("opacity");
                    AvaritiaShaders.cosmicUVs = (CCUniform) shaderInstance.getUniform("cosmicuvs");

                    // 设置着色器应用时的回调
                    ((CCShaderInstance) shaderInstance).onApply(() -> {
                        if (AvaritiaShaders.cosmicTime != null) {
                            AvaritiaShaders.cosmicTime.glUniform1f((float)(System.nanoTime() / 1000000000.0));
                        }

                        // 获取当前的ModelView和Projection矩阵
                        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
                        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

                        // 设置矩阵
                        if (shaderInstance.MODEL_VIEW_MATRIX != null) {
                            shaderInstance.MODEL_VIEW_MATRIX.set(modelViewMatrix);
                        }
                        if (shaderInstance.PROJECTION_MATRIX != null) {
                            shaderInstance.PROJECTION_MATRIX.set(projectionMatrix);
                        }
                    });
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}