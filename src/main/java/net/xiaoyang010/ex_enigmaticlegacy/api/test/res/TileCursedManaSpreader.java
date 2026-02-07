package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.*;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IKeyLocked;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.helper.MathHelper;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileCursedManaSpreader extends BlockEntity implements ICursedManaSpreader, IWandBindable, IKeyLocked, IThrottledPacket, IWandable {
    private static final int TICKS_ALLOWED_WITHOUT_PINGBACK = 20;
    private static final double PINGBACK_EXPIRED_SEARCH_DISTANCE = 0.5;
    private static final String TAG_CURSED_MANA = "cursedMana";
    private static final String TAG_ROTATION_X = "rotationX";
    private static final String TAG_ROTATION_Y = "rotationY";
    private static final String TAG_CAN_SHOOT = "canShoot";
    private static final String TAG_BURST_PARTICLE_TICK = "burstParticleTick";
    private static final String TAG_LAST_BURST_DEATH_TICK = "lastBurstDeathTick";
    private static final String TAG_UUID_MOST = "uuidMost";
    private static final String TAG_UUID_LEAST = "uuidLeast";
    private static final String TAG_PADDING_COLOR = "paddingColor";
    private static final String TAG_LENS = "lens";
    private static final String TAG_RECEIVER_X = "receiverX";
    private static final String TAG_RECEIVER_Y = "receiverY";
    private static final String TAG_RECEIVER_Z = "receiverZ";
    private static final String TAG_PINGBACK_TICKS = "pingbackTicks";
    private static final String TAG_LAST_PINGBACK_X = "lastPingbackX";
    private static final String TAG_LAST_PINGBACK_Y = "lastPingbackY";
    private static final String TAG_LAST_PINGBACK_Z = "lastPingbackZ";
    private int cursedMana = 0;
    public float rotationX = 0F;
    public float rotationY = 0F;
    private boolean canShoot;
    private int burstParticleTick = 0;
    private int lastBurstDeathTick = -1;
    private ICursedManaReceiver receiver = null;
    private ICursedManaReceiver receiverLastTick = null;
    private UUID identity = UUID.randomUUID();
    private String inputKey = "";
    private int ticks = 0;
    private boolean sendPacket = false;
    @Nullable
    public DyeColor paddingColor = null;
    public int pingbackTicks = 0;
    public double lastPingbackX = 0;
    public double lastPingbackY = Integer.MIN_VALUE;
    public double lastPingbackZ = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public TileCursedManaSpreader(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CURSED_SPREADER.get(), pos, state);
    }

    public TileCursedManaSpreader(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player == null) {
            return false;
        }

        if (!player.isShiftKeyDown()) {
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return true;
    }

    public static void commonTick(Level level, BlockPos pos, BlockState state, TileCursedManaSpreader spreader) {
        if (level.isClientSide) {
            clientTick(level, pos, state, spreader);
        } else {
            serverTick(level, pos, state, spreader);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCursedManaSpreader spreader) {
        if (spreader.cursedMana > 0 && level.random.nextInt(20) == 0) {
            BlockCursedManaSpreader.Variant variant = ((BlockCursedManaSpreader) state.getBlock()).variant;
            int color = variant.color;
            float r = (color >> 16 & 0xFF) / 255F;
            float g = (color >> 8 & 0xFF) / 255F;
            float b = (color & 0xFF) / 255F;

            WispParticleData data = WispParticleData.wisp(0.1F, r, g, b, true);
            level.addParticle(data,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCursedManaSpreader spreader) {
        boolean inNetwork = CursedManaNetwork.getInstance().isCollectorIn(level, spreader);
        if (spreader.cursedMana > 0 && level.getGameTime() % 20 == 0 && inNetwork) {
            spreader.corruptNearbyPools();
        }
        if (!inNetwork && !spreader.isRemoved()) {
            CursedManaNetwork.getInstance().fireCursedManaNetworkEvent(
                    spreader, CursedManaBlockType.COLLECTOR, CursedManaNetworkAction.ADD
            );
        }
        if (inNetwork) {
            for (Direction dir : Direction.values()) {
                BlockPos relPos = pos.relative(dir);
                if (level.hasChunkAt(relPos)) {
                    BlockEntity be = level.getBlockEntity(relPos);

                    if (be instanceof ICursedManaPool pool) {
                        if (pool != spreader.receiver) {
                            if (pool instanceof IKeyLocked locked &&
                                    !locked.getOutputKey().equals(spreader.getInputKey())) {
                                continue;
                            }
                            int manaInPool = pool.getCurrentCursedMana();
                            if (manaInPool > 0 && !spreader.isCursedManaFull()) {
                                int manaMissing = spreader.getMaxCursedMana() - spreader.cursedMana;
                                int manaToRemove = Math.min(manaInPool, manaMissing);
                                pool.receiveCursedMana(-manaToRemove);
                                spreader.receiveCursedMana(manaToRemove);
                            }
                        }
                    }
                }
            }
        }
        if (!spreader.canShoot) {
            if (spreader.pingbackTicks <= 0) {
                double x = spreader.lastPingbackX;
                double y = spreader.lastPingbackY;
                double z = spreader.lastPingbackZ;
                AABB aabb = new AABB(x, y, z, x, y, z)
                        .inflate(PINGBACK_EXPIRED_SEARCH_DISTANCE,
                                PINGBACK_EXPIRED_SEARCH_DISTANCE,
                                PINGBACK_EXPIRED_SEARCH_DISTANCE);
                var bursts = level.getEntitiesOfClass(EntityCursedManaBurst.class, aabb);
                EntityCursedManaBurst found = null;
                UUID identity = spreader.getIdentifier();
                for (EntityCursedManaBurst burst : bursts) {
                    if (burst != null && identity.equals(burst.getShooterUUID())) {
                        found = burst;
                        break;
                    }
                }
                if (found != null) {
                    found.ping();
                } else {
                    spreader.setCanShoot(true);
                }
            } else {
                spreader.pingbackTicks--;
            }
        }
        spreader.checkForReceiver();
        BlockCursedManaSpreader.Variant variant = ((BlockCursedManaSpreader) state.getBlock()).variant;
        int burstMana = variant.burstMana;
        boolean shouldShoot = spreader.canShoot && spreader.cursedMana >= burstMana;
        if (shouldShoot && spreader.receiver != null) {
            if (spreader.receiver.isCursedManaFull()) {
                shouldShoot = false;
            }
            if (shouldShoot && !spreader.receiver.canReceiveCursedManaFromBursts()) {
                shouldShoot = false;
            }
        }
        if (shouldShoot) {
            spreader.tryShootBurst(variant);
        }
        if (spreader.receiverLastTick != spreader.receiver) {
            spreader.sendPacket = true;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(spreader);
        }
        spreader.receiverLastTick = spreader.receiver;
        spreader.ticks++;
    }

    private void corruptNearbyPools() {
        if (level == null || level.isClientSide) return;
        int range = 15;
        BlockPos spreaderPos = getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = spreaderPos.offset(x, y, z);
                    BlockEntity tile = level.getBlockEntity(checkPos);

                    if (tile instanceof TilePool normalPool
                            && !(tile instanceof TileCursedManaPool)) {
                        double distance = Math.sqrt(x*x + y*y + z*z);
                        if (distance <= range) {
                            corruptPool(normalPool, checkPos, distance);
                        }
                    }
                }
            }
        }
    }


    private void corruptPool(vazkii.botania.common.block.tile.mana.TilePool pool, BlockPos poolPos, double distance) {
        PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, poolPos);

        int corruptionAmount = (int) (3 / (distance + 1));
        int currentMana = pool.getCurrentMana();

        if (currentMana > 0) {
            corruptionData.addCorruption(corruptionAmount);
            spawnCorruptionParticles(poolPos, false);
        } else {
            corruptionData.addCorruption(corruptionAmount / 3);
            spawnCorruptionParticles(poolPos, true);
        }

        if (corruptionData.getCorruption() >= 100) {
            convertToCursedPool(poolPos);
        }
    }


    private void convertToCursedPool(BlockPos poolPos) {
        if (level == null || level.isClientSide) return;
        BlockEntity tile = level.getBlockEntity(poolPos);
        if (!(tile instanceof vazkii.botania.common.block.tile.mana.TilePool normalPool)) {
            return;
        }

        int remainingMana = normalPool.getCurrentMana();

        BlockState oldState = level.getBlockState(poolPos);
        BlockState cursedPoolState = ModBlockss.CURSED_MANA_POOL.get().defaultBlockState();

        for (var property : oldState.getProperties()) {
            if (cursedPoolState.hasProperty(property)) {
                cursedPoolState = cursedPoolState.setValue(
                        (Property) property,
                        oldState.getValue(property)
                );
            }
        }

        level.setBlockAndUpdate(poolPos, cursedPoolState);

        BlockEntity newTile = level.getBlockEntity(poolPos);
        if (newTile instanceof TileCursedManaPool cursedPool) {
            cursedPool.receiveCursedMana((int)(remainingMana * 0.5));
        }

        PoolCorruptionManager.remove(level, poolPos);

        spawnConversionExplosion(poolPos);
        level.playSound(null, poolPos,
                SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS,
                1.0F, 0.8F);
    }


    private void spawnCorruptionParticles(BlockPos targetPos, boolean isEmpty) {
        if (level == null) return;
        for (int i = 0; i < 3; i++) {
            double t = i / 3.0;
            double x = worldPosition.getX() + 0.5 + (targetPos.getX() - worldPosition.getX()) * t;
            double y = worldPosition.getY() + 0.5 + (targetPos.getY() - worldPosition.getY()) * t;
            double z = worldPosition.getZ() + 0.5 + (targetPos.getZ() - worldPosition.getZ()) * t;
            float r = isEmpty ? 0.3F : 0.5F;
            float g = isEmpty ? 0.3F : 0.0F;
            float b = isEmpty ? 0.3F : 0.5F;
            WispParticleData data = WispParticleData.wisp(0.15F, r, g, b, true);
            level.addParticle(data, x, y, z, 0, 0.01, 0);
        }
    }

    private void spawnConversionExplosion(BlockPos pos) {
        if (level == null) return;
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * Math.PI * 2;
            double pitch = Math.random() * Math.PI;
            double speed = 0.2 + Math.random() * 0.3;
            double vx = Math.cos(angle) * Math.sin(pitch) * speed;
            double vy = Math.cos(pitch) * speed;
            double vz = Math.sin(angle) * Math.sin(pitch) * speed;
            WispParticleData data = WispParticleData.wisp(0.3F, 0.5F, 0.0F, 0.5F, true);
            level.addParticle(data,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    vx, vy, vz);
        }
    }

    private void checkForReceiver() {
        if (level == null || level.isClientSide) return;
        EntityCursedManaBurst fakeBurst = createBurst(getVariant());
        if (fakeBurst != null) {
            fakeBurst.setFake(true);
            fakeBurst.setScanBeam();

            ICursedManaReceiver foundReceiver = fakeBurst.getCollidedTile(true);

            if (foundReceiver != null &&
                    foundReceiver.getCursedManaReceiverLevel().hasChunkAt(foundReceiver.getCursedManaReceiverPos())) {
                this.receiver = foundReceiver;
            } else {
                this.receiver = null;
            }
        }
    }

    private Vec3 calculateBurstDirection() {
        float yaw = (float) Math.toRadians(rotationX);
        float pitch = (float) Math.toRadians(rotationY);

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vec3(x, y, z).normalize();
    }

    private void tryShootBurst(BlockCursedManaSpreader.Variant variant) {
        EntityCursedManaBurst burst = createBurst(variant);
        if (burst != null) {
            cursedMana -= variant.burstMana;
            level.addFreshEntity(burst);
            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.1F, 1.5F);
            burst.ping();
            canShoot = false;

            setChanged();
            markDispatchable();
        }
    }

    @Nullable
    private EntityCursedManaBurst createBurst(BlockCursedManaSpreader.Variant variant) {
        EntityCursedManaBurst burst = new EntityCursedManaBurst(
                ModEntities.CURSED_MANA_BURST.get(),
                level,
                worldPosition,
                rotationX,
                rotationY
        );

        burst.setStartingCursedMana(variant.burstMana);
        burst.setCursedMana(variant.burstMana);
        burst.setMinCursedManaLoss(variant.preLossTicks);
        burst.setCursedManaLossPerTick(variant.lossPerTick);
        burst.setGravity(0F);
        burst.setShooterUUID(identity);
        burst.setColor(variant.color);

        ItemStack lens = itemHandler.getStackInSlot(0);
        if (!lens.isEmpty() && lens.getItem() instanceof ILens) {
            burst.setSourceLens(lens);
        }

        return burst;
    }

    @Override
    public Level getCursedManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getCursedManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentCursedMana() {
        return cursedMana;
    }

    @Override
    public boolean isCursedManaFull() {
        return cursedMana >= getMaxCursedMana();
    }

    @Override
    public void receiveCursedMana(int mana) {
        int oldMana = this.cursedMana;
        this.cursedMana = Math.min(cursedMana + mana, getMaxCursedMana());

        if (oldMana != this.cursedMana) {
            setChanged();
            markDispatchable();
        }
    }

    @Override
    public boolean canReceiveCursedManaFromBursts() {
        return true;
    }

    @Override
    public void onClientDisplayTick() {
    }

    @Override
    public float getCursedManaYieldMultiplier(ICursedManaBurst burst) {
        return 1.0F;
    }

    @Override
    public int getMaxCursedMana() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BlockCursedManaSpreader block) {
            return block.variant.manaCapacity;
        }
        return 1000;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            CursedManaNetwork.getInstance().fireCursedManaNetworkEvent(
                    this,
                    CursedManaBlockType.COLLECTOR,
                    CursedManaNetworkAction.ADD
            );
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            CursedManaNetwork.getInstance().fireCursedManaNetworkEvent(
                    this,
                    CursedManaBlockType.COLLECTOR,
                    CursedManaNetworkAction.REMOVE
            );
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && !level.isClientSide) {
            CursedManaNetwork.getInstance().fireCursedManaNetworkEvent(
                    this,
                    CursedManaBlockType.COLLECTOR,
                    CursedManaNetworkAction.ADD
            );
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> new TileCursedManaSpreader.WandHud(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    public BlockCursedManaSpreader.Variant getVariant() {
        Block b = this.getBlockState().getBlock();
        if (b instanceof BlockCursedManaSpreader spreader) {
            return spreader.variant;
        } else {
            return BlockCursedManaSpreader.Variant.CURSED;
        }
    }

    public static class WandHud implements IWandHUD {
        private final TileCursedManaSpreader spreader;

        public WandHud(TileCursedManaSpreader spreader) {
            this.spreader = spreader;
        }

        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = (new ItemStack(this.spreader.getBlockState().getBlock())).getHoverName().getString();
            int color = this.spreader.getVariant().hudColor;
            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, this.spreader.getCurrentCursedMana(), this.spreader.getMaxCursedMana(), name);

            ItemStack lens = this.spreader.getItemHandler().getStackInSlot(0);
            if (!lens.isEmpty()) {
                Component lensName = lens.getHoverName();
                int width0 = 16 + mc.font.width(lensName) / 2;
                int x = mc.getWindow().getGuiScaledWidth() / 2 - width0;
                width0 = mc.getWindow().getGuiScaledHeight() / 2 + 50;
                mc.font.drawShadow(ms, lensName, (float)(x + 20), (float)(width0 + 5), color);
                mc.getItemRenderer().renderAndDecorateItem(lens, x, width0);
            }

            if (this.spreader.receiver != null) {
                BlockPos receiverPos = this.spreader.receiver.getCursedManaReceiverPos();
                if (receiverPos != null && this.spreader.level != null) {
                    BlockState receiverState = this.spreader.level.getBlockState(receiverPos);

                    if (!receiverState.isAir()) {
                        ItemStack receiverStack = new ItemStack(receiverState.getBlock());

                        if (!receiverStack.isEmpty()) {
                            String stackName = receiverStack.getHoverName().getString();
                            int width = 16 + mc.font.width(stackName) / 2;
                            int x = mc.getWindow().getGuiScaledWidth() / 2 - width;
                            int y = mc.getWindow().getGuiScaledHeight() / 2 + 30;
                            mc.font.drawShadow(ms, stackName, (float)(x + 20), (float)(y + 5), color);
                            mc.getItemRenderer().renderAndDecorateItem(receiverStack, x, y);
                        }
                    }
                }
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }

    @Override
    public int getBurstParticleTick() {
        return burstParticleTick;
    }

    @Override
    public void setBurstParticleTick(int i) {
        this.burstParticleTick = i;
    }

    @Override
    public int getLastBurstDeathTick() {
        return lastBurstDeathTick;
    }

    @Override
    public void setLastBurstDeathTick(int ticksExisted) {
        this.lastBurstDeathTick = ticksExisted;
    }

    @Override
    public EntityCursedManaBurst runBurstSimulation() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BlockCursedManaSpreader block) {
            EntityCursedManaBurst burst = createBurst(block.variant);
            if (burst != null) {
                burst.setFake(true);
            }
            return burst;
        }
        return null;
    }

    @Override
    public float getRotationX() {
        return rotationX;
    }

    @Override
    public float getRotationY() {
        return rotationY;
    }

    @Override
    public void setRotationX(float rot) {
        this.rotationX = rot;
    }

    @Override
    public void setRotationY(float rot) {
        this.rotationY = rot;
    }

    @Override
    public void commitRedirection() {
        setChanged();
    }

    @Override
    public void pingback(EntityCursedManaBurst burst, UUID expectedIdentity) {
        if (identity.equals(expectedIdentity)) {
            pingbackTicks = TICKS_ALLOWED_WITHOUT_PINGBACK;
            lastPingbackX = burst.getX();
            lastPingbackY = burst.getY();
            lastPingbackZ = burst.getZ();
            setCanShoot(false);
        }
    }

    @Override
    public UUID getIdentifier() {
        return identity;
    }

    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_CURSED_MANA, cursedMana);
        cmp.putFloat(TAG_ROTATION_X, rotationX);
        cmp.putFloat(TAG_ROTATION_Y, rotationY);
        cmp.putBoolean(TAG_CAN_SHOOT, canShoot);

        cmp.putInt(TAG_PINGBACK_TICKS, pingbackTicks);
        cmp.putDouble(TAG_LAST_PINGBACK_X, lastPingbackX);
        cmp.putDouble(TAG_LAST_PINGBACK_Y, lastPingbackY);
        cmp.putDouble(TAG_LAST_PINGBACK_Z, lastPingbackZ);

        if (paddingColor != null) {
            cmp.putInt(TAG_PADDING_COLOR, paddingColor.getId());
        }

        if (receiver != null) {
            BlockPos pos = receiver.getCursedManaReceiverPos();
            cmp.putInt(TAG_RECEIVER_X, pos.getX());
            cmp.putInt(TAG_RECEIVER_Y, pos.getY());
            cmp.putInt(TAG_RECEIVER_Z, pos.getZ());
        } else {
            cmp.putInt(TAG_RECEIVER_Y, Integer.MIN_VALUE);
        }
    }

    public void readPacketNBT(CompoundTag cmp) {
        cursedMana = cmp.getInt(TAG_CURSED_MANA);
        rotationX = cmp.getFloat(TAG_ROTATION_X);
        rotationY = cmp.getFloat(TAG_ROTATION_Y);
        canShoot = cmp.getBoolean(TAG_CAN_SHOOT);

        pingbackTicks = cmp.getInt(TAG_PINGBACK_TICKS);
        lastPingbackX = cmp.getDouble(TAG_LAST_PINGBACK_X);
        lastPingbackY = cmp.getDouble(TAG_LAST_PINGBACK_Y);
        lastPingbackZ = cmp.getDouble(TAG_LAST_PINGBACK_Z);

        if (cmp.contains(TAG_PADDING_COLOR)) {
            paddingColor = DyeColor.byId(cmp.getInt(TAG_PADDING_COLOR));
        } else {
            paddingColor = null;
        }

        if (cmp.contains(TAG_RECEIVER_Y) && cmp.getInt(TAG_RECEIVER_Y) != Integer.MIN_VALUE) {
            int x = cmp.getInt(TAG_RECEIVER_X);
            int y = cmp.getInt(TAG_RECEIVER_Y);
            int z = cmp.getInt(TAG_RECEIVER_Z);

            if (level != null) {
                BlockPos receiverPos = new BlockPos(x, y, z);
                BlockEntity tile = level.getBlockEntity(receiverPos);
                if (tile instanceof ICursedManaReceiver) {
                    this.receiver = (ICursedManaReceiver) tile;
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);

        burstParticleTick = tag.getInt(TAG_BURST_PARTICLE_TICK);
        lastBurstDeathTick = tag.getInt(TAG_LAST_BURST_DEATH_TICK);

        long most = tag.getLong(TAG_UUID_MOST);
        long least = tag.getLong(TAG_UUID_LEAST);
        if (most != 0 || least != 0) {
            identity = new UUID(most, least);
        }

        if (tag.contains(TAG_LENS)) {
            itemHandler.deserializeNBT(tag.getCompound(TAG_LENS));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writePacketNBT(tag);

        tag.putInt(TAG_BURST_PARTICLE_TICK, burstParticleTick);
        tag.putInt(TAG_LAST_BURST_DEATH_TICK, lastBurstDeathTick);
        tag.putLong(TAG_UUID_MOST, identity.getMostSignificantBits());
        tag.putLong(TAG_UUID_LEAST, identity.getLeastSignificantBits());
        tag.put(TAG_LENS, itemHandler.serializeNBT());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            checkForReceiver();
            if (!level.isClientSide) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writePacketNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readPacketNBT(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            readPacketNBT(tag);
        }
    }

    @Override
    public void markDispatchable() {
        sendPacket = true;
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) return false;
        BlockEntity targetTile = level.getBlockEntity(pos);
        if (!(targetTile instanceof ICursedManaReceiver)) {
            return false;
        }

        VoxelShape shape = player.level.getBlockState(pos).getShape(player.level, pos);
        AABB axis = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

        Vec3 thisVec = Vec3.atCenterOf(this.getBlockPos());
        Vec3 blockVec = new Vec3(
                axis.minX + (axis.maxX - axis.minX) / 2.0,
                axis.minY + (axis.maxY - axis.minY) / 2.0,
                axis.minZ + (axis.maxZ - axis.minZ) / 2.0
        );

        Vec3 diffVec = blockVec.subtract(thisVec);
        Vec3 diffVec2D = new Vec3(diffVec.x, diffVec.z, 0.0);
        Vec3 rotVec = new Vec3(0.0, 1.0, 0.0);

        double angle = MathHelper.angleBetween(rotVec, diffVec2D) / Math.PI * 180.0;
        if (blockVec.x < thisVec.x) {
            angle = -angle;
        }
        this.rotationX = (float)angle + 90.0F;

        rotVec = new Vec3(diffVec.x, 0.0, diffVec.z);
        angle = MathHelper.angleBetween(diffVec, rotVec) * 180.0 / Math.PI;
        if (blockVec.y < thisVec.y) {
            angle = -angle;
        }
        this.rotationY = (float)angle;

        this.receiver = (ICursedManaReceiver) targetTile;

        this.setChanged();

        if (!level.isClientSide) {
            markDispatchable();
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

        return true;
    }

    @Override
    public BlockPos getBinding() {
        return this.receiver == null ? null : this.receiver.getCursedManaReceiverPos();
    }

    public String getInputKey() {
        return this.inputKey;
    }

    @Override
    public String getOutputKey() {
        return "";
    }
}

