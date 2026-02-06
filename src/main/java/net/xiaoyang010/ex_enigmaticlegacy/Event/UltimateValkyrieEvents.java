package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieChestplate;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;

import java.util.EnumSet;
import java.util.UUID;

import static net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie.isFullSuit;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class UltimateValkyrieEvents {
    private static final String NBT_SHIELD_COUNT = "ValkyrieShieldCount";
    private static final String NBT_COOLDOWN_TIME = "ValkyrieShieldCooldown";
    private static final String NBT_OWNER_UUID = "ValkyrieOwnerUUID";
    private static final String NBT_ENHANCED_SKELETON = "ValkyrieEnhanced";
    private static final String NBT_WITHER_KILL_MARK = "ValkyrieWitherKill";
    private static final String NBT_BONUS_HEALTH = "ValkyrieBonusHealth";
    private static final String NBT_SKELETON_COUNT = "ValkyrieSkeletonCount";
    private static final float HEALTH_PER_SKELETON = 2.0F;
    private static final int MAX_SHIELD_COUNT = 25;
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("6C5B2A4E-3D1F-4E9B-8A7C-1F2E3D4C5B6A");
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("7D6C3B2A-4E1F-5F9C-9B8A-2F3F4E5D6C7B");

    public static class FriendlyWitherSkeletonTargetGoal extends Goal {
        private final WitherSkeleton skeleton;
        private LivingEntity targetMob;
        private final int randomInterval = 10;

        public FriendlyWitherSkeletonTargetGoal(WitherSkeleton skeleton) {
            this.skeleton = skeleton;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            CompoundTag tag = skeleton.getPersistentData();
            if (!tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                return false;
            }

            if (tag.contains("PriorityTarget")) {
                UUID priorityTargetUUID = tag.getUUID("PriorityTarget");
                LivingEntity priorityTarget = findEntityByUUID(priorityTargetUUID);
                if (priorityTarget != null && priorityTarget.isAlive()) {
                    targetMob = priorityTarget;
                    return true;
                } else {
                    tag.remove("PriorityTarget");
                }
            }

            if (skeleton.getRandom().nextInt(randomInterval) != 0) {
                return false;
            }

            findTarget();
            return targetMob != null;
        }

        @Override
        public boolean canContinueToUse() {
            CompoundTag tag = skeleton.getPersistentData();
            if (!tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                return false;
            }

            if (tag.contains("PriorityTarget")) {
                UUID priorityTargetUUID = tag.getUUID("PriorityTarget");
                LivingEntity priorityTarget = findEntityByUUID(priorityTargetUUID);
                if (priorityTarget != null && priorityTarget.isAlive()) {
                    if (targetMob != priorityTarget) {
                        targetMob = priorityTarget;
                        skeleton.setTarget(targetMob);
                    }
                    return true;
                } else {
                    tag.remove("PriorityTarget");
                }
            }

            if (targetMob == null || !targetMob.isAlive()) {
                return false;
            }

            UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
            if (targetMob instanceof Player player && player.getUUID().equals(ownerUUID)) {
                return false;
            }

            return skeleton.distanceToSqr(targetMob) <= 256.0D;
        }

        @Override
        public void start() {
            skeleton.setTarget(targetMob);

            CompoundTag tag = skeleton.getPersistentData();
            if (tag.contains("PriorityTarget")) {
                skeleton.level.playSound(null, skeleton.blockPosition(),
                        SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.HOSTILE, 1.0F, 2.0F);
            }
        }

        @Override
        public void stop() {
            CompoundTag tag = skeleton.getPersistentData();
            if (targetMob != null && !targetMob.isAlive()) {
                tag.remove("PriorityTarget");
            }
            skeleton.setTarget(null);
            targetMob = null;
        }

        private void findTarget() {
            CompoundTag tag = skeleton.getPersistentData();
            UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
            AABB searchArea = skeleton.getBoundingBox().inflate(16.0D);

            targetMob = skeleton.level.getEntitiesOfClass(LivingEntity.class, searchArea, mob -> {
                if (mob == skeleton) return false;

                if (mob instanceof WitherSkeleton) return false;

                if (mob instanceof Player player && player.getUUID().equals(ownerUUID)) {
                    return false;
                }

                if (mob instanceof net.minecraft.world.entity.animal.Animal ||
                        mob instanceof net.minecraft.world.entity.npc.Villager ||
                        mob instanceof net.minecraft.world.entity.animal.IronGolem) {
                    return false;
                }

                if (mob instanceof Player) {
                    return false;
                }

                return mob.isAlive() && skeleton.getSensing().hasLineOfSight(mob);
            }).stream().min((a, b) -> Double.compare(
                    skeleton.distanceToSqr(a),
                    skeleton.distanceToSqr(b)
            )).orElse(null);
        }

        private LivingEntity findEntityByUUID(UUID uuid) {
            if (skeleton.level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    return livingEntity;
                }
            }
            return null;
        }
    }

    /**
     *** 更新玩家的血量上限
    */
    private static void updatePlayerMaxHealth(Player player) {
        if (!UltimateValkyrie.isFullSuit(player)) {
            removeHealthBonus(player);
            return;
        }

        int skeletonCount = getAliveSkeletonCount(player);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        CompoundTag tag = chestplate.getOrCreateTag();
        tag.putInt(NBT_SKELETON_COUNT, skeletonCount);

        float bonusHealth = skeletonCount * HEALTH_PER_SKELETON;

        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr.getModifier(HEALTH_MODIFIER_UUID) != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
        }

        if (bonusHealth > 0) {
            healthAttr.addTransientModifier(
                    new AttributeModifier(HEALTH_MODIFIER_UUID, "Valkyrie Skeleton Health Bonus",
                            bonusHealth, AttributeModifier.Operation.ADDITION)
            );
            tag.putFloat(NBT_BONUS_HEALTH, bonusHealth);
        } else {
            tag.remove(NBT_BONUS_HEALTH);
        }
    }

    /**
     * 移除血量加成
     */
    private static void removeHealthBonus(Player player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr.getModifier(HEALTH_MODIFIER_UUID) != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestplate.isEmpty()) {
            CompoundTag tag = chestplate.getOrCreateTag();
            tag.remove(NBT_BONUS_HEALTH);
            tag.remove(NBT_SKELETON_COUNT);
        }
    }

    /**
     * 凋零骷髅死亡时更新玩家血量
     */
    @SubscribeEvent
    public static void onSkeletonDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof WitherSkeleton skeleton) {
            CompoundTag tag = skeleton.getPersistentData();
            if (tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                if (skeleton.level instanceof ServerLevel serverLevel) {
                    Player owner = serverLevel.getPlayerByUUID(ownerUUID);
                    if (owner != null) {
                        serverLevel.getServer().execute(() -> {
                            updatePlayerMaxHealth(owner);
                            int remainingCount = getAliveSkeletonCount(owner);
                            owner.displayClientMessage(
                                    Component.nullToEmpty("§c凋零骷髅阵亡！剩余: §6" + remainingCount),
                                    true
                            );
                        });
                    }
                }
            }
        }
    }

    /**
     * 获取存活的凋零骷髅数量
     */
    private static int getAliveSkeletonCount(Player player) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        return (int) serverLevel.getEntitiesOfClass(WitherSkeleton.class,
                        player.getBoundingBox().inflate(128.0D))
                .stream()
                .filter(skeleton -> {
                    CompoundTag tag = skeleton.getPersistentData();
                    if (!tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                        return false;
                    }
                    UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                    return ownerUUID.equals(player.getUUID()) && skeleton.isAlive();
                })
                .count();
    }

    private static boolean isValkyrieArmorPiece(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == ModArmors.ULTIMATE_VALKYRIE_HELMET.get() ||
                item == ModArmors.ULTIMATE_VALKYRIE_CHESTPLATE.get() ||
                item == ModArmors.ULTIMATE_VALKYRIE_LEGGINGS.get() ||
                item == ModArmors.ULTIMATE_VALKYRIE_BOOTS.get();
    }

    /**
     * 玩家脱下装备时移除血量加成
     */
    @SubscribeEvent
    public static void onArmorChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level.isClientSide) {
            if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
                if (player.level instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> {
                        boolean wasValkyrieArmor = isValkyrieArmorPiece(event.getFrom());
                        boolean isValkyrieArmor = isValkyrieArmorPiece(event.getTo());

                        if (wasValkyrieArmor || isValkyrieArmor) {
                            if (!UltimateValkyrie.isFullSuit(player)) {
                                removeHealthBonus(player);

                                if (wasValkyrieArmor) {
                                    player.displayClientMessage(
                                            Component.nullToEmpty("§c武神套装效果已失效！"),
                                            true
                                    );
                                }
                            } else {
                                updatePlayerMaxHealth(player);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * 当主人被攻击时，命令附近的友军凋零骷髅反击
     */
    @SubscribeEvent
    public static void onOwnerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player owner) {
            if (!UltimateValkyrie.isFullSuit(owner)) {
                return;
            }

            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                if (attacker == owner) {
                    return;
                }

                if (owner.level instanceof ServerLevel serverLevel) {
                    AABB searchArea = owner.getBoundingBox().inflate(32.0D);
                    serverLevel.getEntitiesOfClass(WitherSkeleton.class, searchArea)
                            .forEach(skeleton -> {
                                CompoundTag tag = skeleton.getPersistentData();
                                if (tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                                    UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                                    if (ownerUUID.equals(owner.getUUID())) {
                                        tag.putUUID("PriorityTarget", attacker.getUUID());
                                        skeleton.setTarget(attacker);

                                        skeleton.level.playSound(null, skeleton.blockPosition(),
                                                SoundEvents.WITHER_SKELETON_HURT,
                                                SoundSource.HOSTILE, 1.0F, 0.8F);
                                    }
                                }
                            });

                    if (!serverLevel.getEntitiesOfClass(WitherSkeleton.class, searchArea,
                            s -> s.getPersistentData().getBoolean(NBT_ENHANCED_SKELETON) &&
                                    s.getPersistentData().getUUID(NBT_OWNER_UUID).equals(owner.getUUID())).isEmpty()) {
                        owner.displayClientMessage(
                                Component.nullToEmpty("§5§l友军凋零骷髅正在支援！"),
                                true
                        );
                    }
                }
            }
        }
    }

    /**
     * 全套免疫凋零效果
     */
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            if (UltimateValkyrie.isFullSuit(player)) {
                if (event.getPotionEffect().getEffect() == MobEffects.WITHER) {
                    event.setResult(Event.Result.DENY);
                    player.removeEffect(MobEffects.WITHER);
                }
            }
        }
    }

    /**
     * 武神盔甲攻击效果 - 吸血和凋零（全套翻倍）
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.getItem() instanceof UltimateValkyrieChestplate) {
                if (event.getEntity() instanceof LivingEntity target) {
                    boolean isFullSuit = UltimateValkyrie.isFullSuit(player);
                    int witherLevel = isFullSuit ? 9 : 4;
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, witherLevel, false, true));

                    CompoundTag tag = target.getPersistentData();
                    tag.putUUID(NBT_WITHER_KILL_MARK, player.getUUID());
                    tag.putLong(NBT_WITHER_KILL_MARK + "_Time", target.level.getGameTime());

                    float damage = event.getAmount();
                    float healPercentage = isFullSuit ? 0.6F : 0.3F;
                    float healAmount = damage * healPercentage;
                    if (player.getHealth() < player.getMaxHealth() && healAmount > 0) {
                        player.heal(healAmount);
                        if (healAmount > 0.5F) {
                            player.displayClientMessage(
                                    Component.nullToEmpty("§c+❤ " + String.format("%.1f", healAmount) +
                                            (isFullSuit ? " §6(双倍)" : "")),
                                    true
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * 友军凋零骷髅攻击时也标记
     */
    @SubscribeEvent
    public static void onSkeletonAttack(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof WitherSkeleton skeleton) {
            CompoundTag skeletonTag = skeleton.getPersistentData();
            if (skeletonTag.getBoolean(NBT_ENHANCED_SKELETON)) {
                if (event.getEntity() instanceof LivingEntity target) {
                    UUID ownerUUID = skeletonTag.getUUID(NBT_OWNER_UUID);
                    CompoundTag targetTag = target.getPersistentData();
                    targetTag.putUUID(NBT_WITHER_KILL_MARK + "_Skeleton", ownerUUID);
                    targetTag.putLong(NBT_WITHER_KILL_MARK + "_Skeleton_Time", target.level.getGameTime());
                }
            }
        }
    }

    /**
     * 凋零骷髅生成时的增强处理
     */
    @SubscribeEvent
    public static void onWitherSkeletonSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (event.getEntity() instanceof WitherSkeleton witherSkeleton) {
            CompoundTag tag = witherSkeleton.getPersistentData();

            if (tag.contains(NBT_OWNER_UUID) && tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                witherSkeleton.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                        new AttributeModifier(DAMAGE_MODIFIER_UUID, "Valkyrie Enhancement", 4.0D,
                                AttributeModifier.Operation.MULTIPLY_TOTAL)
                );

                witherSkeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
                witherSkeleton.setHealth(40.0F);
                witherSkeleton.setGlowingTag(true);
                ItemStack netheriteSword = new ItemStack(Items.NETHERITE_SWORD);
                witherSkeleton.setItemSlot(EquipmentSlot.MAINHAND, netheriteSword);
                witherSkeleton.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            }
        }
    }

    /**
     * 防止凋零骷髅设置玩家为目标（只保护穿全套的玩家）
     */
    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof WitherSkeleton skeleton) {
            CompoundTag tag = skeleton.getPersistentData();
            if (tag.getBoolean(NBT_ENHANCED_SKELETON) && event.getTarget() instanceof Player player) {
                UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                if (player.getUUID().equals(ownerUUID)) {
                    if (UltimateValkyrie.isFullSuit(player)) {
                        skeleton.setTarget(null);
//                    } else if (!tag.getBoolean("ShouldRestore")) {
//                        tag.putBoolean("ShouldRestore", true);
//                        restoreSkeletonHostile(skeleton);
                    }
                }
            }
        }
    }

    /**
     * 全套盔甲效果 - 凋零骷髅友军系统
     */
    @SubscribeEvent
    public static void onAiTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }
        Player player = event.player;

        if (UltimateValkyrie.isFullSuit(player) && player.level instanceof ServerLevel serverLevel) {
            if (player.tickCount % 20 == 0) {
                serverLevel.getEntitiesOfClass(WitherSkeleton.class,
                                player.getBoundingBox().inflate(16.0D))
                        .forEach(skeleton -> {
                            CompoundTag tag = skeleton.getPersistentData();
                            if (!tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                                makeSkeletonFriendly(skeleton, player);
                            } else {
                                UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                                if (ownerUUID.equals(player.getUUID())) {
                                    if (skeleton.getTarget() == player) {
                                        skeleton.setTarget(null);
                                    }
                                    tag.remove("ShouldRestore");
                                }
                            }
                        });
            }
        }
    }

