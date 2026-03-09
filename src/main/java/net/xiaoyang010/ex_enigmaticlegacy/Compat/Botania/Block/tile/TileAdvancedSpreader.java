package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.BlockAdvancedSpreader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.*;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.tile.TileExposedSimpleInventory;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.helper.MathHelper;

import java.util.UUID;

public class TileAdvancedSpreader extends TileExposedSimpleInventory implements IManaSpreader, IWandBindable, IKeyLocked, IThrottledPacket, IWandable {
    private static final int TICKS_ALLOWED_WITHOUT_PINGBACK = 20;
    private static final double PINGBACK_EXPIRED_SEARCH_DISTANCE = 0.5;

    private static final String TAG_MANA = "mana";
    private static final String TAG_ROTATION_X = "rotationX";
    private static final String TAG_ROTATION_Y = "rotationY";
    private static final String TAG_CAN_SHOOT = "canShoot";
    private static final String TAG_PADDING_COLOR = "paddingColor";
    private static final String TAG_UUID_MOST = "uuidMost";
    private static final String TAG_UUID_LEAST = "uuidLeast";
    private static final String TAG_PINGBACK_TICKS = "pingbackTicks";
    private static final String TAG_LAST_PINGBACK_X = "lastPingbackX";
    private static final String TAG_LAST_PINGBACK_Y = "lastPingbackY";
    private static final String TAG_LAST_PINGBACK_Z = "lastPingbackZ";

    private int mana = 0;
    public float rotationX = 0F;
    public float rotationY = 0F;
    private boolean canShoot = true;
    public int burstParticleTick = 0;
    public int lastBurstDeathTick = -1;

    @Nullable
    public DyeColor paddingColor = null;

    private IManaReceiver receiver = null;
    private IManaReceiver receiverLastTick = null;
    private UUID identity = UUID.randomUUID();

    public int pingbackTicks = 0;
    public double lastPingbackX = 0;
    public double lastPingbackY = Integer.MIN_VALUE;
    public double lastPingbackZ = 0;

    private boolean requestsClientUpdate = false;

    private IItemHandlerModifiable itemHandlerWrapper;

    public TileAdvancedSpreader(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_SPREADER.get(), pos, state);
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

    public static void commonTick(Level level, BlockPos pos, BlockState state, TileAdvancedSpreader spreader) {
        if (level.isClientSide) {
            clientTick(level, pos, state, spreader);
        } else {
            serverTick(level, pos, state, spreader);
        }
    }

