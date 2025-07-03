package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.block.tile.TileMod;
import net.minecraft.ChatFormatting;

import java.util.List;

public class TileGameBoard extends TileMod implements IWandHUD {
    public String[] playersName = new String[]{"", ""};
    public byte[] slotChance = new byte[]{0, 0, 0, 0};
    protected int botTick = -1;
    public int endGameTick = -1;
    protected boolean requestUpdate;
    public boolean isSingleGame = true;
    public boolean isCustomGame = false;
    public int[] clientTick = new int[]{0, 0, 0, 0};
    public static final ItemStack headRender;
    private ItemStack customStack = ItemStack.EMPTY;

    public TileGameBoard(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos worldPosition, BlockState state, TileGameBoard self) {
        if (self.botTick > 0) {
            --self.botTick;
        }

        if (self.endGameTick > 0) {
            --self.endGameTick;
        }

        self.updateServer();
    }

    public static void clientTick(Level level, BlockPos worldPosition, BlockState state, TileGameBoard self) {
        self.updateAnimationTicks();
    }

    public void updateAnimationTicks() {
        for(int i = 0; i < this.slotChance.length; ++i) {
            if (this.slotChance[i] > 0) {
                this.clientTick[i]++;
            } else {
                this.clientTick[i] = 0;
            }
        }
    }

    protected void updateServer() {
        if (this.hasGame() && this.endGameTick == 0 && !this.isCustomGame) {
            this.finishGame();
        }

        if (this.requestUpdate) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

        if (this.level.getGameTime() % 20L == 0L && this.hasFullDice() && this.endGameTick == -1) {
            this.endGameTick = 28;
        }

        boolean hasUpdate = false;
        if (this.isSingleGame) {
            if (this.botTick == 0 && this.hasGame()) {
                for(int i = 2; i < 4; ++i) {
                    if (this.slotChance[i] == 0) {
                        this.slotChance[i] = (byte)(this.level.random.nextInt(6) + 1);
                        this.botTick = -1;
                        hasUpdate = true;
                        this.level.playSound(null, this.worldPosition, getSoundEvent("ex_enigmaticlegacy:board_cube"),
                                SoundSource.BLOCKS, 0.6F, 1.0F);
                        break;
                    }
                }
            }
        } else if (this.botTick == 0 && this.hasGame()) {
            this.endGameTick = 0;
        }

        this.requestUpdate = hasUpdate;
    }

    private SoundEvent getSoundEvent(String soundName) {
        return ModSounds.BOARD_CUBE;
    }

    public void setPlayer(String name, boolean isCustomGame) {
        this.isCustomGame = isCustomGame;
        if (this.isSingleGame) {
            this.playersName[0] = name;
            this.playersName[1] = "";
            this.requestUpdate = true;
            this.botTick = 8;
        } else {
            if (this.playersName[0].isEmpty()) {
                this.playersName[0] = name;
            } else if (!this.playersName[0].equals(name)) {
                this.playersName[1] = name;
            }
            this.requestUpdate = true;
        }
    }

    public void setPlayer(Player player) {
        this.setPlayer(player.getName().getString(), false);
    }

    public boolean dropDice(String name) {
        if (this.isSingleGame) {
            if (name.equals(this.playersName[0]) && this.botTick == -1) {
                boolean hasDrop = false;

                for(int i = 0; i < 2; ++i) {
                    if (this.slotChance[i] == 0) {
                        hasDrop = true;
                        if (!this.level.isClientSide) {
                            this.slotChance[i] = (byte)(this.level.random.nextInt(6) + 1);
                            this.botTick = 18;
                            this.level.playSound(null, this.worldPosition, getSoundEvent("ex_enigmaticlegacy:board_cube"),
                                    SoundSource.BLOCKS, 0.6F, 1.0F);
                            this.requestUpdate = true;
                        }
                        break;
                    }
                }
                return hasDrop;
            }
        } else {
            for(int i = 0; i < this.playersName.length; ++i) {
                if (name.equals(this.playersName[i])) {
                    boolean hasDrop = false;

                    for(int j = i * 2; j < (i + 1) * 2; ++j) {
                        if (this.slotChance[j] == 0) {
                            hasDrop = true;
                            if (!this.level.isClientSide) {
                                this.slotChance[j] = (byte)(this.level.random.nextInt(6) + 1);
                                this.botTick = this.playersName[1].isEmpty() ? 240 : 1200;
                                this.level.playSound(null, this.worldPosition, getSoundEvent("ex_enigmaticlegacy:board_cube"),
                                        SoundSource.BLOCKS, 0.6F, 1.0F);
                                this.requestUpdate = true;
                            }
                            break;
                        }
                    }
                    return hasDrop;
                }
            }
        }
        return false;
    }

    public boolean dropDice(Player player) {
        return this.dropDice(player.getName().getString());
    }

