package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

import java.util.function.Predicate;

public class EntityQueueRegistration<T extends Entity> {
    public final Class<T> entityClass;
    public final EntityQueue<T> queue;
    public final Predicate<T> filter;
    public final QueueAddAction<T> addAction;

    public EntityQueueRegistration(Class<T> entityClass, EntityQueue<T> queue, Predicate<T> filter, QueueAddAction<T> addAction) {
        this.entityClass = entityClass;
        this.queue = queue;
        this.filter = filter;
        this.addAction = addAction;
    }

    public boolean tryAdd(Entity entity, PoseStack pose, Matrix4f modelViewMatrix) {
        if (!entityClass.isInstance(entity)) return false;
        T typedEntity = entityClass.cast(entity);
        if (!filter.test(typedEntity)) return false;
        addAction.add(queue, typedEntity, pose, modelViewMatrix);
        return true;
    }
}
