package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import vazkii.botania.common.helper.ItemNBTHelper;

import java.util.List;

public class AntigravityCharm extends Item {
    private static final String ACTIVE_KEY = "isActive";

    public AntigravityCharm(Properties properties) {
        super(new Properties()
                .stacksTo(1)
                .setNoRepair());
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            boolean isActive = player.isShiftKeyDown();
            boolean currentState = ItemNBTHelper.getBoolean(stack, ACTIVE_KEY, false);

            if (isActive != currentState) {
                ItemNBTHelper.setBoolean(stack, ACTIVE_KEY, isActive);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        boolean isActive = ItemNBTHelper.getBoolean(stack, ACTIVE_KEY, false);
        if (!isActive) return;

        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();

        double range = 8.0D;
        AABB searchBox = new AABB(
                posX - range, posY - range, posZ - range,
                posX + range, posY + range, posZ + range
        );

        List<FallingBlockEntity> fallingBlocks = world.getEntitiesOfClass(
                FallingBlockEntity.class,
                searchBox
        );

        for (FallingBlockEntity fallingBlock : fallingBlocks) {
            BlockPos fallingPos = fallingBlock.blockPosition();

            fallingBlock.setDeltaMovement(0, 0, 0);

            if (world.getBlockState(fallingPos).isAir()) {
                if (world.setBlock(fallingPos, ModBlockss.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 3)) {
                    fallingBlock.discard();
                }
            } else {
                BlockPos nearbyAir = findNearbyAirBlock(world, fallingPos);
                if (nearbyAir != null) {
                    if (world.setBlock(nearbyAir, ModBlockss.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 3)) {
                        fallingBlock.discard();
                    }
                }
            }
        }
    }

    private BlockPos findNearbyAirBlock(Level world, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos testPos = center.offset(dx, dy, dz);
                    if (world.getBlockState(testPos).isAir()) {
                        return testPos;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block block = world.getBlockState(pos).getBlock();

        if (block.defaultBlockState().getMaterial() == Material.AIR) {
            return InteractionResult.FAIL;
        }

        if (world.isEmptyBlock(pos.above())) {
            if (world.setBlock(pos.above(), ModBlockss.ANTIGRAVITATION_BLOCK.get().defaultBlockState(), 3)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }
}