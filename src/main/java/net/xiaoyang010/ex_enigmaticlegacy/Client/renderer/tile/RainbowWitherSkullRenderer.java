package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.RainbowWitherSkullModel;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.RainbowWitherSkull;

public class RainbowWitherSkullRenderer extends EntityRenderer<RainbowWitherSkull> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[36];

    static {
        // 初始化所有贴图位置
        for (int i = 0; i < 36; i++) {
            TEXTURES[i] = new ResourceLocation("ex_enigmaticlegacy",
                    "textures/entity/spectrite_wither/spectrite_wither_invulnerable_skull_layer_1/" + i + "crystalline_stardust_ingot.png");
        }
    }

    private final RainbowWitherSkullModel model;

    protected RainbowWitherSkullRenderer(EntityRendererProvider.Context pContext, RainbowWitherSkullModel model) {
        super(pContext);
        this.model = model;
    }


    @Override
    public void render(RainbowWitherSkull entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 设置渲染大小
        poseStack.scale(2.0F, 2.0F, 2.0F);

        // 计算旋转
        float rotation = Mth.rotlerp(entity.yRotO, entity.getYRot(), partialTicks);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation - 180.0F));

        // 计算当前应该使用的贴图索引
        int textureIndex = (int)((entity.level.getGameTime() % 72) / 2) % 36;
        ResourceLocation texture = TEXTURES[textureIndex];

        // 渲染模型
        this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(texture)),
                packedLight, getPackedOverlay(entity), 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RainbowWitherSkull entity) {
        return TEXTURES[0]; // 默认贴图
    }

    private int getPackedOverlay(RainbowWitherSkull entity) {
        return 15728880; // 最大亮度
    }
}