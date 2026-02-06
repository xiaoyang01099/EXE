package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.subtile.TileEntityBindableSpecialFlower;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * 诅咒功能花基类
 * 从诅咒魔力池抽取魔力来执行功能
 * 如果没有诅咒环境，花朵会死亡
 */
public abstract class TileCursedFunctionalFlower extends TileEntityBindableSpecialFlower<ICursedManaPool> {
    private static final ResourceLocation POOL_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "cursed_mana_pool");
    public static final int LINK_RANGE = 10;
    private static final String TAG_MANA = "mana";
    private static final String TAG_CURSE_TIMER = "curseTimer";
    private static final String TAG_DEATH_ANIMATION_TIMER = "deathAnimationTimer";

    private static final int CURSE_CHECK_INTERVAL = 20;
    private static final int MAX_CURSE_TIMER = 15;
    private static final int WARNING_PARTICLE_INTERVAL = 5;

    private static final int STAGE_WARNING = 8;
    private static final int STAGE_CRITICAL = 12;
    private static final int STAGE_DYING = 15;

    private int deathAnimationTimer = -1;
    private int curseTimer = 0;
    private int lastSyncedCurseTimer = -1;
    protected int baseManaConsumption = 10;
    protected int range = 5;
    private int mana = 0;
    public int redstoneSignal = 0;

    public TileCursedFunctionalFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ICursedManaPool.class);
    }

    public boolean acceptsRedstone() {
        return false;
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (deathAnimationTimer >= 0) {
            if (level.isClientSide) {
                spawnDeathStageParticles();
            }
            deathAnimationTimer++;
            if (deathAnimationTimer >= 40) {
                if (!level.isClientSide) {
                    killFlower();
                }
                return;
            }
            return;
        }
        if (!getLevel().isClientSide && getLevel().getGameTime() % CURSE_CHECK_INTERVAL == 0) {
            checkCursedEnvironment();
        }
        drawManaFromPool();
        redstoneSignal = 0;
        if (acceptsRedstone()) {
            for (Direction dir : Direction.values()) {
                int redstoneSide = getLevel().getSignal(getBlockPos().relative(dir), dir);
                redstoneSignal = Math.max(redstoneSignal, redstoneSide);
            }
        }
        if (getLevel().isClientSide) {
            spawnParticles();
            spawnDeathStageParticles();
        } else {
            serverTick();
        }
    }

    private void checkCursedEnvironment() {
        boolean hasCursedPlayer = hasCursedPlayerNearby();

        if (hasCursedPlayer) {
            if (curseTimer != 0) {
                curseTimer = 0;
                markForSync();
            }
        } else {
            if (deathAnimationTimer >= 0) {
                return;
            }

            int oldStage = getDeathStage(curseTimer);
            curseTimer++;
            int newStage = getDeathStage(curseTimer);

            if (newStage != oldStage) {
                playStageTransitionSound(newStage);
            }

            if (curseTimer % WARNING_PARTICLE_INTERVAL == 0 && curseTimer > 0) {
                spawnWitherParticles();
            }

            if (curseTimer >= MAX_CURSE_TIMER) {
                startDeathAnimation();
                return;
            }

            markForSync();
        }
    }

    private int getDeathStage(int timer) {
        if (timer >= STAGE_DYING) return 3;
        if (timer >= STAGE_CRITICAL) return 2;
        if (timer >= STAGE_WARNING) return 1;
        return 0;
    }

    private void playStageTransitionSound(int stage) {
        if (level == null || level.isClientSide) return;

        switch (stage) {
            case 1:
                level.playSound(null, worldPosition,
                        SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS,
                        0.3F, 1.2F);
                break;
            case 2:
                level.playSound(null, worldPosition,
                        SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS,
                        0.5F, 1.5F);
                break;
            case 3:
                level.playSound(null, worldPosition,
                        SoundEvents.WITHER_HURT, SoundSource.BLOCKS,
                        0.7F, 0.8F);
                break;
        }
    }

    private void spawnDeathStageParticles() {
        if (curseTimer <= 0) return;

        int stage = getDeathStage(curseTimer);
        if (stage == 0) return;

        int particleCount = stage;
        float deathProgress = (float) curseTimer / MAX_CURSE_TIMER;

        for (int i = 0; i < particleCount; i++) {
            if (Math.random() < 0.3) { // 30%概率生成粒子
                double x = worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                double y = worldPosition.getY() + 0.3 + Math.random() * 0.5;
                double z = worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;

                float gray = 0.3F * (1.0F - deathProgress);
                WispParticleData data = WispParticleData.wisp(
                        0.15F + stage * 0.05F,
                        gray, gray, gray,
                        true
                );

                level.addParticle(data, x, y, z,
                        0,
                        0.01 + stage * 0.01,
                        0);
            }
        }
    }

    private void startDeathAnimation() {
        if (level == null || level.isClientSide) return;

        level.playSound(null, worldPosition,
                SoundEvents.WITHER_DEATH, SoundSource.BLOCKS,
                1.0F, 0.8F);

        spawnMassiveDeathParticles();

        deathAnimationTimer = 0;
        setChanged();
    }

    private void spawnMassiveDeathParticles() {
        if (level == null) return;

        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 0.5;
            double x = worldPosition.getX() + 0.5 + Math.cos(angle) * radius;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + Math.sin(angle) * radius;

            WispParticleData data = WispParticleData.wisp(
                    0.2F + (float) Math.random() * 0.1F,
                    0.1F, 0.1F, 0.1F,
                    true
            );

            level.addParticle(data, x, y, z,
                    Math.cos(angle) * 0.1,
                    0.1 + Math.random() * 0.1,
                    Math.sin(angle) * 0.1);
        }

        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 4;
            double height = (i / 20.0) * 2.0;
            double x = worldPosition.getX() + 0.5 + Math.cos(angle) * 0.3;
            double y = worldPosition.getY() + 0.5 + height;
            double z = worldPosition.getZ() + 0.5 + Math.sin(angle) * 0.3;

            WispParticleData data = WispParticleData.wisp(
                    0.15F,
                    0.2F, 0.2F, 0.2F,
                    true
            );

            level.addParticle(data, x, y, z, 0, 0.05, 0);
        }
    }

    private void markForSync() {
        if (curseTimer != lastSyncedCurseTimer) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                lastSyncedCurseTimer = curseTimer;
            }
        }
    }

    public void killFlower() {
        if (level == null || level.isClientSide) return;

        spawnFinalExplosionParticles();

        level.playSound(null, worldPosition,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                0.5F, 1.5F);

        level.setBlockAndUpdate(worldPosition, Blocks.DEAD_BUSH.defaultBlockState());
    }

    private void spawnFinalExplosionParticles() {
        if (level == null) return;

        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * Math.PI * 2;
            double pitch = Math.random() * Math.PI;
            double speed = 0.2 + Math.random() * 0.3;

            double vx = Math.cos(angle) * Math.sin(pitch) * speed;
            double vy = Math.cos(pitch) * speed;
            double vz = Math.sin(angle) * Math.sin(pitch) * speed;

            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;

            WispParticleData data = WispParticleData.wisp(
                    0.3F,
                    0.05F, 0.05F, 0.05F,
                    true
            );

            level.addParticle(data, x, y, z, vx, vy, vz);
        }
    }

    public int getCurseTimer() {
        return curseTimer;
    }

    private void spawnWitherParticles() {
        if (level == null) return;

        int stage = getDeathStage(curseTimer);
        int count = 5 + stage * 3;

        for (int i = 0; i < count; i++) {
            double x = worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.6;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.6;

            float brightness = 0.3F - stage * 0.05F;
            WispParticleData data = WispParticleData.wisp(
                    0.1F + stage * 0.03F,
                    brightness, brightness, brightness,
                    true
            );

            level.addParticle(data, x, y, z, 0, 0.02 + stage * 0.01, 0);
        }
    }

    private void serverTick() {
        if (canOperate()) {
            Player cursedPlayer = getStrongestCursedPlayer();
            if (cursedPlayer != null) {
                int manaCost = calculateManaConsumption(cursedPlayer);
                addMana(-manaCost);
                performCursedEffect(cursedPlayer);
            }
        }
    }

    private void spawnParticles() {
        float deathFactor = Math.min(1.0F, (float) curseTimer / MAX_CURSE_TIMER);

        double particleChance = 1F - (double) mana / (double) getMaxMana() / 3.5F;
        int color = getColor();

        float red = (color >> 16 & 0xFF) / 255F * (1.0F - deathFactor * 0.9F);
        float green = (color >> 8 & 0xFF) / 255F * (1.0F - deathFactor * 0.9F);
        float blue = (color & 0xFF) / 255F * (1.0F - deathFactor * 0.9F);

        if (Math.random() > particleChance) {
            BotaniaAPI.instance().sparkleFX(
                    getLevel(),
                    getBlockPos().getX() + 0.3 + Math.random() * 0.5,
                    getBlockPos().getY() + 0.5 + Math.random() * 0.5,
                    getBlockPos().getZ() + 0.3 + Math.random() * 0.5,
                    red, green, blue,
                    (float) Math.random(),
                    5
            );
        }
    }

    @Override
    public int getBindingRadius() {
        return LINK_RANGE;
    }

    @Nullable
    @Override
    public ICursedManaPool findBindCandidateAt(BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ICursedManaPool) {
            return (ICursedManaPool) be;
        }

        return null;
    }

    @Nullable
    @Override
    public BlockPos findClosestTarget() {
        ICursedManaPool closestPool = CursedManaNetwork.getInstance()
                .getClosestCursedPool(getBlockPos(), getLevel(), getBindingRadius());
        return closestPool == null ? null : closestPool.getCursedManaReceiverPos();
    }

    public void drawManaFromPool() {
        if (getMana() >= getMaxMana()) {
            return;
        }

        ICursedManaPool pool = findBoundTile();

        if (pool == null) {
            pool = CursedManaNetwork.getInstance()
                    .getClosestCursedPool(getBlockPos(), getLevel(), getBindingRadius());
        }

        if (pool != null) {
            int manaInPool = pool.getCurrentCursedMana();
            int manaMissing = getMaxMana() - mana;
            int manaToRemove = Math.min(manaMissing, manaInPool);
            if (manaToRemove > 0) {
                pool.receiveCursedMana(-manaToRemove);
                addMana(manaToRemove);
            }
        }
    }

    @Override
    public boolean isValidBinding() {
        return super.isValidBinding();
    }

    public int getMana() {
        return mana;
    }

    public void addMana(int mana) {
        this.mana = Mth.clamp(this.mana + mana, 0, getMaxMana());
        setChanged();
    }

    public abstract int getMaxMana();
    public abstract int getColor();

    protected AABB getEffectBounds() {
        BlockPos pos = getBlockPos();
        return new AABB(
                pos.getX() - range, pos.getY() - range, pos.getZ() - range,
                pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1
        );
    }

    protected boolean hasCursedPlayerNearby() {
        if (level == null) return false;
        return level.getEntitiesOfClass(Player.class, getEffectBounds())
                .stream()
                .anyMatch(CurseAbilityHandler.INSTANCE::isCursed);
    }

    @Nullable
    protected Player getStrongestCursedPlayer() {
        if (level == null) return null;
        return level.getEntitiesOfClass(Player.class, getEffectBounds())
                .stream()
                .filter(CurseAbilityHandler.INSTANCE::isCursed)
                .max(Comparator.comparingInt(CurseAbilityHandler.INSTANCE::getCurseLevel))
                .orElse(null);
    }

    protected int calculateManaConsumption(Player player) {
        return CurseAbilityHandler.INSTANCE.calculateCursedMana(
                player, baseManaConsumption, 0.2f
        );
    }

    protected boolean canOperate() {
        Player cursedPlayer = getStrongestCursedPlayer();
        if (cursedPlayer == null) return false;
        int manaCost = calculateManaConsumption(cursedPlayer);
        return getMana() >= manaCost;
    }

    protected abstract void performCursedEffect(Player cursedPlayer);

    public ItemStack getHudIcon() {
        return Registry.ITEM.getOptional(POOL_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        mana = cmp.getInt(TAG_MANA);
        curseTimer = cmp.getInt(TAG_CURSE_TIMER);
        deathAnimationTimer = cmp.getInt(TAG_DEATH_ANIMATION_TIMER);
        lastSyncedCurseTimer = curseTimer;

    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_CURSE_TIMER, curseTimer);
        cmp.putInt(TAG_DEATH_ANIMATION_TIMER, deathAnimationTimer);
    }

    public static class CursedFunctionalWandHud<T extends TileCursedFunctionalFlower> implements IWandHUD {
        protected final T flower;

        public CursedFunctionalWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            int color = flower.getColor();

            BotaniaAPIClient.instance().drawComplexManaHUD(
                    ms, color,
                    flower.getMana(), flower.getMaxMana(),
                    name, flower.getHudIcon(),
                    flower.isValidBinding()
            );

            int curseTimer = flower.getCurseTimer();
            if (curseTimer > 0) {
                int remaining = MAX_CURSE_TIMER - curseTimer;
                String warning = I18n.get("message.ex_enigmaticlegacy.cursed_flower.wither_warning", remaining);

                int textColor;
                if (remaining <= 3) {
                    textColor = 0xFFFF0000;
                    if (mc.level.getGameTime() % 10 < 5) {
                        textColor = 0xFFFFFFFF;
                    }
                } else if (remaining <= 7) {
                    textColor = 0xFFFF5500;
                } else if (remaining <= 10) {
                    textColor = 0xFFFFAA00;
                } else {
                    textColor = 0xFFFFFF00;
                }

                mc.font.drawShadow(ms, warning,
                        (float) mc.getWindow().getGuiScaledWidth() / 2 - (float) mc.font.width(warning) / 2,
                        (float) mc.getWindow().getGuiScaledHeight() / 2 + 20,
                        textColor);
            }
        }
    }
}
