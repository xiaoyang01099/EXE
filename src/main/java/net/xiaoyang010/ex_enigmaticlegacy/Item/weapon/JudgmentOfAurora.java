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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.entity.EntityFallingStar;
import vazkii.botania.common.entity.EntityPixie;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelSword;

import javax.annotation.Nonnull;

public class JudgmentOfAurora extends ItemManasteelSword {
    // 基础属性
    private static final int INTERVAL = 12;
    private static final String TAG_LAST_TRIGGER = "lastTriggerTime";
    private static final int STAR_DAMAGE = 150;
    private static final float AREA_RADIUS = 5.0f;

    public JudgmentOfAurora() {
        super(Tiers.NETHERITE, 90, -2.4F,
                new Item.Properties()
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
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
//        boolean leftPressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
//        if (stack.getItem() == ModWeapons.JUDGMENT_OF_AURORA.get() && leftPressed && entity instanceof Player player) {
//            summonFallingStarMultiple(stack, world, player);
//            summonStarCircle(stack, world, player);
//        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            // 检查魔力值并消耗
            int manaCost = 1500;
            if (!vazkii.botania.api.mana.ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true)) {
                return InteractionResultHolder.pass(stack);
            }

            // 获取玩家视线方向
            Vec3 lookVec = player.getLookAngle();

            // 在玩家周围生成多个精灵圈
            int circles = 3; // 精灵圈数量
            int pixiesPerCircle = 4; // 每圈精灵数量
            float baseRadius = 2.0f; // 基础半径

            for (int circle = 0; circle < circles; circle++) {
                float radius = baseRadius + (circle * 1.5f);
                float yOffset = circle * 0.5f;

                for (int i = 0; i < pixiesPerCircle; i++) {
                    // 计算圆形分布的位置
                    double angle = (2 * Math.PI * i) / pixiesPerCircle;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    EntityPixie pixie = new EntityPixie(world);
                    pixie.setPos(
                            player.getX() + offsetX,
                            player.getY() + 1 + yOffset,
                            player.getZ() + offsetZ
                    );

                    // 根据圈数设置不同的效果
                    MobEffectInstance effect;
                    float damage;
                    int type;

                    switch (circle) {
                        case 0: // 内圈 - 减速精灵
                            effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
                            damage = 15;
                            type = 0;
                            break;
                        case 1: // 中圈 - 虚弱精灵
                            effect = new MobEffectInstance(MobEffects.WEAKNESS, 80, 1);
                            damage = 20;
                            type = 1;
                            break;
                        default: // 外圈 - 凋零精灵
                            effect = new MobEffectInstance(MobEffects.WITHER, 60, 0);
                            damage = 25;
                            type = 0;
                            break;
                    }

                    // 查找最近的敌对生物作为目标
                    LivingEntity target = world.getNearestEntity(
                            LivingEntity.class,
                            TargetingConditions.forCombat().range(16),
                            player,
                            player.getX(), player.getY(), player.getZ(),
                            player.getBoundingBox().inflate(16, 8, 16)
                    );

                    pixie.setProps(target, player, type, damage);
                    pixie.setApplyPotionEffect(effect);

                    world.addFreshEntity(pixie);

                    // 在精灵周围添加粒子效果
                    SparkleParticleData sparkle = SparkleParticleData.sparkle(
                            1.5F,
                            circle == 0 ? 0.5F : 1F,
                            circle == 1 ? 0.5F : 0.2F,
                            circle == 2 ? 0.8F : 0.4F,
                            20
                    );

                    for (int p = 0; p < 8; p++) {
                        double px = pixie.getX() + (Math.random() - 0.5) * 0.5;
                        double py = pixie.getY() + (Math.random() - 0.5) * 0.5;
                        double pz = pixie.getZ() + (Math.random() - 0.5) * 0.5;
                        world.addParticle(sparkle, px, py, pz, 0, 0.1, 0);
                    }
                }
            }

            // 物品耐久消耗
            int durabilityDamage = ToolCommons.damageItemIfPossible(stack, 1, player, manaCost);
            if (durabilityDamage > 0) {
                stack.hurtAndBreak(durabilityDamage, player, p -> p.broadcastBreakEvent(hand));
            }

            // 播放音效
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                    1.0F, 1.0F + (float) Math.random() * 0.2F);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
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