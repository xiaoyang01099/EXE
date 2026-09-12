package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;


import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res.EntityCursedManaBurst;

import java.util.UUID;

public interface ICursedManaSpreader extends ICursedManaCollector {
    void setCanShoot(boolean canShoot);
    int getBurstParticleTick();
    void setBurstParticleTick(int i);
    int getLastBurstDeathTick();
    void setLastBurstDeathTick(int ticksExisted);
    EntityCursedManaBurst runBurstSimulation();
    float getRotationX();
    float getRotationY();
    void setRotationX(float rot);
    void setRotationY(float rot);
    void commitRedirection();
    void pingback(EntityCursedManaBurst burst, UUID expectedIdentity);
    UUID getIdentifier();
}
