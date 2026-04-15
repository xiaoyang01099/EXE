package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileManaBox;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.BlockItemManaBox;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import vazkii.botania.common.block.BotaniaBlock;
import vazkii.botania.common.entity.ManaBurstEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class BlockManaBox extends BotaniaBlock implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public BlockManaBox() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(2.0F, 2000.0F)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 0)
                .noOcclusion()
                .isValidSpawn(BlockManaBox::neverAllowSpawn)
                .isRedstoneConductor(BlockManaBox::isNotSolid)
                .isSuffocating(BlockManaBox::isNotSolid)
                .isViewBlocking(BlockManaBox::isNotSolid)
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == this || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public @NotNull VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty()) {
            return dropsOriginal;
        } else {
            ItemStack stack = new ItemStack(ModItems.MANA_BOX_ITEM.get());
            TileManaBox manaBox = (TileManaBox) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (manaBox != null) {
                BlockItemManaBox.setMana(stack, manaBox.getCurrentMana());
                manaBox.getColor().ifPresent(color -> BlockItemManaBox.setColor(stack, color));
            }
            return Collections.singletonList(stack);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileManaBox manaBox) {
            manaBox.setMana(BlockItemManaBox.getBlockMana(stack));
            DyeColor color = BlockItemManaBox.getColor(stack);
            manaBox.setColor(Optional.ofNullable(color));
        }
    }
    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos,
                                 Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem && tileEntity instanceof TileManaBox manaBox) {
            DyeColor color = ((DyeItem) stack.getItem()).getDyeColor();
            if (!manaBox.getColor().map(c -> c == color).orElse(false)) {
                manaBox.setColor(Optional.of(color));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof ManaBurstEntity) {
            return SHAPE;
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileManaBox(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MANA_BOX_TILE.get(),
                level.isClientSide ? TileManaBox::clientTick : TileManaBox::serverTick);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof TileManaBox pool) {
            return TileManaBox.calculateComparatorLevel(pool.getCurrentMana(), pool.manaCap);
        }
        return 0;
    }

    private static Boolean neverAllowSpawn(BlockState state, BlockGetter reader, BlockPos pos, EntityType<?> entity) {
        return false;
    }

    private static boolean isNotSolid(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }
}