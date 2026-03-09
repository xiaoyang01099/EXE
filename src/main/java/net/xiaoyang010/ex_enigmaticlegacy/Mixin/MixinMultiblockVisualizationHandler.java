package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.InfinityPotatoTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaContainerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.patchouli.client.handler.MultiblockVisualizationHandler;

@Mixin(value = MultiblockVisualizationHandler.class, remap = false)
public class MixinMultiblockVisualizationHandler {

    @Inject(
            method = "renderBlock",
            at = @At("HEAD"),
            cancellable = true
    )

    private static void onRenderBlock(Level world, BlockState state, BlockPos pos,
                                      float alpha, PoseStack ms, CallbackInfo ci) {
        Block block = state.getBlock();
        Minecraft mc = Minecraft.getInstance();

        BlockEntity tile = null;
        BlockEntityRenderer renderer = null;

        if (block == ModBlockss.MANA_CONTAINER.get()) {
            tile = new ManaContainerTile(ModBlockEntities.MANA_CONTAINER_TILE.get(), pos, state);
            tile.setLevel(world);
            renderer = mc.getBlockEntityRenderDispatcher().getRenderer(tile);
        } else if (block == ModBlockss.INFINITY_POTATO.get()) {
            tile = new InfinityPotatoTile(ModBlockEntities.INFINITY_POTATO.get(), pos, state);
            tile.setLevel(world);
            renderer = mc.getBlockEntityRenderDispatcher().getRenderer(tile);
        } else if (block == ModBlocks.fabulousPool) {
            tile = new TilePool(pos, state);
            tile.setLevel(world);
            renderer = mc.getBlockEntityRenderDispatcher().getRenderer(tile);
        }

        if (renderer != null && tile != null) {
            ms.pushPose();
            ms.translate(pos.getX(), pos.getY(), pos.getZ());

            MultiBufferSource buffers = getBuffers();

            if (buffers != null) {
                try {
                    renderer.render(tile, mc.getFrameTime(), ms, buffers, 0xF000F0,
                            OverlayTexture.NO_OVERLAY);
                } catch (Exception e) {
                }
            }

            ms.popPose();
            ci.cancel();
        }
    }

    private static MultiBufferSource getBuffers() {
        try {
            var field = MultiblockVisualizationHandler.class.getDeclaredField("buffers");
            field.setAccessible(true);
            return (MultiBufferSource) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
}