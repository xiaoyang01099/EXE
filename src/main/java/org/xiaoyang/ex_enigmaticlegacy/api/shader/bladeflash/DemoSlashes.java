package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes;

import java.util.ArrayList;
import java.util.List;

public final class DemoSlashes {
    private static final int SAMPLE_RATE = 120;
    private static final double DURATION = 0.25;
    private static final List<Slash> SLASHES = new ArrayList<>();

    private static final class Slash {
        final Object key = new Object();
        final Vec3 origin;
        final Vec3 forward;
        final Vec3 across;
        final double start;
        final ItemEffectTypes.Blade style;
        int nextSample;

        Slash(LivingEntity entity) {
            InteractionHand hand=entity.swingingArm==null?InteractionHand.MAIN_HAND:entity.swingingArm;
            var selected=ItemEffectTypes.blade(entity.getItemInHand(hand));
            style=selected==ItemEffectTypes.Blade.NONE?ItemEffectTypes.blade(entity.getItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND)):selected;
            origin = entity.getEyePosition().add(0, -0.35, 0);
            forward = entity.getLookAngle().normalize();
            Vec3 right = forward.cross(new Vec3(0, 1, 0));
            if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
            right = right.normalize();
            Vec3 up = right.cross(forward).normalize();
            across = right.scale(0.88).add(up.scale(0.475)).normalize();
            start = BladeFlashClient.timeSeconds();
        }
    }

    static void spawn(LivingEntity entity) {
        if (SLASHES.size() < ConfigHandler.MAX_TRAILS.get()) SLASHES.add(new Slash(entity));
    }

    static void update() {
        double now = BladeFlashClient.timeSeconds();
        SLASHES.removeIf(slash -> {
            int last = Math.min((int) Math.floor((now - slash.start) * SAMPLE_RATE), (int) (DURATION * SAMPLE_RATE));
            while (slash.nextSample <= last) {
                double t = slash.nextSample / (double) SAMPLE_RATE;
                double angle = -1.15 + 2.30 * (t / DURATION);
                Vec3 direction = slash.forward.scale(Math.cos(angle)).add(slash.across.scale(Math.sin(angle)));
                BladeFlashClient.sample(slash.key, slash.origin.add(direction.scale(0.45)),
                    slash.origin.add(direction.scale(2.65)), slash.start + t, ConfigHandler.TRAIL_SECONDS.get(),slash.style);
                slash.nextSample++;
            }
            if (now - slash.start > DURATION) {
                BladeFlashClient.breakTrail(slash.key);
                return true;
            }
            return false;
        });
    }

    static void clear() { SLASHES.clear(); }
}
