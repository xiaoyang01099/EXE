package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEnchantments;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.api.subtile.RadiusDescriptor;

import java.util.List;

public class MingXianLanTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 5;
    private static final int MAX_MANA = 10000;
    private static final int WITHER_CONVERSION_MANA = 500;
    private static final int WITHER_SKELETON_MANA = 2000;

    private boolean isBlossoming = false;

    public MingXianLanTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            // 检查具有 wither 效果的实体
            AABB bounds = new AABB(getEffectivePos().offset(-RANGE, -RANGE, -RANGE),
                    getEffectivePos().offset(RANGE + 1, RANGE + 1, RANGE + 1));
            List<LivingEntity> entities = getLevel().getEntitiesOfClass(LivingEntity.class, bounds);

            for (LivingEntity entity : entities) {
                // 处理凋灵效果移除和魔力生成
                if (entity.hasEffect(MobEffects.WITHER)) {
                    int amplifier = entity.getEffect(MobEffects.WITHER).getAmplifier();
                    entity.removeEffect(MobEffects.WITHER);
                    addMana((amplifier + 1) * WITHER_CONVERSION_MANA);
                }

                // 处理 Wither转化骷髅
                if (entity instanceof WitherSkeleton && getMana() < getMaxMana()) {
                    convertWitherSkeleton((WitherSkeleton) entity);
                }
            }

            // 处理开花状态
            if (getMana() >= getMaxMana()) {
                isBlossoming = true;
                // 检查贴图转换
                List<ItemStack> items = getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, bounds)
                        .stream()
                        .map(net.minecraft.world.entity.item.ItemEntity::getItem)
                        .toList();

                for (ItemStack stack : items) {
                    if (stack.getItem() == Items.BOOK && isBlossoming && getMana() >= getMaxMana()) {
                        convertBook(stack);
                    }
                }
            }
        }
    }

    private void convertWitherSkeleton(WitherSkeleton witherSkeleton) {
        Skeleton skeleton = EntityType.SKELETON.create(getLevel());
        if (skeleton != null) {
            skeleton.moveTo(witherSkeleton.getX(), witherSkeleton.getY(), witherSkeleton.getZ());
            skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                    new ItemStack(Items.BOW));
            getLevel().addFreshEntity(skeleton);
            witherSkeleton.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            addMana(WITHER_SKELETON_MANA);
        }
    }

    private void convertBook(ItemStack stack) {
        if (getMana() >= getMaxMana()) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            ModEnchantments.applyWitherEnchantment(enchantedBook);
            stack.shrink(1);
            getLevel().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    getLevel(),
                    getEffectivePos().getX() + 0.5,
                    getEffectivePos().getY() + 1.5,
                    getEffectivePos().getZ() + 0.5,
                    enchantedBook
            ));
            addMana(-getMaxMana());
            isBlossoming = false;
        }
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x4B0082;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    public boolean isBlossoming() {
        return isBlossoming;
    }
}