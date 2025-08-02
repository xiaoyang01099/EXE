package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FXBurst extends TextureSheetParticle {
    public static final ResourceLocation nodetex = new ResourceLocation("ex_enigmaticlegacy", "textures/misc/nodes.png");

    public FXBurst(ClientLevel world, double x, double y, double z, float size) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.rCol = 0.0F;
        this.gCol = 0.8F + this.random.nextFloat() * 0.2F;
        this.bCol = 0.4F + this.random.nextFloat() * 0.6F;
        this.gravity = 0.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.quadSize *= size;
        this.lifetime = 31;
        this.hasPhysics = false;
        this.setSize(0.01F, 0.01F);
        this.alpha = 0.75F;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        UtilsFX.bindTexture(nodetex);

        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(this.xo + (this.x - this.xo) * partialTicks - vec3.x());
        float f1 = (float)(this.yo + (this.y - this.yo) * partialTicks - vec3.y());
        float f2 = (float)(this.zo + (this.z - this.zo) * partialTicks - vec3.z());

        float var8 = (float)(this.age % 32) / 32.0F;
        float var9 = var8 + 0.03125F;
        float var10 = 0.96875F;
        float var11 = 1.0F;

        float size = this.quadSize;

        Quaternion quaternion = renderInfo.rotation();
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for(int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.transform(quaternion);
            vertex.mul(size);
            vertex.add(f, f1, f2);
        }

        int light = this.getLightColor(partialTicks);

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(var9, var11)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(var9, var10)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(var8, var10)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(var8, var11)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    public void setGravity(float value) {
        this.gravity = value;
    }

    public boolean needsCustomTexture() {
        return true;
    }



    public int getRenderLayer() {
        return 1;
    }
}