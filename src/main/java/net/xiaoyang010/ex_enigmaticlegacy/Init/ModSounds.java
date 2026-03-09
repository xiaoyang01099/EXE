
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
	public static final Map<ResourceLocation, SoundEvent> REGISTRY = new HashMap<>();
	public static final ResourceLocation BLADE_SPACE_ID = new ResourceLocation("ex_enigmaticlegacy", "blade_space");
	public static final ResourceLocation NOTHING_ID = new ResourceLocation("ex_enigmaticlegacy", "nothing");
	public static final ResourceLocation WHAT_ID = new ResourceLocation("ex_enigmaticlegacy", "what");
	public static final ResourceLocation AAAAA_ID = new ResourceLocation("ex_enigmaticlegacy", "aaaaa");
	public static final ResourceLocation KILL_ID = new ResourceLocation("ex_enigmaticlegacy", "kill");
	public static final ResourceLocation SCRAY_ID = new ResourceLocation("ex_enigmaticlegacy", "scray");
	public static final ResourceLocation MEOW_ID = new ResourceLocation("ex_enigmaticlegacy", "meow");
	public static final ResourceLocation CUTE_ID = new ResourceLocation("ex_enigmaticlegacy", "cute");
	public static final ResourceLocation HUNT_DOWN_ID = new ResourceLocation("ex_enigmaticlegacy", "hunt_down");
	public static final ResourceLocation GOLDEN_LAUREL_ID = new ResourceLocation("ex_enigmaticlegacy", "golden_laurel");
	public static final ResourceLocation FLOWEY_LAUGH_ID = new ResourceLocation("ex_enigmaticlegacy", "flowey_laugh");
	public static final ResourceLocation CAPYBARA_AMBIENT_1_ID = new ResourceLocation("ex_enigmaticlegacy", "capybara_ambient_1");
	public static final ResourceLocation CAPYBARA_AMBIENT_2_ID = new ResourceLocation("ex_enigmaticlegacy", "capybara_ambient_2");
	public static final ResourceLocation CAPYBARA_HURT_ID = new ResourceLocation("ex_enigmaticlegacy", "capybara_hurt");
	public static final ResourceLocation CAPYBARA_DEATH_ID = new ResourceLocation("ex_enigmaticlegacy", "capybara_death");
	public static final ResourceLocation AQUA_SWORD_ID = new ResourceLocation("ex_enigmaticlegacy", "aqua_sword");
	public static final ResourceLocation BOARD_CUBE_ID = new ResourceLocation("ex_enigmaticlegacy", "board_cube");
	public static final ResourceLocation HORN_PLENTY_ID = new ResourceLocation("ex_enigmaticlegacy", "horn_plenty_using");
	public static final ResourceLocation WASTELAYER_ID = new ResourceLocation("ex_enigmaticlegacy", "wastelayer");
	public static final ResourceLocation SONG_OF_THE_ABYSS_ID = new ResourceLocation("ex_enigmaticlegacy", "song_of_the_abyss");



	public static final SoundEvent CAPYBARA_AMBIENT_1 = new SoundEvent(CAPYBARA_AMBIENT_1_ID);
	public static final SoundEvent CAPYBARA_AMBIENT_2 = new SoundEvent(CAPYBARA_AMBIENT_2_ID);
	public static final SoundEvent CAPYBARA_HURT = new SoundEvent(CAPYBARA_HURT_ID);
	public static final SoundEvent CAPYBARA_DEATH = new SoundEvent(CAPYBARA_DEATH_ID);
	public static final SoundEvent BLADE_SPACE = new SoundEvent(BLADE_SPACE_ID);
	public static final SoundEvent AQUA_SWORD = new SoundEvent(AQUA_SWORD_ID);
	public static final SoundEvent BOARD_CUBE = new SoundEvent(BOARD_CUBE_ID);
	public static final SoundEvent NOTHING = new SoundEvent(NOTHING_ID);
	public static final SoundEvent WHAT = new SoundEvent(WHAT_ID);
	public static final SoundEvent AAAAA = new SoundEvent(AAAAA_ID);
	public static final SoundEvent KILL = new SoundEvent(KILL_ID);
	public static final SoundEvent SCRAY = new SoundEvent(SCRAY_ID);
	public static final SoundEvent MEOW = new SoundEvent(MEOW_ID);
	public static final SoundEvent CUTE = new SoundEvent(CUTE_ID);
	public static final SoundEvent HUNT_DOWN = new SoundEvent(HUNT_DOWN_ID);
	public static final SoundEvent FLOWEY_LAUGH = new SoundEvent(FLOWEY_LAUGH_ID);
	public static final SoundEvent GOLDEN_LAUREL = new SoundEvent(GOLDEN_LAUREL_ID);
	public static final SoundEvent HORN_PLENTY = new SoundEvent(HORN_PLENTY_ID);
	public static final SoundEvent WASTELAYER = new SoundEvent(WASTELAYER_ID);
	public static final SoundEvent SONG_OF_THE_ABYSS = new SoundEvent(SONG_OF_THE_ABYSS_ID);

	static {
		REGISTRY.put(WASTELAYER_ID, WASTELAYER);
		REGISTRY.put(SONG_OF_THE_ABYSS_ID, SONG_OF_THE_ABYSS);
		REGISTRY.put(HORN_PLENTY_ID, HORN_PLENTY);
		REGISTRY.put(BOARD_CUBE_ID, BOARD_CUBE);
		REGISTRY.put(AQUA_SWORD_ID, AQUA_SWORD);
		REGISTRY.put(BLADE_SPACE_ID, BLADE_SPACE);
		REGISTRY.put(CAPYBARA_AMBIENT_1_ID, CAPYBARA_AMBIENT_1);
		REGISTRY.put(CAPYBARA_AMBIENT_2_ID, CAPYBARA_AMBIENT_2);
		REGISTRY.put(CAPYBARA_HURT_ID, CAPYBARA_HURT);
		REGISTRY.put(CAPYBARA_DEATH_ID, CAPYBARA_DEATH);
		REGISTRY.put(NOTHING_ID, NOTHING);
		REGISTRY.put(WHAT_ID, WHAT);
		REGISTRY.put(AAAAA_ID, AAAAA);
		REGISTRY.put(KILL_ID, KILL);
		REGISTRY.put(SCRAY_ID, SCRAY);
		REGISTRY.put(MEOW_ID, MEOW);
		REGISTRY.put(CUTE_ID, CUTE);
		REGISTRY.put(HUNT_DOWN_ID, HUNT_DOWN);
		REGISTRY.put(FLOWEY_LAUGH_ID, FLOWEY_LAUGH);
		REGISTRY.put(GOLDEN_LAUREL_ID, GOLDEN_LAUREL);
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (Map.Entry<ResourceLocation, SoundEvent> sound : REGISTRY.entrySet()) {
			event.getRegistry().register(sound.getValue().setRegistryName(sound.getKey()));
		}
	}
}