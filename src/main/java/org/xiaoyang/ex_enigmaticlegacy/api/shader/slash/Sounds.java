package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class Sounds {
    private static final AtomicLong NEXT_PRESSURE_BED_OWNER = new AtomicLong();
    private static final Map<Long, PressureBedSound> PRESSURE_BEDS = new HashMap<>();
    private static final Set<FadingSoundInstance> ACTIVE_ONE_SHOTS = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void playCut(long owner, ClientLevel level, SlashLine line, float volumeScale) {
        if (level == null || line == null || ClientEffects.cannotReceiveEffects()) return;

        Vec3 center = line.center();
        playSpatialCutLayer(
                owner,
                center,
                ModSounds.DIMENSIONAL_SLASH_CUT.get(),
                SlashCofig.Audio.CUT_VOLUME,
                SlashCofig.Audio.CUT_PITCH,
                volumeScale
        );
    }

    public static void playFinalCut(long owner, ClientLevel level, SlashLine line, float volumeScale) {
        if (level == null || line == null || ClientEffects.cannotReceiveEffects()) return;

        Vec3 center = line.center();
        SoundEvent slashCut = ModSounds.DIMENSIONAL_SLASH_CUT.get();
        playSpatialCutLayer(owner, center, slashCut, SlashCofig.Audio.FINAL_CUT_VOLUME, SlashCofig.Audio.FINAL_CUT_PITCH, volumeScale);
        playSpatialCutLayer(owner, center, slashCut, SlashCofig.Audio.FINAL_CUT_BOOST_VOLUME, SlashCofig.Audio.FINAL_CUT_PITCH, volumeScale);
    }

    private static void playSpatialCutLayer(long owner, Vec3 center, SoundEvent soundEvent, float volume, float pitch, float volumeScale) {
        playTracked(new FadingSoundInstance(
                soundEvent,
                volume,
                pitch,
                SoundInstance.Attenuation.LINEAR,
                center.x,
                center.y,
                center.z,
                false,
                volumeScale,
                owner
        ));
    }

    public static void playBreakPrepare(long owner, Vec3 center, boolean spatial, float volumeScale) {
        playRelativeOrSpatial(owner, center, spatial, ModSounds.DIMENSIONAL_SLASH_BREAK_PREPARE.get(), SlashCofig.Audio.BREAK_PREPARE_VOLUME, SlashCofig.Audio.BREAK_PREPARE_PITCH, volumeScale);
    }

    public static long newPressureBedOwner() {
        return NEXT_PRESSURE_BED_OWNER.incrementAndGet();
    }

    public static void startPressureBed(long owner, Vec3 center, boolean spatial, float volumeScale) {
        Minecraft minecraft = Minecraft.getInstance();
        if (ClientEffects.cannotReceiveEffects()) return;

        PressureBedSound pressureBed = PRESSURE_BEDS.get(owner);
        boolean pressureBedActive = pressureBed != null && !pressureBed.isFinished()
                && minecraft.getSoundManager().isActive(pressureBed);
        if (!pressureBedActive) {
            if (pressureBed != null) pressureBed.stopImmediately();
            pressureBed = new PressureBedSound(center, spatial);
            pressureBed.setRangeVolume(volumeScale);
            PRESSURE_BEDS.put(owner, pressureBed);
            minecraft.getSoundManager().play(pressureBed);
        } else {
            pressureBed.cancelFadeOut();
            pressureBed.setRangeVolume(volumeScale);
        }
    }

    public static void releasePressureBed(long owner) {
        PressureBedSound pressureBed = PRESSURE_BEDS.get(owner);
        if (pressureBed != null) pressureBed.beginFadeOut();
    }

    public static void clear() {
        for (FadingSoundInstance sound : ACTIVE_ONE_SHOTS) {
            sound.stopImmediately();
        }
        ACTIVE_ONE_SHOTS.clear();
        for (PressureBedSound pressureBed : PRESSURE_BEDS.values()) {
            pressureBed.stopImmediately();
        }
        PRESSURE_BEDS.clear();
    }

    public static void playSpaceFracture(long owner, Vec3 center, boolean spatial, float volumeScale) {
        playRelativeOrSpatial(owner, center, spatial, ModSounds.DIMENSIONAL_SLASH_SPACE_FRACTURE.get(), SlashCofig.Audio.SPACE_FRACTURE_VOLUME, SlashCofig.Audio.SPACE_FRACTURE_PITCH, volumeScale);
    }

    public static void playFinalGlassTail(long owner, Vec3 center, boolean spatial, float volumeScale) {
        playRelativeOrSpatial(owner, center, spatial, ModSounds.DIMENSIONAL_SLASH_SPACE_FRACTURE.get(), SlashCofig.Audio.FINAL_GLASS_TAIL_VOLUME, SlashCofig.Audio.FINAL_GLASS_TAIL_PITCH, volumeScale);
    }

    public static void playGlassBreak(long owner, Vec3 center, boolean spatial, float volumeScale) {
        playRelativeOrSpatial(owner, center, spatial, SoundEvents.GLASS_BREAK, SlashCofig.Audio.GLASS_BREAK_VOLUME, SlashCofig.Audio.GLASS_BREAK_PITCH, volumeScale);
    }

    static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ACTIVE_ONE_SHOTS.removeIf(sound -> !minecraft.getSoundManager().isActive(sound));
        PRESSURE_BEDS.entrySet().removeIf(entry -> entry.getValue().isFinished()
                || !minecraft.getSoundManager().isActive(entry.getValue()));
    }

    static void updateOwnerVolume(long owner, float volumeScale) {
        float safeScale = safeVolumeScale(volumeScale);
        for (FadingSoundInstance sound : ACTIVE_ONE_SHOTS) {
            if (sound.owner() == owner) sound.setRangeVolume(safeScale);
        }
        PressureBedSound pressureBed = PRESSURE_BEDS.get(owner);
        if (pressureBed != null) pressureBed.setRangeVolume(safeScale);
    }

    static void stopOwner(long owner, int fadeTicks) {
        for (FadingSoundInstance sound : ACTIVE_ONE_SHOTS) {
            if (sound.owner() == owner) sound.beginFadeOut(fadeTicks);
        }
        PressureBedSound pressureBed = PRESSURE_BEDS.get(owner);
        if (pressureBed != null) pressureBed.beginFadeOut(fadeTicks);
    }

    private static void playRelativeOrSpatial(long owner, Vec3 center, boolean spatial, SoundEvent soundEvent, float volume, float pitch, float volumeScale) {
        if (ClientEffects.cannotReceiveEffects()) return;

        Vec3 position = spatial && center != null ? center : Vec3.ZERO;
        playTracked(new FadingSoundInstance(
                soundEvent,
                volume,
                pitch,
                spatial ? SoundInstance.Attenuation.LINEAR : SoundInstance.Attenuation.NONE,
                position.x,
                position.y,
                position.z,
                !spatial,
                volumeScale,
                owner
        ));
    }

    private static void playTracked(FadingSoundInstance sound) {
        Minecraft minecraft = Minecraft.getInstance();
        ACTIVE_ONE_SHOTS.add(sound);
        minecraft.getSoundManager().play(sound);
    }

    private static final class FadingSoundInstance extends AbstractTickableSoundInstance {
        private final float baseVolume;
        private final long owner;
        private float rangeVolume = 1.0f;
        private float fadeVolume = 1.0f;
        private int fadeOutAge = -1;
        private int fadeOutTicks = 1;
        private boolean finished;

        private FadingSoundInstance(
                SoundEvent soundEvent,
                float volume,
                float pitch,
                Attenuation attenuation,
                double x,
                double y,
                double z,
                boolean relative,
                float rangeVolume,
                long owner
        ) {
            super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.baseVolume = volume;
            this.owner = owner;
            this.rangeVolume = safeVolumeScale(rangeVolume);
            this.volume = volume * this.rangeVolume;
            this.pitch = pitch;
            this.looping = false;
            this.delay = 0;
            this.attenuation = attenuation;
            this.relative = relative;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void tick() {
            if (fadeOutAge < 0) return;

            fadeOutAge++;
            fadeVolume = ClientLifecycle.fadeAlpha(fadeOutAge, fadeOutTicks);
            updateVolume();
            if (fadeOutAge >= fadeOutTicks) {
                stopImmediately();
            }
        }

        private void beginFadeOut(int ticks) {
            if (finished || fadeOutAge >= 0) return;
            fadeOutTicks = Math.max(1, ticks);
            fadeOutAge = 0;
        }

        private long owner() {
            return owner;
        }

        private void setRangeVolume(float volumeScale) {
            if (fadeOutAge >= 0) return;
            rangeVolume = safeVolumeScale(volumeScale);
            updateVolume();
        }

        private void updateVolume() {
            this.volume = baseVolume * rangeVolume * fadeVolume;
        }

        private void stopImmediately() {
            if (finished) return;
            finished = true;
            stop();
        }
    }

    private static final class PressureBedSound extends AbstractTickableSoundInstance {
        private int age;
        private int fadeOutAge = -1;
        private int fadeOutTicks = 1;
        private float rangeVolume = 1.0f;
        private boolean finished;

        private PressureBedSound(Vec3 center, boolean spatial) {
            super(ModSounds.DIMENSIONAL_SLASH_PRESSURE_BUILD.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.looping = false;
            this.delay = 0;
            Vec3 position = spatial && center != null ? center : Vec3.ZERO;
            this.attenuation = spatial ? Attenuation.LINEAR : Attenuation.NONE;
            this.relative = !spatial;
            this.x = position.x;
            this.y = position.y;
            this.z = position.z;
            // Start above silence so the charge sample's early transient is not discarded by SoundEngine.
            this.age = 1;
            updateSoundShape();
        }

        @Override
        public void tick() {
            age++;
            updateSoundShape();

            if (fadeOutAge >= 0) {
                fadeOutAge++;
                this.volume *= ClientLifecycle.fadeAlpha(fadeOutAge, fadeOutTicks);
                if (fadeOutAge >= fadeOutTicks) {
                    stopImmediately();
                }
            }
        }

        private void updateSoundShape() {
            int fadeInTicks = Math.max(1, SlashCofig.Audio.PRESSURE_BED_FADE_IN_TICKS);
            int riseTicks = SlashCofig.Audio.PRESSURE_BED_RISE_TICKS;
            float fadeIn = Mth.clamp(age / (float) fadeInTicks, 0.0F, 1.0F);
            float rise = Mth.clamp(age / (float) riseTicks, 0.0F, 1.0F);
            this.volume = Math.max(0.001F, fadeIn * Mth.lerp(rise, SlashCofig.Audio.PRESSURE_BED_START_VOLUME, SlashCofig.Audio.PRESSURE_BED_END_VOLUME) * rangeVolume);
            this.pitch = Mth.lerp(rise, SlashCofig.Audio.PRESSURE_BED_START_PITCH, SlashCofig.Audio.PRESSURE_BED_END_PITCH);
        }

        private void beginFadeOut() {
            beginFadeOut(SlashCofig.Audio.PRESSURE_BED_FADE_OUT_TICKS);
        }

        private void beginFadeOut(int ticks) {
            if (finished || fadeOutAge >= 0) return;
            fadeOutTicks = Math.max(1, ticks);
            fadeOutAge = 0;
        }

        private void cancelFadeOut() {
            if (!finished) fadeOutAge = -1;
        }

        private void setRangeVolume(float volumeScale) {
            rangeVolume = safeVolumeScale(volumeScale);
            if (!finished) updateSoundShape();
        }

        private boolean isFinished() {
            return finished;
        }

        private void stopImmediately() {
            if (finished) return;
            finished = true;
            stop();
        }
    }

    private static float safeVolumeScale(float volumeScale) {
        return Mth.clamp(volumeScale, 0.0f, 1.0f);
    }

    private Sounds() {
    }
}
