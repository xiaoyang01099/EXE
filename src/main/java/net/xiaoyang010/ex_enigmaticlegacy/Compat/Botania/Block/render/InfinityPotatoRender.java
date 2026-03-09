package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.InfinityPotatoTile;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import vazkii.botania.api.item.TinyPotatoRenderEvent;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.common.handler.ContributorList;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemFlightTiara;

import javax.annotation.Nonnull;
import java.util.Locale;

public class InfinityPotatoRender implements BlockEntityRenderer<InfinityPotatoTile> {
    public static final ResourceLocation HALO_TEXTURE = new ResourceLocation("avaritia", "textures/item/halo128.png");
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

    private static RenderType getRenderLayer() {
        return RenderType.translucent();
    }

    @Override
    public void render(@Nonnull InfinityPotatoTile potato, float partialTicks, PoseStack ms, @Nonnull MultiBufferSource buffers, int light, int overlay) {
//        if (ConfigHandler.InfinityPotatoConfig.isDrawHalo()) {
//            renderHalo(potato, partialTicks, ms, buffers);
//        }

        if (ConfigHandler.InfinityPotatoConfig.isDrawHalo()) {
            renderHalo(potato, partialTicks, ms, buffers, light);
        }

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
            case SOUTH -> rotY = 180F;
            case NORTH -> rotY = 0F;
            case EAST -> rotY = 90F;
            case WEST -> rotY = 270F;
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

        boolean render = !(name.equals("mami") || name.equals("soaryn")
                || name.equals("eloraam") && jump != 0);
        if (render) {
            ms.pushPose();
            ms.translate(-0.5F, 0, -0.5F);
            VertexConsumer buffer = ItemRenderer.getFoilBuffer(
                    buffers, layer, true, false);
            renderModel(ms, buffer, light, overlay, model);
            ms.popPose();
        }

        ms.translate(0F, 1.5F, 0F);
        ms.pushPose();
        ms.mulPose(Vector3f.ZP.rotationDegrees(180F));
        renderItems(potato, potatoFacing, name, partialTicks, ms, buffers, light, overlay);

        ms.pushPose();
        MinecraftForge.EVENT_BUS.post(new TinyPotatoRenderEvent(
                potato, potato.name, partialTicks, ms, buffers, light, overlay));
        ms.popPose();
        ms.popPose();

        ms.mulPose(Vector3f.ZP.rotationDegrees(-rotZ));
        ms.mulPose(Vector3f.YN.rotationDegrees(-rotY));

        renderName(potato, name, ms, buffers, light);
        ms.popPose();


    }

    private static RenderType createHaloRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(HALO_TEXTURE, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false);
        return RenderType.create(
                "infinity_potato_halo",
                com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                state
        );
    }