//        else if (!UltimateValkyrie.isFullSuit(player) && player.level instanceof ServerLevel serverLevel) {
//            if (player.tickCount % 20 == 0) {
//                serverLevel.getEntitiesOfClass(WitherSkeleton.class,
//                                player.getBoundingBox().inflate(32.0D))
//                        .forEach(skeleton -> {
//                            CompoundTag tag = skeleton.getPersistentData();
//                            if (tag.getBoolean(NBT_ENHANCED_SKELETON)) {
//                                UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
//                                if (ownerUUID.equals(player.getUUID())) {
//                                    if (!tag.getBoolean("ShouldRestore")) {
//                                        tag.putBoolean("ShouldRestore", true);
//
//                                        player.displayClientMessage(
//                                                Component.nullToEmpty("§c§l警告：凋零骷髅失去控制！"),
//                                                true
//                                        );
//
//                                        restoreSkeletonHostile(skeleton);
//                                    }
//                                }
//                            }
//                        });




    /**
     * 恢复凋零骷髅的敌对状态（使用调度器避免并发修改）
     */
//    private static void restoreSkeletonHostile(WitherSkeleton skeleton) {
//        if (skeleton.level instanceof ServerLevel serverLevel) {
//            serverLevel.getServer().execute(() -> {
//                CompoundTag tag = skeleton.getPersistentData();
//
//                tag.remove(NBT_OWNER_UUID);
//                tag.putBoolean(NBT_ENHANCED_SKELETON, false);
//                tag.remove("PriorityTarget");
//
//                skeleton.setGlowingTag(false);
//
//                skeleton.targetSelector.removeAllGoals();
//                skeleton.goalSelector.removeAllGoals();
//
//                skeleton.goalSelector.addGoal(0, new FloatGoal(skeleton));
//                skeleton.goalSelector.addGoal(2, new MeleeAttackGoal(skeleton, 1.0D, false));
//                skeleton.goalSelector.addGoal(5, new RandomStrollGoal(skeleton, 1.0D));
//                skeleton.goalSelector.addGoal(6, new LookAtPlayerGoal(skeleton, Player.class, 8.0F));
//                skeleton.goalSelector.addGoal(7, new RandomLookAroundGoal(skeleton));
//
//                skeleton.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(skeleton, Player.class, true));
//                skeleton.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(skeleton,
//                        net.minecraft.world.entity.animal.IronGolem.class, true));
//
//                if (skeleton.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(DAMAGE_MODIFIER_UUID) != null) {
//                    skeleton.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MODIFIER_UUID);
//                }
//
//                skeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
//                skeleton.setHealth(Math.min(skeleton.getHealth(), 20.0F));
//                skeleton.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0D);
//
//                skeleton.level.playSound(null, skeleton.blockPosition(),
//                        SoundEvents.WITHER_SKELETON_HURT, SoundSource.HOSTILE, 1.0F, 0.8F);
//            });
//        }
//    }

    /**
     * 将凋零骷髅转换为友军 - 使用自定义目标选择器
     */
    private static void makeSkeletonFriendly(WitherSkeleton skeleton, Player player) {
        CompoundTag tag = skeleton.getPersistentData();
        tag.putUUID(NBT_OWNER_UUID, player.getUUID());
        tag.putBoolean(NBT_ENHANCED_SKELETON, true);

        skeleton.setTarget(null);

        skeleton.targetSelector.removeAllGoals();
        skeleton.goalSelector.removeAllGoals();

        skeleton.goalSelector.addGoal(0, new FloatGoal(skeleton));
        skeleton.goalSelector.addGoal(1, new MeleeAttackGoal(skeleton, 1.0D, false));
        skeleton.goalSelector.addGoal(2, new RandomStrollGoal(skeleton, 1.0D));
        skeleton.goalSelector.addGoal(3, new LookAtPlayerGoal(skeleton, Player.class, 8.0F));
        skeleton.goalSelector.addGoal(4, new RandomLookAroundGoal(skeleton));

        skeleton.targetSelector.addGoal(1, new FriendlyWitherSkeletonTargetGoal(skeleton));

        skeleton.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                new AttributeModifier(DAMAGE_MODIFIER_UUID, "Valkyrie Enhancement", 4.0D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL)
        );
        skeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
        skeleton.setHealth(40.0F);
        skeleton.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32.0D);

        skeleton.setGlowingTag(true);

        ItemStack netheriteWord = new ItemStack(Items.NETHERITE_SWORD);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, netheriteWord);
        skeleton.setDropChance(EquipmentSlot.MAINHAND, 0.0F);

        skeleton.level.playSound(null, skeleton.blockPosition(),
                SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.5F);

        updatePlayerMaxHealth(player);
    }

    /**
     * 防止友军凋零骷髅攻击玩家 - 多重保护（只保护穿全套的玩家）
     */
    @SubscribeEvent
    public static void onWitherSkeletonAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof WitherSkeleton skeleton) {
            if (event.getEntity() instanceof Player player) {
                CompoundTag tag = skeleton.getPersistentData();
                if (tag.getBoolean(NBT_ENHANCED_SKELETON)) {
                    UUID ownerUUID = tag.getUUID(NBT_OWNER_UUID);
                    if (player.getUUID().equals(ownerUUID)) {
                        if (UltimateValkyrie.isFullSuit(player)) {
                            event.setCanceled(true);
                            skeleton.setTarget(null);
//                        } else if (!tag.getBoolean("ShouldRestore")) {
//                            tag.putBoolean("ShouldRestore", true);
//                            restoreSkeletonHostile(skeleton);
                        }
                    }
                }
            }
        }
    }

    /**
     * 清理过期的凋零击杀标记
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().tickCount % 20 == 0) {
            LivingEntity entity = event.getEntityLiving();
            CompoundTag tag = entity.getPersistentData();

            if (tag.hasUUID(NBT_WITHER_KILL_MARK)) {
                long markTime = tag.getLong(NBT_WITHER_KILL_MARK + "_Time");
                long currentTime = entity.level.getGameTime();

                if (currentTime - markTime > 200) {
                    tag.remove(NBT_WITHER_KILL_MARK);
                    tag.remove(NBT_WITHER_KILL_MARK + "_Time");
                }
            }
        }
    }

    /**
     * 击杀普通骷髅时生成凋零骷髅
     */
    @SubscribeEvent
    public static void onSkeletonKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton) || event.getEntity() instanceof WitherSkeleton) {
            return;
        }

        Skeleton killedSkeleton = (Skeleton) event.getEntity();
        Player owner = null;

        if (event.getSource().getEntity() instanceof Player player) {
            if (UltimateValkyrie.isFullSuit(player) && player.level instanceof ServerLevel serverLevel) {
                owner = player;
            }
        }

        else if (event.getSource().getEntity() instanceof WitherSkeleton witherSkeleton) {
            CompoundTag skeletonTag = witherSkeleton.getPersistentData();
            if (skeletonTag.getBoolean(NBT_ENHANCED_SKELETON)) {
                UUID ownerUUID = skeletonTag.getUUID(NBT_OWNER_UUID);
                if (killedSkeleton.level instanceof ServerLevel serverLevel) {
                    owner = serverLevel.getPlayerByUUID(ownerUUID);
                }
            }
        }

        else if (event.getSource().isMagic() || event.getSource() == DamageSource.WITHER) {
            CompoundTag tag = killedSkeleton.getPersistentData();
            if (tag.hasUUID(NBT_WITHER_KILL_MARK)) {
                long markTime = tag.getLong(NBT_WITHER_KILL_MARK + "_Time");
                long currentTime = killedSkeleton.level.getGameTime();
                if (currentTime - markTime <= 100) { // 5秒 = 100 ticks
                    UUID ownerUUID = tag.getUUID(NBT_WITHER_KILL_MARK);
                    if (killedSkeleton.level instanceof ServerLevel serverLevel) {
                        owner = serverLevel.getPlayerByUUID(ownerUUID);
                    }
                }
            }
        }

        if (owner != null && killedSkeleton.level instanceof ServerLevel serverLevel) {
            spawnWitherSkeleton(serverLevel, killedSkeleton, owner);
        }
    }

    private static void spawnWitherSkeleton(ServerLevel serverLevel, Skeleton killedSkeleton, Player owner) {
        WitherSkeleton witherSkeleton = EntityType.WITHER_SKELETON.create(serverLevel);
        if (witherSkeleton != null) {
            witherSkeleton.moveTo(
                    killedSkeleton.getX(),
                    killedSkeleton.getY(),
                    killedSkeleton.getZ(),
                    killedSkeleton.getYRot(),
                    0.0F
            );
            makeSkeletonFriendly(witherSkeleton, owner);
            serverLevel.addFreshEntity(witherSkeleton);
            serverLevel.playSound(null, witherSkeleton.blockPosition(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.5F, 1.0F);
            owner.displayClientMessage(
                    Component.nullToEmpty("§5§l凋零骷髅已召唤！"),
                    true
            );
        }
    }

    /**
     * 空手右键发射凋零骷髅头
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();

        if (UltimateValkyrie.isFullSuit(player)) {
            if (player.experienceLevel < 1){
                player.displayClientMessage(Component.nullToEmpty("§c经验不足！"), true);
                return;
            }
            Item item = player.getMainHandItem().getItem();
            if (!player.getCooldowns().isOnCooldown(item)){
                shootWitherSkull(player);
                player.swing(InteractionHand.MAIN_HAND);
                player.getCooldowns().addCooldown(item, 20);
            }
        }
    }

    /**
     * 发射凋零骷髅头
     */
    private static void shootWitherSkull(Player player) {
        Vec3 vec3 = player.getLookAngle();
        WitherSkull skull = new WitherSkull(player.level, player, vec3.x, vec3.y, vec3.z);
        Vec3 lookVec = player.getEyePosition(1.0f);
        skull.setPosRaw(lookVec.x, lookVec.y, lookVec.z);
        skull.setOwner(player);
        skull.setDangerous(true);
        player.level.addFreshEntity(skull);
        player.level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F + player.level.random.nextFloat() * 0.4F);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperienceLevels(-1);
        }

        player.displayClientMessage(Component.nullToEmpty("§5§l凋零之力！"), true);
    }

    /**
     * 修改原有的致命伤害抵御效果
     */
    @SubscribeEvent
    public static void onDeathDefense(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!isFullSuit(player)) {
                return;
            }
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.isEmpty()) {
                return;
            }
            CompoundTag tag = chestplate.getOrCreateTag();
            long currentTime = player.level.getGameTime();
            long cooldownEndTime = tag.getLong(NBT_COOLDOWN_TIME);

            if (player.level instanceof ServerLevel serverLevel) {
                int skeletonCount = getAliveSkeletonCount(player);
                if (skeletonCount > 0) {
                    WitherSkeleton sacrificeSkeleton = serverLevel.getEntitiesOfClass(WitherSkeleton.class,
                                    player.getBoundingBox().inflate(128.0D))
                            .stream()
                            .filter(skeleton -> {
                                CompoundTag skeletonTag = skeleton.getPersistentData();
                                if (!skeletonTag.getBoolean(NBT_ENHANCED_SKELETON)) {
                                    return false;
                                }
                                UUID ownerUUID = skeletonTag.getUUID(NBT_OWNER_UUID);
                                return ownerUUID.equals(player.getUUID()) && skeleton.isAlive();
                            })
                            .findFirst()
                            .orElse(null);

                    if (sacrificeSkeleton != null) {
                        event.setCanceled(true);
                        if (sacrificeSkeleton != null) {
                            event.setCanceled(true);

                            Vec3 skeletonPos = sacrificeSkeleton.position();
                            sacrificeSkeleton.discard();

                            player.setHealth(player.getMaxHealth());
                            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3, false, true));

                            player.level.playSound(null, player.blockPosition(),
                                    SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.8F);
                            player.level.playSound(null, player.blockPosition(),
                                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                            serverLevel.playSound(null, skeletonPos.x, skeletonPos.y, skeletonPos.z,
                                    SoundEvents.WITHER_SKELETON_DEATH, SoundSource.HOSTILE, 1.0F, 0.5F);

                            serverLevel.getServer().execute(() -> {
                                updatePlayerMaxHealth(player);
                                int remainingCount = getAliveSkeletonCount(player);
                                player.displayClientMessage(
                                        Component.nullToEmpty("§5§l凋零骷髅献祭！§6你已复活！\n§c剩余骷髅: §6" + remainingCount),
                                        false
                                );
                            });

                            return;
                        }
                    }
                }

                if (currentTime < cooldownEndTime) {
                    long remainingTicks = cooldownEndTime - currentTime;
                    int remainingSeconds = (int) (remainingTicks / 20);
                    player.displayClientMessage(
                            Component.nullToEmpty("§c能量护甲冷却中: " + remainingSeconds + "秒"),
                            true
                    );
                    return;
                }
                if (!tag.contains(NBT_SHIELD_COUNT)) {
                    tag.putInt(NBT_SHIELD_COUNT, MAX_SHIELD_COUNT);
                }
                int shieldCount = tag.getInt(NBT_SHIELD_COUNT);
                if (shieldCount > 0) {
                    event.setCanceled(true);
                    shieldCount--;
                    tag.putInt(NBT_SHIELD_COUNT, shieldCount);
                    player.setHealth(player.getMaxHealth());
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, true));
                    player.level.playSound(null, player.blockPosition(),
                            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.displayClientMessage(
                            Component.nullToEmpty("§e能量护甲已抵御致命伤害! 剩余次数: §6" + shieldCount + "§e/§6" + MAX_SHIELD_COUNT),
                            true
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }

        Player player = event.player;

        if (player.tickCount % 100 == 0 && isFullSuit(player)) {
            updatePlayerMaxHealth(player);
        }


        if (!isFullSuit(player)) {
            return;
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) {
            return;
        }

        CompoundTag tag = chestplate.getOrCreateTag();
        long currentTime = player.level.getGameTime();
        long cooldownEndTime = tag.getLong(NBT_COOLDOWN_TIME);

        if (currentTime >= cooldownEndTime && cooldownEndTime > 0) {
            tag.putInt(NBT_SHIELD_COUNT, MAX_SHIELD_COUNT);
            tag.putLong(NBT_COOLDOWN_TIME, 0);

            player.displayClientMessage(
                    Component.nullToEmpty("§a能量护甲已充能完毕！ §6" + MAX_SHIELD_COUNT + "§a次防护已就绪"),
                    false
            );

            player.level.playSound(null, player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onKillEnemy(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModArmors.ULTIMATE_VALKYRIE_CHESTPLATE.get())) {
                int currentLevel = 0;
                if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                    currentLevel = player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
                }
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, currentLevel + 1));
            }
        }
    }
}