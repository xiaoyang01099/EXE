package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;

public class DimensionalMirrorContainer extends AbstractContainerMenu {
    private final Player player;
    private final Container itemHandler = new SimpleContainer(1);

    public DimensionalMirrorContainer(int containerId, Inventory inventory, Player player) {
        super(ModMenus.DIMENSIONAL_MIRROR, containerId);
        this.player = player;

        this.addSlot(new Slot(itemHandler, 0, 80, 35));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18,
                        166 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col,
                    8 + col * 18,
                    224
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void teleportToDimension(ResourceKey<Level> dimension) {
        if (!player.level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel targetLevel = serverPlayer.getServer().getLevel(dimension);

            if (hasRequiredItems(dimension)) {
                consumeRequiredItems(dimension);
                if (targetLevel != null) {
                    BlockPos targetPos = findSafeSpawnLocation(targetLevel, serverPlayer);
                    serverPlayer.teleportTo(targetLevel,
                            targetPos.getX() + 0.5D,
                            targetPos.getY() + 0.5D,
                            targetPos.getZ() + 0.5D,
                            serverPlayer.getYRot(),
                            serverPlayer.getXRot());

                    playTeleportEffects(serverPlayer);
                }
            }
        }
    }

    public boolean hasRequiredItems(ResourceKey<Level> dimension) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        if (dimension == Level.NETHER) {
            return player.getInventory().countItem(Items.DIAMOND) >= 4;
        } else if (dimension == Level.END) {
            return player.getInventory().countItem(Items.ENDER_PEARL) >= 8;
        } else if (dimension == Level.OVERWORLD) {
            return true;
        }
        return false;
    }

    private void consumeRequiredItems(ResourceKey<Level> dimension) {
        if (player.getAbilities().instabuild) {
            return;
        }

        if (dimension == Level.NETHER) {
            player.getInventory().clearOrCountMatchingItems(p -> p.is(Items.DIAMOND), 4, player.inventoryMenu.getCraftSlots());
        } else if (dimension == Level.END) {
            player.getInventory().clearOrCountMatchingItems(p -> p.is(Items.ENDER_PEARL), 8, player.inventoryMenu.getCraftSlots());
        }
    }

    private BlockPos findSafeSpawnLocation(ServerLevel targetLevel, ServerPlayer player) {
        BlockPos pos;
        if (targetLevel.dimension() == Level.NETHER) {
            pos = new BlockPos(player.getX() / 8.0D, 64, player.getZ() / 8.0D);
        } else if (targetLevel.dimension() == Level.END) {
            pos = new BlockPos(100, 50, 0);
        } else {
            pos = targetLevel.getSharedSpawnPos();
        }

        pos = ensureSafePlatform(targetLevel, pos);
        return pos;
    }

    private BlockPos ensureSafePlatform(ServerLevel level, BlockPos initialPos) {
        BlockPos checkPos = initialPos.below();
        if (level.getBlockState(checkPos).isAir()) {
            net.minecraft.world.level.block.Block platformBlock;
            if (level.dimension() == Level.NETHER) {
                platformBlock = net.minecraft.world.level.block.Blocks.NETHERRACK;
            } else if (level.dimension() == Level.END) {
                platformBlock = net.minecraft.world.level.block.Blocks.END_STONE;
            } else {
                platformBlock = net.minecraft.world.level.block.Blocks.STONE;
            }

            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos platformPos = checkPos.offset(x, 0, z);
                    level.setBlock(platformPos, platformBlock.defaultBlockState(), 3);
                }
            }
        }

        for (int y = 0; y <= 1; y++) {
            BlockPos headPos = initialPos.above(y);
            if (!level.getBlockState(headPos).isAir()) {
                level.setBlock(headPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }

        return initialPos;
    }

    private void playTeleportEffects(ServerPlayer player) {
        player.level.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        ((ServerLevel) player.level).sendParticles(ParticleTypes.PORTAL,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                50,
                0.5D,
                0.5D,
                0.5D,
                0.1D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}