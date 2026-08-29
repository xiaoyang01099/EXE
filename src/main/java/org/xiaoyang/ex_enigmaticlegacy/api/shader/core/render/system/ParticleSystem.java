package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import net.minecraft.client.Camera;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParticleSystem {
    private final GPUParticleSystem gpuParticleSystem = new GPUParticleSystem();
    private final List<ParticleEmitTask> emitters = new ArrayList<>();
    private final Queue<ParticleEmitTask> pendingEmitters = new ConcurrentLinkedQueue<>();

    public void emit(ParticleEmitTask task) {
        if (task != null) {
            pendingEmitters.add(task);
        }
    }

    public void add(ParticleEmitTask task) {
    }

    private float activeParticleTimeLeft;

    public void updateAndRender(float dt, Matrix4f projectionMatrix, Camera camera) {
        ParticleEmitTask pendingTask;
        while ((pendingTask = pendingEmitters.poll()) != null) {
            emitters.add(pendingTask);
        }

        gpuParticleSystem.beginEmitJobs();

        boolean emittedThisFrame = false;

        Iterator<ParticleEmitTask> iterator = emitters.iterator();
        while (iterator.hasNext()) {
            ParticleEmitTask task = iterator.next();
            int emitCount = task.consumeEmitCount(dt);
            if (emitCount > 0) {
                gpuParticleSystem.addEmitJob(task, emitCount);
                activeParticleTimeLeft = Math.max(activeParticleTimeLeft, task.life);
                emittedThisFrame = true;
            }
            if (task.isDead()) {
                iterator.remove();
            }
        }

        activeParticleTimeLeft = Math.max(0f, activeParticleTimeLeft - dt);

        if (!emittedThisFrame && emitters.isEmpty() && activeParticleTimeLeft <= 0f) {
            return;
        }

        gpuParticleSystem.updateAndRender(dt, projectionMatrix, camera);
    }

    public boolean hasActiveParticles() {
        return !pendingEmitters.isEmpty() || !emitters.isEmpty() || activeParticleTimeLeft > 0.0f;
    }

    public void cleanUp() {
        gpuParticleSystem.cleanUp();
    }
}
