package net.xiaoyang010.ex_enigmaticlegacy.World;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class BuildPortalProcedure {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != event.getPlayer().getUsedItemHand())
			return;
		execute(event, event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(),
				event.getWorld().getBlockState(event.getPos()), event.getPlayer());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
		execute(null, world, x, y, z, blockstate, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, BlockState blockstate, Entity entity) {
		if (entity == null)
			return;
		boolean ok = false;
		double azz = 0;
		double xa = 0;
		double ya = 0;
		double za = 0;
		if (ModItems.IGNITER.get() == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
				.getItem()) {
			if (blockstate.getBlock() == Blocks.QUARTZ_BLOCK) {
				if ((world.getBlockState(new BlockPos(x, y + 3, z))).getBlock() == Blocks.QUARTZ_BLOCK) {
					world.setBlock(new BlockPos(x, y + 1, z), ModBlockss.MINERS_HEAVEN_PORTAL.get().defaultBlockState(), 3);
					world.setBlock(new BlockPos(x, y + 2, z), ModBlockss.MINERS_HEAVEN_PORTAL.get().defaultBlockState(), 3);
				}
			}
		}
	}
}
