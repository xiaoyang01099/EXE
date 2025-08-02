package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.CapybaraModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.CapybaraChestLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CapybaraEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@OnlyIn(Dist.CLIENT)
public class CapybaraRenderer extends MobRenderer<CapybaraEntity, CapybaraModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/capybara/capybara.png");
    private static final ResourceLocation MARIO = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/capybara/mario.png");

    public CapybaraRenderer(EntityRendererProvider.Context context) {
        super(context, new CapybaraModel(context.bakeLayer(CapybaraModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new CapybaraChestLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CapybaraEntity entity) {
        if (entity.getName().getString().equals("Mario")) {
            return MARIO;
        }
        return TEXTURE;
    }

    @Override
    protected void setupRotations(CapybaraEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.scale(0.77f, 0.77f, 0.77f);
        if (entity.isInWater() && !entity.isBaby()) {
            poseStack.translate(0, -0.625, 0);
        }
    }
}