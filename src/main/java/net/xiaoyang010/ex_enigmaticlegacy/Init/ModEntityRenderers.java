package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.NatureBoltEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.SacabambaspisEntity;


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
		event.registerEntityRenderer(ModEntities.CLONE_ENTITY.get(), CloneEntityRenderer::new);
		event.registerEntityRenderer(ModEntities.SEA_SERPENT.get(), SeaSerpentRender::new);
		event.registerEntityRenderer(ModEntities.SACABAMBASPIS.get(), SacabambaspisRender::new);
		event.registerEntityRenderer(ModEntities.RIDEABLE_PEARL_ENTITY.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		event.registerEntityRenderer(ModEntities.NATURE_BOLT.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		//event.registerEntityRenderer(ModEntities.RAINBOW_WITHER_SKULL.get(), RainbowWitherSkullRenderer::new);
	}

}
