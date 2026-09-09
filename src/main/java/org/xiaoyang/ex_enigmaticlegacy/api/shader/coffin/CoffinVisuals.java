package org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.CoffinPacket;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash.BladeRenderState;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT)
public final class CoffinVisuals {
    private static final Map<UUID, CoffinPacket.State> ACTIVE = new LinkedHashMap<>();
    static java.util.Collection<CoffinPacket.State> states() { return ACTIVE.values(); }
    private static final ResourceLocation PARTICLES = new ResourceLocation(Exe.MODID, "textures/coffin/particles.png");
    private static final ResourceLocation LASER = new ResourceLocation(Exe.MODID, "textures/coffin/laser.png");
    private static final ResourceLocation SPIKES = new ResourceLocation(Exe.MODID, "textures/coffin/fire1.png");
    private record Quad(CoffinAnimation.Particle particle, float[] values, Matrix4f transform, Vector3f cameraLocal, float yaw, float depth) {}
    private static final ArrayList<Quad> QUADS = new ArrayList<>();
    private static final BufferBuilder BUFFER = new BufferBuilder(512 * 1024);
    private static final Matrix4f VIEW = new Matrix4f(), PROJECTION = new Matrix4f();
    private static CoffinAnimation animation;
    private static boolean failed, viewReady;
    private static ClientLevel lastLevel;
    public static long receivedStarts, renderedQuads, resourceLoads, preparationNanos, lastRenderNanos;
    public static boolean resourcesReady() { return animation != null && !failed; }

