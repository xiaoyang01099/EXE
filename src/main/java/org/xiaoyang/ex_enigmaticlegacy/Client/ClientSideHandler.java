package org.xiaoyang.ex_enigmaticlegacy.Client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.xiaoyang.ex_enigmaticlegacy.Block.custom.CustomSaplingBlock;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.AsgardandelionParticle;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.RainbowParticle;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.AquaticAnglerNarcissus;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.RuneFlower;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModParticleTypes;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSideHandler {
    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    //花朵方块注册渲染器
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Soarleander.registerRenderLayer();
        OrechidEndium.registerRenderLayer();
        GenEnergydandron.registerRenderLayer();
        FloweyBlock.registerRenderLayer();
        BelieverBlock.registerRenderLayer();
        WitchOpoodBlock.registerRenderLayer();
        DaybloomBlock.registerRenderLayer();
        NightshadeBlock.registerRenderLayer();
        AstralKillop.registerRenderLayer();
        KillingBerry.registerRenderLayer();
        DarkNightGrass.registerRenderLayer();
        FrostLotusFlower.registerRenderLayer();
        Lycorisradiata.registerRenderLayer();
        FrostBlossomBlock.registerRenderLayer();
        CustomSaplingBlock.registerRenderLayer();
        MingXianLanBlock.registerRenderLayer();
        RainbowGeneratingFlowerBlock.registerRenderLayer();
        BlazingOrchidFlowerBlock.registerRenderLayer();
        StreetLightFlowerBlock.registerRenderLayer();
        Vacuity.registerRenderLayer();
        YushouClover.registerRenderLayer();
        CurseThistle.registerRenderLayer();
        EnderLavender.registerRenderLayer();
        AureaAmicitiaCarnation.registerRenderLayer();
        Catnip.registerRenderLayer();
        MusicalOrchid.registerRenderLayer();
        AncientAlphirine.registerRenderLayer();
        Dictarius.registerRenderLayer();
        AquaticAnglerNarcissus.registerRenderLayer();
        EvilForge.registerRenderLayer();
        EtheriumForge.registerRenderLayer();
        ArdentAzarcissus.registerRenderLayer();
        RuneFlower.registerRenderLayer();

        //联动渲染注册
        if (ModList.get().isLoaded("projecte")) {
            AlchemySunflower.registerRenderLayer();
            AlchemyAzalea.registerRenderLayer();
            CelestialBlueHyacinth.registerRenderLayer();
            EMCFlower.registerRenderLayer();
        }
    }


    //粒子效果注册
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticleTypes.ASGARDANDELION.get(),
                AsgardandelionParticle.Factory::new
        );

        event.registerSpriteSet(
                ModParticleTypes.RAINBOW.get(),
                RainbowParticle.Provider::new
        );
    }
}