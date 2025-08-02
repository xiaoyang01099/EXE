package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.block.tile.TileMod;

import java.util.List;

public class ManaCrystalCubeBlockTile extends TileMod implements IWandable, IWandHUD{
    private int knownMana = -1;
    private int knownMaxMana = -1;
    private int ticks = 0;

    public ManaCrystalCubeBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos(), getBlockPos().offset(1, 2, 1));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ManaCrystalCubeBlockTile blockEntity) {
        if (level.isClientSide) {
            return;
        }

        blockEntity.ticks++;
        if (blockEntity.ticks % 20 == 0) {
            blockEntity.findMana();
        }
    }

    private void findMana() {
        int[] mana = getManaAround();
        if (mana[0] != knownMana || mana[1] != knownMaxMana) {
            knownMana = mana[0];
            knownMaxMana = mana[1];
            setChanged();
        }
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public void renderHUD(PoseStack ms, Minecraft mc) {
        Component name = new TranslatableComponent("block.ex_enigmaticlegacy.mana_crystal_cube");
        int color = 0x30D5C8;

        Screen currentScreen = mc.screen;
        if (currentScreen == null) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            ClientHelper.drawSimpleManaHUD(ms, color, knownMana, knownMaxMana,
                    name.getString(), screenWidth, screenHeight);
        }
    }

    public int[] getManaAround() {
        int[] mana = new int[]{0, 0};

        List<IManaSpark> sparks = SparkHelper.getSparksAround(level, worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);

        for(IManaSpark spark : sparks) {
            ISparkAttachable attachable = spark.getAttachedTile();
            if (attachable != null) {
                if (attachable instanceof IManaReceiver) {
                    IManaReceiver receiver = (IManaReceiver) attachable;
                    mana[1] += receiver.getCurrentMana() + attachable.getAvailableSpaceForMana();
                    mana[0] += receiver.getCurrentMana();
                } else {
                    mana[1] += attachable.getAvailableSpaceForMana();
                }
            }
        }

        return mana;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeCustomNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readCustomNBT(tag);
    }

    public void writeCustomNBT(CompoundTag tag) {
        tag.putInt("knownMana", knownMana);
        tag.putInt("knownMaxMana", knownMaxMana);
    }

    public void readCustomNBT(CompoundTag tag) {
        if (tag.contains("knownMana")) {
            knownMana = tag.getInt("knownMana");
        }

        if (tag.contains("knownMaxMana")) {
            knownMaxMana = tag.getInt("knownMaxMana");
        }
    }
}