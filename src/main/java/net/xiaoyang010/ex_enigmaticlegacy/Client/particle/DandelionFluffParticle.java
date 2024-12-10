package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DandelionFluffParticle extends TextureSheetParticle {
    private final SpriteSet animatedSprite;
    private double angleOffset; // 用于粒子摆动的角度偏移
    private double windFactor;  // 风的影响因子

    public DandelionFluffParticle(SpriteSet animatedSprite, ClientLevel world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        super(world, posX, posY, posZ, motionX, motionY, motionZ);

        // 增加粒子的初始速度和方向随机性
        this.xd = motionX + (random.nextDouble() - 0.5D) * 0.1D; // 水平方向随机幅度增加
        this.yd = motionY + (random.nextDouble() * 0.1D) + 0.05D; // 垂直方向增加基础上升速度
        this.zd = motionZ + (random.nextDouble() - 0.5D) * 0.1D; // 水平方向随机幅度增加

        this.animatedSprite = animatedSprite;
        this.lifetime = random.nextInt(100) + 100; // 粒子存在时间
        this.quadSize = 0.2F * (this.random.nextFloat() * 0.5F + 0.5F); // 粒子大小
        this.pickSprite(animatedSprite); // 选择精灵帧

        this.angleOffset = random.nextDouble() * Math.PI * 2; // 初始化角度偏移
        this.windFactor = 0.01 + random.nextDouble() * 0.03; // 增加风的影响幅度
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;

        // 透明度根据剩余寿命逐渐降低
        float lifeFraction = (float) this.age / (float) this.lifetime;
        this.setAlpha(1.0F - lifeFraction); // 透明度从1逐渐减少到0

        if (this.age >= this.lifetime) {
            this.remove(); // 移除粒子
        } else {
            // 模拟风力和上下浮动
            this.angleOffset += 0.15; // 让摆动速度更快
            double sway = Math.sin(this.angleOffset) * 0.1; // 增加摆动幅度，模拟更广的飘动

            // 增加风力的影响，使得粒子有倾斜的效果
            this.xd += sway * this.windFactor;  // 水平方向风力影响更大，增加随机性
            this.zd += sway * this.windFactor;

            // 垂直上升速度略有增加，同时逐渐减慢上升
            this.yd = (0.05D + random.nextDouble() * 0.05D) * (1 - lifeFraction);  // 垂直上升逐渐减慢

            // 移动粒子
            this.move(this.xd, this.yd, this.zd);

            // 模拟轻微的旋转
            this.oRoll = this.roll;
            this.roll += (float) (0.1 * (Math.sin(this.angleOffset) * 0.5)); // 粒子旋转幅度增加
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; // 设置渲染类型为半透明
    }

    @Override
    public int getLightColor(float partialTick) {
        float f = this.lifetime / (((this.age + (this.lifetime * 0.5F)) + partialTick));
        f = Mth.clamp(f, 0F, 0.5F);
        int i = super.getLightColor(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15f * 16f);
        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet animatedSprite;

        public Factory(SpriteSet animatedSprite) {
            this.animatedSprite = animatedSprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new DandelionFluffParticle(this.animatedSprite, world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
