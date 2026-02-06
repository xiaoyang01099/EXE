package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MusicalOrchidTile extends TileEntityGeneratingFlower {
    private static final String TAG_GENERATION_TICKS = "generationTicks";
    private static final String TAG_ACTIVE_MUSIC = "activeMusic";

    private static final int DETECTION_RANGE = 4; // 9x9x9 区域（从中心开始半径 4）
    private static final int MAX_JUKEBOXES = 4;
    private static final int MANA_PER_JUKEBOX_PER_SECOND = 20;
    private static final int MAX_MANA = 12000;
    private static final int MANA_PER_TICK = MANA_PER_JUKEBOX_PER_SECOND / 20; // 1 每个唱片机每刻的魔力值

    private int generationTicks = 0;
    private ResourceLocation activeMusic = null;
    private boolean isGenerating = false;

    public MusicalOrchidTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {

            List<JukeboxBlockEntity> activeJukeboxes = findActiveJukeboxes();
            boolean wasGenerating = isGenerating;

            if (activeJukeboxes.isEmpty()) {
                isGenerating = false;
                activeMusic = null;
                generationTicks = 0;
            } else {
                ResourceLocation currentMusic = getMusicFromJukebox(activeJukeboxes.get(0));
                boolean allSameMusic = activeJukeboxes.stream()
                        .allMatch(jukebox -> {
                            ResourceLocation music = getMusicFromJukebox(jukebox);
                            return music != null && music.equals(currentMusic);
                        });

                if (allSameMusic && currentMusic != null) {
                    activeMusic = currentMusic;
                    isGenerating = true;
                    generationTicks++;

                    int effectiveJukeboxes = Math.min(activeJukeboxes.size(), MAX_JUKEBOXES);
                    int manaToGenerate = effectiveJukeboxes * MANA_PER_TICK;

                    addMana(manaToGenerate);
                } else {
                    isGenerating = false;
                    activeMusic = null;
                    generationTicks = 0;
                }
            }
            if (wasGenerating != isGenerating) {
                sync();
            }
        } else {

            if (isGenerating) {
                generationTicks++;

                if (getLevel().random.nextFloat() < 0.1f) {
                    spawnMusicParticles();
                }

                if (getLevel().random.nextFloat() < 0.05f) {
                    spawnSwayParticles();
                }
            }
        }
    }

    private List<JukeboxBlockEntity> findActiveJukeboxes() {
        List<JukeboxBlockEntity> jukeboxes = new ArrayList<>();
        BlockPos center = getBlockPos();

        for (int x = -DETECTION_RANGE; x <= DETECTION_RANGE; x++) {
            for (int y = -DETECTION_RANGE; y <= DETECTION_RANGE; y++) {
                for (int z = -DETECTION_RANGE; z <= DETECTION_RANGE; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = getLevel().getBlockState(pos);

                    if (state.is(Blocks.JUKEBOX) && state.getValue(JukeboxBlock.HAS_RECORD)) {
                        if (getLevel().getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                            jukeboxes.add(jukebox);
                        }
                    }
                }
            }
        }

        return jukeboxes;
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnSwayParticles() {
        BlockPos pos = getBlockPos();
        float time = getLevel().getGameTime() * 0.1f;

        for (int i = 0; i < 2; i++) {
            double angle = (time + i * 180) * Math.PI / 180;
            double x = pos.getX() + 0.5 + Math.cos(angle) * 0.3;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * 0.3;
            double y = pos.getY() + 0.2 + Math.sin(time * 2) * 0.1;

            getLevel().addParticle(
                    ParticleTypes.ENCHANT,
                    x, y, z, 0, 0.01, 0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnMusicParticles() {
        BlockPos pos = getBlockPos();
        int activeCount = getActiveJukeboxCount();

        for (int i = 0; i < Math.min(activeCount, 2); i++) {
            double x = pos.getX() + 0.5 + (getLevel().random.nextDouble() - 0.5) * 1.5;
            double y = pos.getY() + 0.8 + getLevel().random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.5 + (getLevel().random.nextDouble() - 0.5) * 1.5;

            getLevel().addParticle(
                    ParticleTypes.NOTE,
                    x, y, z,
                    getLevel().random.nextDouble() * 24.0 / 24.0,
                    0.0, 0.0
            );
        }
    }

    @Nullable
    private ResourceLocation getMusicFromJukebox(JukeboxBlockEntity jukebox) {
        if (jukebox.getRecord().getItem() instanceof RecordItem recordItem) {
            return recordItem.getSound().getLocation();
        }
        return null;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x00FFFF;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), DETECTION_RANGE);
    }

    public boolean isGenerating() {
        return isGenerating;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    public int getActiveJukeboxCount() {
        if (!isGenerating) return 0;
        List<JukeboxBlockEntity> activeJukeboxes = findActiveJukeboxes();
        return Math.min(activeJukeboxes.size(), MAX_JUKEBOXES);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_GENERATION_TICKS, generationTicks);
        cmp.putBoolean("isGenerating", isGenerating);
        if (activeMusic != null) {
            cmp.putString(TAG_ACTIVE_MUSIC, activeMusic.toString());
        }
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        generationTicks = cmp.getInt(TAG_GENERATION_TICKS);
        isGenerating = cmp.getBoolean("isGenerating");
        if (cmp.contains(TAG_ACTIVE_MUSIC)) {
            activeMusic = new ResourceLocation(cmp.getString(TAG_ACTIVE_MUSIC));
        } else {
            activeMusic = null;
        }
    }

    public float getSwayAmount() {
        if (!isGenerating) return 0.0f;
        return (float) Math.sin(generationTicks * 0.1) * 0.1f;
    }
}