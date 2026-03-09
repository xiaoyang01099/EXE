package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;

public class YushouCloverTile extends TileEntityFunctionalFlower {

    private static final String TAG_EXTENDED_RANGE = "extendedRange";
    private static final String TAG_EXTENSION_TIME = "extensionTime";

    // Mana消耗配置
    private static final int MANA_COST_PER_TICK = 2; // 基础运行消耗
    private static final int MANA_COST_EXTENSION = 200; // 扩展范围的mana消耗
    private static final int EXTENSION_DURATION = 6000; // 扩展持续时间(5分钟)

    // 保护范围配置（以区块为单位）
    private static final int BASE_CHUNK_RANGE = 1; // 3x3区块，半径1
    private static final int EXTENDED_CHUNK_RANGE = 5;

    private boolean extendedRange = false;
    private int extensionTimeLeft = 0;

    public YushouCloverTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MinecraftForge.EVENT_BUS.register(YushouCloverTile.class);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<YushouCloverTile> {
        public FunctionalWandHud(YushouCloverTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            // 检查是否有足够的mana维持运行
            if (getMana() >= MANA_COST_PER_TICK) {
                addMana(-MANA_COST_PER_TICK);

                // 处理扩展范围逻辑
                if (extendedRange) {
                    extensionTimeLeft--;
                    if (extensionTimeLeft <= 0) {
                        extendedRange = false;
                        setChanged();
                    }
                }

                // 自动检查是否可以激活扩展模式
                checkAutoExtension();

            } else {
                // mana不足时取消扩展范围
                if (extendedRange) {
                    extendedRange = false;
                    extensionTimeLeft = 0;
                    setChanged();
                }
            }
        }
    }

    /**
     * 自动检查并激活扩展范围模式
     */
    private void checkAutoExtension() {
        // 当mana充足且当前未扩展时，自动激活扩展模式
        if (!extendedRange && getMana() >= MANA_COST_EXTENSION + (MANA_COST_PER_TICK * 100)) {
            addMana(-MANA_COST_EXTENSION);
            extendedRange = true;
            extensionTimeLeft = EXTENSION_DURATION;
            setChanged();
        }
    }

    /**
     * 获取当前保护范围（区块数）
     */
    public int getCurrentChunkRange() {
        return extendedRange ? EXTENDED_CHUNK_RANGE : BASE_CHUNK_RANGE;
    }

    /**
     * 监听生物生成事件 - 只阻止敌对生物
     */
    @SubscribeEvent
    public static void onSpawnCheck(LivingSpawnEvent.CheckSpawn event) {
        if (event.getSpawnReason() != MobSpawnType.NATURAL || event.getSpawner() instanceof BaseSpawner) {
            return;
        }

        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) {
            return;
        }

        Vec3 spawnPos = new Vec3(event.getX(), event.getY(), event.getZ());
        BlockPos spawnBlockPos = new BlockPos((int)event.getX(), (int)event.getY(), (int)event.getZ());
        LevelChunk chunk = (LevelChunk)event.getWorld().getChunk(spawnBlockPos);

        int spawnChunkX = spawnBlockPos.getX() >> 4;
        int spawnChunkZ = spawnBlockPos.getZ() >> 4;

        for (int chunkOffsetX = -EXTENDED_CHUNK_RANGE; chunkOffsetX <= EXTENDED_CHUNK_RANGE; chunkOffsetX++) {
            for (int chunkOffsetZ = -EXTENDED_CHUNK_RANGE; chunkOffsetZ <= EXTENDED_CHUNK_RANGE; chunkOffsetZ++) {
                int checkChunkX = spawnChunkX + chunkOffsetX;
                int checkChunkZ = spawnChunkZ + chunkOffsetZ;

                if (event.getWorld().hasChunk(checkChunkX, checkChunkZ)) {
                    LevelChunk checkChunk = (LevelChunk)event.getWorld().getChunk(checkChunkX, checkChunkZ);

                    for (BlockEntity be : checkChunk.getBlockEntities().values()) {
                        if (be instanceof YushouCloverTile flower) {
                            if (flower.getMana() < MANA_COST_PER_TICK) {
                                continue;
                            }

                            BlockPos flowerPos = flower.getEffectivePos();
                            int flowerChunkX = flowerPos.getX() >> 4;
                            int flowerChunkZ = flowerPos.getZ() >> 4;

                            int flowerRange = flower.getCurrentChunkRange();
                            if (Math.abs(spawnChunkX - flowerChunkX) <= flowerRange &&
                                    Math.abs(spawnChunkZ - flowerChunkZ) <= flowerRange) {

                                event.setResult(Result.DENY);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getMaxMana() {
        return 5000;
    }

    @Override
    public int getColor() {
        return extendedRange ? 0x00FF00 : 0x07C44C;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    @Override
    public RadiusDescriptor getRadius() {
        int blockRange = getCurrentChunkRange() * 16;
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), blockRange);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        extendedRange = cmp.getBoolean(TAG_EXTENDED_RANGE);
        extensionTimeLeft = cmp.getInt(TAG_EXTENSION_TIME);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putBoolean(TAG_EXTENDED_RANGE, extendedRange);
        cmp.putInt(TAG_EXTENSION_TIME, extensionTimeLeft);
    }
}