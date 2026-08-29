package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModParticleTypes;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;

import javax.annotation.Nullable;
import java.util.*;

public final class TrueDemonWeaponParticleEmitter {
    private static final double MAX_DISTANCE_SQR = 32.0D * 32.0D;
    private static final float SURFACE_OFFSET = 0.001245F;
    private static final long STATE_RETENTION_TICKS = 200L;
    private static final int CONTEXT_SLOTS = ItemDisplayContext.values().length * 2;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<BakedModel, SurfaceMesh> MODEL_SURFACES = new IdentityHashMap<>();
    private static final Map<ItemStack, SpawnState> SPAWN_STATES = new IdentityHashMap<>();
    @Nullable private static ClientLevel trackedLevel;
    private static long lastStateCleanupTick = Long.MIN_VALUE;
    private static int worldRenderDepth;

    private TrueDemonWeaponParticleEmitter() {
    }

    public static void beginWorldRender() {
        worldRenderDepth++;
    }

    public static void endWorldRender() {
        worldRenderDepth = Math.max(0, worldRenderDepth - 1);
    }

    public static void clearCache() {
        MODEL_SURFACES.clear();
        SPAWN_STATES.clear();
        trackedLevel = null;
        lastStateCleanupTick = Long.MIN_VALUE;
        worldRenderDepth = 0;
    }

    public static void emitFromRenderedModel(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, BakedModel model) {
        if (worldRenderDepth <= 0 || stack.isEmpty()) return;

        WeaponKind kind = WeaponKind.from(stack);
        if (kind == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || minecraft.isPaused()) return;

        ensureLevel(level);
        long gameTime = level.getGameTime();
        int interval = kind.intervalTicks * densityScale(minecraft.options.particles().get());
        SurfaceMesh surfaceMesh = MODEL_SURFACES.computeIfAbsent(model, TrueDemonWeaponParticleEmitter::bakeSurfaceMesh);

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 modelCenter = transformToWorld(poseStack, new Vector3f(0.5F, 0.5F, 0.5F), camera);
        if (!isFinite(modelCenter) || modelCenter.distanceToSqr(minecraft.player.position()) > MAX_DISTANCE_SQR) return;

        cleanupSpawnStates(gameTime);
        int slot = context.ordinal() * 2 + (leftHand ? 1 : 0);
        SpawnState spawnState = SPAWN_STATES.computeIfAbsent(stack, ignored -> new SpawnState());
        spawnState.lastSeenTick = gameTime;
        long previousSpawn = spawnState.lastSpawnTicks[slot];
        if (previousSpawn != Long.MIN_VALUE && gameTime - previousSpawn < interval) return;

        boolean spawned = false;
        for (int particle = 0; particle < kind.particlesPerEmission; particle++) {
            Vector3f modelPoint = surfaceMesh.sample(RANDOM);
            if (modelPoint == null) break;

            Vec3 worldPoint = transformToWorld(poseStack, modelPoint, camera);
            if (!isFinite(worldPoint) || worldPoint.distanceToSqr(minecraft.player.position()) > MAX_DISTANCE_SQR) {
                continue;
            }

            level.addParticle(ModParticleTypes.TRUE_DEMON_WEAPON_PARTICLE.get(),
                    worldPoint.x, worldPoint.y, worldPoint.z,
                    kind.particleScale(context), 0.0D, 0.0D);
            spawned = true;
        }
        if (spawned) spawnState.lastSpawnTicks[slot] = gameTime;
    }

    private static void ensureLevel(ClientLevel level) {
        if (trackedLevel == level) return;
        trackedLevel = level;
        SPAWN_STATES.clear();
        lastStateCleanupTick = Long.MIN_VALUE;
    }

    private static void cleanupSpawnStates(long gameTime) {
        if (lastStateCleanupTick != Long.MIN_VALUE && gameTime - lastStateCleanupTick < STATE_RETENTION_TICKS) return;
        lastStateCleanupTick = gameTime;
        SPAWN_STATES.entrySet().removeIf(entry -> gameTime - entry.getValue().lastSeenTick > STATE_RETENTION_TICKS);
    }

