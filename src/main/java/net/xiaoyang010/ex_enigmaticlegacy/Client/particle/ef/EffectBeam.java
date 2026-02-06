package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Util.RenderUtil;

public class EffectBeam extends Effect {
    public double x2 = 0.0;
    public double y2 = 0.0;
    public double z2 = 0.0;
    public float thickness = 0.0f;

    public EffectBeam() {
        super();
    }

    public EffectBeam(int id) {
        super(id);
    }

    public EffectBeam setTarget(double x2, double y2, double z2) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        return this;
    }

    public EffectBeam setThickness(float thick) {
        this.thickness = thick;
        return this;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        this.x2 = tag.getDouble("x2");
        this.y2 = tag.getDouble("y2");
        this.z2 = tag.getDouble("z2");
        this.thickness = tag.getFloat("thick");
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = super.write();
        tag.putDouble("x2", this.x2);
        tag.putDouble("y2", this.y2);
        tag.putDouble("z2", this.z2);
        tag.putFloat("thick", this.thickness);
        return tag;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack poseStack, float pticks) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();

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
        float alpha = this.a * this.getLifeCoeff(pticks);

        double rx1 = this.x - cam.x;
        double ry1 = this.y - cam.y;
        double rz1 = this.z - cam.z;
        double rx2 = this.x2 - cam.x;
        double ry2 = this.y2 - cam.y;
        double rz2 = this.z2 - cam.z;

        double midX1 = rx2 * 0.1 + rx1 * 0.9;
        double midY1 = ry2 * 0.1 + ry1 * 0.9;
        double midZ1 = rz2 * 0.1 + rz1 * 0.9;
        RenderUtil.renderBeam(buffer, matrix,
                rx1, ry1, rz1,
                midX1, midY1, midZ1,
                this.r, this.g, this.b, 0.0f,
                this.r, this.g, this.b, alpha,
                this.thickness, 30.0);

        double midX2 = rx2 * 0.9 + rx1 * 0.1;
        double midY2 = ry2 * 0.9 + ry1 * 0.1;
        double midZ2 = rz2 * 0.9 + rz1 * 0.1;
        RenderUtil.renderBeam(buffer, matrix,
                midX1, midY1, midZ1,
                midX2, midY2, midZ2,
                this.r, this.g, this.b, alpha,
                this.r, this.g, this.b, alpha,
                this.thickness, 30.0);

        RenderUtil.renderBeam(buffer, matrix,
                midX2, midY2, midZ2,
                rx2, ry2, rz2,
                this.r, this.g, this.b, alpha,
                this.r, this.g, this.b, 0.0f,
                this.thickness, 30.0);

        tess.end();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}

