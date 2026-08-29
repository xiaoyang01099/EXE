package org.xiaoyang.ex_enigmaticlegacy.Util;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MathUtil {
    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();

        matrix.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
        matrix.rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Vec3 cameraPos = camera.getPosition();
        matrix.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
        return matrix;
    }

    public static float getClientTime(float partialTick) {
        if (Minecraft.getInstance().level == null) {
            return partialTick / 20.0f;
        }
        return (Minecraft.getInstance().level.getGameTime() + partialTick) / 20.0f;
    }
}
