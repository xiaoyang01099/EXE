package net.xiaoyang010.ex_enigmaticlegacy.World.ritual;


import com.integral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import vazkii.botania.common.block.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class SpectriteWitherRitual {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExEnigmaticlegacyMod.MODID);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Map<BlockPos, Block> RITUAL_STRUCTURE = new HashMap<>();

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        initRitualStructure();
    }

    private static void initRitualStructure() {
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 0), Blocks.SOUL_SAND);
        RITUAL_STRUCTURE.put(new BlockPos(0, 1, 0), ModBlocks.terraPlate);

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 0), ModBlockss.BLOCKNATURE.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 0), ModBlockss.BLOCKNATURE.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -1), ModBlockss.BLOCKNATURE.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 1), ModBlockss.BLOCKNATURE.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -1), ModBlockss.BLOCKNATURE.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -1), ModBlockss.BLOCKNATURE.get());

        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -2), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 2), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 0), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 0), ModBlockss.PRISMATICRADIANCEBLOCK.get());

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -2), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 2), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 2), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -2), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, -1), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 1), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 1), ModBlockss.EVILBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, -1), ModBlockss.EVILBLOCK.get());

        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, -2), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 2), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, -2), ModBlockss.PRISMATICRADIANCEBLOCK.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 2), ModBlockss.PRISMATICRADIANCEBLOCK.get());

        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, -3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, -3), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 3), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, 3), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(3, 0, -1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, -2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, 2), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, -1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, -2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, 2), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(4, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, 2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, -1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, -2), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, -1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, -2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, 2), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(5, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(5, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(5, 0, -1), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(-5, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-5, 0, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-5, 0, -1), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 5), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 5), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 5), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -5), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -5), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -5), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(6, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-6, 0, 0), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 6), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -6), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 7), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -7), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(7, 0, 0), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 0, 0), Blocks.GLOWSTONE);

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -6), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, -5), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(3, 0, -4), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, -3), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(5, 0, -2), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(6, 0, -1), Blocks.GLOWSTONE);

        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -6), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, -5), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, -4), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, -3), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 0, -2), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 0, -1), Blocks.GLOWSTONE);

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 6), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 5), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 4), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(4, 0, 3), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(5, 0, 2), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(6, 0, 1), Blocks.GLOWSTONE);

        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 6), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 5), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 0, 4), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 0, 3), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 0, 2), Blocks.GLOWSTONE);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 0, 1), Blocks.GLOWSTONE);


        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, -5), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, -4), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 1, -3), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 1, -2), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, 5), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, 4), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 1, 3), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 1, 2), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(2, 1, 5), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(3, 1, 4), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(4, 1, 3), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(5, 1, 2), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(2, 1, -5), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(3, 1, -4), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(4, 1, -3), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(5, 1, -2), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(2, 1, -7), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 1, 7), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, -7), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, 7), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(7, 1, -2), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(7, 1, 2), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 1, -2), EnigmaticLegacy.etheriumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 1, 2), EnigmaticLegacy.etheriumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(2, 3, -7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(2, 3, 7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 3, -7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 3, 7), Blocks.SEA_LANTERN);

        RITUAL_STRUCTURE.put(new BlockPos(7, 3, -2), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(7, 3, 2), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 3, -2), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 3, 2), Blocks.SEA_LANTERN);

        RITUAL_STRUCTURE.put(new BlockPos(7, 4, 0), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(7, 4, 0), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 4, 0), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 4, 0), Blocks.SEA_LANTERN);

        RITUAL_STRUCTURE.put(new BlockPos(7, 4, -1), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(7, 4, 1), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 4, -1), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 4, 1), Blocks.SEA_LANTERN);

        RITUAL_STRUCTURE.put(new BlockPos(1, 4, -7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(1, 4, 7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 4, -7), Blocks.SEA_LANTERN);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 4, 7), Blocks.SEA_LANTERN);

        RITUAL_STRUCTURE.put(new BlockPos(1, 1, -2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 1, 2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 1, 2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 1, -2), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 1, -1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(2, 1, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, 1), ModBlockss.INFINITYGlASS.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 1, -1), ModBlockss.INFINITYGlASS.get());

        RITUAL_STRUCTURE.put(new BlockPos(1, 2, -2), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 2, 2), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(1, 2, 2), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 2, -2), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 2, -1), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 2, 1), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 2, 1), Blocks.AMETHYST_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 2, -1), Blocks.AMETHYST_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 2, -5), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 2, -4), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 2, -3), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 2, -2), Blocks.DIAMOND_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 2, 5), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 2, 4), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 2, 3), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 2, 2), Blocks.DIAMOND_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 2, 5), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(3, 2, 4), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(4, 2, 3), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(5, 2, 2), Blocks.DIAMOND_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 2, -5), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(3, 2, -4), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(4, 2, -3), Blocks.DIAMOND_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(5, 2, -2), Blocks.DIAMOND_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(7, 5, 0), Blocks.LAPIS_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(7, 5, 0), Blocks.LAPIS_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 5, 0), Blocks.LAPIS_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 5, 0), Blocks.LAPIS_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(6, 6, 0), Blocks.NETHERITE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 6, 6), Blocks.NETHERITE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 6, 0), Blocks.NETHERITE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 6, -6), Blocks.NETHERITE_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 6, -5), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 6, -4), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 6, -3), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 6, -2), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 6, 5), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 6, 4), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 6, 3), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 6, 2), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 6, 5), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(3, 6, 4), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(4, 6, 3), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(5, 6, 2), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 6, -5), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(3, 6, -4), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(4, 6, -3), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(5, 6, -2), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 6, -6), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 6, 6), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 6, -6), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 6, 6), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(6, 6, -2), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(6, 6, 2), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 6, -2), Blocks.GOLD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 6, 2), Blocks.GOLD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 8), Blocks.REDSTONE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -8), Blocks.REDSTONE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(8, 0, 0), Blocks.REDSTONE_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-8, 0, 0), Blocks.REDSTONE_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, -7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(7, 0, -1), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 0, -1), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(7, 0, 1), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 0, 1), Blocks.EMERALD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(2, 0, -7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, 7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 0, 7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 0, -7), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(7, 0, -2), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 0, -2), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(7, 0, 2), Blocks.EMERALD_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-7, 0, 2), Blocks.EMERALD_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(6, 7, 0), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-6, 7, 0), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 7, 6), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 7, -6), Blocks.IRON_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(5, 8, 0), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 8, 5), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 8, -5), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-5, 8, 0), Blocks.IRON_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(4, 9, 0), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 9, 4), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 9, -4), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 9, 0), Blocks.IRON_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(3, 10, 0), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 10, 3), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 10, -3), Blocks.IRON_BLOCK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 10, 0), Blocks.IRON_BLOCK);

        RITUAL_STRUCTURE.put(new BlockPos(3, 10, 1), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 10, 3), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(1, 10, -3), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 10, -1), Blocks.CRYING_OBSIDIAN);

        RITUAL_STRUCTURE.put(new BlockPos(-3, 10, 1), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 10, -3), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(1, 10, 3), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(3, 10, -1), Blocks.CRYING_OBSIDIAN);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 10, 2), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 10, -2), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(2, 10, 2), Blocks.CRYING_OBSIDIAN);
        RITUAL_STRUCTURE.put(new BlockPos(2, 10, -2), Blocks.CRYING_OBSIDIAN);

        RITUAL_STRUCTURE.put(new BlockPos(3, 11, 0), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 11, 3), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(0, 11, -3), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 11, 0), Blocks.NETHERRACK);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 11, 2), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 11, -2), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 11, 2), Blocks.NETHERRACK);
        RITUAL_STRUCTURE.put(new BlockPos(2, 11, -2), Blocks.NETHERRACK);

        RITUAL_STRUCTURE.put(new BlockPos(3, 11, 1), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 11, 3), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(1, 11, -3), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 11, -1), ModBlocks.dragonstoneBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-3, 11, 1), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 11, -3), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(1, 11, 3), ModBlocks.dragonstoneBlock);
        RITUAL_STRUCTURE.put(new BlockPos(3, 11, -1), ModBlocks.dragonstoneBlock);

        RITUAL_STRUCTURE.put(new BlockPos(3, 12, 1), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 12, 3), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(1, 12, -3), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 12, -1), ModBlocks.gaiaPylon);

        RITUAL_STRUCTURE.put(new BlockPos(-3, 12, 1), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 12, -3), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(1, 12, 3), ModBlocks.gaiaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(3, 12, -1), ModBlocks.gaiaPylon);

        RITUAL_STRUCTURE.put(new BlockPos(3, 12, 0), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, 3), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, -3), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 12, 0), ModBlocks.manasteelBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, 2), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, -2), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, 2), ModBlocks.manasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, -2), ModBlocks.manasteelBlock);

        RITUAL_STRUCTURE.put(new BlockPos(3, 12, 0), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, 3), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, -3), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 12, 0), ModBlocks.terrasteelBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, 2), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, -2), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, 2), ModBlocks.terrasteelBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, -2), ModBlocks.terrasteelBlock);

        RITUAL_STRUCTURE.put(new BlockPos(3, 12, 0), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, 3), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, -3), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(-3, 12, 0), ModBlocks.naturaPylon);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, 2), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, -2), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, 2), ModBlocks.naturaPylon);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, -2), ModBlocks.naturaPylon);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 12, 0), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(2, 12, 0), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, 2), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(0, 12, -2), ModBlocks.elementiumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-1, 12, -1), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 12, 1), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(1, 12, -1), ModBlocks.elementiumBlock);
        RITUAL_STRUCTURE.put(new BlockPos(1, 12, 1), ModBlocks.elementiumBlock);

        RITUAL_STRUCTURE.put(new BlockPos(-2, 13, 0), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(-2, 13, 0), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 13, 2), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(0, 13, -2), ModBlockss.INFINITY_POTATO.get());

        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 8), ModBlockss.ASGARDANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, -8), ModBlockss.ASGARDANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(8, 0, -1), ModBlockss.ASGARDANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(-8, 0, 1), ModBlockss.ASGARDANDELION.get());

        RITUAL_STRUCTURE.put(new BlockPos(1, 1, 7), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 1, -7), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(-1, 1, 7), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(1, 1, 7), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(7, 1, -1), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(-7, 1, -1), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(7, 1, 1), ModBlockss.INFINITY_POTATO.get());
        RITUAL_STRUCTURE.put(new BlockPos(-7, 1, 1), ModBlockss.INFINITY_POTATO.get());

        RITUAL_STRUCTURE.put(new BlockPos(4, 11, 0), Blocks.END_ROD);
        RITUAL_STRUCTURE.put(new BlockPos(0, 11, 4), Blocks.END_ROD);
        RITUAL_STRUCTURE.put(new BlockPos(0, 11, -4), Blocks.END_ROD);
        RITUAL_STRUCTURE.put(new BlockPos(-4, 11, 0), Blocks.END_ROD);

        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, -5), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 1, -4), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(-5, 1, -3), ModBlockss.FLUFFY_DANDELION.get());


        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, 5), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(-4, 1, 4), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(-5, 1, 3), ModBlockss.FLUFFY_DANDELION.get());


        RITUAL_STRUCTURE.put(new BlockPos(3, 1, 5), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 1, 4), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(5, 1, 3), ModBlockss.FLUFFY_DANDELION.get());


        RITUAL_STRUCTURE.put(new BlockPos(3, 1, -5), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(4, 1, -4), ModBlockss.FLUFFY_DANDELION.get());
        RITUAL_STRUCTURE.put(new BlockPos(5, 1, -3), ModBlockss.FLUFFY_DANDELION.get());

        RITUAL_STRUCTURE.put(new BlockPos(6, 1, -3), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(-6, 1, 3), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(-6, 1, -3), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(6, 1, 3), ModBlockss.GENENERGYDANDRON.get());

        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, -6), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(-3, 1, 6), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 1, 6), ModBlockss.GENENERGYDANDRON.get());
        RITUAL_STRUCTURE.put(new BlockPos(3, 1, -6), ModBlockss.GENENERGYDANDRON.get());


    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return;

        ServerLevel world = (ServerLevel) event.getWorld();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() != ModItems.SPECTRITE_STAR.get()) return;

        if (checkRitualConditions(world, pos)) {
            playSummoningAnimation(world, pos);

            scheduler.schedule(() -> {
                spawnCreature(world, pos.above());
            }, 5, TimeUnit.SECONDS);
        }
    }

    private static boolean checkRitualConditions(ServerLevel world, BlockPos centerPos) {

        for (Map.Entry<BlockPos, Block> entry : RITUAL_STRUCTURE.entrySet()) {
            BlockPos relativePos = entry.getKey();
            Block expectedBlock = entry.getValue();

            BlockPos checkPos = centerPos.offset(relativePos);
            BlockState state = world.getBlockState(checkPos);
            if (state.getBlock() != expectedBlock) {
                return false;
            }
        }
        return true;
    }

    private static void playSummoningAnimation(ServerLevel world, BlockPos pos) {
        world.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.0);
        world.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void spawnCreature(ServerLevel world, BlockPos pos) {
        EntityType<?> entityType = ModEntities.SPECTRITE_WITHER.get();
        entityType.spawn(world, null, null, pos, MobSpawnType.EVENT, true, true);
    }
}
