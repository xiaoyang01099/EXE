package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.world.phys.Vec3;

final class RangeVisibility {
    private RangeVisibility() {
    }

    static float at(Vec3 viewer, Vec3 center, float radius, float edgeFadeFraction) {
        return DomainRange.visibility(viewer, center, radius, edgeFadeFraction);
    }

    static float atDistance(float horizontalDistance, float radius, float edgeFadeFraction) {
        return DomainRange.visibilityAtDistance(horizontalDistance, radius, edgeFadeFraction);
    }
}
