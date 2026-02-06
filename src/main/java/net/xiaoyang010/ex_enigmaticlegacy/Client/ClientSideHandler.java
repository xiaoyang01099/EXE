package net.xiaoyang010.ex_enigmaticlegacy.Client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Block.FluffyDandelionBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Block.custom.CustomSaplingBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.AsgardandelionParticle;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.DandelionFluffParticle;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating.EMCFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Hybrid.AquaticAnglerNarcissus;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Functional.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.GearFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.TrinarySynthesis;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Hybrid.RuneFlower;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSideHandler {

    public static final ResourceLocation INFINITY_CHEST_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entity/chest/infinity_chest");
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "infinity_chest"), "main");
    public static final ResourceLocation SPECTRITE_CHEST_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entity/chest/spectrite_chest");
    public static final ModelLayerLocation SPECTRITE_CHEST = new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "spectrite_chest"), "main");

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event){
        if (event.getAtlas().location().equals(Sheets.CHEST_SHEET)) {
            event.addSprite(INFINITY_CHEST_TEXTURE);
        }

        if (event.getAtlas().location().equals(Sheets.CHEST_SHEET)) {
            event.addSprite(SPECTRITE_CHEST_TEXTURE);
        }
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

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

        if (ModList.get().isLoaded("create")){
        TrinarySynthesis.registerRenderLayer();
        GearFlower.registerRenderLayer();
        }
    }


    //粒子效果注册
    @SubscribeEvent
    public static void registerParticleFactories(final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(
                ModParticleTypes.DANDELION_FLUFF.get(),
                DandelionFluffParticle.Factory::new);

        Minecraft.getInstance().particleEngine.register(
                ModParticleTypes.ASGARDANDELION.get(),
                AsgardandelionParticle.Factory::new);
    }
}