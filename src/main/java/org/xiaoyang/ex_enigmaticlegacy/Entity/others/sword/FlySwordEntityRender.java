package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others.TrailRibbonRenderer;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.TrailRibbonRenderType;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;

import java.util.ArrayList;
import java.util.List;

// FlySwordEntityRender 负责飞剑实体本体与拖尾渲染。
public class FlySwordEntityRender extends EntityRenderer<FlySwordEntity> {
    private static final ResourceLocation FLY_TEX = new ResourceLocation(Exe.MODID, "textures/item/fly_sword_tex.png"); // 兼容返回的贴图路径。
    private static final ModelResourceLocation FLY_SWORD_ENTITY_MODEL = new ModelResourceLocation(new ResourceLocation(Exe.MODID, "fly_sword_3d"), "inventory"); // 召唤飞剑使用的原始 3D 模型。
    private final ItemRenderer itemRenderer; // 原版物品渲染器。
    private final ModelManager modelManager; // 客户端模型管理器，支持资源重载后读取最新模型。

    // 构造飞剑实体渲染器。
    public FlySwordEntityRender(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.itemRenderer = pContext.getItemRenderer();
        this.modelManager = pContext.getModelManager();
    }

    @Override
    public void render(FlySwordEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        // 飞剑 renderer 只记录本帧需要拖尾，真正矩阵在 AFTER_ENTITIES 阶段统一补齐。
        if (Exe.POST != null && pEntity.getMoveState().getID() != 0) {
            Exe.POST.requestFlySwordTrail(pEntity);
        }

        pPoseStack.pushPose();
        // 直接使用原始 3D 烘焙模型，避免包装模型触发手持飞剑的透明后处理渲染。
        ItemStack stack = ModWeapons.FLY_SWORD.get().getDefaultInstance();
        BakedModel model = modelManager.getModel(FLY_SWORD_ENTITY_MODEL);
        float yRot = Mth.rotLerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot());
        pPoseStack.translate(0.0, 1.0, 0.0);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));

        if (pEntity.getMoveState() != FlySwordEntity.MoveState.FOLLOW) {
            pPoseStack.mulPose(Axis.YN.rotationDegrees(-yRot));
            pPoseStack.mulPose(Axis.ZN.rotationDegrees(90));
            pPoseStack.mulPose(Axis.XN.rotationDegrees(90));
        }

        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                false,
                pPoseStack,
                pBuffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                model
        );
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlySwordEntity pEntity) {
        return FLY_TEX;
    }

    // 渲染飞剑拖尾。
    public static void renderTrail(FlySwordEntity entity, float partialTick, Matrix4f modelMatrix, MultiBufferSource bufferSource, Vec3 cameraPos) {
        Matrix4f pose = new Matrix4f(modelMatrix);
        List<Vec3> points = buildInterpolatedTrailPoints(entity.getPrePosList(), partialTick);
        TrailRibbonRenderer.render(
                points,
                bufferSource.getBuffer(TrailRibbonRenderType.getRenderType()),
                pose,
                cameraPos,
                new Vec3(0.0, entity.getBbHeight() - 0.5f, 0.0)
        );
    }

    // 生成插值后的拖尾点位，避免帧间跳变。
    public static List<Vec3> buildInterpolatedTrailPoints(List<Vec3> tickPoints, float partialTick) {
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < tickPoints.size() - 1; i++) {
            Vec3 current = tickPoints.get(i);
            Vec3 previous = tickPoints.get(i + 1);
            points.add(new Vec3(
                    Mth.lerp(partialTick, previous.x, current.x),
                    Mth.lerp(partialTick, previous.y, current.y),
                    Mth.lerp(partialTick, previous.z, current.z)
            ));
        }
        return points;
    }
}
