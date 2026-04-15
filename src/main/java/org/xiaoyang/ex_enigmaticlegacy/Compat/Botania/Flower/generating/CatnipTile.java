package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import java.util.*;

public class CatnipTile extends GeneratingFlowerBlockEntity {
    private static final int MAX_MANA = 20000;
    private static final int DETECTION_RANGE = 3;
    private static final int BASE_MANA_PER_TICK = 50;
    private static final int MANA_PER_CAT_PER_TICK = 50;
    private static final int MAX_CATS = 20;
    private static final int FLOWER_COLOR = 0x6600CC;
    private static final int RANGE = 3;
    private static final int CAT_MANAGEMENT_INTERVAL = 100;
    private static final double CAT_MOVEMENT_THRESHOLD = 1.5;
    private int catManagementTimer = 0;
    private final Map<UUID, BlockPos> catFlowerAssignments = new HashMap<>();
    private final Map<BlockPos, UUID> flowerCatAssignments = new HashMap<>();

    public CatnipTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public CatnipTile(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CATNIP_TILE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            generateManaFromCats();
            manageCatPositions();
        }
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    private void generateManaFromCats() {
        List<Cat> nearbycats = getCatsInRange();

        if (!nearbycats.isEmpty()) {
            int catCount = Math.min(nearbycats.size(), MAX_CATS);
            int manaToGenerate = BASE_MANA_PER_TICK + (catCount * MANA_PER_CAT_PER_TICK);

            if (getMana() < getMaxMana()) {
                addMana(manaToGenerate);
            }
        }
    }

    private void manageCatPositions() {
        catManagementTimer++;
        if (catManagementTimer < CAT_MANAGEMENT_INTERVAL) {
            return;
        }
        catManagementTimer = 0;

        List<Cat> cats = getCatsInRange();
        List<BlockPos> flowers = getFlowersInRange();

        cleanupCatAssignments(cats);

        assignCatsToFlowers(cats, flowers);

        moveCatsToFlowers(cats);
    }

    private List<Cat> getCatsInRange() {
        BlockPos pos = getBlockPos();
        AABB searchBox = new AABB(
                pos.getX() - DETECTION_RANGE, pos.getY() - 1, pos.getZ() - DETECTION_RANGE,
                pos.getX() + DETECTION_RANGE + 1, pos.getY() + 2, pos.getZ() + DETECTION_RANGE + 1
        );

        return getLevel().getEntitiesOfClass(Cat.class, searchBox);
    }

