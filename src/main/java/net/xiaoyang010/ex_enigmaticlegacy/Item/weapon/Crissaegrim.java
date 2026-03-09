package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.Effect;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectCut;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectSlash;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EffectMessage;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class Crissaegrim extends SwordItem {
    private static final int INFINITE_DAMAGE = Integer.MAX_VALUE;
    private static final float INFINITE_ATTACK_SPEED = 4.0f;
    public static Random rand = new Random();

    public Crissaegrim() {
        super(EXEAPI.MIRACLE_ITEM_TIER, INFINITE_DAMAGE, INFINITE_ATTACK_SPEED,
                new Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .rarity(ModRarities.MIRACLE));
    }

    private void forceKill(LivingEntity target, Player attacker) {
        boolean wasInvulnerable = target.isInvulnerable();

        target.setInvulnerable(false);
        target.invulnerableTime = 0;

        target.setLastHurtByPlayer(attacker);

        target.setHealth(0);
        target.die(DamageSource.playerAttack(attacker));

        if (target.isAlive()) {
            target.setInvulnerable(wasInvulnerable);
        }
    }

    private void forceDamage(LivingEntity target, Player attacker, float damage) {
        boolean wasInvulnerable = target.isInvulnerable();

        target.setInvulnerable(false);
        target.invulnerableTime = 0;

        target.setLastHurtByPlayer(attacker);

        float newHealth = Math.max(0, target.getHealth() - damage);
        target.setHealth(newHealth);

        target.hurtTime = 10;
        target.hurtDuration = 10;

        if (newHealth <= 0) {
            target.die(DamageSource.playerAttack(attacker));
        }

        if (target.isAlive()) {
            target.setInvulnerable(wasInvulnerable);
        }
    }

    private void spawnSlashEffectOnTarget(Level level, LivingEntity target) {
        Effect slash = new EffectSlash(level.dimension().location().hashCode())
                .setSlashProperties(
                        rand.nextFloat() * 360.0F,
                        rand.nextFloat() * 60.0F - 30.0F,
                        30.0F + rand.nextFloat() * 120.0F,
                        1.5F,
                        1.0F,
                        90.0F
                )
                .setPosition(
                        target.getX(),
                        target.getY() + target.getBbHeight() / 2.0F,
                        target.getZ()
                )
                .setMotion(0, 0, 0)
                .setLife(8)
                .setAdditive(true)
                .setColor(0.35F, 0.35F, 1.0F, 1.0F);
        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_SLASH, slash.write())
        );
    }

    private void spawnSlashEffect(Player player, Level level) {
        Vec3 lookVec = player.getLookAngle();

        float offX = 0.5F * (float) Math.sin(Math.toRadians(-90.0F - player.getYRot()));
        float offZ = 0.5F * (float) Math.cos(Math.toRadians(-90.0F - player.getYRot()));

        double x1 = player.getX() + lookVec.x * 0.5 + offX;
        double y1 = player.getY() + lookVec.y * 0.5 + player.getEyeHeight();
        double z1 = player.getZ() + lookVec.z * 0.5 + offZ;

        double x2 = player.getX() + lookVec.x * 4.0;
        double y2 = player.getY() + player.getEyeHeight() + lookVec.y * 4.0;
        double z2 = player.getZ() + lookVec.z * 4.0;

        Effect slash = new EffectSlash(level.dimension().location().hashCode())
                .setSlashProperties(
                        player.getYRot(),
                        player.getXRot(),
                        30.0F + rand.nextFloat() * 120.0F,
                        2.0F,
                        1.5F,
                        120.0F
                )
                .setPosition(x1, y1, z1)
                .setMotion(
                        (x2 - x1) / 5.0,
                        (y2 - y1) / 5.0,
                        (z2 - z1) / 5.0
                )
                .setLife(5)
                .setAdditive(true)
                .setColor(0.35F, 0.35F, 1.0F, 1.0F);

        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_SLASH, slash.write())
        );
    }

    private void spawnCutEffect(Player player, Level level, LivingEntity target) {
        Effect cut = new EffectCut(level.dimension().location().hashCode())
                .setSlashProperties(
                        player.getYRot(),
                        player.getXRot(),
                        rand.nextFloat() * 360.0F
                )
                .setColor(0.35F, 0.35F, 1.0F, 1.0F)
                .setPosition(
                        target.getX(),
                        target.getY() + target.getBbHeight() / 2.0F,
                        target.getZ()
                )
                .setAdditive(true)
                .setLife(10);

        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_CUT, cut.write())
        );
    }

    private void spawnMoonlightCutEffect(Entity entity, Level level) {
        float offX = 0.75F * (float) Math.sin(Math.toRadians(-30.0F - entity.getYRot()));
        float offZ = 0.75F * (float) Math.cos(Math.toRadians(-30.0F - entity.getYRot()));
        Effect cut = new EffectCut(level.dimension().location().hashCode())
                .setSlashProperties(0.0F, 0.0F, 90.0F)
                .setLife(20)
                .setColor(0.35F, 0.35F, 1.0F, 1.0F)
                .setAdditive(true)
                .setPosition(
                        entity.getX() + (0.5F * (rand.nextFloat() - 0.5F)) + offX,
                        entity.getY() + (0.5F * (rand.nextFloat() - 0.5F)) + (entity.getBbHeight() / 2.0F),
                        entity.getZ() + (0.5F * (rand.nextFloat() - 0.5F)) + offZ
                );
        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_CUT, cut.write())
        );
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!(entity instanceof Player player)) return;

        if (selected && level.getMoonBrightness() >= 1F && !level.isDay()) {
            if (!level.isClientSide) {
                long gameTime = level.getGameTime();

                if (gameTime % 2 == 0) {
                    spawnSlashEffect(player, level);

                    Vec3 lookVec = player.getLookAngle();
                    double lx = player.getX() + lookVec.x * 2.0;
                    double ly = player.getY() + player.getEyeHeight() + lookVec.y * 2.0;
                    double lz = player.getZ() + lookVec.z * 2.0;

                    AABB damageBox = new AABB(
                            lx - 2.0, ly - 2.0, lz - 2.0,
                            lx + 2.0, ly + 2.0, lz + 2.0
                    );

                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, damageBox);

                    for (LivingEntity target : entities) {
                        if (!target.getUUID().equals(player.getUUID())) {
                            if (target.getHealth() > 0.0F) {
                                spawnCutEffect(player, level, target);
                            }

                            forceDamage(target, player, 6.0F);
                        }
                    }
                }
                {
                    spawnMoonlightCutEffect(player, level);
                }
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(entity instanceof LivingEntity target)) {
            return super.onLeftClickEntity(stack, player, entity);
        }

        Level level = player.level;

        if (!level.isClientSide) {
            spawnSlashEffect(player, level);

            if (target.getHealth() > 0.0F) {
                spawnCutEffect(player, level, target);
            }

            forceKill(target, player);
        }

        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();

        if (!world.isClientSide && player != null) {
            AABB area = new AABB(context.getClickedPos()).inflate(100);

            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity target : entities) {
                if (target.getUUID().equals(player.getUUID())) continue;

                spawnSlashEffectOnTarget(world, target);
                spawnCutEffect(player, world, target);

                forceKill(target, player);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
    }
}
