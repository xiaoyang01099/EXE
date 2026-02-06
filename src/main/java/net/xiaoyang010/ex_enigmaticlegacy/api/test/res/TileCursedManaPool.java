package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IKeyLocked;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nullable;

public class TileCursedManaPool extends BlockEntity implements ICursedManaPool, IWandable, IKeyLocked, IThrottledPacket {
    private static final String TAG_CURSED_MANA = "cursedMana";
    private static final String TAG_CURSED_COLOR = "cursedColor";
    private static final String TAG_OUTPUTTING = "outputting";
    private static final String TAG_CORRUPTION = "corruption";

    private static final int BASE_CAPACITY = 1000000;
    private static final int CURSE_CAPACITY_BONUS = 100000;
    public static final int MAX_MANA = 1000000;

    private int ticks = 0;
    public int cursedMana = 0;
    private DyeColor cursedColor = DyeColor.PURPLE;
    private boolean outputting = false;
    private int corruptionLevel = 0;
    private String inputKey = "";
    private final String outputKey = "";
    private boolean sendPacket = false;

    public TileCursedManaPool(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURSED_MANA_POOL.get(), pos, state);
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

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCursedManaPool pool) {
        pool.onClientDisplayTick();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCursedManaPool pool) {
        if (pool.sendPacket && pool.ticks % 10 == 0) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(pool);
            pool.sendPacket = false;
        }

        if (pool.cursedMana > pool.getMaxCursedMana() * 0.8) {
            pool.corruptNearbyNormalPools();
        }

        if (pool.corruptionLevel > 0 && level.getGameTime() % 20 == 0) {
            ManaCorruptionManager.applyCorruptionEffects(level, pos, pool.corruptionLevel);
        }

        pool.ticks++;
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
    public void markDispatchable() {
        sendPacket = true;
    }

    @Override
    public boolean canReceiveCursedManaFromBursts() {
        return !outputting;
    }

    @Override
    public void onClientDisplayTick() {
        if (level == null || !level.isClientSide) return;

        if (cursedMana > 0 && level.random.nextInt(10) == 0) {
            int color = cursedColor.getFireworkColor();
            float r = (color >> 16 & 0xFF) / 255F;
            float g = (color >> 8 & 0xFF) / 255F;
            float b = (color & 0xFF) / 255F;

            WispParticleData data = WispParticleData.wisp(0.1F, r, g, b, true);
            level.addParticle(data,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
    }

    @Override
    public float getCursedManaYieldMultiplier(ICursedManaBurst burst) {
        return 1.0F;
    }

    @Override
    public int getMaxCursedMana() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BlockCursedManaPool block) {
            switch (block.variant) {
                case CREATIVE: return Integer.MAX_VALUE;
                case DILUTED: return BASE_CAPACITY / 2;
                case CORRUPTED: return BASE_CAPACITY * 2;
                default: return BASE_CAPACITY;
            }
        }
        return BASE_CAPACITY + getCurseCapacityBonus();
    }

    @Override
    public boolean isOutputtingCursedPower() {
        return outputting;
    }

    @Override
    public DyeColor getCursedColor() {
        return cursedColor;
    }

    @Override
    public void setCursedColor(DyeColor color) {
        this.cursedColor = color;
        setChanged();
        markDispatchable();
    }

    private int getCurseCapacityBonus() {
        if (level == null) return 0;

        AABB bounds = new AABB(
                worldPosition.getX() - 8, worldPosition.getY() - 8, worldPosition.getZ() - 8,
                worldPosition.getX() + 9, worldPosition.getY() + 9, worldPosition.getZ() + 9
        );

        return level.getEntitiesOfClass(Player.class, bounds)
                .stream()
                .filter(CurseAbilityHandler.INSTANCE::isCursed)
                .mapToInt(CurseAbilityHandler.INSTANCE::getCurseLevel)
                .max()
                .orElse(0) * CURSE_CAPACITY_BONUS;
    }

    private void corruptNearbyNormalPools() {
        if (level == null || level.isClientSide) return;

        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockEntity tile = level.getBlockEntity(checkPos);

                    if (tile instanceof vazkii.botania.common.block.tile.mana.TilePool normalPool
                            && !(tile instanceof TileCursedManaPool)) {

                        double distance = Math.sqrt(x*x + y*y + z*z);
                        int corruptionAmount = (int) (10 / (distance + 1));

                        int normalMana = normalPool.getCurrentMana();
                        if (normalMana > 0) {
                            int toCorrupt = Math.min(normalMana, corruptionAmount * 10);
                            normalPool.receiveMana(-toCorrupt);
                            this.receiveCursedMana(toCorrupt / 2);

                            this.corruptionLevel = Math.min(100, this.corruptionLevel + 1);
                            spawnCorruptionParticles(checkPos);
                        }
                    }
                }
            }
        }
    }

    private void spawnCorruptionParticles(BlockPos targetPos) {
        if (level == null || !level.isClientSide) return;

        for (int i = 0; i < 5; i++) {
            double t = i / 5.0;
            double x = worldPosition.getX() + 0.5 + (targetPos.getX() - worldPosition.getX()) * t;
            double y = worldPosition.getY() + 0.5 + (targetPos.getY() - worldPosition.getY()) * t;
            double z = worldPosition.getZ() + 0.5 + (targetPos.getZ() - worldPosition.getZ()) * t;

            WispParticleData data = WispParticleData.wisp(0.2F, 0.5F, 0.0F, 0.5F, true);
            level.addParticle(data, x, y, z, 0, 0, 0);
        }
    }

    public void collideEntityItem(ItemEntity item) {
        if (level == null || level.isClientSide || item.isRemoved()) {
            return;
        }
    }

    public boolean consumeCursedMana(int amount) {
        if (cursedMana >= amount) {
            cursedMana -= amount;
            setChanged();
            markDispatchable();
            return true;
        }
        return false;
    }

    public static int calculateComparatorLevel(int mana, int maxMana) {
        if (maxMana == 0) return 0;
        return (int) ((mana / (float) maxMana) * 15);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            CursedManaNetwork.getInstance().fireCursedManaNetworkEvent(
                    this,
                    CursedManaBlockType.POOL,
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
                    CursedManaBlockType.POOL,
                    CursedManaNetworkAction.REMOVE
            );
        }
    }

    public void readPacketNBT(CompoundTag cmp) {
        cursedMana = cmp.getInt(TAG_CURSED_MANA);
        outputting = cmp.getBoolean(TAG_OUTPUTTING);
        cursedColor = DyeColor.byId(cmp.getInt(TAG_CURSED_COLOR));
        corruptionLevel = cmp.getInt(TAG_CORRUPTION);
    }

    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_CURSED_MANA, cursedMana);
        cmp.putBoolean(TAG_OUTPUTTING, outputting);
        cmp.putInt(TAG_CURSED_COLOR, cursedColor.getId());
        cmp.putInt(TAG_CORRUPTION, corruptionLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writePacketNBT(tag);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> new WandHud(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player == null || player.isShiftKeyDown()) {
            outputting = !outputting;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
        return true;
    }

    @Override
    public String getInputKey() {
        return inputKey;
    }

    @Override
    public String getOutputKey() {
        return outputKey;
    }

    public static class WandHud implements IWandHUD {
        private final TileCursedManaPool pool;

        public WandHud(TileCursedManaPool pool) {
            this.pool = pool;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            ItemStack poolStack = new ItemStack(pool.getBlockState().getBlock());
            String name = poolStack.getHoverName().getString();
            int color = 0x8B00FF;

            BotaniaAPIClient.instance().drawSimpleManaHUD(
                    ms, color,
                    pool.getCurrentCursedMana(),
                    pool.getMaxCursedMana(),
                    name
            );

            int x = mc.getWindow().getGuiScaledWidth() / 2 - 11;
            int y = mc.getWindow().getGuiScaledHeight() / 2 + 30;

            int u = pool.outputting ? 22 : 0;
            int v = 38;

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            RenderSystem.setShaderTexture(0, HUDHandler.manaBar);
            RenderHelper.drawTexturedModalRect(ms, x, y, u, v, 22, 15);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            ItemStack tablet = new ItemStack(ModItems.manaTablet);
            ItemManaTablet.setStackCreative(tablet);

            mc.getItemRenderer().renderAndDecorateItem(tablet, x - 20, y);
            mc.getItemRenderer().renderAndDecorateItem(poolStack, x + 26, y);

            RenderSystem.disableBlend();
        }
    }
}
