package net.xiaoyang010.ex_enigmaticlegacy.Init;

import morph.avaritia.client.render.entity.GapingVoidEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block.SpectriteCrystalRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render.AlphirinePortalRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render.RenderAdvencedSpark;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.ADVANCED_SPARK.get(), RenderAdvencedSpark::new);
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
		event.registerEntityRenderer(ModEntities.ALPHIRINE_PORTAL.get(), AlphirinePortalRenderer::new);
		event.registerEntityRenderer(ModEntities.CONTINUUM_BOMB.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		event.registerEntityRenderer(ModEntities.RIDEABLE_PEARL_ENTITY.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		event.registerEntityRenderer(ModEntities.NATURE_BOLT.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		event.registerEntityRenderer(ModEntities.ENTITY_SWORD.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.ENTITY_SEED.get(), (context) -> new ThrownItemRenderer<>(context, 1.0F, true));
		event.registerEntityRenderer(ModEntities.SOUL_ENERGY.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.THUNDERPEAL_ORB.get(), ThunderpealOrbRenderer::new);
		event.registerEntityRenderer(ModEntities.LUNAR_FLARE.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.BABYLON_WEAPON_SS.get(), RenderBabylonWeaponSS::new);
		event.registerEntityRenderer(ModEntities.RAGEOUS_MISSILE.get(), RenderRageousMissile::new);
		event.registerEntityRenderer(ModEntities.CHAOTIC_ORB.get(), RenderChaoticOrb::new);
		event.registerEntityRenderer(ModEntities.SHINY_ENERGY.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.CRIMSON_ORB.get(), RenderCrimsonOrb::new);
		event.registerEntityRenderer(ModEntities.DARK_MATTER_ORB.get(), RenderEldritchOrb::new);
		event.registerEntityRenderer(ModEntities.MANA_VINE_BALL.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.ENTITY_SLASH.get(), EntityNullRender::new);
		event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), GapingVoidEntityRenderer::new);
		event.registerEntityRenderer(ModEntities.CURSED_MANA_BURST.get(), NoopRenderer::new);
//		event.registerEntityRenderer(ModEntities.RAINBOW_WITHER_SKULL.get(), RainbowWitherSkullRenderer::new);
	}

}
