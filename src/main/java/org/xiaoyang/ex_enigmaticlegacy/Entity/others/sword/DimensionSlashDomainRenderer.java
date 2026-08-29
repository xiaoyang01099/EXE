package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// DimensionSlashDomainRenderer 是次元斩领域空渲染器，负责把领域视觉提交给后处理。
public class DimensionSlashDomainRenderer extends EntityRenderer<DimensionSlashDomainEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public DimensionSlashDomainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 每帧提交领域屏幕效果和粒子效果。
    @Override
    public void render(DimensionSlashDomainEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null) return;
        Exe.POST.addDimensionSlashField(entity, partialTick);
        DimensionSlashParticleEffects.emitDomainParticles(entity, partialTick);
        DimensionSlashClientEffects.tryPlayShake(entity);
    }

    @Override
    public ResourceLocation getTextureLocation(DimensionSlashDomainEntity entity) {
        return TEXTURE;
    }
}