    private static SurfaceMesh bakeSurfaceMesh(BakedModel model) {
        ArrayList<BakedQuad> quads = new ArrayList<>();
        Set<BakedQuad> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        RandomSource random = RandomSource.create();

        collectQuads(model, null, random, quads, seen);
        for (Direction direction : Direction.values()) {
            collectQuads(model, direction, random, quads, seen);
        }

        ArrayList<SurfaceFace> faces = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            SurfaceFace face = SurfaceFace.from(quad);
            if (face != null) faces.add(face);
        }
        return new SurfaceMesh(faces);
    }

    private static void collectQuads(BakedModel model, @Nullable Direction direction, RandomSource random, List<BakedQuad> output, Set<BakedQuad> seen) {
        random.setSeed(42L);
        for (BakedQuad quad : model.getQuads(null, direction, random)) {
            if (seen.add(quad)) output.add(quad);
        }
    }

    private static Vec3 transformToWorld(PoseStack poseStack, Vector3f modelPoint, Camera camera) {
        Vector4f viewPoint = poseStack.last().pose().transform(new Vector4f(modelPoint.x(), modelPoint.y(), modelPoint.z(), 1.0F));
        Vector3f worldOffset = new Vector3f(viewPoint.x(), viewPoint.y(), viewPoint.z()).rotateY((float) Math.PI).rotate(camera.rotation());
        return camera.getPosition().add(worldOffset.x(), worldOffset.y(), worldOffset.z());
    }

    private static boolean isFinite(Vec3 point) {
        return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }

    private static int densityScale(ParticleStatus status) {
        return switch (status) {
            case ALL -> 1;
            case DECREASED -> 2;
            case MINIMAL -> 4;
        };
    }

    private static float triangleArea(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f ab = new Vector3f(b).sub(a);
        Vector3f ac = new Vector3f(c).sub(a);
        return ab.cross(ac).length() * 0.5F;
    }

    private static Vector3f sampleTriangle(Vector3f a, Vector3f b, Vector3f c, RandomSource random) {
        float root = (float) Math.sqrt(random.nextFloat());
        float second = random.nextFloat();
        float wa = 1.0F - root;
        float wb = root * (1.0F - second);
        float wc = root * second;
        return new Vector3f(a.x() * wa + b.x() * wb + c.x() * wc, a.y() * wa + b.y() * wb + c.y() * wc, a.z() * wa + b.z() * wb + c.z() * wc);
    }

    private static final class SpawnState {
        private final long[] lastSpawnTicks = new long[CONTEXT_SLOTS];
        private long lastSeenTick;

        private SpawnState() {
            Arrays.fill(lastSpawnTicks, Long.MIN_VALUE);
        }
    }

    private static final class SurfaceMesh {
        private final SurfaceFace[] faces;
        private final float[] cumulativeAreas;
        private final float totalArea;

        private SurfaceMesh(List<SurfaceFace> sourceFaces) {
            this.faces = sourceFaces.toArray(SurfaceFace[]::new);
            this.cumulativeAreas = new float[this.faces.length];
            float area = 0.0F;
            for (int i = 0; i < this.faces.length; i++) {
                area += this.faces[i].area;
                this.cumulativeAreas[i] = area;
            }
            this.totalArea = area;
        }

        @Nullable
        private Vector3f sample(RandomSource random) {
            if (faces.length == 0 || totalArea <= 1.0e-8F) return null;

            float target = random.nextFloat() * totalArea;
            int index = Arrays.binarySearch(cumulativeAreas, target);
            if (index < 0) index = -index - 1;
            index = Math.min(index, faces.length - 1);
            return faces[index].sample(random);
        }
    }

    private static final class SurfaceFace {
        private final Vector3f v0;
        private final Vector3f v1;
        private final Vector3f v2;
        private final Vector3f v3;
        private final Vector3f normal;
        private final float firstTriangleArea;
        private final float secondTriangleArea;
        private final float area;

        private SurfaceFace(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal, float firstTriangleArea, float secondTriangleArea) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.normal = normal;
            this.firstTriangleArea = firstTriangleArea;
            this.secondTriangleArea = secondTriangleArea;
            this.area = firstTriangleArea + secondTriangleArea;
        }

        @Nullable
        private static SurfaceFace from(BakedQuad quad) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            if (stride < 3) return null;

            Vector3f[] points = new Vector3f[4];
            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * stride;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) return null;
                points[vertex] = new Vector3f(x, y, z);
            }

            float firstArea = triangleArea(points[0], points[1], points[2]);
            float secondArea = triangleArea(points[0], points[2], points[3]);
            if (firstArea + secondArea <= 1.0e-8F) return null;

            Vector3f normal = new Vector3f(points[1]).sub(points[0]).cross(new Vector3f(points[2]).sub(points[0]));
            if (normal.lengthSquared() <= 1.0e-10F) return null;
            normal.normalize();

            Direction direction = quad.getDirection();
            float facing = normal.x() * direction.getStepX() + normal.y() * direction.getStepY() + normal.z() * direction.getStepZ();
            if (facing < 0.0F) normal.negate();

            return new SurfaceFace(points[0], points[1], points[2], points[3], normal, firstArea, secondArea);
        }

        private Vector3f sample(RandomSource random) {
            Vector3f point = random.nextFloat() * area < firstTriangleArea ? sampleTriangle(v0, v1, v2, random) : sampleTriangle(v0, v2, v3, random);
            return point.fma(SURFACE_OFFSET, normal);
        }
    }

    private enum WeaponKind {
        BOW(1, 3),
        ARROW(1, 2);

        private final int intervalTicks;
        private final int particlesPerEmission;

        WeaponKind(int intervalTicks, int particlesPerEmission) {
            this.intervalTicks = intervalTicks;
            this.particlesPerEmission = particlesPerEmission;
        }

        private double particleScale(ItemDisplayContext context) {
            return switch (context) {
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> this == BOW ? 0.28D : 0.24D;
                case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> this == BOW ? 0.78D : 0.62D;
                default -> this == BOW ? 1.0D : 0.78D;
            };
        }

        @Nullable
        private static WeaponKind from(ItemStack stack) {
            if (stack.is(ModWeapons.MANAITABOW.get())) return BOW;
            if (stack.is(ModWeapons.JUDGMENT_OF_AURORA.get())) return ARROW;
            return null;
        }
    }
}
