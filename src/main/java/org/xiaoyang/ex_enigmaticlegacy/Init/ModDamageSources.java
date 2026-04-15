package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("removal")
public class ModDamageSources {
    public static final String MOD_ID = "ex_enigmaticlegacy";

    public static final DeferredRegister<DamageType> DAMAGE_TYPES =
            DeferredRegister.create(Registries.DAMAGE_TYPE, MOD_ID);

    public static final RegistryObject<DamageType> INFINITY_ARROW_TYPE = DAMAGE_TYPES.register("infinity_arrow",
            () -> new DamageType("infinity_arrow", 0.1F));
    public static final RegistryObject<DamageType> DRAGONSLAYER_TYPE = DAMAGE_TYPES.register("dragonslayer",
            () -> new DamageType("dragonslayer", 0.1F));
    public static final RegistryObject<DamageType> ABSOLUTE_DAMAGE_TYPE = DAMAGE_TYPES.register("absolute",
            () -> new DamageType("absolute", 0.1F));
    public static final RegistryObject<DamageType> TRUE_DAMAGE_TYPE = DAMAGE_TYPES.register("true_damage",
            () -> new DamageType("true_damage", 0.1F));
    public static final RegistryObject<DamageType> SOUL_DRAIN_TYPE = DAMAGE_TYPES.register("soul_drain",
            () -> new DamageType("soul_drain", 0.1F));
    public static final RegistryObject<DamageType> OBLIVION_TYPE = DAMAGE_TYPES.register("oblivion",
            () -> new DamageType("oblivion", 0.1F));
    public static final RegistryObject<DamageType> FATE_TYPE = DAMAGE_TYPES.register("fate",
            () -> new DamageType("fate", 0.1F));
    public static final RegistryObject<DamageType> LIGHTNING_TYPE = DAMAGE_TYPES.register("lightning",
            () -> new DamageType("lightning", 0.1F));
    public static final RegistryObject<DamageType> DARK_MATTER_TYPE = DAMAGE_TYPES.register("dark_matter",
            () -> new DamageType("dark_matter", 0.1F));
    public static final RegistryObject<DamageType> SUPERPOSITION_TYPE = DAMAGE_TYPES.register("superposition",
            () -> new DamageType("superposition", 0.1F));
    public static final RegistryObject<DamageType> PARADOX_TYPE = DAMAGE_TYPES.register("paradox",
            () -> new DamageType("paradox", 0.1F));
    public static final RegistryObject<DamageType> PARADOX_REFLECTION_TYPE = DAMAGE_TYPES.register("paradox_reflection",
            () -> new DamageType("paradox_reflection", 0.1F));
    public static final RegistryObject<DamageType> MANA_TYPE = DAMAGE_TYPES.register("mana",
            () -> new DamageType("mana", 0.0F));

    public static final ResourceKey<DamageType> INFINITY_ARROW = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "infinity_arrow"));
    public static final ResourceKey<DamageType> DRAGONSLAYER = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "dragonslayer"));
    public static final ResourceKey<DamageType> ABSOLUTE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "absolute"));
    public static final ResourceKey<DamageType> TRUE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "true_damage"));
    public static final ResourceKey<DamageType> SOUL_DRAIN = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "soul_drain"));
    public static final ResourceKey<DamageType> OBLIVION = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "oblivion"));
    public static final ResourceKey<DamageType> FATE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "fate"));
    public static final ResourceKey<DamageType> LIGHTNING = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "lightning"));
    public static final ResourceKey<DamageType> DARK_MATTER = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "dark_matter"));
    public static final ResourceKey<DamageType> SUPERPOSITION = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "superposition"));
    public static final ResourceKey<DamageType> PARADOX = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "paradox"));
    public static final ResourceKey<DamageType> PARADOX_REFLECTION = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "paradox_reflection"));
    public static final ResourceKey<DamageType> MANA = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MOD_ID, "mana"));

    public static void register(IEventBus eventBus) {
        DAMAGE_TYPES.register(eventBus);
    }

    public static DamageSource infinityArrowDamage(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(INFINITY_ARROW), attacker);
    }

    public static DamageSource dragonSlayerDamage(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DRAGONSLAYER), attacker);
    }

    public static DamageSource mana(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MANA), attacker);
    }

    public static DamageSource absolute(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ABSOLUTE));
    }

    public static DamageSource absolute(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ABSOLUTE), attacker);
    }

    public static DamageSource trueDamage(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TRUE_DAMAGE));
    }

    public static DamageSource trueDamage(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TRUE_DAMAGE), attacker);
    }

    public static DamageSource soulDrain(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SOUL_DRAIN), attacker);
    }

    public static DamageSource oblivion(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OBLIVION));
    }

    public static DamageSource fate(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(FATE));
    }

    public static DamageSource lightning(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(LIGHTNING), attacker);
    }

    public static DamageSource darkMatter(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DARK_MATTER), attacker);
    }

    public static DamageSource superposition(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SUPERPOSITION));
    }

    public static DamageSource superposition(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SUPERPOSITION), attacker);
    }

    public static DamageSource paradox(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PARADOX), attacker);
    }

    public static DamageSource paradoxReflection(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PARADOX_REFLECTION));
    }

    public static class DamageSourceTrueDamage extends DamageSource {
        public DamageSourceTrueDamage(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TRUE_DAMAGE), entity);
        }
    }

    public static class DamageSourceSoulDrain extends DamageSource {
        public DamageSourceSoulDrain(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SOUL_DRAIN), entity);
        }
    }

    public static class DamageSourceTrueDamageUndef extends DamageSource {
        public DamageSourceTrueDamageUndef(Level level) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TRUE_DAMAGE));
        }
    }

    public static class DamageSourceOblivion extends DamageSource {
        public DamageSourceOblivion(Level level) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OBLIVION));
        }
    }

    public static class DamageSourceFate extends DamageSource {
        public DamageSourceFate(Level level) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(FATE));
        }
    }

    public static class DamageSourceTLightning extends DamageSource {
        public DamageSourceTLightning(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(LIGHTNING), entity);
        }
    }

    public static class DamageSourceDarkMatter extends DamageSource {
        public DamageSourceDarkMatter(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DARK_MATTER), entity);
        }
    }

    public static class DamageSourceMagic extends DamageSource {
        public DamageSourceMagic(Level level, Entity entity) {
            super(level.damageSources().magic().typeHolder(), entity);
        }
    }

    public static class DamageSourceSuperposition extends DamageSource {
        public DamageSourceSuperposition(Level level) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SUPERPOSITION));
        }
    }

    public static class DamageSourceSuperpositionDefined extends DamageSource {
        public DamageSourceSuperpositionDefined(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SUPERPOSITION), entity);
        }
    }

    public static class DamageSourceParadox extends DamageSource {
        public DamageSourceParadox(Level level, Entity entity) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PARADOX), entity);
        }
    }

    public static class DamageSourceParadoxReflection extends DamageSource {
        public DamageSourceParadoxReflection(Level level) {
            super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PARADOX_REFLECTION));
        }
    }

    public static boolean isSuperpositionDamage(DamageSource damageSource) {
        return damageSource.is(SUPERPOSITION);
    }

    public static DamageSource getAppropriateSuperposition(Level level, @Nullable Entity attacker, DamageSource originalSource) {
        if (attacker != null) {
            return superposition(level, attacker);
        } else {
            return superposition(level);
        }
    }
}