package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;


import net.minecraft.world.inventory.ContainerListener;

import java.util.Collection;

public interface IListenerProvider
{
    Collection<ContainerListener> getListeners();
}