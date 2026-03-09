package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EffectMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.Effect;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectSlash;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.*;

public class Wastelayer extends SwordItem {
    private static final int MAX_MANA = 10000000;
    private static final String MANA_TAG = "mana";
    private static final String BLADE_LOCKED_TAG = "bladeLocked";
    private static final int BASE_MANA_PER_KILL = 500;
    private static final float MANA_PER_HP = 10f;
    private static final float SHARPNESS_STEAL_BASE = 0.05f;
    private static final float MAX_STEAL_PERCENT = 0.5f;
    private static final int OVERFLOW_RADIUS = 20;
    private static final float OVERFLOW_DAMAGE = 300f;
    private static final int BLADE_MANA_COST = 10000;
    private static final float BLADE_UNLOCK_THRESHOLD = 0.4f;
    private static final float BLADE_TRUE_DAMAGE = 50f;
    private static final int BLADE_ROOT_DURATION = 60;
    private static final int BLADE_COOLDOWN = 20;

    public Wastelayer() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 10, -2.4F,
                new Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .rarity(ModRarities.MIRACLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (canUseManaBlade(stack)) {
                if (player.getCooldowns().isOnCooldown(this)) {
                    player.displayClientMessage(
                            new TranslatableComponent("message.wastelayer.cooldown")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return InteractionResultHolder.fail(stack);
                }

                double attackRange = calculateAttackRange(stack);
                fireManaBlade(level, player, stack, attackRange);

                player.getCooldowns().addCooldown(this, BLADE_COOLDOWN);
                return InteractionResultHolder.success(stack);
            } else if (ItemNBTHelper.getBoolean(stack, BLADE_LOCKED_TAG, false)) {
                player.displayClientMessage(
                        new TranslatableComponent("message.wastelayer.blade_locked")
                                .withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            } else {
                player.displayClientMessage(
                        new TranslatableComponent("message.wastelayer.insufficient_mana")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        return super.use(level, player, hand);
    }

    private boolean canUseManaBlade(ItemStack stack) {
        if (ItemNBTHelper.getBoolean(stack, BLADE_LOCKED_TAG, false)) {
            return false;
        }

        int currentMana = getMana(stack);
        return currentMana >= BLADE_MANA_COST;
    }

    private double calculateAttackRange(ItemStack stack) {
        int currentMana = getMana(stack);
        float manaPercent = (float) currentMana / MAX_MANA;

        return 5.0 + (manaPercent * 15.0);
    }

    private void fireManaBlade(Level level, Player player, ItemStack stack, double range) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int currentMana = getMana(stack);
        int newMana = currentMana - BLADE_MANA_COST;
        setMana(stack, newMana);

        if (newMana < MAX_MANA * BLADE_UNLOCK_THRESHOLD) {
            ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, true);
            player.displayClientMessage(
                    new TranslatableComponent("message.wastelayer.blade_locked_warning")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    true
            );
        }

        Vec3 lookVec = player.getLookAngle();
        Vec3 startPos = player.getEyePosition(1.0f);

        float offX = 0.5F * (float)Math.sin(Math.toRadians(-90.0F - player.getYRot()));
        float offZ = 0.5F * (float)Math.cos(Math.toRadians(-90.0F - player.getYRot()));

        double x1 = player.getX() + lookVec.x * 0.5 + offX;
        double y1 = player.getY() + lookVec.y * 0.5 + player.getEyeHeight();
        double z1 = player.getZ() + lookVec.z * 0.5 + offZ;

        double x2 = player.getX() + lookVec.x * range;
        double y2 = player.getY() + player.getEyeHeight() + lookVec.y * range;
        double z2 = player.getZ() + lookVec.z * range;

        Random rand = new Random();
        Effect slash = new EffectSlash(serverLevel.dimension().location().hashCode())
                .setSlashProperties(
                        player.getYRot(),
                        player.getXRot(),
                        30.0F + rand.nextFloat() * 120.0F,
                        2.5F,
                        2.0F,
                        150.0F
                )
                .setPosition(x1, y1, z1)
                .setMotion(
                        (x2 - x1) / 8.0,
                        (y2 - y1) / 8.0,
                        (z2 - z1) / 8.0
                )
                .setLife(8)
                .setAdditive(true)
                .setColor(0.2F, 0.0F, 0.4F, 1.0F);

        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_SLASH, slash.write())
        );

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.WASTELAYER, SoundSource.PLAYERS, 3.0F, 1.0F);

