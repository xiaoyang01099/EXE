package org.xiaoyang.ex_enigmaticlegacy.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// FlySwordBakedModel 为飞剑区分 GUI 二维图标与非 GUI 透明三维模型。
public class FlySwordBakedModel implements BakedModel {
    private final BakedModel guiModel; // GUI 使用的二维贴图模型。
    private final BakedModel handModel; // 非 GUI 使用的三维 Blockbench 模型。
    private ItemDisplayContext lastDisplayContext = ItemDisplayContext.NONE; // 最近一次显示上下文，用于判断是否启用自定义渲染。

    public FlySwordBakedModel(BakedModel guiModel, BakedModel handModel) {
        this.guiModel = guiModel;
        this.handModel = handModel;
    }

    // GUI 直接返回二维模型，其他上下文保留包装器以进入透明后处理。
    @Override
    public BakedModel applyTransform(
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            boolean applyLeftHandTransform) {
        this.lastDisplayContext = displayContext;

        // GUI 必须返回真实二维模型，让物品栏稳定使用 item/generated 的正面光照。
        if (displayContext == ItemDisplayContext.GUI) {
            return guiModel.applyTransform(displayContext, poseStack, applyLeftHandTransform);
        }

        // 非 GUI 路径先应用三维模型变换，再由包装器触发自定义 renderer。
        FlySwordHeldItemRenderer.prepareRenderContext(handModel);
        handModel.applyTransform(displayContext, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource rand,
            ModelData data,
            @Nullable RenderType renderType) {
        return guiModel.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
        return guiModel.getQuads(pState, pDirection, pRandom);
    }

    @Override public boolean useAmbientOcclusion() { return guiModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return guiModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return guiModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return FlySwordHeldItemRenderer.shouldUseHeldPostRenderer(this.lastDisplayContext); }
    @Override public TextureAtlasSprite getParticleIcon() { return guiModel.getParticleIcon(); }
    @Override public ItemOverrides getOverrides() { return guiModel.getOverrides(); }
}
