package net.xiaoyang010.ex_enigmaticlegacy.Client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Block.FluffyDandelionBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.AsgardandelionParticle;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.DandelionFluffParticle;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.InfinityChestRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModParticleTypes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSideHandler {
    public static final ResourceLocation INFINITY_CHEST_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entity/chest/infinity_chest");
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "infinity_chest"), "main");

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event){
        if (event.getAtlas().location().equals(Sheets.CHEST_SHEET)) {
            event.addSprite(INFINITY_CHEST_TEXTURE);
        }
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(INFINITY_CHEST, InfinityChestRenderer::createSingleBodyLayer);
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