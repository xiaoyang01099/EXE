package net.xiaoyang010.ex_enigmaticlegacy.Enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEnchantments;

@Mod.EventBusSubscriber
public class LavaWalkerEnchantment extends Enchantment {

    public LavaWalkerEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int level) {
        return level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            int level = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(ModEnchantments.LAVA_WALKER.get(), event.player);

            if (level > 0) {
                onEntityMoved(event.player, event.player.level, event.player.blockPosition(), level);
            }
        }
    }

    public static void onEntityMoved(LivingEntity pLiving, Level pLevel, BlockPos pPos, int pLevelEnchantment) {
        if (pLiving.isOnGround()) {
            BlockState obsidianState = Blocks.OBSIDIAN.defaultBlockState();
            float radius = (float)Math.min(16, 2 + pLevelEnchantment);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for(BlockPos blockpos : BlockPos.betweenClosed(
                    pPos.offset(-radius, -1.0, -radius),
                    pPos.offset(radius, -1.0, radius))) {

                if (blockpos.closerToCenterThan(pLiving.position(), radius)) {
                    mutablePos.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    BlockState aboveState = pLevel.getBlockState(mutablePos);

                    if (aboveState.isAir()) {
                        BlockState currentState = pLevel.getBlockState(blockpos);
                        boolean isFullLava = currentState.getBlock() == Blocks.LAVA &&
                                (!currentState.hasProperty(LiquidBlock.LEVEL) || currentState.getValue(LiquidBlock.LEVEL) == 0);

                        if (currentState.getMaterial() == Material.LAVA && isFullLava &&
                                obsidianState.canSurvive(pLevel, blockpos) &&
                                pLevel.isUnobstructed(obsidianState, blockpos, CollisionContext.empty()) &&
                                !ForgeEventFactory.onBlockPlace(pLiving, BlockSnapshot.create(pLevel.dimension(), pLevel, blockpos), Direction.UP)) {

                            pLevel.setBlockAndUpdate(blockpos, obsidianState);
                            pLevel.scheduleTick(blockpos, Blocks.OBSIDIAN, Mth.nextInt(pLiving.getRandom(), 60, 120));
                        }
                    }
                }
            }
        }
    }
}