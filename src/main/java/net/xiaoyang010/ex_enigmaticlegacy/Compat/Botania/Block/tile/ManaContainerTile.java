package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.ManaContainerBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.BlockEntityBase;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.recipe.IManaInfusionRecipe;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.block.tile.mana.TileBellows;
import vazkii.botania.common.crafting.ModRecipeTypes;
import vazkii.botania.common.handler.ManaNetworkHandler;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.helper.EntityHelper;
import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.proxy.IProxy;
import vazkii.botania.xplat.BotaniaConfig;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ManaContainerTile extends BlockEntityBase implements IManaPool, ISparkAttachable, IKeyLocked, IThrottledPacket, IWandable {
    public static final int MAX_MANA = 800000000;
    public static final int MAX_MANA_DILLUTED = 64000000;

    private static final String TAG_MANA = "mana";
    private static final String TAG_OUTPUTTING = "outputting";
    private static final String TAG_COLOR = "color";
    private static final String TAG_MANA_CAP = "manaCap";
    private static final String TAG_CAN_ACCEPT = "canAccept";
    private static final String TAG_CAN_SPARE = "canSpare";
    private static final String TAG_INPUT_KEY = "inputKey";
    private static final String TAG_OUTPUT_KEY = "outputKey";
    private static final int CRAFT_EFFECT_EVENT = 0;
    private static final int CHARGE_EFFECT_EVENT = 1;

    private boolean outputting = false;

    private DyeColor color = DyeColor.WHITE;
    private int mana;

    public int manaCap = -1;
    private int soundTicks = 0;
    private boolean canAccept = true;
    private boolean canSpare = true;
    boolean isDoingTransfer = false;
    int ticksDoingTransfer = 0;

    private String inputKey = "";
    private final String outputKey = "";

    private int ticks = 0;
    private boolean sendPacket = false;

    public ManaContainerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, BotaniaForgeCapabilities.MANA_RECEIVER, BotaniaForgeCapabilities.SPARK_ATTACHABLE, BotaniaForgeCapabilities.WANDABLE);
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        switch (event) {
            case CRAFT_EFFECT_EVENT: {
                if (level.isClientSide) {
                    float w = -1F / 8F * 2.4F;
                    float baseY = 0.7F;

                    for (int i = 0; i < 25; i++) {
                        float red = (float) Math.random();
                        float green = (float) Math.random();
                        float blue = (float) Math.random();
                        SparkleParticleData data = SparkleParticleData.sparkle((float) Math.random(), red, green, blue, 10);

                        double particleX = worldPosition.getX() + 0.5 + w + Math.random() * 0.6 - 0.3;
                        double particleY = worldPosition.getY() + baseY + Math.random() * 0.4;
                        double particleZ = worldPosition.getZ() + 0.5 + w + Math.random() * 0.6 - 0.3;

                        level.addParticle(data, particleX, particleY, particleZ, 0, 0, 0);
                    }
                }
                return true;
            }
            case CHARGE_EFFECT_EVENT: {
                if (level.isClientSide) {
                    if (BotaniaConfig.common().chargingAnimationEnabled()) {
                        boolean outputting = param == 1;

                        float w = -1F / 8F * 2.4F;
                        float manaY = 0.7F;

                        Vec3 manaVec = Vec3.atLowerCornerOf(worldPosition).add(0.5 + w, manaY, 0.5 + w);

                        Vec3 itemVec = Vec3.atLowerCornerOf(worldPosition).add(0.5, 1.2 + Math.random() * 0.3, 0.5);

                        IProxy.INSTANCE.lightningFX(
                                outputting ? manaVec : itemVec,
                                outputting ? itemVec : manaVec,
                                80,
                                level.random.nextLong(),
                                0x4400799c,
                                0x4400C6FF
                        );
                    }
                }
                return true;
            }
            default:
                return super.triggerEvent(event, param);
        }
    }

    public static void clientTick(Level level, BlockPos worldPosition, BlockState state, ManaContainerTile self) {
        self.initManaCapAndNetwork();
        double particleChance = 1F - (double) self.getCurrentMana() / (double) self.manaCap * 0.1;
        if (Math.random() > particleChance) {
            long time = level.getGameTime();

            float positionOffset = (worldPosition.getX() + worldPosition.getZ()) * 0.1f;
            float timeOffset = time * 0.03f;
            float hue = (timeOffset + positionOffset + (float) Math.random() * 0.3f) % 1.0f;

            float manaRatio = (float) self.getCurrentMana() / self.manaCap;
            float saturation = 0.6f + manaRatio * 0.4f;
            float brightness = 0.8f + (float) Math.random() * 0.2f;

            int rgb = Color.HSBtoRGB(hue, saturation, brightness);
            float red = ((rgb >> 16) & 0xFF) / 255.0f;
            float green = ((rgb >> 8) & 0xFF) / 255.0f;
            float blue = (rgb & 0xFF) / 255.0f;

            WispParticleData data = WispParticleData.wisp(
                    (float) Math.random() / 2.5F + 0.1f,
                    red, green, blue,
                    3F + (float) Math.random() * 2F
            );

            level.addParticle(data,
                    worldPosition.getX() + 0.2 + Math.random() * 0.6,
                    worldPosition.getY() + 0.5 + Math.random() * 0.4,
                    worldPosition.getZ() + 0.2 + Math.random() * 0.6,
                    (Math.random() - 0.5) * 0.02,
                    (float) Math.random() / 20F,
                    (Math.random() - 0.5) * 0.02
            );
        }
    }

    public static List<IManaInfusionRecipe> manaInfusionRecipes(Level world) {
        return ModRecipeTypes.getRecipes(world, ModRecipeTypes.MANA_INFUSION_TYPE).values().stream()
                .filter(r -> r instanceof IManaInfusionRecipe)
                .map(r -> (IManaInfusionRecipe) r)
                .collect(Collectors.toList());
    }

    public IManaInfusionRecipe getMatchingRecipe(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        List<IManaInfusionRecipe> matchingNonCatRecipes = new ArrayList<>();
        List<IManaInfusionRecipe> matchingCatRecipes = new ArrayList<>();

        for (IManaInfusionRecipe recipe : manaInfusionRecipes(level)) {
            if (recipe.matches(stack)) {
                if (recipe.getRecipeCatalyst() == null) {
                    matchingNonCatRecipes.add(recipe);
                } else if (recipe.getRecipeCatalyst().test(state)) {
                    matchingCatRecipes.add(recipe);
                }
            }
        }

        return !matchingCatRecipes.isEmpty() ? matchingCatRecipes.get(0) : !matchingNonCatRecipes.isEmpty() ? matchingNonCatRecipes.get(0) : null;
    }

    public boolean collideEntityItem(ItemEntity item) {
        if (level.isClientSide || !item.isAlive() || item.getItem().isEmpty()) {
            return false;
        }

        ItemStack stack = item.getItem();

        if (stack.getItem() instanceof IManaDissolvable dissolvable) {
            dissolvable.onDissolveTick(this, stack, item);
        }

        if (IXplatAbstractions.INSTANCE.itemFlagsComponent(item).getManaInfusionCooldown() > 0) {
            return false;
        }

        IManaInfusionRecipe recipe = getMatchingRecipe(stack, level.getBlockState(worldPosition.below()));

        if (recipe != null) {
            int mana = recipe.getManaToConsume();
            if (getCurrentMana() >= mana) {
                receiveMana(-mana);

                ItemStack output = recipe.getRecipeOutput(stack);
                EntityHelper.shrinkItem(item);
                item.setOnGround(false);

                ItemEntity outputItem = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, output);
                IXplatAbstractions.INSTANCE.itemFlagsComponent(outputItem).markNewlyInfused();
                level.addFreshEntity(outputItem);

                craftingFanciness();
                return true;
            }
        }

        return false;
    }

    private void craftingFanciness() {
        if (soundTicks == 0) {
            level.playSound(null, worldPosition, ModSounds.manaPoolCraft, SoundSource.BLOCKS, 1F, 1F);
            soundTicks = 6;
        }

        level.blockEvent(getBlockPos(), getBlockState().getBlock(), CRAFT_EFFECT_EVENT, 0);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.REMOVE);
    }

    public static int calculateComparatorLevel(int mana, int max) {
        int val = (int) ((double) mana / (double) max * 15.0);
        if (mana > 0) {
            val = Math.max(val, 1);
        }
        return val;
    }

    private void initManaCapAndNetwork() {
        if (manaCap == -1) {
            manaCap = ((ManaContainerBlock) getBlockState().getBlock()).variant == ManaContainerBlock.Variant.DEFAULT ? MAX_MANA : MAX_MANA_DILLUTED;
        }
        if (!ManaNetworkHandler.instance.isPoolIn(level, this) && !isRemoved()) {
            BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.ADD);
        }
    }

    public static void serverTick(Level level, BlockPos worldPosition, BlockState state, ManaContainerTile self) {
        self.initManaCapAndNetwork();
        boolean wasDoingTransfer = self.isDoingTransfer;
        self.isDoingTransfer = false;

        if (self.soundTicks > 0) {
            self.soundTicks--;
        }

        if (self.sendPacket && self.ticks % 10 == 0) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
            self.sendPacket = false;
        }

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition, worldPosition.offset(1, 1, 1)));
        for (ItemEntity item : items) {
            if (!item.isAlive()) {
                continue;
            }

            ItemStack stack = item.getItem();
            var mana = IXplatAbstractions.INSTANCE.findManaItem(stack);
            if (!stack.isEmpty() && mana != null) {
                if (self.outputting && mana.canReceiveManaFromPool(self) || !self.outputting && mana.canExportManaToPool(self)) {
                    boolean didSomething = false;

                    int bellowCount = 0;
                    if (self.outputting) {
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            BlockEntity tile = level.getBlockEntity(worldPosition.relative(dir));
                            if (tile instanceof TileBellows bellows && bellows.getLinkedTile() == self) {
                                bellowCount++;
                            }
                        }
                    }
                    int transfRate = 1000 * (bellowCount + 1);

                    if (self.outputting) {
                        if (self.canSpare) {
                            if (self.getCurrentMana() > 0 && mana.getMana() < mana.getMaxMana()) {
                                didSomething = true;
                            }

                            int manaVal = Math.min(transfRate, Math.min(self.getCurrentMana(), mana.getMaxMana() - mana.getMana()));
                            mana.addMana(manaVal);
                            self.receiveMana(-manaVal);
                        }
                    } else {
                        if (self.canAccept) {
                            if (mana.getMana() > 0 && !self.isFull()) {
                                didSomething = true;
                            }

                            int manaVal = Math.min(transfRate, Math.min(self.manaCap - self.getCurrentMana(), mana.getMana()));
                            if (manaVal == 0 && self.level.getBlockState(worldPosition.below()).is(ModBlocks.manaVoid)) {
                                manaVal = Math.min(transfRate, mana.getMana());
                            }
                            mana.addMana(-manaVal);
                            self.receiveMana(manaVal);
                        }
                    }

                    if (didSomething) {
                        if (BotaniaConfig.common().chargingAnimationEnabled() && level.random.nextInt(20) == 0) {
                            level.blockEvent(worldPosition, state.getBlock(), CHARGE_EFFECT_EVENT, self.outputting ? 1 : 0);
                        }
                        EntityHelper.syncItem(item);
                        self.isDoingTransfer = self.outputting;
                    }
                }
            }
        }

        if (self.isDoingTransfer) {
            self.ticksDoingTransfer++;
        } else {
            self.ticksDoingTransfer = 0;
            if (wasDoingTransfer) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
            }
        }

        self.ticks++;
    }

    @Override
    public boolean onUsedByWand(@javax.annotation.Nullable Player player, ItemStack stack, Direction side) {
        if (player == null || player.isShiftKeyDown()) {
            outputting = !outputting;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
        return true;
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
        private final ManaContainerTile pool;

        public WandHud(ManaContainerTile pool) {
            this.pool = pool;
        }

//        @Override
//        public void renderHUD(PoseStack ms, Minecraft mc) {
//            ItemStack poolStack = new ItemStack(pool.getBlockState().getBlock());
//            String name = poolStack.getHoverName().getString();
//            int color = 0xFFD700;
//
//            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, pool.getCurrentMana(), pool.manaCap, name);
//
//            ClientHelper.drawPoolManaHUD(ms, name, pool.getCurrentMana(), pool.manaCap, color);
//
//            int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 11;
//            int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 + 30;
//
//            ItemStack tablet = new ItemStack(ModItems.manaTablet);
//            ItemManaTablet.setStackCreative(tablet);
//
//            mc.getItemRenderer().renderAndDecorateItem(tablet, x - 20, y);
//            mc.getItemRenderer().renderAndDecorateItem(poolStack, x + 26, y);
//        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            ItemStack poolStack = new ItemStack(pool.getBlockState().getBlock());
            String name = poolStack.getHoverName().getString();
            int color = 0xFFD700;
            BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, pool.getCurrentMana(), pool.manaCap, name);

            int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 11;
            int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 + 30;

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

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public boolean isOutputtingPower() {
        return outputting;
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
        if (getBlockState().getBlock() instanceof ManaContainerBlock pool) {
            return pool.variant == ManaContainerBlock.Variant.CREATIVE ? MAX_MANA_DILLUTED : mana;
        }
        return 0;
    }

    @Override
    public String getInputKey() {
        return inputKey;
    }

    @Override
    public String getOutputKey() {
        return outputKey;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public IManaSpark getAttachedSpark() {
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)), Predicates.instanceOf(IManaSpark.class));
        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (IManaSpark) e;
        }

        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    @Override
    public int getAvailableSpaceForMana() {
        int space = Math.max(0, manaCap - getCurrentMana());
        if (space > 0) {
            return space;
        } else if (level.getBlockState(worldPosition.below()).is(ModBlocks.manaVoid)) {
            return manaCap;
        } else {
            return 0;
        }
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public void setColor(DyeColor color) {
        this.color = color;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void markDispatchable() {
        sendPacket = true;
    }

    @Override
    public boolean isFull() {
        BlockState stateBelow = level.getBlockState(worldPosition.below());
        return !stateBelow.is(ModBlocks.manaVoid) && getCurrentMana() >= manaCap;
    }

    @Override
    public void receiveMana(int mana) {
        int old = this.mana;
        this.mana = Math.max(0, Math.min(getCurrentMana() + mana, manaCap));
        if (old != this.mana) {
            setChanged();
            markDispatchable();
        }
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_MANA, mana);
        cmp.putBoolean(TAG_OUTPUTTING, outputting);
        cmp.putInt(TAG_COLOR, color.getId());

        cmp.putInt(TAG_MANA_CAP, manaCap);
        cmp.putBoolean(TAG_CAN_ACCEPT, canAccept);
        cmp.putBoolean(TAG_CAN_SPARE, canSpare);

        cmp.putString(TAG_INPUT_KEY, inputKey);
        cmp.putString(TAG_OUTPUT_KEY, outputKey);
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        mana = cmp.getInt(TAG_MANA);
        outputting = cmp.getBoolean(TAG_OUTPUTTING);
        color = DyeColor.byId(cmp.getInt(TAG_COLOR));

        if (cmp.contains(TAG_MANA_CAP)) {
            manaCap = cmp.getInt(TAG_MANA_CAP);
        }
        if (cmp.contains(TAG_CAN_ACCEPT)) {
            canAccept = cmp.getBoolean(TAG_CAN_ACCEPT);
        }
        if (cmp.contains(TAG_CAN_SPARE)) {
            canSpare = cmp.getBoolean(TAG_CAN_SPARE);
        }

        if (cmp.contains(TAG_INPUT_KEY)) {
            inputKey = cmp.getString(TAG_INPUT_KEY);
        }
        if (cmp.contains(TAG_OUTPUT_KEY)) {
            inputKey = cmp.getString(TAG_OUTPUT_KEY);
        }

    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        writePacketNBT(tag);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        readPacketNBT(pkt.getTag());
    }
}