package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.PlayerMotionUpdateMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisTomeLevelParticleMessage;
import vazkii.botania.api.mana.ManaItemHandler;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelekinesisTomeLevel extends Item {
    private static final float RANGE = 4.0F;
    private static final int MANA_COST_PER_TICK = 50;
    private static final int MANA_COST_ATTACK = 500;
    private static final int MANA_COST_AOE = 2000;
    private static final float BASE_DAMAGE_MIN = 20.0F;
    private static final float BASE_DAMAGE_MAX = 40.0F;
    private static final int AOE_COOLDOWN = 60;
    private static final float HOLD_DISTANCE = 7.5F;
    private static final int EXPIRE_TICKS = 5;
    private static final Map<Player, TomeData> playerDataMap = new HashMap<>();

    private static class TomeData {
        int ticksTillExpire = 0;
        int targetId = -1;
        double holdDistance = -1;
        double actualDistance = -1;
        int aoeCooldown = 0;
        int attackCooldown = 0;

        void reset() {
            targetId = -1;
            holdDistance = -1;
            actualDistance = -1;
        }
    }

    public TelekinesisTomeLevel(Properties properties) {
        super(properties);
    }

    private TomeData getData(Player player) {
        return playerDataMap.computeIfAbsent(player, p -> new TomeData());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.desc1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.desc2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.empty());
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.ability1")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.ability2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.ability3")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(EComponent.empty());
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.telekinesis_tome_level.cost")
                    .withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(EComponent.translatable("tooltip.ex_enigmaticlegacy.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player)) return;

        TomeData data = getData(player);

        if (data.aoeCooldown > 0) data.aoeCooldown--;
        if (data.attackCooldown > 0) data.attackCooldown--;

        if (data.ticksTillExpire > 0) {
            data.ticksTillExpire--;
        } else {
            data.reset();
        }

        if (selected && !level.isClientSide && level.getGameTime() % 20 == 0) {
            applyPassiveEffects(player, level);
        }

        if (level.isClientSide && selected) {
            checkAttackInput(player, stack);
        }
    }

    private void applyPassiveEffects(Player player, Level level) {
        AABB area = player.getBoundingBox().inflate(10.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 100, 3, true, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, true, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3, true, false));
        }

        sendPassiveParticles(player, (ServerLevel) level);
    }

    private void sendPassiveParticles(Player player, ServerLevel level) {
        TelekinesisTomeLevelParticleMessage packet = new TelekinesisTomeLevelParticleMessage(
                player.getX(), player.getY() + 1.0, player.getZ(),
                TelekinesisTomeLevelParticleMessage.Type.PASSIVE_AURA, 0.5F
        );
        NetworkHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        player.getX(), player.getY(), player.getZ(), 64.0, level.dimension())),
                packet
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            TomeData data = getData(player);
            if (data.targetId == -1 && data.aoeCooldown == 0) {
                if (!level.isClientSide) {
                    performAOEAttack(player, level);
                }
                return InteractionResultHolder.success(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        performTelekinesis(player, level, stack);
    }

    public void performTelekinesis(Player player, Level level, ItemStack stack) {
        TomeData data = getData(player);

        if (!consumeMana(player, MANA_COST_PER_TICK)) {
            return;
        }

        LivingEntity target = null;

        if (data.targetId != -1) {
            Entity entity = level.getEntity(data.targetId);
            if (entity instanceof LivingEntity living) {
                target = getExistingTarget(player, level, data.targetId, RANGE + 3);
            }
        }

        if (target == null) {
            target = searchForTarget(player, level, RANGE);
        }

        if (target == null) return;

        if (data.actualDistance == -1) {
            data.actualDistance = player.distanceTo(target);
        }

        double holdDistance = HOLD_DISTANCE;

        target.fallDistance = 0;
        if (target.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 3, true, false));
        }

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetPos;

        if (player.isShiftKeyDown()) {
            targetPos = playerPos.add(lookVec.scale(data.actualDistance));
        } else {
            targetPos = playerPos.add(lookVec.scale(holdDistance));
            data.actualDistance = player.distanceTo(target);
        }

        targetPos = targetPos.add(0, 0.5, 0);

        if (!level.isClientSide) {
            sendTelekinesisParticles(target, (ServerLevel) level);
        }

        if (isEntityBlacklisted(target)) return;

        moveEntityTowards(target, targetPos, 0.5F);

        data.targetId = target.getId();
        data.holdDistance = holdDistance;
        data.ticksTillExpire = EXPIRE_TICKS;
    }

    private void sendTelekinesisParticles(LivingEntity target, ServerLevel level) {
        Vec3 pos = target.position().add(0, target.getBbHeight() / 2, 0);
        TelekinesisTomeLevelParticleMessage packet = new TelekinesisTomeLevelParticleMessage(
                pos.x, pos.y, pos.z,
                TelekinesisTomeLevelParticleMessage.Type.TELEKINESIS, 1.0F
        );
        NetworkHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        pos.x, pos.y, pos.z, 64.0, level.dimension())),
                packet
        );
    }

    @Nullable
    private LivingEntity searchForTarget(Player player, Level level, float range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (int distance = 1; distance < 32; distance++) {
            Vec3 check = start.add(look.scale(distance));
            AABB searchBox = new AABB(
                    check.x - range, check.y - range, check.z - range,
                    check.x + range, check.y + range, check.z + range
            );

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive());

            if (!entities.isEmpty()) {
                return entities.stream()
                        .min(Comparator.comparingDouble(a -> a.distanceTo(player)))
                        .orElse(null);
            }
        }

        return null;
    }

    @Nullable
    private LivingEntity getExistingTarget(Player player, Level level, int targetId, float range) {
        Entity entity = level.getEntity(targetId);
        if (!(entity instanceof LivingEntity target)) return null;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        for (int distance = 1; distance < 32; distance++) {
            Vec3 check = start.add(look.scale(distance));
            AABB searchBox = new AABB(
                    check.x - range, check.y - range, check.z - range,
                    check.x + range, check.y + range, check.z + range
            );

            if (searchBox.contains(target.position())) {
                return target;
            }
        }

        return null;
    }


    private void checkAttackInput(Player player, ItemStack stack) {
    }

    public void performAttack(Player player) {
        if (player.level.isClientSide) return;

        TomeData data = getData(player);
        if (data.targetId == -1 || data.attackCooldown > 0) return;

        Entity entity = player.level.getEntity(data.targetId);
        if (!(entity instanceof LivingEntity target)) return;

        if (player.distanceTo(target) > 16) return;

        if (!consumeMana(player, MANA_COST_ATTACK)) return;

        performLightningAttack(player, target, player.isShiftKeyDown());

        data.attackCooldown = 10;
    }

    private void performLightningAttack(Player player, LivingEntity target, boolean knockback) {
        Level level = player.level;

        float damage = BASE_DAMAGE_MIN + (float)(Math.random() * (BASE_DAMAGE_MAX - BASE_DAMAGE_MIN));

        sendLightningParticles(player, target, (ServerLevel) level);

        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 0.5F, 1.5F);
        level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        target.hurt(createChaosDamageSource(player), damage);

        if (knockback) {
            Vec3 direction = player.getLookAngle().normalize();
            target.setDeltaMovement(
                    direction.x * 3.0,
                    direction.y * 1.5,
                    direction.z * 3.0
            );
            target.hurtMarked = true;

            TomeData data = getData(player);
            data.reset();
            data.ticksTillExpire = 0;
        }
    }

    private void sendLightningParticles(Player player, LivingEntity target, ServerLevel level) {
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);

        TelekinesisTomeLevelParticleMessage packet = new TelekinesisTomeLevelParticleMessage(
                start.x, start.y, start.z,
                end.x, end.y, end.z,
                TelekinesisTomeLevelParticleMessage.Type.LIGHTNING
        );
        NetworkHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        player.getX(), player.getY(), player.getZ(), 64.0, level.dimension())),
                packet
        );
    }

    private void performAOEAttack(Player player, Level level) {
        TomeData data = getData(player);

        if (!consumeMana(player, MANA_COST_AOE)) return;

        data.aoeCooldown = AOE_COOLDOWN;

        AABB area = player.getBoundingBox().inflate(8.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
                SoundSource.PLAYERS, 0.5F, 1.5F);
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 0.3F, 1.0F);

        sendAOEParticles(player, (ServerLevel) level);

        float aoeDamage = (BASE_DAMAGE_MIN + BASE_DAMAGE_MAX) / 2;
        for (LivingEntity target : targets) {
            target.hurt(createChaosDamageSource(player), aoeDamage);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));

            sendLightningParticles(player, target, (ServerLevel) level);
        }
    }

    private void sendAOEParticles(Player player, ServerLevel level) {
        TelekinesisTomeLevelParticleMessage packet = new TelekinesisTomeLevelParticleMessage(
                player.getX(), player.getY(), player.getZ(),
                TelekinesisTomeLevelParticleMessage.Type.AOE_BURST, 8.0F
        );
        NetworkHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        player.getX(), player.getY(), player.getZ(), 64.0, level.dimension())),
                packet
        );
    }

    private void moveEntityTowards(Entity entity, Vec3 target, float speed) {
        Vec3 current = entity.position().add(0, entity.getBbHeight() / 2, 0);
        Vec3 direction = target.subtract(current);

        double distance = direction.length();
        if (distance > 1.0) {
            direction = direction.normalize();
        }

        float adjustedSpeed = speed;
        if (distance < 1.5) {
            adjustedSpeed *= 0.5F;
        } else if (distance > 8) {
            adjustedSpeed *= (float)(distance / 8.0);
        }

        entity.setDeltaMovement(
                direction.x * adjustedSpeed,
                direction.y * adjustedSpeed,
                direction.z * adjustedSpeed
        );
        entity.hurtMarked = true;

        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(serverPlayer,
                    new PlayerMotionUpdateMessage(
                            direction.x * adjustedSpeed,
                            direction.y * adjustedSpeed,
                            direction.z * adjustedSpeed
                    ));
        }
    }

    private boolean consumeMana(Player player, int amount) {
        if (ManaItemHandler.instance().requestManaExact(player.getMainHandItem(), player, amount, true)) {
            return true;
        }
        return ManaItemHandler.instance().requestManaExact(player.getMainHandItem(), player, amount, true);
    }

    private DamageSource createChaosDamageSource(Player player) {
        return new DamageSource("mana") {
            @Override
            public Component getLocalizedDeathMessage(LivingEntity entity) {
                return EComponent.translatable("death.attack.mana",
                        entity.getDisplayName(), player.getDisplayName());
            }
        }.bypassArmor().setMagic();
    }

    private boolean isEntityBlacklisted(LivingEntity entity) {
        return entity instanceof Player && ((Player) entity).isCreative();
    }

    public static void clearPlayerData(Player player) {
        playerDataMap.remove(player);
    }
}
