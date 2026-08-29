package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.CameraShakeUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// HeavenlyThunderEntity 是天雷附魔技能的持续实体，负责服务端范围伤害和客户端法阵雷暴视觉。
public class HeavenlyThunderEntity extends Entity {
    private static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(HeavenlyThunderEntity.class, EntityDataSerializers.INT); // 技能视觉随机种子。
    public static final int CAST_TICKS = 20; // 法阵展开 tick，展开完成前不造成伤害。
    public static final int LIFE_TICKS = 200; // 天雷技能总持续 tick。
    public static final int DAMAGE_INTERVAL_TICKS = 6; // 服务端持续伤害间隔默认值，实际读取天雷独立配置。
    public static final int RING_INTERVAL_TICKS = 10; // 天空法阵扩散雷电提交间隔，加快频率提高密度。
    public static final int BOLT_INTERVAL_TICKS = 5; // 随机下落雷提交间隔，降低外围落雷频率突出中心主雷。
    public static final int SKY_RING_MIN_COUNT = 1; // 每批天空扩散雷电最少数量。
    public static final int SKY_RING_RANDOM_COUNT = 1; // 每批天空扩散雷电额外随机数量。
    public static final double HORIZONTAL_RANGE = 70.0D; // 天雷方形范围 x/z 半宽。
    public static final double DOWN_BOLT_VISUAL_RANGE = 58.0D; // 随机落雷视觉圆形半径，收进法阵范围内。
    public static final double UP_RANGE = 70.0D; // 天雷向上范围和法阵高度。
    public static final double DOWN_RANGE = 5.0D; // 天雷向下伤害范围。
    public static final float CIRCLE_START_RADIUS = 10.5F; // 天空法阵初始半径。
    public static final float CIRCLE_END_RADIUS = 70.0F; // 天空法阵最终半径。
    public static final float CIRCLE_FADE_TIME = 0.3F; // 天空法阵淡出时间。
    public static final float CIRCLE_ALPHA = 0.22F; // 天空法阵整体透明度。
    public static final float LIGHTNING_JITTER_SCALE = 1.65F; // 随机下落雷几何抖动倍率，数值越大路径左右偏移越明显。
    public static final float GROUND_RING_CHANCE = 0.72F; // 每道随机下落雷落地后生成扩散雷电的概率。
    public static final float SKY_RING_GROW_TIME_MIN = 1.52F; // 天空扩散雷圈最短扩散时间，越大扩散越慢。
    public static final float SKY_RING_GROW_TIME_RANDOM = 2.23F; // 天空扩散雷圈额外随机扩散时间。
    public static final float SKY_RING_FADE_TIME_MIN = 0.28F; // 天空扩散雷圈最短淡出时间。
    public static final float SKY_RING_FADE_TIME_RANDOM = 0.20F; // 天空扩散雷圈额外随机淡出时间。
    public static final float SKY_RING_WIDTH_MIN = 5.8F; // 天空扩散雷圈最小宽度，加粗法阵雷电。
    public static final float SKY_RING_WIDTH_RANDOM = 1.8F; // 天空扩散雷圈额外随机宽度。
    public static final int DOWN_BOLT_LOCATION_MIN_COUNT = 10; // 每批随机落雷地点最少数量，每个地点会再落下 1~3 道。
    public static final int DOWN_BOLT_LOCATION_RANDOM_COUNT = 6; // 每批随机落雷地点额外随机数量。
    public static final double DOWN_BOLT_CLUSTER_SKY_OFFSET = 12.0D; // 同地点多道雷的天空起点水平散布范围。
    public static final int DOWN_BOLT_THIN_MIN_COUNT = 2; // 每个落雷地点伴随细雷最少数量。
    public static final int DOWN_BOLT_THIN_RANDOM_COUNT = 3; // 每个落雷地点伴随细雷额外随机数量，结果为 2~4 道。
    public static final double DOWN_BOLT_THIN_HIT_OFFSET = 1.25D; // 细雷命中点围绕粗雷的水平散布范围，降低后更集中。
    public static final double DOWN_BOLT_THIN_SKY_OFFSET = 7.0D; // 细雷天空起点水平散布范围，降低后更像主雷分叉。
    public static final float DOWN_BOLT_THICK_WIDTH_MIN = 2.8F; // 法阵周围粗雷最小宽度，和中心主雷拉开层次。
    public static final float DOWN_BOLT_THICK_WIDTH_RANDOM = 6.0F; // 法阵周围粗雷额外随机宽度，避免随机出接近中心主雷的大雷。
    public static final float DOWN_BOLT_THIN_WIDTH_MIN = 1.2F; // 法阵伴随细雷最小宽度。
    public static final float DOWN_BOLT_THIN_WIDTH_RANDOM = 0.8F; // 法阵伴随细雷额外随机宽度。
    public static final float DOWN_BOLT_NOISE_STRENGTH_MIN = 0.08F; // 法阵落雷噪声扰动强度下限。
    public static final float DOWN_BOLT_NOISE_STRENGTH_RANDOM = 0.10F; // 法阵落雷噪声扰动强度额外随机范围。
    public static final float HEAVENLY_SPLASH_CHANCE = 0.78F; // 法阵落雷地点追加溅射雷的概率，提高后让落雷溅射更明显。
    public static final int AMBIENT_SHAKE_INTERVAL_TICKS = 10; // 法阵持续期间轻微屏幕震动提交间隔。
    public static final int AMBIENT_SHAKE_DURATION_TICKS = 12; // 法阵持续轻微屏幕震动持续 tick。
    public static final float AMBIENT_SHAKE_STRENGTH = 0.32F; // 法阵持续轻微屏幕震动强度。
    public static final int BOLT_SHAKE_DURATION_TICKS = 8; // 法阵落雷瞬间屏幕震动持续 tick。
    public static final float BOLT_SHAKE_STRENGTH = 0.58F; // 法阵落雷瞬间屏幕震动强度。
    public static final int CENTER_BURST_INTERVAL_TICKS = 6; // 法阵中心大型落雷持续释放间隔。
    public static final int CENTER_MAIN_BOLT_MIN_COUNT = 2; // 每批中心大型主雷最少数量。
    public static final int CENTER_MAIN_BOLT_RANDOM_COUNT = 2; // 每批中心大型主雷额外随机数量，结果为 2~3 道。
    public static final double CENTER_ANCHOR_BOLT_HIT_OFFSET = 0.65D; // 每批中心锚点主雷围绕法阵中心的小范围散布。
    public static final double CENTER_MAIN_BOLT_RADIUS_MIN = 1.5D; // 区域中心主雷距离法阵中心的最小半径。
    public static final double CENTER_MAIN_BOLT_RADIUS_MAX = 11.0D; // 区域中心主雷距离法阵中心的最大半径。
    public static final double CENTER_MAIN_BOLT_ANGLE_JITTER = 0.28D; // 同批区域中心主雷扇区角度随机扰动。
    public static final int CENTER_BOLT_THIN_MIN_COUNT = 5; // 中心大型落雷伴随细雷最少数量。
    public static final int CENTER_BOLT_THIN_RANDOM_COUNT = 4; // 中心大型落雷伴随细雷额外随机数量，结果为 5~8 道。
    public static final double CENTER_BOLT_THIN_HIT_OFFSET = 2.0D; // 中心细雷命中点围绕中心的水平散布范围。
    public static final double CENTER_BOLT_SKY_OFFSET = 2.0D; // 中心主雷天空起点水平偏移，提高后每批顶点差异更明显。
    public static final float CENTER_BOLT_JITTER_SCALE = 0.32F; // 中心主雷几何抖动倍率，降低后主雷更直。
    public static final int CENTER_BOLT_TERMINAL_BOUNCE_COUNT = 0; // 中心主雷末端回弹数量，关闭后减少落点分叉。
    public static final float CENTER_BOLT_WIDTH_MIN = 5.0F; // 中心主粗雷最小宽度，提高后中心大型主雷更粗。
    public static final float CENTER_BOLT_WIDTH_RANDOM = 5.0F; // 中心主粗雷额外随机宽度，提高后主雷粗细变化更明显。
    public static final float CENTER_GROUND_RING_START_RADIUS = 0.45F; // 中心持续扩散雷圈起始半径。
    public static final float CENTER_GROUND_RING_END_RADIUS_MIN = 10.0F; // 中心持续扩散雷圈最小结束半径。
    public static final float CENTER_GROUND_RING_END_RADIUS_RANDOM = 8.0F; // 中心持续扩散雷圈额外随机结束半径。
    public static final float CENTER_GROUND_RING_GROW_TIME = 0.35F; // 中心持续扩散雷圈扩散时间。
    public static final float CENTER_GROUND_RING_HOLD_TIME = 0.08F; // 中心持续扩散雷圈保持时间。
    public static final float CENTER_GROUND_RING_FADE_TIME = 0.35F; // 中心持续扩散雷圈淡出时间。
    public static final float CENTER_GROUND_RING_WIDTH_MIN = 1.2F; // 中心持续扩散雷圈最小宽度。
    public static final float CENTER_GROUND_RING_WIDTH_RANDOM = 0.8F; // 中心持续扩散雷圈额外随机宽度。
    public static final int CENTER_SPLASH_REPEAT_MIN_COUNT = 2; // 每批中心主雷最少提交大型溅射次数。
    public static final int CENTER_SPLASH_REPEAT_RANDOM_COUNT = 2; // 每批中心主雷额外随机大型溅射次数，结果为 2~3 次。
    public static final int CENTER_SPLASH_MIN_COUNT = 10; // 中心大型溅射雷最少数量。
    public static final int CENTER_SPLASH_RANDOM_COUNT = 6; // 中心大型溅射雷额外随机数量。
    public static final int CENTER_SPLASH_INVERTED_V_MIN_COUNT = 5; // 中心大型主雷倒 V 溅射最少数量。
    public static final int CENTER_SPLASH_INVERTED_V_RANDOM_COUNT = 4; // 中心大型主雷倒 V 溅射额外随机数量。
    public static final float CENTER_SPLASH_WIDTH_SCALE = 2.2F; // 中心大型溅射雷宽度倍率。
    public static final float CENTER_SPLASH_JITTER_SCALE = 0.30F; // 中心大型溅射雷路径抖动倍率。
    public static final double CENTER_SPLASH_RANGE_SCALE = 2.45D; // 中心大型溅射雷范围倍率，提高后溅射扩散更远。
    public static final double CENTER_SPLASH_HEIGHT_SCALE = 2.20D; // 中心大型溅射雷高度倍率，提高后普通外扩和倒 V 溅射都会更高。
    public static final float CENTER_SPLASH_GROW_TIME_SCALE = 1.45F; // 中心大型溅射雷显现时间倍率，提高后出现更慢更持久。
    public static final float CENTER_SPLASH_HOLD_TIME_SCALE = 1.75F; // 中心大型溅射雷保持时间倍率，提高后亮起停留更久。
    public static final float CENTER_SPLASH_FADE_TIME_SCALE = 1.85F; // 中心大型溅射雷淡出时间倍率，提高后消散更慢。
    public static final double CENTER_SPLASH_Y_OFFSET = 0.65D; // 中心大型溅射起点上抬距离，增加后大型溅射高度更明显。
    public static final int CENTER_CRAWL_MIN_COUNT = 10; // 中心向外蔓延地面雷最少数量。
    public static final int CENTER_CRAWL_RANDOM_COUNT = 7; // 中心向外蔓延地面雷额外随机数量。
    public static final double CENTER_CRAWL_RADIUS_MIN = 8.0D; // 中心蔓延雷最小半径。
    public static final double CENTER_CRAWL_RADIUS_RANDOM = 24.0D; // 中心蔓延雷额外随机半径。
    public static final float CENTER_CRAWL_WIDTH_MIN = 0.75F; // 中心蔓延雷最小宽度。
    public static final float CENTER_CRAWL_WIDTH_RANDOM = 0.90F; // 中心蔓延雷额外随机宽度。
    public static final float CENTER_CRAWL_JITTER_SCALE = 0.65F; // 中心蔓延雷路径抖动倍率。
    public static final float CENTER_CRAWL_GROW_TIME_MIN = 0.16F; // 中心蔓延雷最短显现时间。
    public static final float CENTER_CRAWL_GROW_TIME_RANDOM = 0.22F; // 中心蔓延雷额外随机显现时间。
    public static final float CENTER_CRAWL_HOLD_TIME_MIN = 0.03F; // 中心蔓延雷最短保持时间。
    public static final float CENTER_CRAWL_HOLD_TIME_RANDOM = 0.06F; // 中心蔓延雷额外随机保持时间。
    public static final float CENTER_CRAWL_FADE_TIME_MIN = 0.18F; // 中心蔓延雷最短淡出时间。
    public static final float CENTER_CRAWL_FADE_TIME_RANDOM = 0.20F; // 中心蔓延雷额外随机淡出时间。
    @Nullable
    private UUID ownerUUID; // 释放者 UUID。

