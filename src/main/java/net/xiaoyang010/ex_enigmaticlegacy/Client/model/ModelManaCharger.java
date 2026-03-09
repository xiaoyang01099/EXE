package net.xiaoyang010.ex_enigmaticlegacy.Client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render.RenderTileManaCharger;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaChargerTile;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.proxy.IProxy;

import java.util.Objects;


public class ModelManaCharger extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("ex_enigmaticlegacy", "mana_charger"), "main");

    private final ModelPart chargerBase;
    private final ModelPart chargerPlate;

    public ModelManaCharger(ModelPart root) {
        super(RenderType::entitySolid);
        this.chargerBase = root.getChild("charger_base");
        this.chargerPlate = root.getChild("charger_plate");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition chargerBase = partdefinition.addOrReplaceChild("charger_base", CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-2.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(0, 0).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 2.0F, 7.0F)
                        .texOffs(20, 9).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition chargerPlate = partdefinition.addOrReplaceChild("charger_plate", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(5.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        chargerBase.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        chargerPlate.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void render(RenderTileManaCharger render, PoseStack matrixStack, double time) {
        ManaChargerTile tile = render.charger;
        float offset = (float) Math.sin(time / 40.0F) * 0.1F + 0.05F;
        float polerot = -(float) time / 16.0F * 25.0F;

        matrixStack.pushPose();
        matrixStack.translate(0.0F, offset, 0.0F);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(polerot));

        this.chargerBase.render(matrixStack, render.getBuffer(), render.getPackedLight(), render.getPackedOverlay());

        if (tile.getStackInSlot(0) != null) {
            float rot = chargerPlate.yRot * 180.0F / (float) Math.PI;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(rot));
            matrixStack.translate(0F, 0.8125F, 0F);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            render.renderItemStack(tile.getStackInSlot(0), matrixStack);
        }

        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.translate(0.0F, offset / 1.3F + 0.185F, 0.0F);
        matrixStack.scale(0.85F, 0.85F, 0.85F);

        for (int i = 1; i < 5; ++i) {
            switch (i) {
                case 1 -> chargerPlate.yRot = -(float) Math.PI / 2;
                case 2 -> chargerPlate.yRot = (float) Math.PI / 2;
                case 3 -> chargerPlate.yRot = (float) Math.PI;
                case 4 -> chargerPlate.yRot = 0.0F;
            }

            if (tile.getLevel() != null) {
                time += i * 36.0F;
            }

            float offset1 = (float) Math.sin(time / 15.0F) * 0.1F - 0.1F;
            if (time == -1.0F) {
                offset1 = 0.0F;
            }

            ItemStack stack = tile.getStackInSlot(i);
            matrixStack.translate(0.0F, -offset1, 0.0F);

            if (stack != null) {
                matrixStack.pushPose();
                float manaPercent = ManaChargerTile.getManaPercent(stack);
                float rot = chargerPlate.yRot * 180.0F / (float) Math.PI;

                if (manaPercent < 100.0F) {
                    float chargeY = (offset1 + offset1 / 2.4F) * ((100.0F - manaPercent) / 150.0F);
                    matrixStack.translate(0.0F, chargeY, 0.0F);

                    if (tile.clientTick[i] > 12) {
                        float posX = 0.0F;
                        float posZ = 0.0F;
                        switch (i) {
                            case 1 -> {
                                posX = 0.0F;
                                posZ = -0.375F;
                            }
                            case 2 -> {
                                posX = 0.0F;
                                posZ = 0.375F;
                            }
                            case 3 -> {
                                posX = -0.375F;
                                posZ = 0.0F;
                            }
                            case 4 -> {
                                posX = 0.375F;
                                posZ = 0.0F;
                            }
                        }

                        Vec3 itemVec = new Vec3(
                                render.charger.getBlockPos().getX() + 0.5 + posX + (Math.random() / 8.0 - 0.0625),
                                render.charger.getBlockPos().getY() + 0.67 + offset1,
                                render.charger.getBlockPos().getZ() + 0.5 + posZ + (Math.random() / 8.0 - 0.0625)
                        );

                        Vec3 tileVec = new Vec3(
                                render.charger.getBlockPos().getX() + 0.5 + posX,
                                render.charger.getBlockPos().getY() + 0.7425 + offset1 - chargeY / 2.0F,
                                render.charger.getBlockPos().getZ() + 0.5 + posZ
                        );

                        if (Objects.requireNonNull(render.charger.getLevel()).isClientSide) {
                            IProxy.INSTANCE.lightningFX(
                                    itemVec,
                                    tileVec,
                                    10.0F,
                                    render.charger.getLevel().random.nextLong(),
                                    0x44DDFF,
                                    0x5599FF
                            );

                            int segments = 4;
                            for (int j = 0; j < segments; j++) {
                                double t = j / (double) segments;
                                Vec3 pos = itemVec.lerp(tileVec, t);
                                WispParticleData data = WispParticleData.wisp(
                                        0.1F + (float) Math.random() * 0.05F,
                                        0.2F + (float) Math.random() * 0.2F,
                                        0.2F + (float) Math.random() * 0.2F,
                                        0.6F,
                                        1
                                );
                                render.charger.getLevel().addParticle(data,
                                        pos.x + (Math.random() - 0.5) * 0.05,
                                        pos.y + (Math.random() - 0.5) * 0.05,
                                        pos.z + (Math.random() - 0.5) * 0.05,
                                        0, 0, 0);
                            }

                            render.charger.clientTick[i] = 0;
                        }
                    }
                }

                matrixStack.mulPose(Vector3f.YP.rotationDegrees(8.0F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(rot));
                matrixStack.translate(0.45F, 1.06F, 0.1245F);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                render.renderItemStack(stack, matrixStack);
                matrixStack.popPose();
            }

            this.chargerPlate.render(matrixStack, render.getBuffer(), render.getPackedLight(), render.getPackedOverlay());
            matrixStack.translate(0.0F, offset1, 0.0F);
        }

        matrixStack.popPose();
    }
}