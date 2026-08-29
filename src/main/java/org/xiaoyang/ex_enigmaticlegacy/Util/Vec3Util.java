package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Vec3Util {

    //起点（P0）
    //控制点（P1）
    //终点（P2）
    public static List<Vec3> calculateBezierCurve(Vec3 p0, Vec3 p1, Vec3 p2) {
        List<Vec3> points = new ArrayList<>();
        for (double t = 0; t < 1; t += 0.05) {
            double x = (1 - t) * (1 - t) * p0.x + 2 * (1 - t) * t * p1.x + t * t * p2.x;
            double y = (1 - t) * (1 - t) * p0.y + 2 * (1 - t) * t * p1.y + t * t * p2.y;
            double z = (1 - t) * (1 - t) * p0.z + 2 * (1 - t) * t * p1.z + t * t * p2.z;
            points.add(new Vec3(x, y, z));
        }
        return points;
    }

}
