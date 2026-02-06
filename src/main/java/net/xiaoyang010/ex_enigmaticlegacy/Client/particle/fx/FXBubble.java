package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FXBubble extends TextureSheetParticle {
    public int particle = 16;
    public double bubblespeed = 0.002;

    public FXBubble(ClientLevel world, double x, double y, double z,
                    double motionX, double motionY, double motionZ, int age) {
        super(world, x, y, z, motionX, motionY, motionZ);

        this.rCol = 1.0f;
        this.gCol = 0.0f;
        this.bCol = 0.5f;

        this.setSize(0.02f, 0.02f);

        this.hasPhysics = false;

        this.quadSize *= this.random.nextFloat() * 0.3f + 0.2f;

        this.xd = motionX * 0.2 + (this.random.nextFloat() * 2.0f - 1.0f) * 0.02f;
        this.yd = motionY * 0.2 + this.random.nextFloat() * 0.02f;
        this.zd = motionZ * 0.2 + (this.random.nextFloat() * 2.0f - 1.0f) * 0.02f;

        this.lifetime = (int)((double)(age + 2) + 8.0 / (Math.random() * 0.8 + 0.2));

        LivingEntity renderentity = Minecraft.getInstance().player;
        int visibleDistance = 50;
        if (Minecraft.getInstance().options.graphicsMode != GraphicsStatus.FANCY) {
            visibleDistance = 25;
        }
        if (renderentity.distanceToSqr(this.x, this.y, this.z) > (double)(visibleDistance * visibleDistance)) {
            this.lifetime = 0;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void setFroth() {
        this.quadSize *= 0.75f;
        this.lifetime = 4 + this.random.nextInt(3);
        this.bubblespeed = -0.001;
        this.xd /= 5.0;
        this.yd /= 10.0;
        this.zd /= 5.0;
    }

    public void setFroth2() {
        this.quadSize *= 0.75f;
        this.lifetime = 12 + this.random.nextInt(12);
        this.bubblespeed = -0.005;
        this.xd /= 5.0;
        this.yd /= 10.0;
        this.zd /= 5.0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.yd += this.bubblespeed;

        if (this.bubblespeed > 0.0) {
            this.xd += (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01f;
            this.zd += (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01f;
        }

        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else if (this.lifetime <= 2) {
            ++this.particle;
        }
    }

    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);

        float var8 = (float)(this.particle % 16) / 16.0f;
        float var9 = var8 + 0.0624375f;
        float var10 = (float)(this.particle / 16) / 16.0f;
        float var11 = var10 + 0.0624375f;
        float var12 = 0.1f * this.quadSize;

        Vec3 vec3 = renderInfo.getPosition();
        float var13 = (float)(this.xo + (this.x - this.xo) * (double)partialTicks - vec3.x());
        float var14 = (float)(this.yo + (this.y - this.yo) * (double)partialTicks - vec3.y());
        float var15 = (float)(this.zo + (this.z - this.zo) * (double)partialTicks - vec3.z());

        float var16 = 1.0f;

        float f1 = (float)(-Math.sin(Math.toRadians(renderInfo.getYRot())));
        float f2 = (float)(Math.cos(Math.toRadians(renderInfo.getYRot())));
        float f3 = (float)(-Math.sin(Math.toRadians(renderInfo.getXRot())));
        float f4 = (float)(Math.cos(Math.toRadians(renderInfo.getXRot())) * Math.sin(Math.toRadians(renderInfo.getYRot())));
        float f5 = (float)(Math.cos(Math.toRadians(renderInfo.getXRot())) * Math.cos(Math.toRadians(renderInfo.getYRot())));

        int brightness = 240;

        buffer.vertex(
                        (double)(var13 - f1 * var12 - f4 * var12),
                        (double)(var14 - f2 * var12),
                        (double)(var15 - f3 * var12 - f5 * var12)
                ).uv(var9, var11)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(brightness, brightness)
                .endVertex();

        buffer.vertex(
                        (double)(var13 - f1 * var12 + f4 * var12),
                        (double)(var14 + f2 * var12),
                        (double)(var15 - f3 * var12 + f5 * var12)
                ).uv(var9, var10)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(brightness, brightness)
                .endVertex();

        buffer.vertex(
                        (double)(var13 + f1 * var12 + f4 * var12),
                        (double)(var14 + f2 * var12),
                        (double)(var15 + f3 * var12 + f5 * var12)
                ).uv(var8, var10)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(brightness, brightness)
                .endVertex();

        buffer.vertex(
                        (double)(var13 + f1 * var12 - f4 * var12),
                        (double)(var14 - f2 * var12),
                        (double)(var15 + f3 * var12 - f5 * var12)
                ).uv(var8, var11)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(brightness, brightness)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
