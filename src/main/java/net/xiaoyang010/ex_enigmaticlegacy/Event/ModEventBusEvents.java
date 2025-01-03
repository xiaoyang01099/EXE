package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.CapybaraModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.DragonWingsModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.SpottedGardenEelHidingModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.SpottedGardenEelModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.DragonWingsLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.SpottedGardenEelRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;


@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                new ModelLayerLocation(
                        new ResourceLocation(ExEnigmaticlegacyMod.MODID, "dragonwings_layer"),
                        "main"
                ),
                DragonWingsModel::createBodyLayer
        );
        event.registerLayerDefinition(CapybaraModel.LAYER_LOCATION, CapybaraModel::createBodyLayer);

        event.registerLayerDefinition(
                SpottedGardenEelRenderer.EEL_MODEL_LAYER,
                SpottedGardenEelModel::createBodyLayer
        );
        
        event.registerLayerDefinition(
                SpottedGardenEelRenderer.EEL_HIDING_MODEL_LAYER,
                SpottedGardenEelHidingModel::createBodyLayer
        );
    }



    // 添加渲染层到玩家渲染器
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 为所有已知的玩家皮肤类型添加龙翼层
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
