package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileGameBoard;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class ArdentAzarcissusTile extends TileEntityGeneratingFlower {
    public static final String PLAYER_NAME = "ArdentAzarcissus#21sda2gaj91*21df#111sfq3jrns@#";

    private static final String TAG_COOLDOWN = "cooldown";
    private static final int WORK_MANA = 5000;
    private static final int COOLDOWN_TIME = 40;
    private static final int MAX_MANA = 16000;
    private static final int FLOWER_COLOR = 0xdf8c36; // 橙黄色

    private int cooldown = 0;

    public ArdentAzarcissusTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (getMana() >= getMaxMana()) {
            return;
        }

        boolean needSync = false;
        BlockPos pos = getBlockPos();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos checkPos = pos.offset(i, 0, j);
                BlockEntity blockEntity = level.getBlockEntity(checkPos);

                if (blockEntity instanceof TileGameBoard gameBoard) {
                    if (gameBoard.isSingleGame) {
                        if (!gameBoard.hasGame()) {

                            gameBoard.setPlayer(PLAYER_NAME, true);
                            cooldown = COOLDOWN_TIME;
                        } else {

                            if (!gameBoard.playersName[0].equals(PLAYER_NAME)) {
                                gameBoard.playersName[0] = PLAYER_NAME;
                            }

                            if (!gameBoard.isCustomGame) {
                                gameBoard.isCustomGame = true;
                            }

                            if (gameBoard.endGameTick == 0) {
                                int winCount = gameBoard.slotChance[0] + gameBoard.slotChance[1]
                                        - (gameBoard.slotChance[2] + gameBoard.slotChance[3]);

                                if (winCount > 0) {
                                    int manaGain = WORK_MANA * winCount;
                                    addMana(manaGain);
                                    needSync = true;
                                }

                                gameBoard.finishGame(false);
                            } else {
                                gameBoard.dropDice(PLAYER_NAME);
                            }

                            cooldown = COOLDOWN_TIME;
                        }

                        ItemStack flowerStack = getFlowerStack();
                        if (!flowerStack.isEmpty()) {
                            gameBoard.changeCustomStack(flowerStack);
                        }
                    }
                }
            }
        }

        if (needSync) {
            sync();
        }
    }

    private ItemStack getFlowerStack() {
        return new ItemStack(ModItems.ARDENT_AZARCISSUS.get());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getEffectivePos(), 1);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return FLOWER_COLOR;
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        cooldown = cmp.getInt(TAG_COOLDOWN);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_COOLDOWN, cooldown);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }
}