    public boolean hasFullDice() {
        boolean hasFull = false;
        for(int i = 0; i < this.slotChance.length; ++i) {
            if (this.slotChance[i] <= 0) {
                return false;
            }
            hasFull = this.slotChance[i] > 0;
        }
        return hasFull;
    }

    public boolean hasGame() {
        if (this.isSingleGame) {
            return !this.playersName[0].isEmpty();
        } else {
            for(int i = 0; i < this.playersName.length; ++i) {
                if (!this.playersName[i].isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void finishGame(boolean hasChatMessage) {
        if (this.level != null && !this.level.isClientSide) {
            if (!hasChatMessage) {
                this.resetGame();
            } else if (!this.hasFullDice()) {
                this.sendNearMessage("ex_enigmaticlegacy.gameBoard.misc.notPlayer");
                this.resetGame();
            } else {
                String str = this.isSingleGame ? "" : ".mult";
                if (this.slotChance[0] + this.slotChance[1] > this.slotChance[2] + this.slotChance[3]) {
                    this.sendNearMessage("ex_enigmaticlegacy.gameBoard.misc.0" + str, this.playersName[0]);
                } else if (this.slotChance[0] + this.slotChance[1] == this.slotChance[2] + this.slotChance[3]) {
                    this.sendNearMessage("ex_enigmaticlegacy.gameBoard.misc.1" + str);
                } else {
                    this.sendNearMessage("ex_enigmaticlegacy.gameBoard.misc.2" + str,
                            this.playersName[this.isSingleGame ? 0 : 1]);
                }
                this.resetGame();
            }
        }
    }

    public void finishGame() {
        this.finishGame(true);
    }

    public void sendNearMessage(String text, Object... args) {
        AABB aabb = new AABB(this.worldPosition).inflate(3.5);
        List<Player> players = this.level.getEntitiesOfClass(Player.class, aabb);

        for(Player player : players) {
            if (player != null) {
                MutableComponent message = EComponent.translatable(text, args);
                message.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
                player.displayClientMessage(message,false);
            }
        }
    }

    public boolean changeCustomStack(ItemStack stack) {
        if (!this.isCustomGame) {
            return false;
        } else if (!this.customStack.isEmpty() && ItemStack.isSameItemSameTags(this.customStack, stack)) {
            return false;
        } else {
            this.customStack = stack.copy();
            this.requestUpdate = true;
            return true;
        }
    }

    public void resetGame() {
        this.playersName[0] = "";
        this.playersName[1] = "";

        for(int i = 0; i < this.slotChance.length; ++i) {
            this.slotChance[i] = 0;
        }

        this.botTick = -1;
        this.endGameTick = -1;
        this.isCustomGame = false;
        this.customStack = ItemStack.EMPTY;
        this.requestUpdate = true;
    }

    @Override
    public void renderHUD(PoseStack ms, Minecraft mc) {
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 7;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 12;

        ItemRenderer itemRenderer = mc.getItemRenderer();
        ItemStack renderStack = this.isCustomGame ?
                (this.customStack.isEmpty() ? headRender : this.customStack) : headRender;

        itemRenderer.renderGuiItem(renderStack,
                x - (!this.isSingleGame ? 1 : 0),
                y - (!this.isSingleGame ? 1 : 0));

        if (!this.isSingleGame) {
            itemRenderer.renderGuiItem(headRender, x + 3, y + 3);
        }
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        super.writePacketNBT(cmp);

        for(int i = 0; i < this.playersName.length; ++i) {
            cmp.putString("playerName" + i, this.playersName[i]);
        }

        cmp.putByteArray("slotChance", this.slotChance);
        cmp.putInt("botTick", this.botTick);
        cmp.putInt("endGameTick", this.endGameTick);
        cmp.putBoolean("requestUpdate", this.requestUpdate);
        cmp.putBoolean("isSingleGame", this.isSingleGame);
        cmp.putBoolean("isCustomGame", this.isCustomGame);

        if (!this.customStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            this.customStack.save(stackTag);
            cmp.put("customStack", stackTag);
        }
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        super.readPacketNBT(cmp);

        for(int i = 0; i < this.playersName.length; ++i) {
            this.playersName[i] = cmp.getString("playerName" + i);
        }

        this.botTick = cmp.getInt("botTick");
        this.endGameTick = cmp.getInt("endGameTick");
        this.slotChance = cmp.getByteArray("slotChance");
        this.requestUpdate = cmp.getBoolean("requestUpdate");
        this.isSingleGame = cmp.getBoolean("isSingleGame");
        this.isCustomGame = cmp.getBoolean("isCustomGame");

        if (cmp.contains("customStack")) {
            this.customStack = ItemStack.of(cmp.getCompound("customStack"));
        } else {
            this.customStack = ItemStack.EMPTY;
        }
    }

    static {
        headRender = new ItemStack(Items.PLAYER_HEAD, 1);
    }
}