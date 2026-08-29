package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RibbonGeometryRender {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D);
    public static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D);

    public static List<RibbonPoint> buildBillboardRibbon(List<RibbonCenter> centers, Vec3 cameraPos, double halfWidth) {
        List<RibbonPoint> points = new ArrayList<>();
        if (centers == null || centers.size() < 2 || cameraPos == null) return points;

        for (int i = 0; i < centers.size(); i++) {
            points.add(buildPoint(centers, i, cameraPos, halfWidth));
        }
        return points;
    }

    public static RibbonPoint buildPoint(List<RibbonCenter> centers, int index, Vec3 cameraPos, double halfWidth) {
        RibbonCenter center = centers.get(index);
        Vec3 point = center.position;
        Vec3 forward = safeNormalize(forwardAt(centers, index), WORLD_FORWARD);
        Vec3 toCamera = safeNormalize(cameraPos.subtract(point), WORLD_FORWARD);
        Vec3 right = forward.cross(toCamera);

        if (right.lengthSqr() < 1.0E-8D) right = forward.cross(WORLD_UP);
        if (right.lengthSqr() < 1.0E-8D) right = forward.cross(WORLD_RIGHT);
        right = safeNormalize(right, WORLD_RIGHT);

        Vec3 halfRight = right.scale(Math.max(0.0D, halfWidth));
        return new RibbonPoint(point.subtract(halfRight), point.add(halfRight), center.t);
    }

    public static Vec3 forwardAt(List<RibbonCenter> centers, int index) {
        int size = centers.size();
        Vec3 point = centers.get(index).position;
        if (index == 0) {
            return centers.get(1).position.subtract(point);
        }
        if (index == size - 1) {
            return point.subtract(centers.get(index - 1).position);
        }

        Vec3 fromPrev = safeNormalize(point.subtract(centers.get(index - 1).position), WORLD_FORWARD);
        Vec3 toNext = safeNormalize(centers.get(index + 1).position.subtract(point), WORLD_FORWARD);
        Vec3 forward = fromPrev.add(toNext);
        if (forward.lengthSqr() < 1.0E-8D) return toNext.lengthSqr() > 1.0E-8D ? toNext : fromPrev;
        return forward;
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector == null || vector.lengthSqr() < 1.0E-8D) return fallback;
        return vector.normalize();
    }

    public static class RibbonCenter {
        public final Vec3 position;
        public final float t;

        public RibbonCenter(Vec3 position, float t) {
            this.position = position;
            this.t = t;
        }
    }

    public static class RibbonPoint {
        public final Vec3 left;
        public final Vec3 right;
        public final float t;

        public RibbonPoint(Vec3 left, Vec3 right, float t) {
            this.left = left;
            this.right = right;
            this.t = t;
        }
    }
}
