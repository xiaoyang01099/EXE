package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

public class TerraHoe extends HoeItem {
    private static final int MANA_PER_DAMAGE = 60;

    public TerraHoe(Properties tab) {
        super(BotaniaAPI.instance().getTerrasteelItemTier(), -3, 0.0F,
                new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction face = context.getClickedFace();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }

        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        BlockPos abovePos = pos.above();

        if (face != Direction.DOWN && level.getBlockState(abovePos).isAir() &&
                (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT)) {

            Block targetBlock = ModBlockss.TERRA_FARMLAND.get();

            level.playSound(player, pos, targetBlock.getSoundType(targetBlock.defaultBlockState()).getBreakSound(),
                    SoundSource.BLOCKS,
                    (targetBlock.getSoundType(targetBlock.defaultBlockState()).getVolume() + 1.0F) / 2.0F,
                    targetBlock.getSoundType(targetBlock.defaultBlockState()).getPitch() * 0.8F);

            if (level instanceof ServerLevel) {
                level.setBlockAndUpdate(pos, targetBlock.defaultBlockState());
                damageItem(stack, 1, player);
                return InteractionResult.SUCCESS;
            } else {
                createTerraformParticles(level, pos);
                return InteractionResult.SUCCESS;
            }
        }

        else if (player.isShiftKeyDown() && face != Direction.DOWN &&
                level.getBlockState(abovePos).isAir() &&
                block == ModBlocks.enchantedSoil) {

            if (level instanceof ServerLevel) {
                level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                damageItem(stack, 1, player);

                ItemEntity entity = new ItemEntity(level,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.0D,
                        pos.getZ() + 0.5D,
                        new ItemStack(ModItems.overgrowthSeed));
                level.addFreshEntity(entity);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }

        else if (canHoeBlock(blockState)) {
            return super.useOn(context);
        }

        return InteractionResult.PASS;
    }

    private boolean canHoeBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.DIRT_PATH ||
                block == Blocks.ROOTED_DIRT;
    }

    private void createTerraformParticles(Level level, BlockPos pos) {
        float velMul = 0.025F;
        for (int i = 0; i < 48; ++i) {
            double px = (Math.random() - 0.5D) * 3.0D;
            double py = Math.random() - 0.5D + 1.0D;
            double pz = (Math.random() - 0.5D) * 3.0D;

            BotaniaAPI.instance().sparkleFX(level,
                    pos.getX() + 0.5D + px,
                    pos.getY() + 0.5D + py,
                    pos.getZ() + 0.5D + pz,
                    0.0F, 0.4F, 0.0F,
                    (float)(Math.random() * 0.15F + 0.15F),
                    5);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel) || !(entity instanceof Player player)) {
            return;
        }

        if (stack.getDamageValue() > 0 &&
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    private void damageItem(ItemStack stack, int amount, Player player) {
        stack.hurtAndBreak(amount, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return BotaniaAPI.instance().getTerrasteelItemTier().getEnchantmentValue();
    }
}