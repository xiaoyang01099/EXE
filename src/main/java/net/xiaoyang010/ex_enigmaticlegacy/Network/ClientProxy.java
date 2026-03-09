package net.xiaoyang010.ex_enigmaticlegacy.Network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXBubble;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXBurst;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXLightningBolt;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXWisp;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.proxy.IProxy;

public class ClientProxy extends CommonProxy {
    @OnlyIn(Dist.CLIENT)
    public static void spawnSuperParticle(Level world, String particleType, double x, double y, double z,
                                          double velX, double velY, double velZ, float particleSize, double renderDistance) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.particleEngine != null && mc.level == world) {
            double distX = mc.player.getX() - x;
            double distY = mc.player.getY() - y;
            double distZ = mc.player.getZ() - z;

            if (distX * distX + distY * distY + distZ * distZ < renderDistance * renderDistance) {
                if (particleType.equals("portalstuff")) {
                    for (int i = 0; i < Math.max(1, (int) particleSize); i++) {
                        world.addParticle(ParticleTypes.ENCHANT,
                                x + (Math.random() - 0.5) * 0.5,
                                y + (Math.random() - 0.5) * 0.5,
                                z + (Math.random() - 0.5) * 0.5,
                                velX + (Math.random() - 0.5) * 0.2,
                                velY + (Math.random() - 0.5) * 0.2,
                                velZ + (Math.random() - 0.5) * 0.2);
                    }
                } else if (particleType.equals("explosion")) {
                    int particleCount = Math.max(5, (int) (particleSize * 3));
                    for (int i = 0; i < particleCount; i++) {
                        world.addParticle(ParticleTypes.EXPLOSION,
                                x + (Math.random() - 0.5) * particleSize,
                                y + (Math.random() - 0.5) * particleSize,
                                z + (Math.random() - 0.5) * particleSize,
                                velX + (Math.random() - 0.5) * 0.5,
                                velY + (Math.random() - 0.5) * 0.5,
                                velZ + (Math.random() - 0.5) * 0.5);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void crucibleBubble(Level world, float x, float y, float z, float cr, float cg, float cb) {
        FXBubble fb = new FXBubble((ClientLevel)world, (double)x, (double)y, (double)z, 0.0D, 0.0D, 0.0D, 1);
        fb.setColor(cr, cg, cb);
        ParticleEngine.instance.addEffect(world, fb);
    }

    @OnlyIn(Dist.CLIENT)
    public static void crucibleFroth(Level world, float x, float y, float z) {
        FXBubble fb = new FXBubble((ClientLevel)world, (double)x, (double)y, (double)z, 0.0D, 0.0D, 0.0D, -4);
        fb.setColor(0.5F, 0.5F, 0.7F);
        fb.setFroth();
        ParticleEngine.instance.addEffect(world, fb);
    }

    @OnlyIn(Dist.CLIENT)
    public static void crucibleFrothDown(Level world, float x, float y, float z) {
        FXBubble fb = new FXBubble((ClientLevel)world, (double)x, (double)y, (double)z, 0.0D, 0.0D, 0.0D, -4);
        fb.setColor(0.5F, 0.5F, 0.7F);
        fb.setFroth2();
        ParticleEngine.instance.addEffect(world, fb);
    }

    @OnlyIn(Dist.CLIENT)
    public static void lunarBurst(Level world, double x, double y, double z, float size) {
        FXBurst ef = new FXBurst((ClientLevel) world, x, y, z, size);
        Minecraft.getInstance().particleEngine.add(ef);
    }

    @OnlyIn(Dist.CLIENT)
    public void lightning(Level world, double sx, double sy, double sz, double ex, double ey, double ez, int dur, float curve, int speed, int type, float width) {
        Vec3 vectorStart = new Vec3(sx, sy, sz);
        Vec3 vectorEnd = new Vec3(ex, ey, ez);
        long seed = world.random.nextLong();

        int colorOuter = 0x4169E1;
        int colorInner = 0xFFFFFF;

        IProxy.INSTANCE.lightningFX(vectorStart, vectorEnd, speed, seed, colorOuter, colorInner);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX4(ClientLevel worldObj, double posX, double posY, double posZ, Entity target, int type, boolean shrink, float gravity) {
        if (target == null) return;

        float[] colors = getColorByType(type);
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];

        double dx = target.getX() - posX;
        double dy = target.getY() + target.getBbHeight() / 2 - posY;
        double dz = target.getZ() - posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance > 0) {
            double motionX = dx / distance * 0.15;
            double motionY = dy / distance * 0.15 - gravity * 0.5;
            double motionZ = dz / distance * 0.15;

            float size = shrink ? 0.3f : 0.6f;
            float maxAgeMul = shrink ? 0.8f : 1.2f;

            WispParticleData data = WispParticleData.wisp(size, r, g, b, maxAgeMul, true);
            worldObj.addParticle(data, posX, posY, posZ, motionX, motionY, motionZ);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX2(ClientLevel worldObj, double posX, double posY, double posZ, float size, int type, boolean shrink, boolean clip, float gravity) {
        float[] colors = getColorByType(type);
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];

        float maxAgeMul = shrink ? 0.5f : 1.0f;

        WispParticleData data = WispParticleData.wisp(size, r, g, b, maxAgeMul, true)
                .withNoClip(!clip);

        double motionY = -gravity * 0.5;

        worldObj.addParticle(data, posX, posY, posZ, 0, motionY, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX3(ClientLevel worldObj, double posX, double posY, double posZ, double posX2, double posY2, double posZ2, float size, int type, boolean shrink, float gravity) {
        float[] colors = getColorByType(type);
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];

        double motionX = (posX2 - posX) * 0.1;
        double motionY = (posY2 - posY) * 0.1 - gravity * 0.5;
        double motionZ = (posZ2 - posZ) * 0.1;

        float maxAgeMul = shrink ? 0.5f : 1.5f;

        WispParticleData data = WispParticleData.wisp(size, r, g, b, maxAgeMul, true);

        worldObj.addParticle(data, posX, posY, posZ, motionX, motionY, motionZ);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX(ClientLevel worldObj, double posX, double posY, double posZ, float r, float g, float b, float size) {
        WispParticleData data = WispParticleData.wisp(size, r, g, b, 1.0f, true);

        worldObj.addParticle(data, posX, posY, posZ, 0, -0.02, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX2(Level level, double posX, double posY, double posZ, float size, int type, boolean shrink, boolean hasPhysics, float gravity) {
        FXWisp ef = new FXWisp((ClientLevel) level, posX, posY, posZ, size, type);
        ef.setGravity(gravity);
        ef.shrink = shrink;
        ef.hasPhysics = hasPhysics;
        ParticleEngine.instance.addEffect(level, ef);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX3(Level level, double posX, double posY, double posZ, double targetX, double targetY, double targetZ, float size, int type, boolean shrink, float gravity) {
        FXWisp ef = new FXWisp((ClientLevel) level, posX, posY, posZ, targetX, targetY, targetZ, size, type);
        ef.setGravity(gravity);
        ef.shrink = shrink;
        ParticleEngine.instance.addEffect(level, ef);
    }

    @OnlyIn(Dist.CLIENT)
    public static void wispFX4(Level level, double posX, double posY, double posZ, Entity target, int type, boolean shrink, float gravity) {
        FXWisp ef = new FXWisp((ClientLevel) level, posX, posY, posZ, target, type);
        ef.setGravity(gravity);
        ef.shrink = shrink;
        ParticleEngine.instance.addEffect(level, ef);
    }

    @Override
    public void bolt(Level worldObj, Entity sourceEntity, Entity targetedEntity) {
        if (!(worldObj instanceof ClientLevel)) return;

        FXLightningBolt bolt = new FXLightningBolt(
                (ClientLevel) worldObj,
                sourceEntity,
                targetedEntity,
                worldObj.random.nextLong(),
                4
        );
        bolt.defaultFractal();
        bolt.setType(0);
        bolt.finalizeBolt();
    }

    @Override
    public void nodeBolt(Level worldObj, float x, float y, float z, Entity targetedEntity) {
        if (!(worldObj instanceof ClientLevel)) return;

        FXLightningBolt bolt = new FXLightningBolt(
                (ClientLevel) worldObj,
                x, y, z,
                targetedEntity.getX(),
                targetedEntity.getY(),
                targetedEntity.getZ(),
                worldObj.random.nextLong(),
                10,
                4.0f,
                5
        );
        bolt.defaultFractal();
        bolt.setType(3);
        bolt.finalizeBolt();
    }

    @Override
    public void nodeBolt(Level worldObj, float x, float y, float z, float x2, float y2, float z2) {
        if (!(worldObj instanceof ClientLevel)) return;

        FXLightningBolt bolt = new FXLightningBolt(
                (ClientLevel) worldObj,
                x, y, z,
                x2, y2, z2,
                worldObj.random.nextLong(),
                10,
                4.0f,
                5
        );
        bolt.defaultFractal();
        bolt.setType(0);
        bolt.finalizeBolt();
    }

    private static float[] getColorByType(int type) {
        return switch (type) {
            case 0 -> new float[]{1.0f, 1.0f, 1.0f}; // 白色
            case 1 -> new float[]{1.0f, 0.2f, 0.2f}; // 红色
            case 2 -> new float[]{0.2f, 1.0f, 0.2f}; // 绿色
            case 3 -> new float[]{0.2f, 0.2f, 1.0f}; // 蓝色
            case 4 -> new float[]{1.0f, 1.0f, 0.2f}; // 黄色
            case 5 -> new float[]{1.0f, 0.2f, 1.0f}; // 紫色
            case 6 -> new float[]{0.2f, 1.0f, 1.0f}; // 青色
            case 7 -> new float[]{1.0f, 0.6f, 0.2f}; // 橙色
            case 8 -> new float[]{0.8f, 0.8f, 0.8f}; // 灰色
            case 9 -> new float[]{0.4f, 0.2f, 0.8f}; // 深紫色
            default -> new float[]{0.9f + (float)Math.random() * 0.1f,
                    0.9f + (float)Math.random() * 0.1f,
                    0.9f + (float)Math.random() * 0.1f};
        };
    }
}