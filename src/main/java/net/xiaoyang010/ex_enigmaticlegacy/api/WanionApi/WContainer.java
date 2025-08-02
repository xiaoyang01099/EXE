package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControlContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IController;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IFieldContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IMatchingContainer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public abstract class WContainer<T extends WTileEntity> extends AbstractContainerMenu implements IControlContainer, IFieldContainer, IMatchingContainer {
    private final Dependencies<IController<?, ?>> controllerHandler = new Dependencies<>();
    private final Collection<IController<?, ?>> controllers = controllerHandler.getInstances();
    private final T wTileEntity;
    private final List<ContainerListener> containerListeners;

    public WContainer(MenuType<?> menuType, int containerId, @Nonnull final T wTileEntity) {
        super(menuType, containerId);
        this.containerListeners = Lists.newArrayList();
        this.wTileEntity = wTileEntity;
        wTileEntity.getControllers().forEach(controller -> controllerHandler.add((IController<?, ?>) controller.copy()));
    }

    public WContainer(@Nonnull final T wTileEntity)
    {
        this(null, 0, wTileEntity);
    }

    public final T getTile()
    {
        return wTileEntity;
    }

    public final String getTileName()
    {
        return wTileEntity.getDisplayName().getString();
    }

    @Override
    public void addSlotListener(@Nonnull final ContainerListener listener) {
        super.addSlotListener(listener);
        if (!(listener instanceof ServerPlayer))
            return;
        controllers.forEach(controller -> controller.addListener(containerId, this, (ServerPlayer) listener));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        controllers.forEach(controller -> controller.detectAndSendChanges(containerId, this));
    }

    @Override
    public final boolean stillValid(@Nonnull final Player player)
    {
        return wTileEntity.stillValid(player);
    }

    @Override
    public final Collection<ContainerListener> getListeners()
    {
        return containerListeners;
    }

    @Override
    public void removed(@Nonnull final Player player) {
        super.removed(player);
        if (wTileEntity.hasController(FieldController.class))
            wTileEntity.getController(FieldController.class).getInstances().forEach(field -> field.endInteraction(player));
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag smartNBT) {
        wTileEntity.getControllers().forEach(controller -> controller.readNBT(smartNBT));
        wTileEntity.setChanged();
    }

    @Override
    public void receiveNBT(@Nonnull final CompoundTag compoundTag) {
        getFieldController().receiveNBT(compoundTag);
        wTileEntity.setChanged();
    }

    @Nonnull
    @Override
    public final ControlController getControlController() {
        return wTileEntity.getController(ControlController.class);
    }

    @Nonnull
    @Override
    public final ControlController getContainerControlController() {
        return controllerHandler.get(ControlController.class);
    }

    @Nonnull
    @Override
    public final FieldController getFieldController()
    {
        return wTileEntity.getController(FieldController.class);
    }

    @Nonnull
    @Override
    public final FieldController getContainerFieldController()
    {
        return controllerHandler.get(FieldController.class);
    }

    @Nonnull
    @Override
    public final MatchingController getMatchingController() {
        return wTileEntity.getController(MatchingController.class);
    }

    @Nonnull
    @Override
    public final MatchingController getContainerMatchingController() {
        return controllerHandler.get(MatchingController.class);
    }
}