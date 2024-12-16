package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.DragonWingsModel;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.DragonWingsLayer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;


@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
//        Registry.register(Registry.RECIPE_TYPE, CelestialTransmuteRecipe.Type.ID, CelestialTransmuteRecipe.Type.INSTANCE);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonWingsModel.LAYER_LOCATION, DragonWingsModel::createLayer);
    }


    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 为默认皮肤添加层
        PlayerRenderer defaultRenderer = event.getSkin("default");
        if (defaultRenderer != null) {
            ModelPart modelPart = event.getEntityModels().bakeLayer(DragonWingsModel.LAYER_LOCATION);
            defaultRenderer.addLayer(new DragonWingsLayer(defaultRenderer, modelPart));
        }

        // 为纤细皮肤添加层
        PlayerRenderer slimRenderer = event.getSkin("slim");
        if (slimRenderer != null) {
            ModelPart modelPart = event.getEntityModels().bakeLayer(DragonWingsModel.LAYER_LOCATION);
            slimRenderer.addLayer(new DragonWingsLayer(slimRenderer, modelPart));
        }
    }
}
