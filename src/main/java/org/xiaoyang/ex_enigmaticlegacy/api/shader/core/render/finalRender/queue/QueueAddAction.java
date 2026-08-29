package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

@FunctionalInterface
public interface QueueAddAction<T extends Entity> {
    void add(EntityQueue<T> queue, T entity, PoseStack pose, Matrix4f modelViewMatrix);
}
