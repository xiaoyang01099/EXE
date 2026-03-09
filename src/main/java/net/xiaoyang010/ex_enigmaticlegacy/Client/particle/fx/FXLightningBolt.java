package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.EXVector3;

public class FXLightningBolt extends Particle {
    private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation("ex_enigmaticlegacy", "textures/particle/large.png");
    private static final ResourceLocation TEXTURE_SMALL = new ResourceLocation("ex_enigmaticlegacy", "textures/particle/small.png");
    private int type = 0;
    private float width = 0.03f;
    private FXLightningBoltCommon main;

    public FXLightningBolt(ClientLevel world, EXVector3 jammervec, EXVector3 targetvec, long seed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, jammervec, targetvec, seed);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, Entity detonator, Entity target, long seed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, detonator, target, seed);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, Entity detonator, Entity target, long seed, int speed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, detonator, target, seed, speed);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, BlockEntity detonator, Entity target, long seed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, detonator, target, seed);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, double x1, double y1, double z1,
                           double x, double y, double z, long seed, int duration, float multi) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, x1, y1, z1, x, y, z, seed, duration, multi);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, double x1, double y1, double z1,
                           double x, double y, double z, long seed, int duration, float multi, int speed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, x1, y1, z1, x, y, z, seed, duration, multi, speed);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, double x1, double y1, double z1,
                           double x, double y, double z, long seed, int duration) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, x1, y1, z1, x, y, z, seed, duration, 1.0f);
        this.setupFromMain();
    }

    public FXLightningBolt(ClientLevel world, BlockEntity detonator,
                           double x, double y, double z, long seed) {
        super(world, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.main = new FXLightningBoltCommon(world, detonator, x, y, z, seed);
        this.setupFromMain();
    }

    private void setupFromMain() {
        this.age = this.main.particleMaxAge;
        this.setPos(this.main.start.x, this.main.start.y, this.main.start.z);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    public void defaultFractal() {
        this.main.defaultFractal();
    }

    public void fractal(int splits, float amount, float splitchance,
                        float splitlength, float splitangle) {
        this.main.fractal(splits, amount, splitchance, splitlength, splitangle);
    }

    public void finalizeBolt() {
        this.main.finalizeBolt();
        Minecraft.getInstance().particleEngine.add(this);
    }

    public void setType(int type) {
        this.type = type;
        this.main.type = type;
    }

    public void setMultiplier(float m) {
        this.main.multiplier = m;
    }

    public void setWidth(float m) {
        this.width = m;
    }

    @Override
    public void tick() {
        this.main.onUpdate();
        if (this.main.particleAge >= this.main.particleMaxAge) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return CUSTOM_RENDER_TYPE;
    }

    private static final ParticleRenderType CUSTOM_RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, TEXTURE_LARGE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "LIGHTNING_BOLT";
        }
    };

    @Override
    public void render(VertexConsumer buffer,
                       Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        Minecraft mc = Minecraft.getInstance();
        GraphicsStatus graphicsMode = mc.options.graphicsMode;

        int visibleDistance = switch (graphicsMode) {
            case FAST -> 50;
            case FANCY -> 100;
            case FABULOUS -> 150;
        };

        if (camera.getEntity().distanceToSqr(this.x, this.y, this.z) > visibleDistance * visibleDistance) {
            return;
        }

        setColorByType(0);

        RenderSystem.setShaderTexture(0, TEXTURE_LARGE);
        renderBoltLayer(buffer, cameraPos, partialTicks, 0);

        setColorByType(1);
        RenderSystem.setShaderTexture(0, TEXTURE_SMALL);
        renderBoltLayer(buffer, cameraPos, partialTicks, 1);
    }

    private void setColorByType(int pass) {
        float ma = 1.0f;

        switch (this.type) {
            case 0: // 紫色（魔法）
                if (pass == 0) {
                    this.rCol = 0.6f; this.gCol = 0.3f; this.bCol = 0.6f;
                } else {
                    this.rCol = 1.0f; this.gCol = 0.6f; this.bCol = 1.0f;
                }
                break;
            case 1: // 黄色（电）
                if (pass == 0) {
                    this.rCol = 0.6f; this.gCol = 0.6f; this.bCol = 0.1f;
                } else {
                    this.rCol = 1.0f; this.gCol = 1.0f; this.bCol = 0.1f;
                }
                break;
            case 2: // 蓝色（冰）
                if (pass == 0) {
                    this.rCol = 0.1f; this.gCol = 0.1f; this.bCol = 0.6f;
                } else {
                    this.rCol = 0.1f; this.gCol = 0.1f; this.bCol = 1.0f;
                }
                break;
            case 3: // 绿色（生命）
                if (pass == 0) {
                    this.rCol = 0.1f; this.gCol = 1.0f; this.bCol = 0.1f;
                } else {
                    this.rCol = 0.1f; this.gCol = 0.6f; this.bCol = 0.1f;
                }
                break;
            case 4: // 红色（火）
                if (pass == 0) {
                    this.rCol = 0.6f; this.gCol = 0.1f; this.bCol = 0.1f;
                } else {
                    this.rCol = 1.0f; this.gCol = 0.1f; this.bCol = 0.1f;
                }
                break;
            case 5: // 暗紫（诅咒）
                if (pass == 0) {
                    this.rCol = 0.6f; this.gCol = 0.2f; this.bCol = 0.6f;
                } else {
                    this.rCol = 0.0f; this.gCol = 0.0f; this.bCol = 0.0f;
                }
                break;
            case 6: // 青色（水）
                this.rCol = 0.75f; this.gCol = 1.0f; this.bCol = 1.0f;
                ma = 0.2f;
                break;
            default:
                this.rCol = 1.0f; this.gCol = 1.0f; this.bCol = 1.0f;
        }

        this.alpha = ma;
    }

    private void renderBoltLayer(VertexConsumer buffer,
                                 Vec3 cameraPos, float partialTicks, int pass) {
        Vec3 viewVec = cameraPos.subtract(this.x, this.y, this.z).normalize();
        EXVector3 playervec = new EXVector3(viewVec.x, viewVec.y, viewVec.z);

        float boltage = this.main.particleAge >= 0 ?
                (float)this.main.particleAge / (float)this.main.particleMaxAge : 0.0f;
        float mainAlpha = pass == 0 ?
                (1.0f - boltage) * 0.4f : 1.0f - boltage * 0.5f;

        int renderlength = (int)(((float)this.main.particleAge + partialTicks +
                (float)((int)(this.main.length * 3.0f))) /
                (float)((int)(this.main.length * 3.0f)) * (float)this.main.numsegments0);

        for (FXLightningBoltCommon.Segment segment : this.main.segments) {
            if (segment.segmentno > renderlength) continue;

            renderSegment(buffer, cameraPos, segment, playervec, mainAlpha);
        }
    }

    private void renderSegment(VertexConsumer buffer,
                               Vec3 cameraPos,
                               FXLightningBoltCommon.Segment segment,
                               EXVector3 playervec,
                               float mainAlpha) {

        float viewDist = getRelativeViewVector(segment.startpoint.point).length();
        float width = this.width * (viewDist / 5.0f + 1.0f) *
                (1.0f + segment.light) * 0.5f;

        EXVector3 diff1 = EXVector3.crossProduct(playervec, segment.prevdiff)
                .scale(width / segment.sinprev);
        EXVector3 diff2 = EXVector3.crossProduct(playervec, segment.nextdiff)
                .scale(width / segment.sinnext);

        EXVector3 startvec = segment.startpoint.point;
        EXVector3 endvec = segment.endpoint.point;

        float rx1 = (float)(startvec.x - cameraPos.x);
        float ry1 = (float)(startvec.y - cameraPos.y);
        float rz1 = (float)(startvec.z - cameraPos.z);
        float rx2 = (float)(endvec.x - cameraPos.x);
        float ry2 = (float)(endvec.y - cameraPos.y);
        float rz2 = (float)(endvec.z - cameraPos.z);

        float alpha = mainAlpha * segment.light;
        addVertex(buffer, rx2 - diff2.x, ry2 - diff2.y, rz2 - diff2.z, 0.5f, 0.0f, alpha);
        addVertex(buffer, rx1 - diff1.x, ry1 - diff1.y, rz1 - diff1.z, 0.5f, 0.0f, alpha);
        addVertex(buffer, rx1 + diff1.x, ry1 + diff1.y, rz1 + diff1.z, 0.5f, 1.0f, alpha);
        addVertex(buffer, rx2 + diff2.x, ry2 + diff2.y, rz2 + diff2.z, 0.5f, 1.0f, alpha);

        if (segment.next == null) {
            EXVector3 roundend = segment.endpoint.point.copy()
                    .add(segment.diff.copy().normalize().scale(width));
            float rx3 = (float)(roundend.x - cameraPos.x);
            float ry3 = (float)(roundend.y - cameraPos.y);
            float rz3 = (float)(roundend.z - cameraPos.z);

            addVertex(buffer, rx3 - diff2.x, ry3 - diff2.y, rz3 - diff2.z, 0.0f, 0.0f, alpha);
            addVertex(buffer, rx2 - diff2.x, ry2 - diff2.y, rz2 - diff2.z, 0.5f, 0.0f, alpha);
            addVertex(buffer, rx2 + diff2.x, ry2 + diff2.y, rz2 + diff2.z, 0.5f, 1.0f, alpha);
            addVertex(buffer, rx3 + diff2.x, ry3 + diff2.y, rz3 + diff2.z, 0.0f, 1.0f, alpha);
        }

        if (segment.prev == null) {
            EXVector3 roundend = segment.startpoint.point.copy()
                    .sub(segment.diff.copy().normalize().scale(width));
            float rx3 = (float)(roundend.x - cameraPos.x);
            float ry3 = (float)(roundend.y - cameraPos.y);
            float rz3 = (float)(roundend.z - cameraPos.z);

            addVertex(buffer, rx1 - diff1.x, ry1 - diff1.y, rz1 - diff1.z, 0.5f, 0.0f, alpha);
            addVertex(buffer, rx3 - diff1.x, ry3 - diff1.y, rz3 - diff1.z, 0.0f, 0.0f, alpha);
            addVertex(buffer, rx3 + diff1.x, ry3 + diff1.y, rz3 + diff1.z, 0.0f, 1.0f, alpha);
            addVertex(buffer, rx1 + diff1.x, ry1 + diff1.y, rz1 + diff1.z, 0.5f, 1.0f, alpha);
        }
    }

    private void addVertex(VertexConsumer buffer,
                           float x, float y, float z, float u, float v, float alpha) {
        buffer.vertex(x, y, z)
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, alpha * this.alpha)
                .uv2(240, 240)
                .endVertex();
    }

    private static EXVector3 getRelativeViewVector(EXVector3 pos) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new EXVector3(
                cameraPos.x - pos.x,
                cameraPos.y - pos.y,
                cameraPos.z - pos.z
        );
    }
}
