package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TerraFarmlandList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerraFarmland extends Block {
    public static List<TerraFarmlandList> farmlandList = new ArrayList<>();
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public TerraFarmland() {
        super(Properties.of(Material.DIRT)
                .strength(0.6F)
                .sound(SoundType.GRAVEL)
                .randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        PlantType plantType = plantable.getPlantType(level, pos.relative(facing));

        return plantType == PlantType.CROP ||
                plantType == PlantType.PLAINS ||
                plantType == PlantType.CAVE ||
                plantType == PlantType.BEACH;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!level.isClientSide){
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();

            if (aboveBlock instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(aboveState)) {
                    this.refreshSeed(level, pos, abovePos, aboveState);
                    return;
                }
            } else if (aboveBlock instanceof IPlantable) {
                for (TerraFarmlandList fSeed : farmlandList) {
                    BlockState targetState = fSeed.getBlockState();
                    if (aboveState.equals(targetState)) {
                        this.refreshSeed(level, pos, abovePos, aboveState);
                        return;
                    }
                }
            } else if (aboveState.isAir()) {
                return;
            } else {
                level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            }
        }
    }

    private void refreshSeed(ServerLevel level, BlockPos farmlandPos, BlockPos plantPos, BlockState plantState) {
        AABB searchArea = new AABB(
                farmlandPos.getX() - 4, farmlandPos.getY() - 4, farmlandPos.getZ() - 4,
                farmlandPos.getX() + 4, farmlandPos.getY() + 4, farmlandPos.getZ() + 4
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchArea);

        if (items.isEmpty() || items.size() <= 7) {
            Block plantBlock = plantState.getBlock();

            List<ItemStack> drops = Block.getDrops(plantState, level, plantPos, null);

            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) {
                    if (Block.byItem(stack.getItem()) == plantBlock) {
                        if (stack.getCount() > 1) {
                            stack.shrink(1);
                        }
                    } else {
                        int newCount = Math.min(64, (int)(stack.getCount() * 2.5F));
                        stack.setCount(newCount);
                    }
                }
            }

            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            plantPos.getX() + 0.5D,
                            plantPos.getY() + 1.0D,
                            plantPos.getZ() + 0.5D,
                            stack);
                    level.addFreshEntity(itemEntity);
                }
            }

            level.setBlockAndUpdate(plantPos, plantState);
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.DIRT);
    }
}