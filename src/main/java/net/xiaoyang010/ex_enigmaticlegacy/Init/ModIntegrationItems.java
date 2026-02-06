package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.PetrifyingWand;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.LichShieldRing;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Spell.InfinitasVortex;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Spell.LumenAureum;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityMatter;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModAmorphous;

public class ModIntegrationItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

    // ProjectE 联动物品
    public static RegistryObject<Item> INFINITY_MATTER;
    public static RegistryObject<Item> MATTER_AMORPHOUS_MAX;
    public static RegistryObject<Item> MATTER_CORPOREAL_MAX;
    public static RegistryObject<Item> MATTER_DARK_MAX;
    public static RegistryObject<Item> MATTER_ESSENTIA_MAX;
    public static RegistryObject<Item> MATTER_KINETIC_MAX;
    public static RegistryObject<Item> MATTER_OMNI_MAX;
    public static RegistryObject<Item> MATTER_PROTO_MAX;
    public static RegistryObject<Item> MATTER_TEMPORAL_MAX;
    public static RegistryObject<Item> MATTER_VOID_MAX;

    // Iron's Spellbooks 联动物品
    public static RegistryObject<Item> INFINITAS_VORTEX;
    public static RegistryObject<Item> LUMEN_AUREUM;

    // Ice and Fire 联动物品
    public static RegistryObject<Item> PETRIFYING_WAND;

    // Twilight Forest 联动物品
    public static RegistryObject<Item> LICH_RING;

    static {
        // ProjectE 联动注册
        if (ModList.get().isLoaded("projecte")) {
            INFINITY_MATTER = REGISTRY.register("infinity_matter", InfinityMatter::new);
            MATTER_AMORPHOUS_MAX = REGISTRY.register("matter_amorphous_max", ModAmorphous::new);
            MATTER_CORPOREAL_MAX = REGISTRY.register("matter_corporeal_max", ModAmorphous::new);
            MATTER_DARK_MAX = REGISTRY.register("matter_dark_max", ModAmorphous::new);
            MATTER_ESSENTIA_MAX = REGISTRY.register("matter_essentia_max", ModAmorphous::new);
            MATTER_KINETIC_MAX = REGISTRY.register("matter_kinetic_max", ModAmorphous::new);
            MATTER_OMNI_MAX = REGISTRY.register("matter_omni_max", ModAmorphous::new);
            MATTER_PROTO_MAX = REGISTRY.register("matter_proto_max", ModAmorphous::new);
            MATTER_TEMPORAL_MAX = REGISTRY.register("matter_temporal_max", ModAmorphous::new);
            MATTER_VOID_MAX = REGISTRY.register("matter_void_max", ModAmorphous::new);
        }

        // Iron's Spellbooks 联动注册
        if (ModList.get().isLoaded("irons_spellbooks")) {
            INFINITAS_VORTEX = REGISTRY.register("infinitas_vortex", InfinitasVortex::new);
            LUMEN_AUREUM = REGISTRY.register("lumen_aureum", LumenAureum::new);
        }

        // Ice and Fire 联动注册
        if (ModList.get().isLoaded("iceandfire")) {
            PETRIFYING_WAND = REGISTRY.register("petrifying_wand", PetrifyingWand::new);
        }

        // Twilight Forest 联动注册
        if (ModList.get().isLoaded("twilightforest")) {
            LICH_RING = REGISTRY.register("lich_ring", LichShieldRing::new);
        }
    }
}