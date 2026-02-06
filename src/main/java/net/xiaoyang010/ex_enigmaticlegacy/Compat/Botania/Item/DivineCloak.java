package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.BlinkMessage;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.client.model.ModelCloak;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import java.util.List;

public class DivineCloak extends ItemBauble implements ICurioItem, ICurioRenderer {
    public static final int NJORD = 0;      // 尼约德 - 缓降
    public static final int IDUNN = 1;      // 伊登 - 背后免疫
    public static final int THOR = 2;       // 托尔 - 地震
    public static final int HEIMDALL = 3;   // 海姆达尔 - 闪现
    public static final int LOKI = 4;       // 洛基 - 爆炸/火焰免疫
    public static final DamageSource EARTHQUAKE = new DamageSource("earthquake").bypassArmor();
    private static final double EPSILON = Math.cos(Math.PI / 6);
    private static final double INVERSE_EPSILON = Math.sin(Math.PI / 6);
    private static boolean preventRecursion = false;

    // 纹理资源
    private static final ResourceLocation[] CLOAK_TEXTURES = {
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/njord_cloak.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/idunn_cloak.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/thor_cloak.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/heimdall_cloak.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/loki_cloak.png")
    };

    private static final ResourceLocation[] CLOAK_GLOW_TEXTURES = {
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/njord_cloak_glow.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/idunn_cloak_glow.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/thor_cloak_glow.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/heimdall_cloak_glow.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/models/cloak/loki_cloak_glow.png")
    };

    @OnlyIn(Dist.CLIENT)
    private static final ModelLayerLocation CLOAK_LAYER = new ModelLayerLocation(new ResourceLocation("botania", "cloak"), "main");

    @OnlyIn(Dist.CLIENT)
    private ModelCloak model;

    static {
        MinecraftForge.EVENT_BUS.register(DivineCloak.class);
    }

    public DivineCloak(Properties properties) {
        super(properties);
    }

    public int getCloakType(ItemStack stack) {
        return stack.getDamageValue();
    }

    public ResourceLocation getCloakTexture(ItemStack stack) {
        int type = getCloakType(stack);
        return type >= 0 && type < CLOAK_TEXTURES.length ?
                CLOAK_TEXTURES[type] : CLOAK_TEXTURES[0];
    }

