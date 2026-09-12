package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordBakedModel;
import org.xiaoyang.ex_enigmaticlegacy.Item.TridentPlusBakedModel;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.SwordAuraObjModel;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.block.RainbowTableRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.layer.SlimeArmorLayer;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.tile.FloweyTileRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.render.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.MithrillMultiTool;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.SphereNavigation;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.TerraShovel;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.SpecialMiscellaneousModels;
import org.xiaoyang.ex_enigmaticlegacy.Client.model.*;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.block.*;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.layer.DragonWingsLayer;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.layer.WitherArmorLayer;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.end.EndPortalHaloLoader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic.RainbowCosmicModelLoader;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res.RenderCursedSpreader;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res.RenderTileCursedPool;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonWingsModel.LAYER_LOCATION, DragonWingsModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MIAOMIAO_LAYER, KindMiaoModel::createBodyLayer);
        event.registerLayerDefinition(NebulaArmorModel.LAYER_LOCATION, NebulaArmorModel::createBodyLayer);
        event.registerLayerDefinition(RainbowTableModel.LAYER_LOCATION, RainbowTableModel::createBodyLayer);
        event.registerLayerDefinition(SacabambaspisModel.LAYER_LOCATION, SacabambaspisModel::createBodyLayer);
        event.registerLayerDefinition(ModelNidavellirForge.LAYER_LOCATION, ModelNidavellirForge::createBodyLayer);
        event.registerLayerDefinition(ModelArmorWildHunt.LAYER_LOCATION, ModelArmorWildHunt::createBodyLayer);
        event.registerLayerDefinition(ModelArmorNebula.LAYER_LOCATION, ModelArmorNebula::createBodyLayer);
        event.registerLayerDefinition(ModelManaCharger.LAYER_LOCATION, ModelManaCharger::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MANA_CONTAINER, ModelManaContainer::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.CREATIVE_CONTAINER, ModelManaContainer::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.DILUTED_CONTAINER, ModelManaContainer::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.DICE_FATE, ModelDiceFate::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.ENGINEER_HOPPER, ModelEngineerHopper::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SLIME_CANNON, ModelSlimeCannon::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SLIME_ARMOR, ModelSlimeArmor::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.GIUL_BSEN_LAYER, GiuIBsenModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.FENRIS_ARMOR, ModelArmorFenris::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.ECLIPSE_ARMOR, ModelArmorEclipse::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SUNMAKER_ARMOR, ModelArmorSunmaker::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.STAR_PIECES, StarPiecesModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.VINE_HAND, VineHandModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerItemCapabilities(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CURSED_FLOWER.get(), RenderType.cutout());
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
                    (stack, world, entity, seed) -> SphereNavigation.isEnabled(stack) ? 1.0F : 0.0F
            );
            ItemProperties.register(
                    ModItems.SPHERE_NAVIGATION.get(),
                    new ResourceLocation("ex_enigmaticlegacy", "has_target"),
                    (stack, world, entity, seed) ->
                            SphereNavigation.getFindBlock(stack) != null ? 1.0F : 0.0F
            );
            ItemProperties.register(
                    ModItems.SLING.get(),
                    new ResourceLocation(Exe.MODID, "throw"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        if (entity.isUsingItem() && entity.getUseItem() == stack) {
                            return 1.0F;
                        }
                        boolean isSwingMain = entity.getItemInHand(InteractionHand.MAIN_HAND) == stack
                                && entity.swingTime > 0
                                && entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
                        boolean isSwingOff = entity.getItemInHand(InteractionHand.OFF_HAND) == stack
                                && entity.swingTime > 0
                                && entity.getUsedItemHand() == InteractionHand.OFF_HAND;
                        if (isSwingMain || isSwingOff) {
                            return 2.0F;
                        }
                        return 0.0F;
                    }
            );
        });
        ItemProperties.register(ModWeapons.TRIDENT_PLUS.get(), new ResourceLocation("throwing")
                , (stack, level, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(ModBlockEntities.MANA_BRACKET_TILE.get(), ManaBracketRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ASTRAL_KILLOP_TILE.get(), AstralKillopRenderer::new);
//        BlockEntityRenderers.register(ModBlockEntities.ASTRAL_BLOCK_ENTITY.get(), AstralBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.GAME_BOARD_TILE.get(), RenderTileGameBoard::new);
        BlockEntityRenderers.register(ModBlockEntities.BOARD_FATE_TILE.get(), RenderTileBoardFate::new);
        BlockEntityRenderers.register(ModBlockEntities.FULL_ALTAR_TILE.get(), FullAltarRender::new);
        BlockEntityRenderers.register(ModBlockEntities.MANA_CONTAINER_TILE.get(), ManaContainerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MANA_CRYSTAL_TILE.get(), RenderTileManaCrystalCube::new);
        BlockEntityRenderers.register(ModBlockEntities.PAGED_CHEST.get(), PagedChestRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.INFINITY_POTATO.get(), InfinityPotatoRender::new);
        BlockEntityRenderers.register(ModBlockEntities.RAINBOW_TABLE_TILE.get(), RainbowTableRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(), RenderTileNidavellirForge::new);
        BlockEntityRenderers.register(ModBlockEntities.SPECTRITE_CHEST_TILE.get(), SpectriteChestRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MANA_CHARGER_TILE.get(), RenderTileManaCharger::new);
        BlockEntityRenderers.register(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), PolychromeCollapsePrismRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ENGINEER_HOPPER_TILE.get(), RenderTileEngineerHopper::new);
//        BlockEntityRenderers.register(ModBlockEntities.COSMIC_BLOCK_ENTITY.get(), CosmicBlockRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.FLOWEYTILE.get(), FloweyTileRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CURSED_SPREADER.get(), RenderCursedSpreader::new);
        BlockEntityRenderers.register(ModBlockEntities.CURSED_MANA_POOL.get(), RenderTileCursedPool::new);
//        BlockEntityRenderers.register(ModBlockEntities.STARRY_SKY_BLOCK_ENTITY.get(), StarrySkyBlockRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STARLIT_SANCTUM_OF_MYSTIQUE.get(), StarlitSanctumRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ADVANCED_SPREADER.get(), RenderTileAdvancedSpreader::new);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        SwordAuraObjModel.registerAdditional(event);
        event.register(new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword_3d"), "inventory"));
        event.register(new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword_plus_3d"), "inventory"));
        event.register(new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "trident_plus_in_hand"), "inventory"));
        event.register(new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "trident_plus_throwing"), "inventory"));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            PlayerRenderer playerRenderer = event.getSkin(skinName);
            if (playerRenderer != null) {
                playerRenderer.addLayer(new DragonWingsLayer(
                        playerRenderer,
                        event.getEntityModels()
                ));
            }
        }

        addValkyrieLayerToPlayerSkin(event, "default");
        addValkyrieLayerToPlayerSkin(event, "slim");
        addSlimeLayerToPlayer(event, "default");
        addSlimeLayerToPlayer(event, "slim");
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        SpecialMiscellaneousModels.INSTANCE.onModelBake(
                event.getModelBakery(),
                event.getModels()
        );
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void TronModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        SwordAuraObjModel.onModelBake(event);

        ModelResourceLocation itemLoc = new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword"), "inventory");
        ModelResourceLocation item3dLoc = new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword_3d"), "inventory");
        ModelResourceLocation itemPlusLoc = new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword_plus"), "inventory");
        ModelResourceLocation itemPlus3dLoc = new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "fly_sword_plus_3d"), "inventory");
        ModelResourceLocation tridentPlusLoc = new ModelResourceLocation(new ResourceLocation("ex_enigmaticlegacy", "trident_plus"), "inventory");
        ModelResourceLocation tridentPlusInHandLoc = ItemRenderer.TRIDENT_IN_HAND_MODEL;

        BakedModel guiModel = models.get(itemLoc);
        BakedModel handModel = models.get(item3dLoc);
        BakedModel plusGuiModel = models.get(itemPlusLoc);
        BakedModel plusHandModel = models.get(itemPlus3dLoc);
        BakedModel tridentPlusGuiModel = models.get(tridentPlusLoc);
        BakedModel tridentPlusInHandModel = models.get(tridentPlusInHandLoc);

        if (guiModel == null || handModel == null) {
            return;
        }

        models.put(itemLoc, new FlySwordBakedModel(guiModel, handModel));
        if (plusGuiModel != null && plusHandModel != null) {
            models.put(itemPlusLoc, new FlySwordBakedModel(plusGuiModel, plusHandModel));
        }

        models.put(tridentPlusLoc, new TridentPlusBakedModel(tridentPlusGuiModel, tridentPlusInHandModel));
    }

    private static void addValkyrieLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinType) {
        EntityRenderer<? extends AbstractClientPlayer> renderer = (EntityRenderer<? extends AbstractClientPlayer>) event.getSkin(skinType);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new WitherArmorLayer<>(playerRenderer, playerRenderer.getModel()));
        }
    }

    private static void addSlimeLayerToPlayer(
            EntityRenderersEvent.AddLayers event, String skinType) {
        PlayerRenderer renderer = event.getSkin(skinType);
        if (renderer != null) {
            renderer.addLayer(new SlimeArmorLayer<>(renderer));
        }
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        SpecialMiscellaneousModels.INSTANCE.onModelRegister(event);

        event.register(new ResourceLocation("ex_enigmaticlegacy", "block/advanced_spreader"));
        event.register(new ResourceLocation("ex_enigmaticlegacy", "block/advanced_spreader_core"));
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("rainbow_cosmic", new RainbowCosmicModelLoader());
        event.register("end_portal_halo", new EndPortalHaloLoader());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) throws IOException {
        EXECoreShaders.init(evt.getResourceProvider(), p -> evt.registerShader(p.getFirst(), p.getSecond()));
        EXECoreShaders.registerShaders(evt);
    }
}
