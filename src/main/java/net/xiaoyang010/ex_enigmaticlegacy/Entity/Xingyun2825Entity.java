package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModWeapons;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class Xingyun2825Entity extends Monster implements PowerableMob{
    @SubscribeEvent
    public static void addLivingEntityToBiomes(BiomeLoadingEvent event) {
        event.getSpawns().getSpawner(MobCategory.CREATURE).add(new MobSpawnSettings.SpawnerData(ModEntities.XINGYUN2825.get(), 20, 4, 4));
    }
    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.BLUE, ServerBossEvent.BossBarOverlay.PROGRESS);

    public Xingyun2825Entity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.XINGYUN2825.get(), world);

    }
    public Xingyun2825Entity(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
        MinecraftForge.EVENT_BUS.register(this);
        maxUpStep = 0.6f;
        xpReward = 0;
        setNoAi(false);
        setCustomName(new TextComponent("xingyun"));
        setCustomNameVisible(true);

    }
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
        //this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(0,new HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }


    @Override
    public void remove(RemovalReason p_146834_) {
    }

    @Override
    public void setHealth(float p_21154_) {
        if ((p_21154_ >=0 ) && (getHealth()-p_21154_)<50) {
            super.setHealth(p_21154_);
        }else {
            super.setHealth(getHealth()-50);
        }
    }

    @Override
    public void heal(float pHealAmount) {
        super.heal(pHealAmount);
        bossInfo.setProgress(getHealth()/getMaxHealth());
    }

    @Override
    public void kill() {
    }

    @Override
    public void playerTouch(Player p_20081_) {
        if (p_20081_ instanceof ServerPlayer player){
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
//一般这里有这种方法是用来管理掉落的
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(Items.BEDROCK.asItem()));
        this.spawnAtLocation(new ItemStack(Items.BARRIER.asItem()));
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow)
            return false;
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud)
            return false;
        if (source == DamageSource.FALL)
            return false;
        if (source == DamageSource.CACTUS)
            return false;
        if (source == DamageSource.DROWN)
            return false;
        if (source == DamageSource.LIGHTNING_BOLT)
            return false;
        if (source.isExplosion())
            return false;
        if (source.getMsgId().equals("trident"))
            return false;
        if (source == DamageSource.ANVIL)
            return false;
        if (source == DamageSource.DRAGON_BREATH)
            return false;
        if (source == DamageSource.WITHER)
            return false;
        if (source.getMsgId().equals("witherSkull"))
            return false;
        if (amount>=50) {
            doHurtTarget(getTarget());
        }
        if (amount>50) {
            if (amount>10000000)return false;
            setSpeed(getSpeed()+1f);
            return super.hurt(source, 50);
        }else {
            setSpeed(getSpeed()+1f);
            return super.hurt(source,amount);
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
public void costomDie(){
        setRemoved(RemovalReason.UNLOADED_TO_CHUNK);
}
    public static void init() {
        SpawnPlacements.register(ModEntities.XINGYUN2825.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> (world.getBlockState(pos.below()).getMaterial() == Material.GRASS && world.getRawBrightness(pos, 0) > 8));
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
            if (new Random().nextInt(10) ==1) {
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
            living.invulnerable = false;
            living.hurtTime++;
            living.setHealth(living.getHealth() - (living.getMaxHealth() / 20));
        }
        return true;
    }

    @Override
    protected void doAutoAttackOnTouch(LivingEntity living) {
        living.hurtTime=20;
        living.deathTime=1;
        double x= living.getX();
        double y= living.getY();
        double z= living.getZ();
        createRainLightning_blot(x,y,z);
        createLightning_blot(x,y,z);
        createLightning_blot(x,y,z);
        createLightning_blot(x,y,z);
        setTarget(living);
        living.invulnerable = false;
        living.hurtTime++;
        living.setHealth(getHealth()-(living.getMaxHealth()/20));
        createRainLightning_blot(living.getX(),living.getY(),living.getZ());
        createLightning_blot(living.getX(),living.getY(),living.getZ());
        super.doAutoAttackOnTouch(living);
    }
    @Override
    public Component getDisplayName() {
        return Component.nullToEmpty("xingyun_2825");
    }

public static int dalay = 0;
    @Override
    public void tick() {
        super.tick();
        bossInfo.setDarkenScreen(true);
        //bossInfo.setCreateWorldFog(true);
        final Vec3 _center = new Vec3((getX()), (getY()), (getZ()));
        if (getTarget() != null) {
            if (getTarget().getBlockY()-getBlockY()>4){
                setItemInHand(InteractionHand.MAIN_HAND, ModWeapons.MANAITASWORDGOD.get().getDefaultInstance());
                getTarget().setHealth(0);
            }else {
                //setItemInHand(InteractionHand.MAIN_HAND,Items.AIR.getDefaultInstance());
            }
        }
       /* List<Entity> _entfound = level.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5 / 2d), e -> true).stream()
                .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).collect(Collectors.toList());
        for (Entity entityiterator : _entfound) {
            if (getTarget() == null) {
                if (entityiterator instanceof LivingEntity living) {
                    if (living instanceof Player player && player.isCreative())return;
                    if (!(living instanceof Xingyun2825Entity)){
                        setTarget(living);
                        living.invulnerable = false;
                        living.hurtTime++;
                        living.setHealth(getHealth()-(living.getMaxHealth()/20));
                        createRainLightning_blot(living.getX(),living.getY(),living.getZ());
                        createLightning_blot(living.getX(),living.getY(),living.getZ());
                    }
                }
            }else if (entityiterator instanceof  LivingEntity living){
                if (living.isDeadOrDying())setTarget(null);
                if (living.isRemoved())setTarget(null);
            }
        }*/
        if (getHealth()<=0.0F){
            setRemoved(RemovalReason.KILLED);
        }
        heal(0.5F);
    }

    @Override
    public boolean isPowered() {
        return true;
    }

    @Override
    public @Nullable RemovalReason getRemovalReason() {
        if (getHealth()<=50){
            this.spawnAtLocation(new ItemStack(Items.BEDROCK.asItem()));
            this.spawnAtLocation(new ItemStack(Items.BARRIER.asItem()));
            return RemovalReason.KILLED;
        }
        return null;
    }

    private void createLightning_blot(double x, double y, double z){
        if (level instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }

    }
    private void createRainLightning_blot(double x,double y,double z){
        if (level instanceof ServerLevel _level) {
            EntityRainBowLightningBlot entityToSpawn = ModEntities.LIGHTNING_BLOT.get().create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }

    }
}
