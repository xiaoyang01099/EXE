package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class DarkNightGrassTile extends TileEntityFunctionalFlower {
    private static final int RANGE = 10; // 10格范围，形成20x20区域
    private static final int MANA_COST = 100;
    private static final int SPAWN_INTERVAL = 10; // 1.5秒(30 ticks)生成一只怪
    private static final int MAX_MOBS = 50;
    private static final int EFFECT_DURATION = 2400; // 2分钟(2400 ticks)
    private static final int COOLDOWN_DURATION = 1200; // 1分钟冷却

    private int effectTimer = 0; // 效果持续时间计时器
    private int cooldownTimer = 0; // 冷却时间计时器
    private int spawnTimer = 0; // 生成怪物计时器
    private boolean isActive = false; // 是否处于激活状态
    private AABB activeRegion = null; // 活动区域

    public DarkNightGrassTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<DarkNightGrassTile> {
        public FunctionalWandHud(DarkNightGrassTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level.isClientSide) return;

        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        if (isActive && level instanceof ServerLevel) {
            generateParticles();
        }

        if (!isActive && getMana() >= MANA_COST) {
            startEffect();
        }

        if (isActive) {
            effectTimer--;
            spawnTimer--;

            generateEffects();

            if (effectTimer <= 0) {
                endEffect();
            }
        }
    }

    private void startEffect() {
        isActive = true;
        effectTimer = EFFECT_DURATION;
        BlockPos center = getEffectivePos();
        activeRegion = new AABB(
                center.offset(-RANGE, -RANGE, -RANGE),
                center.offset(RANGE, RANGE, RANGE)
        );
        addMana(-MANA_COST);
    }

    private void endEffect() {
        isActive = false;
        activeRegion = null;
        cooldownTimer = COOLDOWN_DURATION;
        spawnTimer = 0;
    }

    private void generateEffects() {
        if (!isActive || activeRegion == null) return;

        generateParticles();

        if (spawnTimer <= 0) {
            spawnTimer = SPAWN_INTERVAL;
            trySpawnMonster();
        }

        constrainMonstersToRegion();
    }

    private void generateParticles() {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos center = getEffectivePos();
            Random rand = level.random;

            for (int i = 0; i < 20; i++) {

                int face = rand.nextInt(6);
                double x, y, z;

                switch (face) {
                    case 0:
                        x = center.getX() - RANGE + rand.nextDouble() * RANGE * 2;
                        y = center.getY() - RANGE;
                        z = center.getZ() - RANGE + rand.nextDouble() * RANGE * 2;
                        break;
                    case 1:
                        x = center.getX() - RANGE + rand.nextDouble() * RANGE * 2;
                        y = center.getY() + RANGE;
                        z = center.getZ() - RANGE + rand.nextDouble() * RANGE * 2;
                        break;
                    case 2:
                        x = center.getX() - RANGE + rand.nextDouble() * RANGE * 2;
                        y = center.getY() - RANGE + rand.nextDouble() * RANGE * 2;
                        z = center.getZ() - RANGE;
                        break;
                    case 3:
                        x = center.getX() - RANGE + rand.nextDouble() * RANGE * 2;
                        y = center.getY() - RANGE + rand.nextDouble() * RANGE * 2;
                        z = center.getZ() + RANGE;
                        break;
                    case 4:
                        x = center.getX() - RANGE;
                        y = center.getY() - RANGE + rand.nextDouble() * RANGE * 2;
                        z = center.getZ() - RANGE + rand.nextDouble() * RANGE * 2;
                        break;
                    default:
                        x = center.getX() + RANGE;
                        y = center.getY() - RANGE + rand.nextDouble() * RANGE * 2;
                        z = center.getZ() - RANGE + rand.nextDouble() * RANGE * 2;
                        break;
                }

                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        x, y, z,
                        1, // 粒子数量
                        0, 0, 0, // 速度
                        0.1 // 速度随机性
                );
            }
        }
    }
    private void trySpawnMonster() {
        if (!isActive || !(level instanceof ServerLevel serverLevel)) return;

        List<Monster> monsters = serverLevel.getEntitiesOfClass(Monster.class, activeRegion);
        if (monsters.size() >= MAX_MOBS) return;

        Random rand = serverLevel.random;
        BlockPos center = getEffectivePos();
        BlockPos spawnPos;
        int attempts = 0;

        do {
            spawnPos = center.offset(
                    rand.nextInt(RANGE * 2) - RANGE,
                    rand.nextInt(RANGE * 2) - RANGE,
                    rand.nextInt(RANGE * 2) - RANGE
            );
            attempts++;
        } while (!isValidSpawnPos(spawnPos) && attempts < 10);

        if (attempts < 10) {
            spawnMonster(serverLevel, spawnPos);
        }
    }

    private boolean isValidSpawnPos(BlockPos pos) {
        return level.isEmptyBlock(pos) &&
                level.getBlockState(pos.below()).isValidSpawn(level, pos, EntityType.ZOMBIE);
    }

    private void spawnMonster(ServerLevel serverLevel, BlockPos pos) {
        Monster monster = switch (serverLevel.random.nextInt(12)) {
            case 0 -> new Zombie(EntityType.ZOMBIE, serverLevel);
            case 1 -> new Skeleton(EntityType.SKELETON, serverLevel);
            case 2 -> new Spider(EntityType.SPIDER, serverLevel);
            case 3 -> new CaveSpider(EntityType.CAVE_SPIDER, serverLevel);
            case 4 -> new Witch(EntityType.WITCH, serverLevel);
            case 5 -> new WitherSkeleton(EntityType.WITHER_SKELETON, serverLevel);
            case 7 -> new Vindicator(EntityType.VINDICATOR, serverLevel);
            case 8 -> new Evoker(EntityType.EVOKER, serverLevel);
            case 9 -> new EnderMan(EntityType.ENDERMAN, serverLevel);
            case 10 -> new Stray(EntityType.STRAY, serverLevel);
            case 11 -> new Pillager(EntityType.PILLAGER, serverLevel);
            default -> new Creeper(EntityType.CREEPER, serverLevel);
        };

        monster.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        monster.getPersistentData().putBoolean("DarkNightSpawn", true);
        serverLevel.addFreshEntity(monster);
    }

    private void constrainMonstersToRegion() {
        if (activeRegion == null || !(level instanceof ServerLevel)) return;

        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, activeRegion.inflate(5));
        for (Monster monster : monsters) {
            if (monster.getPersistentData().getBoolean("DarkNightSpawn")) {
                if (!activeRegion.contains(monster.position())) {
                    Vec3 center = new Vec3(getEffectivePos().getX(), getEffectivePos().getY(), getEffectivePos().getZ());
                    Vec3 toCenter = center.subtract(monster.position()).normalize();
                    monster.setDeltaMovement(toCenter.x, toCenter.y, toCenter.z);
                }
            }
        }
    }

    @Override
    public int getMaxMana() {
        return 3000;
    }

    @Override
    public int getColor() {
        return 0x000000;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}