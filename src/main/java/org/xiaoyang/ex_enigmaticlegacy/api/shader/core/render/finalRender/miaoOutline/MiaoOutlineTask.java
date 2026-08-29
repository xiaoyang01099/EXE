package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import net.minecraft.world.entity.Entity;

public class MiaoOutlineTask {
    public final Entity entity;
    public final MiaoOutlineStyle style;

    public MiaoOutlineTask(Entity entity, MiaoOutlineStyle style) {
        this.entity = entity;
        this.style = style;
    }
}
