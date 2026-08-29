package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// SwordAuraRenderer 是剑气空实体渲染器，实际视觉交给 bloom 队列绘制。
public class SwordAuraRenderer extends EntityRenderer<SwordAuraEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public SwordAuraRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 把剑气实体提交给后处理 bloom 队列。
    @Override
    public void render(SwordAuraEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null) return;
        Exe.POST.addBloomTask(entity, poseStack);
    }

    @Override
    public ResourceLocation getTextureLocation(SwordAuraEntity entity) {
        return TEXTURE;
    }
}