    private static void clientTick(Level level, BlockPos pos, BlockState state, TileAdvancedSpreader spreader) {
        if (spreader.mana > 0 && level.random.nextInt(20) == 0) {
            WispParticleData data = WispParticleData.wisp(0.1F, 0.604F, 0.804F, 0.196F, true);
            level.addParticle(data,
                    pos.getX() + 0.5 + (Math.random() - 0.5) * 0.3,
                    pos.getY() + 0.5 + (Math.random() - 0.5) * 0.3,
                    pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
    }

    private static void serverTick(Level level, BlockPos pos, BlockState state, TileAdvancedSpreader spreader) {
        if (!spreader.canShoot) {
            if (spreader.pingbackTicks <= 0) {
                AABB aabb = new AABB(
                        spreader.lastPingbackX, spreader.lastPingbackY, spreader.lastPingbackZ,
                        spreader.lastPingbackX, spreader.lastPingbackY, spreader.lastPingbackZ
                ).inflate(PINGBACK_EXPIRED_SEARCH_DISTANCE);

                var bursts = level.getEntitiesOfClass(EntityManaBurst.class, aabb);
                EntityManaBurst found = null;
                for (EntityManaBurst burst : bursts) {
                    IManaBurst manaBurst = burst;
                    if (spreader.identity.equals(manaBurst.getShooterUUID())) {
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

        if (spreader.canShoot && spreader.receiver != null) {
            if (!spreader.receiver.isFull() && spreader.receiver.canReceiveManaFromBursts()) {
                spreader.tryShootBurst();
            }
        }

        if (spreader.receiverLastTick != spreader.receiver) {
            spreader.requestsClientUpdate = true;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(spreader);
        }
        spreader.receiverLastTick = spreader.receiver;
    }

    private void checkForReceiver() {
        EntityManaBurst fakeBurst = createBurst(true);
        if (fakeBurst != null) {
            fakeBurst.setScanBeam();
            IManaReceiver receiver = fakeBurst.getCollidedTile(true);
            if (receiver != null &&
                    receiver.getManaReceiverLevel().hasChunkAt(receiver.getManaReceiverPos())) {
                this.receiver = receiver;
            } else {
                this.receiver = null;
            }
        }
    }

    private void tryShootBurst() {
        EntityManaBurst burst = createBurst(false);
        if (burst != null && level != null) {
            mana -= ConfigHandler.ABSpreaderConfig.getSpreaderBurstMana();
            level.addFreshEntity(burst);
            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS, 0.1F, 1.5F);
            ((IManaBurst)burst).ping();
            canShoot = false;
            setChanged();
        }
    }

    @Nullable
    private EntityManaBurst createBurst(boolean fake) {
        int maxMana = ConfigHandler.ABSpreaderConfig.getSpreaderBurstMana();

        if (mana < maxMana && !fake) {
            return null;
        }

        EntityManaBurst burst = new EntityManaBurst(level, worldPosition, rotationX, rotationY, fake);

        BurstProperties props = new BurstProperties(
                maxMana,
                35,
                maxMana / 4.5F,
                0.0F,
                2.5F,
                0x9ACD32
        );

        ItemStack lens = getItemHandlerModifiable().getStackInSlot(0);
        if (!lens.isEmpty() && lens.getItem() instanceof ILensEffect lensEffect) {
            lensEffect.apply(lens, props, level);
        }

        burst.setSourceLens(lens);
        burst.setColor(props.color);
        burst.setMana(props.maxMana);
        burst.setStartingMana(props.maxMana);
        burst.setMinManaLoss(props.ticksBeforeManaLoss);
        burst.setManaLossPerTick(props.manaLossPerTick);
        burst.setGravity(props.gravity);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(props.motionModifier));

        if (!fake) {
            ((IManaBurst)burst).setShooterUUID(identity);
        }

        return burst;
    }

    @Override
    public void onClientDisplayTick() {}

    @Override
    public float getManaYieldMultiplier(IManaBurst burst) {
        return 1.0F;
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
    public void setLastBurstDeathTick(int i) {
        this.lastBurstDeathTick = i;
    }

    @Override
    public void pingback(IManaBurst burst, UUID expectedIdentity) {
        if (identity.equals(expectedIdentity)) {
            pingbackTicks = TICKS_ALLOWED_WITHOUT_PINGBACK;
            var e = burst.entity();
            lastPingbackX = e.getX();
            lastPingbackY = e.getY();
            lastPingbackZ = e.getZ();
            setCanShoot(false);
        }
    }

    @Override
    public UUID getIdentifier() {
        return identity;
    }

    @Override
    public IManaBurst runBurstSimulation() {
        EntityManaBurst burst = createBurst(true);
        if (burst != null) {
            burst.setScanBeam();
            burst.getCollidedTile(true);
        }
        return burst;
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
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= getMaxMana();
    }

    @Override
    public void receiveMana(int mana) {
        this.mana = Math.min(this.mana + mana, getMaxMana());
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    public int getMaxMana() {
        return ConfigHandler.ABSpreaderConfig.getSpreaderMaxMana();
    }

    public BlockAdvancedSpreader.VariantN getAdvancedVariant() {
        Block b = getBlockState().getBlock();
        if (b instanceof BlockAdvancedSpreader spreader) {
            return spreader.variant;
        }
        return BlockAdvancedSpreader.VariantN.NATURE;
    }

    @Override
    public BlockPos getBinding() {
        return receiver == null ? null : receiver.getManaReceiverPos();
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) return false;

        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        AABB axis = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

        Vec3 thisVec = Vec3.atCenterOf(getBlockPos());
        Vec3 blockVec = new Vec3(
                axis.minX + (axis.maxX - axis.minX) / 2,
                axis.minY + (axis.maxY - axis.minY) / 2,
                axis.minZ + (axis.maxZ - axis.minZ) / 2
        );

        Vec3 diffVec = blockVec.subtract(thisVec);
        Vec3 diffVec2D = new Vec3(diffVec.x, diffVec.z, 0);
        Vec3 rotVec = new Vec3(0, 1, 0);

        double angle = MathHelper.angleBetween(rotVec, diffVec2D) / Math.PI * 180.0;
        if (blockVec.x < thisVec.x) {
            angle = -angle;
        }
        rotationX = (float)angle + 90;

        rotVec = new Vec3(diffVec.x, 0, diffVec.z);
        angle = MathHelper.angleBetween(diffVec, rotVec) * 180.0 / Math.PI;
        if (blockVec.y < thisVec.y) {
            angle = -angle;
        }
        rotationY = (float)angle;

        setChanged();
        return true;
    }

    @Override
    public String getInputKey() {
        return "";
    }

    @Override
    public String getOutputKey() {
        return "";
    }

    @Override
    public void markDispatchable() {
        requestsClientUpdate = true;
    }

    public IItemHandlerModifiable getItemHandlerModifiable() {
        if (itemHandlerWrapper == null) {
            itemHandlerWrapper = new InvWrapper(getItemHandler());
        }
        return itemHandlerWrapper;
    }

    @Override
    protected SimpleContainer createItemHandler() {
        return new SimpleContainer(1) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean canPlaceItem(int index, ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ILens;
            }
        };
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        super.writePacketNBT(cmp);
        cmp.putInt(TAG_MANA, mana);
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

        cmp.putLong(TAG_UUID_MOST, identity.getMostSignificantBits());
        cmp.putLong(TAG_UUID_LEAST, identity.getLeastSignificantBits());
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        super.readPacketNBT(cmp);
        mana = cmp.getInt(TAG_MANA);
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

        long most = cmp.getLong(TAG_UUID_MOST);
        long least = cmp.getLong(TAG_UUID_LEAST);
        if (most != 0 || least != 0) {
            identity = new UUID(most, least);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readPacketNBT(tag);
    }

    @Nullable
    @Override
    public Packet getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            readPacketNBT(tag);
        }
    }

    @NotNull
    @Override
    public  LazyOptional getCapability(@NotNull Capability cap, @Nullable Direction side) {
        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> new WandHud(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    public static class WandHud implements IWandHUD {
        private final TileAdvancedSpreader spreader;

        public WandHud(TileAdvancedSpreader spreader) {
            this.spreader = spreader;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            int color = 0xADFF2F;
            String name = (new ItemStack(this.spreader.getBlockState().getBlock())).getHoverName().getString();
            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, spreader.getCurrentMana(), spreader.getMaxMana(), name);
            ItemStack lens = spreader.getItemHandlerModifiable().getStackInSlot(0);
            if (!lens.isEmpty()) {
                Component lensName = lens.getHoverName();
                int width = 16 + mc.font.width(lensName) / 2;
                int x = mc.getWindow().getGuiScaledWidth() / 2 - width;
                int y = mc.getWindow().getGuiScaledHeight() / 2 + 50;
                mc.font.drawShadow(ms, lensName, x + 20, y + 5, color);
                mc.getItemRenderer().renderAndDecorateItem(lens, x, y);
            }

            if (spreader.receiver != null) {
                var receiverPos = spreader.receiver.getManaReceiverPos();
                ItemStack receiverStack = new ItemStack(
                        spreader.level.getBlockState(receiverPos).getBlock()
                );
                if (!receiverStack.isEmpty()) {
                    String stackName = receiverStack.getHoverName().getString();
                    int width = 16 + mc.font.width(stackName) / 2;
                    int x = mc.getWindow().getGuiScaledWidth() / 2 - width;
                    int y = mc.getWindow().getGuiScaledHeight() / 2 + 30;
                    mc.font.drawShadow(ms, stackName, x + 20, y + 5, color);
                    mc.getItemRenderer().renderAndDecorateItem(receiverStack, x, y);
                }
            }

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
    }
}
