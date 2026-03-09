package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpectriteCrystalEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import java.util.List;


public class SpectriteCrystal extends Item {

    public SpectriteCrystal() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (!blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        } else {
            BlockPos above = clickedPos.above();
            if (!level.isEmptyBlock(above)) {
                return InteractionResult.FAIL;
            } else {
                double x = above.getX();
                double y = above.getY();
                double z = above.getZ();
                List<Entity> entities = level.getEntities(null, new AABB(x, y, z, x + 1.0, y + 2.0, z + 1.0));
                if (!entities.isEmpty()) {
                    return InteractionResult.FAIL;
                } else {
                    if (level instanceof ServerLevel) {
                        SpectriteCrystalEntity endCrystal = new SpectriteCrystalEntity(level, x + 0.5, y, z + 0.5);
                        endCrystal.setShowBottom(false);
                        level.addFreshEntity(endCrystal);
                        level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, above);
//                        EndDragonFight dragonFight = ((ServerLevel)level).dragonFight();
//                        if (dragonFight != null) {
//                            dragonFight.tryRespawn();
//                        }
                    }

                    pContext.getItemInHand().shrink(1);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    public InteractionResultHolder<ItemStack> use000(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitResult = player.pick(5.0D, 0.0F, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            if (!world.isClientSide) {
                SpectriteCrystalEntity crystal = new SpectriteCrystalEntity(ModEntities.SPECTRITE_CRYSTAL.get(), world);
                crystal.moveTo(blockHitResult.getLocation().x, blockHitResult.getLocation().y + 1, blockHitResult.getLocation().z);
                world.addFreshEntity(crystal);
                world.playSound(null, blockHitResult.getBlockPos(), SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
            }

            return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
        } else {
            return InteractionResultHolder.pass(itemstack);
        }
    }
}