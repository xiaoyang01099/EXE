package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DropItemsHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!event.getEntity().level.isClientSide()) {
            Random random = new Random();
            LivingEntity entity = (LivingEntity) event.getEntity();

            if (entity instanceof Player) {
                if (random.nextDouble() < 0.01) {
                    ItemEntity itemEntity = new ItemEntity(
                            entity.level,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            new ItemStack(ModItems.DEAD_SUBSTANCE.get())
                    );
                    entity.level.addFreshEntity(itemEntity);
                }
            } else if (event.getSource().getEntity() instanceof Player) {
                if (random.nextDouble() < 0.001) {
                    ItemEntity itemEntity = new ItemEntity(
                            entity.level,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            new ItemStack(ModItems.DEAD_SUBSTANCE.get())
                    );
                    entity.level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();
        Block brokenBlock = event.getState().getBlock();

        if (isAxe(heldItem) && isWoodLog(brokenBlock)) {
            Random random = new Random();
            if (random.nextDouble() < 0.01) {
                spawnItem(event, ModItems.WOOD_INGOT.get());
            }
        }

        if (isShovel(heldItem) && (isDirt(brokenBlock))) {
            Random random = new Random();
            if (random.nextDouble() < 0.01) {
                spawnItem(event, ModItems.DIRT_INGOT.get());
            }
        }
    }

    private static void spawnItem(BlockEvent.BreakEvent event, Item item) {
        ItemEntity itemEntity = new ItemEntity(
                (Level) event.getWorld(),
                event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5,
                new ItemStack(item)
        );
        event.getWorld().addFreshEntity(itemEntity);
    }

    private static boolean isAxe(ItemStack item) {
        return item.getItem() instanceof AxeItem;
    }

    private static boolean isShovel(ItemStack item) {
        return item.getItem() instanceof ShovelItem;
    }

    private static boolean isWoodLog(Block block) {
        return block.defaultBlockState().is(BlockTags.LOGS);
    }

    private static boolean isDirt(Block block) {
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
    }
}