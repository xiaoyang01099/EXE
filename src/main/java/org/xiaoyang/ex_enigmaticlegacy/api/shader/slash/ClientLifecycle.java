package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

final class ClientLifecycle {
    private ClientLifecycle() {
    }

    static boolean isDead(float health, int deathTime, boolean alive) {
        return health <= 0.0f || deathTime > 0 || !alive;
    }

    static float fadeAlpha(float age, float duration) {
        if (duration <= 0.0f) return 0.0f;
        float progress = Math.max(0.0f, Math.min(1.0f, age / duration));
        float remaining = 1.0f - progress;
        return remaining * remaining * (3.0f - 2.0f * remaining);
    }
}
