package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class BattoSlashRenderer extends EntityRenderer<BattoSlashEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/flowery.png");

    public BattoSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 把拔刀斩实体提交到后处理 bloom 队列。
    @Override
    public void render(BattoSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null) return;
        Exe.POST.addBloomTask(entity, poseStack);
        BattoSlashParticleEffects.emitAppearanceParticles(entity, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(BattoSlashEntity entity) {
        return TEXTURE;
    }
}
