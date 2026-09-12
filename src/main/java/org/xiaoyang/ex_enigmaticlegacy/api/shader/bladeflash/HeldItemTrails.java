package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeldItemTrails {
    public record BladePoints(Vec3 root, Vec3 tip) {
        public BladePoints {
            if (!finite(root) || !finite(tip)) throw new IllegalArgumentException("Finite model-space points required");
        }
    }
    private record Context(LivingEntity owner, ItemStack stack, ItemDisplayContext display, boolean left) {}
    private record Slot(UUID owner, boolean left, boolean firstPerson) {}
    private static final class Track {
        final Object key = new Object();
        final ItemStack stack;
        long frame = -1;
        Track(ItemStack stack) { this.stack = stack.copy(); }
    }

    private static final BladePoints VANILLA_SWORD = new BladePoints(new Vec3(6.5/16,6.5/16,0.5), new Vec3(15.5/16,15.5/16,0.5));
    private static final Map<Item, BladePoints> PROFILES = new HashMap<>();
    private static final Map<Slot, Track> TRACKS = new HashMap<>();
    private static final ArrayDeque<Context> CONTEXTS = new ArrayDeque<>();
    static final TrailStore FIRST_PERSON = new TrailStore();
    private static final Matrix4f inverseView = new Matrix4f();
    private static final Matrix4f inverseViewProjection = new Matrix4f();
    private static Vec3 camera = Vec3.ZERO;
    private static long frame;
    private static boolean inFrame;
    private static boolean previousFirstPerson;
    static long firstPersonSamples;
    static long thirdPersonSamples;
    static Vec3 lastRoot = Vec3.ZERO;
    static Vec3 lastTip = Vec3.ZERO;

    private HeldItemTrails() {}

    public static void registerBlade(Item item, BladePoints points) {
        PROFILES.put(item, points);
    }

    static void beginFrame(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        boolean first = mc.options.getCameraType().isFirstPerson();
        if (first != previousFirstPerson) {
            FIRST_PERSON.clear();
            TRACKS.entrySet().removeIf(entry -> {
                if (mc.player != null && entry.getKey().owner.equals(mc.player.getUUID())) {
                    BladeFlashClient.TRAILS.breakTrail(entry.getValue().key);
                    return true;
                }
                return false;
            });
        }
        previousFirstPerson = first;
        camera = event.getCamera().getPosition();
        inverseView.set(event.getPoseStack().last().pose()).invert();
        inverseViewProjection.set(event.getProjectionMatrix()).mul(event.getPoseStack().last().pose()).invert();
        frame++;
        inFrame = true;
        CONTEXTS.clear();
        if (!ConfigHandler.ENABLED.get() || !ConfigHandler.HELD_SWORD_TRAILS.get()) resetTracks();
    }

    public static void beginItem(LivingEntity owner, ItemStack stack, ItemDisplayContext display, boolean left) {
        if (inFrame) CONTEXTS.push(new Context(owner, stack, display, left));
    }

    public static void endItem() {
        if (inFrame && !CONTEXTS.isEmpty()) CONTEXTS.pop();
    }

    public static void sampleModel(PoseStack pose) {
        if (org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState.isShadowPass()) return;
        if (!inFrame || CONTEXTS.isEmpty() || !ConfigHandler.ENABLED.get() || !ConfigHandler.HELD_SWORD_TRAILS.get()) return;
        Context context = CONTEXTS.peek();
        if (context.owner == null || context.owner.level() != Minecraft.getInstance().level) return;
        boolean first = context.display == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context.display == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean third = context.display == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context.display == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (!first && !third) return;
        if (!eligible(context.stack)) return;
        BladePoints points = PROFILES.getOrDefault(context.stack.getItem(), VANILLA_SWORD);
        Slot slot = new Slot(context.owner.getUUID(), context.left, first);
        TrailStore store = first ? FIRST_PERSON : BladeFlashClient.TRAILS;
        Track track = TRACKS.get(slot);
        if (track != null && track.frame == frame) return; // Foil and multi-pass models render more than once.
        if (track != null && (track.frame != frame - 1 || !ItemStack.isSameItemSameTags(track.stack, context.stack))) {
            store.breakTrail(track.key);
            TRACKS.remove(slot);
            track = null;
        }
        if (track == null) {
            track = new Track(context.stack);
            TRACKS.put(slot, track);
        }
        Matrix4f transform = new Matrix4f();
        if (first) {
            transform.set(inverseViewProjection).mul(RenderSystem.getProjectionMatrix()).mul(pose.last().pose());
        } else {
            transform.set(inverseView).mul(pose.last().pose());
        }
        Vec3 root = worldPoint(transform, points.root);
        Vec3 tip = worldPoint(transform, points.tip);
        double time = BladeFlashClient.timeSeconds();
        store.sample(track.key, root, tip, time, ConfigHandler.TRAIL_SECONDS.get(),org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.blade(context.stack));
        track.frame = frame;
        if (first) firstPersonSamples++; else thirdPersonSamples++;
        lastRoot = root;
        lastTip = tip;
    }

    static void prepareFirstPerson(RenderLevelStageEvent event) {
        FIRST_PERSON.prune(BladeFlashClient.timeSeconds());
        Minecraft mc = Minecraft.getInstance();
        if (!ConfigHandler.ENABLED.get() || !ConfigHandler.HELD_SWORD_TRAILS.get() || mc.player == null) return;
        if (FIRST_PERSON.hasGeometry() || (mc.options.getCameraType().isFirstPerson()
            && (eligible(mc.player.getMainHandItem()) || eligible(mc.player.getOffhandItem())))) {
            BladeFlashRenderer.FIRST_PERSON.prepareFirstPersonDepth(event);
        }
    }

    public static void finishFrame() {
        if (!inFrame) return;
        inFrame = false;
        CONTEXTS.clear();
        TRACKS.entrySet().removeIf(entry -> {
            if (entry.getValue().frame == frame) return false;
            (entry.getKey().firstPerson ? FIRST_PERSON : BladeFlashClient.TRAILS).breakTrail(entry.getValue().key);
            return true;
        });
        BladeFlashRenderer.FIRST_PERSON.renderFirstPerson(FIRST_PERSON, camera);
    }

    static void reset() {
        inFrame = false;
        CONTEXTS.clear();
        resetTracks();
        BladeFlashRenderer.FIRST_PERSON.release();
    }

    private static void resetTracks() {
        for (Map.Entry<Slot, Track> entry : TRACKS.entrySet()) {
            if (!entry.getKey().firstPerson) BladeFlashClient.TRAILS.breakTrail(entry.getValue().key);
        }
        TRACKS.clear();
        FIRST_PERSON.clear();
    }

    private static boolean eligible(ItemStack stack) {
        return !stack.isEmpty() && org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.blade(stack)!=org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.NONE;
    }

    private static Vec3 worldPoint(Matrix4f matrix, Vec3 point) {
        Vector4f p = matrix.transform(new Vector4f((float)point.x, (float)point.y, (float)point.z, 1));
        if (Math.abs(p.w) < 1e-6) throw new IllegalArgumentException("Invalid held-item projection");
        return camera.add(p.x/p.w, p.y/p.w, p.z/p.w);
    }

    private static boolean finite(Vec3 v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }
}
