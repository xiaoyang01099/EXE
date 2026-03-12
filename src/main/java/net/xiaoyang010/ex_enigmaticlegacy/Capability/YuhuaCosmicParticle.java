package net.xiaoyang010.ex_enigmaticlegacy.Capability;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;

@OnlyIn(Dist.CLIENT)
public class YuhuaCosmicParticle extends Particle {
    private float rotX, rotY, rotZ;
    private final float rotSpeedX, rotSpeedY, rotSpeedZ;
    private final float cubeSize;

    protected YuhuaCosmicParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.lifetime = 60 + this.random.nextInt(40);
        this.gravity  = -0.02F;
        this.friction = 0.98F;

        this.xd = xSpeed + (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = ySpeed + 0.05 + this.random.nextDouble() * 0.03;
        this.zd = zSpeed + (this.random.nextDouble() - 0.5) * 0.02;

        this.cubeSize = 0.15F + this.random.nextFloat() * 0.3F;
        this.alpha    = 0.5F;

        this.rotSpeedX = this.random.nextFloat() - 0.5F;
        this.rotSpeedY = this.random.nextFloat() - 0.5F;
        this.rotSpeedZ = this.random.nextFloat() - 0.5F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float lifeProgress = (float) this.age / (float) this.lifetime;
        if (lifeProgress > 0.7F) {
            float fadeProgress = (lifeProgress - 0.7F) / 0.3F;
            this.alpha = 0.8F * (1.0F - fadeProgress);
        }

        this.rotX = this.rotSpeedX;
        this.rotY = this.rotSpeedY;
        this.rotZ = this.rotSpeedZ;

        this.xd *= (double) this.friction;
        this.yd += (double) this.gravity;
        this.yd *= (double) this.friction;
        this.zd *= (double) this.friction;
        this.move(this.xd, this.yd, this.zd);

        if (this.alpha <= 0.001F) this.remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cam.y());
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cam.z());


        Quaternion rotation = Quaternion.ONE.copy();
        rotation.mul(Vector3f.XP.rotation(this.rotX));
        rotation.mul(Vector3f.YP.rotation(this.rotY));
        rotation.mul(Vector3f.ZP.rotation(this.rotZ));

        renderCosmicCube(buffer, px, py, pz, this.cubeSize, rotation);
    }

    private void renderCosmicCube(VertexConsumer buffer,
                                  float x, float y, float z,
                                  float size, Quaternion rotation) {
        float h = size / 2.0F;

        Vector3f[] v = {
                new Vector3f(-h, -h, -h),
                new Vector3f( h, -h, -h),
                new Vector3f( h,  h, -h),
                new Vector3f(-h,  h, -h),
                new Vector3f(-h, -h,  h),
                new Vector3f( h, -h,  h),
                new Vector3f( h,  h,  h),
                new Vector3f(-h,  h,  h)
        };

        for (Vector3f vert : v) {
            vert.transform(rotation);
            vert.add(x, y, z);
        }

        int light = 15728880;

        addFace(buffer, v[4], v[5], v[6], v[7],  0,  0,  1, light); // 前 +Z
        addFace(buffer, v[1], v[0], v[3], v[2],  0,  0, -1, light); // 后 -Z
        addFace(buffer, v[5], v[1], v[2], v[6],  1,  0,  0, light); // 右 +X
        addFace(buffer, v[0], v[4], v[7], v[3], -1,  0,  0, light); // 左 -X
        addFace(buffer, v[7], v[6], v[2], v[3],  0,  1,  0, light); // 上 +Y
        addFace(buffer, v[0], v[1], v[5], v[4],  0, -1,  0, light); // 下 -Y
    }

    private void addFace(VertexConsumer buffer,
                         Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                         float nx, float ny, float nz,
                         int light) {
        putVertex(buffer, a, 0.0f, 0.0f, nx, ny, nz, light);
        putVertex(buffer, b, 1.0f, 0.0f, nx, ny, nz, light);
        putVertex(buffer, c, 1.0f, 1.0f, nx, ny, nz, light);
        putVertex(buffer, d, 0.0f, 1.0f, nx, ny, nz, light);
    }

    private void putVertex(VertexConsumer buffer,
                           Vector3f pos,
                           float u, float v,
                           float nx, float ny, float nz,
                           int light) {
        buffer.vertex(pos.x(), pos.y(), pos.z())
                .color(1.0f, 1.0f, 1.0f, this.alpha)
                .uv(u, v)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }

    public static final ParticleRenderType COSMIC_CUBE_TYPE = new ParticleRenderType() {

        @Override
        public void begin(BufferBuilder builder, TextureManager texManager) {
            if (YuHuaShaders.yuhuaShader == null) return;
            if (AvaritiaShaders.COSMIC_SPRITES[0] == null) return;

            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderTexture(2,
                    Minecraft.getInstance().gameRenderer.lightTexture().lightTexture.getId());

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();

            RenderSystem.setShader(() -> YuHuaShaders.yuhuaShader);

            Minecraft mc = Minecraft.getInstance();
            long time = mc.level != null ? mc.level.getGameTime() : 0L;

            if (YuHuaShaders.yuhuaTime != null)
                YuHuaShaders.yuhuaTime.set((float)(time % Integer.MAX_VALUE));
            if (YuHuaShaders.yuhuaOpacity != null)
                YuHuaShaders.yuhuaOpacity.set(1.0f);
            if (YuHuaShaders.yuhuaExternalScale != null)
                YuHuaShaders.yuhuaExternalScale.set(1.0f);
            if (YuHuaShaders.yuhuaYaw != null)
                YuHuaShaders.yuhuaYaw.set(0.0f);
            if (YuHuaShaders.yuhuaPitch != null)
                YuHuaShaders.yuhuaPitch.set(0.0f);
            if (YuHuaShaders.yuhuaUVs != null)
                YuHuaShaders.yuhuaUVs.set(YuHuaShaders.COSMIC_UVS);

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableBlend();
        }

        @Override
        public String toString() {
            return "YUHUA_COSMIC_CUBE";
        }
    };

    @Override
    public ParticleRenderType getRenderType() {
        return COSMIC_CUBE_TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {}

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new YuhuaCosmicParticle(level, x, y, z, vx, vy, vz);
        }
    }
}