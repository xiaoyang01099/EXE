package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Util.RenderUtil;

public class EffectCut extends Effect {
    public float yaw = 0.0f;
    public float pitch = 0.0f;
    public float slashAngle = 0.0f;

    public EffectCut() {
        super();
    }

    public EffectCut(int id) {
        super(id);
    }

    public EffectCut setSlashProperties(float yaw, float pitch, float angle) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.slashAngle = angle;
        return this;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.slashAngle = tag.getFloat("slashAngle");
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = super.write();
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putFloat("slashAngle", this.slashAngle);
        return tag;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack poseStack, float pticks) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();

        double rx = this.getInterpX(pticks) - cam.x;
        double ry = this.getInterpY(pticks) - cam.y;
        double rz = this.getInterpZ(pticks) - cam.z;
        poseStack.translate(rx, ry, rz);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.yaw));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(this.pitch));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(-this.slashAngle));

        RenderSystem.setShader(GameRenderer::getPositionTexLightmapColorShader);
        RenderSystem.setShaderTexture(0, RenderUtil.beam_texture);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);

        Matrix4f matrix = poseStack.last().pose();
        float lifeCoeff = this.getLifeCoeff(pticks);
        float alpha = this.a * lifeCoeff;
        float beamWidth = 0.75f * lifeCoeff;

        RenderUtil.renderBeam(buffer, matrix,
                -5.0, 0.0, 0.0,
                0.0, 0.0, 0.0,
                this.r, this.g, this.b, 0.0f,
                this.r, this.g, this.b, alpha,
                beamWidth);

        RenderUtil.renderBeam(buffer, matrix,
                0.0, 0.0, 0.0,
                5.0, 0.0, 0.0,
                this.r, this.g, this.b, alpha,
                this.r, this.g, this.b, 0.0f,
                beamWidth);

        tess.end();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}
