package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class LycorisradiataTile extends TileEntityGeneratingFlower {
    private static final int MODE1_MAX_MANA = 10000;
    private static final int MODE2_MAX_MANA = 50000;
    private static final int MODE2_STARTUP_MANA = 10000;
    private static final String TAG_SPAWN_WITHER_PARTICLES = "spawnWitherParticles";
    private boolean shouldSpawnWitherParticles = false;
    private boolean mode2Active = false;

    public LycorisradiataTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level.isClientSide) {
            double particleChance = 1F - (double) getMana() / (double) getMaxMana() / 3.5F;
            if (Math.random() > particleChance) {
                spawnManaParticles();
            }
        }

        else {
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
                    spawnWitherDeathParticles();
                    sync();
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
        boolean shouldSpawnParticles = false;

        if (stack.is(Item.byBlock(Blocks.WITHER_ROSE))) {
            manaToAdd = 200;
            manaPerTick = 200;
            shouldSpawnParticles = true;

        } else if (stack.is(Items.WITHER_SKELETON_SKULL)) {
            manaToAdd = 5000;
            manaPerTick = 1;
            shouldSpawnParticles = true;

        } else if (stack.is(Items.NETHER_STAR)) {
            manaToAdd = 20000;
            manaPerTick = 5;
            shouldSpawnParticles = true;

        }

        if (manaToAdd > 0) {
            entity.getItem().shrink(1);
            entity.setItem(entity.getItem()); // 更新物品实体

            int remainingMana = manaToAdd;
            while (remainingMana > 0 && getMana() < MODE1_MAX_MANA) {
                addMana(Math.min(manaPerTick, remainingMana));
                remainingMana -= manaPerTick;
            }

            if (shouldSpawnParticles) {
                sync();
            }
        }
    }

    private void spawnManaParticles() {
        Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
        double x = getBlockPos().getX() + offset.x;
        double y = getBlockPos().getY() + offset.y;
        double z = getBlockPos().getZ() + offset.z;

        float red = 0.9F;
        float green = 0.0F;
        float blue = 0.0F;

        for (int i = 0; i < 3; i++) {

            double angle = Math.random() * Math.PI * 2;
            double radius = 0.3 + Math.random() * 0.3;

            double px = x + 0.5 + Math.sin(angle) * radius;
            double py = y + 0.3 + Math.random() * 0.7;
            double pz = z + 0.5 + Math.cos(angle) * radius;

            float scale = 0.8F + (float) (Math.random() * 0.4F);

            int lifetime = 15 + level.random.nextInt(10);

            BotaniaAPI.instance().sparkleFX(level, px, py, pz, red, green, blue, scale, lifetime);
        }
    }

    private void spawnWitherDeathParticles() {
        if (!level.isClientSide) {
            shouldSpawnWitherParticles = true;
            sync();
            return;
        }

        Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
        double x = getBlockPos().getX() + offset.x;
        double y = getBlockPos().getY() + offset.y;
        double z = getBlockPos().getZ() + offset.z;

        // 创建更密集的粒子云
        for (int i = 0; i < 100; i++) {
            // 使用球形分布
            double phi = Math.random() * Math.PI * 2;
            double theta = Math.random() * Math.PI;
            double radius = 2.5 + Math.random() * 2.5;

            double px = x + 0.5 + Math.sin(theta) * Math.cos(phi) * radius;
            double py = y + 0.5 + Math.sin(theta) * Math.sin(phi) * radius;
            double pz = z + 0.5 + Math.cos(theta) * radius;

            // 随机化粒子大小和颜色
            float scale = 1.0F + (float) (Math.random() * 1.0F);
            float red = 0.9F + (float) (Math.random() * 0.1F);
            float green = 0.0F;
            float blue = 0.0F + (float) (Math.random() * 0.1F);

            // 增加粒子存在时间
            int lifetime = 25 + level.random.nextInt(15);

            BotaniaAPI.instance().sparkleFX(level, px, py, pz, red, green, blue, scale, lifetime);
        }

        // 添加一些额外的爆发效果
        for (int i = 0; i < 20; i++) {
            double angle = i * (Math.PI * 2 / 20);
            for (int j = 0; j < 3; j++) {
                double distance = 1.0 + j * 0.8;
                double px = x + 0.5 + Math.sin(angle) * distance;
                double py = y + 0.5 + (Math.random() - 0.5) * 2;
                double pz = z + 0.5 + Math.cos(angle) * distance;

                float scale = 1.5F + (float) (Math.random() * 0.5F);
                int lifetime = 30 + level.random.nextInt(10);

                BotaniaAPI.instance().sparkleFX(level, px, py, pz, 1.0F, 0.0F, 0.0F, scale, lifetime);
            }
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putBoolean(TAG_SPAWN_WITHER_PARTICLES, shouldSpawnWitherParticles);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        if (cmp.contains(TAG_SPAWN_WITHER_PARTICLES)) {
            shouldSpawnWitherParticles = cmp.getBoolean(TAG_SPAWN_WITHER_PARTICLES);
            if (shouldSpawnWitherParticles && level.isClientSide) {
                spawnWitherDeathParticles();
                shouldSpawnWitherParticles = false;
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return mode2Active ? MODE2_MAX_MANA : MODE1_MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0xFF0000;
    }

    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 5);
    }
}