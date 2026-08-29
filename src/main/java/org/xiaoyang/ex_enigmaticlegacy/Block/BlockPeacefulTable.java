package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.*;
import java.util.stream.Collectors;

public class BlockPeacefulTable extends Block {
    private static List<EntityType<?>> cachedHostileTypes = null;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 14, 0, 16, 16, 16),
            Block.box(0, 0, 0, 2, 14, 2),
            Block.box(0, 0, 14, 2, 14, 16),
            Block.box(14, 0, 0, 16, 14, 2),
            Block.box(14, 0, 14, 16, 14, 16)
    );

    public BlockPeacefulTable(Properties properties) {
        super(properties);
    }

    public static void clearCache() {
        cachedHostileTypes = null;
        Exe.LOGGER.info("PeacefulTable: cache cleared");
    }

    private static void rebuildCache() {
        List<String> blacklist = ConfigHandler.PeacefulTableConfig.getEntityBlacklist();
        Exe.LOGGER.debug("PeacefulTable: rebuilding cache with blacklist={}", blacklist);
        cachedHostileTypes = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> type.getCategory() == MobCategory.MONSTER)
                .filter(type -> {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    if (id == null) return false;
                    String idStr = id.toString();
                    boolean blacklisted = blacklist.contains(idStr);
                    if (blacklisted) {
                        Exe.LOGGER.debug("PeacefulTable: blacklisted {}", idStr);
                    }
                    return !blacklisted;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<EntityType<?>> getHostileTypes() {
        if (cachedHostileTypes == null) {
            rebuildCache();
        }
        return cachedHostileTypes;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, @NotNull RandomSource random) {
        if (!ConfigHandler.PeacefulTableConfig.isEnabledInAllDifficulties()
                && world.getDifficulty() != Difficulty.PEACEFUL) {
            return;
        }

        List<EntityType<?>> hostileTypes = getHostileTypes();
        if (hostileTypes.isEmpty()) {
            return;
        }

        Mob monster = null;
        EntityType<?> selectedType = null;
        int attempts = 0;

        while (monster == null && attempts < 5) {
            int index = random.nextInt(hostileTypes.size());
            selectedType = hostileTypes.get(index);
            ResourceLocation selectedId = BuiltInRegistries.ENTITY_TYPE.getKey(selectedType);

            Entity rawEntity;
            try {
                rawEntity = selectedType.create(world);
            } catch (Exception e) {
                Exe.LOGGER.warn("PeacefulTable: failed to create entity {}, removing from cache. Error: {}", selectedId, e.getMessage()
                );
                final EntityType<?> failed = selectedType;
                cachedHostileTypes.removeIf(t -> t == failed);
                attempts++;
                continue;
            }

            if (rawEntity instanceof Mob mob) {
                monster = mob;
            } else {
                if (rawEntity != null) rawEntity.discard();
                Exe.LOGGER.warn("PeacefulTable: entity {} is not a Mob, removing from cache", selectedId
                );
                final EntityType<?> failed = selectedType;
                cachedHostileTypes.removeIf(t -> t == failed);
                attempts++;
            }
        }

        if (monster == null) return;

        Container swordInv = null;
        int swordSlot = -1;
        ItemStack sword = ItemStack.EMPTY;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity te = world.getBlockEntity(neighborPos);
            if (te instanceof Container inv) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (!item.isEmpty() && item.getItem() instanceof SwordItem) {
                        swordInv = inv;
                        swordSlot = i;
                        sword = item;
                        break;
                    }
                }
                if (!sword.isEmpty()) break;
            }
        }

        if (sword.isEmpty()) {
            monster.discard();
            return;
        }

        AABB searchBox = new AABB(
                pos.getX() - 2, pos.getY(), pos.getZ() - 2,
                pos.getX() + 3, pos.getY() + 4, pos.getZ() + 3
        );
        Set<UUID> beforeItemUUIDs = new HashSet<>();
        for (ItemEntity item : world.getEntitiesOfClass(
                ItemEntity.class, searchBox, e -> !e.isRemoved())) {
            beforeItemUUIDs.add(item.getUUID());
        }

        monster.moveTo(
                pos.getX() + 0.5,
                pos.getY() + 1.25,
                pos.getZ() + 0.5,
                random.nextFloat() * 360.0F, 0.0F
        );
        monster.setNoAi(true);
        world.addFreshEntity(monster);

        DifficultyInstance hardDifficulty =
                new DifficultyInstance(Difficulty.HARD, 0L, 0L, 0.0F);
        try {
            monster.finalizeSpawn(world, hardDifficulty, MobSpawnType.TRIGGERED, null, null);
        } catch (Exception e) {
            Exe.LOGGER.warn(
                    "PeacefulTable: finalizeSpawn failed for {}: {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(selectedType), e.getMessage()
            );
        }

        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, swordInv.getItem(swordSlot));
        fakePlayer.setYRot(random.nextFloat() * 360.0F);
        fakePlayer.setXRot(random.nextFloat() * 180.0F - 90.0F);

        float hpBefore = monster.getHealth();
        try {
            fakePlayer.attack(monster);
        } catch (Exception e) {
            Exe.LOGGER.warn(
                    "PeacefulTable: attack failed for {}: {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(selectedType), e.getMessage()
            );
        }
        float hpAfter = monster.getHealth();

        if (!monster.isDeadOrDying()) {
            ItemStack currentSword = fakePlayer.getMainHandItem();
            if (!currentSword.isEmpty()) {
                float dmgPerHit = hpBefore - hpAfter;
                if (dmgPerHit > 0) {
                    int extraHits = (int) Math.ceil(hpAfter / dmgPerHit);
                    for (int i = 0; i < extraHits && !currentSword.isEmpty(); i++) {
                        currentSword.hurtAndBreak(1, fakePlayer,
                                p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    }
                }
            }
            try {
                monster.hurt(world.damageSources().playerAttack(fakePlayer), Float.MAX_VALUE);
            } catch (Exception e) {
                Exe.LOGGER.warn(
                        "PeacefulTable: final hurt failed for {}: {}",
                        BuiltInRegistries.ENTITY_TYPE.getKey(selectedType), e.getMessage()
                );
            }
            monster.setDeltaMovement(0, 0, 0);
        }

        ItemStack handItem = fakePlayer.getMainHandItem();
        swordInv.setItem(swordSlot, handItem.isEmpty() ? ItemStack.EMPTY : handItem);
        swordInv.setChanged();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        if (!monster.isRemoved()) {
            monster.remove(Entity.RemovalReason.KILLED);
        }

        List<ItemEntity> afterItems = world.getEntitiesOfClass(
                ItemEntity.class, searchBox, e -> !e.isRemoved());

        for (ItemEntity itemEntity : afterItems) {
            if (beforeItemUUIDs.contains(itemEntity.getUUID())) continue;

            Direction[] dirs = Direction.values().clone();
            shuffleArray(dirs, random);

            ItemStack toInsert = itemEntity.getItem().copy();
            boolean fullyInserted = false;

            for (Direction dir : dirs) {
                BlockPos neighborPos = pos.relative(dir);
                Container neighborInv = HopperBlockEntity.getContainerAt(
                        world,
                        neighborPos.getX() + 0.5,
                        neighborPos.getY() + 0.5,
                        neighborPos.getZ() + 0.5
                );
                if (neighborInv != null) {
                    toInsert = insertItem(neighborInv, toInsert);
                    if (toInsert.isEmpty()) {
                        fullyInserted = true;
                        break;
                    }
                }
            }

            if (fullyInserted) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            } else if (toInsert.getCount() < itemEntity.getItem().getCount()) {
                itemEntity.setItem(toInsert);
            }
        }
    }

    private ItemStack insertItem(Container inv, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        int maxStack = Math.min(stack.getMaxStackSize(), inv.getMaxStackSize());
        int remaining = stack.getCount();

        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            if (!inv.canPlaceItem(i, stack)) continue;
            ItemStack slot = inv.getItem(i);
            if (!slot.isEmpty() && isSameItem(stack, slot)) {
                int space = maxStack - slot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(remaining, space);
                    slot.grow(toAdd);
                    remaining -= toAdd;
                    inv.setItem(i, slot);
                    inv.setChanged();
                }
            }
        }

        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            if (!inv.canPlaceItem(i, stack)) continue;
            ItemStack slot = inv.getItem(i);
            if (slot.isEmpty()) {
                int toAdd = Math.min(remaining, maxStack);
                ItemStack toPlace = stack.copy();
                toPlace.setCount(toAdd);
                inv.setItem(i, toPlace);
                remaining -= toAdd;
                inv.setChanged();
            }
        }

        if (remaining <= 0) return ItemStack.EMPTY;
        ItemStack ret = stack.copy();
        ret.setCount(remaining);
        return ret;
    }

    private boolean isSameItem(ItemStack a, ItemStack b) {
        if (a.getItem() != b.getItem()) return false;
        if (a.getDamageValue() != b.getDamageValue()) return false;
        return ItemStack.isSameItemSameTags(a, b);
    }

    private void shuffleArray(Direction[] arr, RandomSource random) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide) {
            world.scheduleTick(pos, this, 5 + world.random.nextInt(100));
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof ExperienceOrb) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}