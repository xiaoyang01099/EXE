package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.model.DragonWingsModel;

public class DragonWingsLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    private final DragonWingsModel dragonWingsModel;

    public DragonWingsLayer(RenderLayerParent<T, M> renderLayerParent, ModelPart modelPart) {
        super(renderLayerParent);
        this.dragonWingsModel = new DragonWingsModel(modelPart, 1.0F);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       Player player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw,
                       float headPitch) {

        if (checkDragonWing(player)) {
            ResourceLocation texture = new ResourceLocation("ex_enigmaticlegacy",
                    "textures/models/armor/dragonwings_layer.png");

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

            // 设置当前实体
            dragonWingsModel.setEntity(player);

            // 复制玩家模型的姿势到翅膀模型
            dragonWingsModel.prepareMobModel(player, limbSwing, limbSwingAmount, partialTick);
            dragonWingsModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            // 渲染翅膀
            dragonWingsModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
                    OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private boolean checkDragonWing(Player player) {
        if (player == null) {
            return false;
        }

        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestItem.isEmpty()) {
            CompoundTag tag = chestItem.getTag();
            if (tag != null && tag.contains("ex_enigmaticlegacy.dragonWings")) {
                CompoundTag wingsTag = tag.getCompound("ex_enigmaticlegacy.dragonWings");
                return wingsTag.contains("enabled") && wingsTag.getBoolean("enabled");
            }
        }

        return false;
    }
}