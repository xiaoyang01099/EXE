package net.xiaoyang010.ex_enigmaticlegacy;

import com.integral.enigmaticlegacy.proxy.CommonProxy;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.MithrillMultiTool;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.TerraShovel;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render.*;
import net.xiaoyang010.ex_enigmaticlegacy.Event.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.TooltipColorEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(ExEnigmaticlegacyMod.MODID)
public class ExEnigmaticlegacyMod {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "ex_enigmaticlegacy";
	private static final String PROTOCOL_VERSION = "1";
	public static boolean isEx = false;
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

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
		ModMaterials.REGISTRY.register(bus);
		ModFoods.ITEMS.register(bus);
		ModEffects.registerEffects();
		ModParticleTypes.register(bus);
		ModRecipes.SERIALIZERS.register(bus);
		ModRarities.register();
		ModFeatures.REGISTRY.register(bus);
		ModBiomes.REGISTRY.register(bus);
		ModEnchantments.REGISTRY.register(bus);
		ModIntegrationItems.REGISTRY.register(bus);
		ConfigHandler.register();



		ModIntegrationFlowers.BLOCK_REGISTRY.register(bus);
		ModIntegrationFlowers.BLOCK_ENTITY_REGISTRY.register(bus);
		ModIntegrationFlowers.BLOCK_ITEM_REGISTRY.register(bus);
		//AvaritiaShaders.init();

		MinecraftForge.EVENT_BUS.register(new OmegaCoreEffectHandler());
		MinecraftForge.EVENT_BUS.register(new TooltipColorEvent());
		MinecraftForge.EVENT_BUS.register(new SpectatorModeHandler());


		//ModelLoaderRegistry.registerLoader(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "cosmic"), new CosmicModelLoader());

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		bus.addListener(this::commonSetup);
		bus.addListener(this::clientSetup);
		bus.addListener(this::onCommonSetup);
		bus.addListener(this::kpo);
	}

	public static CommonProxy proxy;

	public void onCommonSetup(FMLCommonSetupEvent event) {
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ItemProperties.register(ModWeapons.TERRA_BOW.get(),
					new ResourceLocation("pull"),
					(stack, level, entity, seed) -> {
						if (entity == null) return 0.0F;
						return entity.getUseItem() != stack ? 0.0F :
								(float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
					});

			ItemProperties.register(ModWeapons.TERRA_BOW.get(),
					new ResourceLocation("pulling"),
					(stack, level, entity, seed) ->
							entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

			ItemProperties.register(
					ModItems.MITHRILL_MULTI_TOOL.get(),
					new ResourceLocation("ex_enigmaticlegacy", "enabled"),
					(stack, world, entity, seed) -> MithrillMultiTool.isEnabled(stack) ? 1.0F : 0.0F
			);

			ItemProperties.register(
					ModItems.TERRA_SHOVEL.get(),
					new ResourceLocation("ex_enigmaticlegacy", "enabled"),
					(stack, world, entity, seed) -> TerraShovel.isEnabled(stack) ? 1.0F : 0.0F
			);
		});
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(NetworkHandler::register);
	}

	private void kpo(final FMLClientSetupEvent event) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			KeybindHandler.registerKeybinds(event);
		});
	}

	private void clientSetup(final FMLClientSetupEvent event) {

		ItemBlockRenderTypes.setRenderLayer(ModBlockss.INFINITYGlASS.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(ModBlockss.PAGED_CHEST.get(), RenderType.cutoutMipped());
		event.enqueueWork(() ->{
		});

		BlockEntityRenderers.register(ModBlockEntities.GAME_BOARD_TILE.get(), RenderTileGameBoard::new);
		BlockEntityRenderers.register(ModBlockEntities.BOARD_FATE_TILE.get(), RenderTileBoardFate::new);
		BlockEntityRenderers.register(ModBlockEntities.FULL_ALTAR_TILE.get(), FullAltarRender::new);
		BlockEntityRenderers.register(ModBlockEntities.MANA_CONTAINER_TILE.get(), ManaContainerRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.MANA_CRYSTAL_TILE.get(), RenderTileManaCrystalCube::new);
		BlockEntityRenderers.register(ModBlockEntities.PAGED_CHEST.get(), PagedChestRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.INFINITY_CHEST.get(), InfinityChestRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.INFINITY_POTATO.get(), InfinityPotatoRender::new);
		BlockEntityRenderers.register(ModBlockEntities.INFINITY_SPREADER.get(), InfinityGaiaSpreaderRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.RAINBOW_TABLE_TILE.get(), RainbowTableRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(), RenderTileNidavellirForge::new);
		BlockEntityRenderers.register(ModBlockEntities.SPECTRITE_CHEST_TILE.get(), SpectriteChestRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.MANA_CHARGER_TILE.get(), RenderTileManaCharger::new);
		BlockEntityRenderers.register(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(),PolychromeCollapsePrismRenderer::new);
	}

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder,
											 Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}
	public static ResourceLocation getRL(String s){
		return new ResourceLocation(MODID,s);
	}
}


