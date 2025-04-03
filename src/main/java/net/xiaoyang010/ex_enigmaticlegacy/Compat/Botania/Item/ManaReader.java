package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityGaiaSpreaderTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.block.tile.TileTerraPlate;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.block.tile.mana.TileSpreader;

public class ManaReader extends Item {

    public ManaReader(Properties rarity) {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                .stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        if (player == null) return InteractionResult.FAIL;
        if (te instanceof TilePool pool) {
            int mana = pool.getCurrentMana();
            if (!context.getLevel().isClientSide) {
                player.sendMessage(new TranslatableComponent(String.valueOf(mana)), player.getUUID());
            }
        } else if (te instanceof TileEntityGeneratingFlower gen) {
            int mana = gen.getMana();
            if (!context.getLevel().isClientSide) {
                player.sendMessage(new TranslatableComponent(mana + "/" + gen.getMaxMana()), player.getUUID());
            }
        } else if (te instanceof TileEntityFunctionalFlower func) {
            int mana = func.getMana();
            if (!context.getLevel().isClientSide) {
                player.sendMessage(new TranslatableComponent(mana + "/" + func.getMaxMana()), player.getUUID());
            }
        } else if (te instanceof TileSpreader spreader) {
            int mana = spreader.getCurrentMana();
            if (!context.getLevel().isClientSide) {
                player.sendMessage(new TranslatableComponent(mana + "/" + spreader.getMaxMana()), player.getUUID());
            }
        } else if (te instanceof InfinityGaiaSpreaderTile spreader) {
            int mana = spreader.getCurrentMana();
            if (!context.getLevel().isClientSide) {
                player.sendMessage(new TranslatableComponent(mana + "/" + spreader.getMaxMana()), player.getUUID());
            }
        } else if (te instanceof TileTerraPlate terra) {
            int mana = terra.getCurrentMana();

            if (!context.getLevel().isClientSide) {
                if (terra.canReceiveManaFromBursts()) {
                    float completion = terra.getCompletion();
                    int maxMana = (int) (mana / Math.max(completion, 0.00001f));
                    player.sendMessage(new TranslatableComponent(mana + "/" + maxMana + " (" + (int) (completion * 100) + "%)"), player.getUUID());
                } else {
                    player.sendMessage(new TranslatableComponent("No recipe found"), player.getUUID());
                }
            }
        }
        return InteractionResult.PASS;
    }
}