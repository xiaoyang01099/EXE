package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.entity.EntityFallingStar;
import vazkii.botania.common.entity.EntityPixie;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelSword;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JudgmentOfAurora extends ItemManasteelSword {
    private static final int INTERVAL = 12;
    private static final String TAG_LAST_TRIGGER = "lastTriggerTime";
    private static final int STAR_DAMAGE = 150;
    private static final float AREA_RADIUS = 5.0f;

    public JudgmentOfAurora() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 90, -2.4F,
                new Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level;
        boolean leftPressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
        if (entity instanceof Player player && !level.isClientSide && leftPressed) {
            summonFallingStarMultiple(stack, level, player);
            summonStarCircle(stack, level, player);
        }
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            int manaCost = 1500;
            if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true)) {
                return InteractionResultHolder.pass(stack);
            }

            int circles = 4;
            int pixiesPerCircle = 4;
            float baseRadius = 2.0f;

            List<LivingEntity> availableTargets = findAllValidTargets(world, player, 20);

            for (int circle = 0; circle < circles; circle++) {
                float radius = baseRadius + (circle * 1.5f);
                float yOffset = circle * 0.5f;

                for (int i = 0; i < pixiesPerCircle; i++) {
                    double angle = (2 * Math.PI * i) / pixiesPerCircle;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    EntityPixie pixie = new EntityPixie(world);
                    pixie.setPos(
                            player.getX() + offsetX,
                            player.getY() + 1 + yOffset,
                            player.getZ() + offsetZ
                    );

                    MobEffectInstance effect;
                    float damage;
                    int type;

                    switch (circle) {
                        case 0:
                            effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
                            damage = 15;
                            type = 0;
                            break;
                        case 1:
                            effect = new MobEffectInstance(MobEffects.WEAKNESS, 80, 1);
                            damage = 20;
                            type = 1;
                            break;
                        case 2:
                            effect = new MobEffectInstance(MobEffects.WITHER, 60, 0);
                            damage = 25;
                            type = 0;
                            break;
                        default:
                            effect = new MobEffectInstance(ModEffects.DROWNING.get(), 120, 10);
                            damage = 50;
                            type = 1;
                            break;
                    }

                    LivingEntity target = assignSmartTarget(availableTargets, pixie, circle, i);

                    pixie.setProps(target, player, type, damage);
                    pixie.setApplyPotionEffect(effect);

                    world.addFreshEntity(pixie);

                    float red, green, blue;
                    switch (circle) {
                        case 0:
                            red = 0.5F; green = 0.5F; blue = 1.0F;
                            break;
                        case 1:
                            red = 0.2F; green = 1.0F; blue = 0.2F;
                            break;
                        case 2:
                            red = 0.8F; green = 0.2F; blue = 0.8F;
                            break;
                        default:
                            red = 1.0F; green = 0.1F; blue = 0.1F;
                            break;
                    }

                    SparkleParticleData sparkle = SparkleParticleData.sparkle(1.5F, red, green, blue, 20);

                    for (int p = 0; p < 8; p++) {
                        double px = pixie.getX() + (Math.random() - 0.5) * 0.5;
                        double py = pixie.getY() + (Math.random() - 0.5) * 0.5;
                        double pz = pixie.getZ() + (Math.random() - 0.5) * 0.5;
                        world.addParticle(sparkle, px, py, pz, 0, 0.1, 0);
                    }
                }
            }

            int durabilityDamage = ToolCommons.damageItemIfPossible(stack, 1, player, manaCost);
            if (durabilityDamage > 0) {
                stack.hurtAndBreak(durabilityDamage, player, p -> p.broadcastBreakEvent(hand));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                    1.0F, 1.0F + (float) Math.random() * 0.2F);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * 获取所有有效的攻击目标
     */
    private List<LivingEntity> findAllValidTargets(Level world, Player player, double range) {
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(range, 8, range)
        );

        List<LivingEntity> validTargets = new ArrayList<>();

        for (LivingEntity entity : nearbyEntities) {
            if (isValidPixieTarget(entity, player)) {
                validTargets.add(entity);
            }
        }

        validTargets.sort(Comparator.comparingDouble(player::distanceToSqr));

        return validTargets;
    }

    /**
     * 智能分配目标给精灵
     */
    private LivingEntity assignSmartTarget(List<LivingEntity> availableTargets, EntityPixie pixie, int circle, int pixieIndex) {
        if (availableTargets.isEmpty()) {
            return null;
        }

        switch (circle) {
            case 0:
                return availableTargets.get(0);

            case 1:
                int targetIndex = pixieIndex % availableTargets.size();
                return availableTargets.get(targetIndex);

            case 2:
                return availableTargets.stream()
                        .max((a, b) -> Float.compare(a.getHealth(), b.getHealth()))
                        .orElse(availableTargets.get(0));

            default:
                return availableTargets.get(pixie.level.random.nextInt(availableTargets.size()));
        }
    }

    /**
     * 判断实体是否是精灵的有效攻击目标
     */
    private boolean isValidPixieTarget(LivingEntity entity, Player summoner) {
        // 排除精灵自己
        if (entity instanceof EntityPixie) {
            return false;
        }

        if (summoner != null && entity == summoner) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        return true;
    }

    /**
     * 在玩家周围生成流星圈
     */
    public static void summonStarCircle(ItemStack stack, Level world, Player player) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= AREA_RADIUS && distance >= AREA_RADIUS - 0.5) {
                    Vec3 pos = player.position().add(x, 24, z);
                    Vec3 mot = new Vec3(0, -1.5, 0);

                    EntityFallingStar star = new EntityFallingStar(player, world) {
                        @Override
                        protected void onHitEntity(@Nonnull EntityHitResult hit) {
                            Entity e = hit.getEntity();
                            if (e != getOwner() && e.isAlive()) {
                                e.hurt(DamageSource.playerAttack((Player)getOwner()), STAR_DAMAGE);
                            }
                            discard();
                        }
                    };

                    star.setPos(pos.x, pos.y, pos.z);
                    star.setDeltaMovement(mot);
                    world.addFreshEntity(star);
                }
            }
        }
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    /**
     * 在目标位置生成流星
     */
    public static void summonFallingStarMultiple(ItemStack stack, Level world, Player player) {
        BlockHitResult hitResult = ToolCommons.raytraceFromEntity(player, 48, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Vec3 basePos = Vec3.atLowerCornerOf(hitResult.getBlockPos());

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Vec3 starPos = basePos.add(x, 24, z);
                    Vec3 mot = new Vec3(0, -1.5, 0);

                    EntityFallingStar star = new EntityFallingStar(player, world) {
                        @Override
                        protected void onHitEntity(@Nonnull EntityHitResult hit) {
                            Entity e = hit.getEntity();
                            if (e != getOwner() && e.isAlive()) {
                                e.hurt(DamageSource.playerAttack((Player)getOwner()), STAR_DAMAGE);
                            }
                            discard();
                        }
                    };

                    star.setPos(starPos.x, starPos.y, starPos.z);
                    star.setDeltaMovement(mot);
                    world.addFreshEntity(star);
                }
            }
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }
}