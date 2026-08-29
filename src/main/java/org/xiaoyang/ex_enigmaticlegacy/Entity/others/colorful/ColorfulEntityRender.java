package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class ColorfulEntityRender extends EntityRenderer<ColorfulEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/flowery.png");

    public ColorfulEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ColorfulEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null) return;
        BeamClientEffects.triggerOnce(entity, partialTick);
        Exe.POST.addBloomTask(entity, poseStack);
    }

    @Override
    public ResourceLocation getTextureLocation(ColorfulEntity entity) {
        return TEXTURE;
    }
}
