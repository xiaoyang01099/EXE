package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.util.Optional;


public class SpectriteCrystalEntity extends EndCrystal {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET;
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM;
    public int time;
    private Player player;
    private TilePool pool;
    public int frame;

    public SpectriteCrystalEntity(EntityType<? extends SpectriteCrystalEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
        this.frame = 360;
    }

    public SpectriteCrystalEntity(Level pLevel, double pX, double pY, double pZ) {
        this(ModEntities.SPECTRITE_CRYSTAL.get(), pLevel);
        this.setPos(pX, pY, pZ);
    }

    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
        this.getEntityData().define(DATA_SHOW_BOTTOM, true);
    }

    public void tick() {
        ++this.time;
        ++this.frame;
        if (this.level instanceof ServerLevel) {
            BlockPos pos = this.blockPosition();
            if (((ServerLevel)this.level).dragonFight() != null && this.level.getBlockState(pos).isAir()) {
                this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
            }
            int range = 16;  //范围
            int height = 10; //高度
            boolean flag = false;
            AABB aabb = new AABB(pos.offset(-range, 0, -range), pos.offset(range, height, range));
            for (Player player : level.getEntitiesOfClass(Player.class, aabb)) {
                if (pool != null && pool.getCurrentMana() > 1024 && player.isAlive() && player.getHealth() < player.getMaxHealth()) {
                    this.setBeamTarget(player.getOnPos());
                    this.player = player;
                } else this.setBeamTarget(null);
            }
            for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-range, 0, -range), pos.offset(range, height, range))) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof TilePool tilePool){
                    this.pool = tilePool;
                }
            }
            if (player == null || pool == null){
                this.setBeamTarget(null);
            }else {
                if (level.getGameTime() % 5 == 0 && pool != null) {
                    int mana = pool.getCurrentMana();
                    if (mana > 1024){
                        if (player.isAlive() && player.getHealth() < player.getMaxHealth()){
                            pool.receiveMana(-1024);
                            player.heal(1.f);
                        }
                    }else this.setBeamTarget(null);
                }
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
}