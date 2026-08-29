package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ColorfulEntity;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ChargeLightningClientRegistry;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// BeamCrossTestItem 用于测试蓄力、粒子、闪电和环绕粗光束的组合效果。
public class BeamCrossTestItem extends Item {
    // 物品提示文本。
    private static final Component TOOLTIP_USE = Component.translatable("item.exe.beam_cross_test.tooltip.1");
    // 蓄力总时长，单位 tick。
    private static final int CHARGE_TOTAL_TICKS = 100;
    // 第三秒开始切换到闪电阶段，单位 tick。
    private static final int CHARGE_LIGHTNING_START_TICKS = 140;
    // 蓄力时可释放的最小阈值。
    private static final float MIN_LAUNCH_THRESHOLD = 0.2f;
    // 粗光束最大距离。
    private static final double BEAM_RANGE = 100.0;
    // 粗光束伤害。
    private static final float COLORFUL_DAMAGE = 44.0f;
    // 蓄力粒子生成半径下限。
    private static final double CHARGE_PARTICLE_RADIUS_MIN = 6.0;
    // 蓄力粒子生成半径上限。
    private static final double CHARGE_PARTICLE_RADIUS_MAX = 38.0;
    // 蓄力闪电生成半径下限。
    private static final double CHARGE_LIGHTNING_RADIUS_MIN = 8.0;
    // 蓄力闪电生成半径上限。
    private static final double CHARGE_LIGHTNING_RADIUS_MAX = 36.0;
    // 粒子速度基础值列表。
    private static final float[] CHARGE_PARTICLE_BASE_SPEEDS = { 0.05f, 0.24f, 0.44f, 0.6f };
    // 粒子每秒加速值。
    private static final float CHARGE_PARTICLE_SPEED_UP_PER_SEC = 1.25f;
    // 粒子速度上限。
    private static final float CHARGE_PARTICLE_MAX_SPEED = 1.5f;
    // 每个速度档位的粒子数量。
    private static final int[] CHARGE_PARTICLE_COUNTS = { 10, 20, 28, 36 };
    // 粒子寿命。
    private static final float CHARGE_PARTICLE_LIFE = 7.5f;
    // 粒子扩散幅度。
    private static final float CHARGE_PARTICLE_SPREAD = 0.08f;
    // 蓄力粒子可随机使用的全部形状。
    private static final int[] CHARGE_PARTICLE_SHAPES = {
            ParticleEmitTask.SHAPE_CIRCLE,
            ParticleEmitTask.SHAPE_HEART,
            ParticleEmitTask.SHAPE_TRIANGLE,
            ParticleEmitTask.SHAPE_SQUARE,
            ParticleEmitTask.SHAPE_STAR
    };
    // 闪电数量。
    private static final int CHARGE_LIGHTNING_COUNT = 20;
    // 闪电寿命。
    private static final float CHARGE_LIGHTNING_LIFETIME = 0.28f;
    // 闪电粗细。
    private static final float CHARGE_LIGHTNING_WIDTH = 0.28f;
    // 释放后环绕光束数量。
    private static final int RING_BEAM_COUNT = 8;
    // 环绕光束间隔 tick。
    private static final int RING_BEAM_INTERVAL_TICKS = 3;
    // 充能状态。
    private static final Map<StateKey, ChargeState> CHARGE_STATES = new ConcurrentHashMap<>();
    // 释放队列状态。
    private static final Map<StateKey, ReleaseState> RELEASE_STATES = new ConcurrentHashMap<>();

    public BeamCrossTestItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_USE);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            beginCharge(player.level(), player);
            player.startUsingItem(context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        beginCharge(level, player);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;
        ensureChargeState(level, player);
    }

