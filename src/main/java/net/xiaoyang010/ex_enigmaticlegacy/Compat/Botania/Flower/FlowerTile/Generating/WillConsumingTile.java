package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.lib.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 意噬草 (Gol Hah Dov) - 专门消耗盖亚意志的产魔花
 * 消耗各种意志物品，产出魔力和随机唱片
 */
public class WillConsumingTile extends TileEntityGeneratingFlower {

    // 基础配置
    private static final int MAX_MANA = 2400; // 单个意志的魔力产出
    private static final int MANA_PER_WILL = 2400;
    private static final int PICKUP_RANGE = 3;
    private static final int WORK_INTERVAL = 20; // 1秒执行一次
    private static final double RECORD_DROP_CHANCE = 0.15; // 15%概率掉落唱片
    private static final int RANGE = 10;

    // NBT标签
    private static final String TAG_WORK_TIMER = "workTimer";
    private static final String TAG_LAST_CONSUME_TIME = "lastConsumeTime";

    private int workTimer = 0;
    private int lastConsumeTime = 0;

    // 可消耗的意志物品列表 (根据你的mod调整)
    private static final List<String> CONSUMABLE_WILLS = List.of(
            "botania:will_spirit",      // 意志：灵魂
            "botania:will_force",       // 意志：力量
            "botania:will_hope",        // 意志：希望
            "botania:will_mind",        // 意志：心智
            "botania:will_rage",        // 意志：愤怒
            "botania:will_soul"         // 意志：魂魄
            // 可以添加更多意志类型
    );

    // 可掉落的唱片列表
    private static final List<ItemStack> RECORD_DROPS = List.of(
            new ItemStack(Items.MUSIC_DISC_13),
            new ItemStack(Items.MUSIC_DISC_CAT),
            new ItemStack(Items.MUSIC_DISC_BLOCKS),
            new ItemStack(Items.MUSIC_DISC_CHIRP),
            new ItemStack(Items.MUSIC_DISC_FAR),
            new ItemStack(Items.MUSIC_DISC_MALL),
            new ItemStack(Items.MUSIC_DISC_MELLOHI),
            new ItemStack(Items.MUSIC_DISC_STAL),
            new ItemStack(Items.MUSIC_DISC_STRAD),
            new ItemStack(Items.MUSIC_DISC_WARD),
            new ItemStack(Items.MUSIC_DISC_11),
            new ItemStack(Items.MUSIC_DISC_WAIT),
            new ItemStack(Items.MUSIC_DISC_PIGSTEP),
            new ItemStack(Items.MUSIC_DISC_OTHERSIDE)
    );

    public WillConsumingTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            workTimer++;
            if (workTimer >= WORK_INTERVAL) {
                workTimer = 0;
                tryConsumeWills();
            }
        }
    }

    /**
     * 尝试消耗附近的意志物品
     */
    private void tryConsumeWills() {
        if (getMana() >= getMaxMana()) {
            return; // 魔力已满，停止工作
        }

        // 搜索附近的物品实体
        AABB searchBox = new AABB(getBlockPos()).inflate(PICKUP_RANGE);
        List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, searchBox);

        for (ItemEntity itemEntity : items) {
            if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty()) {
                continue;
            }

            ItemStack stack = itemEntity.getItem();
            if (isConsumableWill(stack)) {
                consumeWill(itemEntity, stack);
                break; // 每次只消耗一个
            }
        }
    }

    /**
     * 检查物品是否为可消耗的意志
     */
    private boolean isConsumableWill(ItemStack stack) {
        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null) return false;

        String itemName = itemId.toString();
        return CONSUMABLE_WILLS.contains(itemName) ||
                itemName.contains("will");

    }

    /**
     * 消耗意志物品
     */
    private void consumeWill(ItemEntity itemEntity, ItemStack stack) {
        // 产出魔力
        addMana(MANA_PER_WILL);

        // 消耗物品
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        // 记录消耗时间
        lastConsumeTime = Math.toIntExact(getLevel().getGameTime());

        // 播放音效
        getLevel().playSound(null, getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS, 0.5f, 1.0f);

        // 生成粒子效果
        spawnConsumeEffects();

        // 随机掉落唱片
        tryDropRecord();

        sync();
    }

    /**
     * 尝试掉落随机唱片
     */
    private void tryDropRecord() {
        Random random = getLevel().getRandom();
        if (random.nextDouble() < RECORD_DROP_CHANCE) {
            ItemStack recordDrop = RECORD_DROPS.get(random.nextInt(RECORD_DROPS.size())).copy();

            // 在花朵位置掉落唱片
            Vec3 offset = getLevel().getBlockState(getBlockPos()).getOffset(getLevel(), getBlockPos());
            double x = getBlockPos().getX() + offset.x + 0.5;
            double y = getBlockPos().getY() + offset.y + 1.0;
            double z = getBlockPos().getZ() + offset.z + 0.5;

            ItemEntity recordEntity = new ItemEntity(getLevel(), x, y, z, recordDrop);
            recordEntity.setPickUpDelay(10);
            getLevel().addFreshEntity(recordEntity);

            // 播放特殊音效
            getLevel().playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.BLOCKS, 0.3f, 1.5f);
        }
    }

    /**
     * 生成消耗时的粒子效果
     */
    private void spawnConsumeEffects() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            Vec3 offset = getLevel().getBlockState(getBlockPos()).getOffset(getLevel(), getBlockPos());
            double x = getBlockPos().getX() + offset.x + 0.5;
            double y = getBlockPos().getY() + offset.y + 0.7;
            double z = getBlockPos().getZ() + offset.z + 0.5;

            // 深紫色粒子效果 (意志被吞噬的感觉)
            for (int i = 0; i < 15; i++) {
                double offsetX = (getLevel().getRandom().nextDouble() - 0.5) * 0.8;
                double offsetY = getLevel().getRandom().nextDouble() * 0.5;
                double offsetZ = (getLevel().getRandom().nextDouble() - 0.5) * 0.8;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0.1, 0, 0.05);
            }

            // 额外的魔力粒子
            for (int i = 0; i < 8; i++) {
                double offsetX = (getLevel().getRandom().nextDouble() - 0.5) * 0.6;
                double offsetY = getLevel().getRandom().nextDouble() * 0.3;
                double offsetZ = (getLevel().getRandom().nextDouble() - 0.5) * 0.6;

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0.1, 0, 0.02);
            }
        }
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        // 深紫色，代表意志的神秘力量
        return 0x4B0082; // 靛蓝色
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        workTimer = cmp.getInt(TAG_WORK_TIMER);
        lastConsumeTime = cmp.getInt(TAG_LAST_CONSUME_TIME);
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_WORK_TIMER, workTimer);
        cmp.putInt(TAG_LAST_CONSUME_TIME, lastConsumeTime);
    }

    /**
     * 获取上次消耗时间 (用于调试或显示)
     */
    public int getLastConsumeTime() {
        return lastConsumeTime;
    }

    /**
     * 检查花朵是否在工作
     */
    public boolean isWorking() {
        return getLevel().getGameTime() - (long)lastConsumeTime < 100; // 5秒内消耗过就算在工作
    }
}