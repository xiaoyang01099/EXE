package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.XIAOYANG_010.get(), Xiaoyang010Renderer::new);
		event.registerEntityRenderer(ModEntities.XINGYUN2825.get(), Xingyun2825Renderer::new);
		event.registerEntityRenderer(ModEntities.LIGHTNING_BLOT.get(), RainLightningRenderer::new);
		event.registerEntityRenderer(ModEntities.MANAITA_ARROW.get(), ManaitaArrowRenderer::new);
		event.registerEntityRenderer(ModEntities.SPECTRITE_CRYSTAL.get(), SpectriteCrystalRenderer::new);
		event.registerEntityRenderer(ModEntities.SPECTRITE_WITHER.get(), SpectriteWitherRenderer::new);
		event.registerEntityRenderer(ModEntities.KIND_MIAO.get(), MiaoMiaoRenderer::new);
		event.registerEntityRenderer(ModEntities.CAPYBARA.get(), CapybaraRenderer::new);
		event.registerEntityRenderer(ModEntities.SPOTTED_GARDEN_EEL.get(), SpottedGardenEelRenderer::new);
		//event.registerEntityRenderer(ModEntities.RAINBOW_WITHER_SKULL.get(), RainbowWitherSkullRenderer::new);
	}

}
