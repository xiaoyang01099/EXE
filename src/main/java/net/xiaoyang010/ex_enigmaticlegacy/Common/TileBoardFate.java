package net.xiaoyang010.ex_enigmaticlegacy.Common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import vazkii.botania.api.item.IRelic;

import java.util.UUID;

public class TileBoardFate extends TileInventory {
    public byte[] slotChance = new byte[getContainerSize()];
    public int[] clientTick = new int[]{0, 0, 0, 0};
    public boolean requestUpdate;

    public TileBoardFate(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void serverTick() {
        if (!level.isClientSide) {
            updateServer();
        } else {
            updateAnimationTicks();
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
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        } else {
            if (!level.isClientSide) {
                ItemStack relic = AdvancedBotanyAPI.relicList.get(Math.min(relicCount - 1, AdvancedBotanyAPI.relicList.size() - 1)).copy();
                level.playSound(null, worldPosition, SoundEvents.ARROW_SHOOT, SoundSource.BLOCKS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

                if (!hasRelicAdvancement(player, relic) && ConfigHandler.fateBoardRelicEnables[relicCount - 1]) {
                    bindRelicToPlayer(relic, player);
                    ItemEntity entityItem = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, relic);
                    player.sendMessage(new TranslatableComponent("botaniamisc.diceRoll", relicCount), player.getUUID());
                    level.addFreshEntity(entityItem);
                } else {
                    player.sendMessage(new TranslatableComponent("botaniamisc.dudDiceRoll", relicCount), player.getUUID());
                }
                requestUpdate = true;
            }
            return true;
        }
    }

    private void dropRelic(Player player, int slot) {
        ItemEntity entityItem = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, getItem(slot).copy());
        float f3 = 0.15F;
        Vec3 vec = player.getLookAngle();
        entityItem.setDeltaMovement(vec.x * f3, 0.25, vec.z * f3);
        setItem(slot, ItemStack.EMPTY);
        level.addFreshEntity(entityItem);
    }

    public static boolean hasRelicAdvancement(Player player, ItemStack rStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (!(rStack.getItem() instanceof IRelic relic)) return false;

        // In 1.18.2, we check for advancements instead of achievements
        // The advancement system is more complex, so for now we'll use a simpler approach
        // You can customize this based on your specific advancement requirements

        // Check if the player has the relic advancement
        String relicName = getRelicName(rStack);
        if (relicName != null) {
            // Create advancement resource location based on relic name
            ResourceLocation advancementId = new ResourceLocation("botania", "challenge/" + relicName);
            var advancement = serverPlayer.getServer().getAdvancements().getAdvancement(advancementId);
            if (advancement != null) {
                return serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
            }
        }

        // If no advancement found, return false to allow the relic to be given
        return false;
    }

    private static String getRelicName(ItemStack stack) {
        // Map item to relic name - you'll need to customize this based on your relic items
        String itemName = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();

        // Map common relic names
        return switch (itemName) {
            case "dice" -> "dice";
            case "infinite_fruit", "fruit_of_grisaia" -> "grisaia";
            case "king_key" -> "king_key";
            case "flugel_eye" -> "flugel_eye";
            case "thor_ring" -> "thor_ring";
            case "odin_ring" -> "odin_ring";
            case "loki_ring" -> "loki_ring";
            default -> null;
        };
    }

    /**
     * Check if the relic belongs to the right player
     */
    public static boolean isRightPlayer(Player player, ItemStack relic) {
        if (!(relic.getItem() instanceof IRelic relicItem)) return true;

        String boundPlayer = String.valueOf(relicItem.getSoulbindUUID());
        if (boundPlayer == null || boundPlayer.isEmpty()) {
            return true; // Not bound to anyone
        }

        return boundPlayer.equals(player.getGameProfile().getName());
    }

    /**
     * Bind the relic to the player
     */
    public static void bindRelicToPlayer(ItemStack relic, Player player) {
        if (relic.getItem() instanceof IRelic relicItem) {
            relicItem.bindToUUID(UUID.fromString(player.getGameProfile().getName()));
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
                worldPosition.getX() + 1, worldPosition.getY() + 0.7, worldPosition.getZ() + 1);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (!item.isRemoved() && !item.getItem().isEmpty()) {
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
                            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6F, 1.0F);
                            return hasUpdate;
                        }
                    }
                }
            }
        }
        return hasUpdate;
    }

    public static boolean isDice(ItemStack stack) {
        for (ItemStack dice : AdvancedBotanyAPI.diceList) {
            if (dice.getItem() == stack.getItem() &&
                    (dice.getDamageValue() == stack.getDamageValue() || dice.getDamageValue() == 32767)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);
        slotChance = tag.getByteArray("slotChance");
        requestUpdate = tag.getBoolean("requestUpdate");
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        tag.putByteArray("slotChance", slotChance);
        tag.putBoolean("requestUpdate", requestUpdate);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }
}