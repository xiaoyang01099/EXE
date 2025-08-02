package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.DragonWingsModel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.DragonWings;

public class DragonWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ExEnigmaticlegacyMod.MODID,"textures/models/armor/dragon.png");
    private final DragonWingsModel model;

    public DragonWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                            EntityModelSet modelSet) {
        super(renderer);
        model = new DragonWingsModel(modelSet.bakeLayer(
                new ModelLayerLocation(new ResourceLocation("ex_enigmaticlegacy", "dragonwings_layer"), "main")));
    }

    private boolean isWearingFullDragonSet(AbstractClientPlayer player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModArmors.dragonArmorBoots.get() &&
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModArmors.dragonArmorLegs.get() &&
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModArmors.dragonArmorChest.get() &&
                player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModArmors.dragonArmorHelm.get();
    }

    private boolean shouldRenderWings(AbstractClientPlayer player) {
        Item chestItem = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        return chestItem instanceof DragonWings || isWearingFullDragonSet(player);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (shouldRenderWings(player)) {
            poseStack.pushPose();
            poseStack.translate(.0D, .05D, .35D);
            poseStack.scale(.2F, .2F, .2F);
            poseStack.mulPose(new Quaternion(-Mth.PI / 4, 0, 0, 1.f));

            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.popPose();
        }
    }
}