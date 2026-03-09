
package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.InfinityPotatoTile;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.api.item.TinyPotatoRenderEvent;
import vazkii.botania.client.core.proxy.ClientProxy;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.regex.Pattern;

public class InfinityPotatoRender implements BlockEntityRenderer<InfinityPotatoTile> {
    public static final String DEFAULT = "default";
    public static final String HALLOWEEN = "halloween";
    private static final Pattern ESCAPED = Pattern.compile("[^a-z0-9/._-]");
    private static final ResourceLocation TE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/infinitato/infinity_potato.png");
    private final BlockRenderDispatcher blockRenderDispatcher;
    public InfinityPotatoRender(BlockEntityRendererProvider.Context ctx) {
        this.blockRenderDispatcher = ctx.getBlockRenderDispatcher();
    }

    private static boolean matches(String name, String match) {
        return name.equals(match) || name.startsWith(match + " ");
    }

    private static String removeFromFront(String name, String match) {
        return name.substring(match.length()).trim();
    }

    public static BakedModel getModelFromDisplayName(Component displayName) {
        return getModel(stripShaderName(displayName.getString().trim().toLowerCase(Locale.ROOT)).getSecond());
    }

    private static Pair<String, String> stripShaderName(String name) {
        if (matches(name, "gaia")) {
            return Pair.of(null, removeFromFront(name, "gaia"));
        } else if (matches(name, "hot")) {
            return Pair.of(null, removeFromFront(name, "hot"));
        } else if (matches(name, "magic")) {
            return Pair.of(null, removeFromFront(name, "magic"));
        } else if (matches(name, "gold")) {
            return Pair.of(null, removeFromFront(name, "gold"));
        } else if (matches(name, "snoop")) {
            return Pair.of(null, removeFromFront(name, "snoop"));
        } else {
            return Pair.of(null, name);
        }
    }

    private static BakedModel getModel(String name) {
        ModelManager mm = Minecraft.getInstance().getModelManager();
        ResourceLocation location = taterLocation(name);
        BakedModel model = mm.getModel(location);
        if (model == mm.getMissingModel()) {
            if (ClientProxy.dootDoot) {
                return mm.getModel(taterLocation(HALLOWEEN));
            } else {
                return mm.getModel(taterLocation(DEFAULT));
            }
        }
        return model;
    }

    private static ResourceLocation taterLocation(String name) {
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/infinitato/infinity_potato.png");
    }

    private static String normalizeName(String name) {
        return ESCAPED.matcher(name).replaceAll("_");
    }

    private static RenderType getRenderLayer() {
        return RenderType.translucent();
    }

    @Override
    public void render(@Nonnull InfinityPotatoTile potato, float partialTicks, PoseStack ms, @Nonnull MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();

        String name = potato.name.getString().toLowerCase(Locale.ROOT).trim();
        Pair<String, String> shaderStrippedName = stripShaderName(name);
        name = shaderStrippedName.getSecond();
        RenderType layer = getRenderLayer();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BlockState blockState = potato.getBlockState();
        BakedModel model = dispatcher.getBlockModel(blockState);

        ms.translate(0.5F, 0F, 0.5F);
        Direction potatoFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float rotY = 0;
        switch (potatoFacing) {
            case SOUTH:
                rotY = 180F;
                break;
            case NORTH:
                break;
            case EAST:
                rotY = 90F;
                break;
            case WEST:
                rotY = 270F;
                break;
        }
        ms.mulPose(Vector3f.YN.rotationDegrees(rotY));

        float jump = potato.jumpTicks;
        if (jump > 0) {
            jump -= partialTicks;
        }

        float up = (float) Math.abs(Math.sin(jump / 10 * Math.PI)) * 0.2F;
        float rotZ = (float) Math.sin(jump / 10 * Math.PI) * 2;
        float wiggle = (float) Math.sin(jump / 10 * Math.PI) * 0.05F;

        ms.translate(wiggle, up, 0F);
        ms.mulPose(Vector3f.ZP.rotationDegrees(rotZ));

        boolean render = !(name.equals("mami") || name.equals("soaryn") || name.equals("eloraam") && jump != 0);
        if (render) {
            ms.pushPose();
            ms.translate(-0.5F, 0, -0.5F);
            VertexConsumer buffer = ItemRenderer.getFoilBuffer(buffers, layer, true, false);

            renderModel(ms, buffer, light, overlay, model);
            ms.popPose();
        }

        ms.translate(0F, 1.5F, 0F);
        ms.pushPose();
        ms.mulPose(Vector3f.ZP.rotationDegrees(180F));
        renderItems(potato, potatoFacing, name, partialTicks, ms, buffers, light, overlay);

        ms.pushPose();
        MinecraftForge.EVENT_BUS.post(new TinyPotatoRenderEvent(potato, potato.name, partialTicks, ms, buffers, light, overlay));
        ms.popPose();
        ms.popPose();

        ms.mulPose(Vector3f.ZP.rotationDegrees(-rotZ));
        ms.mulPose(Vector3f.YN.rotationDegrees(-rotY));

        renderName(potato, name, ms, buffers, light);
        ms.popPose();
    }

    private void renderName(InfinityPotatoTile potato, String name, PoseStack ms, MultiBufferSource buffers, int light) {
        Minecraft mc = Minecraft.getInstance();
        HitResult pos = mc.hitResult;
        if (!name.isEmpty() && pos != null && pos.getType() == HitResult.Type.BLOCK
                && potato.getBlockPos().equals(((BlockHitResult) pos).getBlockPos())) {
            ms.pushPose();
            ms.translate(0F, -0.6F, 0F);
            ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            float f1 = 0.016666668F * 1.6F;
            ms.scale(-f1, -f1, f1);
            int halfWidth = mc.font.width(potato.name.getString()) / 2;

            float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int opacityRGB = (int) (opacity * 255.0F) << 24;
            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, true, opacityRGB, light);
            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, false, 0, light);
            if (name.equals("pahimar") || name.equals("soaryn")) {
                ms.translate(0F, 14F, 0F);
                String str = name.equals("pahimar") ? "[WIP]" : "(soon)";
                halfWidth = mc.font.width(str) / 2;

                mc.font.drawInBatch(str, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, true, opacityRGB, light);
                mc.font.drawInBatch(str, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, true, 0, light);
            }

            ms.popPose();
        }
    }

    private void renderItems(InfinityPotatoTile potato, Direction facing, String name, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
    }

    private void renderModel(PoseStack ms, VertexConsumer buffer, int light, int overlay, BakedModel model) {
        this.blockRenderDispatcher.getModelRenderer().renderModel(ms.last(), buffer, null, model, 1, 1, 1, light, overlay);
    }

    private void renderItem(PoseStack ms, MultiBufferSource buffers, int light, int overlay, ItemStack stack) {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, light, overlay, ms, buffers, 0);
    }

    private void renderBlock(PoseStack ms, MultiBufferSource buffers, int light, int overlay, Block block) {
       this.blockRenderDispatcher.renderSingleBlock(block.defaultBlockState(), ms, buffers, light, overlay);
    }
}
