package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// CameraShakeUtil 提供通用客户端相机抖动能力，世界范围技能和本地效果都可以复用。
@OnlyIn(value = Dist.CLIENT)
public class CameraShakeUtil {
    public static final List<Shake> SHAKES = new ArrayList<>(); // 当前存活的抖动任务。
    public static float PITCH_FREQUENCY = 48.0F; // pitch 抖动频率，数值越大抖动越快。
    public static float YAW_FREQUENCY = 59.0F; // yaw 抖动频率，数值越大抖动越快。
    public static float ROLL_FREQUENCY = 72.0F; // roll 抖动频率，数值越大抖动越快。
    public static float ROLL_STRENGTH_SCALE = 0.68F; // roll 抖动强度倍率。
    public static long lastTickGameTime = Long.MIN_VALUE; // 上一次推进抖动生命周期的游戏 tick。

    // 添加一次带世界范围的相机抖动。
    public static void addShake(Vec3 center, float radius, int durationTicks, float strength) {
        SHAKES.add(new Shake(center, radius, durationTicks, strength));
    }

    // 添加一次先保持强度再淡出的范围相机抖动。
    public static void addSustainedShake(Vec3 center, float radius, int sustainTicks, int fadeTicks, float strength) {
        SHAKES.add(new Shake(center, radius, sustainTicks, fadeTicks, strength));
    }

    // 添加一次本地相机抖动。
    public static void addShake(float durationTicks, float strength) {
        SHAKES.add(new Shake(null, 0.0F, (int) durationTicks, strength));
    }

    // 添加一次先保持强度再淡出的本地相机抖动。
    public static void addSustainedShake(int sustainTicks, int fadeTicks, float strength) {
        SHAKES.add(new Shake(null, 0.0F, sustainTicks, fadeTicks, strength));
    }

    // 推进并清理抖动任务。
    public static void tick() {
        Iterator<Shake> iterator = SHAKES.iterator();
        while (iterator.hasNext()) {
            Shake shake = iterator.next();
            shake.age++;
            if (shake.age > shake.durationTicks) {
                iterator.remove();
            }
        }
    }

    // 根据摄像机位置采样当前抖动强度。
    public static Sample sample(Vec3 cameraPos, float partialTick) {
        float pitch = 0.0F;
        float yaw = 0.0F;
        float roll = 0.0F;
        for (Shake shake : SHAKES) {
            float fade = shake.getLifeFade(partialTick);
            float distanceFade = shake.getDistanceFade(cameraPos);
            float strength = shake.strength * fade * fade * distanceFade;
            double time = Minecraft.getInstance().level == null ? 0.0D : Minecraft.getInstance().level.getGameTime() + partialTick;
            pitch += (float) Math.sin(time * PITCH_FREQUENCY + shake.seed) * strength;
            yaw += (float) Math.cos(time * YAW_FREQUENCY + shake.seed * 0.7D) * strength;
            roll += (float) Math.sin(time * ROLL_FREQUENCY + shake.seed * 1.3D) * strength * ROLL_STRENGTH_SCALE;
        }
        return new Sample(pitch, yaw, roll);
    }

    // 在 Forge 相机角度事件中应用当前抖动。
    public static void apply(ViewportEvent.ComputeCameraAngles event) {
        Vec3 cameraPos = event.getCamera().getPosition();
        Sample sample = sample(cameraPos, (float) event.getPartialTick());
        event.setPitch(event.getPitch() + sample.pitch);
        event.setYaw(event.getYaw() + sample.yaw);
        event.setRoll(event.getRoll() + sample.roll);
        tickOncePerGameTick();
    }

    // 渲染事件每帧触发，生命周期只按游戏 tick 推进，避免抖动被一两帧消耗完。
    public static void tickOncePerGameTick() {
        if (Minecraft.getInstance().level == null) return;
        long gameTime = Minecraft.getInstance().level.getGameTime();
        if (gameTime == lastTickGameTime) return;
        lastTickGameTime = gameTime;
        tick();
    }

    // Shake 表示一次相机抖动任务。
    public static class Shake {
        public final Vec3 center; // 世界范围中心，null 表示本地抖动。
        public final float radius; // 世界范围半径。
        public final int durationTicks; // 持续 tick。
        public final int sustainTicks; // 保持完整强度的 tick。
        public final int fadeTicks; // 淡出 tick。
        public final float strength; // 抖动强度。
        public final float seed; // 抖动随机种子。
        public int age; // 已存在 tick。

        public Shake(Vec3 center, float radius, int durationTicks, float strength) {
            this.center = center;
            this.radius = radius;
            this.durationTicks = Math.max(1, durationTicks);
            this.sustainTicks = 0;
            this.fadeTicks = this.durationTicks;
            this.strength = strength;
            this.seed = (float) (Math.random() * 1000.0D);
        }

        public Shake(Vec3 center, float radius, int sustainTicks, int fadeTicks, float strength) {
            this.center = center;
            this.radius = radius;
            this.sustainTicks = Math.max(0, sustainTicks);
            this.fadeTicks = Math.max(1, fadeTicks);
            this.durationTicks = Math.max(1, this.sustainTicks + this.fadeTicks);
            this.strength = strength;
            this.seed = (float) (Math.random() * 1000.0D);
        }

        // 根据持续段和淡出段计算当前生命周期强度。
        public float getLifeFade(float partialTick) {
            float currentAge = age + partialTick;
            if (sustainTicks <= 0) {
                float lifeProgress = Mth.clamp(currentAge / Math.max(1.0F, (float) durationTicks), 0.0F, 1.0F);
                return 1.0F - lifeProgress;
            }
            if (currentAge <= sustainTicks) return 1.0F;
            float fadeProgress = Mth.clamp((currentAge - sustainTicks) / Math.max(1.0F, (float) fadeTicks), 0.0F, 1.0F);
            return 1.0F - fadeProgress;
        }

        // 根据摄像机到范围中心距离计算衰减。
        public float getDistanceFade(Vec3 cameraPos) {
            if (center == null || radius <= 0.0F) return 1.0F;
            double distance = cameraPos.distanceTo(center);
            return Mth.clamp(1.0F - (float) (distance / radius), 0.0F, 1.0F);
        }
    }

    // Sample 表示当前帧相机角度偏移。
    public static class Sample {
        public final float pitch; // pitch 偏移。
        public final float yaw; // yaw 偏移。
        public final float roll; // roll 偏移。

        public Sample(float pitch, float yaw, float roll) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
        }
    }
}
