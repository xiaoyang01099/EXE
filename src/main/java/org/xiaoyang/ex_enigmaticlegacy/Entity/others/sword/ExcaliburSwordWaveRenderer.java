package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// ExcaliburSwordWaveRenderer 是 EX 剑气控制实体的空渲染器，负责按客户端本地 tick 提交动态多路 GPU 粒子。
public class ExcaliburSwordWaveRenderer extends EntityRenderer<ExcaliburSwordWaveEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public ExcaliburSwordWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 每个客户端 tick 只提交一次当前前沿的多路 EX 剑气粒子。
    @Override
    public void render(ExcaliburSwordWaveEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null) return;
        ExcaliburSwordWaveEffects.emitWaveBatch(entity, Exe.POST::addParticle);
    }

    // 控制实体始终触发 Renderer，不受起点处小包围盒的视锥裁剪影响。
    @Override
    public boolean shouldRender(ExcaliburSwordWaveEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ExcaliburSwordWaveEntity entity) {
        return TEXTURE;
    }
}
