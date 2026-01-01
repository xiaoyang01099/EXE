package net.xiaoyang010.ex_enigmaticlegacy.api;

import codechicken.lib.model.bakedmodels.WrappedItemModel;
import codechicken.lib.render.buffer.AlphaOverrideVertexConsumer;
import codechicken.lib.render.item.IItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.SimpleModelState;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EndPortalHaloBakedModel extends WrappedItemModel implements IItemRenderer {

    // ✅ 复用 Cosmic 模型的生成器
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private final TextureAtlasSprite portalSprite;
    private final List<BakedQuad> maskQuads;  // ✅ 遮罩四边形
    private final int size;
    private final boolean pulse;
    private final boolean animated;

    public EndPortalHaloBakedModel(BakedModel wrapped, TextureAtlasSprite portalSprite, TextureAtlasSprite maskSprite,  // ✅ 新增参数
                                   int size, boolean pulse, boolean animated) {
        super(wrapped);
        this.portalSprite = portalSprite;
        this.size = size;
        this.pulse = pulse;
        this.animated = animated;

        // ✅ 根据遮罩生成四边形
        this.maskQuads = bakeItem(maskSprite);
    }

    /**
     * ✅ 核心：根据遮罩纹理生成 BakedQuad（完全复制 Cosmic 的逻辑）
     */
    private static List<BakedQuad> bakeItem(TextureAtlasSprite maskSprite) {
        List<BakedQuad> quads = new LinkedList<>();

        // 使用 Minecraft 标准物品模型生成器
        for (int i = 0; i < 1; i++) {  // 只有一层遮罩
            for (BlockElement element : ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, maskSprite)) {

                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), maskSprite, entry.getKey(), SimpleModelState.IDENTITY, element.rotation, element.shade, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "dynamic")));
                }
            }
        }

        return quads;
    }

    @Override
    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {

        // 1. 先渲染基础物品
        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);

        // 强制刷新缓冲区
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        // 2. 渲染末地传送门效果（使用遮罩）
        renderPortalEffect(stack, transformType, pStack, source, packedLight, packedOverlay);

        // 3. 脉冲效果（可选，仅 GUI）
        if (this.pulse && transformType == TransformType.GUI) {
            renderPulseEffect(stack, pStack, source, packedLight, packedOverlay);
        }
    }

    /**
     * 渲染末地传送门效果（使用遮罩）
     */
    private void renderPortalEffect(ItemStack stack, TransformType transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        pStack.pushPose();

        // 根据视角调整缩放
        float scale = getScaleForTransform(transformType);
        if (scale != 1.0F) {
            pStack.translate(0.5, 0.5, 0.5);
            pStack.scale(scale, scale, scale);
            pStack.translate(-0.5, -0.5, -0.5);
        }

        // 旋转动画
        if (this.animated) {
            float time = (System.currentTimeMillis() % 8000L) / 8000.0F;
            pStack.translate(0.5, 0.5, 0);
            pStack.mulPose(Vector3f.ZP.rotationDegrees(time * 360.0F));
            pStack.translate(-0.5, -0.5, 0);
        }

        // ✅ 使用末地传送门 RenderType
        RenderType renderType = RenderType.endPortal();
        VertexConsumer buffer = source.getBuffer(renderType);

        // ✅ 渲染遮罩四边形（而不是手动构建的矩形）
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderQuadList(pStack, buffer, this.maskQuads,  // ← 使用遮罩形状
                stack, LightTexture.FULL_BRIGHT, packedOverlay);

        pStack.popPose();
    }

    /**
     * 渲染脉冲效果
     */
    private void renderPulseEffect(ItemStack stack, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        pStack.pushPose();

        double time = System.currentTimeMillis() / 1000.0;
        double scale = 0.95 + Math.sin(time * Math.PI) * 0.05;
        double trans = (1.0 - scale) / 2.0;

        pStack.translate(trans, trans, 0.0);
        pStack.scale((float) scale, (float) scale, 1.0001F);

        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true, e -> new AlphaOverrideVertexConsumer(e, 0.5F));

        pStack.popPose();
    }

    /**
     * 根据变换类型获取缩放比例
     */
    private float getScaleForTransform(TransformType transformType) {
        return switch (transformType) {
            case GUI -> 1.0F;
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> 0.8F;
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> 0.6F;
            case GROUND -> 0.5F;
            default -> 1.0F;
        };
    }

    @Override
    public ModelState getModelTransform() {
        return this.parentState;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }
}