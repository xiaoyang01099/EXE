package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FXSparkle extends TextureSheetParticle {
    public boolean leyLineEffect;
    public int multiplier;
    public boolean shrink;
    public int particle;
    public boolean tinkle;
    public int blendmode;
    public boolean slowdown;
    public int currentColor;

    public FXSparkle(ClientLevel level, double x, double y, double z, float size, float r, float g, float b, int m) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.leyLineEffect = false;
        this.multiplier = 2;
        this.shrink = true;
        this.particle = 16;
        this.tinkle = false;
        this.blendmode = 1;
        this.slowdown = true;
        this.currentColor = 0;

        if (r == 0.0F) {
            r = 1.0F;
        }

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.gravity = 0.0F;
        this.xd = this.yd = this.zd = 0.0D;
        this.quadSize *= size;
        this.lifetime = 3 * m;
        this.multiplier = m;
        this.hasPhysics = false;
        this.setSize(0.01F, 0.01F);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public FXSparkle(ClientLevel level, double x, double y, double z, float size, int type, int m) {
        this(level, x, y, z, size, 0.0F, 0.0F, 0.0F, m);
        this.currentColor = type;
        switch (type) {
            case 0:
                this.rCol = 0.75F + level.random.nextFloat() * 0.25F;
                this.gCol = 0.25F + level.random.nextFloat() * 0.25F;
                this.bCol = 0.75F + level.random.nextFloat() * 0.25F;
                break;
            case 1:
                this.rCol = 0.5F + level.random.nextFloat() * 0.3F;
                this.gCol = 0.5F + level.random.nextFloat() * 0.3F;
                this.bCol = 0.2F;
                break;
            case 2:
                this.rCol = 0.2F;
                this.gCol = 0.2F;
                this.bCol = 0.7F + level.random.nextFloat() * 0.3F;
                break;
            case 3:
                this.rCol = 0.2F;
                this.gCol = 0.7F + level.random.nextFloat() * 0.3F;
                this.bCol = 0.2F;
                break;
            case 4:
                this.rCol = 0.7F + level.random.nextFloat() * 0.3F;
                this.gCol = 0.2F;
                this.bCol = 0.2F;
                break;
            case 5:
                this.blendmode = 771;
                this.rCol = level.random.nextFloat() * 0.1F;
                this.gCol = level.random.nextFloat() * 0.1F;
                this.bCol = level.random.nextFloat() * 0.1F;
                break;
            case 6:
                this.rCol = 0.8F + level.random.nextFloat() * 0.2F;
                this.gCol = 0.8F + level.random.nextFloat() * 0.2F;
                this.bCol = 0.8F + level.random.nextFloat() * 0.2F;
                break;
            case 7:
                this.rCol = 0.2F;
                this.gCol = 0.5F + level.random.nextFloat() * 0.3F;
                this.bCol = 0.6F + level.random.nextFloat() * 0.3F;
        }
    }

    public FXSparkle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, float size, int type, int m) {
        this(level, x, y, z, size, type, m);
        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double dz = targetZ - this.z;
        this.xd = dx / (double)this.lifetime;
        this.yd = dy / (double)this.lifetime;
        this.zd = dz / (double)this.lifetime;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternion quaternion = renderInfo.rotation();

        int part = this.particle + this.age / this.multiplier;
        float u0 = (float)(part % 4) / 16.0F;
        float u1 = u0 + 0.0624375F;
        float v0 = 0.25F;
        float v1 = v0 + 0.0624375F;
        float size = 0.1F * this.quadSize;

        if (this.shrink) {
            size *= (float)(this.lifetime - this.age + 1) / (float)this.lifetime;
        }

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = vertices[i];
            vector3f.transform(quaternion);
            vector3f.mul(size);
            vector3f.add(f, f1, f2);
        }

        float minU = u0;
        float maxU = u1;
        float minV = v0;
        float maxV = v1;
        int j = this.getLightColor(partialTicks);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.blendmode == 1 ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age == 0 && this.tinkle && this.level.random.nextInt(10) == 0) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT,
                    0.02F, 0.7F * ((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.6F + 2.0F), false);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.yd -= 0.04D * (double)this.gravity;

        if (this.hasPhysics) {
            this.pushOutOfBlocks(this.x, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.z);
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.slowdown) {
            this.xd *= 0.908D;
            this.yd *= 0.908D;
            this.zd *= 0.908D;
            if (this.onGround) {
                this.xd *= 0.7D;
                this.zd *= 0.7D;
            }
        }

        if (this.leyLineEffect) {
            FXSparkle fx = new FXSparkle(this.level,
                    this.xo + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.1F),
                    this.yo + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.1F),
                    this.zo + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.1F),
                    1.0F, this.currentColor, 3 + this.level.random.nextInt(3));
            fx.hasPhysics = false;
            Minecraft.getInstance().particleEngine.add(fx);
        }
    }

    public void setGravity(float value) {
        this.gravity = value;
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        double d0 = x - (double)blockpos.getX();
        double d1 = y - (double)blockpos.getY();
        double d2 = z - (double)blockpos.getZ();

        if (!this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos)) {
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

            if (b0 == 0) {
                this.xd = (double)(-f);
                this.yd = this.zd = (double)f1;
            }

            if (b0 == 1) {
                this.xd = (double)f;
                this.yd = this.zd = (double)f1;
            }

            if (b0 == 2) {
                this.yd = (double)(-f);
                this.xd = this.zd = (double)f1;
            }

            if (b0 == 3) {
                this.yd = (double)f;
                this.xd = this.zd = (double)f1;
            }

            if (b0 == 4) {
                this.zd = (double)(-f);
                this.yd = this.xd = (double)f1;
            }

            if (b0 == 5) {
                this.zd = (double)f;
                this.yd = this.xd = (double)f1;
            }

            return true;
        } else {
            return false;
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