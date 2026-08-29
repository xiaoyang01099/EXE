package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;


final class GroundImpactTimeline {
    private GroundImpactTimeline() {
    }

    static float lineProgressAtGround(SlashLine line, double groundY) {
        if (line == null) return 1.0f;
        return GroundImpactShockwave.lineProgressAtGround(line.start(), line.end(), groundY);
    }

    static float contactAge(SlashLine line, double groundY) {
        if (line == null) return Float.POSITIVE_INFINITY;

        return GroundImpactShockwave.contactAge(line.start(), line.end(), line.startTick(), groundY);
    }

    static float progress(float renderAge, float contactAge, float durationTicks) {
        return GroundImpactShockwave.progress(renderAge, contactAge, durationTicks);
    }

    static boolean isActive(float renderAge, float contactAge, float durationTicks) {
        return GroundImpactShockwave.isActive(renderAge, contactAge, durationTicks);
    }

    static float radius(float startRadius, float maximumRadius, float progress) {
        return GroundImpactShockwave.radius(startRadius, maximumRadius, progress);
    }

    static float alpha(float progress) {
        return GroundImpactShockwave.alpha(progress);
    }
}
