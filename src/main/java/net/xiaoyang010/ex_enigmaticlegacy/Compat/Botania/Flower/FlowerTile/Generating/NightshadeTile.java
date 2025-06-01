package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class NightshadeTile extends TileEntityGeneratingFlower {
	public static final String TAG = "passiveDecayTicks";
	public static final int DECAY_TIME = 48000;
	private int passiveDecayTicks;

	public NightshadeTile(BlockPos pos, BlockState state) {
		super(ModBlockEntities.NIGHTSHADE_TILE.get(), pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (level != null && !level.isClientSide()) {
			boolean night = level.isNight();
			long gameTime = level.getGameTime();
			if (++passiveDecayTicks >= DECAY_TIME){
				this.level.destroyBlock(this.getBlockPos(), false);
				if (Blocks.DEAD_BUSH.defaultBlockState().canSurvive(this.level, this.getBlockPos())) {
					this.level.setBlockAndUpdate(this.getBlockPos(), Blocks.DEAD_BUSH.defaultBlockState());
				}
				return;
			}
			if (night && gameTime % 120 == 0) {
				addMana(100);
				setChanged();
			}
		}
	}

	@Override
	public int getMaxMana() {
		return 5000;
	}

	@Override
	public int getColor() {
		return 0x800080;
	}

	@Nullable
	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Circle(getBlockPos(), 5);
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
		return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		this.passiveDecayTicks = cmp.getInt(TAG);
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG, passiveDecayTicks);
	}
}
