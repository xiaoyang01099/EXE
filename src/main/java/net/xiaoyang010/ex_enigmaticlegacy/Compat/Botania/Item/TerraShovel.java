package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.ForgeEventFactory;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.ISequentialBreaker;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.item.ItemTemperanceStone;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelShovel;
import vazkii.botania.common.item.relic.ItemThorRing;

public class TerraShovel extends ItemManasteelShovel implements ISequentialBreaker {

    private static final String TAG_ENABLED = "enabled";
    private static final String TAG_MODE = "function_mode";
    private static final int MANA_PER_DAMAGE = 100;
    private static final int MANA_PER_BONEMEAL = 300;

    public static final int MODE_BONEMEAL = 0; // 催熟作物
    public static final int MODE_TILL = 1;     // 耕地
    public static final int MODE_PATH = 2;     // 制作小径

    private static final String[] MODE_LANG_KEYS = {
            "item.ex_enigmaticlegacy.terra_shovel.mode.bonemeal",
            "item.ex_enigmaticlegacy.terra_shovel.mode.till",
            "item.ex_enigmaticlegacy.terra_shovel.mode.path"
    };

    public TerraShovel(Properties props) {
        super(BotaniaAPI.instance().getTerrasteelItemTier(), props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.tooltip.line1"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.tooltip.line2"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.tooltip.line3"));

        int currentMode = getFunctionMode(stack);
        String modeKey = MODE_LANG_KEYS[currentMode];
        Component modeText = new TranslatableComponent(modeKey);
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.tooltip.mode", modeText));

        boolean enabled = isEnabled(stack);
        String statusKey = enabled ?
                "item.ex_enigmaticlegacy.terra_shovel.status.enabled" :
                "item.ex_enigmaticlegacy.terra_shovel.status.disabled";
        ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component statusText = new TranslatableComponent(statusKey).withStyle(statusColor);
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.tooltip.status", statusText));
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.isShiftKeyDown()) {
            cycleFunctionMode(stack, player);
            return true;
        }

        BlockHitResult raycast = ToolCommons.raytraceFromEntity(player, 10, false);
        if (raycast.getType() == HitResult.Type.BLOCK) {
            Direction face = raycast.getDirection();
            breakOtherBlock(player, stack, pos, pos, face);
            BotaniaAPI.instance().breakOnAllCursors(player, stack, pos, face);
        }

        return false;
    }

    @Override
    public int getManaPerDamage() {
        return MANA_PER_DAMAGE;
    }

