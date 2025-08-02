package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.ControlController;

import javax.annotation.Nonnull;

public interface IControlControllerProvider
{
    @Nonnull
    ControlController getControlController();
}