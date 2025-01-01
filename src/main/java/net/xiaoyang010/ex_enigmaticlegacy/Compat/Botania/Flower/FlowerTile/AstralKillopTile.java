package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.List;


public class AstralKillopTile extends TileEntityFunctionalFlower {
    private static final String TAG_DAYS = "days";
    private static final String TAG_CURRENT_TICK = "currentTick";
    private static final int ASTRAL_NUGGET_DAY = 14;
    private static final int EFFECT_DAY = 25;
    private static final int RANGE = 5;
    private static final int MANA_COST = 100;

    private int days = 0;
    private long lastCheckTime = 0; // 记录上次检查的游戏时间

    public AstralKillopTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AstralKillopTile> {
        public FunctionalWandHud(AstralKillopTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) return;

        // 获取当前游戏时间
        long currentTime = level.getDayTime();

        // 如果没有足够的魔力，直接返回，不重置天数
        if (getMana() < MANA_COST) {
            lastCheckTime = currentTime; // 更新上次检查时间，防止魔力恢复后立即触发
            return;
        }
        addMana(-MANA_COST);

        if (currentTime % 24000 == 0 && (currentTime - lastCheckTime) >= 1) {
            days++;
            lastCheckTime = currentTime;

            ItemStack dropStack;
            int count = 1;

            // 第25天
            if (days == EFFECT_DAY) {
                dropStack = new ItemStack(ModItems.ASTRAL_PILE.get());
                count = 4;

                AABB area = new AABB(getEffectivePos().offset(-RANGE, -RANGE, -RANGE),
                        getEffectivePos().offset(RANGE, RANGE, RANGE));
                List<Player> players = level.getEntitiesOfClass(Player.class, area);

                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 29));
                }

                days = 0;
            }
            // 第14天
            else if (days == ASTRAL_NUGGET_DAY) {
                dropStack = new ItemStack(ModItems.ASTRAL_NUGGET.get());
            }
            // 普通天数
            else {
                dropStack = new ItemStack(ModItems.ASTRAL_PILE.get());
            }

            // 生成掉落物
            for (int i = 0; i < count; i++) {
                ItemEntity itemEntity = new ItemEntity(level,
                        getEffectivePos().getX() + 0.5,
                        getEffectivePos().getY() + 1.0,
                        getEffectivePos().getZ() + 0.5,
                        dropStack.copy());
                level.addFreshEntity(itemEntity);
            }

            // 特效
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        getEffectivePos().getX() + 0.5,
                        getEffectivePos().getY() + 1.0,
                        getEffectivePos().getZ() + 0.5,
                        20, 0.5, 0.5, 0.5, 0.1);

                level.playSound(null, getEffectivePos(),
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_DAYS, days);
        tag.putLong(TAG_CURRENT_TICK, lastCheckTime);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        days = tag.getInt(TAG_DAYS);
        lastCheckTime = tag.getLong(TAG_CURRENT_TICK);
    }

    @Override
    public int getMaxMana() {
        return 10000;
    }

    @Override
    public int getColor() {
        return 0x00008B;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new AstralKillopTile.FunctionalWandHud(this)).cast());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}