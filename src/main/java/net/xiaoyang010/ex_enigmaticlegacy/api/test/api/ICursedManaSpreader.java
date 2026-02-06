package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;


import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.EntityCursedManaBurst;

import java.util.UUID;

/**
 * 诅咒魔力发射器接口
 */
public interface ICursedManaSpreader extends ICursedManaCollector {

    void setCanShoot(boolean canShoot);

    int getBurstParticleTick();
    void setBurstParticleTick(int i);

    int getLastBurstDeathTick();
    void setLastBurstDeathTick(int ticksExisted);

    EntityCursedManaBurst runBurstSimulation();

    /**
     * @return X 旋转角度（度数）
     */
    float getRotationX();

    /**
     * @return Y 旋转角度（度数）
     */
    float getRotationY();

    /**
     * 设置 X 旋转
     */
    void setRotationX(float rot);

    /**
     * 设置 Y 旋转
     */
    void setRotationY(float rot);

    /**
     * 提交旋转更改
     */
    void commitRedirection();

    /**
     * 脉冲回调
     */
    void pingback(EntityCursedManaBurst burst, UUID expectedIdentity);

    /**
     * @return 发射器的唯一标识符
     */
    UUID getIdentifier();
}
