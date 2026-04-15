package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.ManaIvyRegen;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.MithrillRing;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.NebulaRing;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.Factory.ProjecteFactory;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.MODID;

public class ModTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> TAB_EXENIGMATICLEGACY_BOTANIA =
            TABS.register("ex_enigmaticlegacy_botania", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.ex_enigmaticlegacy_botania"))
                            .withBackgroundLocation(new ResourceLocation(MODID,
                                    "textures/gui/container/creative_inventory/tab_botania.png"))
                            .icon(() -> ModItems.ANTIGRAVITY_CHARM.get().getDefaultInstance())
                            .displayItems(ModTabs::populateCreativeTab)
                            .build());

    private static void populateCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // 普通物品
        addNormalItems(output);
        // 无魔力物品
        addEmptyManaItems(output);
        // 满魔力物品
        addFullManaItems(output);
        // 创造物品
        addCreativeItems(output);
        //遗物
        addRelic(output);
        //材料
        addMaterials(output);
        //符文
        addRunes(output);
        //花
        addFlowers(output);
        //武器
        addWeapon(output);
        //盔甲
        addArmors(output);
        //Projecte物品
        addProjecteItems(output);
        //奇点
        addSingularity(output);
        //决心
        addDetermination(output);
        output.accept(ModItems.ADVANCED_SPREADER.get());
    }

    private static void addProjecteItems(CreativeModeTab.Output output) {
        if (!ModList.get().isLoaded("projecte")) return;

        if (ProjecteFactory.MAGIC_TABLE_ITEM != null && ProjecteFactory.MAGIC_TABLE_ITEM.isPresent())
            output.accept(ProjecteFactory.MAGIC_TABLE_ITEM.get());

        if (ProjecteFactory.EMC_WAND != null && ProjecteFactory.EMC_WAND.isPresent())
            output.accept(ProjecteFactory.EMC_WAND.get());

        if (ModIntegrationFlowers.EMC_FLOWER_ITEM != null && ModIntegrationFlowers.EMC_FLOWER_ITEM.isPresent())
            output.accept(ModIntegrationFlowers.EMC_FLOWER_ITEM.get());

        if (ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH_ITEM != null && ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH_ITEM.isPresent())
            output.accept(ModIntegrationFlowers.CELESTIAL_BLUE_HYACINTH_ITEM.get());

        if (ModIntegrationFlowers.ALCHEMY_AZALEA_ITEM != null && ModIntegrationFlowers.ALCHEMY_AZALEA_ITEM.isPresent())
            output.accept(ModIntegrationFlowers.ALCHEMY_AZALEA_ITEM.get());

        if (ModIntegrationFlowers.ALCHEMY_SUNFLOWER_ITEM != null && ModIntegrationFlowers.ALCHEMY_SUNFLOWER_ITEM.isPresent())
            output.accept(ModIntegrationFlowers.ALCHEMY_SUNFLOWER_ITEM.get());
    }

    private static void addDetermination(CreativeModeTab.Output output) {
        output.accept(ModItems.GREEN_DETERMINATION.get());
        output.accept(ModItems.RED_DETERMINATION.get());
        output.accept(ModItems.BLUE_DETERMINATION.get());
        output.accept(ModItems.ORANGE_DETERMINATION.get());
        output.accept(ModItems.YELLOW_DETERMINATION.get());
        output.accept(ModItems.CYAN_DETERMINATION.get());
        output.accept(ModItems.LIGHTER_PURPLE_DETERMINATION.get());
        output.accept(ModItems.BLACK_DETERMINATION.get());
        output.accept(ModItems.PINK_DETERMINATION.get());
        output.accept(ModItems.DEEPER_PURPLE_DETERMINATION.get());
    }

    private static void addSingularity(CreativeModeTab.Output output) {
        output.accept(ModItems.RAINBOW_SINGULARITY.get());
        output.accept(ModItems.WOODEN_SINGULARITY.get());
        output.accept(ModItems.MANYYULLYN_SINGULARITY.get());
        output.accept(ModItems.COBALT_SINGULARITY.get());
        output.accept(ModItems.GAIA_SINGULARITY.get());
        output.accept(ModItems.BEDROCK_SINGULARITY.get());
        output.accept(ModItems.GOBBER_SINGULARITY_END.get());
        output.accept(ModItems.WHITE_MATTER_SINGULARITY.get());
        output.accept(ModItems.POLONIUM_210_SINGULARITY.get());
        output.accept(ModItems.MANAITA_SINGULARITY.get());
        output.accept(ModItems.NEUTRON_SINGULARITY.get());
        output.accept(ModItems.YUANSHI_SINGULARITY.get());
        output.accept(ModItems.ORICHALCOS_SINGULARITY.get());
        output.accept(ModItems.SHADOWIUM_SINGULARITY.get());
        output.accept(ModItems.CRYSTAL_MATRIX_SINGULARITY.get());
        output.accept(ModItems.EVIL_SINGULARITY.get());
        output.accept(ModItems.COMPRESSED_INFINITY_SINGULARITY.get());
    }

    private static void addArmors(CreativeModeTab.Output output) {
        output.accept(ModArmors.MANAITA_HELMET.get());
        output.accept(ModArmors.MANAITA_CHESTPLATE.get());
        output.accept(ModArmors.MANAITA_LEGGINGS.get());
        output.accept(ModArmors.MANAITA_BOOTS.get());
        output.accept(ModArmors.TERRO_RCROWN.get());
        output.accept(ModArmors.DRAGON_WINGS.get());
        output.accept(ModArmors.NEBULA_HELMET.get());
        output.accept(ModArmors.NEBULA_HELMET_REVEAL.get());
        output.accept(ModArmors.NEBULA_CHESTPLATE.get());
        output.accept(ModArmors.NEBULA_LEGGINGS.get());
        output.accept(ModArmors.NEBULA_BOOTS.get());
        output.accept(ModArmors.WILD_HUNT_HELMET.get());
        output.accept(ModArmors.WILD_HUNT_CHESTPLATE.get());
        output.accept(ModArmors.WILD_HUNT_LEGGINGS.get());
        output.accept(ModArmors.WILD_HUNT_BOOTS.get());
        output.accept(ModArmors.dragonArmorHelm.get());
        output.accept(ModArmors.dragonArmorChest.get());
        output.accept(ModArmors.dragonArmorLegs.get());
        output.accept(ModArmors.dragonArmorBoots.get());
        output.accept(ModArmors.ULTIMATE_VALKYRIE_HELMET.get());
        output.accept(ModArmors.ULTIMATE_VALKYRIE_CHESTPLATE.get());
        output.accept(ModArmors.ULTIMATE_VALKYRIE_LEGGINGS.get());
        output.accept(ModArmors.ULTIMATE_VALKYRIE_BOOTS.get());
    }

    private static void addRelic(CreativeModeTab.Output output) {
        output.accept(ModItems.MANAITA.get());
        output.accept(ModItems.VOID_GRIMOIRE.get());
        output.accept(ModItems.BLACK_HALO.get());
        output.accept(ModItems.AESIR_RING.get());
        output.accept(ModItems.ANCIENT_AEGIS.get());
        output.accept(ModItems.SCEPTER_OF_SOVEREIGN.get());
        output.accept(ModItems.TELEPORTATION_TOME.get());
        output.accept(ModItems.POWER_RING.get());
        output.accept(ModItems.DISCORD_RING.get());
        output.accept(ModItems.TALISMAN_HIDDEN_RICHES.get());
        output.accept(ModItems.POCKET_WARDROBE.get());
        output.accept(ModItems.BLACK_HALO_TOME.get());
        output.accept(ModItems.TELEKINESIS_TOME_LEVEL.get());
        output.accept(ModItems.FATE_TOME.get());
    }

    private static void addFlowers(CreativeModeTab.Output output) {
        output.accept(ModItems.ASTRAL_KILLOP.get());
        output.accept(ModItems.RAINBOW_GENERATING_FLOWER.get());
        output.accept(ModItems.CURSET_THISTLE.get());
        output.accept(ModItems.YU_SHOU_CLOVER.get());
        output.accept(ModItems.VACUITY.get());
        output.accept(ModItems.STREET_LIGHT.get());
        output.accept(ModItems.BLAZING_ORCHID.get());
        output.accept(ModItems.MINGXIANLAN.get());
        output.accept(ModItems.FROST_BLOSSOM.get());
        output.accept(ModItems.FROST_LOTUS.get());
        output.accept(ModItems.DARK_NIGHT_GRASS.get());
        output.accept(ModItems.KILLING_BERRY.get());
        output.accept(ModItems.NIGHTSHADE.get());
        output.accept(ModItems.DAYBLOOM.get());
        output.accept(ModItems.BELIEVE.get());
        output.accept(ModItems.GENENERGYDANDRON.get());
        output.accept(ModItems.ORECHIDENDIUM.get());
        output.accept(ModItems.FLOWEY.get());
        output.accept(ModItems.WITCH_OPOOD.get());
        output.accept(ModItems.LYCORISRADIATA.get());
        output.accept(ModItems.SOARLEANDER.get());
        output.accept(ModItems.ENDER_LAVENDER.get());
        output.accept(ModItems.AUREA_AMICITIA_CARNATION.get());
        output.accept(ModItems.CATNIP.get());
        output.accept(ModItems.MUSICAL_ORCHID.get());
        output.accept(ModItems.DICTARIUS.get());
        output.accept(ModItems.ANCIENT_ALPHIRINE.get());
        output.accept(ModItems.EVIL_FORGE.get());
        output.accept(ModItems.ETHERIUM_FORGE.get());
        output.accept(ModItems.ARDENT_AZARCISSUS.get());
        output.accept(ModItems.AQUATIC_ANGLER_NARCISSUS.get());
        output.accept(ModItems.RUNE_FLOWER.get());
    }

    private static void addNormalItems(CreativeModeTab.Output output) {
        output.accept(ModItems.SPAWN_CONTROL_STAFF.get());
        output.accept(ModItems.TERRA_FARMLAND.get());
        output.accept(ModItems.FREYR_SLINGSHOT.get());
        output.accept(ModItems.ANTIGRAVITY_CHARM.get());
        output.accept(ModItems.EMPTY_MANA_BUCKET.get());
        output.accept(ModItems.FILLED_MANA_BUCKET.get());
        output.accept(ModItems.PLUMED_BELT.get());
        output.accept(ModItems.MANA_FLOWER.get());
        output.accept(ModItems.TERRA_HOE.get());
        output.accept(ModItems.TERRA_SHOVEL.get());
        output.accept(ModItems.SPRAWL_ROD.get());
        output.accept(ModItems.CONTINUUM_BOMB.get());
        output.accept(ModItems.ADMIN_CONTROLLER.get());
        output.accept(ModItems.MITHRILL_MULTI_TOOL.get());
        output.accept(ModItems.RIDEABLE_PEARL.get());
        output.accept(ModItems.NEBULA_ROD.get());
        output.accept(ModItems.ADVANCED_SPARK.get());
        output.accept(ModItems.HORN_PLENTY.get());
        output.accept(ModItems.SPHERE_NAVIGATION.get());
        output.accept(ModItems.FATE_HORN.get());
        output.accept(ModItems.SPECTRITE_CRYSTAL.get());
        output.accept(ModItems.BEDROCK_BREAKER.get());
        output.accept(ModItems.WEATHER_STONE.get());
        output.accept(ModItems.DIMENSIONAL_MIRROR.get());
        output.accept(ModItems.OMEGA_CORE.get());
        output.accept(ModItems.CHAOS_CORE.get());
        output.accept(ModItems.STARFLOWER_STONE.get());
        output.accept(ModItems.RAINBOW_MANAITA.get());
        output.accept(ModItems.HOLY_RING.get());
        output.accept(ModItems.STAR_FUEL.get());
        output.accept(ModItems.KILLYOU.get());
        output.accept(ModItems.EGG.get());
        output.accept(ModItems.MEMORIZE.get());
        output.accept(ModItems.MIAOMIAOTOU.get());
        output.accept(ModItems.LOPPING_PEARL.get());
        output.accept(ModItems.MERCURIAL_EYE.get());
        output.accept(ModItems.HEARTH_STONE.get());
        output.accept(ModItems.INFINITY_TOTEM_LEVEL.get());
        output.accept(ModItems.RADIANT_SACRED_RUBY.get());
        output.accept(ModItems.MANA_READER.get());
        output.accept(ModItems.INFINITY_ROD.get());
        output.accept(ModItems.SLING.get());
        output.accept(ModItems.COLOURFUL_DICE.get());
        output.accept(ModItems.SHINY_STONE.get());
        output.accept(ModItems.ELDRITCH_SPELL.get());
        output.accept(ModItems.CRIMSON_SPELL.get());
        output.accept(ModItems.FALSE_JUSTICE.get());
        output.accept(ModItems.XP_TOME.get());
        output.accept(ModItems.GHASTLY_SKULL.get());
        output.accept(ModItems.CHAO_TOME.get());
        output.accept(ModItems.OVERTHROWER.get());
        output.accept(ModItems.MANAITA_SHEARS.get());
        output.accept(ModItems.MISSILE_TOME.get());
        output.accept(ModItems.APOTHEOSIS.get());
        output.accept(ModItems.LUNAR_FLARE.get());
        output.accept(ModItems.DEIFIC_AMULET.get());
        output.accept(ModItems.THUNDER_PEAL.get());
        output.accept(ModItems.SOUL_TOME.get());
        output.accept(ModItems.SUPERPOSITION_RING.get());
        output.accept(ModItems.DORMANT_ARCANUM.get());
        output.accept(ModItems.NEBULOUS_CORE.get());
        output.accept(ModItems.TELEKINESIS_TOME.get());
        output.accept(ModItems.DARK_SUN_RING.get());
        output.accept(ModItems.FLOWER_FINDER_WAND.get());
        output.accept(ModItems.GOLDEN_LAUREL.get());
        output.accept(ModItems.DIVINE_CLOAK_NJORD.get());
        output.accept(ModItems.DIVINE_CLOAK_IDUNN.get());
        output.accept(ModItems.DIVINE_CLOAK_THOR.get());
        output.accept(ModItems.DIVINE_CLOAK_HEIMDALL.get());
        output.accept(ModItems.DIVINE_CLOAK_LOKI.get());
        output.accept(ModItems.SACABAMBASPIS_SPAWN_EGG.get());
        output.accept(ModItems.XIAOYANG_010_SPAWN_EGG.get());
        output.accept(ModItems.MANA_BOX_ITEM.get());
       // output.accept(ModItems.ADVANCED_SPREADER.get());
        output.accept(ModItems.MANA_CRYSTAL.get());
        output.accept(ModItems.MANA_CHARGER.get());
        output.accept(ModItems.CELESTIAL_HOLINESS_TRANSMUTER.get());
        output.accept(ModItems.INFINITYGLASS.get());
        output.accept(ModItems.EVILBLOCK.get());
        output.accept(ModItems.COBBLE_STONE.get());
        output.accept(ModItems.INFINITY_POTATO.get());
        output.accept(ModItems.CUSTOM_SAPLING.get());
        output.accept(ModItems.SPECTRITE_CHEST.get());
        output.accept(ModItems.DRAGON_CRYSTALS_BLOCK.get());
        output.accept(ModItems.POLYCHROME_COLLAPSE_PRISM.get());
        output.accept(ModItems.MANA_CONTAINER.get());
        output.accept(ModItems.DILUTED_CONTAINER.get());
        output.accept(ModItems.CREATIVE_CONTAINER.get());
        output.accept(ModItems.MITHRILL_BLOCK.get());
        output.accept(ModItems.NIDAVELLIR_FORGE.get());
        output.accept(ModItems.GAIA_BLOCK.get());
        output.accept(ModItems.PAGED_CHEST.get());
        output.accept(ModItems.ARCANE_ICE_CHUNK.get());
        output.accept(ModItems.DECAY_BLOCK.get());
        output.accept(ModItems.AERIALITE_BLOCK.get());
        output.accept(ModItems.RAINBOW_TABLE.get());
        output.accept(ModItems.DECON_TABLE_ITEM.get());
        output.accept(ModItems.FULL_ALTAR.get());
        output.accept(ModItems.GAME_BOARD.get());
        output.accept(ModItems.BOARD_FATE.get());
        output.accept(ModItems.MANA_BRACKET.get());
        output.accept(ModItems.ENGINEER_HOPPER.get());
        output.accept(ModItems.LEBETHRON_WOOD.get());
        output.accept(ModItems.LEBETHRON_CORE.get());
        output.accept(ModItems.LEBETHRON_LOG.get());
        output.accept(ModItems.STARLIT_SANCTUM.get());
        output.accept(ModItems.PEACEFUL_TABLE.get());
        output.accept(ModItems.SPECTRITE_ORE.get());
        output.accept(ModItems.BLOCKNATURE.get());
        output.accept(ModItems.PRISMATICRADIANCEBLOCK.get());
        output.accept(ModItems.EXTREME_AUTO_CRAFTER.get());
        output.accept(ModItems.EXTREME_CRAFTING_DISASSEMBLY_TABLE.get());
        output.accept(ModItems.NEUTRONIUM_DECOMPRESSOR.get());
    }

    private static void addMaterials(CreativeModeTab.Output output) {
        output.accept(ModItems.CUSTOM_INGOT.get());
        output.accept(ModItems.CUSTOM_NUGGET.get());
        output.accept(ModItems.DEATH_INGOT.get());
        output.accept(ModItems.DIRT_INGOT.get());
        output.accept(ModItems.ELECTRIC_INGOT.get());
        output.accept(ModItems.GRASS_INGOT.get());
        output.accept(ModItems.ICE_INGOT.get());
        output.accept(ModItems.INGOT_ANIMATION.get());
        output.accept(ModItems.LAVA_INGOT.get());
        output.accept(ModItems.PLATINUM_INGOT.get());
        output.accept(ModItems.WHITE_DUST.get());
        output.accept(ModItems.WOOD_INGOT.get());
        output.accept(ModItems.DEAD_SUBSTANCE.get());
        output.accept(ModItems.NETHER_STAR_NUGGET.get());
        output.accept(ModItems.ENHANCEMENT_CRYSTAL.get());
        output.accept(ModItems.FROST_ENCHANTRESS.get());
        output.accept(ModItems.ASTRAL_PILE.get());
        output.accept(ModItems.ASTRAL_NUGGET.get());
        output.accept(ModItems.DYEABLE_REDSTONE.get());
        output.accept(ModItems.END_CRYSTAIC.get());
        output.accept(ModItems.INFINITYDROP.get());
        output.accept(ModItems.MANA_NETHER_STAR.get());
        output.accept(ModItems.MITHRILL.get());
        output.accept(ModItems.MITHRILL_NUGGET.get());
        output.accept(ModItems.NEBULA_FRAMGMENT.get());
        output.accept(ModItems.PIECE_OF_NEBULA.get());
        output.accept(ModItems.MAT_INSCRIBED_INGOT.get());
        output.accept(ModItems.DRAGON_CRYTAL.get());
        output.accept(ModItems.FOCUS_INFUSION.get());
        output.accept(ModItems.NATURE_INGOT.get());
        output.accept(ModItems.PRISMATICRADIANCEINGOT.get());
        output.accept(ModItems.SPECTRITE_INGOT.get());
        output.accept(ModItems.SPECTRITE_DUST.get());
        output.accept(ModItems.SPECTRITE_GEM.get());
        output.accept(ModItems.RAINBOW_CATALYST.get());
        output.accept(ModItems.FANTASTIC_CATALYST.get());
        output.accept(ModItems.HALO_CATALYST.get());
        output.accept(ModItems.ASTRAL_INGOT.get());
        output.accept(ModItems.COLORFUL_SHADOW_SHARD.get());
        output.accept(ModItems.SPECTRITE_STAR.get());
        output.accept(ModItems.CRYSTALLINE_STARDUST_INGOT.get());
        output.accept(ModItems.STARLIGHT_CRYSTALLINE_INGOT.get());
        output.accept(ModItems.TAINTED_ASTRAL_CRYSTAL_INGOT.get());
        output.accept(ModItems.NEBULA_INGOT.get());
        output.accept(ModItems.NEBULA_STAR.get());
        output.accept(ModItems.NEBULA_NUGGET.get());
        output.accept(ModItems.PIECE_OF_WILD_HUNT.get());
        output.accept(ModItems.WILD_HUNT_NUGGET.get());
        output.accept(ModItems.WILD_HUNT_FRAGMENT.get());
        output.accept(ModItems.WILD_HUNT_INGOT.get());
        output.accept(ModItems.WILD_HUNT_STAR.get());
        output.accept(ModItems.PLAGUE_BANE_INGOT.get());
        output.accept(ModItems.DEAD_CORAL_INGOT.get());
        output.accept(ModItems.ETERNAL_CATALYST.get());
        output.accept(ModItems.GLACIAL_INGOT.get());
        output.accept(ModItems.AMETHYST_INGOT.get());
        output.accept(ModItems.BAUXITE_INGOT.get());
        output.accept(ModItems.BRONZE_INGOT.get());
        output.accept(ModItems.CHROMITE_INGOT.get());
        output.accept(ModItems.FLUORITE_INGOT.get());
        output.accept(ModItems.GYPSUM_INGOT.get());
        output.accept(ModItems.JADE_INGOT.get());
        output.accept(ModItems.LEAD_INGOT.get());
        output.accept(ModItems.OPALLY.get());
        output.accept(ModItems.OPAL_INGOT.get());
        output.accept(ModItems.NICKEL_INGOT.get());
        output.accept(ModItems.IRIDIUM_INGOT.get());
        output.accept(ModItems.DECODE_MATTER_ENIGMA.get());
        output.accept(ModItems.MYSTERIOUS_PRISM.get());
        output.accept(ModItems.ABYSS_INGOT.get());
        output.accept(ModItems.GYRESTEEL.get());
        output.accept(ModItems.EMPYREAN_INGOT.get());
        output.accept(ModItems.MYSTICISM_INGOT.get());
        output.accept(ModItems.RAINBOW_NUGGET.get());
        output.accept(ModItems.RAINBOW_ORE_ROCK.get());
    }

    private static void addEmptyManaItems(CreativeModeTab.Output output) {
        output.accept(ModItems.MANA_IVY_REGEN.get());
        output.accept(ModItems.MITHRILL_RING.get());
        output.accept(ModItems.NEBULA_RING.get());
        output.accept(ModItems.SLIME_CANNON.get());
        output.accept(ModItems.SLIME_NECKLACE.get());
    }

    private static void addFullManaItems(CreativeModeTab.Output output) {
        ItemStack fullManaIvy = new ItemStack(ModItems.MANA_IVY_REGEN.get());
        ManaIvyRegen.setMana(fullManaIvy, ManaIvyRegen.MAX_MANA);
        output.accept(fullManaIvy);

        ItemStack fullManaMithrill = new ItemStack(ModItems.MITHRILL_RING.get());
        MithrillRing.setMana(fullManaMithrill, MithrillRing.MAX_MANA);
        output.accept(fullManaMithrill);

        ItemStack fullManaNebula = new ItemStack(ModItems.NEBULA_RING.get());
        NebulaRing.setMana(fullManaNebula, NebulaRing.MAX_MANA);
        output.accept(fullManaNebula);
    }

    private static void addRunes(CreativeModeTab.Output output) {
        output.accept(ModItems.RadianceRune.get());
        output.accept(ModItems.ShadowyRune.get());
        output.accept(ModItems.IllusoryWorldSpiritSovereignRune.get());
        output.accept(ModItems.MysticalSacredPactRune.get());
        output.accept(ModItems.StellarVaultDivineRevelationRune.get());
        output.accept(ModItems.SereneMysteriousHeavenlyDecreeRune.get());
        output.accept(ModItems.StellarRune.get());
        output.accept(ModItems.HolyBloodCrystalRune.get());
        output.accept(ModItems.VerdantLeafSpiritualRhymeRune.get());
        output.accept(ModItems.DreamFeatherBlueButterflyRune.get());
        output.accept(ModItems.MysteriousPurpleWisteriaSpiritRune.get());
    }

    private static void addCreativeItems(CreativeModeTab.Output output) {
        ItemStack creativeIvy = new ItemStack(ModItems.MANA_IVY_REGEN.get());
        ManaIvyRegen.setMana(creativeIvy, ManaIvyRegen.MAX_MANA);
        ManaIvyRegen.setStackCreative(creativeIvy);
        output.accept(creativeIvy);
    }

    private static void addWeapon(CreativeModeTab.Output output) {
        output.accept(ModWeapons.ANNIHILATION_SWORD.get());
        output.accept(ModWeapons.MANAITASWORDGOD.get());
        output.accept(ModWeapons.MANAITABOW.get());
        output.accept(ModWeapons.REALFINALSWORDGOD.get());
        output.accept(ModWeapons.WASTELAYER.get());
        output.accept(ModWeapons.PARADOX.get());
        output.accept(ModWeapons.CRISSAEGRIM.get());
        output.accept(ModWeapons.OBSIDIAN_EDGE.get());
        output.accept(ModWeapons.CRYSTAL_DAGGER.get());
        output.accept(ModWeapons.CLAYMORE.get());
        output.accept(ModWeapons.WICKEDKRIS.get());
        output.accept(ModWeapons.KISS_OF_NYX.get());
        output.accept(ModWeapons.ICEFORGED_EXCALIBUR.get());
        output.accept(ModWeapons.JUDGMENT_OF_AURORA.get());
        output.accept(ModWeapons.SONG_OF_THE_ABYSS.get());
        output.accept(ModWeapons.SPACE_BLADE.get());
        output.accept(ModWeapons.NATURAL_THOUSAND_BLADES.get());
        output.accept(ModWeapons.AQUA_SWORD.get());
        output.accept(ModWeapons.TERRA_BOW.get());
        output.accept(ModWeapons.HORN_STONE_SWORD.get());
        output.accept(ModWeapons.COSMIC_BREAKER.get());
        output.accept(ModWeapons.COSMIC_ANNIHILATOR_BLADE.get());
        output.accept(ModWeapons.END_BROAD_SWORD.get());
        output.accept(ModWeapons.HALLOWED_EDGE.get());
        output.accept(ModWeapons.HARVESTER.get());
        output.accept(ModWeapons.HEAVENLY_CHIMES.get());
        output.accept(ModWeapons.SWORD_DARK_MATTER.get());
        output.accept(ModWeapons.DRAGONS_LAYER.get());
        output.accept(ModWeapons.FLARE_BRINGER.get());
        output.accept(ModWeapons.SHADOW_BREAKER.get());
        output.accept(ModWeapons.GAIA_KILLER.get());
    }
}