//    @Override
//    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
//        if (!(entity instanceof Player player)) return;
//        // 暂时关闭额外环绕光束队列，只保留释放时立即生成的主光束。
////        if (!level.isClientSide()) {
////            processReleaseQueue(level, player);
////        }
//    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) return;

        float chargeProgress = finishCharge(level, player, timeCharged);
        if (level.isClientSide()) {
            return;
        }

        if (chargeProgress >= MIN_LAUNCH_THRESHOLD) {
            launchColorfulBeamSequence((ServerLevel) level, player);
            player.getCooldowns().addCooldown(this, 20);
        }
    }

    // 开始或刷新蓄力状态。
    private static void beginCharge(Level level, Player player) {
        StateKey key = StateKey.of(player, level.isClientSide());
        CHARGE_STATES.put(key, new ChargeState(level.getGameTime()));
        ChargeLightningClientRegistry.startBeamCross(player);
    }

    // 确保蓄力状态存在。
    private static void ensureChargeState(Level level, Player player) {
        StateKey key = StateKey.of(player, level.isClientSide());
        CHARGE_STATES.computeIfAbsent(key, unused -> new ChargeState(level.getGameTime()));
    }

    // 结束蓄力并返回进度。
    private static float finishCharge(Level level, Player player, int timeCharged) {
        StateKey key = StateKey.of(player, level.isClientSide());
        ChargeState state = CHARGE_STATES.remove(key);
        ChargeLightningClientRegistry.stop(player);
        if (state == null) {
            return Mth.clamp(timeCharged / (float) CHARGE_TOTAL_TICKS, 0.0f, 1.0f);
        }

        long elapsed = Math.max(0L, level.getGameTime() - state.startTick);
        return Mth.clamp(elapsed / (float) CHARGE_TOTAL_TICKS, 0.0f, 1.0f);
    }

    // 处理客户端蓄力特效，渲染事件会在每帧调用这里。
    public static void renderChargeEffects(Player player, float partialTick) {
        if (Exe.POST == null || player == null || !player.isAlive()) return;
        StateKey key = StateKey.of(player, true);
        ChargeState state = CHARGE_STATES.get(key);
        if (state == null) return;

        long currentTick = player.level().getGameTime();
        if (state.lastVisualTick == currentTick) return;
        state.lastVisualTick = currentTick;

        long elapsedTicks = Math.max(0L, currentTick - state.startTick);
        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 lookVec = player.getViewVector(partialTick);
        Vec3 handPos = LightingItem.getChargeEffectHandOrigin(player, eyePos, lookVec);
        Random random = new Random(player.getUUID().getLeastSignificantBits() ^ currentTick * 734287L);

        emitChargeParticles(handPos, elapsedTicks, random);
        if (elapsedTicks >= CHARGE_LIGHTNING_START_TICKS) {
            emitChargeLightning(player, handPos, random, currentTick);
        }
    }

    // 逐档发射不同速度的粒子，不修改粒子系统本身。
    private static void emitChargeParticles(Vec3 handPos, long elapsedTicks, Random random) {
        double elapsedSeconds = elapsedTicks / 20.0;
        for (int band = 0; band < CHARGE_PARTICLE_BASE_SPEEDS.length; band++) {
            int count = CHARGE_PARTICLE_COUNTS[band];
            for (int i = 0; i < count; i++) {
                double radius = lerp(random.nextDouble(), CHARGE_PARTICLE_RADIUS_MIN, CHARGE_PARTICLE_RADIUS_MAX);
                Vec3 offset = randomSphereOffset(random, radius);
                Vec3 start = handPos.add(offset);
                Vec3 direction = handPos.subtract(start);
                if (direction.lengthSqr() < 1.0E-6) continue;

                float speed = (float) Math.min(
                        CHARGE_PARTICLE_BASE_SPEEDS[band] + elapsedSeconds * CHARGE_PARTICLE_SPEED_UP_PER_SEC,
                        CHARGE_PARTICLE_MAX_SPEED
                );
                speed += band * 0.08f;
                speed = Math.min(speed, CHARGE_PARTICLE_MAX_SPEED);

                float size = 0.052f + band * 0.006f + random.nextFloat() * 0.01f;
                int rgb = band <= 1 ? 0xB0D8FF : (band == 2 ? 0x78C0FF : 0x50A8FF);
                int endRgb = band <= 1 ? 0x70A8FF : (band == 2 ? 0x4890FF : 0x2878FF);
                int shape = CHARGE_PARTICLE_SHAPES[random.nextInt(CHARGE_PARTICLE_SHAPES.length)];

                Exe.POST.addParticle(new ParticleEmitTask()
                        .position(start)
                        .direction((float) direction.x, (float) direction.y, (float) direction.z)
                        .speed(speed)
                        .spread(CHARGE_PARTICLE_SPREAD)
                        .life(CHARGE_PARTICLE_LIFE + band * 0.6f)
                        .gravity(0.0f)
                        .size(size, size, random.nextFloat() * 360.0f)
                        .color(rgb, 0.95f)
                        .endColor(endRgb, 0.9f)
                        .shape(shape)
                        .motion(ParticleEmitTask.MOTION_BALLISTIC)
                        .rate(0)
                        .duration(0.0f)
                        .burst(2));
            }
        }
    }

    // 生成朝向玩家的闪电。
    private static void emitChargeLightning(Player player, Vec3 handPos, Random random, long currentTick) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 target = LightingItem.getChargeEffectHandOrigin(player, eyePos, lookVec);

        for (int i = 0; i < CHARGE_LIGHTNING_COUNT; i++) {
            double radius = lerp(random.nextDouble(), CHARGE_LIGHTNING_RADIUS_MIN, CHARGE_LIGHTNING_RADIUS_MAX);
            Vec3 start = handPos.add(randomSphereOffset(random, radius));
            long seed = player.getUUID().getMostSignificantBits() ^ currentTick * 6151L ^ i * 7919L;
            Exe.POST.effects().addLightningStartToEnd(start, target,
                    CHARGE_LIGHTNING_LIFETIME, CHARGE_LIGHTNING_WIDTH, seed,
                    1.0f, 1.0f, 1.0f,
                    0.2f, 0.68f, 1.0f);
        }
    }

    // 释放后把一圈粗光束交给服务端逐个生成。
    private void launchColorfulBeamSequence(ServerLevel serverLevel, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 origin = LightingItem.getBeamHandOrigin(player, eyePos, lookVec);
//        Vec3 forward = horizontalDirection(lookVec);

        spawnColorfulBeam(serverLevel, player, origin, lookVec);

        // 暂时关闭额外环绕光束，只保留主光束。
//        StateKey key = StateKey.of(player, false);
//        RELEASE_STATES.put(key, new ReleaseState(origin, forward, serverLevel.getGameTime() + RING_BEAM_INTERVAL_TICKS, 1, 1));
    }

    // 服务端按 tick 继续生成环绕光束。
    private static void processReleaseQueue(Level level, Player player) {
        StateKey key = StateKey.of(player, false);
        ReleaseState state = RELEASE_STATES.get(key);
        if (state == null || !player.isAlive()) {
            RELEASE_STATES.remove(key);
            return;
        }

        long currentTick = level.getGameTime();
        if (currentTick < state.nextSpawnTick) return;

        ServerLevel serverLevel = (ServerLevel) level;
        spawnReleaseBatch(serverLevel, player, state);
        if (state.emittedCount >= RING_BEAM_COUNT) {
            RELEASE_STATES.remove(key);
            return;
        }
        state.nextSpawnTick = currentTick + RING_BEAM_INTERVAL_TICKS;
    }

    // 释放一批左右对称的圆形光束，偶数数量最后补正后方一道。
    private static void spawnReleaseBatch(ServerLevel serverLevel, Player player, ReleaseState state) {
        float step = 360.0f / RING_BEAM_COUNT;
        int remaining = RING_BEAM_COUNT - state.emittedCount;
        if (remaining <= 0) return;

        if (RING_BEAM_COUNT % 2 == 0 && state.nextBatchIndex == RING_BEAM_COUNT / 2) {
            spawnColorfulBeam(serverLevel, player, state.origin, rotatedDirection(state.forward, 180.0f));
            state.emittedCount++;
            state.nextBatchIndex++;
            return;
        }

        float angle = state.nextBatchIndex * step;
        spawnColorfulBeam(serverLevel, player, state.origin, rotatedDirection(state.forward, angle));
        state.emittedCount++;
        if (state.emittedCount < RING_BEAM_COUNT) {
            spawnColorfulBeam(serverLevel, player, state.origin, rotatedDirection(state.forward, -angle));
            state.emittedCount++;
        }
        state.nextBatchIndex++;
    }

    // 创建一条 ColorfulEntity，并同步它的朝向。
    private static void spawnColorfulBeam(ServerLevel serverLevel, Player player, Vec3 origin, Vec3 direction) {
        Vec3 forward = normalizeOrFallback(direction, new Vec3(0.0, 0.0, 1.0));
        Vec3 endpoint = origin.add(forward.scale(BEAM_RANGE));

        ColorfulEntity beam = new ColorfulEntity(ModEntities.COLORFUL_COIN_ENTITY.get(), serverLevel);
        beam.setBeamData(origin, endpoint, player, COLORFUL_DAMAGE);
        beam.setUseOwnerEyeHitOrigin(false);
        applyBeamRotation(beam, forward);
        serverLevel.addFreshEntity(beam);
    }

    // 根据方向向量设置实体朝向。
    private static void applyBeamRotation(ColorfulEntity beam, Vec3 direction) {
        Vec3 normalized = normalizeOrFallback(direction, new Vec3(0.0, 0.0, 1.0));
        double horizontal = Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z);
        float yaw = (float) (Math.atan2(normalized.z, normalized.x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.atan2(normalized.y, horizontal) * 180.0 / Math.PI);
        beam.setYRot(yaw);
        beam.setXRot(pitch);
    }

    // 在水平面内绕世界 Y 轴旋转，让环绕光束横向展开一圈。
    private static Vec3 rotatedDirection(Vec3 forward, float angleDegrees) {
        Vec3 base = horizontalDirection(forward);
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Vec3 rotated = new Vec3(
                base.x * cos - base.z * sin,
                0.0,
                base.x * sin + base.z * cos
        );
        return normalizeOrFallback(rotated, base);
    }

    // 将方向压到水平 XZ 平面，避免抬头或低头时光束竖向展开。
    private static Vec3 horizontalDirection(Vec3 direction) {
        Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
        return normalizeOrFallback(horizontal, new Vec3(0.0, 0.0, 1.0));
    }

    // 生成均匀球面偏移。
    private static Vec3 randomSphereOffset(Random random, double radius) {
        double u = random.nextDouble() * 2.0 - 1.0;
        double theta = random.nextDouble() * Math.PI * 2.0;
        double horizontal = Math.sqrt(Math.max(0.0, 1.0 - u * u));
        return new Vec3(
                Math.cos(theta) * horizontal * radius,
                u * radius,
                Math.sin(theta) * horizontal * radius
        );
    }

    // 向量归一化，零向量时回退到默认方向。
    private static Vec3 normalizeOrFallback(Vec3 vec, Vec3 fallback) {
        if (vec.lengthSqr() < 1.0E-6) {
            return fallback;
        }
        return vec.normalize();
    }

    // 线性插值。
    private static double lerp(double t, double min, double max) {
        return min + (max - min) * t;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.BOW;
    }

    // 蓄力状态 key。
    private record StateKey(UUID playerId, boolean clientSide) {
        static StateKey of(Player player, boolean clientSide) {
            return new StateKey(player.getUUID(), clientSide);
        }
    }

    // 蓄力时间状态。
    private static final class ChargeState {
        private final long startTick;
        private long lastVisualTick = Long.MIN_VALUE;

        private ChargeState(long startTick) {
            this.startTick = startTick;
        }
    }

    // 释放后环绕光束状态。
    private static final class ReleaseState {
        private final Vec3 origin;
        private final Vec3 forward;
        private long nextSpawnTick;
        private int emittedCount;
        private int nextBatchIndex;

        private ReleaseState(Vec3 origin, Vec3 forward, long nextSpawnTick, int emittedCount, int nextBatchIndex) {
            this.origin = origin;
            this.forward = forward;
            this.nextSpawnTick = nextSpawnTick;
            this.emittedCount = emittedCount;
            this.nextBatchIndex = nextBatchIndex;
        }
    }
}
