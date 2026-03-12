package net.xiaoyang010.ex_enigmaticlegacy.Capability;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class YuhuaEntityRenderer {
    private static final Map<UUID, long[]> YUHUA_MAP = new HashMap<>();
    private static final Map<UUID, Integer> SAVED_DEATH_TIME = new HashMap<>();
    private static final float INFLATE = 0.1f;
    private static final int WRAP_ANIM_TICKS = 20;
    private static Field layersField = null;
    private static boolean layersFieldSearched = false;
    private static final Map<Class<?>, Method> SCALE_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Field> LAYER_MODEL_FIELD_CACHE = new HashMap<>();
    private static final Set<String> LAYER_BLACKLIST_KEYWORDS = Set.of(
            "Armor",
            "ItemInHand",
            "Elytra",
            "CustomHead",
            "Cape",
            "SpinAttack",
            "Parrot",
            "StuckInBody",
            "Arrow",
            "BeeStinger",
            "Saddle",
            "Collar",
            "Cracks",
            "Flower",
            "Trident",
            "Shield",
            "Stray"
    );

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(YuhuaEntityRenderer::onRenderLivingPre);
        MinecraftForge.EVENT_BUS.addListener(YuhuaEntityRenderer::onRenderLivingPost);
    }

    public static void markYuhua(UUID id, int hitCount, int maxHits, long clientTick) {
        YUHUA_MAP.put(id, new long[]{hitCount, maxHits, clientTick});
    }

    public static void clearYuhua(UUID id) {
        YUHUA_MAP.remove(id);
        SAVED_DEATH_TIME.remove(id);
    }

    public static boolean isYuhua(UUID id) {
        return YUHUA_MAP.containsKey(id);
    }

    public static int getHitCount(UUID id) {
        long[] d = YUHUA_MAP.get(id);
        return d != null ? (int) d[0] : 0;
    }

    public static int getMaxHits(UUID id) {
        long[] d = YUHUA_MAP.get(id);
        return d != null ? (int) d[1] : 3;
    }

    public static long getMarkTick(UUID id) {
        long[] d = YUHUA_MAP.get(id);
        return d != null ? d[2] : 0;
    }

    @SuppressWarnings({"rawtypes"})
    private static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUUID();
        if (!isYuhua(uuid)) return;
        if (YuHuaShaders.yuhuaShader == null) return;

        int savedDeathTime = entity.deathTime;
        entity.deathTime = 0;
        SAVED_DEATH_TIME.put(uuid, savedDeathTime);

        int hitCount = getHitCount(uuid);
        int maxHits  = getMaxHits(uuid);
        float ratio  = (float) hitCount / (float) maxHits;
        float opacity = 0.6f + ratio * 0.4f;

        float partialTick = Minecraft.getInstance().getFrameTime();
        LivingEntityRenderer renderer = event.getRenderer();
        EntityModel mainModel = renderer.getModel();
        float wrapProgress = calcWrapProgress(uuid, partialTick);
        boolean freezeAge = (entity instanceof Chicken);

        setupAnimForModel(mainModel, entity, partialTick, freezeAge);
        applyShaderUniforms(entity, opacity);

        renderYuhuaModel(
                event.getPoseStack(), event.getMultiBufferSource(),
                mainModel, event.getPackedLight(), opacity,
                entity, partialTick, renderer, wrapProgress, INFLATE
        );

        List<LayerModelEntry> entries = collectLayerModels(renderer, entity);
        for (LayerModelEntry entry : entries) {
            copyModelProperties(mainModel, entry.model);
            setupAnimForModel(entry.model, entity, partialTick, freezeAge);

            renderYuhuaModel(
                    event.getPoseStack(), event.getMultiBufferSource(),
                    entry.model, event.getPackedLight(), opacity,
                    entity, partialTick, renderer, wrapProgress, INFLATE
            );
        }
    }

    private static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUUID();
        Integer saved = SAVED_DEATH_TIME.remove(uuid);
        if (saved != null) {
            entity.deathTime = saved;
        }
    }

    private static float calcWrapProgress(UUID uuid, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 1.0f;
        long markTick = getMarkTick(uuid);
        long currentTick = mc.level.getGameTime();
        float elapsed = (currentTick - markTick) + partialTick;
        if (elapsed >= WRAP_ANIM_TICKS) return 1.0f;
        if (elapsed <= 0) return 0.0f;
        float t = elapsed / (float) WRAP_ANIM_TICKS;
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private record LayerModelEntry(EntityModel<?> model, RenderLayer<?, ?> layer) {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<LayerModelEntry> collectLayerModels(
            LivingEntityRenderer renderer, LivingEntity entity) {

        List<LayerModelEntry> result = new ArrayList<>();

        try {
            List<RenderLayer> layers = getLayers(renderer);
            if (layers == null) return result;

            EntityModel<?> mainModel = renderer.getModel();

            for (RenderLayer layer : layers) {
                String className = layer.getClass().getSimpleName();
                if (isBlacklisted(className)) continue;

                EntityModel<?> model = extractModelFromLayer(layer);
                if (model == null) continue;

                if (model == mainModel) continue;

                if (!shouldLayerBeVisible(layer, className, entity)) continue;

                result.add(new LayerModelEntry(model, layer));
            }
        } catch (Exception ignored) {}

        return result;
    }

    private static boolean isBlacklisted(String simpleName) {
        for (String keyword : LAYER_BLACKLIST_KEYWORDS) {
            if (simpleName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldLayerBeVisible(RenderLayer<?, ?> layer, String className, LivingEntity entity) {
        if (className.contains("SheepFur") || className.contains("SheepWool")) {
            return (entity instanceof Sheep sheep) && !sheep.isSheared() && !sheep.isInvisible();
        }

        if (className.contains("SlimeOuter")) {
            boolean glowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity)
                    && entity.isInvisible();
            return !entity.isInvisible() || glowing;
        }

        if (className.contains("Power") || className.contains("EnergySwirl")
                || className.contains("Wither")) {
            return false;
        }

        if (className.contains("DrownedOuter")) {
            return !entity.isInvisible();
        }

        return !entity.isInvisible();
    }

    @SuppressWarnings({"rawtypes"})
    private static List<RenderLayer> getLayers(LivingEntityRenderer renderer) {
        try {
            if (!layersFieldSearched) {
                layersFieldSearched = true;
                for (Field f : LivingEntityRenderer.class.getDeclaredFields()) {
                    if (List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        layersField = f;
                        break;
                    }
                }
            }
            if (layersField == null) return null;
            @SuppressWarnings("unchecked")
            List<RenderLayer> layers = (List<RenderLayer>) layersField.get(renderer);
            return layers;
        } catch (Exception e) {
            return null;
        }
    }

    private static EntityModel<?> extractModelFromLayer(Object layer) {
        Class<?> layerClass = layer.getClass();

        if (LAYER_MODEL_FIELD_CACHE.containsKey(layerClass)) {
            Field cached = LAYER_MODEL_FIELD_CACHE.get(layerClass);
            if (cached == null) return null;
            try {
                Object obj = cached.get(layer);
                return (obj instanceof EntityModel<?> em) ? em : null;
            } catch (Exception e) {
                return null;
            }
        }

        Field found = null;
        try {
            Class<?> clazz = layerClass;
            while (clazz != null && clazz != RenderLayer.class && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (EntityModel.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        found = f;
                        break;
                    }
                }
                if (found != null) break;
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {}

        LAYER_MODEL_FIELD_CACHE.put(layerClass, found);

        if (found == null) return null;
        try {
            Object obj = found.get(layer);
            return (obj instanceof EntityModel<?> em) ? em : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyModelProperties(EntityModel from, EntityModel to) {
        try { from.copyPropertiesTo(to); } catch (Exception ignored) {}
    }

    private static void setupAnimForModel(EntityModel<?> model, LivingEntity entity,
                                          float partialTick, boolean freezeAge) {
        if (freezeAge) {
            invokeSetupAnimFrozenAge(model, entity, partialTick);
        } else {
            invokeSetupAnim(model, entity, partialTick);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void invokeSetupAnim(EntityModel model, LivingEntity entity, float partialTick) {
        try {
            float f8 = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            float f5 = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) f5 *= 3.0F;
            if (f8 > 1.0F) f8 = 1.0F;
            model.prepareMobModel(entity, f5, f8, partialTick);
            float yBodyRot   = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            float yHeadRot   = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
            float netHeadYaw = yHeadRot - yBodyRot;
            float headPitch  = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            float ageInTicks = entity.tickCount + partialTick;
            model.setupAnim(entity, f5, f8, ageInTicks, netHeadYaw, headPitch);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void invokeSetupAnimFrozenAge(EntityModel model, LivingEntity entity, float partialTick) {
        try {
            float f8 = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            float f5 = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) f5 *= 3.0F;
            if (f8 > 1.0F) f8 = 1.0F;
            model.prepareMobModel(entity, f5, f8, partialTick);
            float yBodyRot   = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            float yHeadRot   = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
            float netHeadYaw = yHeadRot - yBodyRot;
            float headPitch  = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            model.setupAnim(entity, f5, f8, 0.0F, netHeadYaw, headPitch);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"rawtypes"})
    private static void renderYuhuaModel(PoseStack poseStack, MultiBufferSource buffers,
                                         EntityModel model, int packedLight, float opacity,
                                         LivingEntity entity, float partialTick,
                                         LivingEntityRenderer renderer,
                                         float wrapProgress, float inflate) {
        TextureAtlasSprite placeholder = AvaritiaShaders.COSMIC_SPRITES[0];
        if (placeholder == null) return;

        poseStack.pushPose();

        float yBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yBodyRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        invokeScale(renderer, entity, poseStack, partialTick);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        RenderType renderType = YuhuaRenderType.getYuhuaEntityRenderType();
        VertexConsumer rawConsumer = buffers.getBuffer(renderType);

        VertexConsumer adaptedConsumer = new WrapAnimAdapter(
                rawConsumer, packedLight, opacity,
                placeholder.getU0(), placeholder.getU1(),
                placeholder.getV0(), placeholder.getV1(),
                inflate, wrapProgress
        );

        model.renderToBuffer(
                poseStack, adaptedConsumer,
                packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, opacity
        );

        if (buffers instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(renderType);
        }

        poseStack.popPose();
    }

    private static void applyShaderUniforms(LivingEntity entity, float opacity) {
        if (YuHuaShaders.yuhuaTime != null)
            YuHuaShaders.yuhuaTime.set((float) YuHuaShaders.renderTime + YuHuaShaders.renderFrame);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float yaw   = mc.player.getYRot() * (float)(Math.PI / 180.0);
            float pitch = mc.player.getXRot() * (float)(Math.PI / 180.0);
            if (YuHuaShaders.yuhuaYaw   != null) YuHuaShaders.yuhuaYaw.set(yaw);
            if (YuHuaShaders.yuhuaPitch != null) YuHuaShaders.yuhuaPitch.set(pitch);
        }
        if (YuHuaShaders.yuhuaExternalScale != null) YuHuaShaders.yuhuaExternalScale.set(1.0f);
        if (YuHuaShaders.yuhuaOpacity != null) YuHuaShaders.yuhuaOpacity.set(opacity);
        if (YuHuaShaders.yuhuaUVs != null) YuHuaShaders.yuhuaUVs.set(YuHuaShaders.COSMIC_UVS);
    }

    @SuppressWarnings({"rawtypes"})
    private static void invokeScale(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack, float partialTick) {
        try {
            Class<?> rendererClass = renderer.getClass();

            if (SCALE_METHOD_CACHE.containsKey(rendererClass)) {
                Method cached = SCALE_METHOD_CACHE.get(rendererClass);
                if (cached != null) {
                    cached.invoke(renderer, entity, poseStack, partialTick);
                }
                return;
            }

            Method found = null;
            Class<?> clazz = rendererClass;
            while (clazz != null && found == null) {
                for (Method m : clazz.getDeclaredMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length == 3
                            && LivingEntity.class.isAssignableFrom(p[0])
                            && PoseStack.class.isAssignableFrom(p[1])
                            && p[2] == float.class
                            && m.getReturnType() == void.class) {
                        found = m;
                        break;
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (found != null) {
                found.setAccessible(true);
            }
            SCALE_METHOD_CACHE.put(rendererClass, found);

            if (found != null) {
                found.invoke(renderer, entity, poseStack, partialTick);
            }
        } catch (Exception ignored) {}
    }

    private static class WrapAnimAdapter implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int   forcedLight;
        private final float baseAlpha;
        private final float pu0, pu1, pv0, pv1;
        private final float inflate;
        private final float wrapProgress;

        private static final float MODEL_Y_TOP    = 0.0f;
        private static final float MODEL_Y_BOTTOM = 24.0f;
        private static final float EDGE_FADE      = 3.0f;

        private float vx, vy, vz;
        private float r = 1f, g = 1f, b = 1f, a;
        private float nx, ny, nz;
        private float modelU, modelV;

        WrapAnimAdapter(VertexConsumer delegate, int forcedLight, float alpha,
                        float pu0, float pu1, float pv0, float pv1,
                        float inflate, float wrapProgress) {
            this.delegate      = delegate;
            this.forcedLight   = forcedLight;
            this.baseAlpha     = alpha;
            this.pu0 = pu0; this.pu1 = pu1;
            this.pv0 = pv0; this.pv1 = pv1;
            this.inflate       = inflate;
            this.wrapProgress  = wrapProgress;
            this.a             = alpha;
        }

        private float calcWrapAlpha(float modelY) {
            if (wrapProgress >= 1.0f) return 1.0f;
            if (wrapProgress <= 0.0f) return 0.0f;
            float cutoffY = MODEL_Y_BOTTOM - wrapProgress * (MODEL_Y_BOTTOM - MODEL_Y_TOP);
            if (modelY > cutoffY) return 1.0f;
            if (modelY > cutoffY - EDGE_FADE) return (modelY - (cutoffY - EDGE_FADE)) / EDGE_FADE;
            return 0.0f;
        }

        @Override public VertexConsumer vertex(double x, double y, double z) {
            vx=(float)x; vy=(float)y; vz=(float)z; return this;
        }
        @Override public VertexConsumer color(int r, int g, int b, int a) {
            this.r=r/255f; this.g=g/255f; this.b=b/255f; this.a=(a/255f)*baseAlpha; return this;
        }
        @Override public VertexConsumer color(float r, float g, float b, float a) {
            this.r=r; this.g=g; this.b=b; this.a=a*baseAlpha; return this;
        }
        @Override public VertexConsumer uv(float u, float v) {
            modelU=u; modelV=v; return this;
        }
        @Override public VertexConsumer overlayCoords(int u, int v)  { return this; }
        @Override public VertexConsumer overlayCoords(int packedUv)  { return this; }
        @Override public VertexConsumer uv2(int u, int v)            { return this; }
        @Override public VertexConsumer uv2(int packedUv)            { return this; }
        @Override public VertexConsumer normal(float nx, float ny, float nz) {
            this.nx=nx; this.ny=ny; this.nz=nz; return this;
        }
        @Override
        public void endVertex() {
            float finalX = vx + nx * inflate;
            float finalY = vy + ny * inflate;
            float finalZ = vz + nz * inflate;
            float finalU = pu0 + modelU * (pu1 - pu0);
            float finalV = pv0 + modelV * (pv1 - pv0);
            float wrapAlpha = calcWrapAlpha(vy);
            float finalAlpha = a * wrapAlpha;

            delegate.vertex(finalX, finalY, finalZ)
                    .color(r, g, b, finalAlpha)
                    .uv(finalU, finalV)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(forcedLight)
                    .normal(nx, ny, nz)
                    .endVertex();
        }
        @Override public void defaultColor(int r, int g, int b, int a) { delegate.defaultColor(r,g,b,a); }
        @Override public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
    }
}