package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CurseAbilityHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.TileCursedGeneratingFlower;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.client.fx.WispParticleData;

/**
 * 诅咒收割花
 * 消耗玩家经验来生成诅咒魔力
 */
public class TileCursedGourmaryllis extends TileCursedGeneratingFlower {

    private static final String TAG_COOLDOWN_TICKS = "cooldownTicks";
    private static final int XP_COST = 5; // 每次消耗5点经验
    private static final int COOLDOWN = 1; // 3秒冷却

    private int cooldownTicks = 0;

    public TileCursedGourmaryllis(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CURSED_GOURMARYLLIS_TILE.get(), pos, state);
    }

    public TileCursedGourmaryllis(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.baseManaGeneration = 300;
        this.range = 8;
        this.maxCooldown = COOLDOWN;
    }

    @Override
    protected boolean tryGenerateMana(Player cursedPlayer) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 检查经验值
        int totalXp = getTotalExperience(cursedPlayer);
        if (totalXp < XP_COST) {
            return false;
        }

        // 消耗经验
        removeExperience(cursedPlayer, XP_COST);

        // 播放音效
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition,
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                    0.3F, 1.5F);
        }

        cooldownTicks = COOLDOWN;

        // 生成粒子
        spawnXpParticles(cursedPlayer);

        return true;
    }

    private int getTotalExperience(Player player) {
        int level = player.experienceLevel;
        int xp = (int) (getExperienceForLevel(level) * player.experienceProgress);

        for (int i = 0; i < level; i++) {
            xp += getExperienceForLevel(i);
        }

        return xp;
    }

    private void removeExperience(Player player, int amount) {
        int total = getTotalExperience(player);
        int newTotal = Math.max(0, total - amount);

        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;

        player.giveExperiencePoints(newTotal);
    }

    private int getExperienceForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }

    private void spawnXpParticles(Player player) {
        if (level == null || !level.isClientSide) return;

        int curseLevel = CurseAbilityHandler.INSTANCE.getCurseLevel(player);
        float intensity = 0.5F + curseLevel * 0.1F;

        for (int i = 0; i < 15; i++) {
            WispParticleData data = WispParticleData.wisp(
                    0.15F * intensity,
                    0.8F, 1.0F, 0.2F,
                    true
            );
            level.addParticle(data,
                    worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.6,
                    worldPosition.getY() + 0.8,
                    worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.6,
                    (Math.random() - 0.5) * 0.03,
                    Math.random() * 0.08,
                    (Math.random() - 0.5) * 0.03
            );
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_COOLDOWN_TICKS, cooldownTicks);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        cooldownTicks = cmp.getInt(TAG_COOLDOWN_TICKS);
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), range);
    }
    @Override
    public int getCursedColor() {
        return 0xFFD700;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new CursedGeneratingWandHud<>(this)).cast());
    }
}
