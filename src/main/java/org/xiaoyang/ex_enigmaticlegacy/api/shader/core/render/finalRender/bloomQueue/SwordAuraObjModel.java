package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SwordAuraObjModel {
    public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(Exe.MODID, "sword_aura_obj");
    public static final ModelResourceLocation STANDALONE_MODEL_LOCATION = new ModelResourceLocation(MODEL_LOCATION, "standalone");
    public static BakedModel model;

    public SwordAuraObjModel() {
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(MODEL_LOCATION);
    }

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        model = models.get(MODEL_LOCATION);
        if (model == null) {
            model = models.get(STANDALONE_MODEL_LOCATION);
        }
    }

    public static BakedModel getModel() {
        return model;
    }

    public static boolean isLoaded() {
        return model != null;
    }
}
