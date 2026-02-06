package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ParticleEngine {
    public static final ParticleEngine INSTANCE = new ParticleEngine();
    public static final ResourceLocation PARTICLE_TEXTURE =
            new ResourceLocation("ex_enigmaticlegacy", "textures/misc/particles.png");
    public static final ResourceLocation PARTICLE_TEXTURE_2 =
            new ResourceLocation("ex_enigmaticlegacy", "textures/misc/particles2.png");

    private final HashMap<Integer, ArrayList<Particle>>[] particles = new HashMap[]{
            new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()
    };

    private final Random rand = new Random();
    private static final int MAX_PARTICLES_PER_LAYER = 2000;


    @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            INSTANCE.renderParticles(event.getPoseStack(), event.getPartialTick());
        }
    }

    private void renderParticles(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        int dimension = getDimensionId(mc.level);

        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);

        for (int layer = 0; layer < 4; layer++) {
            List<Particle> particleList = particles[layer].get(dimension);
            if (particleList == null || particleList.isEmpty()) continue;

            // 分离自定义纹理粒子和标准粒子
            List<Particle> customTextureParticles = new ArrayList<>();
            List<Particle> standardParticles = new ArrayList<>();

            for (Particle particle : particleList) {
                if (particle instanceof FXBurst && ((FXBurst) particle).needsCustomTexture()) {
                    customTextureParticles.add(particle);
                } else {
                    standardParticles.add(particle);
                }
            }

            switch (layer) {
                case 0:
                case 2:
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                    break;
                case 1:
                case 3:
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    break;
            }

            if (!customTextureParticles.isEmpty()) {
                RenderSystem.setShaderTexture(0, FXBurst.nodetex);

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                for (Particle particle : customTextureParticles) {
                    if (particle != null && particle.isAlive()) {
                        try {
                            particle.render(buffer, camera, partialTick);
                        } catch (Exception e) {
                            System.err.println("Error rendering FXBurst: " + e.getMessage());
                        }
                    }
                }

                tesselator.end();
            }

            // 渲染标准粒子（使用particles.png）
            if (!standardParticles.isEmpty()) {
                if (layer >= 2) {
                    RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE_2);
                } else {
                    RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE);
                }

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                for (Particle particle : standardParticles) {
                    if (particle != null && particle.isAlive()) {
                        try {
                            particle.render(buffer, camera, partialTick);
                        } catch (Exception e) {
                            System.err.println("Error rendering standard particle: " + e.getMessage());
                        }
                    }
                }

                tesselator.end();
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public void addEffect(Level world, Particle particle, int layer) {
        if (!(world instanceof ClientLevel)) return;
        if (layer < 0 || layer >= 4) layer = 0;

        int dimension = getDimensionId(world);

        particles[layer].computeIfAbsent(dimension, k -> new ArrayList<>());
        List<Particle> particleList = particles[layer].get(dimension);

        if (particleList.size() >= MAX_PARTICLES_PER_LAYER) {
            particleList.remove(0);
        }

        particleList.add(particle);
    }


    public void addEffect(Level world, Particle fx) {
        int layer = 0;

        if (fx instanceof FXBurst) {
            layer = ((FXBurst) fx).getRenderLayer();
        } else if (fx instanceof FXSparkle) {
            layer = 0;
        } else if (fx instanceof FXBubble) {
            layer = 1;
        } else if (fx instanceof FXScorch) {
            layer = 0;
        }

        addEffect(world, fx, layer);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            INSTANCE.updateParticles();
        }
    }

    private void updateParticles() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int dimension = getDimensionId(mc.level);

        for (int layer = 0; layer < 4; layer++) {
            if (!particles[layer].containsKey(dimension)) continue;

            List<Particle> particleList = particles[layer].get(dimension);
            particleList.removeIf(particle -> {
                if (particle == null) return true;

                try {
                    particle.tick();
                    return !particle.isAlive();
                } catch (Exception e) {
                    System.err.println("Error updating particle: " + e.getMessage());
                    return true;
                }
            });
        }
    }

    public void clearDimension(int dimension) {
        for (int layer = 0; layer < 4; layer++) {
            particles[layer].remove(dimension);
        }
    }

    public void clearAll() {
        for (int layer = 0; layer < 4; layer++) {
            particles[layer].clear();
        }
    }

    private int getDimensionId(Level level) {
        return level.dimension().location().hashCode();
    }

    public int getParticleCount(int layer, int dimension) {
        if (layer < 0 || layer >= 4) return 0;
        return particles[layer].getOrDefault(dimension, new ArrayList<>()).size();
    }

    public int getTotalParticleCount() {
        int total = 0;
        for (int layer = 0; layer < 4; layer++) {
            for (List<Particle> list : particles[layer].values()) {
                total += list.size();
            }
        }
        return total;
    }
}