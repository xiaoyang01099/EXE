package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModDamageSources;

import javax.annotation.Nullable;

public class ProjectileAntiImmunityDamage extends DamageSource {
    private final Entity projectile;
    @Nullable
    private final Entity owner;

    public ProjectileAntiImmunityDamage(Level level, String msgId, Entity projectile, @Nullable Entity owner) {
        super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(resolveKey(msgId)), projectile, owner);
        this.projectile = projectile;
        this.owner = owner;
    }

    private static ResourceKey<DamageType> resolveKey(String msgId) {
        return switch (msgId) {
            case "dragonslayer"        -> ModDamageSources.DRAGONSLAYER;
            case "absolute"            -> ModDamageSources.ABSOLUTE;
            case "true_damage"         -> ModDamageSources.TRUE_DAMAGE;
            case "soul_drain"          -> ModDamageSources.SOUL_DRAIN;
            case "oblivion"            -> ModDamageSources.OBLIVION;
            case "fate"                -> ModDamageSources.FATE;
            case "lightning"           -> ModDamageSources.LIGHTNING;
            case "dark_matter"         -> ModDamageSources.DARK_MATTER;
            case "superposition"       -> ModDamageSources.SUPERPOSITION;
            case "paradox"             -> ModDamageSources.PARADOX;
            case "paradox_reflection"  -> ModDamageSources.PARADOX_REFLECTION;
            case "mana"                -> ModDamageSources.MANA;
            case "infinity_arrow"      -> ModDamageSources.INFINITY_ARROW;
            default -> throw new IllegalArgumentException("miss: " + msgId);
        };
    }

    @Override
    @Nullable
    public Entity getDirectEntity() {
        return null;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.owner;
    }

    public Component getLocalizedDeathMessage(LivingEntity victim) {
        Component attackerName = (this.owner != null)
                ? this.owner.getDisplayName()
                : this.projectile.getDisplayName();

        ItemStack weapon = (this.owner instanceof LivingEntity livingOwner)
                ? livingOwner.getMainHandItem()
                : ItemStack.EMPTY;

        String baseKey = "death.attack." + this.getMsgId();
        String itemKey = baseKey + ".item";

        return (!weapon.isEmpty() && weapon.hasCustomHoverName())
                ? Component.translatable(itemKey,
                victim.getDisplayName(),
                attackerName,
                weapon.getDisplayName())
                : Component.translatable(baseKey,
                victim.getDisplayName(),
                attackerName
        );
    }
}