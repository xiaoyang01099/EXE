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

// TridentPlusBakedModel 为天雷战戟区分 GUI 默认图标和手持原版三叉戟自定义渲染。
public class TridentPlusBakedModel implements BakedModel {
    private final BakedModel guiModel; // 背包和 GUI 中使用的默认模型。
    private final BakedModel handModel; // 第一人称和第三人称手持上下文使用的姿态模型。
    private ItemDisplayContext lastDisplayContext = ItemDisplayContext.NONE; // 最近一次渲染上下文，用于判断是否启用自定义渲染。

    public TridentPlusBakedModel(BakedModel guiModel, BakedModel handModel) {
        this.guiModel = guiModel;
        this.handModel = handModel;
    }

    // 按显示上下文切换模型，GUI 始终走默认 baked quads，其余手持上下文交给自定义渲染。
    @Override
    public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
        this.lastDisplayContext = displayContext;
        TridentPlusItemRenderer.prepareRenderContext(displayContext);
        return switch (displayContext) {
            case GUI -> guiModel.applyTransform(displayContext, poseStack, applyLeftHandTransform);
            default -> handModel.applyTransform(displayContext, poseStack, applyLeftHandTransform);
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        return guiModel.getQuads(state, side, random, data, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return guiModel.getQuads(state, side, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return guiModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return guiModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return guiModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.lastDisplayContext != ItemDisplayContext.GUI;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return guiModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return guiModel.getOverrides();
    }
}
