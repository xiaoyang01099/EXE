package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy")
public class GodSlayerSword extends SwordItem {

    private long rightClickStartTime = 0;

    public GodSlayerSword(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (selected && entity instanceof Player) {
            Vec3 pos = entity.position();
            double x = pos.x + (level.random.nextDouble() - 0.5D) * 0.5D;
            double y = pos.y + level.random.nextDouble() * 2.0D;
            double z = pos.z + (level.random.nextDouble() - 0.5D) * 0.5D;

            level.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 0, 0.1D, 0);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level;
        BlockPos pos = target.blockPosition();

        if (target instanceof Player) {
            Player player = (Player) target;
            createKillEffect(level, target);
            clearPlayerInventory(player);
        }
        absoluteKillEntity(target);
        clearArea(target.level, target.blockPosition(), 10);
        return super.hurtEnemy(stack, target, attacker);
    }

    private void createKillEffect(Level level, LivingEntity target) {
        BlockPos pos = target.blockPosition();
        Vec3 targetPos = target.position();

        for(int i = 0; i < 50; i++) {
            double x = targetPos.x + (level.random.nextDouble() - 0.5D) * 3.0D;
            double y = targetPos.y + level.random.nextDouble() * 3.0D;
            double z = targetPos.z + (level.random.nextDouble() - 0.5D) * 3.0D;

            level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0, 0, 0);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.1D, 0);
        }

        for(int y = 0; y < 20; y++) {
            for(int i = 0; i < 10; i++) {
                double angle = i * Math.PI * 2 / 10;
                double radius = 0.5D;
                double x = targetPos.x + Math.cos(angle) * radius;
                double z = targetPos.z + Math.sin(angle) * radius;

                level.addParticle(ParticleTypes.END_ROD, x, targetPos.y + y, z, 0, 0, 0);
            }
        }

        level.playSound(null, pos, SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!level.isClientSide) {
            BlockState prevState = level.getBlockState(pos.above());
            level.setBlockAndUpdate(pos.above(), Blocks.LIGHT.defaultBlockState());
            level.scheduleTick(pos.above(), Blocks.LIGHT, 20);
        }
    }

    private void clearPlayerInventory(Player player) {
        try {
            player.getInventory().clearContent();

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }

            removeDisarmProtection(player);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(new TextComponent("Your equipment has been forcibly removed!"), false);
            }
        } catch (Exception e) {
            System.err.println("Error clearing player inventory: " + e.getMessage());
        }
    }

    private void removeDisarmProtection(LivingEntity entity) {
        try {
            for (var attribute : entity.getAttributes().getSyncableAttributes()) {
                var instance = entity.getAttribute(attribute.getAttribute());

                if (instance != null) {
                    for (var modifier : instance.getModifiers()) {
                        instance.removeModifier(modifier);
                    }
                }
            }
            for (var effect : entity.getActiveEffects()) {
                if (effect.getEffect().getDescriptionId().toLowerCase().contains("protect")
                        || effect.getEffect().getDescriptionId().toLowerCase().contains("disarm")) {
                    entity.removeEffect(effect.getEffect());
                }
            }
        } catch (Exception e) {
            System.err.println("Error removing disarm protection: " + e.getMessage());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (player.isUsingItem()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - rightClickStartTime > 5000) {
                    reverseTime(serverLevel, 24000);
                    player.displayClientMessage(new TextComponent("Reversing time!"), true);
                } else if (currentTime - rightClickStartTime > 2000) {
                    accelerateTime(serverLevel, 2000);
                    player.displayClientMessage(new TextComponent("Accelerating time!"), true);
                }
            } else {
                freezeTime(serverLevel);
                player.displayClientMessage(new TextComponent("Freezing time!"), true);
            }

            rightClickStartTime = System.currentTimeMillis();
        }

        return super.use(level, player, hand);
    }

    private void absoluteKillEntity(LivingEntity target) {
        try {
            target.setInvulnerable(false);
            target.invulnerableTime = 0;
            removeEntityProtection(target);

            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.setGameMode(GameType.SURVIVAL);
                serverPlayer.getInventory().clearContent();
            }

            target.setHealth(0);
            target.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            target.kill();

            if (!target.isRemoved()) {
                target.remove(Entity.RemovalReason.KILLED);
            }
        } catch (Exception e) {
            System.err.println("Error during absolute kill: " + e.getMessage());
        }
    }

    private void clearArea(Level world, BlockPos center, int radius) {
        AABB area = new AABB(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius));
        List<Entity> entities = world.getEntities(null, area);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                absoluteKillEntity(livingEntity);
            } else if (!entity.isRemoved()) {
                entity.remove(Entity.RemovalReason.KILLED);
            }
        }

        for(int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;

            world.addParticle(ParticleTypes.SWEEP_ATTACK, x, center.getY(), z, 0, 0.1D, 0);
            world.addParticle(ParticleTypes.DRAGON_BREATH, x, center.getY(), z, 0, 0.1D, 0);
        }
    }

    private void removeEntityProtection(LivingEntity target) {
        try {
            for (Field field : target.getClass().getDeclaredFields()) {
                if (field.getName().toLowerCase().contains("protect") || field.getName().toLowerCase().contains("invuln")) {
                    field.setAccessible(true);
                    field.setBoolean(target, false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void freezeTime(ServerLevel level) {
        level.setDayTime(level.getDayTime());
        level.getServer().getPlayerList().broadcastMessage(new TextComponent("Time is frozen!"), ChatType.SYSTEM, Util.NIL_UUID);
        for(ServerPlayer player : level.players()) {
            // 添加时间冻结特效
            Vec3 pos = player.position();
            for(int i = 0; i < 20; i++) {
                double x = pos.x + (level.random.nextDouble() - 0.5D) * 10.0D;
                double y = pos.y + level.random.nextDouble() * 5.0D;
                double z = pos.z + (level.random.nextDouble() - 0.5D) * 10.0D;

                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 1, 0, 0, 0, 0.1D);
            }
            
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, true, false));
        }
    }

    public static void accelerateTime(ServerLevel level, int ticksPerSecond) {
        level.setDayTime(level.getDayTime() + ticksPerSecond);
        level.getServer().getPlayerList().broadcastMessage(new TextComponent("Time is accelerating!"), ChatType.SYSTEM, Util.NIL_UUID);
    }

    public static void reverseTime(ServerLevel level, int ticks) {
        level.setDayTime(level.getDayTime() - ticks);
        level.getServer().getPlayerList().broadcastMessage(new TextComponent("Time is reversing!"), ChatType.SYSTEM, Util.NIL_UUID);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        if (isEntityProtected(target)) {
            removeEntityProtection(target);
        }
        target.setHealth(0);
        event.setCanceled(false);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        if (!target.isRemoved()) {
            target.remove(Entity.RemovalReason.KILLED);
        }
        event.setCanceled(false);
    }

    private boolean isEntityProtected(LivingEntity target) {
        try {
            for (Field field : target.getClass().getDeclaredFields()) {
                if (field.getName().toLowerCase().contains("protect") || field.getName().toLowerCase().contains("invuln")) {
                    field.setAccessible(true);
                    if (field.getBoolean(target)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}