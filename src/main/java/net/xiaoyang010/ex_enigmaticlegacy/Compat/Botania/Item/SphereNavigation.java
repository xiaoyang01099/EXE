package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ItemsRemainingRender;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.FindBlocksPacket;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.ItemRelic;

import java.awt.Color;

public class SphereNavigation extends ItemRelic {
    public static final int RANGE_SEARCH = 16;
    public static final int MAX_COOLDOWN = 158;
    public static final int MANA_COST = 50;

    public SphereNavigation() {
        super(new Properties()
                .stacksTo(1)
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }


    @Override
    public Component getName(ItemStack stack) {
        Block block = getFindBlock(stack);
        int meta = getFindMeta(stack);
        if (block != null) {
            ItemStack renderStack = new ItemStack(block, 1);
            return super.getName(stack).copy()
                    .append(" (")
                    .append(renderStack.getHoverName())
                    .append(")");
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getFindBlock(stack) != null && player.isShiftKeyDown()) {
            int damage = stack.getDamageValue();
            stack.setDamageValue(~damage & 1);
            world.playSound(player, player.getOnPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.3F, 0.1F);
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof Player player) {
            if (!world.isClientSide && stack.getDamageValue() == 0 &&
                    getFindBlock(stack) != null && canWork(stack) &&
                    ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {

                setMaxTick(stack);
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendTo(
                            new FindBlocksPacket(
                                    getFindBlock(stack), getFindMeta(stack)), serverPlayer);
                }
            }
        }
    }

    public static void findBlocks(Level world, Block findBlock, int findMeta, Player player) {
        if (world.isClientSide) {
            ItemStack renderStack = null;
            int maxFoundBlocks = 32;
            int foundBlocks = 0;

            BlockPos playerPos = player.blockPosition();

            outerLoop:
            for (int y = -32; y < 16; y++) {
                for (int x = -RANGE_SEARCH; x < RANGE_SEARCH; x++) {
                    if (world.random.nextInt(maxFoundBlocks) < maxFoundBlocks - foundBlocks &&
                            !world.random.nextBoolean()) {
                        for (int z = -RANGE_SEARCH; z < RANGE_SEARCH; z++) {
                            if (world.random.nextInt(maxFoundBlocks) < maxFoundBlocks - foundBlocks &&
                                    !world.random.nextBoolean()) {

                                if (foundBlocks >= maxFoundBlocks) {
                                    break outerLoop;
                                }

                                BlockPos pos = playerPos.offset(x, y, z);
                                if (pos.getY() >= world.getMinBuildHeight() && pos.getY() < world.getMaxBuildHeight()) {
                                    BlockState state = world.getBlockState(pos);
                                    Block block = state.getBlock();

                                    if (block == findBlock) {
                                        if (renderStack == null) {
                                            renderStack = new ItemStack(block);
                                        }

                                        foundBlocks++;
                                        float maxAge = 2.7F + 0.5F * world.random.nextFloat();

                                        float distance = (float)(Math.abs(x) + Math.min(16, Math.abs(y)) + Math.abs(z));
                                        float hue = (120.0F - distance / 64.0F * 120.0F) / 360.0F;
                                        if (hue <= 70.0F / 360.0F) {
                                            hue *= 0.1F;
                                        }

                                        Color color = new Color(Color.HSBtoRGB(hue, 0.9F + world.random.nextFloat() * 0.1F, 1.0F));

                                        for (int i = 0; i < 11; i++) {
                                            double particleX = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5);
                                            double particleY = pos.getY() + 0.5 + (world.random.nextDouble() - 0.5);
                                            double particleZ = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5);

                                            BotaniaAPI.instance().sparkleFX(world, particleX, particleY, particleZ,
                                                    color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F,
                                                    0.3F + world.random.nextFloat() * 0.25F, (int)maxAge);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (renderStack != null && foundBlocks > 0) {
                // 显示找到的区块计数 - 您需要在客户端处理程序中实现这一点
                displayFoundBlocks(renderStack, foundBlocks);
            }
        }
    }

    private static void displayFoundBlocks(ItemStack stack, int count) {
        ItemsRemainingRender.set(
                stack, "Found: " + count);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != null) {
                ItemStack stack = context.getItemInHand();
                setFindBlock(stack, block, 0);

                if (world.isClientSide) {
                    displayFoundBlocks(new ItemStack(block), 1);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    public boolean canWork(ItemStack stack) {
        int tick = ItemNBTHelper.getInt(stack, "cooldown", 0);
        if (tick == 0) {
            return true;
        } else {
            if (tick > 0) {
                ItemNBTHelper.setInt(stack, "cooldown", tick - 1);
            }
            return false;
        }
    }

    public void setMaxTick(ItemStack stack) {
        ItemNBTHelper.setInt(stack, "cooldown", MAX_COOLDOWN);
    }

    public static void setFindBlock(ItemStack stack, Block block, int meta) {
        ItemNBTHelper.setString(stack, "findBlockID",
                net.minecraft.core.Registry.BLOCK.getKey(block).toString());
        ItemNBTHelper.setInt(stack, "findBlockMeta", meta);
    }

    public static Block getFindBlock(ItemStack stack) {
        String blockID = ItemNBTHelper.getString(stack, "findBlockID", "");
        if (blockID.isEmpty()) {
            return null;
        }
        try {
            return net.minecraft.core.Registry.BLOCK.get(new net.minecraft.resources.ResourceLocation(blockID));
        } catch (Exception e) {
            return null;
        }
    }

    public static int getFindMeta(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "findBlockMeta", 0);
    }
}