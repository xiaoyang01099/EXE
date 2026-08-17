package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuo.endless.client.AvaritiaShaders;
import com.yuo.endless.client.model.CosmicBakedModel;
import com.yuo.endless.client.model.WrappedItemModel;
import com.yuo.endless.items.EndlessItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXECosmicItemLateRenderQueue;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CosmicBakedModel.class, remap = false)
public abstract class EXECosmicBakedModelMixin extends WrappedItemModel {
    @Shadow @Final
    private List<ResourceLocation> maskSprite;
    @Shadow
    public abstract float getMatterClusterOpacity(ItemStack itemStack);

    public EXECosmicBakedModelMixin(BakedModel wrapped) {
        super(wrapped);
    }

    @Inject(
            method = "renderItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exe_deferCosmicRendering(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int light,
            int overlay,
            CallbackInfo ci
    ) {
        if (!EXECosmicItemLateRenderQueue.shouldDefer()) {
            return;
        }

        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }Minecraft mc = Minecraft.getInstance();

        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 1.0F;
        if (!AvaritiaShaders.inventoryRender && transformType != ItemDisplayContext.GUI) {
            yaw = (float)((double)(mc.player.getYRot() * 2.0F) * Math.PI / 360.0);
            pitch = -((float)((double)(mc.player.getXRot() * 2.0F) * Math.PI / 360.0));} else {
            scale = 100.0F;
        }

        float opacity = (stack.getItem() == EndlessItems.matterCluster.get())
                ? this.getMatterClusterOpacity(stack)
                : 1.0F;

        float[] uvs = new float[40];
        for (int i = 0; i < 10; i++) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(com.yuo.endless.EndlessUtils.fa("shader/cosmic_" + i));
            uvs[i * 4]     = sprite.getU0();
            uvs[i * 4 + 1] = sprite.getV0();
            uvs[i * 4 + 2] = sprite.getU1();
            uvs[i * 4 + 3] = sprite.getV1();
        }

        float cosmicTime = (float)(System.currentTimeMillis() - (long)AvaritiaShaders.renderTime) / 2000.0F;

        EXECosmicItemLateRenderQueue.CosmicUniforms uniforms =
                new EXECosmicItemLateRenderQueue.CosmicUniforms(cosmicTime, yaw, pitch, scale, opacity, uvs);

        BakedModel model = this.wrapped.getOverrides().resolve(
                this.wrapped, stack, this.world, this.entity, 0
        );

        if (model != null && model.isGui3d() && stack.getItem() instanceof BlockItem) {
            EXECosmicItemLateRenderQueue.enqueue(
                    pStack, exe_buildBlockItemQuads(model), stack, light, overlay, uniforms
            );
        } else {
            List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
            for (ResourceLocation res : this.maskSprite) {
                atlasSprite.add(mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
            }
            EXECosmicItemLateRenderQueue.enqueue(
                    pStack, WrappedItemModel.bakeItem(atlasSprite), stack, light, overlay, uniforms
            );
        }

        ci.cancel();
    }

    @Unique
    private List<BakedQuad> exe_buildBlockItemQuads(BakedModel model) {
        Minecraft mc = Minecraft.getInstance();

        List<BakedQuad> blockLayer = new ArrayList<>();
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            blockLayer.addAll(model.getQuads((BlockState) null, direction, random));
        }

        List<TextureAtlasSprite> maskSprites = new ArrayList<>();
        for (ResourceLocation res : this.maskSprite) {
            maskSprites.add(mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }

        List<BakedQuad> overlayQuads = new ArrayList<>();
        for (BakedQuad base : blockLayer) {
            for (TextureAtlasSprite sprite : maskSprites) {
                overlayQuads.add(new BakedQuad(
                        base.getVertices(),
                        base.getTintIndex(),
                        base.getDirection(),
                        sprite,
                        base.isShade()
                ));
            }
        }

        return overlayQuads;
    }
}