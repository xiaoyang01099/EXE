package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class EXVector3 {
    public float x;
    public float y;
    public float z;

    public EXVector3(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }

    public EXVector3(BlockEntity tile) {
        this.x = (float)tile.getBlockPos().getX() + 0.5f;
        this.y = (float)tile.getBlockPos().getY() + 0.5f;
        this.z = (float)tile.getBlockPos().getZ() + 0.5f;
    }

    public EXVector3(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public EXVector3 add(EXVector3 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public EXVector3 sub(EXVector3 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public EXVector3 scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    public EXVector3 scale(float scalex, float scaley, float scalez) {
        this.x *= scalex;
        this.y *= scaley;
        this.z *= scalez;
        return this;
    }

    public EXVector3 normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;
        return this;
    }

    public float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float lengthPow2() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public EXVector3 copy() {
        return new EXVector3(this.x, this.y, this.z);
    }

    public static EXVector3 crossProduct(EXVector3 vec1, EXVector3 vec2) {
        return new EXVector3(
                vec1.y * vec2.z - vec1.z * vec2.y,
                vec1.z * vec2.x - vec1.x * vec2.z,
                vec1.x * vec2.y - vec1.y * vec2.x
        );
    }

    public static EXVector3 xCrossProduct(EXVector3 vec) {
        return new EXVector3(0.0, vec.z, -vec.y);
    }

    public static EXVector3 zCrossProduct(EXVector3 vec) {
        return new EXVector3(-vec.y, vec.x, 0.0);
    }

    public static float dotProduct(EXVector3 vec1, EXVector3 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
    }

    public static float angle(EXVector3 vec1, EXVector3 vec2) {
        return anglePreNorm(vec1.copy().normalize(), vec2.copy().normalize());
    }

    public static float anglePreNorm(EXVector3 vec1, EXVector3 vec2) {
        return (float)Math.acos(dotProduct(vec1, vec2));
    }

    public EXVector3 rotate(float angle, EXVector3 axis) {
        return EXMat4.rotationMat(angle, axis).translate(this);
    }

    @Override
    public String toString() {
        return "[" + this.x + "," + this.y + "," + this.z + "]";
    }

    public Vec3 toVec3D() {
        return new Vec3(this.x, this.y, this.z);
    }

    public static EXVector3 getPerpendicular(EXVector3 vec) {
        if (vec.z == 0.0f) {
            return zCrossProduct(vec);
        }
        return xCrossProduct(vec);
    }

    public boolean isZero() {
        return this.x == 0.0f && this.y == 0.0f && this.z == 0.0f;
    }
}