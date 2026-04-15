package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.PolychromeCollapsePrismTile;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModRecipes;
import vazkii.botania.common.block.BotaniaBlock;
import vazkii.botania.mixin.RecipeManagerAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PolychromeCollapsePrism extends BotaniaBlock implements EntityBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

    public PolychromeCollapsePrism(Properties builder) {
        super(builder);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && usesItem(stack, world)) {
            if (!world.isClientSide) {
                ItemStack target = stack.split(1);
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, target);
                item.setPickUpDelay(40);
                item.setDeltaMovement(Vec3.ZERO);
                world.addFreshEntity(item);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean usesItem(ItemStack stack, Level world) {
        for (Recipe<?> value : ((RecipeManagerAccessor) world.getRecipeManager()).botania_getAll(ModRecipes.POLYCHROME_TYPE.get()).values()) {
            for (Ingredient i : value.getIngredients()) {
                if (i.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new PolychromeCollapsePrismTile(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), PolychromeCollapsePrismTile::serverTick);
        }
        return null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        PolychromeCollapsePrismTile plate = (PolychromeCollapsePrismTile) world.getBlockEntity(pos);
        return plate.getComparatorLevel();
    }
}