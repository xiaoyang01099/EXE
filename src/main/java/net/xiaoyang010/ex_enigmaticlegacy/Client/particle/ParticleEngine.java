package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ParticleEngine {
    public static ParticleEngine instance = new ParticleEngine();
    public static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/misc/particles.png");
    public static final ResourceLocation PARTICLE_TEXTURE_2 = new ResourceLocation("ex_enigmaticlegacy", "textures/misc/particles2.png");
    protected Level worldObj;
    private HashMap<Integer, ArrayList<Particle>>[] particles = new HashMap[]{
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
    };
    private Random rand = new Random();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        Camera camera = event.getCamera();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int dim = mc.level.dimension().location().hashCode();

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);

        RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE);
        renderParticleLayer(poseStack, camera, partialTick, dim, 0, true);
        renderParticleLayer(poseStack, camera, partialTick, dim, 1, false);

        RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE_2);
        renderParticleLayer(poseStack, camera, partialTick, dim, 2, true);
        renderParticleLayer(poseStack, camera, partialTick, dim, 3, false);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void renderParticleLayer(PoseStack poseStack, Camera camera, float partialTick,
                                     int dim, int layer, boolean additiveBlend) {
        if (!this.particles[layer].containsKey(dim)) return;

        ArrayList<Particle> parts = this.particles[layer].get(dim);
        if (parts.isEmpty()) return;

        poseStack.pushPose();

        if (additiveBlend) {
            RenderSystem.blendFunc(770, 1);
        } else {
            RenderSystem.defaultBlendFunc();
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        for (int j = 0; j < parts.size(); j++) {
            Particle particle = parts.get(j);
            if (particle == null) continue;

            try {
                particle.render(buffer, camera, partialTick);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        tesselator.end();
        poseStack.popPose();
    }

    public void addEffect(Level world, Particle fx, int layer) {
        if (fx == null) return;

        if (layer < 0 || layer >= 4) layer = 0;

        int dim = world.dimension().location().hashCode();

        if (!this.particles[layer].containsKey(dim)) {
            this.particles[layer].put(dim, new ArrayList<>());
        }

        ArrayList<Particle> parts = this.particles[layer].get(dim);
        if (parts.size() >= 2000) {
            parts.remove(0);
        }
        parts.add(fx);
    }

    public void addEffect(Level world, Particle fx) {
        addEffect(world, fx, 0);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void updateParticles(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int dim = mc.level.dimension().location().hashCode();

        for (int layer = 0; layer < 4; layer++) {
            if (!this.particles[layer].containsKey(dim)) continue;

            ArrayList<Particle> parts = this.particles[layer].get(dim);

            for (int j = 0; j < parts.size(); j++) {
                Particle particle = parts.get(j);

                try {
                    if (particle != null) {
                        particle.tick();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                if (particle == null || !particle.isAlive()) {
                    parts.remove(j--);
                }
            }
        }
    }

    public void clearEffects(int dim) {
        for (int layer = 0; layer < 4; layer++) {
            if (this.particles[layer].containsKey(dim)) {
                this.particles[layer].get(dim).clear();
            }
        }
    }

    public void clearEffects() {
        for (int layer = 0; layer < 4; layer++) {
            this.particles[layer].clear();
        }
    }
}