package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class ModModelLayers {
    public static final ModelLayerLocation MIAOMIAO_LAYER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "kind_miao"), "main");

    public static final ModelLayerLocation DICE_FATE = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "dice_fate"), "main");

    public static final ModelLayerLocation MANA_CONTAINER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "mana_container"), "main");

    public static final ModelLayerLocation CREATIVE_CONTAINER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "creative_container"), "main");

    public static final ModelLayerLocation DILUTED_CONTAINER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "diluted_container"), "main");

    public static final ModelLayerLocation ENGINEER_HOPPER = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "engineer_hopper"), "main");

    private static ModelLayerLocation createLocation(String name, String variant) {
        return new ModelLayerLocation(new ResourceLocation(ExEnigmaticlegacyMod.MODID, name), variant);

    }
}
