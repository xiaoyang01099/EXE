package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * 诅咒产能花基类 - 完全独立的诅咒魔力生成系统
 */
public abstract class TileCursedGeneratingFlower extends TileEntityBindableSpecialFlower<ICursedManaCollector> {
    private static final ResourceLocation SPREADER_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "cursed_spreader");
    private static final String TAG_CURSED_MANA = "cursedMana";
    private static final String TAG_COOLDOWN = "cooldown";
    private static final String TAG_CURSE_TIMER = "curseTimer";
    private static final String TAG_DEATH_ANIMATION_TIMER = "deathAnimationTimer";
    protected int baseManaGeneration = 100;
    protected int range = 8;
    protected int cooldown = 0;
    protected int maxCooldown = 100;
    private int cursedMana = 0;
    private static final int MAX_CURSED_MANA = 1000;
    private static final int COLLECTOR_SEARCH_RANGE = 6;
    public static final int LINK_RANGE = 6;

    private static final int CURSE_CHECK_INTERVAL = 20;
    private static final int MAX_CURSE_TIMER = 15;
    private static final int WARNING_PARTICLE_INTERVAL = 5;

    private static final int STAGE_WARNING = 8;
    private static final int STAGE_CRITICAL = 12;
    private static final int STAGE_DYING = 15;

    private int deathAnimationTimer = -1;
    private int curseTimer = 0;
    private int lastSyncedCurseTimer = -1;

    public TileCursedGeneratingFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ICursedManaCollector.class);
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

        if (!level.isClientSide && level.getGameTime() % CURSE_CHECK_INTERVAL == 0) {
            checkCursedEnvironment();
        }

        if (level.isClientSide) {
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
            if (Math.random() < 0.3) {
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

    @Nullable
    @Override
    public ICursedManaCollector findBindCandidateAt(BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ICursedManaCollector) {
            return (ICursedManaCollector) be;
        }

        return null;
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

    private void spawnParticles() {
        if (cursedMana <= 0) return;

        float deathFactor = Math.min(1.0F, (float) curseTimer / MAX_CURSE_TIMER);
        double particleChance = 1.0 - (double) cursedMana / (double) getMaxCursedMana() / 3.5;

        if (Math.random() > particleChance) {
            int color = getCursedColor();
            float r = ((color >> 16 & 0xFF) / 255F) * (1.0F - deathFactor * 0.9F);
            float g = ((color >> 8 & 0xFF) / 255F) * (1.0F - deathFactor * 0.9F);
            float b = ((color & 0xFF) / 255F) * (1.0F - deathFactor * 0.9F);

            BotaniaAPI.instance().sparkleFX(
                    level,
                    worldPosition.getX() + 0.3 + Math.random() * 0.5,
                    worldPosition.getY() + 0.5 + Math.random() * 0.5,
                    worldPosition.getZ() + 0.3 + Math.random() * 0.5,
                    r, g, b,
                    (float) Math.random(),
                    5
            );
        }
    }

    private void serverTick() {
        if (cooldown > 0) {
            cooldown--;
        }

        if (canGenerate()) {
            Player cursedPlayer = getStrongestCursedPlayer();
            if (cursedPlayer != null && tryGenerateMana(cursedPlayer)) {
                int manaGenerated = calculateManaGeneration(cursedPlayer);
                addCursedMana(manaGenerated);
                cooldown = maxCooldown;
                sync();
            }
        }

        emptyCursedManaIntoCollector();
    }

    public int getCursedMana() {
        return cursedMana;
    }

    public int getMaxCursedMana() {
        return MAX_CURSED_MANA;
    }

    @Nullable
    @Override
    public BlockPos findClosestTarget() {
        ICursedManaCollector closestCollector = CursedManaNetwork.getInstance()
                .getClosestCursedCollector(getBlockPos(), getLevel(), getBindingRadius());
        return closestCollector == null ? null : closestCollector.getCursedManaReceiverPos();
    }

    public void addCursedMana(int mana) {
        this.cursedMana = Math.min(getMaxCursedMana(), this.cursedMana + mana);
        setChanged();
    }

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

    protected int calculateManaGeneration(Player player) {
        return CurseAbilityHandler.INSTANCE.calculateCursedMana(
                player, baseManaGeneration, 0.3f
        );
    }

    protected void emptyCursedManaIntoCollector() {
        if (level == null || cursedMana <= 0) return;

        ICursedManaCollector collector = findBoundTile();

        if (collector == null) {
            collector = CursedManaNetwork.getInstance()
                    .getClosestCursedCollector(worldPosition, level, COLLECTOR_SEARCH_RANGE);
        }

        if (collector != null && !collector.isCursedManaFull()) {
            int manaToTransfer = Math.min(
                    cursedMana,
                    collector.getMaxCursedMana() - collector.getCurrentCursedMana()
            );
            if (manaToTransfer > 0) {
                cursedMana -= manaToTransfer;
                collector.receiveCursedMana(manaToTransfer);
                setChanged();
            }
        }
    }

    @Override
    public boolean isValidBinding() {
        return super.isValidBinding();
    }

    public ItemStack getHudIcon() {
        return Registry.ITEM.getOptional(SPREADER_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    protected boolean canGenerate() {
        if (cooldown > 0) {
            return false;
        }
        Player cursedPlayer = getStrongestCursedPlayer();
        return cursedPlayer != null && getCursedMana() < getMaxCursedMana();
    }

    protected abstract boolean tryGenerateMana(Player cursedPlayer);

    public abstract int getCursedColor();

    @Override
    public int getBindingRadius() {
        return LINK_RANGE;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_CURSED_MANA, cursedMana);
        cmp.putInt(TAG_COOLDOWN, cooldown);
        cmp.putInt(TAG_CURSE_TIMER, curseTimer);
        cmp.putInt(TAG_DEATH_ANIMATION_TIMER, deathAnimationTimer);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        cursedMana = cmp.getInt(TAG_CURSED_MANA);
        cooldown = cmp.getInt(TAG_COOLDOWN);
        curseTimer = cmp.getInt(TAG_CURSE_TIMER);
        deathAnimationTimer = cmp.getInt(TAG_DEATH_ANIMATION_TIMER);
        lastSyncedCurseTimer = curseTimer;
    }

    public int getCurseTimer() {
        return curseTimer;
    }

    public static class CursedGeneratingWandHud<T extends TileCursedGeneratingFlower> implements IWandHUD {
        protected final T flower;

        public CursedGeneratingWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            int color = flower.getCursedColor();

            BotaniaAPIClient.instance().drawComplexManaHUD(ms, color,
                    flower.getCursedMana(), flower.getMaxCursedMana(),
                    name, flower.getHudIcon(), flower.isValidBinding());

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
