package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ILensEffect;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.WispParticleData;


import javax.annotation.Nullable;
import java.util.List;

public class AquaSword extends SwordItem implements IManaItem, ILensEffect {
    private static final int MAX_MANA = 100000;
    private static final int[] MANA_COSTS = {1000, 2000, 5000, 8000, 13000, 18000};
    private static final int[] EFFECT_LEVELS = {4, 5, 6, 7, 8, 10};
    private static final String TAG_POWER_LEVEL = "powerLevel";
    private static final String TAG_INTERNAL_MANA = "internalMana";
    private boolean hasSoundPlayed = false;
    private static final int MANA_PER_USE = 1000;

    public AquaSword(Properties Properties) {
        super(EXEAPI.mithrilToolMaterial, 3, -2.4F,
                new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            handleManaAbsorption(stack, world, player);
        }
        super.inventoryTick(stack, world, entity, slotId, isSelected);
    }

    private void handleManaAbsorption(ItemStack stack, Level world, Player player) {
        BlockPos pos = player.getOnPos();

        for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof IManaPool pool) {
                int poolMana = pool.getCurrentMana();
                int currentMana = getInternalMana(stack);
                int absorbAmount = 500;

                if (poolMana > absorbAmount && currentMana < MAX_MANA) {
                    int actualAbsorb = Math.min(absorbAmount, MAX_MANA - currentMana);
                    if (actualAbsorb > 0) {
                        setInternalMana(stack, currentMana + actualAbsorb);
                        pool.receiveMana(-actualAbsorb);

                        if (world.isClientSide && world.random.nextFloat() < 0.1f) {
                            spawnManaAbsorptionParticles(world, player);
                        }
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnManaAbsorptionParticles(Level world, Player player) {
        for (int i = 0; i < 3; i++) {
            double x = player.getX() + (world.random.nextDouble() - 0.5) * 0.8;
            double y = player.getY() + world.random.nextDouble() * 1.5;
            double z = player.getZ() + (world.random.nextDouble() - 0.5) * 0.8;

            WispParticleData data = WispParticleData.wisp(
                    0.1F + world.random.nextFloat() * 0.1F,
                    0.0F,
                    0.3F + world.random.nextFloat() * 0.2F,
                    0.8F + world.random.nextFloat() * 0.2F,
                    0.3F
            );
            world.addParticle(data, x, y, z,
                    (player.getX() - x) * 0.1,
                    (player.getY() + 1 - y) * 0.1,
                    (player.getZ() - z) * 0.1);
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                int currentLevel = getPowerLevel(stack);
                int nextLevel = (currentLevel + 1) % MANA_COSTS.length;
                setPowerLevel(stack, nextLevel);

                player.displayClientMessage(
                        new TranslatableComponent("message.ex_enigmaticlegacy.aqua_sword.power_level",
                                nextLevel + 1,
                                MANA_COSTS[nextLevel]),
                        true);
            }
            return false;
        }

        AABB axis = new AABB(entity.getX(), entity.getY(), entity.getZ(),
                entity.xOld, entity.yOld, entity.zOld).inflate(1.7F);
        List<LivingEntity> entities = entity.level.getEntitiesOfClass(LivingEntity.class, axis);

        double posX = entity.getX();
        double posY = entity.getY() + entity.getEyeHeight();
        double posZ = entity.getZ();

        if (!player.level.isClientSide) {
            boolean hasWaterSplash = false;
            int powerLevel = getPowerLevel(stack);
            int manaCost = MANA_COSTS[powerLevel];
            int effectLevel = EFFECT_LEVELS[powerLevel];

            for (LivingEntity living : entities) {
                if (!(living instanceof Player) ||
                        (!((Player)living).getGameProfile().getName().equals(player.getGameProfile().getName())
                                && player.getServer() != null && player.getServer().isPvpAllowed())) {

                    boolean hasEnoughMana = false;
                    if (getInternalMana(stack) >= manaCost) {
                        hasEnoughMana = true;
                    } else if (ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, false)) {
                        hasEnoughMana = true;
                    }

                    if (!hasEnoughMana) {
                        if (!hasWaterSplash) {
                            player.displayClientMessage(
                                    new TranslatableComponent("message.ex_enigmaticlegacy.aqua_sword.mana_required",
                                            manaCost),
                                    true);
                            hasWaterSplash = true;
                        }

                        living.hurt(DamageSource.playerAttack(player),
                                EXEAPI.mithrilToolMaterial.getAttackDamageBonus() / 2.0F);
                        continue;
                    }

                    if (living.hurt(DamageSource.playerAttack(player),
                            EXEAPI.mithrilToolMaterial.getAttackDamageBonus() / 2.0F)) {

                        living.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 2000, effectLevel));

                        if (getInternalMana(stack) >= manaCost) {
                            setInternalMana(stack, getInternalMana(stack) - manaCost);
                        } else {
                            ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true);
                        }

                        if (!hasWaterSplash) {
                            hasWaterSplash = true;
                        }

                        Vec3 vec3 = player.getViewVector(1.0F).normalize();
                        living.setDeltaMovement(living.getDeltaMovement().add(
                                vec3.x * 1.35F,
                                vec3.y / 1.8F,
                                vec3.z * 1.35F
                        ));
                    }
                }
            }

