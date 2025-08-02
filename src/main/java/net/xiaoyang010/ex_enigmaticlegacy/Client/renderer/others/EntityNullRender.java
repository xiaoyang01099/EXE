package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;
import net.minecraft.client.renderer.MultiBufferSource;

public class EntityNullRender<T extends Entity> extends EntityRenderer<T> {

    public EntityNullRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return DefaultPlayerSkin.getDefaultSkin(UUID.randomUUID());
    }
}