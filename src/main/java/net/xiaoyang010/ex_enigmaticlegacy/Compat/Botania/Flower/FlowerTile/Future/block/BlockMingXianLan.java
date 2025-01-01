/*package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile.Future.block;



import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import vazkii.botania.common.block.BlockSpecialFlower;
import vazkii.botania.common.item.ModItems;

public class BlockMingXianLan extends BlockSpecialFlower {
    public static final EnumProperty<MingXianLanState> STATE = EnumProperty.create("state", MingXianLanState.class);

    public BlockMingXianLan(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(STATE, MingXianLanState.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        if (state.getValue(STATE) == MingXianLanState.WITHERED) {
            return new ItemStack(ModItems.WITHERED_MINGXIANLAN);
        }
        return super.getCloneItemStack(world, pos, state);
    }

    public enum MingXianLanState implements StringRepresentable {
        NORMAL,
        BLOSSOMING,
        WITHERED;

        @Override
        public String getSerializedName() {
            return "";
        }
    }
}*/