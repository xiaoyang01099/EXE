package net.xiaoyang010.ex_enigmaticlegacy;

import com.integral.enigmaticlegacy.proxy.CommonProxy;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Event.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Event.TooltipEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Util.SkeletonGoalReplacer;
import net.xiaoyang010.ex_enigmaticlegacy.api.emc.NoEMCCommandInterceptor;
import net.xiaoyang010.ex_enigmaticlegacy.api.emc.NoEMCEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	public static CommonProxy proxy;

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
		ModRecipes.SERIALIZERS.register(bus);
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

		if (ModList.get().isLoaded("projecte")) {
			MinecraftForge.EVENT_BUS.register(NoEMCEventHandler.class);
			MinecraftForge.EVENT_BUS.register(NoEMCCommandInterceptor.class);
		}

		MinecraftForge.EVENT_BUS.register(new RelicsEventHandler());
		MinecraftForge.EVENT_BUS.register(new TooltipEvent());
		MinecraftForge.EVENT_BUS.register(new SpectatorModeHandler());

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		bus.addListener(this::commonSetup);
		bus.addListener(this::clientSetup);
		bus.addListener(this::onCommonSetup);
		bus.addListener(this::kpo);
	}

	public void onCommonSetup(FMLCommonSetupEvent event) {
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		AvaritiaShaders.init();
	}

	public static ResourceLocation path(String path) {
		return new ResourceLocation("ex_enigmaticlegacy", path);
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
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


