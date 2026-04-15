package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IModelRegister {
    @OnlyIn(Dist.CLIENT)
    void registerModels();
}