    public HeavenlyThunderEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    // 初始化天雷实体位置、释放者和视觉随机种子。
    public void setThunderData(Vec3 position, @Nullable Entity owner, int visualSeed) {
        this.setPos(position.x, position.y, position.z);
        this.ownerUUID = owner == null ? null : owner.getUUID();
        this.entityData.set(VISUAL_SEED, visualSeed);
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(VISUAL_SEED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            tickClientVisuals();
        } else {
            tickServerDamage();
        }
    }

    // 客户端提交天空法阵、法阵雷圈和随机下落雷视觉。
    public void tickClientVisuals() {
        if (Exe.POST == null) return;
        if (this.tickCount == 1) {
            submitSkyCircle();
            HeavenlyThunderCloudRingEffects.submit(this);
        }
        if (this.tickCount < CAST_TICKS) return;
        List<HeavenlyThunderBoltData> tickBolts = new ArrayList<>();
        if (shouldSubmitCenterThunderBurst()) {
            collectCenterThunderBurst(tickBolts);
        }
        if (this.tickCount == CAST_TICKS || this.tickCount % AMBIENT_SHAKE_INTERVAL_TICKS == 0) {
            submitAmbientScreenShake();
        }
        if (this.tickCount == CAST_TICKS || this.tickCount % RING_INTERVAL_TICKS == 0) {
            submitSkyLightningRing();
        }
        if (this.tickCount == CAST_TICKS || this.tickCount % BOLT_INTERVAL_TICKS == 0) {
            collectRandomDownBolts(tickBolts);
            submitBoltScreenShake();
        }
        submitDownBoltBatch(tickBolts);
    }

