package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.xplat.IXplatAbstractions;

import java.awt.*;
import java.util.List;


public class TileBoardFate extends TileInventory {
    public static final List<ItemStack> FATE_RELIC_STACKS = List.of(
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.WEATHER_STONE.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.BLACK_HALO.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.SUPERPOSITION_RING.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.TALISMAN_HIDDEN_RICHES.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.FLOWER_FINDER_WAND.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.NEBULOUS_CORE.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.SHINY_STONE.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.ANCIENT_ALPHIRINE.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.APOTHEOSIS.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.CHAO_TOME.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.CRIMSON_SPELL.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.DARK_SUN_RING.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.DEIFIC_AMULET.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.DORMANT_ARCANUM.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.ELDRITCH_SPELL.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.GHASTLY_SKULL.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.MANAITA_SHEARS.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.OVERTHROWER.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.SOUL_TOME.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.TELEKINESIS_TOME.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.MISSILE_TOME.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.XP_TOME.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.THUNDER_PEAL.get()),
            new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.FALSE_JUSTICE.get())
            );

    private static final String TAG_SLOT_CHANCE = "slotChance";
    private static final String TAG_REQUEST_UPDATE = "requestUpdate";

    public byte[] slotChance = new byte[getContainerSize()];
    public int[] clientTick = new int[]{0, 0, 0, 0};
    public boolean requestUpdate;

    public TileBoardFate(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(TileBoardFate tile) {
        if (tile.level == null) return;

        if (!tile.level.isClientSide) {
            tile.updateServer();
        } else {
            tile.updateAnimationTicks();
        }
    }

    public void updateAnimationTicks() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                clientTick[i]++;
            } else {
                clientTick[i] = 0;
            }
        }
    }

    protected void updateServer() {
        if (requestUpdate) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

        boolean hasUpdate = false;
        if (hasFreeSlot()) {
            hasUpdate = setDiceFate();
        }

        requestUpdate = hasUpdate;
    }

    public boolean spawnRelic(Player player) {
        int relicCount = 0;

        for (int i = 0; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                slotChance[i] = 0;
            } else {
                if (!isRightPlayer(player, getItem(i))) {
                    if (!level.isClientSide) {
                        dropRelic(player, i);
                    }
                    return true;
                }
                setItem(i, ItemStack.EMPTY);
            }
            relicCount += slotChance[i];
        }

        if (relicCount < 1) {
            return false;
        }

        if (!level.isClientSide) {
            ItemStack relic = getRelicFromList(Math.min(relicCount - 1, getRelicListSize() - 1));

            level.playSound(null, worldPosition, SoundEvents.ARROW_SHOOT, SoundSource.BLOCKS,
                    0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

            if (!relic.isEmpty()) {
                bindRelicToPlayer(relic, player);

                ItemEntity entityItem = new ItemEntity(level,
                        worldPosition.getX() + 0.5F,
                        worldPosition.getY() + 0.5F,
                        worldPosition.getZ() + 0.5F,
                        relic);

                Component message = new TranslatableComponent("botaniamisc.diceRoll", relicCount)
                        .withStyle(net.minecraft.ChatFormatting.DARK_GREEN);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendMessage(message, ChatType.GAME_INFO, player.getUUID());
                }

                level.addFreshEntity(entityItem);
            } else {
                Component message = new TranslatableComponent("botaniamisc.dudDiceRoll", relicCount)
                        .withStyle(net.minecraft.ChatFormatting.DARK_GREEN);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendMessage(message, ChatType.GAME_INFO, player.getUUID());
                }
            }

            requestUpdate = true;
        }

        return true;
    }

    private void dropRelic(Player player, int slot) {
        ItemEntity entityItem = new ItemEntity(level,
                worldPosition.getX() + 0.5F,
                worldPosition.getY() + 0.8F,
                worldPosition.getZ() + 0.5F,
                getItem(slot).copy());

        float f3 = 0.15F;
        Vec3 vec = player.getLookAngle();
        entityItem.setDeltaMovement(vec.x * f3, 0.25F, vec.z * f3);

        setItem(slot, ItemStack.EMPTY);
        level.addFreshEntity(entityItem);
    }

    public static boolean hasRelicAchievement(Player player, ItemStack rStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        IRelic relic = IXplatAbstractions.INSTANCE.findRelic(rStack);
        if (relic == null || relic.getAdvancement() == null) {
            return false;
        }

        return serverPlayer.getAdvancements().getOrStartProgress(
                serverPlayer.getServer().getAdvancements().getAdvancement(relic.getAdvancement())
        ).isDone();
    }

    public static boolean isRightPlayer(Player player, ItemStack stack) {
        IRelic relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        return relic == null || relic.isRightPlayer(player);
    }

    private void bindRelicToPlayer(ItemStack relic, Player player) {
        IRelic iRelic = IXplatAbstractions.INSTANCE.findRelic(relic);
        if (iRelic != null) {
            iRelic.bindToUUID(player.getUUID());
        }
    }

    protected boolean hasFreeSlot() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected boolean setDiceFate() {
        boolean hasUpdate = false;

        AABB aabb = new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 0.7F, worldPosition.getZ() + 1);

        List<ItemEntity> items = null;
        if (level != null) {
            items = level.getEntitiesOfClass(ItemEntity.class, aabb);
        }

        for (ItemEntity item : items) {
            if (!item.isRemoved() && item.getItem() != null) {
                ItemStack stack = item.getItem();
                if (isDice(stack)) {
                    for (int s = 0; s < getContainerSize(); s++) {
                        ItemStack slotStack = getItem(s);
                        if (slotStack.isEmpty()) {
                            ItemStack copy = stack.copy();
                            copy.setCount(1);
                            setItem(s, copy);
                            slotChance[s] = (byte) (level.random.nextInt(6) + 1);

                            stack.shrink(1);
                            if (stack.isEmpty()) {
                                item.discard();
                            }

                            hasUpdate = true;
                            level.playSound(null, worldPosition, SoundEvents.WOOD_PLACE,
                                    SoundSource.BLOCKS, 0.6F, 1.0F);
                            return hasUpdate;
                        }
                    }
                }
            }
        }

        return hasUpdate;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileBoardFate tile) {
        if (tile.level == null) return;
        tile.updateServer();

        if (tile.level.getGameTime() % 4 == 0) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileBoardFate tile) {
        if (tile.level == null) return;
        tile.updateAnimationTicks();
        tile.clientTick();
    }

    public void serverTick() {
        if (level == null) return;

        updateServer();

        if (level.getGameTime() % 4 == 0) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
    }

    public void clientTick() {
        if (level == null) return;

        updateAnimationTicks();

        if (level.getGameTime() % 4 == 0) {
            for (int i = 0; i < 4; i++) {
                double x = worldPosition.getX() + 0.2 + level.random.nextDouble() * 0.6;
                double y = worldPosition.getY() + 0.1 + level.random.nextDouble() * 0.8;
                double z = worldPosition.getZ() + 0.2 + level.random.nextDouble() * 0.6;

                float hue = (level.getGameTime() % 360) / 360.0f + level.random.nextFloat() * 0.5f;
                int color = Color.HSBtoRGB(hue, 0.8f, 1.0f);

                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;

                double motionX = (level.random.nextDouble() - 0.5) * 0.08;
                double motionY = level.random.nextDouble() * 0.05;
                double motionZ = (level.random.nextDouble() - 0.5) * 0.08;

                SparkleParticleData sparkleData =
                        SparkleParticleData.sparkle(
                                0.8f + level.random.nextFloat() * 0.4f,
                                red, green, blue,
                                10 + level.random.nextInt(20)
                        );

                level.addParticle(sparkleData, x, y, z, motionX, motionY, motionZ);
            }
        }

        if (level.getGameTime() % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double x = worldPosition.getX() + 0.1 + level.random.nextDouble() * 0.8;
                double y = worldPosition.getY() + 0.05 + level.random.nextDouble() * 0.9;
                double z = worldPosition.getZ() + 0.1 + level.random.nextDouble() * 0.8;

                float time = (level.getGameTime() + i * 20) % 240;
                float hue = time / 240.0f;
                int color = Color.HSBtoRGB(hue, 0.9f, 1.0f);

                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;

                double motionX = (level.random.nextDouble() - 0.5) * 0.1;
                double motionY = level.random.nextDouble() * 0.08;
                double motionZ = (level.random.nextDouble() - 0.5) * 0.1;

                switch (level.random.nextInt(4)) {
                    case 0:
                        WispParticleData wispData =
                                WispParticleData.wisp(
                                        0.3f + level.random.nextFloat() * 0.2f,
                                        red, green, blue,
                                        0.7f + level.random.nextFloat() * 0.3f
                                );
                        level.addParticle(wispData, x, y, z, motionX, motionY, motionZ);
                        break;

                    case 1:
                        SparkleParticleData sparkleData =
                                SparkleParticleData.sparkle(
                                        0.8f + level.random.nextFloat() * 0.4f,
                                        red, green, blue,
                                        10 + level.random.nextInt(15)
                                );
                        level.addParticle(sparkleData, x, y, z, motionX, motionY, motionZ);
                        break;

                    case 2:
                        SparkleParticleData fakeSparkData =
                                SparkleParticleData.fake(
                                        0.6f + level.random.nextFloat() * 0.3f,
                                        red, green, blue,
                                        8 + level.random.nextInt(12)
                                );
                        level.addParticle(fakeSparkData, x, y, z, motionX, motionY, motionZ);
                        break;

                    case 3:
                        WispParticleData magicData =
                                WispParticleData.wisp(
                                        0.2f + level.random.nextFloat() * 0.15f,
                                        red, green, blue,
                                        0.5f
                                );
                        level.addParticle(magicData, x, y, z, motionX * 0.5, motionY * 0.5, motionZ * 0.5);
                        break;
                }
            }
        }

        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!getItem(slot).isEmpty() && clientTick[slot] > 0) {
                if (level.getGameTime() % 1 == 0) {
                    double centerX = worldPosition.getX() + 0.25 + (slot * 0.5);
                    double centerY = worldPosition.getY() + 0.7;
                    double centerZ = worldPosition.getZ() + 0.5;

                    double angle = clientTick[slot] * 0.2;
                    double radius = 0.25 + Math.sin(clientTick[slot] * 0.05) * 0.1;
                    double height = Math.sin(clientTick[slot] * 0.03) * 0.15;

                    double particleX = centerX + Math.cos(angle) * radius;
                    double particleY = centerY + height;
                    double particleZ = centerZ + Math.sin(angle) * radius;

                    float hue = ((clientTick[slot] + slot * 60) % 360) / 360.0f;
                    int color = Color.HSBtoRGB(hue, 0.8f, 1.0f);

                    float red = ((color >> 16) & 0xFF) / 255.0f;
                    float green = ((color >> 8) & 0xFF) / 255.0f;
                    float blue = (color & 0xFF) / 255.0f;

                    double spiralMotionX = -Math.sin(angle) * 0.02;
                    double spiralMotionY = 0.01;
                    double spiralMotionZ = Math.cos(angle) * 0.02;

                    if (clientTick[slot] % 3 == 0) {
                        WispParticleData spiralWisp =
                                WispParticleData.wisp(
                                        0.25f, red, green, blue, 0.8f
                                );
                        level.addParticle(spiralWisp, particleX, particleY, particleZ,
                                spiralMotionX, spiralMotionY, spiralMotionZ);
                    } else {
                        SparkleParticleData spiralSparkle =
                                SparkleParticleData.sparkle(
                                        0.6f, red, green, blue, 15
                                );
                        level.addParticle(spiralSparkle, particleX, particleY, particleZ,
                                spiralMotionX, spiralMotionY, spiralMotionZ);
                    }
                }
            }
        }

        if (level.getGameTime() % 8 == 0) {
            double centerX = worldPosition.getX() + 0.5;
            double centerY = worldPosition.getY() + 0.1;
            double centerZ = worldPosition.getZ() + 0.5;

            for (int i = 0; i < 8; i++) {
                double angle = (i / 8.0) * Math.PI * 2;
                double radius = 0.8;

                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;

                float hue = (i / 8.0f + (level.getGameTime() % 200) / 200.0f) % 1.0f;
                int color = Color.HSBtoRGB(hue, 0.6f, 0.9f);

                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;

                WispParticleData ringWisp =
                        WispParticleData.wisp(
                                0.15f, red, green, blue, 0.4f
                        );
                level.addParticle(ringWisp, x, centerY, z, 0, 0.005, 0);
            }
        }
    }

    public static boolean isDice(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.dice;
    }

    private ItemStack getRelicFromList(int index) {
        return FATE_RELIC_STACKS.get(index).copy();
    }

    private int getRelicListSize() {
        return FATE_RELIC_STACKS.size();
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);
        slotChance = tag.getByteArray(TAG_SLOT_CHANCE);
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        tag.putByteArray(TAG_SLOT_CHANCE, slotChance);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return super.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return super.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return super.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        for (int i = 0; i < slotChance.length; i++) {
            slotChance[i] = 0;
        }
    }
}