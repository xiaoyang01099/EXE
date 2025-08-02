package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.server.level.ServerPlayer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WContainer;

import javax.annotation.Nonnull;
import java.util.List;

public interface IController<C extends ICopyable<C>, O> extends ICopyable<C>, ISmartNBT
{
    @Nonnull
    List<O> compareContents(@Nonnull final C otherController);

    void addListener(final int windowId, @Nonnull final WContainer<?> wContainer, @Nonnull final ServerPlayer serverPlayer);

    void detectAndSendChanges(final int windowId, @Nonnull final WContainer<?> wContainer);
}