        spawnBladeParticles(serverLevel, startPos, lookVec, range);

        performBladeDamage(serverLevel, player, x1, y1, z1, lookVec, range);
    }

    private void spawnBladeParticles(ServerLevel level, Vec3 start, Vec3 direction, double range) {
        int particleCount = (int)(range * 5);

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 pos = start.add(direction.scale(progress * range));

            WispParticleData data = WispParticleData.wisp(
                    0.6F + level.random.nextFloat() * 0.4F,
                    0.2f, 0.0f, 0.4f
            );

            level.sendParticles(data,
                    pos.x, pos.y, pos.z,
                    3,
                    0.2, 0.2, 0.2,
                    0.08);
        }
    }

    private void performBladeDamage(ServerLevel level, Player player, double x, double y, double z,
                                    Vec3 lookVec, double range) {
        double lx = x + lookVec.x * (range / 2.0);
        double ly = y + lookVec.y * (range / 2.0);
        double lz = z + lookVec.z * (range / 2.0);

        AABB damageBox = new AABB(
                lx - range / 2.0, ly - 2.0, lz - range / 2.0,
                lx + range / 2.0, ly + 2.0, lz + range / 2.0
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                damageBox
        );

        for (LivingEntity entity : entities) {
            if (!entity.getUUID().equals(player.getUUID())) {
                entity.invulnerableTime = 0;
                entity.setLastHurtByPlayer(player);

                DamageSource trueDamage = new TrueDamageSource(player);
                entity.hurt(trueDamage, BLADE_TRUE_DAMAGE);

                entity.addEffect(new MobEffectInstance(
                        ModEffects.ROOTED.get(),
                        BLADE_ROOT_DURATION,
                        0,
                        false,
                        true,
                        true
                ));

                if (entity.getHealth() > 0.0F) {
                    Random rand = new Random();
                    Effect cut = new net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectCut(
                            level.dimension().location().hashCode())
                            .setSlashProperties(
                                    player.getYRot(),
                                    player.getXRot(),
                                    rand.nextFloat() * 360.0F
                            )
                            .setColor(0.2F, 0.0F, 0.4F, 1.0F)
                            .setPosition(
                                    entity.getX(),
                                    entity.getY() + entity.getBbHeight() / 2.0F,
                                    entity.getZ()
                            )
                            .setAdditive(true)
                            .setLife(15);

                    NetworkHandler.CHANNEL.send(
                            PacketDistributor.ALL.noArg(),
                            new EffectMessage(FXHandler.FX_CUT, cut.write())
                    );
                }

                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level.isClientSide) return true;

        double attackRange = calculateAttackRange(stack);
        double distance = attacker.distanceTo(target);

        if (distance <= attackRange && distance > 3.0) {
            float bonusDamage = (float) ((distance - 3.0) / attackRange * 10.0);
            target.hurt(DamageSource.playerAttack((Player) attacker), bonusDamage);
        }

        if (target instanceof Player targetPlayer) {
            handlePlayerAttack(stack, targetPlayer, attacker);
        }

        if (target.getHealth() <= 0 || !target.isAlive()) {
            onEntityKilled(stack, target, attacker);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    private void handlePlayerAttack(ItemStack stack, Player target, LivingEntity attacker) {
        int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);

        if (sharpnessLevel > 0) {
            float stealPercent = Math.min(sharpnessLevel * SHARPNESS_STEAL_BASE, MAX_STEAL_PERCENT);
            int stolenMana = stealManaFromPlayer(target, stealPercent);

            if (stolenMana > 0) {
                int currentMana = getMana(stack);
                int newMana = currentMana + stolenMana;
                int overflow = 0;

                if (newMana > MAX_MANA) {
                    overflow = newMana - MAX_MANA;
                    newMana = MAX_MANA;
                } else if (newMana >= MAX_MANA) {
                    ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, false);
                    if (attacker instanceof Player player) {
                        player.displayClientMessage(
                                new TranslatableComponent("message.wastelayer.blade_unlocked")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                                true
                        );
                    }
                }

                if (overflow > 0 && attacker instanceof Player attackerPlayer) {
                    overflow = distributeManaToPlayer(attackerPlayer, overflow);
                }

                setMana(stack, newMana);

                if (overflow > 0) {
                    createManaOverflowExplosion(attacker);
                    setMana(stack, newMana);
                }

                target.level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        } else {
            int stolenMana = stealFixedManaFromPlayer(target, 1000);
            if (stolenMana > 0) {
                int currentMana = getMana(stack);
                int newMana = Math.min(MAX_MANA, currentMana + stolenMana);

                if (newMana >= MAX_MANA) {
                    ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, false);
                    if (attacker instanceof Player player) {
                        player.displayClientMessage(
                                new TranslatableComponent("message.wastelayer.blade_unlocked")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                                true
                        );
                    }
                }

                setMana(stack, newMana);
            }
        }
    }

    public void onEntityKilled(ItemStack stack, LivingEntity killed, LivingEntity killer) {
        if (killer.level.isClientSide) return;

        int manaGain = calculateManaGain(killed, killer);
        int currentMana = getMana(stack);
        int newMana = currentMana + manaGain;

        if (newMana > MAX_MANA) {
            int overflow = newMana - MAX_MANA;

            if (killer instanceof Player player) {
                overflow = distributeManaToPlayer(player, overflow);
            }

            setMana(stack, MAX_MANA);
            ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, false);

            if (killer instanceof Player player) {
                player.displayClientMessage(
                        new TranslatableComponent("message.wastelayer.blade_unlocked")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                        true
                );
            }

            if (overflow > 0) {
                createManaOverflowExplosion(killer);
                setMana(stack, MAX_MANA);
            }
        } else {
            setMana(stack, newMana);

            if (newMana >= MAX_MANA) {
                ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, false);
                if (killer instanceof Player player) {
                    player.displayClientMessage(
                            new TranslatableComponent("message.wastelayer.blade_unlocked")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                            true
                    );
                }
            }
        }

        killer.level.playSound(null, killer.getX(), killer.getY(), killer.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private static class TrueDamageSource extends DamageSource {
        public TrueDamageSource(Player player) {
            super("wastelayer_true_damage");
            this.bypassArmor();
            this.bypassMagic();
            this.bypassInvul();
            this.setMagic();
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }
    }

    private int calculateManaGain(LivingEntity killed, LivingEntity killer) {
        float maxHealth = killed.getMaxHealth();
        float difficulty = maxHealth / 20f;

        if (killed.isOnFire()) difficulty *= 1.2f;
        if (killed.hasEffect(MobEffects.POISON)) difficulty *= 1.1f;
        if (killed.hasEffect(MobEffects.WITHER)) difficulty *= 1.3f;

        if (killer.distanceToSqr(killed) > 100) difficulty *= 1.15f;

        int manaGain = (int) (BASE_MANA_PER_KILL + (maxHealth * MANA_PER_HP * difficulty));

        return Math.max(manaGain, BASE_MANA_PER_KILL);
    }

    private int stealManaFromPlayer(Player target, float percent) {
        int totalStolen = 0;

        totalStolen += stealFromItem(target.getMainHandItem(), percent);
        totalStolen += stealFromItem(target.getOffhandItem(), percent);

        for (ItemStack armor : target.getArmorSlots()) {
            totalStolen += stealFromItem(armor, percent);
        }

        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack item = target.getInventory().getItem(i);
            totalStolen += stealFromItem(item, percent);
        }

        LazyOptional<ICuriosItemHandler> curiosOptional = CuriosApi.getCuriosHelper().getCuriosHandler(target);
        final int[] stolen = {totalStolen};
        curiosOptional.ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (ICurioStacksHandler stacksHandler : curios.values()) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    stolen[0] += stealFromItem(stack, percent);
                }
            }
        });

        return stolen[0];
    }

    private int stealFixedManaFromPlayer(Player target, int amount) {
        int totalStolen = 0;
        int remaining = amount;

        int stolen = stealFixedFromItem(target.getMainHandItem(), remaining);
        totalStolen += stolen;
        remaining -= stolen;
        if (remaining <= 0) return totalStolen;

        stolen = stealFixedFromItem(target.getOffhandItem(), remaining);
        totalStolen += stolen;
        remaining -= stolen;
        if (remaining <= 0) return totalStolen;

        for (ItemStack armor : target.getArmorSlots()) {
            if (remaining <= 0) break;
            stolen = stealFixedFromItem(armor, remaining);
            totalStolen += stolen;
            remaining -= stolen;
        }
        if (remaining <= 0) return totalStolen;

        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) break;
            ItemStack item = target.getInventory().getItem(i);
            stolen = stealFixedFromItem(item, remaining);
            totalStolen += stolen;
            remaining -= stolen;
        }
        if (remaining <= 0) return totalStolen;

        LazyOptional<ICuriosItemHandler> curiosOptional = CuriosApi.getCuriosHelper().getCuriosHandler(target);
        final int[] finalStolen = {totalStolen};
        final int[] finalRemaining = {remaining};

        curiosOptional.ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (ICurioStacksHandler stacksHandler : curios.values()) {
                if (finalRemaining[0] <= 0) return;

                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (finalRemaining[0] <= 0) return;

                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    int s = stealFixedFromItem(stack, finalRemaining[0]);
                    finalStolen[0] += s;
                    finalRemaining[0] -= s;
                }
            }
        });

        return finalStolen[0];
    }

    private int stealFromItem(ItemStack stack, float percent) {
        if (stack.isEmpty()) return 0;

        LazyOptional<IManaItem> manaItemCap = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        final int[] stolen = {0};

        manaItemCap.ifPresent(manaItem -> {
            int currentMana = manaItem.getMana();
            int toSteal = (int) (currentMana * percent);

            if (toSteal > 0) {
                manaItem.addMana(-toSteal);
                stolen[0] = toSteal;
            }
        });

        return stolen[0];
    }

    private int stealFixedFromItem(ItemStack stack, int amount) {
        if (stack.isEmpty()) return 0;

        LazyOptional<IManaItem> manaItemCap = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        final int[] stolen = {0};

        manaItemCap.ifPresent(manaItem -> {
            int currentMana = manaItem.getMana();
            int toSteal = Math.min(currentMana, amount);

            if (toSteal > 0) {
                manaItem.addMana(-toSteal);
                stolen[0] = toSteal;
            }
        });

        return stolen[0];
    }

    private int distributeManaToPlayer(Player player, int mana) {
        int remaining = mana;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() != this) {
            remaining -= distributeManaToItem(mainHand, remaining);
            if (remaining <= 0) return 0;
        }

        remaining -= distributeManaToItem(player.getOffhandItem(), remaining);
        if (remaining <= 0) return 0;

        for (ItemStack armor : player.getArmorSlots()) {
            if (remaining <= 0) break;
            remaining -= distributeManaToItem(armor, remaining);
        }
        if (remaining <= 0) return 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) break;
            ItemStack item = player.getInventory().getItem(i);
            if (!item.isEmpty() && item.getItem() != this) {
                remaining -= distributeManaToItem(item, remaining);
            }
        }
        if (remaining <= 0) return 0;

        LazyOptional<ICuriosItemHandler> curiosOptional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        final int[] finalRemaining = {remaining};

        curiosOptional.ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (ICurioStacksHandler stacksHandler : curios.values()) {
                if (finalRemaining[0] <= 0) return;

                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (finalRemaining[0] <= 0) return;

                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        int distributed = distributeManaToItem(stack, finalRemaining[0]);
                        finalRemaining[0] -= distributed;
                    }
                }
            }
        });

        return finalRemaining[0];
    }

    private int distributeManaToItem(ItemStack stack, int mana) {
        if (stack.isEmpty() || mana <= 0) return 0;

        LazyOptional<IManaItem> manaItemCap = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        final int[] distributed = {0};

        manaItemCap.ifPresent(manaItem -> {
            int maxMana = manaItem.getMaxMana();
            int currentMana = manaItem.getMana();
            int canAdd = maxMana - currentMana;

            if (canAdd > 0) {
                int toAdd = Math.min(canAdd, mana);
                manaItem.addMana(toAdd);
                distributed[0] = toAdd;
            }
        });

        return distributed[0];
    }

    private void createManaOverflowExplosion(LivingEntity entity) {
        if (!(entity.level instanceof ServerLevel level)) return;

        ItemStack heldWeapon = null;
        int savedMana = 0;
        if (entity instanceof Player player) {
            heldWeapon = player.getMainHandItem();
            if (heldWeapon.getItem() == this) {
                savedMana = getMana(heldWeapon);
            }
        }

        Vec3 pos = entity.position();
        AABB area = new AABB(pos.x - OVERFLOW_RADIUS, pos.y - OVERFLOW_RADIUS, pos.z - OVERFLOW_RADIUS,
                pos.x + OVERFLOW_RADIUS, pos.y + OVERFLOW_RADIUS, pos.z + OVERFLOW_RADIUS);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : entities) {
            if (target == entity) continue;

            double distance = target.distanceToSqr(pos);
            if (distance > OVERFLOW_RADIUS * OVERFLOW_RADIUS) continue;

            if (target instanceof Player player) {
                removeTotemEffects(player);
            }

            DamageSource damageSource = new ManaOverflowDamageSource();
            target.hurt(damageSource, OVERFLOW_DAMAGE);

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));

            if (target instanceof Player player) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();

                player.getPersistentData().putLong("manaOverflowTeleportBlock", level.getGameTime() + 100);
            }
        }

        spawnManaOverflowParticles(level, pos);

        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 0.8F);

        if (heldWeapon != null && savedMana > 0) {
            setMana(heldWeapon, savedMana);
        }
    }

    private void removeTotemEffects(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.getItem().toString().contains("totem")) {
            mainHand.shrink(1);
        }
        if (offHand.getItem().toString().contains("totem")) {
            offHand.shrink(1);
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem().toString().contains("totem")) {
                stack.shrink(1);
            }
        }
    }

    private void spawnManaOverflowParticles(ServerLevel level, Vec3 pos) {
        spawnMultiStageParticleAnimation(level, pos);
    }

    private void spawnMultiStageParticleAnimation(ServerLevel level, Vec3 pos) {
        int totalDuration = 60;

        for (int tick = 0; tick < totalDuration; tick++) {
            final int currentTick = tick;

            level.getServer().tell(new net.minecraft.server.TickTask(tick, () -> {
                double progress = (double) currentTick / totalDuration;

                if (progress < 0.3) {
                    spawnConvergingFrame(level, pos, progress / 0.3);
                } else if (progress < 0.8) {
                    double spiralProgress = (progress - 0.3) / 0.5;
                    spawnSpiralRisingFrame(level, pos, spiralProgress);
                } else {
                    double burstProgress = (progress - 0.8) / 0.2;
                    spawnTopBurstFrame(level, pos, burstProgress);
                }
            }));
        }
    }

    private void spawnConvergingFrame(ServerLevel level, Vec3 pos, double progress) {
        int particleCount = 30;
        double startRadius = OVERFLOW_RADIUS * 1.5;

        for (int i = 0; i < particleCount; i++) {
            double theta = Math.PI * 2 * i / particleCount;
            double phi = Math.acos(2 * level.random.nextDouble() - 1);

            double currentRadius = startRadius * (1.0 - progress);

            double x = currentRadius * Math.sin(phi) * Math.cos(theta);
            double y = currentRadius * Math.sin(phi) * Math.sin(theta);
            double z = currentRadius * Math.cos(phi);

            Vector3 particlePos = new Vector3(
                    pos.x + x,
                    pos.y + y + 1.0,
                    pos.z + z
            );

            double velocityX = -x * 0.08;
            double velocityY = -y * 0.08;
            double velocityZ = -z * 0.08;

            float r = 0.3f + (float)progress * 0.4f;
            float g = 0.1f;
            float b = 0.8f - (float)progress * 0.2f;

            WispParticleData data = WispParticleData.wisp(
                    0.4F + (float)progress * 0.3F, r, g, b
            );

            level.sendParticles(data,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, velocityX, velocityY, velocityZ, 0.05);
        }
    }

    private void spawnSpiralRisingFrame(ServerLevel level, Vec3 pos, double progress) {
        int spiralCount = 3;
        int particlesPerSpiral = 8;
        double maxHeight = OVERFLOW_RADIUS * 2.5;
        double currentHeight = progress * maxHeight;

        for (int spiral = 0; spiral < spiralCount; spiral++) {
            double spiralOffset = (Math.PI * 2 * spiral) / spiralCount;

            for (int i = 0; i < particlesPerSpiral; i++) {
                double layerOffset = (double) i / particlesPerSpiral;
                double particleHeight = currentHeight - layerOffset * 2.0;

                if (particleHeight < 0) continue;

                double heightRatio = particleHeight / maxHeight;

                double radius;
                if (heightRatio < 0.2) {
                    radius = heightRatio / 0.2 * OVERFLOW_RADIUS * 0.4;
                } else if (heightRatio < 0.6) {
                    radius = OVERFLOW_RADIUS * (0.4 + (heightRatio - 0.2) / 0.4 * 0.5);
                } else {
                    radius = OVERFLOW_RADIUS * (0.9 - (heightRatio - 0.6) / 0.4 * 0.7);
                }

                double rotations = 8;
                double angle = spiralOffset + heightRatio * Math.PI * 2 * rotations;

                double wobble = Math.sin(heightRatio * Math.PI * 4) * 0.3;
                double wobbleAngle = angle + wobble;

                double x = Math.cos(wobbleAngle) * radius;
                double z = Math.sin(wobbleAngle) * radius;
                double y = particleHeight;

                Vector3 particlePos = new Vector3(
                        pos.x + x,
                        pos.y + y + 1.0,
                        pos.z + z
                );

                double nextAngle = wobbleAngle + 0.1;
                double velocityX = (Math.cos(nextAngle) * radius - x) * 0.5;
                double velocityZ = (Math.sin(nextAngle) * radius - z) * 0.5;
                double velocityY = 0.15;

                float colorProgress = (float)heightRatio;
                float r = 0.6f + colorProgress * 0.3f;
                float g = 0.1f + colorProgress * 0.4f;
                float b = 0.8f - colorProgress * 0.3f;

                float size = 0.6F - colorProgress * 0.3F;

                WispParticleData data = WispParticleData.wisp(size, r, g, b);

                level.sendParticles(data,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, velocityX, velocityY, velocityZ, 0.08);
            }
        }

        spawnCenterPillarParticles(level, pos, progress, maxHeight);
    }

    private void spawnCenterPillarParticles(ServerLevel level, Vec3 pos, double progress, double maxHeight) {
        int pillarParticles = 5;
        double currentHeight = progress * maxHeight;

        for (int i = 0; i < pillarParticles; i++) {
            double heightOffset = level.random.nextDouble() * currentHeight;

            Vector3 particlePos = new Vector3(
                    pos.x + (level.random.nextDouble() - 0.5) * 0.3,
                    pos.y + heightOffset + 1.0,
                    pos.z + (level.random.nextDouble() - 0.5) * 0.3
            );

            WispParticleData data = WispParticleData.wisp(
                    0.3F, 0.9f, 0.7f, 1.0f
            );

            level.sendParticles(data,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0.1, 0, 0.02);
        }
    }

    private void spawnTopBurstFrame(ServerLevel level, Vec3 pos, double progress) {
        int burstParticles = 20;
        double maxHeight = OVERFLOW_RADIUS * 2.5;
        double explosionRadius = progress * OVERFLOW_RADIUS * 1.5;

        for (int i = 0; i < burstParticles; i++) {
            double theta = Math.PI * 2 * level.random.nextDouble();
            double phi = Math.acos(2 * level.random.nextDouble() - 1);

            double x = explosionRadius * Math.sin(phi) * Math.cos(theta);
            double y = explosionRadius * Math.sin(phi) * Math.sin(theta);
            double z = explosionRadius * Math.cos(phi);

            Vector3 particlePos = new Vector3(
                    pos.x + x * 0.3,
                    pos.y + maxHeight + 1.0,
                    pos.z + z * 0.3
            );

            double velocityX = x * 0.1;
            double velocityY = y * 0.1;
            double velocityZ = z * 0.1;

            float r = 1.0f;
            float g = 0.5f + (float)progress * 0.3f;
            float b = 0.8f;

            WispParticleData data = WispParticleData.wisp(
                    0.5F - (float)progress * 0.3F, r, g, b
            );

            level.sendParticles(data,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, velocityX, velocityY, velocityZ, 0.15);
        }

        if (progress > 0.5) {
            spawnParticleRain(level, pos, maxHeight, progress);
        }
    }

    private void spawnParticleRain(ServerLevel level, Vec3 pos, double height, double progress) {
        int rainParticles = 15;
        double spreadRadius = OVERFLOW_RADIUS;

        for (int i = 0; i < rainParticles; i++) {
            double angle = Math.PI * 2 * level.random.nextDouble();
            double radius = level.random.nextDouble() * spreadRadius;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Vector3 particlePos = new Vector3(
                    pos.x + x,
                    pos.y + height + 1.0,
                    pos.z + z
            );

            WispParticleData data = WispParticleData.wisp(
                    0.3F, 0.7f, 0.3f, 0.9f
            );

            level.sendParticles(data,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, -0.2, 0, 0.05);
        }
    }

    private int getMana(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, MANA_TAG, 0);
    }

    private void setMana(ItemStack stack, int mana) {
        ItemNBTHelper.setInt(stack, MANA_TAG, Math.max(0, Math.min(MAX_MANA, mana)));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new WastelayerManaItem(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int mana = getMana(stack);
        float manaPercent = (float) mana / MAX_MANA;
        NumberFormat format = NumberFormat.getInstance(Locale.US);

        tooltip.add(new TranslatableComponent("tooltip.wastelayer.mana",
                format.format(mana), format.format(MAX_MANA))
                .withStyle(ChatFormatting.AQUA));

        String percentColor = manaPercent >= 1.0f ? ChatFormatting.LIGHT_PURPLE.toString() :
                manaPercent >= 0.4f ? ChatFormatting.AQUA.toString() :
                        ChatFormatting.RED.toString();
        tooltip.add(new TextComponent(percentColor + String.format("%.1f%%", manaPercent * 100)));

        tooltip.add(new TextComponent(""));

        double attackRange = calculateAttackRange(stack);
        tooltip.add(new TranslatableComponent("tooltip.wastelayer.attack_range",
                String.format("%.1f", attackRange))
                .withStyle(ChatFormatting.GOLD));

        tooltip.add(new TextComponent(""));
        tooltip.add(new TranslatableComponent("tooltip.wastelayer.ability1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("tooltip.wastelayer.ability2")
                .withStyle(ChatFormatting.GRAY));

        boolean bladeLocked = ItemNBTHelper.getBoolean(stack, BLADE_LOCKED_TAG, false);
        if (bladeLocked) {
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_locked")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_unlock_hint")
                    .withStyle(ChatFormatting.RED));
        } else if (mana >= BLADE_MANA_COST) {
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_ready")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_cost",
                    format.format(BLADE_MANA_COST))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_damage",
                    String.format("%.0f", BLADE_TRUE_DAMAGE))
                    .withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.blade_insufficient")
                    .withStyle(ChatFormatting.GRAY));
        }

        int sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if (sharpness > 0) {
            float percent = Math.min(sharpness * SHARPNESS_STEAL_BASE * 100, MAX_STEAL_PERCENT * 100);
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.sharpness",
                    String.format("%.0f%%", percent))
                    .withStyle(ChatFormatting.GOLD));
        }

        if (mana > MAX_MANA * 0.9) {
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("tooltip.wastelayer.overflow")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getMana(stack) / (float) MAX_MANA);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = getMana(stack) / (float) MAX_MANA;
        boolean bladeLocked = ItemNBTHelper.getBoolean(stack, BLADE_LOCKED_TAG, false);

        if (bladeLocked) {
            int r = (int) (100 * fraction);
            int g = 0;
            int b = 0;
            return (r << 16) | (g << 8) | b;
        } else if (fraction >= 1.0f) {
            return 0x8B00FF;
        } else {
            int r = (int) (75 * fraction);
            int g = (int) (8 * fraction);
            int b = (int) (130 + 100 * fraction);
            return (r << 16) | (g << 8) | b;
        }
    }

    private static class ManaOverflowDamageSource extends DamageSource {
        public ManaOverflowDamageSource() {
            super("manaOverflow");
            this.bypassArmor();
            this.bypassMagic();
            this.bypassInvul();
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }
    }

    private class WastelayerManaItem implements IManaItem, ICapabilityProvider {
        private final ItemStack stack;
        private final LazyOptional<IManaItem> holder = LazyOptional.of(() -> this);

        public WastelayerManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return ItemNBTHelper.getInt(stack, MANA_TAG, 0);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            int current = getMana();
            int newMana = Math.max(0, Math.min(MAX_MANA, current + mana));
            ItemNBTHelper.setInt(stack, MANA_TAG, newMana);

            if (newMana >= MAX_MANA) {
                ItemNBTHelper.setBoolean(stack, BLADE_LOCKED_TAG, false);
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

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                return holder.cast();
            }
            return LazyOptional.empty();
        }
    }
}