    public ResourceLocation getCloakGlowTexture(ItemStack stack) {
        int type = getCloakType(stack);
        return type >= 0 && type < CLOAK_GLOW_TEXTURES.length ?
                CLOAK_GLOW_TEXTURES[type] : CLOAK_GLOW_TEXTURES[0];
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player)) {
            return;
        }

        int type = getCloakType(stack);

        // 尼约德斗篷 - 缓降效果
        if (type == NJORD) {
            if ((!player.getAbilities().flying) &&
                    !player.isOnGround() &&
                    !player.isFallFlying() &&
                    !player.isInWater()) {

                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, motion.y + 0.05, motion.z);
            }
            player.fallDistance = 0.0f;
        }
        // 海姆达尔斗篷 - 闪现
        else if (type == HEIMDALL) {
            if (player.level.isClientSide && player.isSprinting()) {
                if (player.getDeltaMovement().y > 0.3 && player.isOnGround()) {
                    Vec3 lookVec = player.getLookAngle();
                    double distance = 6.0;

                    Vec3 targetPos = new Vec3(
                            player.getX() + lookVec.x * distance,
                            player.getY() + lookVec.y * distance,
                            player.getZ() + lookVec.z * distance
                    );

                    BlockPos blockPos = new BlockPos(targetPos);
                    BlockPos blockPosUp = blockPos.above();

                    if (!player.level.getBlockState(blockPos).isSolidRender(player.level, blockPos) &&
                            !player.level.getBlockState(blockPosUp).isSolidRender(player.level, blockPosUp)) {

                        NetworkHandler.CHANNEL.sendToServer(new BlinkMessage());
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack cloak = getCloakFromPlayer(player);
        if (cloak.isEmpty()) {
            return;
        }

        int type = cloak.getDamageValue();

        // 尼约德斗篷 - 完全免疫掉落伤害
        if (type == NJORD) {
            event.setCanceled(true);
        }
        // 托尔斗篷 - 地震效果
        else if (type == THOR) {
            MobEffectInstance jumpBoost = player.getEffect(MobEffects.JUMP);
            float jumpModifier = jumpBoost == null ? 0.0f : (jumpBoost.getAmplifier() + 1);
            float damage = Math.min((event.getDistance() - 3.0f - jumpModifier) * event.getDamageMultiplier(), 5.0f);

            if (damage > 0.0f) {
                event.setCanceled(true);

                // 对周围实体造成伤害
                AABB aabb = player.getBoundingBox().inflate(4.0, 3.0, 4.0);
                List<LivingEntity> entities = player.level.getEntitiesOfClass(
                        LivingEntity.class,
                        aabb,
                        entity -> entity != player && entity.isOnGround()
                );

                for (LivingEntity entity : entities) {
                    entity.hurt(EARTHQUAKE, damage * 2);
                }

                player.hurt(EARTHQUAKE, 0.00005f);

                BlockPos playerPos = player.blockPosition();
                for (BlockPos pos : BlockPos.betweenClosed(
                        playerPos.offset(-1, -1, -1),
                        playerPos.offset(1, -1, 1))) {

                    BlockState state = player.level.getBlockState(pos);
                    if (state.isSolidRender(player.level, pos)) {
                        player.level.levelEvent(2001, pos,
                                net.minecraft.world.level.block.Block.getId(state));
                    }
                }

                player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDamage(LivingAttackEvent event) {
        if (preventRecursion) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack cloak = getCloakFromPlayer(player);
        if (cloak.isEmpty()) {
            return;
        }

        int type = cloak.getDamageValue();

        // 伊登斗篷 - 背后免疫
        if (type == IDUNN && event.getSource().getEntity() != null) {
            Vec3 lookVec = player.getLookAngle().normalize();
            Vec3 attackerPos = event.getSource().getEntity().position();
            Vec3 dirVec = player.position().subtract(attackerPos).normalize();

            double dot = lookVec.dot(dirVec);

            if (dot < INVERSE_EPSILON) {
                event.setCanceled(true);

                if (dot <= EPSILON) {
                    preventRecursion = true;
                    player.hurt(event.getSource(), 0.00005f);
                    preventRecursion = false;
                }
            }
        }
        // 洛基斗篷 - 爆炸和火焰免疫
        else if (type == LOKI) {
            if (event.getSource().isExplosion() || event.getSource().isFire()) {
                event.setCanceled(true);
            }
        }
    }


    private static ItemStack getCloakFromPlayer(Player player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof DivineCloak)
                .map(slotResult -> slotResult.stack())
                .orElse(ItemStack.EMPTY);
    }

    @OnlyIn(Dist.CLIENT)
    private void initModel() {
        if (model == null) {
            try {
                EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
                ModelPart root = modelSet.bakeLayer(CLOAK_LAYER);
                model = new ModelCloak(root);
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.error("Failed to initialize cloak model", e);
            }
        }
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }


        initModel();
        if (model == null) {
            return;
        }

        matrixStack.pushPose();

        if (player.isCrouching()) {
            matrixStack.translate(0.0, 0.2, 0.0);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(28.0f));
        }

        boolean hasChestplate = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        matrixStack.translate(0.0, hasChestplate ? -0.07 : -0.01, 0.0);

        float scale = 0.0625f;
        matrixStack.scale(scale, scale, scale);

        ResourceLocation texture = getCloakTexture(stack);
        model.renderToBuffer(matrixStack,
                renderTypeBuffer.getBuffer(model.renderType(texture)),
                light,
                OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);

        ResourceLocation glowTexture = getCloakGlowTexture(stack);
        model.renderToBuffer(matrixStack,
                renderTypeBuffer.getBuffer(model.renderType(glowTexture)),
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);

        matrixStack.popPose();
    }
}
