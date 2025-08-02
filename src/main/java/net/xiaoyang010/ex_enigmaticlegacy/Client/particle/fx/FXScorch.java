package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FXScorch extends TextureSheetParticle {
    public boolean pvp = true;
    public boolean mobs = true;
    public boolean animals = true;
    private double px;
    private double py;
    private double pz;
    private float transferParticleScale;
    Entity partDestEnt;
    public boolean lance = false;
    private int textureIndex = 151;

    public FXScorch(ClientLevel level, double x, double y, double z, Vec3 direction, float spread, boolean lance) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.x = x;
        this.y = y;
        this.z = z;
        this.lance = lance;

        this.px = x + direction.x * 100.0D;
        this.py = y + direction.y * 100.0D;
        this.pz = z + direction.z * 100.0D;

        if (!lance) {
            this.px += (double)((this.random.nextFloat() - this.random.nextFloat()) * spread);
            this.py += (double)((this.random.nextFloat() - this.random.nextFloat()) * spread);
            this.pz += (double)((this.random.nextFloat() - this.random.nextFloat()) * spread);
        } else {
            this.px += (double)(this.random.nextFloat() - this.random.nextFloat()) * 0.5D;
            this.py += (double)(this.random.nextFloat() - this.random.nextFloat()) * 0.5D;
            this.pz += (double)(this.random.nextFloat() - this.random.nextFloat()) * 0.5D;
        }

        this.transferParticleScale = this.quadSize = this.random.nextFloat() * 0.5F + 2.0F;
        if (!lance) {
            this.transferParticleScale = this.quadSize = this.random.nextFloat() + 3.0F;
        }

        this.lifetime = 50;
        this.setSize(0.1F, 0.1F);
        this.textureIndex = 151;
        this.hasPhysics = false;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.alpha = 0.66F;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 210;
    }

    public float getQuadSize(float partialTick) {
        return 1.0F;
    }

    @Override
    public void tick() {
        double dx = this.px - this.x;
        double dy = this.py - this.y;
        double dz = this.pz - this.z;
        double distance = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));

        this.xd = dx / (distance * 1.25D);
        this.yd = dy / (distance * 1.25D);
        this.zd = dz / (distance * 1.25D);

        this.xd *= (double)((float)(this.lifetime - this.age) / (float)this.lifetime);
        this.yd *= (double)((float)(this.lifetime - this.age) / (float)this.lifetime);
        this.zd *= (double)((float)(this.lifetime - this.age) / (float)this.lifetime);

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd += (double)(this.random.nextFloat() * 0.07F - 0.035F);
        this.yd += (double)(this.random.nextFloat() * 0.07F - 0.035F);
        this.zd += (double)(this.random.nextFloat() * 0.07F - 0.035F);

        int blockX = Mth.floor(this.x);
        int blockY = Mth.floor(this.y);
        int blockZ = Mth.floor(this.z);
        BlockPos pos = new BlockPos(blockX, blockY, blockZ);

        if (this.age > 1 && this.level.getBlockState(pos).isSolidRender(this.level, pos)) {
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            this.age += 10;
        }

        this.pushOutOfBlocks(this.x, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.z);
        this.move(this.xd, this.yd, this.zd);

        ++this.age;
        if (this.age >= this.lifetime) {
            this.remove();
        }

        float fs = (float)this.age / (float)(this.lifetime - 9);
        if (fs <= 1.0F) {
            this.textureIndex = (int)(151.0F + fs * 6.0F);
        } else {
            this.textureIndex = 159 - (this.lifetime - this.age) / 3;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float fs = (float)this.age / (float)this.lifetime;
        this.quadSize = this.transferParticleScale * (fs + 0.25F) * 2.0F;

        float fc = (float)this.age * 9.0F / (float)this.lifetime;
        if (fc > 1.0F) {
            fc = 1.0F;
        }

        this.rCol = this.gCol = fc;
        this.bCol = 1.0F;

        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(this.xo + (this.x - this.xo) * partialTicks - vec3.x());
        float f1 = (float)(this.yo + (this.y - this.yo) * partialTicks - vec3.y());
        float f2 = (float)(this.zo + (this.z - this.zo) * partialTicks - vec3.z());

        float u0 = (float)(this.textureIndex % 16) / 16.0F;
        float u1 = u0 + 0.0625F;
        float v0 = (float)(this.textureIndex / 16) / 16.0F;
        float v1 = v0 + 0.0625F;

        float size = this.quadSize * 0.1F;

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
                .uv(u1, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    private void pushOutOfBlocks(double x, double y, double z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        double d0 = x - (double)blockpos.getX();
        double d1 = y - (double)blockpos.getY();
        double d2 = z - (double)blockpos.getZ();

        if (this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos)) {
            boolean flag = !this.isBlocking(blockpos.west());
            boolean flag1 = !this.isBlocking(blockpos.east());
            boolean flag2 = !this.isBlocking(blockpos.below());
            boolean flag3 = !this.isBlocking(blockpos.above());
            boolean flag4 = !this.isBlocking(blockpos.north());
            boolean flag5 = !this.isBlocking(blockpos.south());

            byte b0 = -1;
            double d3 = 9999.0D;

            if (flag && d0 < d3) {
                d3 = d0;
                b0 = 0;
            }

            if (flag1 && 1.0D - d0 < d3) {
                d3 = 1.0D - d0;
                b0 = 1;
            }

            if (flag2 && d1 < d3) {
                d3 = d1;
                b0 = 2;
            }

            if (flag3 && 1.0D - d1 < d3) {
                d3 = 1.0D - d1;
                b0 = 3;
            }

            if (flag4 && d2 < d3) {
                d3 = d2;
                b0 = 4;
            }

            if (flag5 && 1.0D - d2 < d3) {
                d3 = 1.0D - d2;
                b0 = 5;
            }

            float f = this.random.nextFloat() * 0.05F + 0.025F;
            float f1 = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;

            switch (b0) {
                case 0:
                    this.xd = (double)(-f);
                    this.yd = this.zd = (double)f1;
                    break;
                case 1:
                    this.xd = (double)f;
                    this.yd = this.zd = (double)f1;
                    break;
                case 2:
                    this.yd = (double)(-f);
                    this.xd = this.zd = (double)f1;
                    break;
                case 3:
                    this.yd = (double)f;
                    this.xd = this.zd = (double)f1;
                    break;
                case 4:
                    this.zd = (double)(-f);
                    this.yd = this.xd = (double)f1;
                    break;
                case 5:
                    this.zd = (double)f;
                    this.yd = this.xd = (double)f1;
                    break;
            }
        }
    }

    private boolean isBlocking(BlockPos pos) {
        BlockState blockstate = this.level.getBlockState(pos);
        return blockstate.isSolidRender(this.level, pos);
    }

    public void setSize(float width, float height) {
        if (width != this.bbWidth || height != this.bbHeight) {
            this.bbWidth = width;
            this.bbHeight = height;
            AABB aabb = this.getBoundingBox();
            double d0 = (aabb.minX + aabb.maxX - (double)width) / 2.0D;
            double d1 = (aabb.minZ + aabb.maxZ - (double)width) / 2.0D;
            this.setBoundingBox(new AABB(d0, aabb.minY, d1, d0 + (double)this.bbWidth, aabb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
        }
    }
}