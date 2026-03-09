package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.mixin.client.AccessorModelBakery;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod.path;
import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class SpecialMiscellaneousModels {
    public static final SpecialMiscellaneousModels INSTANCE = new SpecialMiscellaneousModels();
    public boolean registeredModels = false;
    public final Material polychromeCollapsePrismOverlay = mainAtlas("blocks/polychrome/polychrome_collapse_prism_overlay");
    public final Material rainbowManaWater = mainAtlas("blocks/mana_water/rainbow_mana_water");
    public final Material superconductiveSparkIcon = mainAtlas("entity/superconductive_spark");
    public final Material superconductiveSparkIconStar = mainAtlas("items/res/superconductive_spark_star");
    public final Material nebula_eyes = mainAtlas("models/nebula_eyes");
    public final Material evilManaWater = mainAtlas("blocks/mana_water/evil_water");

    public BakedModel cursedSpreaderCore;
    public BakedModel cursedSpreaderScaffolding;
    public BakedModel advancedSpreaderCore;

    public void onModelRegister(ResourceManager rm, Consumer<ResourceLocation> consumer) {
        consumer.accept(path("block/advanced_spreader_core"));
        consumer.accept(path("block/cursed_spreader_core"));
        consumer.accept(path("block/cursed_spreader_scaffolding"));


        if (!registeredModels) {
            registeredModels = true;
        }
    }


    public void onModelBake(ModelBakery loader, Map<ResourceLocation, BakedModel> map) {
        if (!registeredModels) {
            ExEnigmaticlegacyMod.LOGGER.error("Cursed spreader models failed to register! Skipping bake.");
            return;
        }
        advancedSpreaderCore = map.get(path("block/advanced_spreader_core"));
        cursedSpreaderCore = map.get(path("block/cursed_spreader_core"));
        cursedSpreaderScaffolding = map.get(path("block/cursed_spreader_scaffolding"));
    }



    private static Material mainAtlas(String name) {
        return new Material(InventoryMenu.BLOCK_ATLAS, EXE(name));
    }

    public static ResourceLocation EXE(String name){
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, name);
    }
}
