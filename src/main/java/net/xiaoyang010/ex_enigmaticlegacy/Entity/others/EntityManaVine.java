package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Util.CommonHelper;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.UUID;

public class EntityManaVine extends ThrowableProjectile {
    private static final EntityDataAccessor<String> ATTACKER = SynchedEntityData.defineId(EntityManaVine.class, EntityDataSerializers.STRING);
    private static final int MAX_LIFETIME = 240;

    public EntityManaVine(EntityType<? extends EntityManaVine> entityType, Level level) {
        super(entityType, level);
    }

    public EntityManaVine(Level level, LivingEntity shooter) {
        super(ModEntities.MANA_VINE_BALL.get(), shooter, level);
        if (shooter instanceof Player player) {
            setAttacker(player.getStringUUID());
        }
        this.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 1.5F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ATTACKER, "");
    }

    public String getAttacker() {
        return this.entityData.get(ATTACKER);
    }

    public void setAttacker(String uuid) {
        this.entityData.set(ATTACKER, uuid);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (this.level.isClientSide) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        float spread = 6.0F;
        float motion = 0.02F;

        for (int i = 0; i < 4; i++) {
            double posX = this.getX() + (this.random.nextDouble() / spread - 0.5 / spread);
            double posY = this.getY() + (this.random.nextDouble() / spread - 0.5 / spread);
            double posZ = this.getZ() + (this.random.nextDouble() / spread - 0.5 / spread);

            float mx = (float)(this.random.nextDouble() - 0.5) * motion;
            float my = (float)(this.random.nextDouble() - 0.5) * motion;
            float mz = (float)(this.random.nextDouble() - 0.5) * motion;

            Color color = getCorporeaRuneColor((int)posX, (int)posY, (int)posZ, 3);
            float r = color.getRed() / 255.0F;
            float g = color.getGreen() / 255.0F;
            float b = color.getBlue() / 255.0F;

            WispParticleData data = WispParticleData.wisp(0.15F + this.random.nextFloat() * 0.12F, r, g, b, 0.7F);
            this.level.addParticle(data, posX, posY, posZ, mx, my, mz);
        }
    }


    private Color getCorporeaRuneColor(int posX, int posY, int posZ, int meta) {
        double time = this.tickCount + (this.level.getGameTime() % 20) / 20.0;
        time += (posX ^ posY ^ posZ) % 360;
        float sin = (float)(Math.sin(time / 20.0) * 0.15) - 0.15F;

        int colorValue = switch (meta) {
            case 0 -> Color.HSBtoRGB(0.0F, 0.0F, 0.54F + sin / 1.2F);
            case 1 -> Color.HSBtoRGB(0.688F, 0.93F, 0.96F + sin - 0.15F);
            case 2 -> Color.HSBtoRGB(0.983F, 0.99F, 1.0F + sin - 0.15F);
            case 3 -> Color.HSBtoRGB(0.319F, 0.92F, 0.95F + sin - 0.15F);
            case 4 -> Color.HSBtoRGB(0.536F, 0.53F, 0.92F + sin - 0.15F);
            default -> Color.HSBtoRGB(0.319F, 0.92F, 0.95F + sin - 0.15F);
        };

        return new Color(colorValue);
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) result;
            onBlockHit(blockHit);
        }
        super.onHit(result);
    }

    private void onBlockHit(BlockHitResult result) {
        if (this.level.isClientSide) return;

        Player player = null;
        String attackerUuid = getAttacker();
        if (!attackerUuid.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(attackerUuid);
                player = this.level.getPlayerByUUID(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (player == null) {
            this.discard();
            return;
        }

        BlockPos hitPos = result.getBlockPos();
        makeAnimalsLoveInArea(hitPos, player);
        growPlantsInArea(hitPos, player);
        spawnDeathParticles();

        this.discard();
    }

    private void makeAnimalsLoveInArea(BlockPos center, Player player) {
        AABB area = new AABB(center).inflate(10.0);
        List<Animal> animals = this.level.getEntitiesOfClass(Animal.class, area);

        for (Animal animal : animals) {
            animal.setInLove(null);
            if (this.level instanceof ServerLevel serverLevel) {
                serverLevel.broadcastEntityEvent(animal, (byte) 18);
            }
        }
    }

    private void growPlantsInArea(BlockPos center, Player player) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 5; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = this.level.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof BonemealableBlock growable && !(block instanceof GrassBlock)) {
                        CommonHelper.fertilizer(this.level, block, pos.getX(), pos.getY(), pos.getZ(), 12, player);

                        spawnGrowthParticles(pos);

                        this.level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS,
                                0.01F, 0.5F + this.random.nextFloat() * 0.5F);
                    }
                    else if (canGrowLiana(pos)) {
                        growLianaDown(pos, center, player);
                    }
                }
            }
        }
    }

    private boolean canGrowLiana(BlockPos pos) {

        BlockPos below = pos.below();
        BlockState belowState = this.level.getBlockState(below);

        return belowState.isSolidRender(this.level, below) && this.level.getBlockState(pos).isAir();
    }

    private void growLianaDown(BlockPos startPos, BlockPos center, Player player) {
        Vec3 startVec = Vec3.atCenterOf(startPos);
        Vec3 centerVec = Vec3.atCenterOf(center);
        int distance = (int) startVec.distanceTo(centerVec);

        BlockPos currentPos = startPos.below();

        if (this.random.nextInt(distance + 1) == 0) {
            while (currentPos.getY() > this.level.getMinBuildHeight()) {
                BlockState currentState = this.level.getBlockState(currentPos);
                if (!currentState.isAir()) {
                    break;
                }

                if (this.random.nextInt(4) < 3) {
                    // CommonHelper.setBlock(level, YourLianaBlock, 0, currentPos.getX(), currentPos.getY(), currentPos.getZ(), player, false);
                    this.level.setBlock(currentPos, Blocks.VINE.defaultBlockState(), 3);
                } else {
                    // CommonHelper.setBlock(level, YourLuminousLianaBlock, 0, currentPos.getX(), currentPos.getY(), currentPos.getZ(), player, false);
                    this.level.setBlock(currentPos, Blocks.CAVE_VINES.defaultBlockState(), 3);
                }

                if (this.level instanceof ServerLevel serverLevel) {
                    BlockState vineState = Blocks.VINE.defaultBlockState();
                    BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, vineState);
                    serverLevel.sendParticles(particleData,
                            currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5,
                            10, 0.0, 0.0, 0.0, 0.1);
                }

                currentPos = currentPos.below();
            }
        }
    }

    private void spawnGrowthParticles(BlockPos pos) {
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    6, 0.5, 0.5, 0.5, 0.0);
        }
    }

    private void spawnDeathParticles() {
        if (this.level.isClientSide) {
            float motion = 0.175F;

            for (int i = 0; i < 32; i++) {
                float mx = (float)(this.random.nextDouble() - 0.5) * motion;
                float my = (float)(this.random.nextDouble() - 0.5) * motion;
                float mz = (float)(this.random.nextDouble() - 0.5) * motion;

                Color color = getCorporeaRuneColor((int)this.getX(), (int)this.getY(), (int)this.getZ(), 3);
                float r = color.getRed() / 255.0F;
                float g = color.getGreen() / 255.0F;
                float b = color.getBlue() / 255.0F;

                WispParticleData data = WispParticleData.wisp(0.2F + this.random.nextFloat() * 0.12F, r, g, b, 2.0F);
                this.level.addParticle(data, this.getX(), this.getY(), this.getZ(), mx, my, mz);
            }
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", this.tickCount);
        tag.putString("Attacker", getAttacker());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tickCount = tag.getInt("Age");
        setAttacker(tag.getString("Attacker"));
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}