    private List<BlockPos> getFlowersInRange() {
        List<BlockPos> flowers = new ArrayList<>();
        BlockPos center = getBlockPos();

        for (int x = -DETECTION_RANGE; x <= DETECTION_RANGE; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -DETECTION_RANGE; z <= DETECTION_RANGE; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = getLevel().getBlockState(pos);

                    if (isCatnipFlower(state)) {
                        flowers.add(pos);
                    }
                }
            }
        }

        return flowers;
    }

    private boolean isCatnipFlower(BlockState state) {
        return state.getBlock() instanceof Catnip;
    }

    private void cleanupCatAssignments(List<Cat> currentCats) {
        Set<UUID> currentCatUUIDs = new HashSet<>();
        for (Cat cat : currentCats) {
            currentCatUUIDs.add(cat.getUUID());
        }

        catFlowerAssignments.entrySet().removeIf(entry -> !currentCatUUIDs.contains(entry.getKey()));
        flowerCatAssignments.entrySet().removeIf(entry -> !currentCatUUIDs.contains(entry.getValue()));
    }

    private void assignCatsToFlowers(List<Cat> cats, List<BlockPos> flowers) {
        if (flowers.isEmpty()) return;
        for (Cat cat : cats) {
            UUID catUUID = cat.getUUID();

            if (!catFlowerAssignments.containsKey(catUUID)) {
                BlockPos assignedFlower = findAvailableFlower(flowers);
                if (assignedFlower != null) {
                    catFlowerAssignments.put(catUUID, assignedFlower);
                    flowerCatAssignments.put(assignedFlower, catUUID);
                }
            }
        }
    }

    private BlockPos findAvailableFlower(List<BlockPos> flowers) {
        List<BlockPos> shuffledFlowers = new ArrayList<>(flowers);
        Collections.shuffle(shuffledFlowers);

        for (BlockPos flower : shuffledFlowers) {
            if (!flowerCatAssignments.containsKey(flower)) {
                return flower;
            }
        }

        if (!flowers.isEmpty()) {
            return flowers.get(getLevel().random.nextInt(flowers.size()));
        }

        return null;
    }

    private void resetCatState(Cat cat) {
        cat.setLying(false);
        cat.setRelaxStateOne(false);
        cat.getNavigation().stop();
    }

    private void continueCatRest(Cat cat) {
        cat.getNavigation().stop();

        if (getLevel().random.nextFloat() < 0.02f) {
            cat.playSound(SoundEvents.CAT_PURR, 0.3F, 1.0F + (getLevel().random.nextFloat() - 0.5F) * 0.2F);
        }
    }

    private void moveCatsToFlowers(List<Cat> cats) {
        for (Cat cat : cats) {
            UUID catUUID = cat.getUUID();
            BlockPos assignedFlower = catFlowerAssignments.get(catUUID);

            if (assignedFlower != null) {
                Vec3 catPos = cat.position();
                Vec3 flowerPos = Vec3.atCenterOf(assignedFlower);

                double offsetX = (getLevel().random.nextDouble() - 0.5) * 1.0;
                double offsetZ = (getLevel().random.nextDouble() - 0.5) * 1.0;
                Vec3 targetPos = new Vec3(flowerPos.x + offsetX, flowerPos.y, flowerPos.z + offsetZ);

                double distance = catPos.distanceTo(targetPos);

                if (cat.isLying() || cat.isRelaxStateOne()) {
                    if (distance <= CAT_MOVEMENT_THRESHOLD * 2) {
                        continueCatRest(cat);
                        continue;
                    }
                    resetCatState(cat);
                }

                if (distance > CAT_MOVEMENT_THRESHOLD) {
                    moveToTarget(cat, targetPos);
                } else {
                    makeCatRest(cat);
                }
            }
        }
    }

    private void moveToTarget(Cat cat, Vec3 targetPos) {
        resetCatState(cat);

        Vec3 catPos = cat.position();
        Vec3 direction = targetPos.subtract(catPos).normalize();

        cat.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 0.6);
        cat.getLookControl().setLookAt(targetPos.x, targetPos.y + 0.5, targetPos.z);
    }

    private void makeCatRest(Cat cat) {
        cat.getNavigation().stop();
        cat.getNavigation().resetMaxVisitedNodesMultiplier();

        if (cat.isTame()) {
            cat.setOrderedToSit(true);
        }
    }

    public void onClicked(Player player, InteractionHand hand) {
        if (!getLevel().isClientSide) {
            getLevel().playSound(null, getBlockPos(), SoundEvents.CAT_AMBIENT,
                    SoundSource.BLOCKS, 1.0F, 1.0F + (getLevel().random.nextFloat() - 0.5F) * 0.2F);

            makeCatsScatter();
        }
    }

    private void makeCatsScatter() {
        List<Cat> nearbycats = getCatsInRange();

        for (Cat cat : nearbycats) {
            resetCatState(cat);

            if (cat.isTame()) {
                cat.setOrderedToSit(false);
            }

            UUID catUUID = cat.getUUID();
            BlockPos assignedFlower = catFlowerAssignments.remove(catUUID);
            if (assignedFlower != null) {
                flowerCatAssignments.remove(assignedFlower);
            }

            double randomX = (getLevel().random.nextDouble() - 0.5) * 0.8;
            double randomZ = (getLevel().random.nextDouble() - 0.5) * 0.8;
            double jumpY = 0.4 + getLevel().random.nextDouble() * 0.2;

            cat.setDeltaMovement(randomX, jumpY, randomZ);
            cat.hasImpulse = true;

            cat.playSound(SoundEvents.CAT_HURT, 0.5F, 1.0F + (getLevel().random.nextFloat() - 0.5F) * 0.2F);
        }

        catManagementTimer = 0;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return FLOWER_COLOR;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt("catManagementTimer", catManagementTimer);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        catManagementTimer = cmp.getInt("catManagementTimer");
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(this)).cast());
    }
}