package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 诅咒魔力脉冲接口
 */
public interface ICursedManaBurst {
    BlockPos NO_SOURCE = new BlockPos(0, Integer.MIN_VALUE, 0);

    boolean isFake();

    int getColor();
    void setColor(int color);

    int getCursedMana();
    void setCursedMana(int mana);

    int getStartingCursedMana();
    void setStartingCursedMana(int mana);

    int getMinCursedManaLoss();
    void setMinCursedManaLoss(int minManaLoss);

    float getCursedManaLossPerTick();
    void setCursedManaLossPerTick(float mana);

    float getBurstGravity();
    void setGravity(float gravity);

    BlockPos getBurstSourceBlockPos();
    void setBurstSourceCoords(BlockPos pos);

    ItemStack getSourceLens();
    void setSourceLens(ItemStack lens);

    boolean hasAlreadyCollidedAt(BlockPos pos);
    void setCollidedAt(BlockPos pos);

    int getTicksExisted();
    void setFake(boolean fake);

    void setShooterUUID(UUID uuid);
    UUID getShooterUUID();

    void ping();

    boolean hasWarped();
    void setWarped(boolean warped);

    boolean hasLeftSource();

    default ThrowableProjectile entity() {
        return (ThrowableProjectile) this;
    }
}
