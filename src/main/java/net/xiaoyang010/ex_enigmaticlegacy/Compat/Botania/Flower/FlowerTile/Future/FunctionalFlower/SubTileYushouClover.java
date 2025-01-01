package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.FunctionalFlower;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

public class SubTileYushouClover extends TileEntityFunctionalFlower {
    private static final int MANA_COST = 5;
    private static final int BASE_RANGE = 3;
    private static final int MAX_RANGE = 5;
    private int currentRange = BASE_RANGE;

    public SubTileYushouClover(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            // 消耗魔力以保持扩大的范围
            if (currentRange > BASE_RANGE) {
                int expandedChunks = (currentRange * currentRange) - (BASE_RANGE * BASE_RANGE);
                int manaCost = MANA_COST * expandedChunks;

                if (getMana() >= manaCost) {
                    addMana(-manaCost);
                } else {
                    currentRange = BASE_RANGE;
                }
            }
        }
    }

    public void increaseRange() {
        if (currentRange < MAX_RANGE) {
            currentRange++;
        }
    }

    @SubscribeEvent
    public static void onSpawnCheck(LivingSpawnEvent.CheckSpawn event) {
        if (event.getSpawnReason() == MobSpawnType.NATURAL && !(event.getSpawner() instanceof BaseSpawner)) {
            Vec3 spawnPos = new Vec3(event.getX(), event.getY(), event.getZ());
            LevelChunk chunk = (LevelChunk)event.getWorld().getChunk(new BlockPos((int)event.getX(), (int)event.getY(), (int)event.getZ()));

            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof SubTileYushouClover flower) {
                    BlockPos flowerPos = flower.getEffectivePos();
                    int range = flower.currentRange * 16;

                    double distSq = spawnPos.distanceToSqr(flowerPos.getX(), flowerPos.getY(), flowerPos.getZ());
                    if (distSq <= range * range) {
                        event.setResult(Result.DENY);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public int getMaxMana() {
        return 1000;
    }

    @Override
    public int getColor() {
        return 0x07C44C;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), currentRange * 16);
    }
}