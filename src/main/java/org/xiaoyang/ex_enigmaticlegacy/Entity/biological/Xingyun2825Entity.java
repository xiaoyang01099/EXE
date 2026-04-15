package org.xiaoyang.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.EntityRainBowLightningBlot;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;

import java.util.List;
import java.util.Random;

public class Xingyun2825Entity extends Monster implements PowerableMob {
    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.BLUE, ServerBossEvent.BossBarOverlay.PROGRESS);
    public int deathTick = 0;

    public Xingyun2825Entity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.XINGYUN2825.get(), world);
    }

    public Xingyun2825Entity(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
        maxUpStep = 0.6f;
        xpReward = 0;
        setNoAi(false);
        setCustomName(Component.translatable("xingyun"));
        setCustomNameVisible(true);
        sentMassageToLocalPlayer("§eXingyun2825 加入了游戏");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected double getAttackReachSqr(LivingEntity entity) {
                return (double) (4.0 + entity.getBbWidth() * entity.getBbWidth());
            }
        });
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override
    public void kill() {}

    @Override
    public void setHealth(float p_21154_) {
        if ((p_21154_ >= 0) && (getHealth() - p_21154_) < 50) {
            super.setHealth(p_21154_);
        } else {
            super.setHealth(getHealth() - 50);
        }
    }

    @Override
    public void heal(float pHealAmount) {
        super.heal(pHealAmount);
        bossInfo.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void playerTouch(Player p_20081_) {
        if (p_20081_ instanceof ServerPlayer player) {
            startSeenByPlayer(player);
        }
        super.playerTouch(p_20081_);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    public void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(Items.BEDROCK));
        this.spawnAtLocation(new ItemStack(Items.BARRIER));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == null) return true;
        if (source.getDirectEntity() == null) return true;
        if (amount >= 50) {
            doHurtTarget(getTarget());
        }
        if (amount > 50) {
            if (amount > 10000000) return false;
            setSpeed(getSpeed() + 1f);
            return super.hurt(source, 50);
        } else {
            setSpeed(getSpeed() + 1f);
            return super.hurt(source, amount);
        }
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public void costomDie() {
        for (int i = 0; i < 22; i++) {
            setHealth(0);
        }
    }

    public static void init() {
        SpawnPlacements.register(ModEntities.XINGYUN2825.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> (world.getBlockState(pos.below()).is(BlockTags.DIRT) && world.getRawBrightness(pos, 0) > 8));
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 1024);
        builder = builder.add(Attributes.ARMOR, 100);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 300);
        builder = builder.add(Attributes.FOLLOW_RANGE, 100);
        builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 100);
        builder = builder.add(Attributes.ATTACK_KNOCKBACK, 5);
        return builder;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if (pEntity instanceof LivingEntity living) {
            if (new Random().nextInt(10) == 1) {
                double x = living.getX();
                double y = living.getY();
                double z = living.getZ();
                createRainLightning_blot(x, y, z);
                createLightning_blot(x, y, z);
                createLightning_blot(x, y, z);
                createLightning_blot(x, y, z);
                createRainLightning_blot(living.getX(), living.getY(), living.getZ());
                createLightning_blot(living.getX(), living.getY(), living.getZ());
            }
            living.hurtTime = 20;
            living.deathTime = 1;
            living.setInvulnerable(false);
            living.hurtTime++;
            living.setHealth(living.getHealth() - (living.getMaxHealth() / 20));
        }
        return true;
    }

    @Override
    protected void doAutoAttackOnTouch(LivingEntity living) {
        living.hurtTime = 20;
        living.deathTime = 1;
        double x = living.getX();
        double y = living.getY();
        double z = living.getZ();
        createRainLightning_blot(x, y, z);
        createLightning_blot(x, y, z);
        createLightning_blot(x, y, z);
        createLightning_blot(x, y, z);
        setTarget(living);
        living.setInvulnerable(false);
        living.hurtTime++;
        living.setHealth(getHealth() - (living.getMaxHealth() / 20));
        createRainLightning_blot(living.getX(), living.getY(), living.getZ());
        createLightning_blot(living.getX(), living.getY(), living.getZ());
        super.doAutoAttackOnTouch(living);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(ColorText.GetColor1("x") + "ingyun_2825");
    }

    @Override
    public Component getName() {
        return Component.literal(ColorText.GetColor1("X") + "ingyun2825");
    }

    @Override
    public @Nullable Component getCustomName() {
        return Component.literal(ColorText.GetColor1("X") + "ingyun2825");
    }

    @Override
    public void tick() {
        super.tick();
        setSpeed(50);
        bossInfo.setDarkenScreen(true);
        bossInfo.setName(getDisplayName());

        final Vec3 _center = new Vec3((getX()), (getY()), (getZ()));
        if (getTarget() != null) {
            if (getTarget().getBlockY() - getBlockY() > 4) {
                if (getTarget() instanceof Player player && level instanceof ClientLevel) {
                    player.sendSystemMessage(Component.literal("<Xingyun2825> 欺负我打不到你是吧!"));
                }
                getTarget().setHealth(0);
            }
        }

        if (!isDeadOrDying()) {
            heal(0.5F);
        } else {
            setDeltaMovement(new Vec3(0, 0.01, 0));
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
    }

    @Override
    protected void tickDeath() {
        deathTick++;
        deathTime = -2;
        hurtTime = 1;

        if (deathTick >= 1000) {
            setRemoved(RemovalReason.KILLED);
        }

        if (level.isClientSide) {
            switch (deathTick) {
                case 100:
                    sentMassageToLocalPlayer("<Xingyun2825> 还是被干掉了吗!");
                    break;
                case 250:
                    sentMassageToLocalPlayer("<Xingyun2825> 真是不甘心啊......");
                    break;
                case 400:
                    sentMassageToLocalPlayer("<Xingyun2825> 但是......");
                    break;
                case 600:
                    sentMassageToLocalPlayer("<Xingyun2825> 即便是死，我也不会让你全身而退!");
                    break;
                case 750:
                    sentMassageToLocalPlayer("<Xingyun2825> 后背隐藏能源，启动!");
                    break;
            }
        }

        level.explode(this, getX(), getY(), getZ(), 1, Level.ExplosionInteraction.MOB);
        super.tickDeath();

        if (deathTick >= 650 && this.level instanceof ServerLevel) {
            List<Entity> list1 = this.level.getEntities(this, new AABB(this.getX() - 40, this.getY() - 40, this.getZ() - 40, this.getX() + 40, this.getY() + 40.0, this.getZ() + 40), (p_147140_) -> {
                return p_147140_.isAlive() && !(p_147140_ instanceof Xingyun2825Entity);
            });

            list1.forEach(entity -> {
                if (entity instanceof LivingEntity l) {
                    if (!(l instanceof Player player && player.isCreative())) {
                        if (l.getHealth() <= 0) {
                            l.hurtTime = -1;
                            l.deathTime = -1;
                            l.kill();
                            l.remove(RemovalReason.KILLED);
                            l.onRemovedFromWorld();
                            l.setRemoved(RemovalReason.KILLED);
                            return;
                        }
                        l.hurtDuration = 0;
                        l.hurt(l.damageSources().fellOutOfWorld(), 1);
                        l.deathTime++;
                        l.hurtTime++;
                        l.setHealth(l.getHealth() - 1f);
                        if (l.getMaxHealth() > 150)
                            l.setHealth(l.getHealth() - l.getMaxHealth() / 20);

                        this.deathTime = -2;
                        this.hurtTime = 1;
                    }
                    if (entity instanceof Player player) {
                        if (deathTick >= 950 && !player.getInventory().contains(ModWeapons.ANNIHILATION_SWORD.get().getDefaultInstance()))
                            player.addItem(ModWeapons.ANNIHILATION_SWORD.get().getDefaultInstance());
                    }
                }

                if (deathTick > 900)
                    level.explode(this, getX(), getY(), getZ(), 5, Level.ExplosionInteraction.MOB);
            });
        }
    }

//    @Override
//    public boolean isRemoved() {
//        if (deathTick >= 1000) {
//            return super.isRemoved();
//        }
//        return false;
//    }
//
//    @Override
//    public void setRemoved(RemovalReason pRemovalReason) {
//        super.setRemoved(pRemovalReason);
//    }

    @Override
    public boolean isPowered() {
        return true;
    }

    @Override
    public @Nullable RemovalReason getRemovalReason() {
        if (getHealth() <= 50) {
            this.spawnAtLocation(new ItemStack(Items.BEDROCK));
            this.spawnAtLocation(new ItemStack(Items.BARRIER));
            return RemovalReason.KILLED;
        }
        return null;
    }

    private void createLightning_blot(double x, double y, double z) {
        if (level instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }
    }

    private void createRainLightning_blot(double x, double y, double z) {
        if (level instanceof ServerLevel _level) {
            EntityRainBowLightningBlot entityToSpawn = ModEntities.LIGHTNING_BLOT.get().create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }
    }

    public void sentMassageToLocalPlayer(String s) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(s));
        }
    }
}