package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public interface IResourceShapedContainer
{
    void defineShape(@Nonnull ResourceLocation resourceLocation);

    void clearShape();
}