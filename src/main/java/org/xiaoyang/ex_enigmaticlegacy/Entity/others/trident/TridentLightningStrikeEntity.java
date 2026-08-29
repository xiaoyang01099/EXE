package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// TridentLightningStrikeEntity 是天雷战戟落点雷电实体，负责范围伤害和客户端闪电视觉。
public class TridentLightningStrikeEntity extends Entity {
    private static final EntityDataAccessor<Boolean> ENHANCED = SynchedEntityData.defineId(TridentLightningStrikeEntity.class, EntityDataSerializers.BOOLEAN); // 是否引雷强化。
    private static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(TridentLightningStrikeEntity.class, EntityDataSerializers.INT); // 视觉随机种子。
    private static final float DOWN_BOLT_PATH_JITTER_SCALE = 0.35F; // 下落雷路径几何抖动倍率，降低 XZ 跳动保证 Bloom 连续。
    private static final double VERTICAL_SHOCKWAVE_Y_OFFSET = 0.15D; // 竖向冲击波中心上移，避免贴地深度冲突。
    private static final float VERTICAL_SHOCKWAVE_START_RADIUS = 0.12F; // 竖向冲击波初始半径。
    private static final float VERTICAL_SHOCKWAVE_NORMAL_END_RADIUS = 4.0F; // 普通落雷竖向冲击波结束半径，范围调大。
    private static final float VERTICAL_SHOCKWAVE_ENHANCED_END_RADIUS = 5.8F; // 强化落雷竖向冲击波结束半径，范围调大。
    private static final float VERTICAL_SHOCKWAVE_GROW_TIME = 0.10F; // 竖向冲击波扩散时间，数值越小扩散越快。
    private static final float VERTICAL_SHOCKWAVE_NORMAL_HOLD_TIME = 0.01F; // 普通落雷竖向冲击波保持时间，减少亮度停留。
    private static final float VERTICAL_SHOCKWAVE_ENHANCED_HOLD_TIME = 0.02F; // 强化落雷竖向冲击波保持时间，减少亮度停留。
    private static final float VERTICAL_SHOCKWAVE_FADE_TIME = 0.05F; // 竖向冲击波淡出时间，数值越小淡出越快。
    private static final float VERTICAL_SHOCKWAVE_NORMAL_ALPHA = 0.05F; // 普通落雷竖向冲击波透明度，压低颜色亮度。
    private static final float VERTICAL_SHOCKWAVE_ENHANCED_ALPHA = 0.08F; // 强化落雷竖向冲击波透明度，压低颜色亮度。
    @Nullable
    private UUID ownerUUID; // 释放者 UUID。
    private boolean normalDamageApplied; // 普通模式是否已经造成伤害。
    @Nullable
    private BlockPos lightBlockPos; // 强化落雷实体创建的 Light Block 位置。
    private boolean lightBlockPlaced; // 是否已经成功创建 Light Block。

