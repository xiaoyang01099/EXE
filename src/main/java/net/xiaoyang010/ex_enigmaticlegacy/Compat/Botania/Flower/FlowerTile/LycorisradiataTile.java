package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class LycorisradiataTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 4;
    private static final int MODE1_MAX_MANA = 10000;
    private static final int MODE2_MAX_MANA = 50000;
    private static final int MODE2_STARTUP_MANA = 10000;

    private boolean mode2Active = false;

    public LycorisradiataTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!level.isClientSide) {
            // 模式1: 吸收物品
            if (getMana() < MODE1_MAX_MANA) {
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                        new AABB(getEffectivePos(), getEffectivePos().offset(1, 1, 1)))) {
                    processItem(item);
                    break;
                }
            }

            // 模式2: 击杀凋灵
            if (getMana() >= MODE2_STARTUP_MANA) {
                mode2Active = true;
            }

            if (mode2Active) {
                AABB bossArea = new AABB(getEffectivePos().offset(-4, -4, -4),
                        getEffectivePos().offset(5, 5, 5));
                for (WitherBoss wither : level.getEntitiesOfClass(WitherBoss.class, bossArea)) {
                    wither.hurt(DamageSource.MAGIC, Float.MAX_VALUE);
                    addMana(MODE2_MAX_MANA - getMana()); // 填满魔力
                    break;
                }
            }
        }
    }

    private void processItem(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) return;

        int manaToAdd = 0;
        int manaPerTick = 0;

        if (stack.is(Item.byBlock(Blocks.WITHER_ROSE))) {
            manaToAdd = 200;
            manaPerTick = 200;
        } else if (stack.is(Items.WITHER_SKELETON_SKULL)) {
            manaToAdd = 5000;
            manaPerTick = 1;
        } else if (stack.is(Items.NETHER_STAR)) {
            manaToAdd = 20000;
            manaPerTick = 5;
        }

        if (manaToAdd > 0) {
            entity.getItem().shrink(1);
            entity.setItem(entity.getItem()); // 更新物品实体

            int remainingMana = manaToAdd;
            while (remainingMana > 0 && getMana() < MODE1_MAX_MANA) {
                addMana(Math.min(manaPerTick, remainingMana));
                remainingMana -= manaPerTick;
            }
        }
    }

    @Override
    public int getMaxMana() {
        return mode2Active ? MODE2_MAX_MANA : MODE1_MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x000000;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), mode2Active ? RANGE : 0);
    }
}