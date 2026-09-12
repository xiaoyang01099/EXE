package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICursedManaReceiver {
    Level getCursedManaReceiverLevel();
    BlockPos getCursedManaReceiverPos();
    int getCurrentCursedMana();
    boolean isCursedManaFull();
    void receiveCursedMana(int mana);
    boolean canReceiveCursedManaFromBursts();
}