    public TridentLightningStrikeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    // 服务端初始化落点、释放者、强化状态和视觉 seed。
    public void setStrikeData(Vec3 position, @Nullable Entity owner, boolean enhanced, int visualSeed) {
        this.setPos(position.x, position.y, position.z);
        this.ownerUUID = owner == null ? null : owner.getUUID();
        this.entityData.set(ENHANCED, enhanced);
        this.entityData.set(VISUAL_SEED, visualSeed);
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(ENHANCED, false);
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

    // 客户端根据同步实体提交本地闪电视觉，多人各客户端都会执行。
    public void tickClientVisuals() {
        if (Exe.POST == null) return;
        if (isEnhanced()) {
            tickClientEnhancedVisuals();
        } else {
            tickClientNormalVisuals();
        }
    }

    // 普通模式：初始向上爆发粒子，两批蓝色落雷，前几个 tick 提交更密的地面雷圈。
    public void tickClientNormalVisuals() {
        if (this.tickCount == 1) {
            TridentLightningParticleEffects.emitLandingBurst(this.position(), false, getVisualSeed(0, 0xB451L));
        }
        if (this.tickCount == 1 || this.tickCount == 4) {
            submitDownBolts(nextDownBoltCount(false, this.tickCount), false);
        }
        if (this.tickCount <= 10 && this.tickCount % 4 == 1) {
            submitGroundRing(false, this.tickCount);
        }
    }

    // 引雷强化：持续生成蓝多红少的粗落雷、更密雷圈和多次向上爆发粒子。
    public void tickClientEnhancedVisuals() {
        if (this.tickCount == 1) {
            TridentLightningParticleEffects.emitLandingBurst(this.position(), true, getVisualSeed(0, 0xB451L));
        } else if (this.tickCount % 3 == 1) {
            TridentLightningParticleEffects.emitSmallStormBurst(this.position(), getVisualSeed(this.tickCount, 0x51A7L));
        }
        if (this.tickCount % 4 == 1) {
            int count = nextDownBoltCount(true, this.tickCount);
            submitDownBolts(count, true);
        }
        if (this.tickCount % 4 == 1) {
            submitGroundRing(true, this.tickCount);
        }
    }

    // 服务端按模式处理伤害和生命周期。
    public void tickServerDamage() {
        tickServerLightBlock();
        tickServerSound();

        if (isEnhanced()) {
            int interval = Math.max(1, TridentPlusConfig.enhancedDamageInterval());
            if (this.tickCount > 0 && this.tickCount % interval == 0) {
                applyAreaDamage((float) (TridentPlusConfig.splashDamage() * 0.55F), TridentPlusConfig.splashRadius() * 1.5D);
            }
            if (this.tickCount > TridentPlusConfig.enhancedDurationTicks()) {
                this.discard();
            }
            return;
        }

        if (!this.normalDamageApplied && this.tickCount == 2) {
            applyAreaDamage(TridentPlusConfig.splashDamage(), TridentPlusConfig.splashRadius());
            this.normalDamageApplied = true;
        }
        if (this.tickCount > 12) {
            this.discard();
        }
    }

    // 为引雷强化落雷创建真实 Light Block，正常情况会在第一服务端 tick 完成。
    public void tickServerLightBlock() {
        if (!isEnhanced()) return;
        if (this.lightBlockPlaced && this.lightBlockPos != null
                && this.level().hasChunkAt(this.lightBlockPos)
                && this.level().getBlockState(this.lightBlockPos).is(Blocks.LIGHT)) {
            return;
        }
        this.lightBlockPos = null;
        this.lightBlockPlaced = false;
        tryPlaceLightBlock();
    }

    // 在落雷中心附近查找空气并放置最高亮度 Light Block。
    public boolean tryPlaceLightBlock() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return false;
        BlockPos targetPos = findLightBlockPosition();
        if (targetPos == null) return false;

        BlockState lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
        boolean placed = serverLevel.setBlock(targetPos, lightState, Block.UPDATE_ALL);
        if (!placed) return false;

        this.lightBlockPos = targetPos.immutable();
        this.lightBlockPlaced = true;
        return true;
    }

