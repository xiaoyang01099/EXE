package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.MatchingController;

import javax.annotation.Nonnull;

public interface IMatchingContainer extends IMatchingControllerProvider, IListenerProvider, ISmartNBT {
    @Nonnull
    MatchingController getContainerMatchingController();
}