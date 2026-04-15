package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.PlayerEMC;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import javax.annotation.Nullable;
import java.util.UUID;

public class EMCFlowerTile extends GeneratingFlowerBlockEntity {
    private static final String TAG_PLAYER_UUID = "playerUUID";
    private static final String TAG_MANA_PER_EMC = "manaPerEMC";

    @Nullable
    private Player player;
    @Nullable
    private UUID playerUUID;
    private int manaPerEMC;

    public EMCFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.manaPerEMC = ConfigHandler.FlowerConfig.getEMCFlowerManaPerEMC();
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) {
            return;
        }

        Player nearestPlayer = level.getNearestPlayer(
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                16.0,
                false
        );

        if (nearestPlayer == null) {
            return;
        }

        int maxMana = getMaxMana();

        if (ticksExisted % 5 == 0 && getMana() < maxMana) {
            if (playerUUID != null && isValidUUID(playerUUID)) {
                PlayerEMC playerEMC = new PlayerEMC(level, playerUUID);

                if (playerEMC.getEmc() >= 1.0) {
                    addMana(manaPerEMC);
                    playerEMC.removeEMC(1.0);
                }
            } else {
                BlockEntity be = level.getBlockEntity(getBlockPos());
                if (be != null) {
                    CompoundTag tileData = be.getPersistentData();
                    readFromPacketNBT(tileData);
                }
            }
        }
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);

        if (cmp.hasUUID(TAG_PLAYER_UUID)) {
            this.playerUUID = cmp.getUUID(TAG_PLAYER_UUID);
        }

        if (cmp.contains(TAG_MANA_PER_EMC)) {
            this.manaPerEMC = cmp.getInt(TAG_MANA_PER_EMC);
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        if (playerUUID != null && isValidUUID(playerUUID)) {
            cmp.putUUID(TAG_PLAYER_UUID, playerUUID);
        }
        else if (player != null) {
            cmp.putUUID(TAG_PLAYER_UUID, player.getUUID());
            this.playerUUID = player.getUUID();
        }

        cmp.putInt(TAG_MANA_PER_EMC, manaPerEMC);
    }

    @Override
    public int getMaxMana() {
        return ConfigHandler.FlowerConfig.getEMCFlowerMaxMana();
    }

    @Override
    public int getColor() {
        return 0xFFFF00;
    }

    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player playerEntity) {
            this.player = playerEntity;
            this.playerUUID = playerEntity.getUUID();

            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                CompoundTag tileData = be.getPersistentData();
                writeToPacketNBT(tileData);
                setChanged();
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(this)).cast());
    }

    private boolean isValidUUID(UUID uuid) {
        return uuid.getLeastSignificantBits() != 0L || uuid.getMostSignificantBits() != 0L;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 5);
    }
}