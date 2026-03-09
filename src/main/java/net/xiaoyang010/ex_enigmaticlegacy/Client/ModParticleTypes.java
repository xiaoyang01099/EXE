package net.xiaoyang010.ex_enigmaticlegacy.Client;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "ex_enigmaticlegacy");
    public static final RegistryObject<SimpleParticleType> DANDELION_FLUFF = PARTICLE_TYPES.register("dandelion_fluff", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ASGARDANDELION = PARTICLE_TYPES.register("asgardandelion", () -> new SimpleParticleType(true));



    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
