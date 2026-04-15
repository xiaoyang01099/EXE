package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.ModelEvent;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Map;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.path;


public class SpecialMiscellaneousModels {
    public static final SpecialMiscellaneousModels INSTANCE = new SpecialMiscellaneousModels();
    public boolean registeredModels = false;
    public final Material polychromeCollapsePrismOverlay = mainAtlas("block/polychrome/polychrome_collapse_prism_overlay");
    public final Material rainbowManaWater = mainAtlas("block/mana_water/rainbow_mana_water");
    public final Material superconductiveSparkIcon = mainAtlas("item/entity/superconductive_spark");
    public final Material superconductiveSparkIconStar = mainAtlas("item/res/superconductive_spark_star");
    public final Material nebula_eyes = mainAtlas("models/nebula_eyes");
    public final Material evilManaWater = mainAtlas("block/mana_water/evil_water");

    public BakedModel cursedSpreaderCore;
    public BakedModel cursedSpreaderScaffolding;
    public BakedModel advancedSpreaderCore;

    public void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(path("block/advanced_spreader_core"));
        event.register(path("block/cursed_spreader_core"));
        event.register(path("block/cursed_spreader_scaffolding"));

        if (!registeredModels) {
            registeredModels = true;
        }
    }

    public void onModelBake(ModelBakery loader, Map<ResourceLocation, BakedModel> map) {
        if (!registeredModels) {
            Exe.LOGGER.error("Cursed spreader models failed to register! Skipping bake.");
            return;
        }
        advancedSpreaderCore = map.get(path("block/advanced_spreader_core"));
        cursedSpreaderCore = map.get(path("block/cursed_spreader_core"));
        cursedSpreaderScaffolding = map.get(path("block/cursed_spreader_scaffolding"));
    }

    private static Material mainAtlas(String name) {
        return new Material(InventoryMenu.BLOCK_ATLAS, EXE(name));
    }

    public static ResourceLocation EXE(String name) {
        return new ResourceLocation(Exe.MODID, name);
    }
}