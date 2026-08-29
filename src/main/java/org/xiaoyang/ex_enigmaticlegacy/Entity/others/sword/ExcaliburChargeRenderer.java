package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.ExcaliburSpiralTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// ExcaliburChargeRenderer 是咖喱棒蓄力同步实体的空渲染器，负责提交玩家中心金色螺旋和双材质粒子。
public class ExcaliburChargeRenderer extends EntityRenderer<ExcaliburChargeEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public ExcaliburChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 每帧提交玩家中心螺旋和倒锥粒子，并给第 10 tick 爆发提供玩家脚下锚点。
    @Override
    public void render(ExcaliburChargeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Exe.POST == null || entity == null) return;
        Player owner = entity.getOwnerPlayer();
        Vec3 anchor = owner == null ? entity.getPosition(partialTick) : entity.getPlayerCenterAnchor(owner, partialTick);
        Vec3 footAnchor = owner == null
                ? anchor.add(0.0D, -0.90D, 0.0D)
                : owner.getPosition(partialTick).add(0.0D, 0.08D, 0.0D);
        ExcaliburChargeParticleEffects.emitChargeParticles(entity, anchor, footAnchor);
        float ageTicks = entity.getChargeAge() + partialTick;
        float releaseAgeTicks = entity.getReleaseAge() + partialTick;
        Exe.POST.submit(ExcaliburSpiralTask.create(anchor, ageTicks, entity.getFullChargeTicks(),
                entity.isReleased(), releaseAgeTicks, entity.getVisualSeed()));
    }

    @Override
    public boolean shouldRender(ExcaliburChargeEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ExcaliburChargeEntity entity) {
        return TEXTURE;
    }
}
