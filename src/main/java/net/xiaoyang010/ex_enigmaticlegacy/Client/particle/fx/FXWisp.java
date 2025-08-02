package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;

public class FXWisp extends TextureSheetParticle {
    private Entity target;
    public boolean shrink = false;
    private float moteParticleScale;
    private int moteHalfLife;
    public boolean tinkle = false;
    public int blendmode = 1;

    public FXWisp(ClientLevel level, double x, double y, double z, float scale, float red, float green, float blue) {
        this(level, x, y, z, scale, red, green, blue, 0.0f);
    }

    public FXWisp(ClientLevel level, double x, double y, double z, float scale, float red, float green, float blue, float unused) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.target = null;
        this.shrink = false;
        this.tinkle = false;
        this.blendmode = 1;

        if (red == 0.0f) {
            red = 1.0f;
        }

        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.gravity = 0.0f;

        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        this.quadSize *= scale;
        this.moteParticleScale = this.quadSize;
        this.lifetime = (int)(36.0 / (Math.random() * 0.3 + 0.7));
        this.moteHalfLife = this.lifetime / 2;

        this.hasPhysics = false;
        this.setSize(0.1f, 0.1f);

        Entity renderEntity = Minecraft.getInstance().getCameraEntity();
        int visibleDistance = 50;
        if (Minecraft.getInstance().options.graphicsMode != GraphicsStatus.FANCY) {
            visibleDistance = 25;
        }
        if (renderEntity != null && renderEntity.distanceToSqr(this.x, this.y, this.z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public FXWisp(ClientLevel level, double x, double y, double z, float scale, int type) {
        this(level, x, y, z, scale, 0.0f, 0.0f, 0.0f, 0.0f);

        switch (type) {
            case 0: {
                this.rCol = 0.75f + level.random.nextFloat() * 0.25f;
                this.gCol = 0.25f + level.random.nextFloat() * 0.25f;
                this.bCol = 0.75f + level.random.nextFloat() * 0.25f;
                break;
            }
            case 1: {
                this.rCol = 0.5f + level.random.nextFloat() * 0.3f;
                this.gCol = 0.5f + level.random.nextFloat() * 0.3f;
                this.bCol = 0.2f;
                break;
            }
            case 2: {
                this.rCol = 0.2f;
                this.gCol = 0.2f;
                this.bCol = 0.7f + level.random.nextFloat() * 0.3f;
                break;
            }
            case 3: {
                this.rCol = 0.2f;
                this.gCol = 0.7f + level.random.nextFloat() * 0.3f;
                this.bCol = 0.2f;
                break;
            }
            case 4: {
                this.rCol = 0.7f + level.random.nextFloat() * 0.3f;
                this.gCol = 0.2f;
                this.bCol = 0.2f;
                break;
            }
            case 5: {
                this.blendmode = 771;
                this.rCol = level.random.nextFloat() * 0.1f;
                this.gCol = level.random.nextFloat() * 0.1f;
                this.bCol = level.random.nextFloat() * 0.1f;
                break;
            }
            case 6: {
                this.rCol = 0.8f + level.random.nextFloat() * 0.2f;
                this.gCol = 0.8f + level.random.nextFloat() * 0.2f;
                this.bCol = 0.8f + level.random.nextFloat() * 0.2f;
                break;
            }
            case 7: {
                this.rCol = 0.7f + level.random.nextFloat() * 0.3f;
                this.gCol = 0.5f + level.random.nextFloat() * 0.2f;
                this.bCol = 0.3f + level.random.nextFloat() * 0.1f;
                break;
            }
        }
    }

    public FXWisp(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, float scale, int type) {
        this(level, x, y, z, scale, type);
        if (this.lifetime > 0) {
            double dx = targetX - this.x;
            double dy = targetY - this.y;
            double dz = targetZ - this.z;
            this.xd = dx / this.lifetime;
            this.yd = dy / this.lifetime;
            this.zd = dz / this.lifetime;
        }
    }

    public FXWisp(ClientLevel level, double x, double y, double z, Entity target, int type) {
        this(level, x, y, z, 0.4f, type);
        this.target = target;
    }

    public FXWisp(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, float scale, float red, float green, float blue) {
        this(level, x, y, z, scale, red, green, blue, 0.0f);
        if (this.lifetime > 0) {
            double dx = targetX - this.x;
            double dy = targetY - this.y;
            double dz = targetZ - this.z;
            this.xd = dx / this.lifetime;
            this.yd = dy / this.lifetime;
            this.zd = dz / this.lifetime;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        UtilsFX.bindTexture(ParticleEngine.PARTICLE_TEXTURE);

        float agescale = 0.0f;
        if (this.shrink) {
            agescale = (this.lifetime - (float)this.age) / this.lifetime;
        } else {
            agescale = this.age / (float)this.moteHalfLife;
            if (agescale > 1.0f) {
                agescale = 2.0f - agescale;
            }
        }
        this.quadSize = this.moteParticleScale * agescale;

        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());

        Vec3[] avec3 = new Vec3[]{
                new Vec3(-1.0D, -1.0D, 0.0D),
                new Vec3(-1.0D, 1.0D, 0.0D),
                new Vec3(1.0D, 1.0D, 0.0D),
                new Vec3(1.0D, -1.0D, 0.0D)
        };

        float f3 = this.quadSize;

        for(int i = 0; i < 4; ++i) {
            Vec3 vec31 = avec3[i];
            vec31 = vec31.scale(f3);
            vec31 = vec31.add(f, f1, f2);
        }

        float f4 = this.getU0();
        float f5 = this.getU1();
        float f6 = this.getV0();
        float f7 = this.getV1();
        int j = this.getLightColor(partialTicks);

        buffer.vertex(avec3[0].x, avec3[0].y, avec3[0].z)
                .uv(f5, f7)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(j)
                .endVertex();

        buffer.vertex(avec3[1].x, avec3[1].y, avec3[1].z)
                .uv(f5, f6)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(j)
                .endVertex();

        buffer.vertex(avec3[2].x, avec3[2].y, avec3[2].z)
                .uv(f4, f6)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(j)
                .endVertex();

        buffer.vertex(avec3[3].x, avec3[3].y, avec3[3].z)
                .uv(f4, f7)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(j)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return (this.blendmode != 1) ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age == 0 && this.tinkle && this.level.random.nextInt(3) == 0) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT,
                    0.02f, 0.5f * ((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.6f + 2.0f), false);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.yd -= 0.04 * this.gravity;

        if (this.hasPhysics) {
            this.pushOutOfBlocks(this.x, this.y, this.z);
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.target != null) {
            this.xd *= 0.985;
            this.yd *= 0.985;
            this.zd *= 0.985;

            double dx = this.target.getX() - this.x;
            double dy = this.target.getY() - this.y;
            double dz = this.target.getZ() - this.z;

            final double d13 = 0.2;
            final double d14 = Math.sqrt(dx * dx + dy * dy + dz * dz);

            dx /= d14;
            dy /= d14;
            dz /= d14;

            this.xd += dx * d13;
            this.yd += dy * d13;
            this.zd += dz * d13;

            this.xd = Mth.clamp(this.xd, -0.2, 0.2);
            this.yd = Mth.clamp(this.yd, -0.2, 0.2);
            this.zd = Mth.clamp(this.zd, -0.2, 0.2);
        } else {
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;

            if (this.onGround) {
                this.xd *= 0.7;
                this.zd *= 0.7;
            }
        }
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        double dx = x - blockpos.getX();
        double dy = y - blockpos.getY();
        double dz = z - blockpos.getZ();

        if (!this.level.getBlockState(blockpos).isAir() &&
                this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos) &&
                !this.level.getBlockState(blockpos).getFluidState().isEmpty()) {

            boolean[] canMove = new boolean[6];
            canMove[0] = this.level.getBlockState(blockpos.west()).isAir();
            canMove[1] = this.level.getBlockState(blockpos.east()).isAir();
            canMove[2] = this.level.getBlockState(blockpos.below()).isAir();
            canMove[3] = this.level.getBlockState(blockpos.above()).isAir();
            canMove[4] = this.level.getBlockState(blockpos.north()).isAir();
            canMove[5] = this.level.getBlockState(blockpos.south()).isAir();

            int direction = -1;
            double minDistance = 9999.0;

            if (canMove[0] && dx < minDistance) {
                minDistance = dx;
                direction = 0;
            }
            if (canMove[1] && 1.0 - dx < minDistance) {
                minDistance = 1.0 - dx;
                direction = 1;
            }
            if (canMove[2] && dy < minDistance) {
                minDistance = dy;
                direction = 2;
            }
            if (canMove[3] && 1.0 - dy < minDistance) {
                minDistance = 1.0 - dy;
                direction = 3;
            }
            if (canMove[4] && dz < minDistance) {
                minDistance = dz;
                direction = 4;
            }
            if (canMove[5] && 1.0 - dz < minDistance) {
                minDistance = 1.0 - dz;
                direction = 5;
            }

            float velocity = this.random.nextFloat() * 0.05f + 0.025f;
            float randomMotion = (this.random.nextFloat() - this.random.nextFloat()) * 0.1f;

            switch (direction) {
                case 0:
                    this.xd = -velocity;
                    this.yd = randomMotion;
                    this.zd = randomMotion;
                    break;
                case 1:
                    this.xd = velocity;
                    this.yd = randomMotion;
                    this.zd = randomMotion;
                    break;
                case 2:
                    this.yd = -velocity;
                    this.xd = randomMotion;
                    this.zd = randomMotion;
                    break;
                case 3:
                    this.yd = velocity;
                    this.xd = randomMotion;
                    this.zd = randomMotion;
                    break;
                case 4:
                    this.zd = -velocity;
                    this.xd = randomMotion;
                    this.yd = randomMotion;
                    break;
                case 5:
                    this.zd = velocity;
                    this.xd = randomMotion;
                    this.yd = randomMotion;
                    break;
            }
            return true;
        }
        return false;
    }

    public void setGravity(float value) {
        this.gravity = value;
    }

    protected float getU0() {
        return 0.0f;
    }

    protected float getU1() {
        return 0.125f;
    }

    protected float getV0() {
        return 0.875f;
    }

    protected float getV1() {
        return 1.0f;
    }
}