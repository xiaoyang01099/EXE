package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.SprawlRod;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ItemGrassSeeds;

import java.awt.Color;
import java.util.UUID;

public class EntitySeed extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_SEED = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_RADIUS = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_ATTACKER = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.STRING);

    public EntitySeed(EntityType<? extends EntitySeed> type, Level world) {
        super(type, world);
    }

    public EntitySeed(EntityType<? extends EntitySeed> type, Level world, Player player) {
        super(type, player, world);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SEED, new ItemStack(Blocks.AIR));
        this.entityData.define(DATA_RADIUS, 1);
        this.entityData.define(DATA_ATTACKER, "");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= 240) {
            this.discard();
        }

        if (this.level.isClientSide) {
            spawnTrailParticles();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnTrailParticles() {
        float m = 0.02F;
        int radius = this.getRadius();
        if (radius <= 0) {
            radius = 1;
        }

        float f1 = 4.0F / ((float)radius / 20.0F);
        float size = 1.0F + (float)radius / 12.0F;

        for(int i = 0; i < 5; ++i) {
            double posX = this.getX() + (Math.random() - 0.5F) / f1;
            double posY = this.getY() + (Math.random() - 0.5F) / f1;
            double posZ = this.getZ() + (Math.random() - 0.5F) / f1;
            float mx = (float)(Math.random() - 0.5F) * m;
            float my = (float)(Math.random() - 0.5F) * m;
            float mz = (float)(Math.random() - 0.5F) * m;

            Color color = getSeedColor(this.getSeed());
            BotaniaAPI.instance().sparkleFX(this.level, posX, posY, posZ,
                    (float)color.getRed() / 255.0F,
                    (float)color.getGreen() / 255.0F,
                    (float)color.getBlue() / 255.0F,
                    (float)(0.0625F * size + Math.random() * 0.12F),
                    3);
        }
    }

    private Color getSeedColor(ItemStack seed) {
        try {
            return SprawlRod.getSeedColor(seed);
        } catch (Exception e) {
            return new Color(0, 102, 0);
        }
    }

    public int getRadius() {
        int radius = this.entityData.get(DATA_RADIUS);
        return Math.max(1, radius);
    }

    public void setRadius(int radius) {
        this.entityData.set(DATA_RADIUS, Math.max(1, radius));
    }

    public ItemStack getSeed() {
        return this.entityData.get(DATA_SEED);
    }

    public void setSeed(ItemStack stack) {
        if (stack != null) {
            stack = stack.copy();
            stack.setCount(1);
        } else {
            stack = new ItemStack(Blocks.AIR);
        }
        this.entityData.set(DATA_SEED, stack);
    }

    public String getAttacker() {
        return this.entityData.get(DATA_ATTACKER);
    }

    public void setAttacker(String str) {
        this.entityData.set(DATA_ATTACKER, str == null ? "" : str);
    }

    @Override
    protected float getGravity() {
        return 0.025F;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos hitPos = blockHit.getBlockPos();
            Block block = this.level.getBlockState(hitPos).getBlock();
            Player player = null;

            if (this.getOwner() instanceof Player) {
                player = (Player) this.getOwner();
            } else {
                String attackerName = this.getAttacker();
                if (!attackerName.isEmpty()) {
                    player = this.level.getPlayerByUUID(null);
                    for (Player p : this.level.players()) {
                        if (p.getName().getString().equals(attackerName)) {
                            player = p;
                            break;
                        }
                    }

                    if (player == null) {
                        try {
                            UUID attackerUUID = UUID.fromString(attackerName);
                            player = this.level.getPlayerByUUID(attackerUUID);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            }

            if (block instanceof BushBlock || block instanceof LeavesBlock || player == null) {
                this.discard();
                return;
            }

            ItemStack seed = this.getSeed();
            if (!seed.isEmpty() && seed.getItem() instanceof ItemGrassSeeds) {
                ItemGrassSeeds itemSeed = (ItemGrassSeeds) seed.getItem();

                for(int i = 0; i < this.getRadius(); ++i) {
                    for(int k = 0; k < this.getRadius(); ++k) {
                        int posX = hitPos.getX() + i - this.getRadius() / 2;
                        int posY = hitPos.getY();
                        int posZ = hitPos.getZ() + k - this.getRadius() / 2;

                        int j = posY;
                        if (isTopBlock(new BlockPos(posX, posY - 1, posZ))) {
                            j = Math.max(0, posY - 20);
                        }

                        while(!isTopBlock(new BlockPos(posX, j, posZ)) && Math.abs(j - posY) <= 40) {
                            ++j;
                        }

                        BlockPos targetPos = new BlockPos(posX, j, posZ);

                        if (!this.level.isClientSide && isDirt(targetPos)) {
                            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(this.level, targetPos,
                                    this.level.getBlockState(targetPos), player);
                            MinecraftForge.EVENT_BUS.post(event);

                            if (!event.isCanceled()) {
                                // 修复：直接使用种子副本调用 useOn，避免影响玩家手中的物品
                                ItemStack seedCopy = seed.copy();
                                seedCopy.setCount(1);

                                UseOnContext context = new UseOnContext(
                                        this.level,
                                        player,
                                        InteractionHand.MAIN_HAND,
                                        seedCopy,
                                        new BlockHitResult(
                                                Vec3.atCenterOf(targetPos),
                                                Direction.UP,
                                                targetPos,
                                                false
                                        )
                                );

                                // 直接调用 useOn，不需要临时替换玩家手中的物品
                                itemSeed.useOn(context);
                            }
                        } else if ((Math.random() < 0.15F || this.getRadius() < 3) && isDirt(targetPos)) {
                            spawnGrowParticle(targetPos);
                        }
                    }
                }
            }

            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ticks", this.tickCount);
        compound.putString("attacker", this.getAttacker());

        CompoundTag stackCompound = new CompoundTag();
        ItemStack stack = this.getSeed();
        if (!stack.isEmpty()) {
            stack.save(stackCompound);
        }
        compound.put("seedStack", stackCompound);
        compound.putInt("radius", this.getRadius());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tickCount = compound.getInt("ticks");
        this.setAttacker(compound.getString("attacker"));

        CompoundTag stackCompound = compound.getCompound("seedStack");
        this.setSeed(ItemStack.of(stackCompound));
        this.setRadius(compound.getInt("radius"));
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnGrowParticle(BlockPos pos) {
        for(int i = 0; i < 50; ++i) {
            double x = (Math.random() - 0.5F) * 3.0F;
            double y = Math.random() - 0.5F + 1.0F;
            double z = (Math.random() - 0.5F) * 3.0F;
            float velMul = 0.025F;
            Color color = getSeedColor(this.getSeed());

            BotaniaAPI.instance().sparkleFX(this.level,
                    pos.getX() + 0.5F + x,
                    pos.getY() + 0.5F + y,
                    pos.getZ() + 0.5F + z,
                    (float)color.getRed() / 255.0F,
                    (float)color.getGreen() / 255.0F,
                    (float)color.getBlue() / 255.0F,
                    (float)(Math.random() * 0.15F + 0.15F),
                    5);
        }
    }

    private boolean isDirt(BlockPos pos) {
        Block block = this.level.getBlockState(pos).getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
    }

    private boolean isTopBlock(BlockPos pos) {
        Block block = this.level.getBlockState(pos.above()).getBlock();
        return this.level.getBlockState(pos.above()).getMaterial() == Material.AIR ||
                block instanceof BushBlock;
    }

    @Override
    public ItemStack getItem() {
        return getSeed().getItem().getDefaultInstance();
    }
}