package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControl;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IController;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ControlController extends Dependencies<IControl<?>> implements IController<ControlController, IControl<?>>
{
    private final Container inventory;

    public ControlController(@NotNull final Container inventory)
    {
        this.inventory = inventory;
    }

    public ControlController(@NotNull final Container inventory, final IControl<?>... dependencies)
    {
        super(dependencies);
        this.inventory = inventory;
    }

    public ControlController(@NotNull final Container inventory, @NotNull final Collection<IControl<?>> dependencies)
    {
        super(dependencies);
        this.inventory = inventory;
    }

    public ControlController(@NotNull final Container inventory, @NotNull final ControlController controlController)
    {
        super(controlController);
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public List<IControl<?>> compareContents(@NotNull final ControlController otherController)
    {
        return super.compareContents(otherController);
    }

    @Override
    public void addListener(final int windowId, @NotNull final WContainer<?> wContainer, @NotNull final ServerPlayer serverPlayer)
    {
        NetworkHelper.addControlListener(windowId, wContainer, serverPlayer);
    }

    @Override
    public void detectAndSendChanges(final int windowId, @NotNull final WContainer<?> wContainer)
    {
        NetworkHelper.detectAndSendControlChanges(windowId, wContainer);
    }

    @NotNull
    @Override
    public CompoundTag writeNBT()
    {
        final CompoundTag controlNBT = new CompoundTag();
        final ListTag controlTagList = new ListTag();
        controlNBT.put("control", controlTagList);
        getInstances().forEach(control -> controlTagList.add(control.writeNBT()));
        return controlNBT;
    }

    @Override
    public void afterWriteNBT(@NotNull final CompoundTag smartNBT)
    {
        final ListTag controlTagList = smartNBT.getList("control", 10);
        if (controlTagList.isEmpty())
            return;
        getInstances().forEach(control -> {
            for (int i = 0; i < controlTagList.size(); i++)
                control.afterWriteNBT(controlTagList.getCompound(i));
        });
    }

    @Override
    public void readNBT(@NotNull final CompoundTag smartNBT)
    {
        final ListTag controlTagList = smartNBT.getList("control", 10);
        if (controlTagList.isEmpty())
            return;
        getInstances().forEach(control -> {
            for (int i = 0; i < controlTagList.size(); i++)
                control.readNBT(controlTagList.getCompound(i));
        });
        inventory.setChanged();
    }

    @NotNull
    @Override
    public ControlController copy()
    {
        return new ControlController(inventory, getInstances().stream().<IControl<?>>map(IControl::copy).collect(Collectors.toList()));
    }
}