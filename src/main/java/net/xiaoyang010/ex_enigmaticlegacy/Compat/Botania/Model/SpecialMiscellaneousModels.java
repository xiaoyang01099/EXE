package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SpecialMiscellaneousModels {
    public static final SpecialMiscellaneousModels INSTANCE = new SpecialMiscellaneousModels();

    public final Material polychromeCollapsePrismOverlay = mainAtlas("blocks/polychrome/polychrome_collapse_prism_overlay");
    public final Material rainbowManaWater = mainAtlas("blocks/rainbow_mana_water");
    public final Material superconductiveSparkIcon = mainAtlas("entity/superconductive_spark");
    public final Material superconductiveSparkIconStar = mainAtlas("items/res/superconductive_spark_star");


    private static Material mainAtlas(String name) {
        return new Material(InventoryMenu.BLOCK_ATLAS, EXE(name));
    }

    private static ResourceLocation EXE(String name){
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, name);
    }
}