    @Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void setup(FMLClientSetupEvent event) { CoffinPacket.clientReceiver = CoffinVisuals::accept; }
        public static void reload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new SimplePreparableReloadListener<CoffinAnimation>() {
                @Override protected CoffinAnimation prepare(ResourceManager manager, ProfilerFiller profiler) {
                    long start = System.nanoTime();
                    try { return CoffinAnimation.load(manager); }
                    catch (Exception error) {
                        LogUtils.getLogger().error("Failed to prepare black coffin animation", error);
                        return null;
                    } finally { preparationNanos = System.nanoTime() - start; }
                }
                @Override protected void apply(CoffinAnimation prepared, ResourceManager manager, ProfilerFiller profiler) {
                    animation = prepared; failed = prepared == null; resourceLoads++;
                    if (failed) return;
                    var textures = Minecraft.getInstance().getTextureManager();
                    for (var texture : new ResourceLocation[]{PARTICLES, LASER, SPIKES}) textures.getTexture(texture).getId();
                    LogUtils.getLogger().info("Black coffin prepared in {} ms (reload {})", preparationNanos / 1_000_000, resourceLoads);
                }
            });
        }
    }

    private static void accept(CoffinPacket.State state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().location().equals(state.dimension())) return;
        if (lastLevel != mc.level) { ACTIVE.clear(); lastLevel = mc.level; }
        if (state.ended()) ACTIVE.remove(state.target());
        else { ACTIVE.put(state.target(), state); receivedStarts++; }
    }

    public static boolean locked(Entity entity) {
        var state = ACTIVE.get(entity.getUUID());
        return state != null && entity.level() == Minecraft.getInstance().level && age(state, 0) < CoffinExecutions.duration(state.form());
    }

    private static float age(CoffinPacket.State state, float partial) {
        return (Minecraft.getInstance().level.getGameTime() - state.startTime()) + partial;
    }

    public static void hold(Entity entity) {
        var state = ACTIVE.get(entity.getUUID());
        if (state == null) return;
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPos(state.anchor().x, state.anchor().y, state.anchor().z);
        entity.xo = entity.getX(); entity.yo = entity.getY(); entity.zo = entity.getZ();
        entity.setYRot(state.yaw()); entity.yRotO = state.yaw();
        entity.setXRot(state.pitch()); entity.xRotO = state.pitch();
        if (entity instanceof LivingEntity living) {
            living.setYHeadRot(state.yaw()); living.yHeadRotO = state.yaw();
            living.setYBodyRot(state.yaw()); living.yBodyRotO = state.yaw();
            living.walkAnimation.setSpeed(0);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastLevel) { ACTIVE.clear(); lastLevel = mc.level; }
        if (mc.level == null) return;
        ACTIVE.values().removeIf(state -> age(state, 0) > CoffinExecutions.duration(state.form()) + 40);
        if (mc.player != null && locked(mc.player)) hold(mc.player);
    }

    @SubscribeEvent
    public static void input(MovementInputUpdateEvent event) {
        if (!locked(event.getEntity())) return;
        var input = event.getInput();
        input.forwardImpulse = input.leftImpulse = 0;
        input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START && mc.player != null && locked(mc.player)) hold(mc.player);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && locked(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void living(RenderLivingEvent.Pre<?, ?> event) {
        var state = ACTIVE.get(event.getEntity().getUUID());
        if (state != null && age(state, event.getPartialTick()) >= CoffinExecutions.hideAt(state.form())) event.setCanceled(true);
    }

    public static void captureView(RenderLevelStageEvent event) {
        VIEW.set(event.getPoseStack().last().pose()); PROJECTION.set(event.getProjectionMatrix()); viewReady = true;
    }

    public static void render(RenderLevelStageEvent event) {
        if (!viewReady) return;
        viewReady = false;
        if (ACTIVE.isEmpty() || !resourcesReady() || EXECoreShaders.coffin == null) return;
        long renderStart = System.nanoTime();
        Minecraft mc = Minecraft.getInstance();
        BladeRenderState saved = new BladeRenderState();
        try {
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest(); RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.disableCull(); RenderSystem.enableBlend(); RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
            EXECoreShaders.coffin.safeGetUniform("BladeView").set(VIEW);
            EXECoreShaders.coffin.safeGetUniform("BladeProjection").set(PROJECTION);
            RenderSystem.setShader(() -> EXECoreShaders.coffin);
            EXECoreShaders.coffin.setSampler("ParticleTexture", mc.getTextureManager().getTexture(PARTICLES).getId());
            EXECoreShaders.coffin.setSampler("LaserTexture", mc.getTextureManager().getTexture(LASER).getId());
            EXECoreShaders.coffin.setSampler("SpikeTexture", mc.getTextureManager().getTexture(SPIKES).getId());
            QUADS.clear();
            for (var state : ACTIVE.values()) {
                if(state.form()!=0) continue;
                float age = age(state, event.getPartialTick());
                if (age >= 0 && age < CoffinExecutions.DURATION && state.anchor().distanceToSqr(event.getCamera().getPosition()) < 256 * 256)
                    collect(state, age / 20f, event);
            }
            QUADS.sort(Comparator.comparingDouble(Quad::depth));
            BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            for (var quad : QUADS) draw(quad, event);
            BufferUploader.drawWithShader(BUFFER.end());
        } catch (Exception error) {
            if (BUFFER.building()) BUFFER.discard();
            failed = true;
            LogUtils.getLogger().error("Black coffin renderer failed; reload resources to retry", error);
        } finally { QUADS.clear(); saved.restore(); lastRenderNanos = System.nanoTime() - renderStart; }
    }

    private static void collect(CoffinPacket.State state, float seconds, RenderLevelStageEvent event) {
        float frameTime = Math.min(seconds * animation.fps, animation.frames.length - 1);
        int frameIndex = (int) frameTime;
        float blend = frameTime - frameIndex;
        var a = animation.frames[frameIndex];
        var b = animation.frames[Math.min(frameIndex + 1, animation.frames.length - 1)];
        float yaw = (float) Math.toRadians(-state.yaw());
        Vec3 camera = event.getCamera().getPosition();
        Vector3f cameraLocal = new Vector3f((float) (camera.x - state.anchor().x),
            (float) (camera.y - state.anchor().y), (float) (camera.z - state.anchor().z)).rotateY(-yaw)
            .div(state.width(), state.height(), state.width());
        Matrix4f transform = new Matrix4f().translation((float) (state.anchor().x - camera.x),
            (float) (state.anchor().y - camera.y), (float) (state.anchor().z - camera.z))
            .rotateY(yaw).scale(state.width(), state.height(), state.width());
        Vector3f point = new Vector3f();
        for (int i = 0; i < a.ids().length; i++) {
            int id = a.ids()[i];
            var particle = animation.particles[id];
            float[] value = new float[10];
            int offset = i * 10, next = b.offsets()[id];
            for (int k = 0; k < 10; k++) value[k] = next < 0 ? a.values()[offset + k]
                : net.minecraft.util.Mth.lerp(blend, a.values()[offset + k], b.values()[next + k]);
            if (next < 0) value[9] *= 1 - blend;
            if (value[9] < 0.003f || value[3] < 0.0001f || value[4] < 0.0001f) continue;
            point.set(value[0], value[1], value[2]);
            transform.transformPosition(point); VIEW.transformPosition(point);
            QUADS.add(new Quad(particle, value, transform, cameraLocal, yaw, point.z));
        }
    }

    private static void draw(Quad quad, RenderLevelStageEvent event) {
            var particle = quad.particle();
            float[] value = quad.values();
            Vector3f right = new Vector3f(), up = new Vector3f(), toward = new Vector3f(), point = new Vector3f();
            if (particle.mode() == 0) {
                right.set(1, 0, 0).rotate(particle.orientation()); up.set(0, 1, 0).rotate(particle.orientation());
            } else if (particle.mode() == 1) {
                right.set(1, 0, 0).rotate(event.getCamera().rotation()).rotateY(-quad.yaw());
                up.set(0, 1, 0).rotate(event.getCamera().rotation()).rotateY(-quad.yaw());
            } else {
                toward.set(quad.cameraLocal()).sub(value[0], value[1], value[2]);
                if (particle.mode() == 2) { up.set(0, 1, 0); right.set(toward.z, 0, -toward.x); }
                else {
                    right.set(particle.direction());
                    up.set(toward).cross(right);
                }
                if (right.lengthSquared() < 1e-8) right.set(1, 0, 0);
                if (up.lengthSquared() < 1e-8) up.set(0, 1, 0);
                right.normalize(); up.normalize();
            }
            if (particle.mode() != 0) {
                float cos = (float) Math.cos(value[5]), sin = (float) Math.sin(value[5]);
                point.set(right).mul(cos).fma(sin, up);
                up.mul(cos).fma(-sin, right); right.set(point);
            }
            right.mul(value[3]); up.mul(value[4]);
            for (int vertex = 0; vertex < 4; vertex++) {
                float x = vertex == 0 || vertex == 3 ? -1 : 1;
                float y = vertex < 2 ? 1 : -1;
                point.set(value[0], value[1], value[2]).fma(x, right).fma(y, up);
                quad.transform().transformPosition(point);
                BUFFER.vertex(point.x, point.y, point.z).color(value[6], value[7], value[8], value[9])
                    .uv(particle.material() * 2 + (x < 0 ? particle.u0() : particle.u1()),
                        y > 0 ? particle.v0() : particle.v1()).endVertex();
            }
            renderedQuads++;
    }

    private CoffinVisuals() {}
}
