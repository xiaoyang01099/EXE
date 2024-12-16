package net.xiaoyang010.ex_enigmaticlegacy.Client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Block.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModParticleTypes;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.AsgardandelionParticle;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.DandelionFluffParticle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSideHandler {


    //花朵方块注册渲染器
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Asgardandelion.registerRenderLayer();
        Soarleander.registerRenderLayer();
        OrechidEndium.registerRenderLayer();
        FluffyDandelionBlock.registerRenderLayer();
        GenEnergydandron.registerRenderLayer();
        FloweyBlock.registerRenderLayer();
        BelieverBlock.registerRenderLayer();
        WitchOpoodBlock.registerRenderLayer();
        DaybloomBlock.registerRenderLayer();
        NightshadeBlock.registerRenderLayer();
        AstralKillop.registerRenderLayer();
        CelestialHolinessTransmuter.registerRenderLayer();
    }





    //粒子效果注册
    @SubscribeEvent
    public static void registerParticleFactories(final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.DANDELION_FLUFF.get(),
                DandelionFluffParticle.Factory::new);

        Minecraft.getInstance().particleEngine.register(ModParticleTypes.ASGARDANDELION.get(),
                AsgardandelionParticle.Factory::new);

    }
}