
package net.xiaoyang010.ex_enigmaticlegacy.Block.portal;


import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.World.teleporter.MinersHeavenOldPortalShape;
import net.xiaoyang010.ex_enigmaticlegacy.World.teleporter.MinersHeavenOldTeleporter;

import java.util.Random;
import java.util.Optional;

public class MinersHeavenPortalBlock extends NetherPortalBlock {
	public MinersHeavenPortalBlock() {
		super(Properties.of(Material.PORTAL).noCollission().randomTicks().strength(-1.0F).sound(SoundType.GLASS).lightLevel(s -> 12)
				.noDrops());
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
	}

	public static void portalSpawn(Level world, BlockPos pos) {
		Optional<MinersHeavenOldPortalShape> optional = MinersHeavenOldPortalShape.findEmptyPortalShape(world, pos, Direction.Axis.X);
		if (optional.isPresent()) {
			optional.get().createPortalBlocks();
		}
	}

	@Override
	public BlockState updateShape(BlockState p_54928_, Direction p_54929_, BlockState p_54930_, LevelAccessor p_54931_, BlockPos p_54932_,
			BlockPos p_54933_) {
		Direction.Axis direction$axis = p_54929_.getAxis();
		Direction.Axis direction$axis1 = p_54928_.getValue(AXIS);
		boolean flag = direction$axis1 != direction$axis && direction$axis.isHorizontal();
		return !flag && !p_54930_.is(this) && !(new MinersHeavenOldPortalShape(p_54931_, p_54932_, direction$axis1)).isComplete()
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(p_54928_, p_54929_, p_54930_, p_54931_, p_54932_, p_54933_);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		for (int i = 0; i < 4; i++) {
			double px = pos.getX() + random.nextFloat();
			double py = pos.getY() + random.nextFloat();
			double pz = pos.getZ() + random.nextFloat();
			double vx = (random.nextFloat() - 0.5) / 2.;
			double vy = (random.nextFloat() - 0.5) / 2.;
			double vz = (random.nextFloat() - 0.5) / 2.;
			int j = random.nextInt(4) - 1;
			if (world.getBlockState(pos.west()).getBlock() != this && world.getBlockState(pos.east()).getBlock() != this) {
				px = pos.getX() + 0.5 + 0.25 * j;
				vx = random.nextFloat() * 2 * j;
			} else {
				pz = pos.getZ() + 0.5 + 0.25 * j;
				vz = random.nextFloat() * 2 * j;
			}
			world.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
		}
		if (random.nextInt(110) == 0)
			world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(("block.portal.ambient"))), SoundSource.BLOCKS, 0.5f,
					random.nextFloat() * 0.4f + 0.8f);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions() && !entity.level.isClientSide() && true) {
			if (entity.isOnPortalCooldown()) {
				entity.setPortalCooldown();
			} else if (entity.level.dimension() != ResourceKey.create(Registry.DIMENSION_REGISTRY,
					new ResourceLocation("ex_enigmaticlegacy:miners_heaven_old"))) {
				entity.setPortalCooldown();
				teleportToDimension(entity, pos,
						ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ex_enigmaticlegacy:miners_heaven_old")));
			} else {
				entity.setPortalCooldown();
				teleportToDimension(entity, pos, Level.OVERWORLD);
			}
		}
	}

	private void teleportToDimension(Entity entity, BlockPos pos, ResourceKey<Level> destinationType) {
		entity.changeDimension(entity.getServer().getLevel(destinationType),
				new MinersHeavenOldTeleporter(entity.getServer().getLevel(destinationType), pos));
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderLayer() {
		ItemBlockRenderTypes.setRenderLayer(ModBlockss.MINERS_HEAVEN_PORTAL.get(),
				renderType -> renderType == RenderType.translucent());
	}
}
