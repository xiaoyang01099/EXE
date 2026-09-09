package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;

public final class BladeFlashRenderer {
    public static final BladeFlashRenderer INSTANCE = new BladeFlashRenderer();
    public static final BladeFlashRenderer FIRST_PERSON = new BladeFlashRenderer();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BufferBuilder maskBuffer = new BufferBuilder(256 * 1024);
    private final BufferBuilder screenBuffer = new BufferBuilder(256);
    private RenderTarget mask;
    private RenderTarget scene;
    private boolean failed;
    private BladeRenderState saved;
    private final Matrix4f worldView = new Matrix4f();
    private final Matrix4f worldProjection = new Matrix4f();
    private boolean viewReady;
    private boolean depthReady;
    int debugView;

    public void captureView(RenderLevelStageEvent event) {
        worldView.set(event.getPoseStack().last().pose());
        worldProjection.set(event.getProjectionMatrix());
        viewReady = true;
    }

    public void render(RenderLevelStageEvent event) {
        if (!viewReady) return;
        viewReady = false;
        if (failed || !ConfigHandler.ENABLED.get() || EXECoreShaders.edgemask == null || EXECoreShaders.edgecomposite == null) return;
        renderTrails(BladeFlashClient.TRAILS, event.getCamera().getPosition(), true, false);
    }

    public void prepareFirstPersonDepth(RenderLevelStageEvent event) {
        worldView.set(INSTANCE.worldView);
        worldProjection.set(INSTANCE.worldProjection);
        if (failed || EXECoreShaders.edgemask == null || EXECoreShaders.edgecomposite == null) return;
        BladeRenderState state = new BladeRenderState();
        try {
            RenderSystem.disableScissor();
            RenderSystem.depthMask(true);
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            ensureTargets(main);
            mask.clear(Minecraft.ON_OSX);
            mask.copyDepthFrom(main);
            depthReady = true;
        } finally {
            state.restore();
        }
    }

    public void renderFirstPerson(TrailStore trails, Vec3 camera) {
        boolean ready = depthReady;
        depthReady = false;
        if (!ready || failed || !ConfigHandler.ENABLED.get() || !ConfigHandler.HELD_SWORD_TRAILS.get()) return;
        debugView = INSTANCE.debugView;
        renderTrails(trails, camera, false, true);
    }

    private void renderTrails(TrailStore trails, Vec3 camera, boolean external, boolean keepDepth) {
        double now = BladeFlashClient.timeSeconds();
        trails.prune(now);
        MaskCanvas canvas = new MaskCanvas(maskBuffer, EXECoreShaders.edgemask, camera, () -> prepare(keepDepth));
        try {
            trails.draw(canvas, now);
            canvas.style(org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.CLASSIC);
            if (external) MinecraftForge.EVENT_BUS.post(new MaskRenderEvent(canvas, now));
            if (canvas.finish()) composite(now);
        } catch (RuntimeException e) {
            canvas.abort();
            failed = true;
            LOGGER.error("BladeFlash rendering disabled until resource reload (F3+T)", e);
        } finally {
            if (saved != null) {
                saved.restore();
                saved = null;
            }
        }
    }

    private void prepare(boolean keepDepth) {
        saved = new BladeRenderState();
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        RenderSystem.disableScissor();
        RenderSystem.depthMask(true);
        ensureTargets(main);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, scene.width, scene.height,
            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        if (!keepDepth) {
            mask.clear(Minecraft.ON_OSX);
            mask.copyDepthFrom(main);
        }
        mask.bindWrite(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        EXECoreShaders.edgemask.safeGetUniform("BladeView").set(worldView);
        EXECoreShaders.edgemask.safeGetUniform("BladeProjection").set(worldProjection);
    }

    private void ensureTargets(RenderTarget main) {
        if (mask != null && (mask.width != main.width || mask.height != main.height
            || mask.isStencilEnabled() != main.isStencilEnabled())) release();
        if (mask == null) {
            mask = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            if (main.isStencilEnabled()) mask.enableStencil();
            mask.setClearColor(0, 0, 0, 0);
            mask.setFilterMode(GL11.GL_NEAREST);
            scene = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            scene.setFilterMode(GL11.GL_LINEAR);
        }
    }

    private void composite(double now) {
        Minecraft mc = Minecraft.getInstance();
        ShaderInstance shader = EXECoreShaders.edgecomposite;
        mc.getMainRenderTarget().bindWrite(true);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        shader.setSampler("SceneSampler", scene.getColorTextureId());
        shader.setSampler("MaskSampler", mask.getColorTextureId());
        texture(shader, "NoiseSampler", "noise_worley");
        texture(shader, "EndSkySampler", "end_sky");
        texture(shader, "EndPortalSampler", "end_portal");
        shader.setSampler("CosmicSampler",mc.getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getId());
        org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaShaders.cosmicAtlas(shader);
        shader.safeGetUniform("InverseViewProjection").set(new Matrix4f(worldProjection).mul(worldView).invert());
        shader.safeGetUniform("ScreenSize").set((float) scene.width, (float) scene.height);
        shader.safeGetUniform("EffectTime").set((float) (now % 1200.0));
        shader.safeGetUniform("DistortionPixels").set(ConfigHandler.DISTORTION_PIXELS.get().floatValue());
        shader.safeGetUniform("Brightness").set(ConfigHandler.BRIGHTNESS.get().floatValue());
        shader.safeGetUniform("DebugView").set((float) debugView);
        RenderSystem.setShader(() -> shader);
        screenBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        screenBuffer.vertex(-1, -1, 0).uv(0, 0).endVertex();
        screenBuffer.vertex(1, -1, 0).uv(1, 0).endVertex();
        screenBuffer.vertex(1, 1, 0).uv(1, 1).endVertex();
        screenBuffer.vertex(-1, 1, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(screenBuffer.end());
    }

    private void texture(ShaderInstance shader, String sampler, String name) {
        shader.setSampler(sampler, Minecraft.getInstance().getTextureManager()
            .getTexture(new ResourceLocation("ex_enigmaticlegacy", "textures/effect/" + name + ".png")).getId());
    }

    public void reloaded() {
        release();
        failed = false;
    }

    public void release() {
        if (mask != null) mask.destroyBuffers();
        if (scene != null) scene.destroyBuffers();
        mask = null;
        scene = null;
        depthReady = false;
    }
}
