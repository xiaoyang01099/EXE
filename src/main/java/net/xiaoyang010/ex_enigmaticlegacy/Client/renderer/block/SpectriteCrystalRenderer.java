package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpectriteCrystalEntity;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import org.jetbrains.annotations.NotNull;

public class SpectriteCrystalRenderer extends EntityRenderer<SpectriteCrystalEntity> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation(ExEnigmaticlegacyMod.MODID,"textures/entity/spectrite_crystal/0.png");
    private static final RenderType RENDER_TYPE;
    private static final float SIN_45;
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public SpectriteCrystalRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0.5F;
        ModelPart $$1 = pContext.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = $$1.getChild("glass");
        this.cube = $$1.getChild("cube");
        this.base = $$1.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition $$0 = new MeshDefinition();
        PartDefinition $$1 = $$0.getRoot();
        $$1.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        $$1.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        $$1.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create($$0, 64, 32);
    }

    public void render(SpectriteCrystalEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        int index = Math.min(35, pEntity.frame / 10);
        Minecraft.getInstance().getEntityRenderDispatcher().textureManager.bindForSetup(
                new ResourceLocation(ExEnigmaticlegacyMod.MODID,"textures/entity/spectrite_crystal/" + index)
        );
        float y = getY(pEntity, pPartialTicks);
        float $$7 = ((float)pEntity.time + pPartialTicks) * 3.0F;
        VertexConsumer $$8 = pBuffer.getBuffer(RENDER_TYPE);
        pMatrixStack.pushPose();
        pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        pMatrixStack.translate(0.0, -0.5, 0.0);
        int $$9 = OverlayTexture.NO_OVERLAY;
        if (pEntity.showsBottom()) {
            this.base.render(pMatrixStack, $$8, pPackedLight, $$9);
        }

        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees($$7));
        pMatrixStack.translate(0.0, (double)(1.5F + y / 2.0F), 0.0);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        this.glass.render(pMatrixStack, $$8, pPackedLight, $$9);
        float $$10 = 0.875F;
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees($$7));
        this.glass.render(pMatrixStack, $$8, pPackedLight, $$9);
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees($$7));
        this.cube.render(pMatrixStack, $$8, pPackedLight, $$9);
        pMatrixStack.popPose();
        pMatrixStack.popPose();
        BlockPos pos = pEntity.getBeamTarget();
        if (pos != null) {
            float $$12 = (float)pos.getX() + 0.5F;
            float $$13 = (float)pos.getY() + 0.5F;
            float $$14 = (float)pos.getZ() + 0.5F;
            float $$15 = (float)((double)$$12 - pEntity.getX());
            float $$16 = (float)((double)$$13 - pEntity.getY());
            float $$17 = (float)((double)$$14 - pEntity.getZ());
            pMatrixStack.translate((double)$$15, (double)$$16, (double)$$17);
            EnderDragonRenderer.renderCrystalBeams(-$$15, -$$16 + y, -$$17, pPartialTicks, pEntity.time, pMatrixStack, pBuffer, pPackedLight);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public static float getY(SpectriteCrystalEntity pEndCrystal, float pPartialTick) {
        float v = (float)pEndCrystal.time + pPartialTick;
        float $$3 = Mth.sin(v * 0.2F) / 2.0F + 0.5F;
        $$3 = ($$3 * $$3 + $$3) * 0.4F;
        return $$3 - 1.4F;
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation(SpectriteCrystalEntity pEntity) {
        int index = Math.min(35, pEntity.frame / 10);
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID,"textures/entity/spectrite_crystal/" + index);
    }

    public boolean shouldRender(SpectriteCrystalEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ) || pLivingEntity.getBeamTarget() != null;
    }

    static {
        RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
        SIN_45 = (float)Math.sin(0.7853981633974483);
    }
}
