package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.common.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManaIvyRegen extends Item {
	private static final int REPAIR_MANA = 800;
	private static final int CURSE_REMOVAL_MANA = 1500;
	public static final int MAX_MANA = 500000;
	private static final String TAG_MANA = "mana";
	private static final String TAG_CREATIVE = "creative";

	public ManaIvyRegen() {
		super(new Properties()
				.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
				.stacksTo(1)
				.fireResistant()
				.rarity(Rarity.EPIC));
	}

	public static class ManaItem implements IManaItem {
		private final ItemStack stack;

		public ManaItem(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int getMana() {
			if (isStackCreative(stack)) {
				return getMaxMana();
			}
			return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
		}

		@Override
		public int getMaxMana() {
			return isStackCreative(stack) ? MAX_MANA + 1000 : MAX_MANA;
		}

		@Override
		public void addMana(int mana) {
			if (!isStackCreative(stack)) {
				setMana(stack, Math.min(getMana() + mana, getMaxMana()));
			}
		}

		@Override
		public boolean canReceiveManaFromPool(BlockEntity pool) {
			return !isStackCreative(stack);
		}

		@Override
		public boolean canReceiveManaFromItem(ItemStack otherStack) {
			return !isStackCreative(stack);
		}

		@Override
		public boolean canExportManaToPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canExportManaToItem(ItemStack otherStack) {
			return false;
		}

		@Override
		public boolean isNoExport() {
			return false;
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide && entity instanceof Player player) {
			stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).ifPresent(manaItem -> {
				BlockPos pos = entity.blockPosition();
				for (BlockPos checkPos : BlockPos.betweenClosed(
						pos.offset(-2, -2, -2),
						pos.offset(2, 2, 2))) {
					BlockEntity be = level.getBlockEntity(checkPos);
					if (be instanceof IManaPool pool) {
						int space = manaItem.getMaxMana() - manaItem.getMana();
						if (space > 0 && pool.getCurrentMana() > 0) {
							int manaToTransfer = Math.min(1000, Math.min(space, pool.getCurrentMana()));
							pool.receiveMana(-manaToTransfer);
							manaItem.addMana(manaToTransfer);
							break;
						}
					}
				}

				if (manaItem.getMana() >= REPAIR_MANA || isStackCreative(stack)) {

					for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
						ItemStack itemToRepair = player.getInventory().getItem(i);

						if (itemToRepair.isEmpty() || !itemToRepair.isDamaged()) {
							continue;
						}

						boolean hasCurseBinding = EnchantmentHelper.hasBindingCurse(itemToRepair);
						int requiredMana = hasCurseBinding ? CURSE_REMOVAL_MANA : REPAIR_MANA;

						if (manaItem.getMana() >= requiredMana || isStackCreative(stack)) {
							itemToRepair.setDamageValue(itemToRepair.getDamageValue() - 1);

							if (hasCurseBinding) {
								Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemToRepair);
								enchantments.remove(Enchantments.BINDING_CURSE);
								EnchantmentHelper.setEnchantments(enchantments, itemToRepair);
								if (!isStackCreative(stack)) {
									stack.shrink(1);
									if (stack.isEmpty()) {
										return;
									}
								}
							}

							if (!isStackCreative(stack)) {
								manaItem.addMana(-requiredMana);
							}

							level.playSound(null, player.getX(), player.getY(), player.getZ(),
									SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.3F, 1.0F);

							break;
						}
					}
				}
			});
		}
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM)
				.map(manaItem -> Math.round(13 * ManaBarTooltip.getFractionForDisplay(manaItem)))
				.orElse(0);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM)
				.map(manaItem -> Mth.hsvToRgb(ManaBarTooltip.getFractionForDisplay(manaItem) / 3.0F, 1.0F, 1.0F))
				.orElse(0);
	}

	protected static void setMana(ItemStack stack, int mana) {
		if (mana > 0) {
			ItemNBTHelper.setInt(stack, TAG_MANA, mana);
		} else {
			ItemNBTHelper.removeEntry(stack, TAG_MANA);
		}
	}

	public static void setStackCreative(ItemStack stack) {
		ItemNBTHelper.setBoolean(stack, TAG_CREATIVE, true);
	}

	public static boolean isStackCreative(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_CREATIVE, false);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return !isStackCreative(stack);
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			private final LazyOptional<IManaItem> manaHandler = LazyOptional.of(() -> new ManaItem(stack));

			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
				if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
					return manaHandler.cast();
				}
				return LazyOptional.empty();
			}
		};
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		LazyOptional<IManaItem> manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
		if (manaItem.isPresent()) {
			return Optional.of(ManaBarTooltip.fromManaItem(stack));
		}
		return Optional.empty();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		if (isStackCreative(stack)) {
			tooltip.add(new TranslatableComponent("botaniamisc.creative").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> stacks) {
		if (allowdedIn(tab)) {
			stacks.add(new ItemStack(this));

			ItemStack fullPower = new ItemStack(this);
			setMana(fullPower, MAX_MANA);
			stacks.add(fullPower);

			ItemStack creative = new ItemStack(this);
			setMana(creative, MAX_MANA);
			setStackCreative(creative);
			stacks.add(creative);
		}
	}

	@Nonnull
	@Override
	public Rarity getRarity(@Nonnull ItemStack stack) {
		return isStackCreative(stack) ? Rarity.EPIC : super.getRarity(stack);
	}
}