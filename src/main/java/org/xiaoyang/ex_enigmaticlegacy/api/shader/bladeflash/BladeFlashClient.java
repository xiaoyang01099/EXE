package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;

public final class BladeFlashClient {
    static final TrailStore TRAILS = new TrailStore();
    private static double lastTime;

    private BladeFlashClient() {}

    public static double timeSeconds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0.0;
        if (!mc.isPaused()) lastTime = (mc.level.getGameTime() + mc.getFrameTime()) / 20.0;
        return lastTime;
    }

    public static void sample(Object key, Vec3 bladeRoot, Vec3 bladeTip) {
        sample(key, bladeRoot, bladeTip, timeSeconds(), ConfigHandler.TRAIL_SECONDS.get());
    }

    public static void sample(Object key, Vec3 bladeRoot, Vec3 bladeTip, double time, double lifetime) {
        sample(key,bladeRoot,bladeTip,time,lifetime,org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.CLASSIC);
    }
    public static void sample(Object key, Vec3 bladeRoot, Vec3 bladeTip, double time, double lifetime,org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade style) {
        requireClientThread();
        if (Minecraft.getInstance().level != null && ConfigHandler.ENABLED.get()) {
            TRAILS.sample(key, bladeRoot, bladeTip, time, lifetime,style);
        }
    }

    public static void breakTrail(Object key) {
        requireClientThread();
        TRAILS.breakTrail(key);
    }

    public static void clear() {
        requireClientThread();
        TRAILS.clear();
        HeldItemTrails.reset();
        lastTime = 0.0;
    }

    private static void requireClientThread() {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("EXE API must run on the Minecraft client thread");
        }
    }
}