    // 按落雷上方、中心、水平邻位和三乘三范围的顺序寻找最近空气位置。
    @Nullable
    public BlockPos findLightBlockPosition() {
        BlockPos origin = this.blockPosition();
        BlockPos[] primaryCandidates = {
                origin.above(),
                origin,
                origin.above(2),
                origin.north(),
                origin.south(),
                origin.west(),
                origin.east(),
                origin.below()
        };
        for (BlockPos candidate : primaryCandidates) {
            if (canPlaceLightBlockAt(candidate)) return candidate;
        }

        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos candidate = origin.offset(offsetX, offsetY, offsetZ);
                    if (!canPlaceLightBlockAt(candidate)) continue;
                    double distance = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                    if (distance >= nearestDistance) continue;
                    nearest = candidate;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    // 只允许在已加载区块内的空气位置放置 Light Block，避免覆盖正常方块。
    public boolean canPlaceLightBlockAt(BlockPos pos) {
        if (!this.level().isInWorldBounds(pos)) return false;
        if (!this.level().hasChunkAt(pos)) return false;
        return this.level().getBlockState(pos).isAir();
    }

    // 删除当前落雷实体创建的 Light Block，方块已被替换时不执行覆盖。
    public void clearLightBlock() {
        if (this.level().isClientSide()) return;
        if (!this.lightBlockPlaced || this.lightBlockPos == null) return;
        if (this.level().hasChunkAt(this.lightBlockPos)) {
            BlockState state = this.level().getBlockState(this.lightBlockPos);
            if (state.is(Blocks.LIGHT)) {
                this.level().removeBlock(this.lightBlockPos, false);
            }
        }
        this.lightBlockPos = null;
        this.lightBlockPlaced = false;
    }

    // 落雷实体因持续时间结束、区块卸载或其他原因移除时同步清理真实光源。
    @Override
    public void remove(RemovalReason reason) {
        clearLightBlock();
        super.remove(reason);
    }

    // 服务端播放雷声，普通模式一次，引雷模式持续多次。
    public void tickServerSound() {
        if (this.tickCount == 1) {
            playThunderSound(isEnhanced() ? 2.0F : 1.35F, isEnhanced() ? 0.78F : 0.95F);
            if (isEnhanced()) {
                playImpactSound(1.25F, 0.9F);
            }
            return;
        }
        if (isEnhanced() && this.tickCount % 12 == 1) {
            playThunderSound(1.35F, 0.82F + this.random.nextFloat() * 0.18F);
        }
    }

    // 播放原版闪电雷声音效。
    public void playThunderSound(float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, volume, pitch);
    }

