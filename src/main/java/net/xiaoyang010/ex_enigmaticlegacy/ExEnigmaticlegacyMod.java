package net.xiaoyang010.ex_enigmaticlegacy;

import com.integral.enigmaticlegacy.proxy.CommonProxy;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.InfinityChestRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.InfinityPotatoRender;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.InfinityChestModel;
/*import net.xiaoyang010.ex_enigmaticlegacy.entity.biological.Sacabambaspis;*/
import net.xiaoyang010.ex_enigmaticlegacy.Event.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;



import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.xiaoyang010.ex_enigmaticlegacy.Item.OmegaCore;
import net.xiaoyang010.ex_enigmaticlegacy.Util.TooltipColorEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/*import static net.xiaoyang010.ex_enigmaticlegacy.init.ModEntities.SACABAMBASPIS;*/


@Mod(ExEnigmaticlegacyMod.MODID)
public class ExEnigmaticlegacyMod {
	public static final Logger LOGGER = LogManager.getLogger(ExEnigmaticlegacyMod.class);
	public static final String MODID = "ex_enigmaticlegacy";
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public ExEnigmaticlegacyMod() {
		ModTabs.load();
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

		MinecraftForge.EVENT_BUS.register(new ArmorEffectEvent());
		MinecraftForge.EVENT_BUS.register(new StarEventHandlers());
		MinecraftForge.EVENT_BUS.register(new ArmorProtectionEvent());
		MinecraftForge.EVENT_BUS.register(new OmegaCoreEffectHandler());
		MinecraftForge.EVENT_BUS.register(new TimeStopEffectHandler());
		MinecraftForge.EVENT_BUS.register(new OmegaCore.TimeStopHandler());
		MinecraftForge.EVENT_BUS.register(new DamageReductionEventHandler());
		MinecraftForge.EVENT_BUS.register(new CreeperBehaviorHandler());
		MinecraftForge.EVENT_BUS.register(new BedrockBreakEventHandler());
		MinecraftForge.EVENT_BUS.register(new TooltipColorEvent());
		/*MinecraftForge.EVENT_BUS.register(new WaterClickEvent());*/


		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		/*bus.addListener(this::registerAttributes);*/
		bus.addListener(this::commonSetup);
		bus.addListener(this::clientSetup); // 注册客户端设置
	}

    private void registerRecipeTypes() {
	}

	public static CommonProxy proxy;

	private void commonSetup(final FMLCommonSetupEvent event) {
	}

	private void setup(final FMLCommonSetupEvent event) {
		registerCustomEMC();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		// 客户端相关设置...
	}
	/*private void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(SACABAMBASPIS.get(), Sacabambaspis.createAttributes().build());
	}*/

	private void clientSetup(final FMLClientSetupEvent event) {
		// 为自定义玻璃设置渲染层
		ItemBlockRenderTypes.setRenderLayer(ModBlockss.INFINITYGlASS.get(), RenderType.translucent());
		event.enqueueWork(() ->{
			/*EntityRenderers.register(SACABAMBASPIS.get(), SacabambaspisRenderer::new);*/

		});

		BlockEntityRenderers.register(ModBlockEntities.INFINITY_CHEST.get(), InfinityChestRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.INFINITY_POTATO.get(), InfinityPotatoRender::new);

	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		// 注册模型层定义
		event.registerLayerDefinition(
				new ModelLayerLocation(new ResourceLocation(MODID, "infinity_chest"), "main"),
				InfinityChestModel::createBodyLayer
		);
	}

	public void registerCustomEMC() {
		NSSItem endlessCake = NSSItem.createItem(new ResourceLocation("ex_enigmaticlegacy:endless_cake"));
		CustomEMCRegistration emcRegistration = new CustomEMCRegistration(endlessCake, 100245);
		APICustomEMCMapper.INSTANCE.registerCustomEMC("ex_enigmaticlegacy", emcRegistration);
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


