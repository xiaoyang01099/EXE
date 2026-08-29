package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityQueue<T extends Entity> {
    public List<T> entities;
    public boolean activeInFrame;

    public EntityQueue() {
        this.entities = new ArrayList<>();
        this.activeInFrame = false;
    }

    public void add(T entity) {
        entities.add(entity);
    }

    public abstract void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float parTick, Matrix4f viewMatrix);

    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float parTick, Matrix4f viewMatrix, float frameDeltaSeconds) {
        render(fboBuffer, camera, parTick, viewMatrix);
    }

    public PostRenderPhase getPhase() {
        return PostRenderPhase.DEPTH_TESTED_WORLD;
    }
}
