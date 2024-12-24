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

        // 添加消耗物品槽位
        this.addSlot(new Slot(itemHandler, 0, 80, 35));

        // 添加玩家背包槽位
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18,    // X坐标保持不变
                        166 + row * 18   // Y坐标从84改为166，将背包往下移
                ));
            }
        }

        // 添加玩家快捷栏槽位
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col,
                    8 + col * 18,     // X坐标保持不变
                    224              // Y坐标从142改为224，将快捷栏往下移
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // 处理维度传送逻辑
    public void teleportToDimension(ResourceKey<Level> dimension) {
        if (!player.level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel targetLevel = serverPlayer.getServer().getLevel(dimension);

            // 检查消耗品是否足够
            if (hasRequiredItems(dimension)) {
                // 消耗物品
                consumeRequiredItems(dimension);

                // 执行传送
                if (targetLevel != null) {
                    BlockPos targetPos = findSafeSpawnLocation(targetLevel, serverPlayer);
                    serverPlayer.teleportTo(targetLevel,
                            targetPos.getX() + 0.5D,
                            targetPos.getY() + 0.5D,
                            targetPos.getZ() + 0.5D,
                            serverPlayer.getYRot(),
                            serverPlayer.getXRot());

                    // 播放效果
                    playTeleportEffects(serverPlayer);
                }
            }
        }
    }

    public boolean hasRequiredItems(ResourceKey<Level> dimension) {
        if (dimension == Level.NETHER) {
            return player.getInventory().countItem(Items.DIAMOND) >= 4;
        } else if (dimension == Level.END) {
            return player.getInventory().countItem(Items.ENDER_PEARL) >= 8;
        }
        return true;
    }

    private void consumeRequiredItems(ResourceKey<Level> dimension) {
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
            // 末地维度的生成点通常在这些坐标
            pos = new BlockPos(100, 50, 0);  // 默认的末地生成坐标

            // 或者你也可以使用世界生成点
            // pos = targetLevel.getSharedSpawnPos();

            // 在这个位置找到最高的方块
            for (int y = 50; y < 80; y++) {
                BlockPos checkPos = new BlockPos(100, y, 0);
                if (targetLevel.getBlockState(checkPos).isAir() &&
                        targetLevel.getBlockState(checkPos.above()).isAir()) {
                    pos = checkPos;
                    break;
                }
            }
        } else {
            pos = targetLevel.getSharedSpawnPos();
        }

        return pos;
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