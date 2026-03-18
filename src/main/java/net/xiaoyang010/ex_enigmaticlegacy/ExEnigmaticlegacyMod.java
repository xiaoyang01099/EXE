package net.xiaoyang010.ex_enigmaticlegacy;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.EMCEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua.YuhuaEntityRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua.YuHuaShaders;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.PolychromeCollapsePrismTile;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Event.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.ContinuumItem;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;
import net.xiaoyang010.ex_enigmaticlegacy.Network.CommonProxy;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Event.TooltipEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.NoEMCCommandInterceptor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.EventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Event.CrissaegrimEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectManager;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;
import net.xiaoyang010.ex_enigmaticlegacy.Client.BloodBarHud;
import net.xiaoyang010.ex_enigmaticlegacy.Util.WaveNameData;
import net.xiaoyang010.ex_enigmaticlegacy.Util.WaveNameTooltipComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(ExEnigmaticlegacyMod.MODID)
public class ExEnigmaticlegacyMod {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "ex_enigmaticlegacy";
	public static boolean isEx = false;
	public static CommonProxy proxy = DistExecutor.unsafeRunForDist(
			() -> ClientProxy::new,
			() -> CommonProxy::new
	);

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	public ExEnigmaticlegacyMod() {
		ModTabs.load();
		isEx = ModList.get().isLoaded("enigmaticlegacy");
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModBlockss.REGISTRY.register(bus);
		ModItems.REGISTRY.register(bus);
		ModEntities.REGISTRY.register(bus);
		ModBlockEntities.REGISTRY.register(bus);
		ModWeapons.REGISTRY.register(bus);
		ModArmors.REGISTRY.register(bus);
		ModFoods.ITEMS.register(bus);
		ModEffects.registerEffects();
		ModParticleTypes.register(bus);
		ModRecipes.register(bus);
		ModRarities.register();
		ModFeatures.REGISTRY.register(bus);
		ModBiomes.REGISTRY.register(bus);
		ModEnchantments.REGISTRY.register(bus);
		ModIntegrationItems.REGISTRY.register(bus);
		ModMaterials.REGISTRY.register(bus);
		ConfigHandler.register();

		ModIntegrationFlowers.BLOCK_REGISTRY.register(bus);
		ModIntegrationFlowers.BLOCK_ENTITY_REGISTRY.register(bus);
		ModIntegrationFlowers.BLOCK_ITEM_REGISTRY.register(bus);

		MinecraftForge.EVENT_BUS.register(new CurseAbilityHandler());
		MinecraftForge.EVENT_BUS.register(new RelicsEventHandler());
		MinecraftForge.EVENT_BUS.register(new TooltipEvent());
		MinecraftForge.EVENT_BUS.register(new SpectatorModeHandler());
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
		MinecraftForge.EVENT_BUS.register(new CrissaegrimEventHandler());

		if (ModList.get().isLoaded("projecte")) {
			MinecraftForge.EVENT_BUS.register(EMCEventHandler.class);
			MinecraftForge.EVENT_BUS.register(NoEMCCommandInterceptor.class);
		}

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			MinecraftForge.EVENT_BUS.register(new EffectManager());
			FXHandler.registerEffects();
		});

		bus.addListener(this::commonSetup);
		bus.addListener(this::clientSetup);
		bus.addListener(this::kpo);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		AvaritiaShaders.init();
		YuHuaShaders.init();
		YuhuaEntityRenderer.register();
		event.enqueueWork(() -> {
			ForgeRegistries.ITEMS.forEach(ContinuumItem::addPossibleItem);
			ForgeRegistries.BLOCKS.forEach(ContinuumItem::addPossibleItem);

			PatchouliAPI.get().registerMultiblock(
					new ResourceLocation("ex_enigmaticlegacy", "polychrome_collapse_prism"),
					PolychromeCollapsePrismTile.MULTIBLOCK.get()
			);
			PatchouliAPI.get().registerMultiblock(
					new ResourceLocation("ex_enigmaticlegacy", "starlit_sanctum"),
					StarlitSanctumTile.MULTIBLOCK.get()
			);

			NetworkHandler.register();
		});

		OverlayRegistry.registerOverlayAbove(
				ForgeIngameGui.FOOD_LEVEL_ELEMENT,
				"blood_bar",
				BloodBarHud.INSTANCE
		);
	}

	public static ResourceLocation path(String path) {
		return new ResourceLocation("ex_enigmaticlegacy", path);
	}

	private void kpo(final FMLClientSetupEvent event) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			KeybindHandler.registerKeybinds(event);
		});
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() ->{
			ItemBlockRenderTypes.setRenderLayer(ModBlockss.INFINITYGlASS.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(ModBlockss.PAGED_CHEST.get(), RenderType.cutoutMipped());
		});

		MinecraftForgeClient.registerTooltipComponentFactory(
				WaveNameData.class,
				WaveNameTooltipComponent::new
		);
	}
}


