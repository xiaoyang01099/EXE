package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.common.block.BlockModWaterloggable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 魔力转换器方块
 */
public class BlockManaConverter extends BlockModWaterloggable implements EntityBlock {

    public static final BooleanProperty CONVERTING = BooleanProperty.create("converting");
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public enum ConversionMode {
        NORMAL_TO_CURSED,  // 原版魔力 -> 诅咒魔力
        CURSED_TO_NORMAL   // 诅咒魔力 -> 原版魔力
    }

    private final ConversionMode mode;

    public BlockManaConverter(ConversionMode mode, Properties properties) {
        super(properties);
        this.mode = mode;
        registerDefaultState(defaultBlockState().setValue(CONVERTING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONVERTING);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        if (mode == ConversionMode.NORMAL_TO_CURSED) {
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.mana_converter.normal_to_cursed.tooltip"));
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.mana_converter.warning"));
        } else {
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.mana_converter.cursed_to_normal.tooltip"));
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.mana_converter.purify"));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileManaConverter converter) {
                player.displayClientMessage(
                        new TranslatableComponent("block.ex_enigmaticlegacy.mana_converter.info",
                                converter.getCorruptionLevel(),
                                String.format("%.1f%%", converter.getConversionEfficiency() * 100)
                        ), true
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileManaConverter(pos, state, mode);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MANA_CONVERTER.get(),
                level.isClientSide ? TileManaConverter::clientTick : TileManaConverter::serverTick);
    }
}
