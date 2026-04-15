package org.xiaoyang.ex_enigmaticlegacy;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.ef.EffectManager;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.ef.FXHandler;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.EventHandler;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.Factory.ProjecteFactory;
import org.xiaoyang.ex_enigmaticlegacy.Event.CrissaegrimEventHandler;
import org.xiaoyang.ex_enigmaticlegacy.Event.KeybindHandler;
import org.xiaoyang.ex_enigmaticlegacy.Event.RelicsEventHandler;
import org.xiaoyang.ex_enigmaticlegacy.Event.SpectatorModeHandler;
import org.xiaoyang.ex_enigmaticlegacy.Font.WaveNameData;
import org.xiaoyang.ex_enigmaticlegacy.Font.WaveNameTooltipComponent;
import org.xiaoyang.ex_enigmaticlegacy.Init.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.ContinuumItem;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Util.AnimatedChestTexture;
import org.xiaoyang.ex_enigmaticlegacy.api.emc.NoEMCCommandInterceptor;
import org.xiaoyang.ex_enigmaticlegacy.api.emc.NoEMCEventHandler;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(Exe.MODID)
public class Exe {
    public static final String MODID = "ex_enigmaticlegacy";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean isEx = false;
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();


    public static void queueServerWork(int tick, Runnable action) {
        workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SuppressWarnings("removal")  //禁用过期警告
    public Exe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        isEx = ModList.get().isLoaded("enigmaticlegacy");

        ModEffects.registerEffects();
        ModSounds.register();
        ModRecipes.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ConfigHandler.register();
        SpawnControlConfig.init();
        ModMenus.registerConditional();
        ModMenus.MENU_TYPES.register(modEventBus);
        ModWeapons.REGISTRY.register(modEventBus);
        ModArmors.REGISTRY.register(modEventBus);
        ModEnchantments.REGISTRY.register(modEventBus);
        ModParticleTypes.register(modEventBus);

        ModIntegrationFlowers.BLOCK_REGISTRY.register(modEventBus);
        ModIntegrationFlowers.BLOCK_ENTITY_REGISTRY.register(modEventBus);
        ModIntegrationFlowers.BLOCK_ITEM_REGISTRY.register(modEventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(new EffectManager());
            FXHandler.registerEffects();
        });

        MinecraftForge.EVENT_BUS.register(new RelicsEventHandler());
        MinecraftForge.EVENT_BUS.register(new SpectatorModeHandler());
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        MinecraftForge.EVENT_BUS.register(new CrissaegrimEventHandler());

        if (ModList.get().isLoaded("projecte")) {
            MinecraftForge.EVENT_BUS.register(NoEMCEventHandler.class);
            MinecraftForge.EVENT_BUS.register(NoEMCCommandInterceptor.class);
            ProjecteFactory.init(modEventBus);
            ProjecteFactory.BlockEntityRegister(ModBlockEntities.BLOCK_ENTITIES);
            ModBlocks.MAGIC_TABLE = ProjecteFactory.MAGIC_TABLE;
            ModItems.MAGIC_TABLE_ITEM = ProjecteFactory.MAGIC_TABLE_ITEM;
            ModItems.EMC_WAND = ProjecteFactory.EMC_WAND;
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            init();
            LOGGER.info("HELLO FROM COMMON SETUP");
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        });
    }

    @SuppressWarnings("removal")  //禁用过期警告
    public static ResourceLocation path(String path) {
        return new ResourceLocation("ex_enigmaticlegacy", path);
    }

    public void init() {
        ForgeRegistries.ITEMS.forEach(ContinuumItem::addPossibleItem);
        ForgeRegistries.BLOCKS.forEach(ContinuumItem::addPossibleItem);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            KeybindHandler.onRegisterKeyMappings(event);
        }

        @SubscribeEvent
        public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(WaveNameData.class, WaveNameTooltipComponent::new);
        }

        @SuppressWarnings("removal")  //禁用过期警告
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            AnimatedChestTexture.init();
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.INFINITYGlASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAGED_CHEST.get(), RenderType.cutoutMipped());
        }
    }
}
