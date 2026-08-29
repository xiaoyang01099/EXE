package org.xiaoyang.ex_enigmaticlegacy.Init;

import com.yuo.endless.client.render.GapingVoidRender;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.*;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicArrowRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicBowParticleEffectRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ColorfulEntityRender;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.RailgunBeamEntityRender;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.HeavenlyThunderRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.TridentLightningStrikeRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.TridentPlusEntityRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.block.*;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others.*;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.tile.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.render.*;
import org.xiaoyang.ex_enigmaticlegacy.api.test.NebulaBowArrowAroundEffectRenderer;
import org.xiaoyang.ex_enigmaticlegacy.api.test.NebulaBowArrowLowRenderer;
import org.xiaoyang.ex_enigmaticlegacy.api.test.ShockwaveRenderer;



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
		event.registerEntityRenderer(ModEntities.KIND_MIAO.get(), MiaoMiaoRenderer::new);
		event.registerEntityRenderer(ModEntities.GIUL_BSEN.get(), GiulBsenRenderer::new);
		event.registerEntityRenderer(ModEntities.CLONE_ENTITY.get(), CloneEntityRenderer::new);
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
		event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), GapingVoidRender::new);
		event.registerEntityRenderer(ModEntities.SLIME_CANNON_BALL.get(), RendererSlimeCannonBall::new);
		event.registerEntityRenderer(ModEntities.SLING_BULLET.get(), RenderSlingBullet::new);
		event.registerEntityRenderer(ModEntities.INFINITY_ARROW_LEVEL_ENTITY.get(), EntityInfinityArrowLevelRenderer::new);
		event.registerEntityRenderer(ModEntities.SHOCK_WAVE.get(), ShockwaveRenderer::new);
		event.registerEntityRenderer(ModEntities.NEBULA_ARROW.get(), NebulaBowArrowLowRenderer::new);
		event.registerEntityRenderer(ModEntities.NEBULA_ARROW_AROUND.get(), NebulaBowArrowAroundEffectRenderer::new);
		event.registerEntityRenderer(ModEntities.FLY_SWORD_ENTITY.get(), FlySwordEntityRender::new);
		event.registerEntityRenderer(ModEntities.RAILGUN_BEAM_ENTITY.get(), RailgunBeamEntityRender::new);
		event.registerEntityRenderer(ModEntities.COLORFUL_COIN_ENTITY.get(), ColorfulEntityRender::new);
		event.registerEntityRenderer(ModEntities.MAGIC_ARROW_ENTITY.get(), MagicArrowRenderer::new);
		event.registerEntityRenderer(ModEntities.MAGIC_BOW_PARTICLE_EFFECT_ENTITY.get(), MagicBowParticleEffectRenderer::new);
		event.registerEntityRenderer(ModEntities.SWORD_AURA_ENTITY.get(), SwordAuraRenderer::new);
		event.registerEntityRenderer(ModEntities.DIMENSION_SLASH_DOMAIN.get(), DimensionSlashDomainRenderer::new);
		event.registerEntityRenderer(ModEntities.DIMENSION_SLASH_STRIKE.get(), DimensionSlashStrikeRenderer::new);
		event.registerEntityRenderer(ModEntities.BATTO_SLASH.get(), BattoSlashRenderer::new);
		event.registerEntityRenderer(ModEntities.TRIDENT_PLUS_ENTITY.get(), TridentPlusEntityRenderer::new);
		event.registerEntityRenderer(ModEntities.TRIDENT_LIGHTNING_STRIKE.get(), TridentLightningStrikeRenderer::new);
		event.registerEntityRenderer(ModEntities.HEAVENLY_THUNDER.get(), HeavenlyThunderRenderer::new);
		event.registerEntityRenderer(ModEntities.EXCALIBUR_CHARGE.get(), ExcaliburChargeRenderer::new);
		event.registerEntityRenderer(ModEntities.EXCALIBUR_SWORD_WAVE.get(), ExcaliburSwordWaveRenderer::new);
	}
}