    // 服务端在法阵展开后按方形范围持续造成伤害。
    public void tickServerDamage() {
        tickServerSound();
        if (this.tickCount >= CAST_TICKS && this.tickCount % TridentPlusConfig.heavenlyThunderDamageIntervalTicks() == 0) {
            applyAreaDamage(TridentPlusConfig.heavenlyThunderDamage());
        }
        if (this.tickCount > LIFE_TICKS) {
            this.discard();
        }
    }

    // 提交持续到技能结束的天空法阵冲击波。
    public void submitSkyCircle() {
        Vec3 center = skyCircleCenter();
        float holdTime = Math.max(0.1F, (LIFE_TICKS - CAST_TICKS) / 20.0F);
        Exe.POST.effects().addCircleShockwave(center, new Vec3(0.0D, -1.0D, 0.0D),
                CIRCLE_START_RADIUS, CIRCLE_END_RADIUS, CAST_TICKS / 20.0F, holdTime, CIRCLE_FADE_TIME,
                0.0F, getVisualSeed(0, 0xC17C1EL), CIRCLE_ALPHA);
    }


    // 在天空法阵位置提交扩散雷电圈。
    public void submitSkyLightningRing() {
        long seed = getVisualSeed(this.tickCount, 0x51A7F00DL);
        RandomSource random = RandomSource.create(seed);
        int count = SKY_RING_MIN_COUNT + random.nextInt(SKY_RING_RANDOM_COUNT);
        for (int i = 0; i < count; i++) {
            submitOneSkyLightningRing(random, seed + i * 7919L);
        }
    }

