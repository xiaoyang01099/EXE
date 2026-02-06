package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ItemsRemainingRender;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.ItemRelic;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;

public class SphereNavigation extends ItemRelic {
    public static final int RANGE_SEARCH = 16;
    public static final int MAX_COOLDOWN = 158;
    public static final int MANA_COST = 50;

    public SphereNavigation(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        Block block = getFindBlock(stack);
        if (block != null) {
            ItemStack renderStack = new ItemStack(block, 1);
            return super.getName(stack).copy()
                    .append(ChatFormatting.RESET + " (")
                    .append(renderStack.getHoverName().copy().withStyle(ChatFormatting.GREEN))
                    .append(ChatFormatting.RESET + ")");
        }
        return super.getName(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        boolean active = stack.getDamageValue() == 0;
        tooltip.add(new TranslatableComponent(active ? "botaniamisc.active" : "botaniamisc.inactive"));

        Block findBlock = getFindBlock(stack);
        if (findBlock != null) {
            tooltip.add(new TranslatableComponent("ex_enigmaticlegacy.sphereNavigation.target")
                    .append(": ")
                    .append(new ItemStack(findBlock).getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && getFindBlock(stack) != null) {
            int dmg = stack.getDamageValue();
            stack.setDamageValue(dmg == 0 ? MAX_COOLDOWN : 0);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F,
                    dmg == 0 ? 0.8F : 1.2F);

            if (world.isClientSide) {
                player.displayClientMessage(
                        new TranslatableComponent(dmg == 0 ?
                                "ex_enigmaticlegacy.sphereNavigation.disabled" :
                                "ex_enigmaticlegacy.sphereNavigation.enabled"),
                        true
                );
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block != null) {
                ItemStack renderStack = new ItemStack(block, 1);
                setFindBlock(stack, block, 0);

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 1.2F);

                if (world.isClientSide) {
                    ItemsRemainingRender.set(renderStack, renderStack.getHoverName().getString());
                    player.displayClientMessage(
                            new TranslatableComponent("ex_enigmaticlegacy.sphereNavigation.set")
                                    .append(": ")
                                    .append(renderStack.getHoverName()),
                            true
                    );
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof Player player)) return;

        if (world.isClientSide) {
            Block findBlock = getFindBlock(stack);
            if (findBlock != null && stack.getDamageValue() == 0 && canWork(stack)) {
                findBlocks(world, findBlock, getFindMeta(stack), player);
            }
            return;
        }

        Block findBlock = getFindBlock(stack);
        if (findBlock != null && stack.getDamageValue() == 0 && canWork(stack)) {
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
                setMaxTick(stack);
            } else {
                stack.setDamageValue(MAX_COOLDOWN);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void findBlocks(Level world, Block findBlock, int findMeta, Player player) {
        if (!world.isClientSide) return;

        ItemStack renderStack = null;
        int maxDisplayBlocks = 32;
        int totalFoundBlocks = 0;
        int displayedBlocks = 0;
        BlockPos playerPos = player.blockPosition();

        for (int y = -32; y < 16; y++) {
            for (int x = -RANGE_SEARCH; x < RANGE_SEARCH; x++) {
                for (int z = -RANGE_SEARCH; z < RANGE_SEARCH; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (pos.getY() < world.getMinBuildHeight()) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block == findBlock) {
                        if (renderStack == null) {
                            renderStack = new ItemStack(block, 1);
                        }
                        totalFoundBlocks++;

                        if (displayedBlocks < maxDisplayBlocks) {
                            int remaining = totalFoundBlocks - displayedBlocks;
                            if (world.random.nextInt(remaining) == 0 || displayedBlocks == 0) {
                                displayedBlocks++;
                                spawnParticlesForBlock(world, pos, x, y, z);
                            }
                        }
                    }
                }
            }
        }

        if (renderStack != null) {
            ItemsRemainingRender.set(renderStack,
                    new TranslatableComponent("ex_enigmaticlegacy.sphereNavigation.founded").getString()
                            + " " + totalFoundBlocks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnParticlesForBlock(Level world, BlockPos pos, int relX, int relY, int relZ) {
        float maxAge = 2.7F + 0.5F * (float)Math.random();

        float distance = (float)(Math.abs(relX) + Math.min(16, Math.abs(relY)) + Math.abs(relZ));
        float far = 120.0F - distance / 64.0F * 120.0F;
        if (far <= 70.0F) {
            far *= 0.1F;
        }

        Color color = new Color(Color.HSBtoRGB(
                far / 360.0F,
                0.9F + (float)(Math.random() * 0.1),
                1.0F));

        for (int i = 0; i < 11; i++) {
            double particleX = pos.getX() + 0.5 + (Math.random() - 0.5);
            double particleY = pos.getY() + 0.5 + (Math.random() - 0.5);
            double particleZ = pos.getZ() + 0.5 + (Math.random() - 0.5);

            WispParticleData data = WispParticleData.wisp(
                    0.3F + (float)(Math.random() * 0.25),
                    color.getRed() / 255.0F,
                    color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F,
                    maxAge,
                    false
            );

            world.addParticle(data, particleX, particleY, particleZ, 0, 0, 0);
        }
    }


    public boolean canWork(ItemStack stack) {
        int tick = ItemNBTHelper.getInt(stack, "cooldown", 0);
        if (tick == 0) {
            return true;
        }
        if (tick > 0) {
            ItemNBTHelper.setInt(stack, "cooldown", tick - 1);
        }
        return false;
    }

    public void setMaxTick(ItemStack stack) {
        ItemNBTHelper.setInt(stack, "cooldown", MAX_COOLDOWN);
    }

    public static void setFindBlock(ItemStack stack, Block block, int meta) {
        String blockId = ForgeRegistries.BLOCKS.getKey(block).toString();
        ItemNBTHelper.setString(stack, "findBlockID", blockId);
        ItemNBTHelper.setInt(stack, "findBlockMeta", meta);
    }

    @Nullable
    public static Block getFindBlock(ItemStack stack) {
        String blockID = ItemNBTHelper.getString(stack, "findBlockID", "");
        if (blockID.isEmpty()) {
            return null;
        }
        return ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(blockID));
    }

    public static int getFindMeta(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "findBlockMeta", -1);
    }
}