    // 播放原版闪电落点冲击音效。
    public void playImpactSound(float volume, float pitch) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, volume, pitch);
    }
    // 对落点范围内允许受伤的生物造成伤害。
    public void applyAreaDamage(float damage, double radius) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (damage <= 0.0F || radius <= 0.0D) return;

        Vec3 center = this.position();
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canDamageTarget);
        for (LivingEntity target : targets) {
            target.hurt(getDamageSource(), damage);
        }
    }

    // 判断目标是否允许受到天雷战戟落点伤害。
    public boolean canDamageTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) return false;
        if (target instanceof Player && !ConfigFile.damagePlayers()) return false;
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

    // 随机生成单次落雷数量，让每次只出现少量清晰落雷。
    public int nextDownBoltCount(boolean enhanced, int salt) {
        RandomSource random = RandomSource.create(getVisualSeed(salt, 0x71C0L));
        return 1 + random.nextInt(1);
    }

    // 提交从空中劈下的路径闪电。
    public void submitDownBolts(int count, boolean enhanced) {
        double hitRange = enhanced ? 0.18D : 0.12D;
        for (int i = 0; i < count; i++) {
            long seed = getVisualSeed(i, 0x5A17L);
            RandomSource random = RandomSource.create(seed);
            Vec3 origin = this.position();
            Vec3 hitPos = new Vec3(origin.x + (random.nextDouble()) * hitRange, origin.y, origin.z + (random.nextDouble() - 0.5D) * hitRange);
            double startRange = enhanced ? 6.0D : 4.0D;
            Vec3 skyPos = hitPos.add((random.nextDouble() - 0.5D) * startRange, 40.0D, (random.nextDouble() - 0.5D) * startRange);
            TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, enhanced);
            float width = 1.0F + random.nextFloat() * (enhanced ? 0.8F : 0.18F);
            Vec3 renderHitPos = hitPos.add(0.0D, -3.0D, 0.0D);
            int terminalBounceCount = 1 + random.nextInt(2);
            float noiseIndex = random.nextBoolean() ? CoinLightningQueue.NOISE_INDEX_ALT : CoinLightningQueue.NOISE_INDEX_PRIMARY;
            float noiseStrength = CoinLightningQueue.DEFAULT_NOISE_STRENGTH;
            submitLightningPath(skyPos, renderHitPos, seed, style, enhanced, width, terminalBounceCount, noiseIndex, noiseStrength);
            TridentLightningSplashEffects.submitRandomSplash(hitPos, enhanced, seed ^ 0x51A5B017L);
        }
    }

    // 提交地面扩散雷电圈。
    public void submitGroundRing(boolean enhanced, int localTick) {
        long seed = getVisualSeed(localTick, 0x3F1A5C8EL);
        RandomSource random = RandomSource.create(seed);
        TridentLightningColorStyle style = TridentLightningColorStyle.pick(random, enhanced);
        float endRadius = (float) (TridentPlusConfig.splashRadius() * (enhanced ? 0.95D : 0.65D));
        if (enhanced) {
            endRadius *= 0.85F + random.nextFloat() * 0.35F;
        }

        Exe.POST.effects().addLightningRing(this.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D), 0.1F, endRadius,
                0.2F, enhanced ? 0.05F : 0.03F, 0.18F, enhanced ? 1.72F :1.10F, seed,
                style.ringCoreR, style.ringCoreG, style.ringCoreB, style.ringBloomR, style.ringBloomG, style.ringBloomB);

        submitVerticalShockwave(enhanced, seed);


    }

    // 在地面扩散雷圈同步提交一个较淡的竖向冲击波，增加落雷命中点的垂直冲击感。
    public void submitVerticalShockwave(boolean enhanced, long seed) {
        Vec3 center = this.position().add(0.0D, VERTICAL_SHOCKWAVE_Y_OFFSET, 0.0D);
        Exe.POST.effects().addShockwave(center, new Vec3(0.0D, 0.0D, 1.0D),
                VERTICAL_SHOCKWAVE_START_RADIUS,
                enhanced ? VERTICAL_SHOCKWAVE_ENHANCED_END_RADIUS : VERTICAL_SHOCKWAVE_NORMAL_END_RADIUS,
                VERTICAL_SHOCKWAVE_GROW_TIME,
                enhanced ? VERTICAL_SHOCKWAVE_ENHANCED_HOLD_TIME : VERTICAL_SHOCKWAVE_NORMAL_HOLD_TIME,
                VERTICAL_SHOCKWAVE_FADE_TIME,
                0.0F,
                seed ^ 0x5A77C1E2L,
                enhanced ? VERTICAL_SHOCKWAVE_ENHANCED_ALPHA : VERTICAL_SHOCKWAVE_NORMAL_ALPHA);
    }

    // 提交单道落雷路径，并显式携带本条落雷的噪声图和噪声强度。
    public void submitLightningPath(Vec3 skyPos, Vec3 hitPos, long seed, TridentLightningColorStyle style, boolean enhanced, float width, int terminalBounceCount, float noiseIndex, float noiseStrength) {
        Exe.POST.effects().addLightningPath(skyPos, hitPos, enhanced ? 0.25F : 0.18F, enhanced ? 0.15F : 0.1F, 0.16F,
                width, seed,
                style.pathCoreR, style.pathCoreG, style.pathCoreB, style.pathBloomR, style.pathBloomG, style.pathBloomB,
                DOWN_BOLT_PATH_JITTER_SCALE, terminalBounceCount, noiseIndex, noiseStrength);
    }

    public boolean isEnhanced() {
        return this.entityData.get(ENHANCED);
    }

    public int getSyncedVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    public long getVisualSeed(int index, long salt) {
        return this.getId() * 73471L + this.tickCount * 9973L + index * 7919L + this.getSyncedVisualSeed() + salt;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.normalDamageApplied = tag.getBoolean("NormalDamageApplied");
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.entityData.set(ENHANCED, tag.getBoolean("Enhanced"));
        this.entityData.set(VISUAL_SEED, tag.getInt("VisualSeed"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("NormalDamageApplied", this.normalDamageApplied);
        tag.putBoolean("Enhanced", this.isEnhanced());
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
        double radius = Math.max(8.0D, TridentPlusConfig.splashRadius() * 2.0D);
        Vec3 center = this.position();
        return new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 285.0D, center.z + radius);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
