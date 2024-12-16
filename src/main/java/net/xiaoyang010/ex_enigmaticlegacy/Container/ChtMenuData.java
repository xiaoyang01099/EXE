package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.inventory.ContainerData;

public class ChtMenuData implements ContainerData {
    private int timer;
    @Override
    public int get(int i) {
        return this.timer;
    }

    @Override
    public void set(int i, int i1) {
        this.timer = i1;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
