package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityGaiaSpreader;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityGaiaSpreader.VariantE;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.*;
import vazkii.botania.common.block.tile.TileExposedSimpleInventory;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.entity.EntityManaBurst.PositionProperties;
import vazkii.botania.common.handler.ManaNetworkHandler;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.common.item.ItemLexicon;
import vazkii.botania.xplat.BotaniaConfig;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InfinityGaiaSpreaderTile extends TileExposedSimpleInventory implements IWandBindable, IKeyLocked, IThrottledPacket, IManaSpreader, IWandable {
    private boolean mapmakerOverride = false;
    private int mmForcedColor = 2162464;
    private int mmForcedManaPayload = 160;
    private int mmForcedTicksBeforeManaLoss = 60;
    private float mmForcedManaLossPerTick = 4.0F;
    private float mmForcedGravity = 0.0F;
    private float mmForcedVelocityMultiplier = 1.0F;
    private String inputKey = "";
    private final String outputKey = "";
    private UUID identity = UUID.randomUUID();
    private int mana;
    public float rotationX;
    public float rotationY;
    @Nullable
    public DyeColor paddingColor = null;
    private boolean requestsClientUpdate = false;
    private boolean hasReceivedInitialPacket = false;
    private IManaReceiver receiver = null;
    private IManaReceiver receiverLastTick = null;
    private boolean poweredLastTick = true;
    public boolean canShootBurst = true;
    public int lastBurstDeathTick = -1;
    public int burstParticleTick = 0;
    public int pingbackTicks = 0;
    public double lastPingbackX = 0.0;
    public double lastPingbackY = -2.147483648E9;
    public double lastPingbackZ = 0.0;
    private List<PositionProperties> lastTentativeBurst;
    private boolean invalidTentativeBurst = false;

    public InfinityGaiaSpreaderTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFINITY_SPREADER.get(), pos, state);
    }

    public boolean isFull() {
        return this.mana >= this.getMaxMana();
    }

    public void receiveMana(int mana) {
        this.mana = Math.min(this.mana + mana, this.getMaxMana());
        this.setChanged();
    }

    public void setRemoved() {
        super.setRemoved();
        BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.COLLECTOR, ManaNetworkAction.REMOVE);
    }

    public static void commonTick(Level level, BlockPos worldPosition, BlockState state, InfinityGaiaSpreaderTile self) {
        boolean inNetwork = ManaNetworkHandler.instance.isCollectorIn(level, self);
        boolean wasInNetwork = inNetwork;
        if (!inNetwork && !self.isRemoved()) {
            BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(self, ManaBlockType.COLLECTOR, ManaNetworkAction.ADD);
        }

        boolean powered = false;
        Direction[] var7 = Direction.values();
        int var8 = var7.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            Direction dir = var7[var9];
            BlockPos relPos = worldPosition.relative(dir);
            if (level.hasChunkAt(relPos)) {
                IManaReceiver receiverAt = IXplatAbstractions.INSTANCE.findManaReceiver(level, relPos, level.getBlockState(relPos), level.getBlockEntity(relPos), dir.getOpposite());
                if (receiverAt instanceof IManaPool) {
                    IManaPool pool = (IManaPool)receiverAt;
                    if (wasInNetwork && (pool != self.receiver || self.getVariant() == VariantE.REDSTONE)) {
                        if (pool instanceof IKeyLocked) {
                            IKeyLocked locked = (IKeyLocked)pool;
                            if (!locked.getOutputKey().equals(self.getInputKey())) {
                                continue;
                            }
                        }

                        int manaInPool = pool.getCurrentMana();
                        if (manaInPool > 0 && !self.isFull()) {
                            int manaMissing = self.getMaxMana() - self.mana;
                            int manaToRemove = Math.min(manaInPool, manaMissing);
                            pool.receiveMana(-manaToRemove);
                            self.receiveMana(manaToRemove);
                        }
                    }
                }

                powered = powered || level.hasSignal(relPos, dir);
            }
        }

        if (self.needsNewBurstSimulation()) {
            self.checkForReceiver();
        }

        if (!self.canShootBurst) {
            if (self.pingbackTicks <= 0) {
                double x = self.lastPingbackX;
                double y = self.lastPingbackY;
                double z = self.lastPingbackZ;
                AABB aabb = (new AABB(x, y, z, x, y, z)).inflate(0.5, 0.5, 0.5);
                List<ThrowableProjectile> bursts = level.getEntitiesOfClass(ThrowableProjectile.class, aabb, Predicates.instanceOf(IManaBurst.class));
                IManaBurst found = null;
                UUID identity = self.getIdentifier();
                Iterator var17 = bursts.iterator();

                while(var17.hasNext()) {
                    IManaBurst burst = (IManaBurst)var17.next();
                    if (burst != null && identity.equals(burst.getShooterUUID())) {
                        found = burst;
                        break;
                    }
                }

                if (found != null) {
                    found.ping();
                } else {
                    self.setCanShoot(true);
                }
            } else {
                --self.pingbackTicks;
            }
        }

        boolean shouldShoot = !powered;
        boolean redstoneSpreader = self.getVariant() == VariantE.REDSTONE;
        if (redstoneSpreader) {
            shouldShoot = powered && !self.poweredLastTick;
        }

        if (shouldShoot) {
            IManaReceiver var24 = self.receiver;
            if (var24 instanceof IKeyLocked) {
                IKeyLocked locked = (IKeyLocked)var24;
                shouldShoot = locked.getInputKey().equals(self.getOutputKey());
            }
        }

        ItemStack lens = self.getItemHandler().getItem(0);
        ILensControl control = self.getLensController(lens);
        if (control != null) {
            if (redstoneSpreader) {
                if (shouldShoot) {
                    control.onControlledSpreaderPulse(lens, self);
                }
            } else {
                control.onControlledSpreaderTick(lens, self, powered);
            }

            shouldShoot = shouldShoot && control.allowBurstShooting(lens, self, powered);
        }

        if (shouldShoot) {
            self.tryShootBurst();
        }

        if (self.receiverLastTick != self.receiver && !level.isClientSide) {
            self.requestsClientUpdate = true;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
        }

        self.poweredLastTick = powered;
        self.receiverLastTick = self.receiver;
    }

    public void writePacketNBT(CompoundTag cmp) {
        super.writePacketNBT(cmp);
        cmp.putUUID("uuid", this.getIdentifier());
        cmp.putInt("mana", this.mana);
        cmp.putFloat("rotationX", this.rotationX);
        cmp.putFloat("rotationY", this.rotationY);
        cmp.putBoolean("requestUpdate", this.requestsClientUpdate);
        cmp.putInt("paddingColor", this.paddingColor == null ? -1 : this.paddingColor.getId());
        cmp.putBoolean("canShootBurst", this.canShootBurst);
        cmp.putInt("pingbackTicks", this.pingbackTicks);
        cmp.putDouble("lastPingbackX", this.lastPingbackX);
        cmp.putDouble("lastPingbackY", this.lastPingbackY);
        cmp.putDouble("lastPingbackZ", this.lastPingbackZ);
        cmp.putString("inputKey", this.inputKey);
        cmp.putString("outputKey", "");
        cmp.putInt("forceClientBindingX", this.receiver == null ? 0 : this.receiver.getManaReceiverPos().getX());
        cmp.putInt("forceClientBindingY", this.receiver == null ? Integer.MIN_VALUE : this.receiver.getManaReceiverPos().getY());
        cmp.putInt("forceClientBindingZ", this.receiver == null ? 0 : this.receiver.getManaReceiverPos().getZ());
        cmp.putBoolean("mapmakerOverrideEnabled", this.mapmakerOverride);
        cmp.putInt("mmForcedColor", this.mmForcedColor);
        cmp.putInt("mmForcedManaPayload", this.mmForcedManaPayload);
        cmp.putInt("mmForcedTicksBeforeManaLoss", this.mmForcedTicksBeforeManaLoss);
        cmp.putFloat("mmForcedManaLossPerTick", this.mmForcedManaLossPerTick);
        cmp.putFloat("mmForcedGravity", this.mmForcedGravity);
        cmp.putFloat("mmForcedVelocityMultiplier", this.mmForcedVelocityMultiplier);
        this.requestsClientUpdate = false;
    }

    public void readPacketNBT(CompoundTag cmp) {
        super.readPacketNBT(cmp);
        String tagUuidMostDeprecated = "uuidMost";
        String tagUuidLeastDeprecated = "uuidLeast";
        if (cmp.hasUUID("uuid")) {
            this.identity = cmp.getUUID("uuid");
        } else if (cmp.contains(tagUuidLeastDeprecated) && cmp.contains(tagUuidMostDeprecated)) {
            long most = cmp.getLong(tagUuidMostDeprecated);
            long least = cmp.getLong(tagUuidLeastDeprecated);
            if (this.identity == null || most != this.identity.getMostSignificantBits() || least != this.identity.getLeastSignificantBits()) {
                this.identity = new UUID(most, least);
            }
        }

        this.mana = cmp.getInt("mana");
        this.rotationX = cmp.getFloat("rotationX");
        this.rotationY = cmp.getFloat("rotationY");
        this.requestsClientUpdate = cmp.getBoolean("requestUpdate");
        if (cmp.contains("inputKey")) {
            this.inputKey = cmp.getString("inputKey");
        }

        if (cmp.contains("outputKey")) {
            this.inputKey = cmp.getString("outputKey");
        }

        this.mapmakerOverride = cmp.getBoolean("mapmakerOverrideEnabled");
        this.mmForcedColor = cmp.getInt("mmForcedColor");
        this.mmForcedManaPayload = cmp.getInt("mmForcedManaPayload");
        this.mmForcedTicksBeforeManaLoss = cmp.getInt("mmForcedTicksBeforeManaLoss");
        this.mmForcedManaLossPerTick = cmp.getFloat("mmForcedManaLossPerTick");
        this.mmForcedGravity = cmp.getFloat("mmForcedGravity");
        this.mmForcedVelocityMultiplier = cmp.getFloat("mmForcedVelocityMultiplier");
        if (cmp.contains("paddingColor")) {
            this.paddingColor = cmp.getInt("paddingColor") == -1 ? null : DyeColor.byId(cmp.getInt("paddingColor"));
        }

        if (cmp.contains("canShootBurst")) {
            this.canShootBurst = cmp.getBoolean("canShootBurst");
        }

        this.pingbackTicks = cmp.getInt("pingbackTicks");
        this.lastPingbackX = cmp.getDouble("lastPingbackX");
        this.lastPingbackY = cmp.getDouble("lastPingbackY");
        this.lastPingbackZ = cmp.getDouble("lastPingbackZ");
        if (this.requestsClientUpdate && this.level != null) {
            int x = cmp.getInt("forceClientBindingX");
            int y = cmp.getInt("forceClientBindingY");
            int z = cmp.getInt("forceClientBindingZ");
            if (y != Integer.MIN_VALUE) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = this.level.getBlockState(pos);
                BlockEntity be = this.level.getBlockEntity(pos);
                this.receiver = IXplatAbstractions.INSTANCE.findManaReceiver(this.level, pos, state, be, (Direction)null);
            } else {
                this.receiver = null;
            }
        }

        if (this.level != null && this.level.isClientSide) {
            this.hasReceivedInitialPacket = true;
        }

    }

    public boolean canReceiveManaFromBursts() {
        return true;
    }

    public Level getManaReceiverLevel() {
        return this.getLevel();
    }

    public BlockPos getManaReceiverPos() {
        return this.getBlockPos();
    }

    public int getCurrentMana() {
        return this.mana;
    }

    public boolean onUsedByWand(@Nullable Player player, ItemStack wand, Direction side) {
        if (player == null) {
            return false;
        } else {
            if (!player.isShiftKeyDown()) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            } else {
                BlockHitResult bpos = ItemLexicon.doRayTrace(this.level, player, Fluid.NONE);
                if (!this.level.isClientSide) {
                    double x = bpos.getLocation().x - (double)this.getBlockPos().getX() - 0.5;
                    double y = bpos.getLocation().y - (double)this.getBlockPos().getY() - 0.5;
                    double z = bpos.getLocation().z - (double)this.getBlockPos().getZ() - 0.5;
                    if (bpos.getDirection() != Direction.DOWN && bpos.getDirection() != Direction.UP) {
                        Vec3 clickVector = new Vec3(x, 0.0, z);
                        Vec3 relative = new Vec3(-0.5, 0.0, 0.0);
                        double angle = Math.acos(clickVector.dot(relative) / (relative.length() * clickVector.length())) * 180.0 / Math.PI;
                        this.rotationX = (float)angle + 180.0F;
                        if (clickVector.z < 0.0) {
                            this.rotationX = 360.0F - this.rotationX;
                        }
                    }

                    double angle = y * 180.0;
                    this.rotationY = -((float)angle);
                    this.setChanged();
                    this.requestsClientUpdate = true;
                }
            }

            return true;
        }
    }

    private boolean needsNewBurstSimulation() {
        if (this.level.isClientSide && !this.hasReceivedInitialPacket) {
            return false;
        } else if (this.lastTentativeBurst == null) {
            return true;
        } else {
            Iterator var1 = this.lastTentativeBurst.iterator();

            PositionProperties props;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                props = (PositionProperties)var1.next();
            } while(props.contentsEqual(this.level));

            this.invalidTentativeBurst = props.isInvalidIn(this.level);
            return !this.invalidTentativeBurst;
        }
    }

    private void tryShootBurst() {
        boolean redstone = this.getVariant() == VariantE.REDSTONE;
        if ((this.receiver != null || redstone) && !this.invalidTentativeBurst && this.canShootBurst && (redstone || this.receiver.canReceiveManaFromBursts() && !this.receiver.isFull())) {
            EntityManaBurst burst = this.getBurst(false);
            if (burst != null && !this.level.isClientSide) {
                this.mana -= burst.getStartingMana();
                burst.setShooterUUID(this.getIdentifier());
                this.level.addFreshEntity(burst);
                burst.ping();
                if (!BotaniaConfig.common().silentSpreaders()) {
                    this.level.playSound((Player)null, this.worldPosition, ModSounds.spreaderFire, SoundSource.BLOCKS, 0.05F * (this.paddingColor != null ? 0.2F : 1.0F), 0.7F + 0.3F * (float)Math.random());
                }
            }
        }

    }

    public VariantE getVariant() {
        Block b = this.getBlockState().getBlock();
        if (b instanceof InfinityGaiaSpreader spreader) {
            return spreader.variant;
        } else {
            return VariantE.MANA;
        }
    }

    public void checkForReceiver() {
        ItemStack stack = this.getItemHandler().getItem(0);
        ILensControl control = this.getLensController(stack);
        if (control == null || control.allowBurstShooting(stack, this, false)) {
            EntityManaBurst fakeBurst = this.getBurst(true);
            fakeBurst.setScanBeam();
            IManaReceiver receiver = fakeBurst.getCollidedTile(true);
            if (receiver != null && receiver.getManaReceiverLevel().hasChunkAt(receiver.getManaReceiverPos())) {
                this.receiver = receiver;
            } else {
                this.receiver = null;
            }

            this.lastTentativeBurst = fakeBurst.propsList;
        }
    }

    public IManaBurst runBurstSimulation() {
        EntityManaBurst fakeBurst = this.getBurst(true);
        fakeBurst.setScanBeam();
        fakeBurst.getCollidedTile(true);
        return fakeBurst;
    }

    private EntityManaBurst getBurst(boolean fake) {
        VariantE variant = getVariant();
        float gravity = 0.0F;

        float time = level.getGameTime() + level.getRandom().nextFloat();
        float hue1 = (time * 0.005F) % 1.0F;
        float hue2 = (time * 0.005F + 0.33F) % 1.0F; // 偏移120度
        float hue3 = (time * 0.005F + 0.66F) % 1.0F; // 偏移240度

        int color1 = Color.HSBtoRGB(hue1, 0.7F, 0.9F);
        int color2 = Color.HSBtoRGB(hue2, 0.7F, 0.9F);
        int color3 = Color.HSBtoRGB(hue3, 0.7F, 0.9F);

        int finalColor;
        float random = level.getRandom().nextFloat();
        if (random < 0.33F) {
            finalColor = color1;
        } else if (random < 0.66F) {
            finalColor = color2;
        } else {
            finalColor = color3;
        }

        BurstProperties props = new BurstProperties(
                variant.burstMana,
                variant.preLossTicks,
                variant.lossPerTick,
                gravity,
                variant.motionModifier,
                finalColor
        );

        ItemStack lens = getItemHandler().getItem(0);
        if (!lens.isEmpty()) {
            Item item = lens.getItem();
            if (item instanceof ILensEffect) {
                ((ILensEffect) item).apply(lens, props, level);
            }
        }

        if (getCurrentMana() < props.maxMana && !fake) {
            return null;
        }

        EntityManaBurst burst = new EntityManaBurst(getLevel(), getBlockPos(), getRotationX(), getRotationY(), fake);
        burst.setSourceLens(lens);

        if (mapmakerOverride) {
            burst.setColor(mmForcedColor);
            burst.setMana(mmForcedManaPayload);
            burst.setStartingMana(mmForcedManaPayload);
            burst.setMinManaLoss(mmForcedTicksBeforeManaLoss);
            burst.setManaLossPerTick(mmForcedManaLossPerTick);
            burst.setGravity(mmForcedGravity);
            burst.setDeltaMovement(burst.getDeltaMovement().scale(mmForcedVelocityMultiplier));
        } else {
            burst.setColor(finalColor);
            burst.setMana(props.maxMana);
            burst.setStartingMana(props.maxMana);
            burst.setMinManaLoss(props.ticksBeforeManaLoss);
            burst.setManaLossPerTick(props.manaLossPerTick);
            burst.setGravity(props.gravity);
            burst.setDeltaMovement(burst.getDeltaMovement().scale(props.motionModifier));
        }

        return burst;
    }

    public ILensControl getLensController(ItemStack stack) {
        if (!stack.isEmpty()) {
            Item var3 = stack.getItem();
            if (var3 instanceof ILensControl) {
                ILensControl control = (ILensControl)var3;
                if (control.isControlLens(stack)) {
                    return control;
                }
            }
        }

        return null;
    }

    public void onClientDisplayTick() {
        if (this.level != null) {
            EntityManaBurst burst = this.getBurst(true);
            burst.getCollidedTile(false);
        }

    }

    public float getManaYieldMultiplier(IManaBurst burst) {
        return 1.0F;
    }

    protected SimpleContainer createItemHandler() {
        return new SimpleContainer(1) {
            public int getMaxStackSize() {
                return 1;
            }

            public boolean canPlaceItem(int index, ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ILens;
            }
        };
    }

    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.checkForReceiver();
            if (!this.level.isClientSide) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
        }

    }

    public BlockPos getBinding() {
        return this.receiver == null ? null : this.receiver.getManaReceiverPos();
    }

    public int getMaxMana() {
        return this.getVariant().manaCapacity;
    }

    public String getInputKey() {
        return this.inputKey;
    }

    public String getOutputKey() {
        return "";
    }

    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        VoxelShape shape = player.level.getBlockState(pos).getShape(player.level, pos);
        AABB axis = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
        Vec3 thisVec = Vec3.atCenterOf(this.getBlockPos());
        Vec3 blockVec = new Vec3(axis.minX + (axis.maxX - axis.minX) / 2.0, axis.minY + (axis.maxY - axis.minY) / 2.0, axis.minZ + (axis.maxZ - axis.minZ) / 2.0);
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
        this.setChanged();
        return true;
    }

    public void markDispatchable() {
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public void setRotationX(float rot) {
        this.rotationX = rot;
    }

    public void setRotationY(float rot) {
        this.rotationY = rot;
    }

    public void rotate(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                this.rotationX += 270.0F;
                break;
            case CLOCKWISE_180:
                this.rotationX += 180.0F;
                break;
            case COUNTERCLOCKWISE_90:
                this.rotationX += 90.0F;
            case NONE:
        }

        if (this.rotationX >= 360.0F) {
            this.rotationX -= 360.0F;
        }

    }

    public void mirror(Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                this.rotationX = 360.0F - this.rotationX;
                break;
            case FRONT_BACK:
                this.rotationX = 180.0F - this.rotationX;
            case NONE:
        }

        if (this.rotationX < 0.0F) {
            this.rotationX += 360.0F;
        }

    }

    public void commitRedirection() {
        this.setChanged();
    }

    public void setCanShoot(boolean canShoot) {
        this.canShootBurst = canShoot;
    }

    public int getBurstParticleTick() {
        return this.burstParticleTick;
    }

    public void setBurstParticleTick(int i) {
        this.burstParticleTick = i;
    }

    public int getLastBurstDeathTick() {
        return this.lastBurstDeathTick;
    }

    public void setLastBurstDeathTick(int i) {
        this.lastBurstDeathTick = i;
    }

    public void pingback(IManaBurst burst, UUID expectedIdentity) {
        if (this.getIdentifier().equals(expectedIdentity)) {
            this.pingbackTicks = 20;
            Entity e = burst.entity();
            this.lastPingbackX = e.getX();
            this.lastPingbackY = e.getY();
            this.lastPingbackZ = e.getZ();
            this.setCanShoot(false);
        }

    }

    public UUID getIdentifier() {
        return this.identity;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> new WandHud(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    public static class WandHud implements IWandHUD {
        private final InfinityGaiaSpreaderTile spreader;

        public WandHud(InfinityGaiaSpreaderTile spreader) {
            this.spreader = spreader;
        }

        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = (new ItemStack(this.spreader.getBlockState().getBlock())).getHoverName().getString();
            int color = this.spreader.getVariant().hudColor;
            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, this.spreader.getCurrentMana(), this.spreader.getMaxMana(), name);
            ItemStack lens = this.spreader.getItemHandler().getItem(0);
            int width;
            if (!lens.isEmpty()) {
                Component lensName = lens.getHoverName();
                int width0 = 16 + mc.font.width(lensName) / 2;
                int x = mc.getWindow().getGuiScaledWidth() / 2 - width0;
                width0 = mc.getWindow().getGuiScaledHeight() / 2 + 50;
                mc.font.drawShadow(ms, lensName, (float)(x + 20), (float)(width0 + 5), color);
                mc.getItemRenderer().renderAndDecorateItem(lens, x, width0);
            }

            if (this.spreader.receiver != null) {
                BlockPos receiverPos = this.spreader.receiver.getManaReceiverPos();
                ItemStack recieverStack = new ItemStack(this.spreader.level.getBlockState(receiverPos).getBlock());
                if (!recieverStack.isEmpty()) {
                    String stackName = recieverStack.getHoverName().getString();
                    width = 16 + mc.font.width(stackName) / 2;
                    int x = mc.getWindow().getGuiScaledWidth() / 2 - width;
                    int y = mc.getWindow().getGuiScaledHeight() / 2 + 30;
                    mc.font.drawShadow(ms, stackName, (float)(x + 20), (float)(y + 5), color);
                    mc.getItemRenderer().renderAndDecorateItem(recieverStack, x, y);
                }
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}