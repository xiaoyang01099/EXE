package net.xiaoyang010.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.DragonWingsLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.SpottedGardenEelRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialCoreShaders;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;
import vazkii.botania.forge.mixin.client.ForgeAccessorModelBakery;

import java.io.IOException;

import static net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmor.NEBULA_EYES_TEXTURE;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    public static final ModelLayerLocation NEBULA_ARMOR_LAYER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "armor_nebula"), "main");

    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
    }

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
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
//            MinecraftForgeClient.registerTooltipComponentFactory(
//            );
        });
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.polychromeCollapsePrismOverlay.texture());
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.rainbowManaWater.texture());
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIcon.texture());
            event.addSprite(SpecialMiscellaneousModels.INSTANCE.superconductiveSparkIconStar.texture());

        }
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            event.addSprite(NEBULA_EYES_TEXTURE);
        }
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent evt) {
        ResourceManager resourceManager = null;
        if (ForgeModelBakery.instance() != null) {
            resourceManager = ((ForgeAccessorModelBakery) (Object) ForgeModelBakery.instance()).getResourceManager();
        }
        //SpecialMiscellaneousModels.INSTANCE.onModelRegister(resourceManager, ForgeModelBakery::addSpecialModel);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) throws IOException {
        SpecialCoreShaders.init(evt.getResourceManager(), p -> evt.registerShader(p.getFirst(), p.getSecond()));
    }
}
