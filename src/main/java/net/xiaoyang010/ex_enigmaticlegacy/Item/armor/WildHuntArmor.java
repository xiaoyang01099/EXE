package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelArmorWildHunt;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.CloudJumpParticlePacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.JumpPacket;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class WildHuntArmor extends ItemManasteelArmor implements IManaItem, IManaProficiencyArmor {
    private static ItemStack[] armorSet;
    private static final Properties WILD_HUNT_ARMOR = new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).durability(0).rarity(EXEAPI.rarityWildHunt).setNoRepair();
    private static final String TAG_MANA = "mana";
    private static final int MAX_MANA = 500000;
    private static final ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();
    private static final Map<UUID, Long> lastHealthRegenTime = new HashMap<>();
    private static final float HEALTH_REGEN_AMOUNT = 10.0F;
    private static final long HEALTH_REGEN_INTERVAL_MS = 1000L;
    private static final Map<UUID, Boolean> canDoubleJump = new HashMap<>();
    private static final Map<UUID, Boolean> hasDoubleJumped = new HashMap<>();
    private static final Map<UUID, Boolean> playerEffectsActive = new HashMap<>();
    private static final UUID JUMP_STRENGTH_UUID = UUID.fromString("A5B719E6-34D3-47C1-B98F-89A5F1B7D373");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("2B5D5D6C-3E1D-4D95-A7B2-D7A3F4C52B24");
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("DF82A3E7-8521-4F98-B702-99D194061D63");
    private static final UUID ARMOR_UUID = UUID.fromString("9C54B336-9211-4E9F-A4D8-0A77A1D5C6E2");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("AB34991E-F4D7-4644-BFB1-AC024A3770FB");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("8742DE69-717F-4B68-8E61-527AA5880262");

    public WildHuntArmor(EquipmentSlot slot) {
        super(slot, EXEAPI.wildHuntArmor, WILD_HUNT_ARMOR);
    }

    public ItemStack[] getArmorSetStacks() {
        if (armorSet == null) {
            armorSet = new ItemStack[]{
                    new ItemStack(ModArmors.WILD_HUNT_HELMET.get()),
                    new ItemStack(ModArmors.WILD_HUNT_CHESTPLATE.get()),
                    new ItemStack(ModArmors.WILD_HUNT_LEGGINGS.get()),
                    new ItemStack(ModArmors.WILD_HUNT_BOOTS.get())
            };
        }
        return armorSet;
    }

    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        switch (slot) {
            case HEAD:
                return stack.is(ModArmors.WILD_HUNT_HELMET.get());
            case CHEST:
                return stack.is(ModArmors.WILD_HUNT_CHESTPLATE.get());
            case LEGS:
                return stack.is(ModArmors.WILD_HUNT_LEGGINGS.get());
            case FEET:
                return stack.is(ModArmors.WILD_HUNT_BOOTS.get());
            default:
                return false;
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        CURRENT_STACK.set(stack);

        try {
            if (!world.isClientSide() && getMana() != getMaxMana() &&
                    ManaItemHandler.instance().requestManaExactForTool(stack, player, 1000, true)) {
                addMana(1000);
            }

            if (!world.isClientSide) {
                UUID playerUUID = player.getUUID();
                boolean fullSet = isWearingFullSet(player);
                boolean previouslyActive = playerEffectsActive.getOrDefault(playerUUID, false);
                long currentTime = System.currentTimeMillis();

                if (fullSet) {
                    if (!previouslyActive) {
                        applyArmorEffects(player);
                        playerEffectsActive.put(playerUUID, true);
                    }

                    MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
                    if (nightVision == null || nightVision.getDuration() < 210) {
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
                    }

                    MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                    if (resistance == null || resistance.getAmplifier() < 3 || resistance.getDuration() < 5) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3, false, false));
                    }

                    if (player.hasEffect(ModEffects.DROWNING.get())) {
                        player.removeEffect(ModEffects.DROWNING.get());
                    }

                    if (player.hasEffect(MobEffects.POISON)) {
                        player.removeEffect(MobEffects.POISON);
                    }

                    if (player.hasEffect(MobEffects.WITHER)) {
                        player.removeEffect(MobEffects.WITHER);
                    }

                    if (!lastHealthRegenTime.containsKey(playerUUID) ||
                            currentTime - lastHealthRegenTime.get(playerUUID) >= HEALTH_REGEN_INTERVAL_MS) {

                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(HEALTH_REGEN_AMOUNT);
                        }
                        lastHealthRegenTime.put(playerUUID, currentTime);
                    }

                    MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
                    if (speed == null || speed.getDuration() < 5) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 5, false, false));
                    }

                    updateDoubleJumpStatus(player);

                } else if (previouslyActive) {
                    removeArmorEffects(player);
                    playerEffectsActive.put(playerUUID, false);
                }
            }
        } finally {
            CURRENT_STACK.remove();
        }
    }

    private void applyArmorEffects(Player player) {
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).addTransientModifier(
                new AttributeModifier(ATTACK_DAMAGE_UUID, "", 50.0, AttributeModifier.Operation.ADDITION));
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).addTransientModifier(
                new AttributeModifier(MAX_HEALTH_UUID, "", 40.0, AttributeModifier.Operation.ADDITION));
        Objects.requireNonNull(player.getAttribute(Attributes.ARMOR)).addTransientModifier(
                new AttributeModifier(ARMOR_UUID, "", 1000.0, AttributeModifier.Operation.ADDITION));
        Objects.requireNonNull(player.getAttribute(Attributes.ARMOR_TOUGHNESS)).addTransientModifier(
                new AttributeModifier(ARMOR_TOUGHNESS_UUID, "", 1000.0, AttributeModifier.Operation.ADDITION));
        Objects.requireNonNull(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).addTransientModifier(
                new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "", 1.0, AttributeModifier.Operation.ADDITION));
        Objects.requireNonNull(player.getAttribute(ForgeMod.ENTITY_GRAVITY.get())).addTransientModifier(
                new AttributeModifier(JUMP_STRENGTH_UUID, "", -0.04, AttributeModifier.Operation.ADDITION));
        player.setHealth(player.getHealth() + 30.0f);

        UUID playerUUID = player.getUUID();
        canDoubleJump.put(playerUUID, true);
        hasDoubleJumped.put(playerUUID, false);
    }

    @SubscribeEvent
    public static void onPlayerJump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            Player player = (Player) event.getEntityLiving();
            if (isWearingFullSet(player)) {
                player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y * 2, player.getDeltaMovement().z);
            }
        }
    }

    private void updateDoubleJumpStatus(Player player) {
        UUID playerUUID = player.getUUID();

        if (player.isOnGround()) {
            canDoubleJump.put(playerUUID, true);
            hasDoubleJumped.put(playerUUID, false);
        }
    }

    public static void performDoubleJump(Player player) {
        if (!player.level.isClientSide && isWearingFullSet(player)) {
            UUID playerUUID = player.getUUID();

            if (canDoubleJump.getOrDefault(playerUUID, false) &&
                    !hasDoubleJumped.getOrDefault(playerUUID, false)) {

                player.setDeltaMovement(player.getDeltaMovement().x, 0.5, player.getDeltaMovement().z);

                player.playSound(SoundEvents.WITHER_SHOOT, 0.3F, 1.5F);
                player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 0.4F, 0.8F);
                player.playSound(SoundEvents.BLAZE_SHOOT, 0.25F, 0.7F);

                double damageRange = 3.5D;
                List<LivingEntity> nearbyEntities = player.level.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(
                                player.getX() - damageRange, player.getY() - 1.0, player.getZ() - damageRange,
                                player.getX() + damageRange, player.getY() + 2.0, player.getZ() + damageRange
                        )
                );

                for (LivingEntity entity : nearbyEntities) {
                    if (entity != player) {
                        DamageSource absoluteDamage = new DamageSource("wild_hunt_jump")
                                .bypassArmor()
                                .bypassMagic()
                                .bypassInvul()
                                .setMagic();

                        if (entity instanceof Mob) {
                            Mob mob = (Mob) entity;
                            LivingEntity originalTarget = mob.getTarget();

                            entity.hurt(absoluteDamage, 100.0F);

                            if (originalTarget != null && originalTarget.isAlive()) {
                                mob.setTarget(originalTarget);
                            } else if (mob.getTarget() == player) {
                                mob.setTarget(null);
                            }
                        } else {
                            entity.hurt(absoluteDamage, 100.0F);
                        }
                    }
                }

                CloudJumpParticlePacket packet = new CloudJumpParticlePacket(
                        player.getX(), player.getY(), player.getZ()
                );

                for (ServerPlayer serverPlayer : Objects.requireNonNull(player.getServer()).getPlayerList().getPlayers()) {
                    if (serverPlayer.level == player.level &&
                            serverPlayer.distanceToSqr(player) < 64 * 64) {
                        NetworkHandler.sendToPlayer(serverPlayer, packet);
                    }
                }

                hasDoubleJumped.put(playerUUID, true);
                canDoubleJump.put(playerUUID, false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null && !mc.player.isOnGround() && mc.options.keyJump.isDown()) {
                UUID playerUUID = mc.player.getUUID();
                if (isWearingFullSet(mc.player) &&
                        canDoubleJump.getOrDefault(playerUUID, false) &&
                        !hasDoubleJumped.getOrDefault(playerUUID, false)) {

                    NetworkHandler.sendToServer(new JumpPacket());

                    mc.player.playSound(SoundEvents.WITHER_SHOOT, 0.3F, 1.5F);
                    mc.player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 0.4F, 0.8F);
                    mc.player.playSound(SoundEvents.BLAZE_SHOOT, 0.25F, 0.7F);

                    spawnCloudParticlesClient(mc.player);

                    mc.player.jumpFromGround();

                    hasDoubleJumped.put(playerUUID, true);
                    canDoubleJump.put(playerUUID, false);
                }
            }
        }
    }

    private void removeArmorEffects(Player player) {
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).removeModifier(ATTACK_DAMAGE_UUID);
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).removeModifier(MAX_HEALTH_UUID);
        Objects.requireNonNull(player.getAttribute(Attributes.ARMOR)).removeModifier(ARMOR_UUID);
        Objects.requireNonNull(player.getAttribute(Attributes.ARMOR_TOUGHNESS)).removeModifier(ARMOR_TOUGHNESS_UUID);
        Objects.requireNonNull(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).removeModifier(KNOCKBACK_RESISTANCE_UUID);
        Objects.requireNonNull(player.getAttribute(ForgeMod.ENTITY_GRAVITY.get())).removeModifier(JUMP_STRENGTH_UUID);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            Player attacker = (Player) event.getSource().getEntity();
            if (isWearingFullSet(attacker)) {
                event.setCanceled(true);
                float originalDamage = event.getAmount();
                LivingEntity target = event.getEntityLiving();
                DamageSource absoluteDamage = ModDamageSources.ABSOLUTE;
                target.hurt(absoluteDamage, originalDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            Player player = (Player) event.getEntityLiving();
            if (isWearingFullSet(player)) {
                if (event.getSource().isExplosion()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            Player player = (Player) event.getEntityLiving();
            if (isWearingFullSet(player)) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getArmorModel(LivingEntity entity, ItemStack stack,
                                                  EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                ModelPart modelPart = Minecraft.getInstance().getEntityModels()
                        .bakeLayer(ModelArmorWildHunt.LAYER_LOCATION);

                HumanoidModel<?> armorModel;
                switch (slot) {
                    case HEAD:
                        armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
                                Map.of("head", new ModelArmorWildHunt<>(modelPart, slot).head,
                                        "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                        break;
                    case CHEST:
                        armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
                                Map.of("body", new ModelArmorWildHunt<>(modelPart, slot).chest,
                                        "right_arm", new ModelArmorWildHunt<>(modelPart, slot).rightArm,
                                        "left_arm", new ModelArmorWildHunt<>(modelPart, slot).leftArm,
                                        "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                        break;
                    case LEGS:
                        armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
                                Map.of("right_leg", new ModelArmorWildHunt<>(modelPart, slot).rightLeg,
                                        "left_leg", new ModelArmorWildHunt<>(modelPart, slot).leftLeg,
                                        "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                        break;
                    case FEET:
                        armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
                                Map.of("right_leg", new ModelArmorWildHunt<>(modelPart, slot).rightBoot,
                                        "left_leg", new ModelArmorWildHunt<>(modelPart, slot).leftBoot,
                                        "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                        "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                        break;
                    default:
                        return defaultModel;
                }

                armorModel.crouching = entity.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = entity.isBaby();
                return armorModel;
            }
        });
    }

    public static boolean isWearingFullSet(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof WildHuntArmor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc0"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc1"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc2"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc5"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc6"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc7"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc8"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc9"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.wild_hunt_armor.desc10"));
        tooltip.add(new TranslatableComponent("item.info.mana",
                getManaInternal(stack), getMaxMana())
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    @Override
    public int getMana() {
        ItemStack stack = CURRENT_STACK.get();
        if (stack != null) {
            return getManaInternal(stack);
        }
        return 0;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
        ItemStack stack = CURRENT_STACK.get();
        if (stack != null) {
            int currentMana = getManaInternal(stack);
            int newMana = Math.min(currentMana + mana, getMaxMana());
            setManaInternal(stack, newMana);
        }
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean isNoExport() {
        return true;
    }

    public static int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    public static void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.min(mana, MAX_MANA));
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return ExEnigmaticlegacyMod.MODID + ":textures/models/armor/wild_hunt_armor.png";
    }

    private static void spawnCloudParticlesClient(Player player) {
        Level level = player.level;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        for (int i = 0; i < 80; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * 1.8D;
            double offsetX = Math.sin(angle) * distance;
            double offsetZ = Math.cos(angle) * distance;

            double offsetY = level.random.nextDouble() * 0.4D;

            level.addParticle(
                    ParticleTypes.CLOUD,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    offsetX * 0.08D,
                    level.random.nextDouble() * 0.1D,
                    offsetZ * 0.08D
            );

            if (i % 2 == 0) {
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        x + offsetX * 0.7D,
                        y + offsetY * 0.5D,
                        z + offsetZ * 0.7D,
                        offsetX * 0.1D,
                        level.random.nextDouble() * 0.15D,
                        offsetZ * 0.1D
                );
            }

            if (i % 6 == 0) {
                double blastDistance = distance * 1.2;
                double blastX = Math.sin(angle) * blastDistance;
                double blastZ = Math.cos(angle) * blastDistance;

                level.addParticle(
                        ParticleTypes.POOF,
                        x + blastX,
                        y + 0.1D,
                        z + blastZ,
                        blastX * 0.15D,
                        0.05D,
                        blastZ * 0.15D
                );
            }

            if (i % 7 == 0) {
                double flameAngle = angle + (level.random.nextDouble() - 0.5) * 0.5;
                double flameDistance = distance * 0.9;
                double flameX = Math.sin(flameAngle) * flameDistance;
                double flameZ = Math.cos(flameAngle) * flameDistance;

                level.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        x + flameX,
                        y + level.random.nextDouble() * 0.3D,
                        z + flameZ,
                        0,
                        0.1D + level.random.nextDouble() * 0.1D,
                        0
                );
            }

            if (i % 10 == 0 && distance < 0.7) {
                level.addParticle(
                        ParticleTypes.SMALL_FLAME,
                        x + offsetX * 0.5D,
                        y + 0.05D,
                        z + offsetZ * 0.5D,
                        0,
                        0.15D + level.random.nextDouble() * 0.1D,
                        0
                );
            }

            if (i % 8 == 0) {
                double ashDistance = distance * 1.3;
                double ashX = Math.sin(angle + level.random.nextDouble() * 0.3) * ashDistance;
                double ashZ = Math.cos(angle + level.random.nextDouble() * 0.3) * ashDistance;

                level.addParticle(
                        ParticleTypes.ASH,
                        x + ashX,
                        y + level.random.nextDouble() * 0.5D,
                        z + ashZ,
                        (level.random.nextDouble() - 0.5) * 0.1D,
                        level.random.nextDouble() * 0.05D,
                        (level.random.nextDouble() - 0.5) * 0.1D
                );
            }
        }
    }
}