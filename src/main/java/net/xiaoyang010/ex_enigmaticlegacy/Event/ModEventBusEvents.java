package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.DragonWingsLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.SpottedGardenEelRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.DragonCrystalArmor;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    public static final ModelLayerLocation MIAOMIAO_LAYER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "kind_miao"), "main");

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
        event.registerLayerDefinition(MIAOMIAO_LAYER, KindMiaoModel::createBodyLayer
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
}
