package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

public class Effect {
    public int lifetime = 0;
    public int maxLife = 0;
    public float r = 0.0F;
    public float g = 0.0F;
    public float b = 0.0F;
    public float a = 0.0F;
    public boolean inited = false;
    public double x = 0.0;
    public double y = 0.0;
    public double z = 0.0;
    public double px = 0.0;
    public double py = 0.0;
    public double pz = 0.0;
    public double vx = 0.0;
    public double vy = 0.0;
    public double vz = 0.0;
    public boolean additive = false;
    public boolean dead = false;
    public int dimId = 0;

    public Effect() {
    }

    public Effect(int id) {
        this.dimId = id;
    }

    public Effect setLife(int l) {
        this.maxLife = l;
        this.lifetime = l;
        return this;
    }

    public Effect setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public Effect setPosition(double x, double y, double z) {
        this.px = x;
        this.py = y;
        this.pz = z;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Effect setMotion(double vx, double vy, double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        return this;
    }

    public Effect setAdditive(boolean additive) {
        this.additive = additive;
        return this;
    }

    public void update() {
        if (!this.inited) {
            this.inited = true;
        }

        this.px = this.x;
        this.py = this.y;
        this.pz = this.z;
        this.x += this.vx;
        this.y += this.vy;
        this.z += this.vz;
        --this.lifetime;
        if (this.lifetime < 0) {
            this.kill();
        }
    }

    public void kill() {
        this.dead = true;
    }

    public float getLifeCoeff(float pTicks) {
        return Math.max(0.0F, ((float)this.lifetime - pTicks) / (float)this.maxLife);
    }

    public float getInterpX(float pticks) {
        return (float)this.px + ((float)this.x - (float)this.px) * pticks;
    }

    public float getInterpY(float pticks) {
        return (float)this.py + ((float)this.y - (float)this.py) * pticks;
    }

    public float getInterpZ(float pticks) {
        return (float)this.pz + ((float)this.z - (float)this.pz) * pticks;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTotal(PoseStack poseStack, float pticks) {
        if (this.inited) {
            RenderSystem.enableBlend();

            if (this.additive) {
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            } else {
                RenderSystem.blendFuncSeparate(
                        GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE, GL11.GL_ZERO
                );
            }

            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

            poseStack.pushPose();
            this.render(poseStack, pticks);
            poseStack.popPose();

            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    public void read(CompoundTag tag) {
        this.px = this.x;
        this.x = tag.getDouble("x");
        this.py = this.y;
        this.y = tag.getDouble("y");
        this.pz = this.z;
        this.z = tag.getDouble("z");
        this.vx = tag.getDouble("vx");
        this.vy = tag.getDouble("vy");
        this.vz = tag.getDouble("vz");
        this.r = tag.getFloat("r");
        this.g = tag.getFloat("g");
        this.b = tag.getFloat("b");
        this.a = tag.getFloat("a");
        this.maxLife = tag.getInt("maxlife");
        this.lifetime = tag.getInt("life");
        this.dimId = tag.getInt("dim");
        this.additive = tag.getBoolean("additive");
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", this.x);
        tag.putDouble("y", this.y);
        tag.putDouble("z", this.z);
        tag.putDouble("vx", this.vx);
        tag.putDouble("vy", this.vy);
        tag.putDouble("vz", this.vz);
        tag.putFloat("r", this.r);
        tag.putFloat("g", this.g);
        tag.putFloat("b", this.b);
        tag.putFloat("a", this.a);
        tag.putInt("maxlife", this.maxLife);
        tag.putInt("life", this.lifetime);
        tag.putInt("dim", this.dimId);
        tag.putBoolean("additive", this.additive);
        return tag;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack poseStack, float pticks) {
    }
}