//    private void renderHalo(InfinityPotatoTile potato, float partialTicks, PoseStack ms, MultiBufferSource buffers) {
//        Minecraft mc = Minecraft.getInstance();
//        ms.pushPose();
//        ms.translate(0.5, 0.4, 0.5);
//        ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
//        float haloScale = 0.65F;
//        ms.scale(haloScale, haloScale, haloScale);
//        RenderType haloRenderType = createHaloRenderType();
//        VertexConsumer buffer = buffers.getBuffer(haloRenderType);
//        Matrix4f mat = ms.last().pose();
//        float size = 1.0F;
//        buffer.vertex(mat, -size, -size, 0)
//                .color(255, 255, 255, 200)
//                .uv(0, 0)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(0xF000F0)
//                .normal(0, 0, 1)
//                .endVertex();
//        buffer.vertex(mat, -size, size, 0)
//                .color(255, 255, 255, 200)
//                .uv(0, 1)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(0xF000F0)
//                .normal(0, 0, 1)
//                .endVertex();
//        buffer.vertex(mat, size, size, 0)
//                .color(255, 255, 255, 200)
//                .uv(1, 1)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(0xF000F0)
//                .normal(0, 0, 1)
//                .endVertex();
//        buffer.vertex(mat, size, -size, 0)
//                .color(255, 255, 255, 200)
//                .uv(1, 0)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(0xF000F0)
//                .normal(0, 0, 1)
//                .endVertex();
//        ms.popPose();
//    }

    private void renderHalo(InfinityPotatoTile potato, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
        Minecraft mc = Minecraft.getInstance();

        ms.pushPose();
        ms.translate(0.5, 0.4, 0.5);
        ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        float haloScale = 0.65F;
        ms.scale(haloScale, haloScale, haloScale);

        RenderType haloRenderType = RenderType.entityTranslucent(HALO_TEXTURE);
        VertexConsumer buffer = buffers.getBuffer(haloRenderType);

        Matrix4f mat = ms.last().pose();
        float size = 1.0F;

        buffer.vertex(mat, -size, -size, 0)
                .color(255, 255, 255, 200)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(mat, -size, size, 0)
                .color(255, 255, 255, 200)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(mat, size, size, 0)
                .color(255, 255, 255, 200)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(mat, size, -size, 0)
                .color(255, 255, 255, 200)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

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
            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0x20FFFFFF, false,
                    ms.last().pose(), buffers, true, opacityRGB, light);
            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0xFFFFFFFF, false,
                    ms.last().pose(), buffers, false, 0, light);

            if (name.equals("pahimar") || name.equals("soaryn")) {
                ms.translate(0F, 14F, 0F);
                String str = name.equals("pahimar") ? "[WIP]" : "(soon)";
                halfWidth = mc.font.width(str) / 2;

                mc.font.drawInBatch(str, -halfWidth, 0, 0x20FFFFFF, false,
                        ms.last().pose(), buffers, true, opacityRGB, light);
                mc.font.drawInBatch(str, -halfWidth, 0, 0xFFFFFFFF, false,
                        ms.last().pose(), buffers, true, 0, light);
            }

            ms.popPose();
        }
    }

    private void renderItems(InfinityPotatoTile potato, Direction facing, String name, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();
        ms.mulPose(Vector3f.ZP.rotationDegrees(180F));
        ms.translate(0F, -1F, 0F);
        float s = 1F / 3.5F;
        ms.scale(s, s, s);

        for (int i = 0; i < potato.getContainerSize(); i++) {
            ItemStack stack = potato.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            ms.pushPose();
            Direction side = Direction.values()[i];
            if (side.getAxis() != Direction.Axis.Y) {
                float sideAngle = side.toYRot() - facing.toYRot();
                side = Direction.fromYRot(sideAngle);
            }

            boolean block = stack.getItem() instanceof BlockItem;
            boolean mySon = stack.getItem() == ModBlockss.INFINITY_POTATO.get().asItem();

            switch (side) {
                case UP:
                    if (mySon) {
                        ms.translate(0F, 2.0F, 1.0F);
                    } else if (block) {
                        ms.translate(0F, 0.6F, 0.5F);
                    }
                    ms.translate(0F, 0.6F, -0.4F);
                    break;
                case DOWN:
                    ms.translate(0, -2.3F, -1.35F);
                    if (mySon) {
                        ms.translate(0, 1.5F, 0.8F);
                    } else if (block) {
                        ms.translate(0, 1, 0.6F);
                    }
                    break;
                case NORTH:
                    ms.translate(0, -1.2F, 0.48F);
                    if (mySon) {
                        ms.translate(0, 2.6, 0.68F);
                    } else if (block) {
                        ms.translate(0, 1, 0.6F);
                    }
                    break;
                case SOUTH:
                    ms.translate(0, -1.2F, -1.35F);
                    if (mySon) {
                        ms.translate(0, 3.4F, 1.2F);
                    } else if (block) {
                        ms.translate(0, 1.0F, 0.5F);
                    }
                    break;
                case EAST:
                    if (mySon) {
                        ms.translate(-0.8F, 1.6F, -0.2F);
                    } else if (block) {
                        ms.translate(-0.2F, 0.8F, -0.4F);
                    } else {
                        ms.mulPose(Vector3f.YP.rotationDegrees(-90F));
                    }
                    ms.translate(-0.8F, -1.0F, 0.48F);
                    break;
                case WEST:
                    if (mySon) {
                        ms.translate(2.5F, 1.6F, 1.5F);
                    } else if (block) {
                        ms.translate(1.92F, 0.8F, 1.3F);
                    } else {
                        ms.mulPose(Vector3f.YP.rotationDegrees(-90F));
                    }
                    ms.translate(-0.8F, -1.0F, -1.34F);
                    break;
            }

            if (mySon) {
                ms.scale(1.1F, 1.1F, 1.1F);
            } else if (block) {
                ms.scale(0.5F, 0.5F, 0.5F);
            }
            if (block && side == Direction.NORTH) {
                ms.mulPose(Vector3f.YP.rotationDegrees(180F));
            }
            renderItem(ms, buffers, light, overlay, stack);
            ms.popPose();
        }
        ms.popPose();

        ms.pushPose();
        if (!name.isEmpty()) {
            ContributorList.firstStart();

            float scale = 1F / 4F;
            ms.translate(0F, 1F, 0F);
            ms.scale(scale, scale, scale);
            switch (name) {
                case "phi":
                case "vazkii":
                    ms.pushPose();
                    ms.translate(-0.15, 0.1, 0.4);
                    ms.mulPose(Vector3f.YP.rotationDegrees(90F));
                    ms.mulPose(new Vector3f(1, 0, 1).rotationDegrees(20));
                    renderModel(ms, buffers, light, overlay, MiscellaneousModels.INSTANCE.phiFlowerModel);
                    ms.popPose();

                    if (name.equals("vazkii")) {
                        ms.scale(1.25F, 1.25F, 1.25F);
                        ms.mulPose(Vector3f.XP.rotationDegrees(180F));
                        ms.mulPose(Vector3f.YP.rotationDegrees(-90F));
                        ms.translate(0.2, -1.25, 0);
                        renderModel(ms, buffers, light, overlay, MiscellaneousModels.INSTANCE.nerfBatModel);
                    }
                    break;
                case "haighyorkie":
                    ms.scale(1.25F, 1.25F, 1.25F);
                    ms.mulPose(Vector3f.ZP.rotationDegrees(180F));
                    ms.mulPose(Vector3f.YP.rotationDegrees(-90F));
                    ms.translate(-0.5F, -1.2F, -0.075F);
                    renderModel(ms, buffers, light, overlay, MiscellaneousModels.INSTANCE.goldfishModel);
                    break;
                case "martysgames":
                case "marty":
                    ms.scale(0.7F, 0.7F, 0.7F);
                    ms.mulPose(Vector3f.ZP.rotationDegrees(180F));
                    ms.translate(-0.3F, -2.7F, -1.2F);
                    ms.mulPose(Vector3f.ZP.rotationDegrees(15F));
                    renderItem(ms, buffers, light, overlay,
                            new ItemStack(ModItems.infiniteFruit, 1)
                                    .setHoverName(new TextComponent("das boot")));
                    break;
                case "jibril":
                    ms.scale(1.5F, 1.5F, 1.5F);
                    ms.translate(0F, 0.8F, 0F);
                    ItemFlightTiara.ClientLogic.renderHalo(null, null, ms, buffers, partialTicks);
                    break;
                case "kingdaddydmac":
                    ms.scale(0.5F, 0.5F, 0.5F);
                    ms.mulPose(Vector3f.ZP.rotationDegrees(180));
                    ms.mulPose(Vector3f.YP.rotationDegrees(90));
                    ms.pushPose();
                    ms.translate(0F, -2.5F, 0.65F);
                    ItemStack ring = new ItemStack(ModItems.manaRing);
                    renderItem(ms, buffers, light, overlay, ring);
                    ms.translate(0F, 0F, -4F);
                    renderItem(ms, buffers, light, overlay, ring);
                    ms.popPose();

                    ms.translate(1.5, -4, -2.5);
                    renderBlock(ms, buffers, light, overlay, Blocks.CAKE);
                    break;
                default:
                    ItemStack icon = ContributorList.getFlower(name);
                    if (icon != null && !icon.isEmpty()) {
                        ms.mulPose(Vector3f.XP.rotationDegrees(180));
                        ms.mulPose(Vector3f.YP.rotationDegrees(180));
                        ms.translate(0, -0.75, -0.5);
                        Minecraft.getInstance().getItemRenderer().renderStatic(
                                icon, ItemTransforms.TransformType.HEAD,
                                light, overlay, ms, buffers, 0);
                    }
                    break;
            }
        }
        ms.popPose();
    }

    private void renderModel(PoseStack ms, MultiBufferSource buffers, int light, int overlay, BakedModel model) {
        renderModel(ms, buffers.getBuffer(Sheets.translucentCullBlockSheet()),
                light, overlay, model);
    }

    private void renderModel(PoseStack ms, VertexConsumer buffer, int light, int overlay, BakedModel model) {
        this.blockRenderDispatcher.getModelRenderer()
                .renderModel(ms.last(), buffer, null, model, 1, 1, 1, light, overlay);
    }

    private void renderItem(PoseStack ms, MultiBufferSource buffers, int light, int overlay, ItemStack stack) {
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, ItemTransforms.TransformType.HEAD,
                        light, overlay, ms, buffers, 0);
    }

    private void renderBlock(PoseStack ms, MultiBufferSource buffers, int light, int overlay, Block block) {
        this.blockRenderDispatcher.renderSingleBlock(
                block.defaultBlockState(), ms, buffers, light, overlay);
    }
}