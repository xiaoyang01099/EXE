package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ExtremeDisassemblyMenu;

public class ExtremeCraftingDisassembly extends Block {
    public ExtremeCraftingDisassembly() {
        super(Properties.of(Material.METAL)
                .strength(5.0F, 6000.0F)
                .sound(SoundType.GLASS)
                .requiresCorrectToolForDrops());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            if (player instanceof ServerPlayer) {
                NetworkHooks.openGui((ServerPlayer) player,
                        new SimpleMenuProvider(
                                (windowId, playerInventory, playerEntity) ->
                                        new ExtremeDisassemblyMenu(windowId, playerInventory, ContainerLevelAccess.create(level, pos)),
                                new TranslatableComponent("")
                        ),
                        pos);
            }
            return InteractionResult.CONSUME;
        }
    }
}