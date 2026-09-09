package org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

final class CoffinAnimation {
    record Particle(int material, int mode, Quaternionf orientation, Vector3f direction, float u0, float v0, float u1, float v1) {}
    record Frame(int[] ids, int[] offsets, float[] values) {}
    final int fps;
    final Particle[] particles;
    final Frame[] frames;

    private CoffinAnimation(int fps, Particle[] particles, Frame[] frames) {
        this.fps = fps; this.particles = particles; this.frames = frames;
    }

    static CoffinAnimation load(ResourceManager resources) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(resources.open(
            new ResourceLocation(Exe.MODID, "coffin/animation.bfc.gz"))), 64 * 1024))) {
            if (in.readInt() != 0x42464333) throw new IOException("Invalid coffin animation header");
            int fps = bounded(in.readInt(), 1, 120), count = bounded(in.readInt(), 2, 1000);
            int capacity = bounded(in.readInt(), 1, 10000);
            Particle[] particles = new Particle[capacity];
            for (int i = 0; i < capacity; i++) particles[i] = new Particle(
                bounded(in.readUnsignedByte(), 0, 4), bounded(in.readUnsignedByte(), 0, 3),
                new Quaternionf(number(in), number(in), number(in), number(in)),
                new Vector3f(number(in), number(in), number(in)), number(in), number(in), number(in), number(in));
            Frame[] frames = new Frame[count];
            for (int i = 0; i < count; i++) {
                int size = bounded(in.readInt(), 0, capacity);
                int[] ids = new int[size], offsets = new int[capacity];
                Arrays.fill(offsets, -1);
                float[] values = new float[size * 10];
                for (int j = 0; j < size; j++) {
                    ids[j] = bounded(in.readInt(), 0, capacity - 1);
                    offsets[ids[j]] = j * 10;
                    for (int k = 0; k < 10; k++) values[j * 10 + k] = number(in);
                }
                frames[i] = new Frame(ids, offsets, values);
            }
            return new CoffinAnimation(fps, particles, frames);
        }
    }

    private static int bounded(int value, int min, int max) throws IOException {
        if (value < min || value > max) throw new IOException("Invalid coffin animation count");
        return value;
    }
    private static float number(DataInputStream in) throws IOException {
        float value = in.readFloat();
        if (!Float.isFinite(value)) throw new IOException("Nonfinite coffin animation value");
        return value;
    }
}