    @Override
    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, BlockPos originPos,
                                Direction side) {
        if (player.isShiftKeyDown() || !isEnabled(stack))
            return;

        Level world = player.level;
        BlockState blockstate = world.getBlockState(pos);

        if (!blockstate.getMaterial().isReplaceable() && !blockstate.is(BlockTags.MINEABLE_WITH_SHOVEL)
                && !(blockstate.getBlock() instanceof BonemealableBlock))
            return;

        if (world.isEmptyBlock(pos))
            return;

        boolean crop = blockstate.getBlock() instanceof BonemealableBlock
                && !blockstate.is(BlockTags.MINEABLE_WITH_SHOVEL);
        boolean thor = !ItemThorRing.getThorRing(player).isEmpty();
        boolean doX = thor || side.getStepX() == 0;
        boolean doY = (thor || side.getStepY() == 0) && !crop;
        boolean doZ = thor || side.getStepZ() == 0;

        int range = 2 + (thor ? 1 : 0);
        if (ItemTemperanceStone.hasTemperanceActive(player))
            range = 1;

        int rangeY = Math.max(1, range);

        Vec3i beginDiff = new Vec3i(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
        Vec3i endDiff = new Vec3i(doX ? range : 0, doY ? rangeY * 2 - 1 : 0, doZ ? range : 0);
        Predicate<BlockState> filter = state -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        if (crop)
            filter = state -> state.getBlock() instanceof BonemealableBlock;

        ToolCommons.removeBlocksInIteration(player, stack, world, pos, beginDiff, endDiff, filter);
    }

    public static boolean isEnabled(ItemStack stack) {
        return getNBTBoolean(stack, TAG_ENABLED, false);
    }

    void setEnabled(ItemStack stack, boolean enabled) {
        setNBTBoolean(stack, TAG_ENABLED, enabled);
    }

    public static int getFunctionMode(ItemStack stack) {
        return getNBTInt(stack, TAG_MODE, MODE_BONEMEAL);
    }

    void setFunctionMode(ItemStack stack, int mode) {
        setNBTInt(stack, TAG_MODE, mode);
    }

    private void cycleFunctionMode(ItemStack stack, Player player) {
        int currentMode = getFunctionMode(stack);
        int nextMode = (currentMode + 1) % 3;
        setFunctionMode(stack, nextMode);

        if (!player.level.isClientSide) {
            Component message = new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.mode_change")
                    .withStyle(ChatFormatting.GREEN)
                    .append(new TranslatableComponent(MODE_LANG_KEYS[nextMode])
                            .withStyle(ChatFormatting.YELLOW));
            player.displayClientMessage(message, false);

            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.terraPickMode, SoundSource.PLAYERS, 0.3F, 1.0F + nextMode * 0.2F);
        }
    }

    private static boolean getNBTBoolean(ItemStack stack, String key, boolean defaultValue) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains(key) ? tag.getBoolean(key) : defaultValue;
    }

    private static void setNBTBoolean(ItemStack stack, String key, boolean value) {
        stack.getOrCreateTag().putBoolean(key, value);
    }

    private static int getNBTInt(ItemStack stack, String key, int defaultValue) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains(key) ? tag.getInt(key) : defaultValue;
    }

    private static void setNBTInt(ItemStack stack, String key, int value) {
        stack.getOrCreateTag().putInt(key, value);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        setEnabled(stack, !isEnabled(stack));
        if (!world.isClientSide) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.terraPickMode,
                    SoundSource.PLAYERS, 0.5F, 0.4F);

            String statusKey = isEnabled(stack) ?
                    "item.ex_enigmaticlegacy.terra_shovel.status.enabled" :
                    "item.ex_enigmaticlegacy.terra_shovel.status.disabled";
            ChatFormatting statusColor = isEnabled(stack) ? ChatFormatting.GREEN : ChatFormatting.RED;

            Component message = new TranslatableComponent("item.ex_enigmaticlegacy.terra_shovel.toggle")
                    .withStyle(ChatFormatting.GREEN)
                    .append(new TranslatableComponent(statusKey)
                            .withStyle(statusColor));
            player.displayClientMessage(message, false);
        }
        return InteractionResultHolder.success(stack);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (player == null || !player.mayUseItemAt(pos, ctx.getClickedFace(), stack))
            return InteractionResult.PASS;

        Block block = world.getBlockState(pos).getBlock();

        int range = 2 + (!ItemThorRing.getThorRing(player).isEmpty() ? 1 : 0);
        if (ItemTemperanceStone.hasTemperanceActive(player))
            range = 1;
        if (player.isShiftKeyDown() || !isEnabled(stack))
            range = 0;

        int startX = pos.getX() - range;
        int endX = pos.getX() + range;
        int endZ = pos.getZ() + range;

        int currentMode = getFunctionMode(stack);

        switch (currentMode) {
            case MODE_BONEMEAL:
                return handleBonemealMode(ctx, block, startX, endX, endZ);
            case MODE_TILL:
                return handleTillMode(ctx, block, startX, endX, endZ);
            case MODE_PATH:
                return handlePathMode(ctx, block, startX, endX, endZ);
            default:
                return InteractionResult.PASS;
        }
    }

    private InteractionResult handleBonemealMode(UseOnContext ctx, Block block, int startX, int endX, int endZ) {
        if (!(block instanceof BonemealableBlock)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockPos pos1 = new BlockPos(startX, pos.getY(), pos.getZ() - (endX - startX) / 2);
        boolean success = false;
        while (pos1.getZ() <= endZ) {
            while (pos1.getX() <= endX) {
                success = bonemealCrop(ctx, pos1) == InteractionResult.SUCCESS || success;
                pos1 = pos1.offset(1, 0, 0);
            }
            pos1 = new BlockPos(startX, pos1.getY(), pos1.getZ() + 1);
        }
        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private InteractionResult handleTillMode(UseOnContext ctx, Block block, int startX, int endX, int endZ) {
        BlockState tilled = getTilledState(block);
        if (tilled == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockPos pos1 = new BlockPos(startX, pos.getY(), pos.getZ() - (endX - startX) / 2);
        InteractionResult result = InteractionResult.PASS;
        while (pos1.getZ() <= endZ) {
            while (pos1.getX() <= endX) {
                if (pos1.equals(pos)) {
                    result = tillBlock(pos1, ctx);
                    if (ctx.getLevel().getBlockState(pos1).getBlock() == tilled.getBlock()) {
                        result = InteractionResult.SUCCESS;
                    }
                } else {
                    tillBlock(pos1, ctx);
                }
                pos1 = pos1.offset(1, 0, 0);
            }
            pos1 = new BlockPos(startX, pos1.getY(), pos1.getZ() + 1);
        }
        return result;
    }

    private InteractionResult handlePathMode(UseOnContext ctx, Block block, int startX, int endX, int endZ) {
        if (ctx.getClickedFace() != Direction.DOWN &&
                ctx.getLevel().getBlockState(ctx.getClickedPos().above()).isAir() &&
                block == Blocks.GRASS_BLOCK) {

            BlockState converted = Blocks.DIRT_PATH.defaultBlockState();
            Level world = ctx.getLevel();
            BlockPos pos = ctx.getClickedPos();
            Player player = ctx.getPlayer();

            world.playSound(null, pos, converted.getSoundType().getStepSound(), SoundSource.BLOCKS,
                    (converted.getSoundType().getVolume() + 1.0F) / 2.0F, converted.getSoundType().getPitch() * 0.8F);

            if (world.isClientSide)
                return InteractionResult.SUCCESS;
            else {
                BlockPos pos1 = new BlockPos(startX, pos.getY(), pos.getZ() - (endX - startX) / 2);
                while (pos1.getZ() <= endZ) {
                    while (pos1.getX() <= endX) {
                        if (world.getBlockState(pos1).getBlock() == Blocks.GRASS_BLOCK &&
                                world.getBlockState(pos1.above()).isAir() &&
                                player.mayUseItemAt(pos1, ctx.getClickedFace(), ctx.getItemInHand()))
                            world.setBlockAndUpdate(pos1, converted);
                        pos1 = pos1.offset(1, 0, 0);
                    }
                    pos1 = new BlockPos(startX, pos1.getY(), pos1.getZ() + 1);
                }
                ctx.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private BlockState getTilledState(Block block) {
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT ||
                block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.ROOTED_DIRT) {
            return Blocks.FARMLAND.defaultBlockState();
        }
        return null;
    }

    private InteractionResult tillBlock(BlockPos pos, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        Level world = ctx.getLevel();

        if (player != null && player.mayUseItemAt(pos, ctx.getClickedFace(), stack)) {
            Block block = world.getBlockState(pos).getBlock();
            BlockState converted = getTilledState(block);

            if (converted == null)
                return InteractionResult.PASS;

            if (ctx.getClickedFace() != Direction.DOWN && world.getBlockState(pos.above()).isAir()) {
                world.playSound(null, pos, converted.getSoundType().getStepSound(), SoundSource.BLOCKS,
                        (converted.getSoundType().getVolume() + 1.0F) / 2.0F,
                        converted.getSoundType().getPitch() * 0.8F);

                if (world.isClientSide)
                    return InteractionResult.SUCCESS;
                else {
                    world.setBlockAndUpdate(pos, converted);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack before, @Nonnull ItemStack after, boolean slotChanged) {
        return after.getItem() != this || isEnabled(before) != isEnabled(after) ||
                getFunctionMode(before) != getFunctionMode(after);
    }

    private InteractionResult bonemealCrop(UseOnContext ctx, BlockPos pos) {
        if (!ManaItemHandler.instance().requestManaExactForTool(ctx.getItemInHand(), (Player) ctx.getPlayer(),
                MANA_PER_BONEMEAL, false))
            return InteractionResult.PASS;
        Level world = ctx.getLevel();
        BlockPos pos1 = pos.relative(ctx.getClickedFace());
        if (applyBonemeal(ctx.getItemInHand(), world, pos, ctx.getPlayer())) {
            if (!world.isClientSide) {
                world.levelEvent(2005, pos, 0);
            }

            ManaItemHandler.instance().requestManaExactForTool(ctx.getItemInHand(), (Player) ctx.getPlayer(),
                    MANA_PER_BONEMEAL, true);
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate = world.getBlockState(pos);
            boolean flag = blockstate.isFaceSturdy(world, pos, ctx.getClickedFace());
            if (flag && growSeagrass(ctx.getItemInHand(), world, pos1, ctx.getClickedFace())) {
                if (!world.isClientSide) {
                    world.levelEvent(2005, pos1, 0);
                }

                ManaItemHandler.instance().requestManaExactForTool(ctx.getItemInHand(), (Player) ctx.getPlayer(),
                        MANA_PER_BONEMEAL, true);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static boolean applyBonemeal(ItemStack stack, Level worldIn, BlockPos pos,
                                        Player player) {
        BlockState blockstate = worldIn.getBlockState(pos);
        int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, pos, blockstate, stack);
        if (hook != 0)
            return hook > 0;
        if (blockstate.getBlock() instanceof BonemealableBlock) {
            BonemealableBlock igrowable = (BonemealableBlock) blockstate.getBlock();
            if (igrowable.isValidBonemealTarget(worldIn, pos, blockstate, worldIn.isClientSide)) {
                if (worldIn instanceof ServerLevel) {
                    if (igrowable.isBonemealSuccess(worldIn, worldIn.random, pos, blockstate)) {
                        igrowable.performBonemeal((ServerLevel) worldIn, worldIn.random, pos, blockstate);
                    }
                }
                return true;
            }
        }

        return false;
    }

    public static boolean growSeagrass(ItemStack stack, Level worldIn, BlockPos pos, @Nullable Direction side) {
        if (worldIn.getBlockState(pos).is(Blocks.WATER) && worldIn.getFluidState(pos).getAmount() == 8) {
            if (!(worldIn instanceof ServerLevel)) {
                return true;
            } else {
                label80: for (int i = 0; i < 128; ++i) {
                    BlockPos blockpos = pos;
                    BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();

                    for (int j = 0; j < i / 16; ++j) {
                        blockpos = blockpos.offset(worldIn.random.nextInt(3) - 1,
                                (worldIn.random.nextInt(3) - 1) * worldIn.random.nextInt(3) / 2,
                                worldIn.random.nextInt(3) - 1);
                        if (worldIn.getBlockState(blockpos).canOcclude()) {
                            continue label80;
                        }
                    }

                    ResourceKey<Biome> biomeKey = worldIn.getBiome(blockpos).unwrapKey().orElse(null);

                    if (biomeKey != null &&
                            (biomeKey.equals(Biomes.WARM_OCEAN) || biomeKey.equals(Biomes.DEEP_LUKEWARM_OCEAN))) {
                        if (i == 0 && side != null && side.getAxis().isHorizontal()) {
                            blockstate = Blocks.TUBE_CORAL_WALL_FAN.defaultBlockState()
                                    .setValue(HorizontalDirectionalBlock.FACING, side);
                        } else if (worldIn.random.nextInt(4) == 0) {
                            blockstate = Blocks.SEAGRASS.defaultBlockState();
                        }
                    }

                    if (blockstate.is(BlockTags.WALL_CORALS)) {
                        for (int k = 0; !blockstate.canSurvive(worldIn, blockpos) && k < 4; ++k) {
                            blockstate = blockstate.setValue(HorizontalDirectionalBlock.FACING,
                                    Direction.Plane.HORIZONTAL.getRandomDirection(worldIn.random));
                        }
                    }

                    if (blockstate.canSurvive(worldIn, blockpos)) {
                        BlockState blockstate1 = worldIn.getBlockState(blockpos);
                        if (blockstate1.is(Blocks.WATER) && worldIn.getFluidState(blockpos).getAmount() == 8) {
                            worldIn.setBlock(blockpos, blockstate, 3);
                        } else if (blockstate1.is(Blocks.SEAGRASS) && worldIn.random.nextInt(10) == 0) {
                            ((BonemealableBlock) Blocks.SEAGRASS).performBonemeal((ServerLevel) worldIn, worldIn.random, blockpos, blockstate1);
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }
}