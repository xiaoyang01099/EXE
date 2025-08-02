package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.RainbowTableModel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.RainbowTableTile;

public class RainbowTableRenderer implements BlockEntityRenderer<RainbowTableTile> {
    private final RainbowTableModel model;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];
    private static final int[] FRAME_SEQUENCE = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0
    };
    private static final int FRAME_COUNT = FRAME_SEQUENCE.length;
    private static final float FRAME_DURATION = 2.0f;

    static {
        for (int i = 0; i < 10; i++) {
            TEXTURES[i] = new ResourceLocation(ExEnigmaticlegacyMod.MODID,
                    "textures/entity/rainbow_table/" + i + ".png");
        }
    }

    public RainbowTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new RainbowTableModel(context.bakeLayer(RainbowTableModel.LAYER_LOCATION));
    }

    @Override
    public void render(RainbowTableTile blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

        int totalTicks = (int) blockEntity.getLevel().getGameTime();
        float frameTime = (totalTicks + partialTick) / FRAME_DURATION;
        int currentIndex = Mth.floor(frameTime) % FRAME_COUNT;
        int nextIndex = (currentIndex + 1) % FRAME_COUNT;

        int currentFrame = FRAME_SEQUENCE[currentIndex];
        int nextFrame = FRAME_SEQUENCE[nextIndex];

        float blend = (frameTime - Mth.floor(frameTime));

        ResourceLocation currentTexture = TEXTURES[currentFrame];
        ResourceLocation nextTexture = TEXTURES[nextFrame];

        var vertexConsumer = buffer.getBuffer(model.renderType(currentTexture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F * (1 - blend));

        vertexConsumer = buffer.getBuffer(model.renderType(nextTexture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F * blend);

        poseStack.popPose();
    }
}