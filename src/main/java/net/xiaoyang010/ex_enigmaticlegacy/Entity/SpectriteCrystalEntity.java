package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;


public class SpectriteCrystalEntity extends EndCrystal {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET;
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM;
    public int time;
    private Player player;

    public SpectriteCrystalEntity(EntityType<? extends SpectriteCrystalEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public SpectriteCrystalEntity(Level pLevel, double pX, double pY, double pZ) {
        this(ModEntities.SPECTRITE_CRYSTAL.get(), pLevel);
        this.setPos(pX, pY, pZ);
    }

    protected Entity.MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
        this.getEntityData().define(DATA_SHOW_BOTTOM, true);
    }

    public void tick() {
        ++this.time;
        if (this.level instanceof ServerLevel) {
            BlockPos pos = this.blockPosition();
            if (((ServerLevel)this.level).dragonFight() != null && this.level.getBlockState(pos).isAir()) {
                this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
            }
            int range = 16;  //范围
            int height = 10; //高度
            AABB aabb = new AABB(pos.offset(-range, 0, -range), pos.offset(range, height, range));
            for (Player player : level.getEntitiesOfClass(Player.class, aabb)) {
                if (player.isAlive() && player.getHealth() < player.getMaxHealth()) {
                    this.setBeamTarget(player.getOnPos());
                    this.player = player;
                } else this.setBeamTarget(null);
            }
            if (player == null){
                this.setBeamTarget(null);
            }else {
                if (level.getGameTime() % 5 == 0)
                    player.heal(0.25f);
                double sqrt = Math.sqrt(player.getOnPos().distToCenterSqr(pos.getX(), pos.getY(), pos.getZ()));
                if (sqrt > 16.0d)
                    this.setBeamTarget(null);
            }

        }
        spawnParticles();
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if (this.getBeamTarget() != null) {
            pCompound.put("BeamTarget", NbtUtils.writeBlockPos(this.getBeamTarget()));
        }

        pCompound.putBoolean("ShowBottom", this.showsBottom());
    }

    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("BeamTarget", 10)) {
            this.setBeamTarget(NbtUtils.readBlockPos(pCompound.getCompound("BeamTarget")));
        }

        if (pCompound.contains("ShowBottom", 1)) {
            this.setShowBottom(pCompound.getBoolean("ShowBottom"));
        }

    }

    public boolean isPickable() {
        return true;
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            if (!this.isRemoved() && !this.level.isClientSide) {
                this.remove(RemovalReason.KILLED);
                if (!pSource.isExplosion()) {
                    this.level.explode((Entity)null, this.getX(), this.getY(), this.getZ(), 6.0F, BlockInteraction.DESTROY);
                }
            }

            return true;
        }
    }

    public void kill() {
        super.kill();
    }

    public void setBeamTarget(@Nullable BlockPos pBeamTarget) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(pBeamTarget));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return (BlockPos)((Optional)this.getEntityData().get(DATA_BEAM_TARGET)).orElse(null);
    }

    public void setShowBottom(boolean pShowBottom) {
        this.getEntityData().set(DATA_SHOW_BOTTOM, pShowBottom);
    }

    public boolean showsBottom() {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return super.shouldRenderAtSqrDistance(pDistance) || this.getBeamTarget() != null;
    }

    public ItemStack getPickResult() {
        return new ItemStack(ModItems.SPECTRITE_CRYSTAL.get());
    }

    public @NotNull Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    static {
        DATA_BEAM_TARGET = SynchedEntityData.defineId(SpectriteCrystalEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
        DATA_SHOW_BOTTOM = SynchedEntityData.defineId(SpectriteCrystalEntity.class, EntityDataSerializers.BOOLEAN);
    }

    // 修改颜色定义部分：
    private static final DustColorTransitionOptions RED_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(1.0F, 0.33F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions ORANGE_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.5F, 0.0F), new Vector3f(1.0F, 0.67F, 0.0F), 1.0F);
    private static final DustColorTransitionOptions YELLOW_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions GREEN_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.33F, 1.0F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions BLUE_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.33F, 0.33F, 1.0F), 1.0F);
    private static final DustColorTransitionOptions INDIGO_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.29F, 0.0F, 0.51F), new Vector3f(0.48F, 0.17F, 0.75F), 1.0F);
    private static final DustColorTransitionOptions VIOLET_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.58F, 0.0F, 0.83F), new Vector3f(0.67F, 0.33F, 1.0F), 1.0F);


    private float particleTime = 0.0F;

    private void spawnParticles() {
        // 更新粒子时间
        particleTime += 0.1F;

        // 创建螺旋形彩虹粒子效果
        double radius = 1.0;
        for (int i = 0; i < 2; i++) {
            double angle = particleTime + (Math.PI * 2 * i / 2);

            double x = this.getX() + Math.cos(angle) * radius;
            double y = this.getY() + Math.sin(particleTime * 0.5) * 0.5 + 0.5;
            double z = this.getZ() + Math.sin(angle) * radius;

            // 随机选择彩虹颜色过渡效果
            DustColorTransitionOptions particleOptions = switch (this.random.nextInt(7)) {
                case 0 -> RED_PARTICLE;
                case 1 -> ORANGE_PARTICLE;
                case 2 -> YELLOW_PARTICLE;
                case 3 -> GREEN_PARTICLE;
                case 4 -> BLUE_PARTICLE;
                case 5 -> INDIGO_PARTICLE;
                default -> VIOLET_PARTICLE;
            };

            // 生成颜色过渡粒子
            this.level.addParticle(particleOptions, x, y, z, 0, 0, 0);


            // 添加一些星星般的闪光效果
            if (this.random.nextInt(20) == 0) {
                this.level.addParticle(ParticleTypes.END_ROD,
                        x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }


    /*private static final double CRYSTAL_WIDTH = 2.0D;
    private static final double CRYSTAL_HEIGHT = 2.0D;
    private BlockPos beamTarget;
    private int age = 0;
    private float particleTime = 0.0F;
    private static final int HEALING_RANGE = 20;
    public Entity healingTarget;

    // 修改颜色定义部分：
    private static final DustColorTransitionOptions RED_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(1.0F, 0.33F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions ORANGE_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.5F, 0.0F), new Vector3f(1.0F, 0.67F, 0.0F), 1.0F);
    private static final DustColorTransitionOptions YELLOW_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions GREEN_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.33F, 1.0F, 0.33F), 1.0F);
    private static final DustColorTransitionOptions BLUE_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.33F, 0.33F, 1.0F), 1.0F);
    private static final DustColorTransitionOptions INDIGO_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.29F, 0.0F, 0.51F), new Vector3f(0.48F, 0.17F, 0.75F), 1.0F);
    private static final DustColorTransitionOptions VIOLET_PARTICLE = new DustColorTransitionOptions(
            new Vector3f(0.58F, 0.0F, 0.83F), new Vector3f(0.67F, 0.33F, 1.0F), 1.0F);

    // 添加彩虹颜色数组
    private static final DustColorTransitionOptions[] RAINBOW_COLORS = {
            RED_PARTICLE,
            ORANGE_PARTICLE,
            YELLOW_PARTICLE,
            GREEN_PARTICLE,
            BLUE_PARTICLE,
            INDIGO_PARTICLE,
            VIOLET_PARTICLE
    };

    public SpectriteCrystalEntity(EntityType<? extends SpectriteCrystalEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        if (this.level.isClientSide) {
            this.spawnParticles();
            // 如果有治疗目标，显示光束
            if (this.healingTarget != null) {
                spawnHealingBeam();
            }
        } else {
            // 服务器端逻辑
            findAndHealPlayer();
        }
    }

    private void findAndHealPlayer() {
        if (this.healingTarget instanceof Player player &&
                (player.getHealth() >= player.getMaxHealth() ||
                        player.distanceToSqr(this) > HEALING_RANGE * HEALING_RANGE)) {
            this.healingTarget = null;
            return;
        }

        if (this.healingTarget == null && this.age % 20 == 0) {
            // 每秒检查一次附近的玩家
            List<Player> nearbyPlayers = this.level.getEntitiesOfClass(
                    Player.class,
                    new AABB(this.getX() - HEALING_RANGE, this.getY() - HEALING_RANGE, this.getZ() - HEALING_RANGE,
                            this.getX() + HEALING_RANGE, this.getY() + HEALING_RANGE, this.getZ() + HEALING_RANGE)
            );

            for (Player player : nearbyPlayers) {
                if (player.getHealth() < player.getMaxHealth()) {
                    this.healingTarget = player;
                    break;
                }
            }
        }

        // 治疗目标玩家
        if (this.healingTarget instanceof Player player && this.age % 20 == 0) {
            player.heal(1.0F); // 每秒恢复0.5颗心
        }
    }

    private void spawnHealingBeam() {
        if (this.healingTarget == null) return;

        double deltaX = this.healingTarget.getX() - this.getX();
        double deltaY = (this.healingTarget.getY() + this.healingTarget.getBbHeight() / 2) - this.getY();
        double deltaZ = this.healingTarget.getZ() - this.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        // 创建彩虹色光束效果
        for (int i = 1; i < 32; i++) {
            float progress = i / 32.0F;
            double x = this.getX() + deltaX * progress;
            double y = this.getY() + deltaY * progress;
            double z = this.getZ() + deltaZ * progress;

            // 主光束效果 - 使用彩虹色粒子
            DustColorTransitionOptions particleOptions = RAINBOW_COLORS[(age + i) % RAINBOW_COLORS.length];
            for (int j = 0; j < 2; j++) {
                double spreadX = (this.random.nextDouble() - 0.5) * 0.3;
                double spreadY = (this.random.nextDouble() - 0.5) * 0.3;
                double spreadZ = (this.random.nextDouble() - 0.5) * 0.3;
                this.level.addParticle(particleOptions,
                        x + spreadX, y + spreadY, z + spreadZ,
                        0, 0, 0);
            }

            if (this.random.nextInt(8) == 0) {
                this.level.addParticle(ParticleTypes.DRAGON_BREATH,
                        x + (this.random.nextDouble() - 0.5) * 0.2,
                        y + (this.random.nextDouble() - 0.5) * 0.2,
                        z + (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.01,
                        (this.random.nextDouble() - 0.5) * 0.01,
                        (this.random.nextDouble() - 0.5) * 0.01);
            }

            // 添加额外的闪光效果
            if (this.random.nextInt(12) == 0) {
                this.level.addParticle(ParticleTypes.END_ROD,
                        x + (this.random.nextDouble() - 0.5) * 0.2,
                        y + (this.random.nextDouble() - 0.5) * 0.2,
                        z + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }

        // 在开始和结束点添加特效
        for (int i = 0; i < 3; i++) {
            // 起点效果
            DustColorTransitionOptions startColor = RAINBOW_COLORS[age % RAINBOW_COLORS.length];
            this.level.addParticle(startColor,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + 0.5 + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);

            // 终点效果
            if (this.healingTarget != null) {
                DustColorTransitionOptions endColor = RAINBOW_COLORS[(age + 3) % RAINBOW_COLORS.length];
                this.level.addParticle(endColor,
                        this.healingTarget.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.healingTarget.getY() + this.healingTarget.getBbHeight() / 2,
                        this.healingTarget.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide) {
            explodeNonDestructive();
            this.remove(Entity.RemovalReason.KILLED);
        }
        return true;
    }

    private void explodeNonDestructive() {
        // 播放爆炸音效
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);

        // 生成爆炸特效但不破坏方块
        this.level.explode(this, this.getX(), this.getY(), this.getZ(), 3.0F, false,
                Explosion.BlockInteraction.NONE);
    }


    private void spawnParticles() {
        // 更新粒子时间
        particleTime += 0.1F;

        // 创建螺旋形彩虹粒子效果
        double radius = 1.0;
        for (int i = 0; i < 2; i++) {
            double angle = particleTime + (Math.PI * 2 * i / 2);

            double x = this.getX() + Math.cos(angle) * radius;
            double y = this.getY() + Math.sin(particleTime * 0.5) * 0.5 + 0.5;
            double z = this.getZ() + Math.sin(angle) * radius;

            // 随机选择彩虹颜色过渡效果
            DustColorTransitionOptions particleOptions;
            switch (this.random.nextInt(7)) {
                case 0:
                    particleOptions = RED_PARTICLE;
                    break;
                case 1:
                    particleOptions = ORANGE_PARTICLE;
                    break;
                case 2:
                    particleOptions = YELLOW_PARTICLE;
                    break;
                case 3:
                    particleOptions = GREEN_PARTICLE;
                    break;
                case 4:
                    particleOptions = BLUE_PARTICLE;
                    break;
                case 5:
                    particleOptions = INDIGO_PARTICLE;
                    break;
                default:
                    particleOptions = VIOLET_PARTICLE;
                    break;
            }

            // 生成颜色过渡粒子
            this.level.addParticle(particleOptions, x, y, z, 0, 0, 0);


            // 添加一些星星般的闪光效果
            if (this.random.nextInt(20) == 0) {
                this.level.addParticle(ParticleTypes.END_ROD,
                        x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("BeamTargetX")) {
            int x = compound.getInt("BeamTargetX");
            int y = compound.getInt("BeamTargetY");
            int z = compound.getInt("BeamTargetZ");
            this.beamTarget = new BlockPos(x, y, z);
        }
        if (compound.contains("Age")) {
            this.age = compound.getInt("Age");
        }
        if (compound.contains("HealingTargetUUID")) {
            UUID uuid = UUID.fromString(compound.getString("HealingTargetUUID"));
            if (this.level != null) {
                for (Entity entity : this.level.players()) {
                    if (entity.getUUID().equals(uuid)) {
                        this.healingTarget = entity;
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.beamTarget != null) {
            compound.putInt("BeamTargetX", this.beamTarget.getX());
            compound.putInt("BeamTargetY", this.beamTarget.getY());
            compound.putInt("BeamTargetZ", this.beamTarget.getZ());
        }
        compound.putInt("Age", this.age);
        if (this.healingTarget != null) {
            compound.putString("HealingTargetUUID", this.healingTarget.getStringUUID());
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        this.setBoundingBox(new AABB(
                x - CRYSTAL_WIDTH / 2.0D,
                y - 0.0D,
                z - CRYSTAL_WIDTH / 2.0D,
                x + CRYSTAL_WIDTH / 2.0D,
                y + CRYSTAL_HEIGHT,
                z + CRYSTAL_WIDTH / 2.0D
        ));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public BlockPos getBeamTarget() {
        return this.beamTarget;
    }

    public void setBeamTarget(BlockPos target) {
        this.beamTarget = target;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }
        d0 *= 64.0D;
        return distance < d0 * d0;
    }*/
}