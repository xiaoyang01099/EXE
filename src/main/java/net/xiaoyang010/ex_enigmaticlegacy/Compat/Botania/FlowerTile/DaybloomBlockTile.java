package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class DaybloomBlockTile extends TileEntityGeneratingFlower {
    public static final String TAG = ExEnigmaticlegacyMod.MODID + ":decayTicks";
    public static final int DECAY_TIME = 48000;
    private int decayTicks;

    public DaybloomBlockTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DAYBLOOM_TILE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level != null && !level.isClientSide()) {
            boolean day = level.isDay();
            long gameTime = level.getGameTime();
            if (++decayTicks >= DECAY_TIME){
                this.level.destroyBlock(this.getBlockPos(), false);
                if (Blocks.DEAD_BUSH.defaultBlockState().canSurvive(this.level, this.getBlockPos())) {
                    this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.DEAD_BUSH.defaultBlockState());
                }
                return;
            }
            if (day && gameTime % 1200 == 0) {
                addMana(100);
                setChanged();
            }
        }
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        this.decayTicks = cmp.getInt(TAG);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG, decayTicks);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return 5000;  // 最大魔力为100
    }

    @Override
    public int getColor() {
        return 0xFFFF00;  // 花的颜色
    }

    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 5);  // 设置作用半径为5格
    }

}
