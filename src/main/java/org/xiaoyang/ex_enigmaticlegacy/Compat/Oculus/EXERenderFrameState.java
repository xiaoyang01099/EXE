package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

public final class EXERenderFrameState {
    private static final Snapshot OUTSIDE = new Snapshot(false, false, false, false);
    private static Snapshot current = OUTSIDE;

    public static Snapshot beginFrame() {
        current = new Snapshot(
                true,
                OculusCompat.isOculusLoaded(),
                OculusCompat.isOculusEmbeddiumActive(),
                OculusCompat.isShaderPackActive()
        );
        return current;
    }

    public static void endFrame() {
        current = OUTSIDE;
    }

    public static void clear() {
        current = OUTSIDE;
    }

    public static Snapshot current() {
        return current;
    }

    public static boolean isWorldRenderActive() {
        return current.worldRenderActive();
    }

    public static boolean shaderPackActiveForRender() {
        return current.worldRenderActive()
                ? current.shaderPackActive()
                : OculusCompat.isShaderPackActive();
    }

    public static boolean shouldDeferWorldEffect() {
        return current.worldRenderActive() && current.shaderPackActive();
    }

    public record Snapshot(
            boolean worldRenderActive,
            boolean oculusLoaded,
            boolean oculusEmbeddiumActive,
            boolean shaderPackActive
    ) {}

    private EXERenderFrameState() {}
}