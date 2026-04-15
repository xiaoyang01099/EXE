package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
			DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "ex_enigmaticlegacy");

	public static final RegistryObject<SoundEvent> BLADE_SPACE =
			SOUND_EVENTS.register("blade_space",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "blade_space")));

	public static final RegistryObject<SoundEvent> NOTHING =
			SOUND_EVENTS.register("nothing",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "nothing")));

	public static final RegistryObject<SoundEvent> WHAT =
			SOUND_EVENTS.register("what",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "what")));

	public static final RegistryObject<SoundEvent> AAAAA =
			SOUND_EVENTS.register("aaaaa",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "aaaaa")));

	public static final RegistryObject<SoundEvent> KILL =
			SOUND_EVENTS.register("kill",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "kill")));

	public static final RegistryObject<SoundEvent> SCRAY =
			SOUND_EVENTS.register("scray",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "scray")));

	public static final RegistryObject<SoundEvent> MEOW =
			SOUND_EVENTS.register("meow",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "meow")));

	public static final RegistryObject<SoundEvent> CUTE =
			SOUND_EVENTS.register("cute",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "cute")));

	public static final RegistryObject<SoundEvent> HUNT_DOWN =
			SOUND_EVENTS.register("hunt_down",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "hunt_down")));

	public static final RegistryObject<SoundEvent> GOLDEN_LAUREL =
			SOUND_EVENTS.register("golden_laurel",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "golden_laurel")));

	public static final RegistryObject<SoundEvent> FLOWEY_LAUGH =
			SOUND_EVENTS.register("flowey_laugh",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "flowey_laugh")));

	public static final RegistryObject<SoundEvent> AQUA_SWORD =
			SOUND_EVENTS.register("aqua_sword",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "aqua_sword")));

	public static final RegistryObject<SoundEvent> BOARD_CUBE =
			SOUND_EVENTS.register("board_cube",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "board_cube")));

	public static final RegistryObject<SoundEvent> HORN_PLENTY =
			SOUND_EVENTS.register("horn_plenty_using",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "horn_plenty_using")));

	public static final RegistryObject<SoundEvent> WASTELAYER =
			SOUND_EVENTS.register("wastelayer",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "wastelayer")));

	public static final RegistryObject<SoundEvent> SONG_OF_THE_ABYSS =
			SOUND_EVENTS.register("song_of_the_abyss",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ex_enigmaticlegacy", "song_of_the_abyss")));

	public static void register() {
		SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}