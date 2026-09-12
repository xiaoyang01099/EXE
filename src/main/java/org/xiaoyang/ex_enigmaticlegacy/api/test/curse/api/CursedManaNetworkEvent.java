package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

import net.minecraftforge.eventbus.api.Event;

public class CursedManaNetworkEvent extends Event {
    private final ICursedManaReceiver receiver;
    private final CursedManaBlockType type;
    private final CursedManaNetworkAction action;

    public CursedManaNetworkEvent(ICursedManaReceiver receiver, CursedManaBlockType type, CursedManaNetworkAction action) {
        this.receiver = receiver;
        this.type = type;
        this.action = action;
    }

    public ICursedManaReceiver getReceiver() {
        return receiver;
    }

    public CursedManaBlockType getType() {
        return type;
    }

    public CursedManaNetworkAction getAction() {
        return action;
    }
}
