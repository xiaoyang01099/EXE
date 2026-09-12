package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes;

public final class MaskCanvas {
    public enum Channel { SOLID, ALPHA, RED }
    public record Vertex(Vec3 position, float u, float v, float strength, float opacity) {}
    private static final ResourceLocation WHITE = new ResourceLocation("ex_enigmaticlegacy", "textures/effect/white.png");
    private final BufferBuilder buffer;
    private final ShaderInstance shader;
    private final Vec3 camera;
    private final Runnable prepare;
    private ResourceLocation texture = WHITE;
    private Channel channel = Channel.SOLID;
    private boolean building;
    private boolean closed;
    private int vertices;
    private boolean drew;
    private boolean prepared;
    private boolean trailCoordinates = true;
    private org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade style=org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.CLASSIC;
    private boolean screenSpace;
    private int textureId = -1;

    MaskCanvas(BufferBuilder buffer, ShaderInstance shader, Vec3 camera, Runnable prepare) {
        this.buffer = buffer;
        this.shader = shader;
        this.camera = camera;
        this.prepare = prepare;
    }

    public void texture(ResourceLocation texture, Channel channel) {
        texture(texture, channel, channel == Channel.SOLID);
    }

    public void texture(ResourceLocation texture, Channel channel, boolean trailCoordinates) {
        checkOpen();
        flush();
        this.texture = texture;
        this.channel = channel;
        this.trailCoordinates = trailCoordinates;
        this.textureId = -1;
    }

    public void solid() { texture(WHITE, Channel.SOLID); }
    public void style(org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade style) {
        checkOpen();
        if(this.style!=style) { flush(); this.style=style; }
    }

    public void screenMask(int colorTextureId, Channel channel) {
        checkOpen();
        if (colorTextureId <= 0 || channel == Channel.SOLID) {
            throw new IllegalArgumentException("A valid color texture and ALPHA or RED channel are required");
        }
        flush();
        textureId = colorTextureId;
        this.channel = channel;
        trailCoordinates = false;
        screenSpace = true;
        quad(new Vertex(camera.add(-1, -1, 0), 0, 0, 1, 1),
            new Vertex(camera.add(1, -1, 0), 1, 0, 1, 1),
            new Vertex(camera.add(1, 1, 0), 1, 1, 1, 1),
            new Vertex(camera.add(-1, 1, 0), 0, 1, 1, 1));
        flush();
        screenSpace = false;
        solid();
    }

    public void quad(Vertex a, Vertex b, Vertex c, Vertex d) {
        triangle(a, b, c);
        triangle(a, c, d);
    }

    public void triangle(Vertex a, Vertex b, Vertex c) {
        checkOpen();
        if (!prepared) {
            prepare.run();
            prepared = true;
        }
        if (!building) {
            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX);
            building = true;
        }
        vertex(a);
        vertex(b);
        vertex(c);
    }

    private void vertex(Vertex v) {
        Vec3 p = v.position.subtract(camera);
        buffer.vertex(p.x, p.y, p.z).color(v.strength, v.strength, v.strength, v.opacity)
            .uv(v.u, v.v).endVertex();
        vertices++;
    }

    private void flush() {
        if (!building) return;
        shader.setSampler("MaskTexture", textureId > 0 ? textureId
            : Minecraft.getInstance().getTextureManager().getTexture(texture).getId());
        shader.safeGetUniform("MaskMode").set((float) channel.ordinal());
        shader.safeGetUniform("TrailCoordinates").set(trailCoordinates ? 1.0F : 0.0F);
        shader.safeGetUniform("ScreenSpace").set(screenSpace ? 1.0F : 0.0F);
        shader.safeGetUniform("EffectStyle").set(style== ItemEffectTypes.Blade.COSMIC?1f:0f);
        if (screenSpace) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        }
        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(buffer.end());
        if (screenSpace) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }
        drew |= vertices > 0;
        vertices = 0;
        building = false;
    }

    boolean finish() {
        flush();
        closed = true;
        return drew;
    }

    void abort() {
        if (building) buffer.discard();
        building = false;
        closed = true;
    }

    private void checkOpen() {
        if (closed) throw new IllegalStateException("MaskCanvas is only valid inside MaskRenderEvent");
    }
}
