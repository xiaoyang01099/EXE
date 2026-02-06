package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.IBoundRender;
import vazkii.botania.api.item.ICoordBoundItem;
import vazkii.botania.client.core.handler.ClientTickHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWireframeAABBProvider;

import java.awt.Color;


@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
@OnlyIn(Dist.CLIENT)
public class BoundRenderHandler {
    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            renderBoundBlocks(event.getPoseStack(), event.getPartialTick());
        }
    }

    private void renderBoundBlocks(PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty() || !(stack.getItem() instanceof ICoordBoundItem)) {
            return;
        }

        int color = Color.HSBtoRGB((float)(ClientTickHandler.ticksInGame % 200) / 200.0F, 0.6F, 1.0F);

        BlockPos[] coords = null;
        HitResult hitResult = mc.hitResult;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity tile = null;
            if (mc.level != null) {
                tile = mc.level.getBlockEntity(pos);
            }

            if (tile instanceof IBoundRender) {
                coords = ((IBoundRender) tile).getBlocksCoord();
            }
        }

        if (coords != null && coords.length > 0) {
            setupRenderState();

            Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            for (BlockPos coord : coords) {
                if (coord != null && coord.getY() != -1) {
                    renderBlockOutlineAt(poseStack, coord, color, 1.0F);
                }
            }

            poseStack.popPose();
            restoreRenderState();
        }
    }

    private void setupRenderState() {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
    }

    private void restoreRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private void renderBlockOutlineAt(PoseStack poseStack, BlockPos pos, int color, float thickness) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        AABB aabb;
        if (block instanceof IWireframeAABBProvider) {
            aabb = ((IWireframeAABBProvider) block).getWireframeAABB(level, pos);
        } else {
            aabb = blockState.getShape(level, pos).bounds();
            if (aabb.getSize() == 0) {
                aabb = new AABB(0, 0, 0, 1, 1, 1);
            }
        }

        aabb = aabb.move(pos);

        Color colorRGB = new Color(color);
        float r = colorRGB.getRed() / 255.0F;
        float g = colorRGB.getGreen() / 255.0F;
        float b = colorRGB.getBlue() / 255.0F;

        RenderSystem.lineWidth(thickness);
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        renderAABBOutline(poseStack, aabb);

        RenderSystem.lineWidth(thickness + 3.0F);
        RenderSystem.setShaderColor(r, g, b, 0.25F);
        renderAABBOutline(poseStack, aabb);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.lineWidth(1.0F);
    }

    private void renderAABBOutline(PoseStack poseStack, AABB aabb) {
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);

        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).endVertex();

        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).endVertex();

        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).endVertex();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).endVertex();

        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).endVertex();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).endVertex();

        tesselator.end();
    }
}