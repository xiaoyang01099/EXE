package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.phys.Vec3;

public class Bone {
    private float length = 1.0F;
    private final Euler angle = new Euler();
    private final List<Bone> child = new ArrayList<>();
    @Nullable
    private final Bone parent;

    public Bone() {
        this(null);
    }

    public Bone(Bone parentNode) {
        this.parent = parentNode;
        if (parentNode != null) {
            this.parent.addChild(this);
        }
    }

    public void addChild(Bone childNode) {
        this.child.add(childNode);
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getLength() {
        return this.length;
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.angle.setAngles(pitch, yaw, roll);
    }

    public void setRotation(Euler euler) {
        this.angle.setAngles(euler);
    }

    public Euler getRotation() {
        return this.angle;
    }

    @Nullable
    public Bone getParent() {
        return this.parent;
    }

    public Euler getAbsoluteRotation() {
        return this.parent != null ? this.angle.getRotated(this.parent.getAbsoluteRotation()) : new Euler(this.angle);
    }

    public Vec3 getRotatedVector() {
        return this.angle.rotateVector(this.length);
    }

    public Vec3 getAbsoluteRotatedVector() {
        return this.getAbsoluteRotation().rotateVector(this.length);
    }

    public Vec3 getTotalRotatedVector() {
        if (this.parent != null) {
            Vec3 vec = this.parent.getTotalRotatedVector();
            Vec3 vec2 = this.getAbsoluteRotatedVector();
            return vec.add(vec2.x, vec2.y, vec2.z);
        } else {
            return this.getRotatedVector();
        }
    }
}