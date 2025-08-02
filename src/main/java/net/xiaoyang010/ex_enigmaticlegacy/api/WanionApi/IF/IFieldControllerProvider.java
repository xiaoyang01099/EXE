package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.FieldController;

import javax.annotation.Nonnull;

public interface IFieldControllerProvider
{
    @Nonnull
    FieldController getFieldController();
}