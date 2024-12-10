package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent evt) {
        DataGenerator generator = evt.getGenerator();
        if (evt.includeServer()) {
            generator.addProvider(new RecipeProvider(generator));
        }

        if (evt.includeClient()) {
        }
    }
}
