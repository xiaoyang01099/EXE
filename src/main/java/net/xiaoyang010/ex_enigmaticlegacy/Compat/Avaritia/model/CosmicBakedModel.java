package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model;

import codechicken.lib.render.item.IItemRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static morph.avaritia.init.AvaritiaModContent.MATTER_CLUSTER;

public class CosmicBakedModel extends WrappedItemModel implements IItemRenderer {
    private final TextureAtlasSprite maskSprite;

    public CosmicBakedModel(BakedModel wrapped, TextureAtlasSprite maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.maskQuad = bakeItem(maskSprite);
    }

    public static boolean isBlockContext(TransformType transformType) {
        switch (transformType) {
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
            case GROUND:
            case FIXED:
            case GUI:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void renderItem(ItemStack stack, TransformType transformType, PoseStack mStack, MultiBufferSource source, int light, int overlay) {
        renderWrapped(stack, mStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs)
            bs.endBatch();

        if (stack.getItem() == MATTER_CLUSTER.get()) {
//            AvaritiaShaders.cosmicOpacity.set(getMatterClusterOpacity(stack));
        } else {
            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }

        Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 25.0F;
        if (transformType != TransformType.GUI && !AvaritiaShaders.inventoryRender) {
            if (mc.player != null) {
                yaw = (float) ((mc.player.getYHeadRot() * 2.0F) * Math.PI / 360.0D);
                pitch = -((float) ((mc.player.getXRot() * 2.0F) * Math.PI / 360.0D));
                scale = 1.0F;
            }
        }

        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);

        VertexConsumer cosmicConsumer = source.getBuffer(RenderType.create("cosmic", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 0, true, true,
                RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.cosmicShader)).setTextureState(RenderType.BLOCK_SHEET).setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setWriteMaskState(RenderType.COLOR_WRITE).setLightmapState(RenderType.LIGHTMAP).createCompositeState(true)));

        BakedModel model = this.wrapped.getOverrides().resolve(this.wrapped, stack, this.world, this.entity, 0);
        if (model != null && model.isGui3d() && isBlockContext(transformType)) {
            renderBlockItem(mStack, cosmicConsumer, model, stack, light, overlay, mc);
        } else {
            mc.getItemRenderer().renderQuadList(mStack, cosmicConsumer, this.maskQuad, stack, light, overlay);
        }
    }

    private void renderBlockItem(PoseStack mStack, VertexConsumer cosmicConsumer, BakedModel model, ItemStack stack, int light, int overlay, Minecraft mc) {
        List<BakedQuad> blockLayer = new ArrayList<>();
        Random random = new Random();

        for (Direction direction : Direction.values()) {
            blockLayer.addAll(model.getQuads((BlockState) null, direction, random));
        }

        List<TextureAtlasSprite> maskSprites = new ArrayList<>();
        maskSprites.add(this.maskSprite);

        List<BakedQuad> overlayQuads = new ArrayList<>();

        for (BakedQuad base : blockLayer) {
            for (TextureAtlasSprite sprite : maskSprites) {
                BakedQuad masked = new BakedQuad(base.getVertices(), base.getTintIndex(), base.getDirection(), sprite, base.isShade());
                overlayQuads.add(masked);
            }
        }

        mc.getItemRenderer().renderQuadList(mStack, cosmicConsumer, overlayQuads, stack, light, overlay);
    }

//    public float getMatterClusterOpacity(ItemStack itemStack){
//        float i = MATTER_CLUSTER.getItemTag(itemStack).size() / (Config.SERVER.matterClusterMaxTerm.get() * 1.0f);
//        return (float) (Math.floor(i * 100) / 100.f);
//    }

    @Override
    boolean isCosmic() {
        return true;
    }
}