package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ChtMenuData;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

public class CelestialHTTile extends RandomizableContainerBlockEntity implements MenuProvider {
	private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
	private ChtMenuData data = new ChtMenuData();

	public CelestialHTTile(BlockPos position, BlockState state) {
		super(ModBlockEntities.CELESTIAL_HOLINESS_TRANSMUTER_TILE.get(), position, state);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.items);
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
	public void setItem(int pIndex, ItemStack pStack) {
		this.items.set(pIndex, pStack);
	}

	@Override
	public ItemStack getItem(int pIndex) {
		return this.items.get(pIndex);
	}

	@Override
	public ItemStack removeItem(int pIndex, int pCount) {
		return ContainerHelper.removeItem(this.items, pIndex, pCount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pIndex) {
		return ContainerHelper.takeItem(this.items, pIndex);
	}

	@Override
	protected Component getDefaultName() {
		return new TextComponent("celestial_holiness_transmuter");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new CelestialHTMenu(id, player, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.getBlockPos()), data);
	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("Celestial Holiness Transmuter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new CelestialHTMenu(id, playerInventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.getBlockPos()), data);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CelestialHTTile tile) {
		if (level.isClientSide) return;

		ItemStack result = tile.getItem(0);

		boolean isInput = true;

		for (int j = 1; j < 5; j++) {
			if (tile.getItem(j).isEmpty()) {
				isInput = false;
				tile.data.set(0, 0);
				setChanged(level, pos, state);
			}
		}

		if (!isInput) return;

		Optional<CelestialTransmuteRecipe> recipes = level.getRecipeManager().getRecipeFor(ModRecipes.CHT_TYPE, tile, level);
		recipes.ifPresent(recipe -> {
			ItemStack resultItem = recipe.getResultItem();
			if (resultItem.isEmpty()) return;

			if (result.isEmpty() || (resultItem.getItem() == result.getItem()
					&& result.getCount() + resultItem.getCount() <= result.getMaxStackSize())) {

				tile.data.set(0, tile.data.get(0) + 1);
				if (tile.data.get(0) <= 200) {
					setChanged(level, pos, state);
					return;
				}

				boolean hasEnoughItems = true;
				NonNullList<ItemStack> tempInputs = NonNullList.withSize(4, ItemStack.EMPTY);

				for (int i = 0; i < 4; i++) {
					tempInputs.set(i, tile.getItem(i + 1).copy());
					int required = recipe.getInputCounts().get(i);
					if (tempInputs.get(i).getCount() < required) {
						hasEnoughItems = false;
						break;
					}
				}

				if (hasEnoughItems) {
					for (int i = 0; i < 4; i++) {
						int required = recipe.getInputCounts().get(i);
						ItemStack currentItem = tile.getItem(i + 1);
						currentItem.setCount(currentItem.getCount() - required);
					}

					if (result.isEmpty()) {
						tile.setItem(0, resultItem.copy());
					} else {
						result.grow(resultItem.getCount());
					}

					tile.data.set(0, 0);
					setChanged(level, pos, state);
				}
			}
		});
	}
}