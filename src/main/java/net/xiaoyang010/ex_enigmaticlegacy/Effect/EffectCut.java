package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EffectCut {
    /*private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/effect/beam.png");*/
    
    private float yaw = 0.0F;
    private float pitch = 0.0F;
    private float slashAngle = 0.0F;
    private float r, g, b, a;
    private double x, y, z;
    private double prevX, prevY, prevZ;
    private int lifetime;
    private int age;

    public EffectCut(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.lifetime = 20; // 默认持续时间
    }

    public EffectCut setSlashProperties(float yaw, float pitch, float angle) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.slashAngle = angle;
        return this;
    }

    public EffectCut setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public void read(CompoundTag tag) {
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.slashAngle = tag.getFloat("slashAngle");
        this.x = tag.getDouble("x");
        this.y = tag.getDouble("y");
        this.z = tag.getDouble("z");
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putFloat("slashAngle", this.slashAngle);
        tag.putDouble("x", this.x);
        tag.putDouble("y", this.y);
        tag.putDouble("z", this.z);
        return tag;
    }

    public void tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.age++;
    }

    private float getLifeCoeff(float partialTicks) {
        return Math.max(0.0F, 1.0F - (this.age + partialTicks) / this.lifetime);
    }

    private double getInterpX(float partialTicks) {
        return this.prevX + (this.x - this.prevX) * partialTicks;
    }

    private double getInterpY(float partialTicks) {
        return this.prevY + (this.y - this.prevY) * partialTicks;
    }

    private double getInterpZ(float partialTicks) {
        return this.prevZ + (this.z - this.prevZ) * partialTicks;
    }

    public void render(PoseStack poseStack, float partialTicks) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        poseStack.pushPose();
        poseStack.translate(getInterpX(partialTicks), getInterpY(partialTicks), getInterpZ(partialTicks));
        
        // 应用旋转
        poseStack.mulPose(new Quaternion(0, -this.yaw, 0, true));
        poseStack.mulPose(new Quaternion(this.pitch, 0, 0, true));
        poseStack.mulPose(new Quaternion(0, 0, -this.slashAngle, true));

        Matrix4f matrix = poseStack.last().pose();
        float alpha = this.a * getLifeCoeff(partialTicks);
        float width = 0.75F * getLifeCoeff(partialTicks);

        VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.lightning());

        // 渲染第一段光束
        builder.vertex(matrix, -5.0F, 0.0F, 0.0F)
                .color(this.r, this.g, this.b, 0.0F)
                .endVertex();
        builder.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(this.r, this.g, this.b, alpha)
                .endVertex();

        // 渲染第二段光束
        builder.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(this.r, this.g, this.b, alpha)
                .endVertex();
        builder.vertex(matrix, 5.0F, 0.0F, 0.0F)
                .color(this.r, this.g, this.b, 0.0F)
                .endVertex();

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        poseStack.popPose();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public boolean isAlive() {
        return this.age < this.lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }
}