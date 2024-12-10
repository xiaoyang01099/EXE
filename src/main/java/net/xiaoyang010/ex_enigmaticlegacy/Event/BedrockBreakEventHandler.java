package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.BedrockBreaker;

@Mod.EventBusSubscriber
public class BedrockBreakEventHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        BlockState state = event.getWorld().getBlockState(pos);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof BedrockBreaker && state.is(Blocks.BEDROCK)) {

            event.getWorld().destroyBlock(pos, false);

            if (!event.getWorld().isClientSide && event.getWorld() instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) event.getWorld();
                ItemStack bedrockStack = new ItemStack(Blocks.BEDROCK);
                ItemEntity bedrockItemEntity = new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bedrockStack);
                serverWorld.addFreshEntity(bedrockItemEntity);
            }

            event.setCanceled(true);
        }
    }
}
