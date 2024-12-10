
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {
	public static Map<ResourceLocation, SoundEvent> REGISTRY = new HashMap<>();
	static {
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "nothing"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "nothing")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "what"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "what")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "aaaaa"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "aaaaa")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "kill"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "kill")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "scray"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "scray")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "meow"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "meow")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "cute"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "cute")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "hunt_down"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "hunt_down")));
		REGISTRY.put(new ResourceLocation("ex_enigmaticlegacy", "flowey_laugh"), new SoundEvent(new ResourceLocation("ex_enigmaticlegacy", "flowey_laugh")));
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (Map.Entry<ResourceLocation, SoundEvent> sound : REGISTRY.entrySet())
			event.getRegistry().register(sound.getValue().setRegistryName(sound.getKey()));
	}
}