            if (hasWaterSplash) {
                player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.AQUA_SWORD,
                        player.getSoundSource(), 1.2F, 1.2F);
            }
        } else if (getInternalMana(stack) >= MANA_COSTS[getPowerLevel(stack)] ||
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COSTS[getPowerLevel(stack)], false)) {
            spawnWispParticles(player.level, posX, posY, posZ);
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    private int getPowerLevel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_POWER_LEVEL);
    }

    private void setPowerLevel(ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_POWER_LEVEL, level);
    }

    private int getInternalMana(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_INTERNAL_MANA);
    }

    private void setInternalMana(ItemStack stack, int mana) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_INTERNAL_MANA, Math.max(0, Math.min(mana, MAX_MANA)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        int powerLevel = getPowerLevel(stack);
        int internalMana = getInternalMana(stack);

        list.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.aqua_sword.power_level",
                powerLevel + 1));
        list.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.aqua_sword.mana_cost",
                MANA_COSTS[powerLevel]));
        list.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.aqua_sword.effect_level",
                EFFECT_LEVELS[powerLevel] + 1));
        list.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.aqua_sword.internal_mana",
                internalMana, MAX_MANA));
        list.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.aqua_sword.shift_hint"));
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnWispParticles(Level level, double posX, double posY, double posZ) {
        for (int i = 0; i < 24; ++i) {
            float mtX = (float) ((Math.random() - 0.5F) * 0.12F);
            float mtY = (float) ((Math.random() - 0.5F) * 0.12F);
            float mtZ = (float) ((Math.random() - 0.5F) * 0.12F);

            WispParticleData data = WispParticleData.wisp(
                    0.17F + (float) (Math.random() * 0.3F),
                    0.0F,
                    (float) (Math.random() * 0.35F),
                    1.0F - (float) (Math.random() * 0.4F),
                    0.512F
            );
            level.addParticle(data, posX, posY, posZ, mtX, mtY, mtZ);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playWaterSound(Player player) {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.WATER_AMBIENT, 0.8F, 1.7F)
        );
    }

    public void onUsingTick(ItemStack stack, LivingEntity livingEntity, int count) {
        if (!(livingEntity instanceof Player player)) return;

        if (player.level.isClientSide) {
            boolean hasEnoughMana = getInternalMana(stack) >= 15 ||
                    ManaItemHandler.instance().requestManaExactForTool(stack, player, 15, false);

            if (!hasEnoughMana) {
                return;
            }

            if (!hasSoundPlayed) {
                playWaterSound(player);
                hasSoundPlayed = true;
            }

            int totalTime = getUseDuration(stack) - count;
            int time = totalTime % 120;

            double baseHeight = player.getY() + 0.1;
            double radius = 1.4;

            int wispCount = 10;
            double tickIncrement = 360.0 / wispCount;

            for (int layer = 0; layer < 2; layer++) {
                double heightOffset = layer * 0.5;

                for (int i = 0; i < wispCount; ++i) {
                    double angle = (i * tickIncrement + totalTime * 2) * Math.PI / 180.0; // 增加旋转速度
                    double posX = player.getX() + Math.sin(angle) * radius;
                    double posY = baseHeight + heightOffset + Math.sin(time * 0.05 + i * 0.5) * 0.1; // 添加垂直波动
                    double posZ = player.getZ() + Math.cos(angle) * radius;

                    float motionScale = 0.02F;
                    float mtX = (float) (-Math.sin(angle) * motionScale);
                    float mtY = (float) (Math.random() * 0.02 - 0.01);
                    float mtZ = (float) (-Math.cos(angle) * motionScale);

                    WispParticleData data = WispParticleData.wisp(
                            0.3F,
                            0.0F,
                            0.4F + (float) (Math.random() * 0.2F),
                            0.8F + (float) (Math.random() * 0.2F),
                            0.8F
                    );

                    player.level.addParticle(data, posX, posY, posZ, mtX, mtY, mtZ);
                }
            }

            if (time % 4 == 0) {
                for (int i = 0; i < 4; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double randRadius = radius * (0.8 + Math.random() * 0.2);
                    double posX = player.getX() + Math.sin(angle) * randRadius;
                    double posZ = player.getZ() + Math.cos(angle) * randRadius;

                    WispParticleData topData = WispParticleData.wisp(
                            0.2F,
                            0.0F,
                            0.4F,
                            0.9F,
                            0.6F
                    );
                    player.level.addParticle(topData,
                            posX,
                            player.getY() + 1.8,
                            posZ,
                            0, -0.02F, 0);

                    WispParticleData bottomData = WispParticleData.wisp(
                            0.2F,
                            0.0F,
                            0.4F,
                            0.9F,
                            0.6F
                    );
                    player.level.addParticle(bottomData,
                            posX,
                            player.getY() + 0.1,
                            posZ,
                            0, 0.02F, 0);
                }
            }
        } else {
            AABB axis = new AABB(player.getX(), player.getY(), player.getZ(),
                    player.xOld, player.yOld, player.zOld).inflate(2.75F);

            for (LivingEntity living : player.level.getEntitiesOfClass(LivingEntity.class, axis)) {
                if (!(living instanceof Player) ||
                        (!((Player)living).getGameProfile().getName().equals(player.getGameProfile().getName())
                                && player.getServer() != null && player.getServer().isPvpAllowed())) {

                    double dist = living.distanceTo(player) / 2.5F;
                    boolean hasEnoughMana = false;

                    if (getInternalMana(stack) >= 15) {
                        hasEnoughMana = true;
                    } else if (ManaItemHandler.instance().requestManaExactForTool(stack, player, 15, false)) {
                        hasEnoughMana = true;
                    }

                    if (hasEnoughMana && living.hurt(DamageSource.playerAttack(player), 1.0F)) {
                        if (getInternalMana(stack) >= 15) {
                            setInternalMana(stack, getInternalMana(stack) - 15);
                        } else {
                            ManaItemHandler.instance().requestManaExactForTool(stack, player, 15, true);
                        }

                        if (dist <= 1.0F) {
                            double d5 = living.getX() - player.getX();
                            double d7 = living.getZ() - player.getZ();
                            double d9 = Mth.sqrt((float) (d5 * d5 + d7 * d7));

                            if (d9 != 0.0F) {
                                d5 /= d9;
                                d7 /= d9;
                                living.setDeltaMovement(living.getDeltaMovement().add(
                                        d5 * 1.2F,
                                        0,
                                        d7 * 1.2F
                                ));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        hasSoundPlayed = false;
    }

    @Override
    public int getMana() {
        return MAX_MANA;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return false;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }

    @Override
    public void apply(ItemStack stack, BurstProperties props, Level level) {
    }

    @Override
    public boolean collideBurst(IManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
        return false;
    }

    @Override
    public void updateBurst(IManaBurst burst, ItemStack stack) {
    }

    @Override
    public boolean doParticles(IManaBurst burst, ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean hasEnoughMana = getInternalMana(stack) >= MANA_PER_USE ||
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, false);

        if (hasEnoughMana) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}