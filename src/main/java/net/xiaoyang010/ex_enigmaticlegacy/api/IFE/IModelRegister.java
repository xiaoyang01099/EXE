package net.xiaoyang010.ex_enigmaticlegacy.api.IFE;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IModelRegister {
    @OnlyIn(Dist.CLIENT)
    void registerModels();
}