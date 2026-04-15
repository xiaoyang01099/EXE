package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IContinuumSpecial;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContinuumItem extends Item {
    private static final Random random = new Random();
    private static final ArrayList<ItemStack> possibleItems = new ArrayList<>();

    public ContinuumItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(itemstack);
        }

        if (possibleItems.isEmpty()) {
            return InteractionResultHolder.fail(itemstack);
        }

        ItemStack randomItem = getRandomStack(level.random);
        if (itemstack.getCount() == 1) {
            return InteractionResultHolder.success(randomItem);
        }

        if (player.getInventory().add(randomItem)) {
            itemstack.shrink(1);
            return InteractionResultHolder.success(itemstack);
        }

        return InteractionResultHolder.fail(itemstack);
    }

    public static ItemStack getRandomStack(RandomSource rand) {
        if (possibleItems.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack randomStack = possibleItems.get(rand.nextInt(possibleItems.size()));
        if (randomStack.getItem() instanceof IContinuumSpecial continuumSpecial) {
            randomStack = continuumSpecial.getContinuumDrop(randomStack, rand);
        }
        return randomStack;
    }

    public static void addPossibleItem(ItemStack stack) {
        if (!stack.isEmpty() && !possibleItems.contains(stack)) {
            possibleItems.add(stack);
        }
    }

    public static void addPossibleItem(Item item) {
        addPossibleItem(new ItemStack(item));
    }

    public static void addPossibleItem(Block block) {
        addPossibleItem(new ItemStack(block));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.aether.continuum.ability"));
        tooltip.add(Component.translatable("item.aether.continuum.usage"));
    }
}