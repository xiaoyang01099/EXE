
package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DeconTableMenu;

public class DeconTableBlock extends Block {
    public DeconTableBlock() {
        super(Properties.of(Material.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD));
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
                                        new DeconTableMenu(windowId, playerInventory, ContainerLevelAccess.create(level, pos)),
                                new TranslatableComponent("container.ex_enigmaticlegacy.decontable")
                        ),
                        pos);
            }
            return InteractionResult.CONSUME;
        }
    }
}