    // 提交单个更粗的天空法阵扩散雷圈。
    public void submitOneSkyLightningRing(RandomSource random, long seed) {
        TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, true);
        float startRadius = 1.0F + random.nextFloat() * 9.0F;
        float endRadius = 35.0F + random.nextFloat() * 37.0F;
        float growTime = SKY_RING_GROW_TIME_MIN + random.nextFloat() * SKY_RING_GROW_TIME_RANDOM;
        float holdTime = 0.04F + random.nextFloat() * 0.06F;
        float fadeTime = SKY_RING_FADE_TIME_MIN + random.nextFloat() * SKY_RING_FADE_TIME_RANDOM;
        float width = SKY_RING_WIDTH_MIN + random.nextFloat() * SKY_RING_WIDTH_RANDOM;
        Exe.POST.effects().addLightningRing(skyCircleCenter(), new Vec3(0.0D, -1.0D, 0.0D),
                startRadius, endRadius, growTime, holdTime, fadeTime, width, seed,
                style.ringCoreR, style.ringCoreG, style.ringCoreB, style.ringBloomR, style.ringBloomG, style.ringBloomB);
    }

    // 提交法阵持续期间的轻微范围屏幕震动。
    public void submitAmbientScreenShake() {
        CameraShakeUtil.addShake(this.position(), (float) HORIZONTAL_RANGE, AMBIENT_SHAKE_DURATION_TICKS, AMBIENT_SHAKE_STRENGTH);
    }

    // 提交法阵落雷瞬间的短促范围屏幕震动。
    public void submitBoltScreenShake() {
        CameraShakeUtil.addShake(this.position(), (float) HORIZONTAL_RANGE, BOLT_SHAKE_DURATION_TICKS, BOLT_SHAKE_STRENGTH);
    }

    // 在法阵圆形视觉范围内随机提交多道从法阵劈下的视觉雷电。
    public void submitRandomDownBolts() {
        List<HeavenlyThunderBoltData> bolts = new ArrayList<>();
        collectRandomDownBolts(bolts);
        submitDownBoltBatch(bolts);
    }

    // 收集当前 tick 的外围随机落雷，交给客户端 tick 与中心落雷统一对齐到地。
    public void collectRandomDownBolts(List<HeavenlyThunderBoltData> bolts) {
        long batchSeed = getVisualSeed(this.tickCount, 0xB0175L);
        RandomSource random = RandomSource.create(batchSeed);
        int count = DOWN_BOLT_LOCATION_MIN_COUNT + random.nextInt(DOWN_BOLT_LOCATION_RANDOM_COUNT);
        for (int i = 0; i < count; i++) {
            collectOneDownBoltCluster(random, batchSeed + i * 7919L, bolts);
        }
    }

    // 判断当前 tick 是否需要持续提交中心大型落雷。
    public boolean shouldSubmitCenterThunderBurst() {
        return this.tickCount >= CAST_TICKS
                && (this.tickCount == CAST_TICKS || this.tickCount % CENTER_BURST_INTERVAL_TICKS == 0);
    }

    // 提交法阵中心持续大型落雷、配套溅射和向外蔓延地面雷电。
    public void submitCenterThunderBurst() {
        List<HeavenlyThunderBoltData> bolts = new ArrayList<>();
        collectCenterThunderBurst(bolts);
        submitDownBoltBatch(bolts);
    }

    // 收集中心主雷和伴随细雷，配套地面效果保持当前 tick 提交。
    public void collectCenterThunderBurst(List<HeavenlyThunderBoltData> bolts) {
        long seed = getVisualSeed(this.tickCount, 0xCE17B017L);
        RandomSource random = RandomSource.create(seed);
        Vec3 center = this.position();
        TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, true);
        int mainCount = CENTER_MAIN_BOLT_MIN_COUNT + random.nextInt(CENTER_MAIN_BOLT_RANDOM_COUNT);
        double regionalBaseAngle = random.nextDouble() * Math.PI * 2.0D;
        int regionalCount = Math.max(mainCount - 1, 1);
        for (int i = 0; i < mainCount; i++) {
            long mainSeed = seed ^ (0x9E3779B97F4A7C15L + i * 0x632BE59BD9B4E019L);
            RandomSource mainRandom = RandomSource.create(mainSeed);
            Vec3 hitPos = i == 0
                    ? randomCenterAnchorHitPos(mainRandom, center)
                    : randomCenterMainBoltHitPos(mainRandom, center, regionalBaseAngle, i - 1, regionalCount);
            bolts.add(createCenterMainBolt(mainRandom, mainSeed, hitPos, style));
        }

        int thinCount = CENTER_BOLT_THIN_MIN_COUNT + random.nextInt(CENTER_BOLT_THIN_RANDOM_COUNT);
        for (int i = 0; i < thinCount; i++) {
            long boltSeed = seed + (i + 1L) * 6151L;
            RandomSource boltRandom = RandomSource.create(boltSeed);
            Vec3 hitPos = center.add((boltRandom.nextDouble() * 2.0D - 1.0D) * CENTER_BOLT_THIN_HIT_OFFSET,
                    0.0D, (boltRandom.nextDouble() * 2.0D - 1.0D) * CENTER_BOLT_THIN_HIT_OFFSET);
            bolts.add(createDownBoltData(boltRandom, boltSeed, hitPos, style, false));
        }
        submitCenterGroundPulse(random, center, style, seed ^ 0x601DCA7L);
        submitCenterLargeSplash(random, center, style, seed ^ 0x5C17A5B0L);
        TridentLightningParticleEffects.emitMagicCircleLandingBurst(center, seed ^ 0xB451CAFE);
        submitCenterGroundCrawl(random, center, style, seed ^ 0xC0A771L);
    }

    // 生成每批第一道中心锚点主雷的落点，始终保留法阵中心的垂直视觉主轴。
    public Vec3 randomCenterAnchorHitPos(RandomSource random, Vec3 center) {
        return center.add(
                (random.nextDouble() * 2.0D - 1.0D) * CENTER_ANCHOR_BOLT_HIT_OFFSET,
                0.0D,
                (random.nextDouble() * 2.0D - 1.0D) * CENTER_ANCHOR_BOLT_HIT_OFFSET
        );
    }

    // 在中心核心圆形区域内按扇区生成大型主雷落点，避免同批主雷互相重叠。
    public Vec3 randomCenterMainBoltHitPos(RandomSource random, Vec3 center, double baseAngle,
                                           int regionalIndex, int regionalCount) {
        double sectorAngle = Math.PI * 2.0D * regionalIndex / Math.max(regionalCount, 1);
        double angle = baseAngle + sectorAngle
                + (random.nextDouble() * 2.0D - 1.0D) * CENTER_MAIN_BOLT_ANGLE_JITTER;
        double radiusT = Math.sqrt(random.nextDouble());
        double radius = CENTER_MAIN_BOLT_RADIUS_MIN
                + (CENTER_MAIN_BOLT_RADIUS_MAX - CENTER_MAIN_BOLT_RADIUS_MIN) * radiusT;
        return center.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
    }

    // 提交一道中心大型主雷，使用独立宽度和更小偏移，和周围随机落雷保持明显层次。
    public void submitCenterMainBolt(RandomSource random, long seed, Vec3 hitPos, TridentLightningColorStyle style) {
        submitDownBoltBatch(List.of(createCenterMainBolt(random, seed, hitPos, style)));
    }

    // 创建中心大型主雷数据，保留原有随机 growTime 作为单道落雷速度。
    public HeavenlyThunderBoltData createCenterMainBolt(RandomSource random, long seed, Vec3 hitPos, TridentLightningColorStyle style) {
        Vec3 skyPos = skyCircleCenter().add((random.nextDouble() * 2.0D - 1.0D) * CENTER_BOLT_SKY_OFFSET, 0.0D,
                (random.nextDouble() * 2.0D - 1.0D) * CENTER_BOLT_SKY_OFFSET);
        float width = CENTER_BOLT_WIDTH_MIN + random.nextFloat() * CENTER_BOLT_WIDTH_RANDOM;
        float noiseStrength = DOWN_BOLT_NOISE_STRENGTH_MIN + random.nextFloat() * DOWN_BOLT_NOISE_STRENGTH_RANDOM;
        float noiseIndex = random.nextBoolean() ? CoinLightningQueue.NOISE_INDEX_ALT : CoinLightningQueue.NOISE_INDEX_PRIMARY;
        float growTime = 0.12F + random.nextFloat() * 0.08F;
        float holdTime = 0.08F + random.nextFloat() * 0.08F;
        float fadeTime = 0.18F + random.nextFloat() * 0.12F;
        return new HeavenlyThunderBoltData(skyPos, hitPos.add(0.0D, -3.0D, 0.0D), growTime, holdTime, fadeTime,
                width, seed, style, CENTER_BOLT_JITTER_SCALE, CENTER_BOLT_TERMINAL_BOUNCE_COUNT, noiseIndex, noiseStrength);
    }

    // 提交中心大型主雷专用溅射，多次上抬提交，避免被中心地面蔓延和扩散雷圈盖住。
    public void submitCenterLargeSplash(RandomSource random, Vec3 center, TridentLightningColorStyle style, long seed) {
        int repeatCount = CENTER_SPLASH_REPEAT_MIN_COUNT + random.nextInt(CENTER_SPLASH_REPEAT_RANDOM_COUNT);
        Vec3 splashCenter = center.add(0.0D, CENTER_SPLASH_Y_OFFSET, 0.0D);
        for (int i = 0; i < repeatCount; i++) {
            TridentLightningSplashEffects.submitRandomSplash(splashCenter, style, true, seed + i * 6151L,
                    CENTER_SPLASH_MIN_COUNT, CENTER_SPLASH_RANDOM_COUNT, CENTER_SPLASH_INVERTED_V_MIN_COUNT, CENTER_SPLASH_INVERTED_V_RANDOM_COUNT,
                    CENTER_SPLASH_WIDTH_SCALE, CENTER_SPLASH_JITTER_SCALE,
                    CENTER_SPLASH_RANGE_SCALE, CENTER_SPLASH_HEIGHT_SCALE,
                    CENTER_SPLASH_GROW_TIME_SCALE, CENTER_SPLASH_HOLD_TIME_SCALE, CENTER_SPLASH_FADE_TIME_SCALE);
        }
    }

    // 提交中心主雷落地后的持续扩散雷圈，强化中心落点的法阵爆发感。
    public void submitCenterGroundPulse(RandomSource random, Vec3 center, TridentLightningColorStyle style, long seed) {
        float ringEnd = CENTER_GROUND_RING_END_RADIUS_MIN + random.nextFloat() * CENTER_GROUND_RING_END_RADIUS_RANDOM;
        float ringWidth = CENTER_GROUND_RING_WIDTH_MIN + random.nextFloat() * CENTER_GROUND_RING_WIDTH_RANDOM;
        Exe.POST.effects().addLightningRing(center.add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                CENTER_GROUND_RING_START_RADIUS, ringEnd, CENTER_GROUND_RING_GROW_TIME, CENTER_GROUND_RING_HOLD_TIME,
                CENTER_GROUND_RING_FADE_TIME, ringWidth, seed,
                style.ringCoreR, style.ringCoreG, style.ringCoreB, style.ringBloomR, style.ringBloomG, style.ringBloomB);
    }

    // 提交从法阵中心向外蔓延的多条地面雷电，作为中心大型落雷的持续配套表现。
    public void submitCenterGroundCrawl(RandomSource random, Vec3 center, TridentLightningColorStyle style, long seed) {
        int count = CENTER_CRAWL_MIN_COUNT + random.nextInt(CENTER_CRAWL_RANDOM_COUNT);
        Vec3 start = center.add(0.0D, 0.08D, 0.0D);
        for (int i = 0; i < count; i++) {
            long pathSeed = seed + i * 7919L;
            RandomSource pathRandom = RandomSource.create(pathSeed);
            double angle = pathRandom.nextDouble() * Math.PI * 2.0D;
            double radius = CENTER_CRAWL_RADIUS_MIN + pathRandom.nextDouble() * CENTER_CRAWL_RADIUS_RANDOM;
            Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 end = center.add(dir.scale(radius)).add(0.0D, 0.08D, 0.0D);
            float growTime = CENTER_CRAWL_GROW_TIME_MIN + pathRandom.nextFloat() * CENTER_CRAWL_GROW_TIME_RANDOM;
            float holdTime = CENTER_CRAWL_HOLD_TIME_MIN + pathRandom.nextFloat() * CENTER_CRAWL_HOLD_TIME_RANDOM;
            float fadeTime = CENTER_CRAWL_FADE_TIME_MIN + pathRandom.nextFloat() * CENTER_CRAWL_FADE_TIME_RANDOM;
            float width = CENTER_CRAWL_WIDTH_MIN + pathRandom.nextFloat() * CENTER_CRAWL_WIDTH_RANDOM;
            float noiseIndex = pathRandom.nextBoolean() ? CoinLightningQueue.NOISE_INDEX_ALT : CoinLightningQueue.NOISE_INDEX_PRIMARY;
            float noiseStrength = DOWN_BOLT_NOISE_STRENGTH_MIN + pathRandom.nextFloat() * DOWN_BOLT_NOISE_STRENGTH_RANDOM;
            Exe.POST.effects().addLightningPath(start, end, growTime, holdTime, fadeTime, width, pathSeed,
                    style.pathCoreR, style.pathCoreG, style.pathCoreB, style.pathBloomR, style.pathBloomG, style.pathBloomB,
                    CENTER_CRAWL_JITTER_SCALE, 0, noiseIndex, noiseStrength);
        }
    }

    // 提交一个随机地点的粗细组合落雷，共享同一颜色但拥有独立起点和参数。
    public void submitOneDownBoltCluster(RandomSource random, long seed) {
        List<HeavenlyThunderBoltData> bolts = new ArrayList<>();
        collectOneDownBoltCluster(random, seed, bolts);
        submitDownBoltBatch(bolts);
    }

    // 收集一个外围地点的粗雷和伴随细雷，交给调用方按同 tick 批次统一提交。
    public void collectOneDownBoltCluster(RandomSource random, long seed, List<HeavenlyThunderBoltData> bolts) {
        Vec3 clusterHitPos = randomDownBoltHitPos(random);
        TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, true);
        bolts.add(createDownBoltData(RandomSource.create(seed), seed, clusterHitPos, style, true));
        int thinCount = DOWN_BOLT_THIN_MIN_COUNT + random.nextInt(DOWN_BOLT_THIN_RANDOM_COUNT);
        for (int i = 0; i < thinCount; i++) {
            long boltSeed = seed + (i + 1L) * 7919L;
            RandomSource boltRandom = RandomSource.create(boltSeed);
            Vec3 hitPos = clusterHitPos.add(
                    (boltRandom.nextDouble() * 2.0D - 1.0D) * DOWN_BOLT_THIN_HIT_OFFSET,
                    0.0D,
                    (boltRandom.nextDouble() * 2.0D - 1.0D) * DOWN_BOLT_THIN_HIT_OFFSET
            );
            bolts.add(createDownBoltData(boltRandom, boltSeed, hitPos, style, false));
        }
        submitRandomGroundRing(random, clusterHitPos, style, seed);
        submitRandomLandingSplash(random, clusterHitPos, style, seed ^ 0x41F2C9A3L);
    }

    // 保留单道落雷入口，内部转为一个地点只生成一组落雷，方便旧调用语义兼容。
    public void submitOneDownBolt(RandomSource random, long seed) {
        Vec3 hitPos = randomDownBoltHitPos(random);
        TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, true);
        submitDownBoltBatch(List.of(createDownBoltData(random, seed, hitPos, style, true)));
        submitRandomGroundRing(random, hitPos, style, seed);
    }

    // 在法阵圆形视觉范围内生成随机落雷命中点，避免方形角落超出法阵半径。
    public Vec3 randomDownBoltHitPos(RandomSource random) {
        Vec3 center = this.position();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Math.sqrt(random.nextDouble()) * DOWN_BOLT_VISUAL_RANGE;
        double hitX = center.x + Math.cos(angle) * radius;
        double hitZ = center.z + Math.sin(angle) * radius;
        return new Vec3(hitX, center.y, hitZ);
    }

    // 按指定命中点提交一道从天空法阵范围落下的雷电。
    public void submitOneDownBoltAt(RandomSource random, long seed, Vec3 hitPos, TridentLightningColorStyle style) {
        submitOneDownBoltAt(random, seed, hitPos, style, true);
    }

    // 按粗细类型提交一道从天空法阵范围落下的雷电。
    public void submitOneDownBoltAt(RandomSource random, long seed, Vec3 hitPos, TridentLightningColorStyle style, boolean thick) {
        submitDownBoltBatch(List.of(createDownBoltData(random, seed, hitPos, style, thick)));
    }

    // 创建单道外围下落雷数据，保留原有 growTime、宽度、噪声和回弹参数。
    public HeavenlyThunderBoltData createDownBoltData(RandomSource random, long seed, Vec3 hitPos,
                                                      TridentLightningColorStyle style, boolean thick) {
        double skyOffset = thick ? DOWN_BOLT_CLUSTER_SKY_OFFSET : DOWN_BOLT_THIN_SKY_OFFSET;
        Vec3 skyPos = hitPos.add((random.nextDouble() - 0.5D) * skyOffset, UP_RANGE, (random.nextDouble() - 0.5D) * skyOffset);
        float growTime = 0.055F + random.nextFloat() * 0.105F;
        float holdTime = 0.035F + random.nextFloat() * 0.105F;
        float fadeTime = 0.10F + random.nextFloat() * 0.16F;
        float width = thick ? DOWN_BOLT_THICK_WIDTH_MIN + random.nextFloat() * DOWN_BOLT_THICK_WIDTH_RANDOM
                : DOWN_BOLT_THIN_WIDTH_MIN + random.nextFloat() * DOWN_BOLT_THIN_WIDTH_RANDOM;
        int terminalBounceCount = thick && random.nextFloat() < 0.35F ? 1 + random.nextInt(2) : 0;
        float noiseIndex = random.nextBoolean() ? CoinLightningQueue.NOISE_INDEX_ALT : CoinLightningQueue.NOISE_INDEX_PRIMARY;
        float noiseStrength = DOWN_BOLT_NOISE_STRENGTH_MIN + random.nextFloat() * DOWN_BOLT_NOISE_STRENGTH_RANDOM;
        float jitterBase = thick ? 0.62F : 0.88F;
        float jitterRandom = thick ? 0.28F : 0.50F;
        float jitterScale = LIGHTNING_JITTER_SCALE * (jitterBase + random.nextFloat() * jitterRandom);
        return new HeavenlyThunderBoltData(skyPos, hitPos.add(0.0D, -3.0D, 0.0D), growTime, holdTime, fadeTime,
                width, seed, style, jitterScale, terminalBounceCount, noiseIndex, noiseStrength);
    }

    // 按本批最大 growTime 反算开始延迟，让不同速度的落雷在同一时刻到地。
    public void submitDownBoltBatch(List<HeavenlyThunderBoltData> bolts) {
        if (bolts == null || bolts.isEmpty()) return;
        float batchGrowTime = 0.0F;
        for (HeavenlyThunderBoltData bolt : bolts) {
            batchGrowTime = Math.max(batchGrowTime, bolt.growTime);
        }
        for (HeavenlyThunderBoltData bolt : bolts) {
            bolt.submit(Math.max(0.0F, batchGrowTime - bolt.growTime));
        }
    }

    // 随机在下落雷命中点追加一次地面扩散雷电，只作为视觉表现，不额外造成伤害。
    public void submitRandomGroundRing(RandomSource random, Vec3 hitPos, TridentLightningColorStyle style, long seed) {
        if (random.nextFloat() >= GROUND_RING_CHANCE) return;
        float ringStart = 0.15F + random.nextFloat() * 0.30F;
        float ringEnd = 3.5F + random.nextFloat() * 5.5F;
        float ringGrow = 0.12F + random.nextFloat() * 0.12F;
        float ringHold = 0.02F + random.nextFloat() * 0.04F;
        float ringFade = 0.14F + random.nextFloat() * 0.12F;
        float ringWidth = 0.55F + random.nextFloat() * 0.70F;
        Exe.POST.effects().addLightningRing(hitPos.add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                ringStart, ringEnd, ringGrow, ringHold, ringFade, ringWidth, seed ^ 0x6D1B5A7L,
                style.ringCoreR, style.ringCoreG, style.ringCoreB, style.ringBloomR, style.ringBloomG, style.ringBloomB);
    }

    // 随机给法阵落雷地点追加一次同色的普通外扩或向外倒 V 溅射雷，只做客户端视觉。
    public void submitRandomLandingSplash(RandomSource random, Vec3 hitPos, TridentLightningColorStyle style, long seed) {
        if (random.nextFloat() >= HEAVENLY_SPLASH_CHANCE) return;
        TridentLightningSplashEffects.submitRandomSplash(hitPos, style, true, seed,
                TridentLightningSplashEffects.RANDOM_SPLASH_MIN_COUNT, TridentLightningSplashEffects.RANDOM_SPLASH_RANDOM_COUNT,
                TridentLightningSplashEffects.RANDOM_SPLASH_WIDTH_SCALE, TridentLightningSplashEffects.RANDOM_SPLASH_JITTER_SCALE);
    }

    // 对方形范围内非 owner 且不在白名单内的生物造成伤害。
    public void applyAreaDamage(float damage) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (damage <= 0.0F) return;
        Vec3 center = this.position();
        AABB area = new AABB(
                center.x - HORIZONTAL_RANGE, center.y - DOWN_RANGE, center.z - HORIZONTAL_RANGE,
                center.x + HORIZONTAL_RANGE, center.y + UP_RANGE, center.z + HORIZONTAL_RANGE
        );
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canDamageTarget);
        for (LivingEntity target : targets) {
            target.hurt(getDamageSource(), damage);
        }
    }

    // 判断目标是否允许受到天雷技能伤害，玩家规则统一交给白名单配置处理。
    public boolean canDamageTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 构造尽量贴近三叉戟归属的伤害源。
    public DamageSource getDamageSource() {
        Entity owner = getOwnerEntity();
        return this.damageSources().trident(this, owner == null ? this : owner);
    }

    @Nullable
    public Entity getOwnerEntity() {
        if (this.ownerUUID == null) return null;
        if (this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(this.ownerUUID);
        }
        return null;
    }

    // 持续播放和战戟落点雷电一致的雷声音效。
    public void tickServerSound() {
        if (this.tickCount == CAST_TICKS) {
            playThunderSound(2.0F, 0.78F);
            playImpactSound(1.25F, 0.9F);
            return;
        }
        if (this.tickCount > CAST_TICKS && this.tickCount % 12 == 1) {
            playThunderSound(1.35F, 0.82F + this.random.nextFloat() * 0.18F);
        }
    }

    // 播放原版闪电雷声音效。
    public void playThunderSound(float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, volume, pitch);
    }

    // 播放原版闪电冲击音效。
    public void playImpactSound(float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, volume, pitch);
    }

    public Vec3 skyCircleCenter() {
        return this.position().add(0.0D, UP_RANGE, 0.0D);
    }

    public int getSyncedVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    public long getVisualSeed(int index, long salt) {
        return this.getId() * 73471L + this.tickCount * 9973L + index * 7919L + this.getSyncedVisualSeed() + salt;
    }

    // HeavenlyThunderBoltData 保存天雷附魔技能单道下落雷的完整提交参数。
    public static class HeavenlyThunderBoltData {
        public final Vec3 start; // 闪电天空起点。
        public final Vec3 end; // 闪电地面终点。
        public final float growTime; // 单道闪电原有下落显现时间。
        public final float holdTime; // 到地后的保持时间。
        public final float fadeTime; // 保持结束后的淡出时间。
        public final float width; // 闪电条带半宽。
        public final long seed; // 闪电稳定随机种子。
        public final TridentLightningColorStyle style; // 闪电颜色样式。
        public final float jitterScale; // 路径几何抖动倍率。
        public final int terminalBounceCount; // 末端回弹分支数量。
        public final float noiseIndex; // 闪电噪声图索引。
        public final float noiseStrength; // 闪电噪声扰动强度。

        public HeavenlyThunderBoltData(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                       float width, long seed, TridentLightningColorStyle style, float jitterScale,
                                       int terminalBounceCount, float noiseIndex, float noiseStrength) {
            this.start = start;
            this.end = end;
            this.growTime = growTime;
            this.holdTime = holdTime;
            this.fadeTime = fadeTime;
            this.width = width;
            this.seed = seed;
            this.style = style;
            this.jitterScale = jitterScale;
            this.terminalBounceCount = terminalBounceCount;
            this.noiseIndex = noiseIndex;
            this.noiseStrength = noiseStrength;
        }

        // 把本条落雷连同批次计算出的延迟提交到公共闪电队列。
        public void submit(float startDelay) {
            if (Exe.POST == null || style == null) return;
            Exe.POST.effects().addLightningPath(start, end, growTime, holdTime, fadeTime, width, seed,
                    style.pathCoreR, style.pathCoreG, style.pathCoreB,
                    style.pathBloomR, style.pathBloomG, style.pathBloomB,
                    jitterScale, terminalBounceCount, noiseIndex, noiseStrength, startDelay);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.entityData.set(VISUAL_SEED, tag.getInt("VisualSeed"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("VisualSeed", this.getSyncedVisualSeed());
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        Vec3 center = this.position();
        return new AABB(
                center.x - HORIZONTAL_RANGE, center.y - DOWN_RANGE, center.z - HORIZONTAL_RANGE,
                center.x + HORIZONTAL_RANGE, center.y + UP_RANGE + 8.0D, center.z + HORIZONTAL_RANGE
        );
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
