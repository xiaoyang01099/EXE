package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import java.util.List;

public class DictariusTile extends TileEntityGeneratingFlower {
    private static final int WORK_MANA_PLAYER = 480;
    private static final int WORK_MANA_VILLAGER = 80;
    private static final int COOLDOWN_TIME = 100;
    private static final int MAX_MANA = 8000;
    private static final int COLOR = 13815218;
    private static final int DETECTION_RANGE = 2;
    private static final int CHECK_RANGE = 4;

    private int cooldown = 0;

    public DictariusTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide()) {
            return;
        }

        if (ticksExisted % 1200 == 0) {
            checkNearDictarius();
        }

        if (getMana() < getMaxMana() && cooldown == 0) {
            BlockPos effectivePos = getEffectivePos();
            AABB detectionArea = new AABB(effectivePos).inflate(DETECTION_RANGE, 1.0, DETECTION_RANGE);

            List<LivingEntity> livingEntities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    detectionArea
            );

            int totalWorkMana = 0;
            int villagerCount = 0;

            if (!livingEntities.isEmpty()) {
                int maxCheck = Math.min(livingEntities.size(), 16);

                for (int i = 0; i < maxCheck; i++) {
                    LivingEntity entity = livingEntities.get(i);

                    if (entity instanceof Player) {
                        totalWorkMana += WORK_MANA_PLAYER;
                    } else if (entity instanceof Villager) {
                        totalWorkMana += WORK_MANA_VILLAGER;
                        villagerCount++;

                        if (villagerCount > 15 && level.getRandom().nextInt(100) <= 4) {
                            entity.discard();
                        }
                    }
                }
            }

            if (totalWorkMana > 0) {
                cooldown = COOLDOWN_TIME;
                totalWorkMana = (int) (totalWorkMana * level.getRandom().nextDouble());
                addMana(totalWorkMana);
                sync();
            }
        }

        if (cooldown > 0) {
            cooldown--;
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getEffectivePos(), DETECTION_RANGE);
    }

    private void checkNearDictarius() {
        if (level == null) return;

        int foundFlowers = 0;
        BlockPos centerPos = getEffectivePos();

        for (int x = -CHECK_RANGE; x <= CHECK_RANGE; x++) {
            for (int y = -CHECK_RANGE; y <= CHECK_RANGE; y++) {
                for (int z = -CHECK_RANGE; z <= CHECK_RANGE; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);

                    if (blockEntity instanceof DictariusTile) {
                        foundFlowers++;

                        if (foundFlowers >= ConfigHandler.maxDictariusCount) {
                            level.levelEvent(2001, getBlockPos(),
                                    getId(getBlockState()));

                            BlockPos belowPos = getBlockPos().below();
                            if (level.getBlockState(belowPos).isFaceSturdy(level, belowPos,
                                    Direction.UP)) {
                                level.setBlock(getBlockPos(), Blocks.FIRE.defaultBlockState(), 3);
                            } else {
                                level.removeBlock(getBlockPos(), false);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public boolean isOvergrowthAffected() {
        return true;
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt("cooldown", cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        cooldown = tag.getInt("cooldown");
    }

    private int getId(BlockState state) {
        return Registry.BLOCK.getId(state.getBlock());
    }
}