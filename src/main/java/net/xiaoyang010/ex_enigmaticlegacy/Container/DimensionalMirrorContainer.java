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
        // 创造模式不需要消耗物品
        if (player.getAbilities().instabuild) {
            return true;
        }

        if (dimension == Level.NETHER) {
            return player.getInventory().countItem(Items.DIAMOND) >= 4;
        } else if (dimension == Level.END) {
            return player.getInventory().countItem(Items.ENDER_PEARL) >= 8;
        } else if (dimension == Level.OVERWORLD) {
            return true; // 返回主世界不需要消耗物品
        }
        return false; // 对于其他维度，默认不允许传送
    }

    private void consumeRequiredItems(ResourceKey<Level> dimension) {
        // 创造模式不消耗物品
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

        // 确保位置是安全的，如果需要则生成平台
        pos = ensureSafePlatform(targetLevel, pos);
        return pos;
    }

    private BlockPos ensureSafePlatform(ServerLevel level, BlockPos initialPos) {
        // 检查脚下是否有方块
        BlockPos checkPos = initialPos.below();
        if (level.getBlockState(checkPos).isAir()) {
            // 获取对应维度的基础方块
            net.minecraft.world.level.block.Block platformBlock;
            if (level.dimension() == Level.NETHER) {
                platformBlock = net.minecraft.world.level.block.Blocks.NETHERRACK;
            } else if (level.dimension() == Level.END) {
                platformBlock = net.minecraft.world.level.block.Blocks.END_STONE;
            } else {
                platformBlock = net.minecraft.world.level.block.Blocks.STONE;
            }

            // 生成9x9平台
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos platformPos = checkPos.offset(x, 0, z);
                    level.setBlock(platformPos, platformBlock.defaultBlockState(), 3);
                }
            }
        }

        // 确保头部有足够空间
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