package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile;

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

    private static final int COST = 2000; // 每次施法消耗的魔力
    private static final int MAX_MANA = 6000; // 最大魔力储存
    private static final int COOLDOWN = 60; // 冷却时间（ticks）
    private static final int RANGE_X = 5; // x轴检测范围
    private static final int RANGE_Y = 4; // y轴检测范围

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

        // 检查是否有足够的魔力
        if (getMana() >= COST) {
            boolean shouldStrike = true; // 可以根据需求添加额外条件

            if (shouldStrike) {
                // 在四个方向生成闪电
                strikeAround();

                // 消耗魔力
                addMana(-COST);

                // 设置冷却时间
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

        // 上下左右四个方向的位置
        BlockPos[] positions = {
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };

        // 在每个位置生成闪电
        for (BlockPos strikePos : positions) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(getLevel());
            if (lightning != null) {
                lightning.moveTo(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                lightning.setVisualOnly(true);
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
        return Math.max(RANGE_X, RANGE_Y); // 返回最大范围值，确保可以检测到指定范围内的魔力池
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GenEnergydandronTile.FunctionalWandHud(this)).cast());
    }

    @Override
    @Nullable
    public BlockPos findClosestTarget() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();

        // 获取当前花的位置
        BlockPos pos = getBlockPos();

        // 在指定范围内搜索最近的魔力池
        var closestPool = network.getClosestPool(pos, getLevel(), Math.max(RANGE_X, RANGE_Y));

        // 如果找到魔力池，检查是否在我们想要的范围内
        if (closestPool != null) {
            BlockPos poolPos = closestPool.getManaReceiverPos();
            // 检查是否在指定的矩形范围内
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
