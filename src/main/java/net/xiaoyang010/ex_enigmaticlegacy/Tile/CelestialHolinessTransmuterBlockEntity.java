package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHolinessTransmuteMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;

import java.util.Optional;

public class CelestialHolinessTransmuterBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider {
	private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);

	public CelestialHolinessTransmuterBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.CELESTIAL_HOLINESS_TRANSMUTER.get(), position, state);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.items);
	}

	private void craftItem() {
		Level level = this.level;
		SimpleContainer inventory = new SimpleContainer(this.items.size());
		for (int i = 0; i < this.items.size(); i++) {
			inventory.setItem(i, this.items.get(i));
		}

		Optional<CelestialTransmuteRecipe> recipe = level.getRecipeManager()
				.getRecipeFor(CelestialTransmuteRecipe.Type.INSTANCE, inventory, level);

		if (recipe.isPresent()) {
			this.items.set(0, recipe.get().getResultItem().copy());
			this.items.get(1).shrink(1);
			this.items.get(2).shrink(1);
			this.items.get(3).shrink(1);
			this.items.get(4).shrink(1);
			setChanged(level, this.worldPosition, this.getBlockState());
		}
	}


	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		ContainerHelper.saveAllItems(compound, this.items);
	}

	@Override
	public int getContainerSize() {
		return items.size();
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected Component getDefaultName() {
		return new TextComponent("celestial_holiness_transmuter");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new CelestialHolinessTransmuteMenu(id, player, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.getBlockPos()));
	}

	// 实现 MenuProvider 接口的方法
	@Override
	public Component getDisplayName() {
		return new TextComponent("Celestial Holiness Transmuter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new CelestialHolinessTransmuteMenu(id, playerInventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.getBlockPos()));
	}
}
