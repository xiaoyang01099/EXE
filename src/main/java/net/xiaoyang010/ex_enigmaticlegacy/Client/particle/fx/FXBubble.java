package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FXBubble extends TextureSheetParticle {
    public int particle = 16;
    public double bubblespeed = 0.002;

    public FXBubble(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int age) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.rCol = 1.0F;
        this.gCol = 0.0F;
        this.bCol = 0.5F;

        this.setSize(0.02F, 0.02F);

        this.hasPhysics = false;

        this.quadSize *= this.random.nextFloat() * 0.3F + 0.2F;

        this.xd = xSpeed * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        this.yd = ySpeed * 0.2F + this.random.nextFloat() * 0.02F;
        this.zd = zSpeed * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;

        this.lifetime = (int)((age + 2) + 8.0F / (this.random.nextFloat() * 0.8F + 0.2F));

        LivingEntity renderEntity = Minecraft.getInstance().player;
        int visibleDistance = 50;
        if (Minecraft.getInstance().options.graphicsMode == GraphicsStatus.FAST) {
            visibleDistance = 25;
        }

        if (renderEntity != null) {
            double distance = renderEntity.distanceToSqr(this.x, this.y, this.z);
            if (distance > visibleDistance * visibleDistance) {
                this.lifetime = 0;
            }
        }
    }

    public void setFroth() {
        this.quadSize *= 0.75F;
        this.lifetime = 4 + this.random.nextInt(3);
        this.bubblespeed = -0.001;
        this.xd /= 5.0F;
        this.yd /= 10.0F;
        this.zd /= 5.0F;
    }

    public void setFroth2() {
        this.quadSize *= 0.75F;
        this.lifetime = 12 + this.random.nextInt(12);
        this.bubblespeed = -0.005;
        this.xd /= 5.0F;
        this.yd /= 10.0F;
        this.zd /= 5.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.yd += this.bubblespeed;
        if (this.bubblespeed > 0.0F) {
            this.xd += (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01F;
            this.zd += (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01F;
        }

        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        this.xd *= 0.85F;
        this.yd *= 0.85F;
        this.zd *= 0.85F;

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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());

        float var8 = (float)(this.particle % 16) / 16.0F;      // U坐标起始
        float var9 = var8 + 0.0624375F;                        // U坐标结束
        float var10 = (float)(this.particle / 16) / 16.0F;     // V坐标起始
        float var11 = var10 + 0.0624375F;                      // V坐标结束

        float var12 = 0.1F * this.quadSize;

        int light = this.getLightColor(partialTicks);

        Quaternion quaternion = renderInfo.rotation();
        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(var12);
            vector3f.add(f, f1, f2);
        }

        float var16 = 1.0F;

        buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z())
                .uv(var9, var11)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z())
                .uv(var9, var10)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z())
                .uv(var8, var10)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z())
                .uv(var8, var11)
                .color(this.rCol * var16, this.gCol * var16, this.bCol * var16, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }
}