package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DragonCrystalArmor extends ArmorItem {
    private static final ResourceLocation OVERLAY = new ResourceLocation("ex_enigmaticlegacy", "textures/overlay.png");

    private static final Map<UUID, Boolean> playerWearingFullSet = new HashMap<>();
    private static final Map<UUID, Boolean> playerOriginalFlightStatus = new HashMap<>();
    private static final Map<UUID, Float> playerOriginalFlySpeed = new HashMap<>();

    private static final int REGENERATION_TICKS = 5;
    private static final float REGENERATION_AMOUNT = 20.0F;
    private static final Map<UUID, Integer> playerRegenerationTimer = new HashMap<>();

    private static final float DRAGON_ARMOR_FLY_SPEED = 0.5F;

    public DragonCrystalArmor(ArmorMaterial material, EquipmentSlot slot) {
        super(material, slot, new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                .durability(0)
                .setNoRepair());
        MinecraftForge.EVENT_BUS.register(DragonCrystalArmor.class);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return slot != EquipmentSlot.LEGS ?
                "ex_enigmaticlegacy:textures/models/armor/dragonarmor_layer_1.png" :
                "ex_enigmaticlegacy:textures/models/armor/dragonarmor_layer_2.png";
    }

    private static boolean isWearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModArmors.dragonArmorBoots.get() &&
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModArmors.dragonArmorLegs.get() &&
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModArmors.dragonArmorChest.get() &&
                player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModArmors.dragonArmorHelm.get();
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (!world.isClientSide) {
            if (!(stack.getItem() instanceof DragonCrystalArmor)) {
                return;
            }

            UUID playerUUID = player.getUUID();
            boolean fullSet = isWearingFullSet(player);

            boolean previousFullSet = playerWearingFullSet.getOrDefault(playerUUID, false);
            playerWearingFullSet.put(playerUUID, fullSet);

            if (fullSet) {
                if (!previousFullSet) {
                    playerOriginalFlightStatus.put(playerUUID, player.getAbilities().mayfly);
                    playerOriginalFlySpeed.put(playerUUID, player.getAbilities().getFlyingSpeed());
                }

                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.getAbilities().setFlyingSpeed(DRAGON_ARMOR_FLY_SPEED);
                    player.onUpdateAbilities();
                } else if (player.getAbilities().getFlyingSpeed() != DRAGON_ARMOR_FLY_SPEED) {
                    player.getAbilities().setFlyingSpeed(DRAGON_ARMOR_FLY_SPEED);
                    player.onUpdateAbilities();
                }
                processRegeneration(player, world);
                applyEffects(player);
            } else {
                if (previousFullSet) {

                    removeAllEffects(player);

                    Boolean originalFlight = playerOriginalFlightStatus.remove(playerUUID);
                    Float originalSpeed = playerOriginalFlySpeed.remove(playerUUID);

                    if (originalFlight != null && originalSpeed != null) {
                        player.getAbilities().mayfly = originalFlight;
                        if (!originalFlight) {
                            player.getAbilities().flying = false;
                        }
                        player.getAbilities().setFlyingSpeed(originalSpeed);
                        player.onUpdateAbilities();
                    }
                    playerRegenerationTimer.remove(playerUUID);
                    playerWearingFullSet.put(playerUUID, false);
                }
            }
        }
    }

    private void processRegeneration(Player player, Level level) {
        if (level.isClientSide) return;

        UUID playerUUID = player.getUUID();
        int timer = playerRegenerationTimer.getOrDefault(playerUUID, 0);
        timer++;

        if (timer >= REGENERATION_TICKS) {
            timer = 0;

            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(REGENERATION_AMOUNT);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.3F, 0.5F + level.random.nextFloat() * 0.5F);

                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    spawnHealingParticles(player, serverLevel);
                }
            }
        }

        playerRegenerationTimer.put(playerUUID, timer);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            Player player = (Player) event.getEntityLiving();
            if (isWearingFullSet(player)) {
                if (event.getSource().isExplosion()) {
                    event.setCanceled(true);
                    return;
                }
                else {
                    float originalDamage = event.getAmount();
                    float reducedDamage = originalDamage * 0.1f;
                    event.setAmount(reducedDamage);

                    if (player.level instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel) player.level;
                        spawnDamageParticles(player, serverLevel);
                    }
                }
            }
        }
    }

    private void applyEffects(Player player) {
        MobEffectInstance dmgBoost = player.getEffect(MobEffects.DAMAGE_BOOST);
        if (dmgBoost == null) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 127, false, false));
        }

        MobEffectInstance waterBreathing = player.getEffect(MobEffects.WATER_BREATHING);
        if (waterBreathing == null) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, Integer.MAX_VALUE, 10, false, false));
        }

        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (speed == null) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 40, false, false));
        }

        MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
        if (nightVision == null) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 5, false, false));
        }

        MobEffectInstance fireResistance = player.getEffect(MobEffects.FIRE_RESISTANCE);
        if (fireResistance == null) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 5, false, false));
        }
    }

    private void removeAllEffects(Player player) {
        removeEffectIfInfinite(player, MobEffects.DAMAGE_BOOST);
        removeEffectIfInfinite(player, MobEffects.WATER_BREATHING);
        removeEffectIfInfinite(player, MobEffects.MOVEMENT_SPEED);
        removeEffectIfInfinite(player, MobEffects.NIGHT_VISION);
        removeEffectIfInfinite(player, MobEffects.FIRE_RESISTANCE);
    }

    private void removeEffectIfInfinite(Player player, MobEffect effect) {
        MobEffectInstance instance = player.getEffect(effect);
        if (instance != null && instance.getDuration() > 20000) {
            player.removeEffect(effect);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderHelmetOverlay(PoseStack poseStack, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        int amount = 0;
        if (player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModArmors.dragonArmorBoots.get()) amount++;
        if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModArmors.dragonArmorLegs.get()) amount++;
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModArmors.dragonArmorChest.get()) amount++;
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModArmors.dragonArmorHelm.get()) amount++;


        if (amount >= 4) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, OVERLAY);

            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();

            GuiComponent.blit(poseStack, 0, 0, 0, 0, width, height, width, height);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.0F);
        return super.use(world, player, hand);
    }

    private void spawnHealingParticles(Player player, ServerLevel serverLevel) {
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI * 2 / 16;
            double radius = 0.7;
            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getY() + 1.0;
            double z = player.getZ() + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    3,
                    0.02, 0.1, 0.02,
                    0.05
            );

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    1,
                    0.05, 0.05, 0.05,
                    0.01
            );
        }

        for (int i = 0; i < 8; i++) {
            double yOffset = 2.0 + i * 0.25;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(), player.getY() + yOffset, player.getZ(),
                    1,
                    0.1, 0.0, 0.1,
                    0.02
            );

            serverLevel.sendParticles(
                    ParticleTypes.GLOW,
                    player.getX(), player.getY() + yOffset, player.getZ(),
                    1,
                    0.15, 0.05, 0.15,
                    0.07
            );
        }
    }

    private static void spawnDamageParticles(Player player, ServerLevel serverLevel) {
        for (int i = 0; i < 30; i++) {
            double phi = Math.PI * 2 * serverLevel.random.nextDouble();
            double theta = Math.PI * serverLevel.random.nextDouble();

            double radius = 1.0;
            double x = player.getX() + radius * Math.sin(theta) * Math.cos(phi);
            double y = player.getY() + 1.0 + radius * Math.cos(theta);
            double z = player.getZ() + radius * Math.sin(theta) * Math.sin(phi);

            double velocityX = (x - player.getX()) * 0.1;
            double velocityY = (y - (player.getY() + 1.0)) * 0.1;
            double velocityZ = (z - player.getZ()) * 0.1;

            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    x, y, z,
                    1,
                    velocityX, velocityY, velocityZ,
                    0.05
            );

            if (i % 3 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        x, y, z,
                        1,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5,
                        0.02
                );
            }
        }
    }
}