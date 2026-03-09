package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

public class GenEnergydandronTile extends TileEntityFunctionalFlower {

    private static final int COST = 2000;
    private static final int MAX_MANA = 6000;
    private static final int COOLDOWN = 60;
    private static final int RANGE_X = 5;
    private static final int RANGE_Y = 4;

    private int cooldownTime = 0;

    public GenEnergydandronTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<GenEnergydandronTile> {
        public FunctionalWandHud(GenEnergydandronTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (getLevel().isClientSide) {
            return;
        }

        if (cooldownTime > 0) {
            cooldownTime--;
            return;
        }

        if (getMana() >= COST) {
            boolean shouldStrike = true;
            if (shouldStrike) {

                strikeAround();

                addMana(-COST);

                cooldownTime = COOLDOWN;
            }
        }
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return null;
    }

    private void strikeAround() {
        BlockPos pos = getBlockPos();

        BlockPos[] positions = {
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };

        for (BlockPos strikePos : positions) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(getLevel());
            if (lightning != null) {
                lightning.moveTo(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                getLevel().addFreshEntity(lightning);
            }
        }
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x9999FF;
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }

    @Override
    public int getBindingRadius() {
        return Math.max(RANGE_X, RANGE_Y);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    @Override
    @Nullable
    public BlockPos findClosestTarget() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();

        BlockPos pos = getBlockPos();

        var closestPool = network.getClosestPool(pos, getLevel(), Math.max(RANGE_X, RANGE_Y));

        if (closestPool != null) {
            BlockPos poolPos = closestPool.getManaReceiverPos();
            int dx = Math.abs(poolPos.getX() - pos.getX());
            int dy = Math.abs(poolPos.getY() - pos.getY());
            int dz = Math.abs(poolPos.getZ() - pos.getZ());

            if (dx <= RANGE_X && dy <= RANGE_Y && dz <= RANGE_X) {
                return poolPos;
            }
        }

        return null;
    }
}
