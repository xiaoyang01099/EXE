package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud.ItemsRemainingRender;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.FindBlocksPacket;
import net.minecraftforge.network.PacketDistributor;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicItem;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class SphereNavigation extends RelicItem {
    public static final int RANGE_SEARCH = 16;
    public static final int MAX_COOLDOWN = 158;
    public static final int MANA_COST = 50;
    private static final String TAG_ENABLED = "navigationEnabled";

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
            return super.getName(stack).copy()
                    .append(ChatFormatting.RESET + " (")
                    .append(block.getName().copy().withStyle(ChatFormatting.GREEN))
                    .append(ChatFormatting.RESET + ")");
        }
        return super.getName(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        boolean active = isEnabled(stack);
        tooltip.add(Component.translatable(active ? "botaniamisc.active" : "botaniamisc.inactive"));

        Block findBlock = getFindBlock(stack);
        if (findBlock != null) {
            tooltip.add(Component.translatable("ex_enigmaticlegacy.sphereNavigation.target")
                    .append(": ")
                    .append(findBlock.getName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Use Botania's relic convention so the client predicts the toggle
        // immediately and the server persists the same state.
        if (hand == InteractionHand.MAIN_HAND && player.isSecondaryUseActive()
                && getFindBlock(stack) != null) {
            boolean enabled = !isEnabled(stack);
            setEnabled(stack, enabled);
            if (!world.isClientSide) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F,
                        enabled ? 1.2F : 0.8F);
                player.displayClientMessage(
                        Component.translatable(enabled ?
                                "ex_enigmaticlegacy.sphereNavigation.enabled" :
                                "ex_enigmaticlegacy.sphereNavigation.disabled"),
                        true
                );
            }

            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
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

            if (!state.isAir()) {
                ItemStack renderStack = new ItemStack(block, 1);
                if (world.isClientSide) {
                    ItemsRemainingRender.set(renderStack, block.getName().getString());
                } else {
                    setFindBlock(stack, block, 0);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 1.2F);
                    player.displayClientMessage(
                            Component.translatable("ex_enigmaticlegacy.sphereNavigation.set")
                                    .append(": ")
                                    .append(block.getName()),
                            true
                    );
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClientSide || !(entity instanceof Player player) || !isEnabled(stack)) return;

        Block findBlock = getFindBlock(stack);
        if (findBlock == null) return;
        int cooldown = ItemNBTHelper.getInt(stack, "cooldown", 0);
        if (cooldown > 0) {
            ItemNBTHelper.setInt(stack, "cooldown", Math.min(cooldown, MAX_COOLDOWN) - 1);
            return;
        }
        if (canWork(stack)) {
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
                setMaxTick(stack);
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new FindBlocksPacket(findBlock, getFindMeta(stack)));
                }
            } else {
                setEnabled(stack, false);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void findBlocks(Level world, Block findBlock, int findMeta, Player player) {
        if (!world.isClientSide || findBlock == Blocks.AIR) return;

        ItemStack renderStack = new ItemStack(findBlock);
        int maxDisplayBlocks = 32;
        int totalFoundBlocks = 0;
        List<BlockPos> highlights = new ArrayList<>(maxDisplayBlocks);
        BlockPos playerPos = player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = -32; y < 16; y++) {
            for (int x = -RANGE_SEARCH; x < RANGE_SEARCH; x++) {
                for (int z = -RANGE_SEARCH; z < RANGE_SEARCH; z++) {
                    pos.setWithOffset(playerPos, x, y, z);
                    if (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block == findBlock) {
                        totalFoundBlocks++;
                        if (highlights.size() < maxDisplayBlocks) {
                            highlights.add(pos.immutable());
                        } else {
                            int replacement = world.random.nextInt(totalFoundBlocks);
                            if (replacement < maxDisplayBlocks) highlights.set(replacement, pos.immutable());
                        }
                    }
                }
            }
        }

        for (BlockPos highlight : highlights) {
            spawnParticlesForBlock(world, highlight, highlight.getX() - playerPos.getX(),
                    highlight.getY() - playerPos.getY(), highlight.getZ() - playerPos.getZ());
        }
        ItemsRemainingRender.set(renderStack.isEmpty() ? new ItemStack(Items.COMPASS) : renderStack,
                Component.translatable("ex_enigmaticlegacy.sphereNavigation.founded").getString()
                        + " " + totalFoundBlocks);
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
        return ItemNBTHelper.getInt(stack, "cooldown", 0) <= 0;
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_ENABLED)
                ? stack.getTag().getBoolean(TAG_ENABLED) : stack.getDamageValue() == 0;
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(TAG_ENABLED, enabled);
        stack.getOrCreateTag().remove("Damage");
    }

    public void setMaxTick(ItemStack stack) {
        ItemNBTHelper.setInt(stack, "cooldown", MAX_COOLDOWN);
    }

    public static void setFindBlock(ItemStack stack, Block block, int meta) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null || block == Blocks.AIR) return;
        ItemNBTHelper.setString(stack, "findBlockID", blockId.toString());
        ItemNBTHelper.setInt(stack, "findBlockMeta", meta);
        ItemNBTHelper.setInt(stack, "cooldown", 0);
    }

    @Nullable
    public static Block getFindBlock(ItemStack stack) {
        String blockID = ItemNBTHelper.getString(stack, "findBlockID", "");
        if (blockID.isEmpty()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(blockID);
        if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return block == Blocks.AIR ? null : block;
    }

    public static int getFindMeta(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "findBlockMeta", -1);
    }
}
