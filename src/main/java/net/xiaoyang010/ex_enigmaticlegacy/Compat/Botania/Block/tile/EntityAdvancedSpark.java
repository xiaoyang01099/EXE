package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.entity.EntitySparkBase;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class EntityAdvancedSpark extends EntitySparkBase implements IManaSpark {

    public static final EntityDataAccessor<Integer> DATA_UPGRADE =
            SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_INVISIBLE =
            SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_NETWORK =
            SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.INT);

    private final Set<IManaSpark> transfers = Collections.newSetFromMap(new WeakHashMap<>());
    private int transferSpeed = 480000;
    private int removeTransferants = 2;
    private boolean firstTick = true;

    public EntityAdvancedSpark(EntityType<EntityAdvancedSpark> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public EntityAdvancedSpark(Level level) {
        this(ModEntities.ADVANCED_SPARK.get(), level);
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_UPGRADE, 0);
        this.entityData.define(DATA_INVISIBLE, false);
        this.entityData.define(DATA_NETWORK, DyeColor.WHITE.getId());
    }

    @Override
    public void tick() {
        super.tick();

        ISparkAttachable attachable = getAttachedTile();
        if (attachable == null) {
            if (!level.isClientSide) {
                discard();
            }
            return;
        }

        boolean isFirst = level.isClientSide && firstTick;
        SparkUpgradeType upgrade = getUpgrade();
        List<IManaSpark> allSparks = null;

        if (isFirst || upgrade == SparkUpgradeType.RECESSIVE || upgrade == SparkUpgradeType.DOMINANT) {
            allSparks = SparkHelper.getSparksAround(level, getX(), getY(), getZ())
                    .stream()
                    .filter(spark -> spark instanceof IManaSpark)
                    .map(spark -> (IManaSpark) spark)
                    .collect(Collectors.toList());
        }

        if (isFirst) {
            firstTick = false;
        }

        Collection<IManaSpark> transfers = getTransfers();

        if (upgrade != SparkUpgradeType.NONE) {
            switch (upgrade) {
                case DISPERSIVE:
                    handleDispersiveUpgrade();
                    break;
                case DOMINANT:
                    handleDominantUpgrade(allSparks);
                    break;
                case RECESSIVE:
                    handleRecessiveUpgrade(allSparks);
                    break;
                case ISOLATED:
                    break;
            }
        }

        if (!transfers.isEmpty() && !level.isClientSide) {
            IManaReceiver attachedReceiver = getAttachedManaReceiver();
            if (attachedReceiver != null) {
                int manaTotal = Math.min(transferSpeed * transfers.size(), attachedReceiver.getCurrentMana());
                int manaForEach = manaTotal / transfers.size();
                int manaSpent = 0;

                if (manaForEach > 0) {
                    for (IManaSpark spark : transfers) {
                        ISparkAttachable sparkAttachable = spark.getAttachedTile();
                        if (sparkAttachable != null && !sparkAttachable.areIncomingTranfersDone()) {
                            int availableSpace = sparkAttachable.getAvailableSpaceForMana();
                            if (availableSpace > 0) {
                                int spend = Math.min(availableSpace, manaForEach);
                                if (sparkAttachable instanceof IManaReceiver) {
                                    ((IManaReceiver) sparkAttachable).receiveMana(spend);
                                }
                                manaSpent += spend;
                                createParticlesTowards(spark.entity());
                            }
                        }
                    }

                    if (manaSpent > 0) {
                        attachedReceiver.receiveMana(-manaSpent);
                    }
                }
            }
        }

        if (removeTransferants > 0) {
            removeTransferants--;
        }
    }

    private void handleDispersiveUpgrade() {
        if (level.isClientSide) return;

        List<Player> players = getEntitiesAround(Player.class);
        Map<Player, Map<ItemStack, Integer>> receivingPlayers = new HashMap<>();
        ItemStack input = new ItemStack(ModItems.spark);

        for (Player player : players) {
            List<ItemStack> stacks = new ArrayList<>();

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }

            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof IManaItem manaItem) {
                    if (manaItem.canReceiveManaFromPool((BlockEntity) getAttachedTile())) {
                        Map<ItemStack, Integer> receivingStacks = receivingPlayers.computeIfAbsent(player, k -> new HashMap<>());

                        IManaReceiver attachedReceiver = getAttachedManaReceiver();
                        if (attachedReceiver != null) {
                            int recv = Math.min(attachedReceiver.getCurrentMana(),
                                    Math.min(transferSpeed, manaItem.getMaxMana() - manaItem.getMana()));
                            if (recv > 0) {
                                receivingStacks.put(stack, recv);
                            }
                        }
                    }
                }
            }
        }

        if (!receivingPlayers.isEmpty()) {
            List<Player> keys = new ArrayList<>(receivingPlayers.keySet());
            Collections.shuffle(keys);
            Player player = keys.get(0);
            Map<ItemStack, Integer> items = receivingPlayers.get(player);
            ItemStack stack = items.keySet().iterator().next();
            int cost = items.get(stack);

            IManaReceiver attachedReceiver = getAttachedManaReceiver();
            if (attachedReceiver != null) {
                int manaToPut = Math.min(attachedReceiver.getCurrentMana(), cost);
                ((IManaItem) stack.getItem()).addMana(manaToPut);
                attachedReceiver.receiveMana(-manaToPut);
                createParticlesTowards(player);
            }
        }
    }

    private void handleDominantUpgrade(List<IManaSpark> allSparks) {
        if (allSparks == null) return;

        List<IManaSpark> validSparks = allSparks.stream()
                .filter(spark -> spark != this)
                .filter(spark -> spark.getUpgrade() == SparkUpgradeType.NONE)
                .filter(spark -> spark.getAttachedTile() instanceof IManaPool)
                .collect(Collectors.toList());

        if (!validSparks.isEmpty()) {
            IManaSpark targetSpark = validSparks.get(level.getRandom().nextInt(validSparks.size()));
            targetSpark.registerTransfer(this);
        }
    }

    private void handleRecessiveUpgrade(List<IManaSpark> allSparks) {
        if (allSparks == null) return;

        for (IManaSpark spark : allSparks) {
            if (spark != this) {
                SparkUpgradeType sparkUpgrade = spark.getUpgrade();
                if (sparkUpgrade != SparkUpgradeType.DOMINANT &&
                        sparkUpgrade != SparkUpgradeType.RECESSIVE &&
                        sparkUpgrade != SparkUpgradeType.ISOLATED) {
                    transfers.add(spark);
                }
            }
        }
    }

    private <T extends Entity> List<T> getEntitiesAround(Class<T> entityClass) {
        int range = SparkHelper.SPARK_SCAN_RANGE;
        AABB aabb = new AABB(getX() - range, getY() - range, getZ() - range,
                getX() + range, getY() + range, getZ() + range);
        return level.getEntitiesOfClass(entityClass, aabb);
    }

    private void createParticlesTowards(Entity target) {
        if (level.isClientSide) {
            Vec3 thisVec = this.position().add(0, 0.25, 0);
            Vec3 targetVec = target.position().add(0, 0.25, 0);

            double randomOffset = 0.45;
            thisVec = thisVec.add((random.nextDouble() - 0.5) * randomOffset,
                    (random.nextDouble() - 0.5) * randomOffset,
                    (random.nextDouble() - 0.5) * randomOffset);
            targetVec = targetVec.add((random.nextDouble() - 0.5) * randomOffset,
                    (random.nextDouble() - 0.5) * randomOffset,
                    (random.nextDouble() - 0.5) * randomOffset);

            Vec3 motion = targetVec.subtract(thisVec).scale(0.04);

            float r = 0.4F + 0.3F * random.nextFloat();
            float g = 0.4F + 0.3F * random.nextFloat();
            float b = 0.4F + 0.3F * random.nextFloat();
            float size = 0.125F + 0.125F * random.nextFloat();
        }
    }

    public static void createParticleBeam(Entity e1, Entity e2) {
        if (e1 == null || e2 == null || !e1.level.isClientSide) return;

        Vec3 orig = e1.position().add(0, 0.25, 0);
        Vec3 end = e2.position().add(0, 0.25, 0);
        Vec3 diff = end.subtract(orig);
        Vec3 movement = diff.normalize().scale(0.1);
        int iterations = (int) (diff.length() / movement.length());

        float huePer = 1.0F / iterations;
        float hueSum = e1.random.nextFloat();
        Vec3 currentPos = orig;

        for (int i = 0; i < iterations; i++) {
            float hue = i * huePer + hueSum;
            Color color = Color.getHSBColor(hue, 1.0F, 1.0F);
            float r = Math.min(1.0F, color.getRed() / 255.0F + 0.4F);
            float g = Math.min(1.0F, color.getGreen() / 255.0F + 0.4F);
            float b = Math.min(1.0F, color.getBlue() / 255.0F + 0.4F);

            currentPos = currentPos.add(movement);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.isEmpty()) {
            SparkUpgradeType currentUpgrade = getUpgrade();

            if (stack.getItem() == ModItems.twigWand) {
                if (player.isShiftKeyDown()) {
                    if (currentUpgrade != SparkUpgradeType.NONE) {
                        if (!level.isClientSide) {
                            spawnAtLocation(new ItemStack(ModItems.sparkUpgradeDispersive, 1), 0.0F);
                        }
                        setUpgrade(SparkUpgradeType.NONE);
                        transfers.clear();
                        removeTransferants = 2;
                    } else {
                        discard();
                    }

                    if (level.isClientSide) {
                        player.swing(hand);
                    }
                    return InteractionResult.SUCCESS;
                }

                for (IManaSpark spark : SparkHelper.getSparksAround(level, getX(), getY(), getZ())) {
                    createParticleBeam(this, spark.entity());
                }
                return InteractionResult.SUCCESS;
            }

            if (stack.getItem() == ModItems.sparkUpgradeDispersive && currentUpgrade == SparkUpgradeType.NONE) {
                SparkUpgradeType newUpgrade = SparkUpgradeType.values()[stack.getDamageValue() + 1];
                setUpgrade(newUpgrade);
                stack.shrink(1);

                if (level.isClientSide) {
                    player.swing(hand);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return handlePhantomInk(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean handlePhantomInk(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == ModItems.phantomInk && !level.isClientSide) {
            boolean currentInvis = entityData.get(DATA_INVISIBLE);
            entityData.set(DATA_INVISIBLE, !currentInvis);
            return true;
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setUpgrade(SparkUpgradeType.values()[tag.getInt("upgrade")]);
        entityData.set(DATA_INVISIBLE, tag.getBoolean("invisible"));
        if (tag.contains("network")) {
            setNetwork(DyeColor.values()[tag.getInt("network")]);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("upgrade", getUpgrade().ordinal());
        tag.putBoolean("invisible", entityData.get(DATA_INVISIBLE));
        tag.putInt("network", getNetwork().getId());
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!level.isClientSide && reason != RemovalReason.CHANGED_DIMENSION) {
            spawnAtLocation(new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.ADVANCED_SPARK.get()), 0.0F);
            SparkUpgradeType upgrade = getUpgrade();
            if (upgrade != SparkUpgradeType.NONE) {
                spawnAtLocation(new ItemStack(ModItems.sparkUpgradeDispersive, 1), 0.0F);
            }
        }
    }

    @Override
    public @Nullable ISparkAttachable getAttachedTile() {
        BlockPos pos = getAttachPos();
        if (level.getBlockEntity(pos) instanceof ISparkAttachable attachable) {
            return attachable;
        }
        return null;
    }

    @Override
    public Collection<IManaSpark> getTransfers() {
        Set<IManaSpark> toRemove = new HashSet<>();

        for (IManaSpark spark : transfers) {
            SparkUpgradeType thisUpgrade = getUpgrade();
            SparkUpgradeType sparkUpgrade = spark.getUpgrade();
            ISparkAttachable attachable = spark.getAttachedTile();

            if (spark == this ||
                    spark.areIncomingTransfersDone() ||
                    attachable == null ||
                    attachable.getAvailableSpaceForMana() <= 0 ||
                    (!isValidTransferTarget(thisUpgrade, sparkUpgrade, attachable))) {
                toRemove.add(spark);
            }
        }

        transfers.removeAll(toRemove);
        return transfers;
    }

    private boolean isValidTransferTarget(SparkUpgradeType thisUpgrade, SparkUpgradeType targetUpgrade, ISparkAttachable targetAttachable) {
        if (thisUpgrade == SparkUpgradeType.NONE && targetUpgrade == SparkUpgradeType.DOMINANT) {
            return true;
        }
        if (thisUpgrade == SparkUpgradeType.RECESSIVE &&
                (targetUpgrade == SparkUpgradeType.NONE || targetUpgrade == SparkUpgradeType.DISPERSIVE)) {
            return !(targetAttachable instanceof IManaPool);
        }
        return false;
    }

    @Override
    public void registerTransfer(IManaSpark spark) {
        if (!transfers.contains(spark)) {
            transfers.add(spark);
        }
    }

    @Override
    public SparkUpgradeType getUpgrade() {
        int upgradeOrdinal = entityData.get(DATA_UPGRADE);
        return SparkUpgradeType.values()[Math.max(0, Math.min(upgradeOrdinal, SparkUpgradeType.values().length - 1))];
    }

    @Override
    public void setUpgrade(SparkUpgradeType upgrade) {
        entityData.set(DATA_UPGRADE, upgrade.ordinal());
    }

    @Override
    public boolean areIncomingTransfersDone() {
        ISparkAttachable attachable = getAttachedTile();
        if (attachable instanceof IManaPool) {
            return removeTransferants > 0;
        }
        return attachable != null && attachable.areIncomingTranfersDone();
    }

    @Override
    public BlockPos getAttachPos() {
        return new BlockPos(Mth.floor(getX()), Mth.floor(getY()) - 1, Mth.floor(getZ()));
    }

    @Override
    public @Nullable IManaReceiver getAttachedManaReceiver() {
        BlockPos pos = getAttachPos();
        if (level.getBlockEntity(pos) instanceof IManaReceiver receiver) {
            return receiver;
        }
        return null;
    }

    @Override
    public DyeColor getNetwork() {
        int networkId = entityData.get(DATA_NETWORK);
        return DyeColor.values()[Math.max(0, Math.min(networkId, DyeColor.values().length - 1))];
    }

    @Override
    public void setNetwork(DyeColor color) {
        entityData.set(DATA_NETWORK, color.getId());
    }

    @Override
    public Entity entity() {
        return this;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return entity instanceof Player && interact((Player) entity, InteractionHand.MAIN_HAND).consumesAction();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}