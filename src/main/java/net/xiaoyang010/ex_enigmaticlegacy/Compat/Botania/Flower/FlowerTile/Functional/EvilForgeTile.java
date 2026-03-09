package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import com.integral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.util.List;
import java.util.ArrayList;

public class EvilForgeTile extends TileEntityFunctionalFlower {

    private static final String TAG_BURN_TIME = "burnTime";
    private static final int RANGE = 3;
    private static final int MANA_COST = 8000;
    private static final int MAX_BURN_TIME = 200;

    private int burnTime = 0;

    public EvilForgeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<EvilForgeTile> {
        public FunctionalWandHud(EvilForgeTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (getLevel() != null && getLevel().isClientSide) {
            return;
        }

        if (burnTime > 0) {
            burnTime--;
            sync();
        }

        if (burnTime <= 0 && getMana() >= MANA_COST) {
            List<ItemEntity> items = getItemsInRange();

            ItemEntity goldIngot = findItemOfType(items, Items.GOLD_INGOT);
            ItemEntity etherium = findItemOfType(items, EnigmaticLegacy.etheriumIngot);
            ItemEntity netheriteIngot = findItemOfType(items, Items.NETHERITE_INGOT);
            ItemEntity enderPearl = findItemOfType(items, Items.ENDER_PEARL);

            if (goldIngot != null && etherium != null && netheriteIngot != null && enderPearl != null) {
                addMana(-MANA_COST);

                consumeItem(goldIngot);
                consumeItem(etherium);
                consumeItem(netheriteIngot);
                consumeItem(enderPearl);

                ItemStack evilIngot = new ItemStack(
                        EnigmaticLegacy.evilIngot, 1
                );

                ItemEntity newItem = new ItemEntity(
                        getLevel(),
                        goldIngot.getX(),
                        goldIngot.getY(),
                        goldIngot.getZ(),
                        evilIngot
                );

                getLevel().addFreshEntity(newItem);

                burnTime = MAX_BURN_TIME;
                sync();
            }
        }
    }

    private List<ItemEntity> getItemsInRange() {
        List<ItemEntity> items = new ArrayList<>();
        BlockPos pos = getBlockPos();

        net.minecraft.world.phys.AABB boundingBox = new net.minecraft.world.phys.AABB(
                pos.getX() - RANGE, pos.getY() - RANGE, pos.getZ() - RANGE,
                pos.getX() + RANGE + 1, pos.getY() + RANGE + 1, pos.getZ() + RANGE + 1
        );

        getLevel().getEntitiesOfClass(ItemEntity.class, boundingBox)
                .forEach(item -> {
                    if (!item.isRemoved() && item.isAlive()) {
                        items.add(item);
                    }
                });

        return items;
    }

    private ItemEntity findItemOfType(List<ItemEntity> items, net.minecraft.world.item.Item itemType) {
        for (ItemEntity item : items) {
            if (item.getItem().is(itemType) && item.getItem().getCount() >= 1) {
                return item;
            }
        }
        return null;
    }

    private void consumeItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), RANGE);
    }

    @Override
    public int getColor() {
        return 0x301934;
    }

    @Override
    public int getMaxMana() {
        return 16000;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_BURN_TIME, burnTime);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        burnTime = cmp.getInt(TAG_BURN_TIME);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    public boolean isBurning() {
        return burnTime > 0;
    }
}
