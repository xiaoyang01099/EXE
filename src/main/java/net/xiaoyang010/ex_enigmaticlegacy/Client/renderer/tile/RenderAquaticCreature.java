package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.AquaticCreature;

public abstract class RenderAquaticCreature<T extends AquaticCreature> extends MobRenderer<T, EntityModel<T>> {

    public RenderAquaticCreature(EntityRendererProvider.Context context, EntityModel<T> model, float shadowSize) {
        super(context, model, shadowSize);
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        if (entity.getRotatePitch()) {
            poseStack.mulPose(Vector3f.XP.rotation(-entity.getCurrentPitch(partialTicks) * ((float)Math.PI / 180F)));
        }
    }

    @Override
    protected void scale(T living, PoseStack poseStack, float partialTicks) {
        float x = Mth.cos(((float)(living.tickCount + living.randNumTick) + partialTicks) * 0.16F);
        float y = Mth.sin(((float)(living.tickCount + living.randNumTick) + partialTicks) * 0.12F);
        float z = Mth.sin(((float)(living.tickCount + living.randNumTick) + partialTicks) * 0.08F);
        float moveScale = 0.04F;

        if (living.isInWater() && !living.isPassenger()) {
            poseStack.translate(x * moveScale, y * moveScale, z * moveScale);
        }
    }
}