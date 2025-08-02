package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.block.EXEShaders;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.*;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CosmicBeamRenderer {

    private static final Random RANDOM = new Random();

    public static class CosmicBeam {
        public Vec3 startPos;           // 起始位置（玩家前方1格）
        public Vec3 direction;          // 射线方向
        public float maxLength;         // 最大长度
        public float currentLength;     // 当前长度
        public float maxWidth;          // 最大宽度
        public float currentWidth;      // 当前宽度
        public float time;              // 当前时间
        public float maxTime;           // 总持续时间
        public CosmicPhase phase;        // 当前阶段

        public float chargeTime;        // 充能时间
        public float maxChargeTime;     // 最大充能时间
        public List<EnergyRing> chargeRings; // 充能环效果

        public float orbSize;           // 圆球大小
        public float maxOrbSize;        // 最大圆球大小
        public float orbPulse;          // 圆球脉冲效果

        public List<ElectricArc> electricArcs;

        public List<EnergyParticle> particles;

        public float[] waveOffsets;     // 波动偏移
        public float waveIntensity;     // 波动强度

        private static final float CHARGE_TIME = 1.2f;      // 充能阶段时间
        private static final float EXPAND_TIME = 0.6f;      // 扩张阶段时间
        private static final float SUSTAIN_TIME = 2.5f;     // 持续阶段时间
        private static final float SHRINK_TIME = 0.8f;      // 收缩阶段时间

        public enum CosmicPhase {
            CHARGING,       // 充能阶段
            EXPANDING,      // 扩张阶段
            SUSTAINING,     // 持续阶段
            SHRINKING,      // 收缩阶段
            FINISHED        // 结束
        }

        public CosmicBeam(Vec3 startPos, Vec3 direction, float length, float width) {
            this.startPos = startPos;
            this.direction = direction.normalize();
            this.maxLength = length;
            this.maxWidth = width;
            this.maxOrbSize = width * 0.6f;
            this.maxChargeTime = CHARGE_TIME;
            this.time = 0;
            this.chargeTime = 0;
            this.maxTime = CHARGE_TIME + EXPAND_TIME + SUSTAIN_TIME + SHRINK_TIME;
            this.phase = CosmicPhase.CHARGING;
            this.currentLength = 0;
            this.currentWidth = 0;
            this.orbSize = 0;
            this.orbPulse = 0;

            this.chargeRings = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                chargeRings.add(new EnergyRing(startPos, i * 0.3f));
            }

            this.electricArcs = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                electricArcs.add(new ElectricArc(startPos, direction, length));
            }

            this.particles = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                particles.add(new EnergyParticle(startPos, direction));
            }

            this.waveOffsets = new float[10];
            for (int i = 0; i < waveOffsets.length; i++) {
                waveOffsets[i] = RANDOM.nextFloat() * 360;
            }
            this.waveIntensity = 0;
        }

        public void update(float deltaTime) {
            time += deltaTime;

            if (time <= CHARGE_TIME) {
                phase = CosmicPhase.CHARGING;
                chargeTime += deltaTime;
                float chargeProgress = chargeTime / maxChargeTime;
                float easedProgress = easeOutQuart(chargeProgress);

                orbSize = maxOrbSize * 0.3f + maxOrbSize * 0.7f * easedProgress;
                orbPulse = (float)Math.sin(time * 8) * 0.3f * easedProgress;

                for (EnergyRing ring : chargeRings) {
                    ring.update(deltaTime, easedProgress);
                }

            } else if (time <= CHARGE_TIME + EXPAND_TIME) {
                if (phase == CosmicPhase.CHARGING) {
                    playLaserFireSound();
                }
                phase = CosmicPhase.EXPANDING;

                float expandProgress = (time - CHARGE_TIME) / EXPAND_TIME;
                float easedProgress = easeOutCubic(expandProgress);

                currentLength = maxLength * easedProgress;
                currentWidth = maxWidth * easedProgress;
                orbSize = maxOrbSize * (1.0f + orbPulse);
                waveIntensity = maxWidth * 0.2f * easedProgress;

                updateEffects(deltaTime);

            } else if (time <= CHARGE_TIME + EXPAND_TIME + SUSTAIN_TIME) {
                phase = CosmicPhase.SUSTAINING;
                currentLength = maxLength;
                currentWidth = maxWidth;
                orbSize = maxOrbSize * (1.0f + (float)Math.sin(time * 6) * 0.1f);
                waveIntensity = maxWidth * 0.15f;

                updateEffects(deltaTime);

            } else if (time <= maxTime) {
                phase = CosmicPhase.SHRINKING;
                float shrinkProgress = (time - CHARGE_TIME - EXPAND_TIME - SUSTAIN_TIME) / SHRINK_TIME;
                float easedProgress = easeInCubic(shrinkProgress);

                currentLength = maxLength * (1.0f - easedProgress);
                currentWidth = maxWidth * (1.0f - easedProgress);
                orbSize = maxOrbSize * (1.0f - easedProgress);
                waveIntensity = maxWidth * 0.15f * (1.0f - easedProgress);

                updateEffects(deltaTime);

            } else {
                phase = CosmicPhase.FINISHED;
                currentLength = 0;
                currentWidth = 0;
                orbSize = 0;
                waveIntensity = 0;
            }

            for (int i = 0; i < waveOffsets.length; i++) {
                waveOffsets[i] += deltaTime * 200 * (i + 1);
            }
        }

        private void updateEffects(float deltaTime) {
            for (ElectricArc arc : electricArcs) {
                arc.update(deltaTime);
            }

            for (EnergyParticle particle : particles) {
                particle.update(deltaTime, direction, currentLength);
            }
        }

        private void playLaserFireSound() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.level.playLocalSound(
                        startPos.x, startPos.y, startPos.z,
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.BLOCKS,
                        3.0f,
                        2.0f,
                        false
                );

                mc.level.playLocalSound(
                        startPos.x, startPos.y, startPos.z,
                        SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.BLOCKS,
                        2.0f,
                        0.5f,
                        false
                );
            }
        }

        private float easeOutCubic(float t) {
            return 1 - (float)Math.pow(1 - t, 3);
        }

        private float easeInCubic(float t) {
            return t * t * t;
        }

        private float easeOutQuart(float t) {
            return 1 - (float)Math.pow(1 - t, 4);
        }

        public boolean isActive() {
            return phase != CosmicPhase.FINISHED;
        }

        public float getProgress() {
            return Mth.clamp(time / maxTime, 0.0f, 1.0f);
        }

        public float getAlpha() {
            switch (phase) {
                case CHARGING:
                    return Math.min(1.0f, chargeTime / (maxChargeTime * 0.5f));
                case EXPANDING:
                    return 1.0f;
                case SUSTAINING:
                    return 1.0f;
                case SHRINKING:
                    float shrinkProgress = (time - CHARGE_TIME - EXPAND_TIME - SUSTAIN_TIME) / SHRINK_TIME;
                    return 1.0f - shrinkProgress;
                default:
                    return 0.0f;
            }
        }

        public Vec3 getWaveOffset(float distance, Vec3 right, Vec3 up) {
            if (waveIntensity <= 0) return Vec3.ZERO;

            float normalizedDistance = distance / maxLength;
            float offsetX = 0, offsetY = 0;

            for (int i = 0; i < 3; i++) {
                float wave = (float)Math.sin((waveOffsets[i] + normalizedDistance * 360 * (i + 1)) * Math.PI / 180);
                float intensity = waveIntensity * (1.0f - normalizedDistance) * 0.1f / (i + 1);
                offsetX += wave * intensity;
                offsetY += (float)Math.cos((waveOffsets[i + 3] + normalizedDistance * 360 * (i + 1)) * Math.PI / 180) * intensity;
            }

            return right.scale(offsetX).add(up.scale(offsetY));
        }

        public Vec3 getPointOnBeam(float distance) {
            return startPos.add(direction.scale(distance));
        }

        public Vec3 getEndPos() {
            return getPointOnBeam(currentLength);
        }
    }

    // 充能环效果
    public static class EnergyRing {
        public Vec3 center;
        public float radius;
        public float maxRadius;
        public float rotation;
        public float alpha;
        public float delay;
        public float life;

        public EnergyRing(Vec3 center, float delay) {
            this.center = center;
            this.delay = delay;
            this.radius = 0;
            this.maxRadius = 3.0f;
            this.rotation = 0;
            this.alpha = 0;
            this.life = 0;
        }

        public void update(float deltaTime, float chargeProgress) {
            life += deltaTime;
            if (life > delay) {
                float progress = Math.min(1.0f, (life - delay) * 2);
                radius = maxRadius * progress * chargeProgress;
                alpha = (1.0f - progress) * chargeProgress;
                rotation += deltaTime * 180;
            }
        }
    }

    // 电光效果
    public static class ElectricArc {
        public Vec3 start, end;
        public List<Vec3> points;
        public float life, maxLife;
        public float intensity;

        public ElectricArc(Vec3 start, Vec3 direction, float length) {
            this.start = start;
            this.end = start.add(direction.scale(length));
            this.maxLife = 0.1f + RANDOM.nextFloat() * 0.1f;
            this.life = 0;
            this.intensity = RANDOM.nextFloat();
            generatePoints();
        }

        private void generatePoints() {
            points = new ArrayList<>();
            int segments = 8;
            for (int i = 0; i <= segments; i++) {
                float t = (float)i / segments;
                Vec3 point = start.lerp(end, t);

                if (i > 0 && i < segments) {
                    Vec3 offset = new Vec3(
                            (RANDOM.nextFloat() - 0.5f) * 2,
                            (RANDOM.nextFloat() - 0.5f) * 2,
                            (RANDOM.nextFloat() - 0.5f) * 2
                    ).scale(0.5f);
                    point = point.add(offset);
                }
                points.add(point);
            }
        }

        public void update(float deltaTime) {
            life += deltaTime;
            if (life >= maxLife) {
                life = 0;
                generatePoints();
            }
        }

        public float getAlpha() {
            return (1.0f - life / maxLife) * intensity;
        }
    }

    public static class EnergyParticle {
        public Vec3 position;
        public Vec3 velocity;
        public float life, maxLife;
        public float size;
        public Vec3 startPos;

        public EnergyParticle(Vec3 startPos, Vec3 direction) {
            this.startPos = startPos;
            this.position = startPos;
            this.velocity = direction.scale(RANDOM.nextFloat() * 20 + 10);
            this.maxLife = RANDOM.nextFloat() * 2 + 1;
            this.life = RANDOM.nextFloat() * maxLife;
            this.size = RANDOM.nextFloat() * 0.3f + 0.1f;
        }

        public void update(float deltaTime, Vec3 direction, float beamLength) {
            life += deltaTime;
            if (life >= maxLife) {
                // 重置粒子
                life = 0;
                position = startPos.add(new Vec3(
                        (RANDOM.nextFloat() - 0.5f) * 2,
                        (RANDOM.nextFloat() - 0.5f) * 2,
                        (RANDOM.nextFloat() - 0.5f) * 2
                ));
                velocity = direction.scale(RANDOM.nextFloat() * 30 + 15);
            } else {
                position = position.add(velocity.scale(deltaTime));
            }
        }

        public float getAlpha() {
            float progress = life / maxLife;
            if (progress < 0.2f) return progress / 0.2f;
            else return 1.0f - (progress - 0.2f) / 0.8f;
        }
    }

    private static final List<CosmicBeam> CosmicBeams = new ArrayList<>();

    private static final RenderType LASER_BEAM_TYPE = RenderType.create(
            "laser_beam",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/particle/laser_beam.png"), false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final RenderType LASER_ORB_TYPE = RenderType.create(
            "laser_orb",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/particle/laser_orb.png"), false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final RenderType LASER_CORE_TYPE = RenderType.create(
            "laser_core",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/particle/laser_core.png"), false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    // 橙色外层光晕
    private static final RenderType LASER_ORANGE_GLOW_TYPE = RenderType.create(
            "laser_orange_glow",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/particle/laser_beam.png"), false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final RenderType ENERGY_RING_TYPE = RenderType.create(
            "energy_ring",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final RenderType ELECTRIC_ARC_TYPE = RenderType.create(
            "electric_arc",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> EXEShaders.cosmicShader))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
                    .createCompositeState(false)
    );

    public static void createLaserFromPlayer(Player player, float length, float width) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 startPos = playerPos.add(lookDir.scale(2.0));

        createLaser(startPos, lookDir, length, width);
    }

    public static void createLaser(Vec3 startPos, Vec3 direction, float length, float width) {
        CosmicBeam laser = new CosmicBeam(startPos, direction, length, width);
        CosmicBeams.add(laser);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.playLocalSound(
                    startPos.x, startPos.y, startPos.z,
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS,
                    2.0f,
                    0.5f,
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (CosmicBeams.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float deltaTime = event.getPartialTick() * 0.016f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        for (CosmicBeam laser : CosmicBeams) {
            if (laser.isActive()) {
                if (laser.phase == CosmicBeam.CosmicPhase.CHARGING) {
                    renderChargeRings(poseStack, bufferSource, laser, cameraPos);
                }
                renderLaserOrb(poseStack, bufferSource, laser, cameraPos);

                // 渲染橙色外层光晕（最外层）
                renderLaserOrangeGlow(poseStack, bufferSource, laser, cameraPos);

                // 渲染激光射线主体（外层光晕）
                renderLaserGlow(poseStack, bufferSource, laser, cameraPos);

                // 渲染激光射线主体
                renderCosmicBeam(poseStack, bufferSource, laser, cameraPos);

                // 渲染激光核心
                renderLaserCore(poseStack, bufferSource, laser, cameraPos);

                // 渲染电光效果
                renderElectricArcs(poseStack, bufferSource, laser, cameraPos);

                // 渲染能量粒子
                renderEnergyParticles(poseStack, bufferSource, laser, cameraPos);
            }
        }

        bufferSource.endBatch();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        Iterator<CosmicBeam> it = CosmicBeams.iterator();
        while (it.hasNext()) {
            CosmicBeam laser = it.next();
            laser.update(deltaTime);
            if (!laser.isActive()) {
                it.remove();
            }
        }
    }

    // 渲染充能环
    private static void renderChargeRings(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                          CosmicBeam laser, Vec3 cameraPos) {
        VertexConsumer buffer = bufferSource.getBuffer(ENERGY_RING_TYPE);

        for (EnergyRing ring : laser.chargeRings) {
            if (ring.alpha > 0.01f) {
                renderEnergyRing(poseStack, buffer, ring, cameraPos);
            }
        }
    }

    private static void renderEnergyRing(PoseStack poseStack, VertexConsumer buffer,
                                         EnergyRing ring, Vec3 cameraPos) {
        poseStack.pushPose();

        Vec3 relativePos = ring.center.subtract(cameraPos);
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(ring.rotation));

        Matrix4f matrix = poseStack.last().pose();

        int segments = 24;
        int color = ((int)(ring.alpha * 255) << 24) | (0 << 16) | (150 << 8) | 255; // 青色

        for (int i = 0; i <= segments; i++) {
            float angle = (float)(i * 2 * Math.PI / segments);
            float x = Mth.cos(angle) * ring.radius;
            float z = Mth.sin(angle) * ring.radius;

            buffer.vertex(matrix, x * 0.8f, 0, z * 0.8f).color(color).endVertex();
            buffer.vertex(matrix, x, 0, z).color(color).endVertex();
        }

        poseStack.popPose();
    }

    private static void renderLaserOrangeGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                              CosmicBeam laser, Vec3 cameraPos) {
        if (laser.currentLength <= 0.01f || laser.currentWidth <= 0.01f) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_ORANGE_GLOW_TYPE);

        poseStack.pushPose();

        Vec3 relativeStart = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);

        Vec3 forward = laser.direction;
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Matrix4f matrix = poseStack.last().pose();

        float alpha = laser.getAlpha() * 0.2f;
        int color = ((int)(alpha * 255) << 24) | (255 << 16) | (150 << 8) | 100; // 橙色

        float orangeGlowWidth = laser.currentWidth * 3.5f;
        renderBeamGeometry(matrix, buffer, forward, right, up, laser, orangeGlowWidth, color);

        poseStack.popPose();
    }

    private static void renderLaserGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                        CosmicBeam laser, Vec3 cameraPos) {
        if (laser.currentLength <= 0.01f || laser.currentWidth <= 0.01f) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_BEAM_TYPE);

        poseStack.pushPose();

        Vec3 relativeStart = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);

        Vec3 forward = laser.direction;
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Matrix4f matrix = poseStack.last().pose();

        float alpha = laser.getAlpha() * 0.3f;
        int color = ((int)(alpha * 255) << 24) | (100 << 16) | (200 << 8) | 255;

        float glowWidth = laser.currentWidth * 2.5f;
        renderBeamGeometry(matrix, buffer, forward, right, up, laser, glowWidth, color);

        poseStack.popPose();
    }

    private static void renderCosmicBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                        CosmicBeam laser, Vec3 cameraPos) {
        if (laser.currentLength <= 0.01f || laser.currentWidth <= 0.01f) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_BEAM_TYPE);

        poseStack.pushPose();

        Vec3 relativeStart = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);

        Vec3 forward = laser.direction;
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Matrix4f matrix = poseStack.last().pose();

        // 主体颜色：蓝色
        float alpha = laser.getAlpha() * 0.8f;
        int color = ((int)(alpha * 255) << 24) | (50 << 16) | (150 << 8) | 255;

        renderBeamGeometry(matrix, buffer, forward, right, up, laser, laser.currentWidth, color);

        poseStack.popPose();
    }

    // 渲染激光核心
    private static void renderLaserCore(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                        CosmicBeam laser, Vec3 cameraPos) {
        if (laser.currentLength <= 0.01f || laser.currentWidth <= 0.01f) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_CORE_TYPE);

        poseStack.pushPose();

        Vec3 relativeStart = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);

        Vec3 forward = laser.direction;
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Matrix4f matrix = poseStack.last().pose();

        // 核心颜色：纯白
        float alpha = laser.getAlpha();
        int color = ((int)(alpha * 255) << 24) | (255 << 16) | (255 << 8) | 255;

        float coreWidth = laser.currentWidth * 0.4f;
        renderBeamGeometry(matrix, buffer, forward, right, up, laser, coreWidth, color);

        poseStack.popPose();
    }

    private static void renderBeamGeometry(Matrix4f matrix, VertexConsumer buffer,
                                           Vec3 forward, Vec3 right, Vec3 up,
                                           CosmicBeam laser, float width, int color) {
        int segments = 20;
        float length = laser.currentLength;

        for (int i = 0; i < segments; i++) {
            float t1 = (float)i / segments;
            float t2 = (float)(i + 1) / segments;

            float dist1 = t1 * length;
            float dist2 = t2 * length;

            Vec3 pos1 = forward.scale(dist1);
            Vec3 pos2 = forward.scale(dist2);

            Vec3 wave1 = laser.getWaveOffset(dist1, right, up);
            Vec3 wave2 = laser.getWaveOffset(dist2, right, up);

            pos1 = pos1.add(wave1);
            pos2 = pos2.add(wave2);

            Vec3 p1 = pos1.add(right.scale(-width));
            Vec3 p2 = pos1.add(right.scale(width));
            Vec3 p3 = pos2.add(right.scale(width));
            Vec3 p4 = pos2.add(right.scale(-width));

            buffer.vertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z).color(color).uv(0, t1).endVertex();
            buffer.vertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z).color(color).uv(1, t1).endVertex();
            buffer.vertex(matrix, (float)p3.x, (float)p3.y, (float)p3.z).color(color).uv(1, t2).endVertex();
            buffer.vertex(matrix, (float)p4.x, (float)p4.y, (float)p4.z).color(color).uv(0, t2).endVertex();

            Vec3 vp1 = pos1.add(up.scale(-width));
            Vec3 vp2 = pos1.add(up.scale(width));
            Vec3 vp3 = pos2.add(up.scale(width));
            Vec3 vp4 = pos2.add(up.scale(-width));

            buffer.vertex(matrix, (float)vp1.x, (float)vp1.y, (float)vp1.z).color(color).uv(0, t1).endVertex();
            buffer.vertex(matrix, (float)vp2.x, (float)vp2.y, (float)vp2.z).color(color).uv(1, t1).endVertex();
            buffer.vertex(matrix, (float)vp3.x, (float)vp3.y, (float)vp3.z).color(color).uv(1, t2).endVertex();
            buffer.vertex(matrix, (float)vp4.x, (float)vp4.y, (float)vp4.z).color(color).uv(0, t2).endVertex();
        }
    }

    private static void renderLaserOrb(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                       CosmicBeam laser, Vec3 cameraPos) {
        if (laser.orbSize <= 0.01f) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_ORB_TYPE);

        poseStack.pushPose();

        Vec3 relativePos = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());

        float scale = laser.orbSize;
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();

        float alpha = laser.getAlpha();
        int baseColor;
        if (laser.phase == CosmicBeam.CosmicPhase.CHARGING) {
            baseColor = ((int)(alpha * 255) << 24) | (0 << 16) | (200 << 8) | 255; // 青色
        } else {
            baseColor = ((int)(alpha * 255) << 24) | (200 << 16) | (230 << 8) | 255; // 白蓝色
        }

        buffer.vertex(matrix, -1, -1, 0).color(baseColor).uv(0, 0).endVertex();
        buffer.vertex(matrix, 1, -1, 0).color(baseColor).uv(1, 0).endVertex();
        buffer.vertex(matrix, 1, 1, 0).color(baseColor).uv(1, 1).endVertex();
        buffer.vertex(matrix, -1, 1, 0).color(baseColor).uv(0, 1).endVertex();

        poseStack.popPose();
    }

    private static void renderElectricArcs(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                           CosmicBeam laser, Vec3 cameraPos) {
        if (laser.phase == CosmicBeam.CosmicPhase.CHARGING || laser.phase == CosmicBeam.CosmicPhase.FINISHED) return;

        VertexConsumer buffer = bufferSource.getBuffer(ELECTRIC_ARC_TYPE);

        poseStack.pushPose();

        Vec3 relativeStart = laser.startPos.subtract(cameraPos);
        poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z);

        Matrix4f matrix = poseStack.last().pose();

        for (ElectricArc arc : laser.electricArcs) {
            float alpha = arc.getAlpha() * laser.getAlpha() * 0.7f;
            if (alpha > 0.01f) {
                int color = ((int)(alpha * 255) << 24) | (255 << 16) | (255 << 8) | 200; // 电光白色

                for (int i = 0; i < arc.points.size() - 1; i++) {
                    Vec3 p1 = arc.points.get(i);
                    Vec3 p2 = arc.points.get(i + 1);

                    buffer.vertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z).color(color).normal(0, 1, 0).endVertex();
                    buffer.vertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z).color(color).normal(0, 1, 0).endVertex();
                }
            }
        }

        poseStack.popPose();
    }

    private static void renderEnergyParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                              CosmicBeam laser, Vec3 cameraPos) {
        if (laser.phase == CosmicBeam.CosmicPhase.CHARGING || laser.phase == CosmicBeam.CosmicPhase.FINISHED) return;

        VertexConsumer buffer = bufferSource.getBuffer(LASER_ORB_TYPE);

        for (EnergyParticle particle : laser.particles) {
            float alpha = particle.getAlpha() * laser.getAlpha() * 0.6f;
            if (alpha > 0.01f) {
                renderEnergyParticle(poseStack, buffer, particle, cameraPos, alpha);
            }
        }
    }

    private static void renderEnergyParticle(PoseStack poseStack, VertexConsumer buffer,
                                             EnergyParticle particle, Vec3 cameraPos, float alpha) {
        poseStack.pushPose();

        Vec3 relativePos = particle.position.subtract(cameraPos);
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());

        float scale = particle.size;
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();

        int color = ((int)(alpha * 255) << 24) | (255 << 16) | (200 << 8) | 150; // 金黄色粒子

        buffer.vertex(matrix, -1, -1, 0).color(color).uv(0, 0).endVertex();
        buffer.vertex(matrix, 1, -1, 0).color(color).uv(1, 0).endVertex();
        buffer.vertex(matrix, 1, 1, 0).color(color).uv(1, 1).endVertex();
        buffer.vertex(matrix, -1, 1, 0).color(color).uv(0, 1).endVertex();

        poseStack.popPose();
    }

    public static void clearAllLasers() {
        CosmicBeams.clear();
    }

    public static int getActiveLaserCount() {
        return CosmicBeams.size();
    }
}