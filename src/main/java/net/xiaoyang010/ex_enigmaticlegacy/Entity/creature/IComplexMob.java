package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Interface for entities composed of multiple parts, like the Sea Serpent.
 * Similar to Minecraft's multipart entity system.
 */
public interface IComplexMob {
    /**
     * Gets all the parts that make up this mob
     */
    BodyPart[] getParts();

    /**
     * Called when a part of this mob is damaged
     */
    boolean hurt(BodyPart part, DamageSource source, float amount);
}