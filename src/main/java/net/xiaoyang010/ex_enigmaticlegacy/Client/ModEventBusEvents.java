package net.xiaoyang010.ex_enigmaticlegacy.Client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.DragonWingsLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.WitherArmorLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile.SpottedGardenEelRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.CosmicBlockRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.RainbowCosmicModelLoader;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.CosmicModelLoader;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model.InfinityArmorModel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.FloweyTileRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.MithrillMultiTool;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.SphereNavigation;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.TerraShovel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialCoreShaders;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.EndPortalHaloLoader;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.StarrySkyBlockRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.RenderCursedSpreader;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.RenderTileCursedPool;
import vazkii.botania.forge.mixin.client.ForgeAccessorModelBakery;
import vazkii.botania.mixin.client.AccessorModelBakery;
import vazkii.botania.mixin.client.AccessorRenderBuffers;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonWingsModel.LAYER_LOCATION, DragonWingsModel::createBodyLayer
        );
        event.registerLayerDefinition(CapybaraModel.LAYER_LOCATION, CapybaraModel::createBodyLayer
        );
        event.registerLayerDefinition(SpottedGardenEelRenderer.EEL_MODEL_LAYER, SpottedGardenEelModel::createBodyLayer
        );
        event.registerLayerDefinition(SpottedGardenEelRenderer.EEL_HIDING_MODEL_LAYER, SpottedGardenEelHidingModel::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.MIAOMIAO_LAYER, KindMiaoModel::createBodyLayer
        );
        event.registerLayerDefinition(NebulaArmorModel.LAYER_LOCATION, NebulaArmorModel::createBodyLayer
        );
        event.registerLayerDefinition(RainbowTableModel.LAYER_LOCATION, RainbowTableModel::createBodyLayer
        );
        event.registerLayerDefinition(ModelSeaSerpent.LAYER_LOCATION, ModelSeaSerpent::createBodyLayer
        );
        event.registerLayerDefinition(SacabambaspisModel.LAYER_LOCATION, SacabambaspisModel::createBodyLayer
        );
        event.registerLayerDefinition(ModelNidavellirForge.LAYER_LOCATION, ModelNidavellirForge::createBodyLayer
        );
        event.registerLayerDefinition(ModelArmorWildHunt.LAYER_LOCATION, ModelArmorWildHunt::createBodyLayer
        );
        event.registerLayerDefinition(ModelArmorNebula.LAYER_LOCATION, ModelArmorNebula::createBodyLayer
        );
        event.registerLayerDefinition(ModelManaCharger.LAYER_LOCATION, ModelManaCharger::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.MANA_CONTAINER, ModelManaContainer::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.CREATIVE_CONTAINER, ModelManaContainer::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.DILUTED_CONTAINER, ModelManaContainer::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.DICE_FATE, ModelDiceFate::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.ENGINEER_HOPPER, ModelEngineerHopper::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.VINE_AND_PIECE, VineHandModel::createBodyLayer
        );
        event.registerLayerDefinition(ModModelLayers.VINE_AND_PIECE, PieceModel::createBodyLayer
        );

    }

    @SubscribeEvent
    public static void registerItemCapabilities(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModWeapons.TERRA_BOW.get(),
                    new ResourceLocation("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        return entity.getUseItem() != stack ? 0.0F :
                                (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
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

            ItemProperties.register(
                    ModItems.SPHERE_NAVIGATION.get(),
                    new ResourceLocation("ex_enigmaticlegacy", "enabled"),
                    (stack, world, entity, seed) -> stack.getDamageValue() == 0 ? 1.0F : 0.0F
            );
            ItemProperties.register(
                    ModItems.SPHERE_NAVIGATION.get(),
                    new ResourceLocation("ex_enigmaticlegacy", "has_target"),
                    (stack, world, entity, seed) ->
                            SphereNavigation.getFindBlock(stack) != null ? 1.0F : 0.0F
            );
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(ModBlockEntities.MANA_BRACKET_TILE.get(), ManaBracketRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ASTRAL_KILLOP_TILE.get(), AstralKillopRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ASTRAL_BLOCK_ENTITY.get(), AstralBlockEntityRenderer::new);
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
        BlockEntityRenderers.register(ModBlockEntities.ENGINEER_HOPPER_TILE.get(),RenderTileEngineerHopper::new);
        BlockEntityRenderers.register(ModBlockEntities.COSMIC_BLOCK_ENTITY.get(), CosmicBlockRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.FLOWEYTILE.get(), FloweyTileRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CURSED_SPREADER.get(), RenderCursedSpreader::new);
        BlockEntityRenderers.register(ModBlockEntities.CURSED_MANA_POOL.get(), RenderTileCursedPool::new);
        BlockEntityRenderers.register(ModBlockEntities.STARRY_SKY_BLOCK_ENTITY.get(), StarrySkyBlockRenderer::new);

    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers ev) {
        addLayer(ev, "default");
        addLayer(ev, "slim");
    }

    public static void addLayer(EntityRenderersEvent.AddLayers ev, String s) {
        LivingEntityRenderer r = ev.getSkin(s);
        r.addLayer(new InfinityArmorModel.PlayerRender(r));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            PlayerRenderer playerRenderer = event.getSkin(skinName);
            if (playerRenderer != null) {
                ModelPart modelPart = event.getEntityModels().bakeLayer(
                        new ModelLayerLocation(
                                new ResourceLocation(ExEnigmaticlegacyMod.MODID, "dragonwings_layer"),
                                "main"
                        )
                );
                playerRenderer.addLayer(new DragonWingsLayer(
                        playerRenderer,
                        event.getEntityModels()
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        SpecialMiscellaneousModels.INSTANCE.onModelBake(
                event.getModelLoader(),
                event.getModelRegistry()
        );
    }

    private static void addValkyrieLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinType) {
        EntityRenderer<? extends AbstractClientPlayer> renderer = (EntityRenderer<? extends AbstractClientPlayer>) event.getSkin(skinType);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new WitherArmorLayer<>(playerRenderer, playerRenderer.getModel()));
        }
    }

    @SubscribeEvent
    public static void onAddValkyrieArmorLayers(EntityRenderersEvent.AddLayers event) {
        addValkyrieLayerToPlayerSkin(event, "default");
        addValkyrieLayerToPlayerSkin(event, "slim");
    }


    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIcon.texture());
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIconStar.texture());
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.nebula_eyes.texture());
        }
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent evt) {
        var resourceManager = ((ForgeAccessorModelBakery) (Object) ForgeModelBakery.instance())
                .getResourceManager();

        SpecialMiscellaneousModels.INSTANCE.onModelRegister(
                resourceManager,
                ForgeModelBakery::addSpecialModel
        );

        Set<Material> materials = AccessorModelBakery.getMaterials();
        materials.add(SpecialMiscellaneousModels.INSTANCE.evilManaWater);
        materials.add(SpecialMiscellaneousModels.INSTANCE.rainbowManaWater);
        materials.add(SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay);

        ModelLoaderRegistry.registerLoader(new ResourceLocation("ex_enigmaticlegacy", "rainbow_cosmic"), new RainbowCosmicModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation("ex_enigmaticlegacy", "cosmic"), new CosmicModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation("ex_enigmaticlegacy", "end_portal_halo"), new EndPortalHaloLoader());
    }

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent evt) {
        SortedMap<RenderType, BufferBuilder> layers = ((AccessorRenderBuffers) Minecraft.getInstance()
                .renderBuffers()).getEntityBuilders();
        layers.put(SpecialRenderHelper.RAINBOW_MANA_WATER, new BufferBuilder(SpecialRenderHelper.RAINBOW_MANA_WATER.bufferSize()));
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) throws IOException {
        SpecialCoreShaders.init(evt.getResourceManager(), p -> evt.registerShader(p.getFirst(), p.getSecond()));
    }
}
