package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ChargeTracker;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.RailgunBeamEntity;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ChargeLightningClientRegistry;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.List;
import java.util.Random;
public class LightingItem extends Item {
    private static final Component TOOLTIP_USE = Component.translatable("item.exe.tooltip.1");
    private static final int[] FULL_CHARGE_PARTICLE_SHAPES = new int[] {
            ParticleEmitTask.SHAPE_CIRCLE,
            ParticleEmitTask.SHAPE_SQUARE,
            ParticleEmitTask.SHAPE_TRIANGLE,
            ParticleEmitTask.SHAPE_HEART,
            ParticleEmitTask.SHAPE_STAR
    };
    protected double maxRange = 50.0;
    protected float beamDamage = 20.0f;

    private static final double BEAM_HAND_FORWARD_OFFSET = 0.35;
    private static final double BEAM_HAND_SIDE_OFFSET = 0.38;
    private static final double BEAM_HAND_DOWN_OFFSET = 0.32;
    private static final double CHARGE_EFFECT_LEFT_OFFSET = -0.15;

    public LightingItem(Properties pProperties) {
        super(pProperties);
    }

    public void loadConfigValues() {
        this.maxRange = ConfigFile.MaxRange();
        this.beamDamage = ConfigFile.BeamDamage();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_USE);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            ChargeTracker.startCharge(player, player.tickCount);
            ChargeLightningClientRegistry.start(player, false);
            player.startUsingItem(context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ChargeTracker.startCharge(player, player.tickCount);
        ChargeLightningClientRegistry.start(player, false);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player) {
            ChargeTracker.updateCharge(player, player.tickCount);

            if (level.isClientSide() && ChargeTracker.isFullyCharged(player)) {
                emitFullChargeParticles(player, false);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) return;
        if (!ChargeTracker.isCharging(player)) return;
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof LightingItem) return;

        ChargeTracker.stopCharge(player);
        ChargeLightningClientRegistry.stop(player);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (livingEntity instanceof Player player) {
            float chargeProgress = ChargeTracker.stopCharge(player);
            ChargeLightningClientRegistry.stop(player);

            if (!level.isClientSide() && chargeProgress >= ChargeTracker.MIN_LAUNCH_THRESHOLD) {
                launchBeam(level, player, chargeProgress);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
    }
    private void launchBeam(Level level, Player player, float chargeProgress) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 origin = getBeamHandOrigin(player, eyePos, lookVec);

        double range = maxRange * chargeProgress;
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        RailgunBeamEntity beamEntity = new RailgunBeamEntity(ModEntities.RAILGUN_BEAM_ENTITY.get(), level);
        beamEntity.setBeamData(origin, endPos, player.getUUID(), beamDamage * chargeProgress);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(beamEntity);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.8f, 1.0f);

        player.getCooldowns().addCooldown(this, 10);
    }

    public static Vec3 getBeamHandOrigin(Player player, Vec3 eyePos, Vec3 lookVec) {
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = up.cross(lookVec);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }

        double side = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? -BEAM_HAND_SIDE_OFFSET : BEAM_HAND_SIDE_OFFSET;
        return eyePos
                .add(lookVec.scale(BEAM_HAND_FORWARD_OFFSET))
                .add(right.scale(side))
                .add(0.0, -BEAM_HAND_DOWN_OFFSET, 0.0)
                .add(0,0.2,0);
    }

    public static Vec3 getChargeEffectHandOrigin(Player player, Vec3 eyePos, Vec3 lookVec) {
        return getBeamHandOrigin(player, eyePos, lookVec)
                .add(getViewLeft(lookVec).scale(CHARGE_EFFECT_LEFT_OFFSET));
    }

    private static Vec3 getViewLeft(Vec3 lookVec) {
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = up.cross(lookVec);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }
        return right.scale(-1.0);
    }

    public static void emitFullChargeParticles(Player player, boolean colorful) {
        if (Exe.POST == null || player.tickCount % 2 != 0) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 handPos = getChargeEffectHandOrigin(player, eyePos, lookVec);
        Random random = new Random(player.getUUID().getLeastSignificantBits() ^ (long) player.tickCount * 918271L);

        int rgb = colorful ? 0x864e74 : 0xA5D8FF;
        int endRgb = colorful ? 0xAF5579 : 0xA5D8Fe;
        for (int i = 0; i < 18; i++) {
            Vec3 start = handPos.add(randomSphereOffset(random, 1.5));
            Vec3 direction = handPos.subtract(start);
            if (direction.lengthSqr() < 1.0E-6) continue;

            float size = 0.02f + random.nextFloat() * 0.015f;
            float rotation = random.nextFloat() * 360.0f;
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(start)
                    .direction((float) direction.x, (float) direction.y, (float) direction.z)
                    .speed(0.1f + random.nextFloat() * 1.1f)
                    .spread(0.06f)
                    .life(1.24f + random.nextFloat() * 0.16f)
                    .gravity(0.0f)
                    .size(size, size, rotation)
                    .color(rgb, 0.9f)
                    .endColor(endRgb, 0.8f)
                    .shape(randomFullChargeParticleShape(random))
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .rate(0)
                    .duration(0.0f)
                    .burst(10));
        }
    }

    private static int randomFullChargeParticleShape(Random random) {
        return FULL_CHARGE_PARTICLE_SHAPES[random.nextInt(FULL_CHARGE_PARTICLE_SHAPES.length)];
    }

    private static Vec3 randomSphereOffset(Random random, double radius) {
        double x = (random.nextDouble() * 2.0 - 1.0) * radius;
        double y = (random.nextDouble() * 2.0 - 1.0) * radius;
        double z = (random.nextDouble() * 2.0 - 1.0) * radius;
        return new Vec3(x, y, z);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.BOW;
    }
}
