package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

@Mod.EventBusSubscriber
public class KillingBerryTile extends GeneratingFlowerBlockEntity {
    private static final int BOSS_KILL_MANA = 20000;
    private static final int RANGE = 20;
    private static final int MAX_MANA = 20000;
    private static final int HOSTILE_KILL_MANA = 200;
    private static final int PASSIVE_KILL_PENALTY = -500;
    private static final int PLAYER_DEATH_PENALTY = -100;
    private static final float PLAYER_KILL_MULTIPLIER = 2.0f;

    public KillingBerryTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (!getLevel().isClientSide && getMana() > 0) {
            emptyManaIntoCollector();
        }
    }

    private static boolean isBossEntity(LivingEntity entity) {
        return entity instanceof EnderDragon ||
                entity instanceof WitherBoss ||
                entity instanceof CatMewEntity;
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level.isClientSide)
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        boolean isPlayerDeath = entity instanceof Player;
        boolean isPlayerKill = event.getSource().getEntity() instanceof Player;

        for (BlockEntity be : entity.level().getChunkAt(entity.blockPosition()).getBlockEntities().values()) {
            if (be instanceof KillingBerryTile berry) {
                BlockPos deathPos = entity.blockPosition();
                BlockPos flowerPos = berry.getEffectivePos();

                if (deathPos.distSqr(flowerPos) <= RANGE * RANGE) {
                    int manaChange;
                    if (isPlayerDeath) {
                        manaChange = PLAYER_DEATH_PENALTY;
                    } else {
                        manaChange = calculateManaChange(entity, isPlayerKill);
                    }

                    int currentMana = berry.getMana();
                    int newMana = Math.max(0, currentMana + manaChange);
                    berry.addMana(newMana - currentMana);
                    break;
                }
            }
        }
    }

    private static int calculateManaChange(LivingEntity entity, boolean isPlayerKill) {
        if (isBossEntity(entity)) {
            return BOSS_KILL_MANA;
        }

        MobCategory category = entity.getType().getCategory();
        int baseMana = switch (category) {
            case MONSTER -> HOSTILE_KILL_MANA;
            case CREATURE, AMBIENT, AXOLOTLS, UNDERGROUND_WATER_CREATURE, WATER_CREATURE -> PASSIVE_KILL_PENALTY;
            default -> 0;
        };

        return isPlayerKill && baseMana > 0 ? (int)(baseMana * PLAYER_KILL_MULTIPLIER) : baseMana;
    }

    @Override
    public void addMana(int mana) {
        int newMana = Math.max(0, Math.min(MAX_MANA, this.getMana() + mana));
        super.addMana(newMana - this.getMana());
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(this)).cast());
    }

    @Override
    public int getColor() {
        return 0xD40000;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getEffectivePos(), RANGE);
    }
}