package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkHelper
{
    private NetworkHelper() {}

    public static void addControlListener(final int windowId, @Nonnull final IControlControllerProvider controlControllerProvider, @Nonnull final ServerPlayer serverPlayer)
    {
        final CompoundTag smartNBT = new CompoundTag();
        final ListTag controlTagList = new ListTag();
        smartNBT.put("control", controlTagList);
        controlControllerProvider.getControlController().getInstances().forEach(control -> controlTagList.add(control.writeNBT()));
        NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), serverPlayer);
    }

    public static void detectAndSendControlChanges(final int windowId, @Nonnull final IControlContainer controlContainer)
    {
        final List<IControl<?>> controlList = controlContainer.getControlController().compareContents(controlContainer.getContainerControlController());
        if (!controlList.isEmpty()) {
            controlContainer.getContainerControlController().forceAdd(controlList.stream().<IControl<?>>map(IControl::copy).collect(Collectors.toList()));
            final CompoundTag smartNBT = new CompoundTag();
            final ListTag controlTagList = new ListTag();
            smartNBT.put("control", controlTagList);
            controlList.forEach(control -> controlTagList.add(control.writeNBT()));
            for (final ContainerListener containerListener : controlContainer.getListeners())
                if (containerListener instanceof ServerPlayer)
                    NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), (ServerPlayer) containerListener);
        }
    }

    public static void addFieldListener(final int windowId, @Nonnull final IFieldControllerProvider fieldControllerProvider, @Nonnull final ServerPlayer serverPlayer)
    {
        final CompoundTag smartNBT = new CompoundTag();
        final ListTag fieldTagList = new ListTag();
        smartNBT.put("field", fieldTagList);
        fieldControllerProvider.getFieldController().getInstances().forEach(field -> fieldTagList.add(field.writeNBT()));
        NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), serverPlayer);
    }

    public static void detectAndSendFieldChanges(final int windowId, @Nonnull final IFieldContainer fieldContainer)
    {
        final List<IField<?>> fieldList = fieldContainer.getFieldController().compareContents(fieldContainer.getContainerFieldController());
        if (!fieldList.isEmpty()) {
            fieldList.stream().map(IField::copy).collect(Collectors.toList()).forEach(fieldContainer.getContainerFieldController()::add);
            final CompoundTag smartNBT = new CompoundTag();
            final ListTag fieldTag = new ListTag();
            smartNBT.put("field", fieldTag);
            fieldList.forEach(field -> fieldTag.add(field.writeNBT()));
            for (final ContainerListener containerListener : fieldContainer.getListeners())
                if (containerListener instanceof ServerPlayer)
                    NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), (ServerPlayer) containerListener);
        }
    }

    public static void addMatchingListener(final int windowId, @Nonnull final IMatchingControllerProvider matchingControllerProvider, @Nonnull final ServerPlayer serverPlayer)
    {
        final CompoundTag smartNBT = new CompoundTag();
        final ListTag fieldTagList = new ListTag();
        smartNBT.put("matching", fieldTagList);
        matchingControllerProvider.getMatchingController().getInstances().forEach(matching -> fieldTagList.add(matching.writeNBT()));
        NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), serverPlayer);
    }

    public static void detectAndSendMatchingChanges(final int windowId, @Nonnull final IMatchingContainer matchingContainer)
    {
        final List<AbstractMatching<?>> matchingList = matchingContainer.getMatchingController().compareContents(matchingContainer.getContainerMatchingController());
        if (!matchingList.isEmpty()) {
            matchingList.stream().map(AbstractMatching::copy).collect(Collectors.toList()).forEach(matchingContainer.getContainerMatchingController()::add);
            final CompoundTag smartNBT = new CompoundTag();
            final ListTag matchingTag = new ListTag();
            smartNBT.put("matching", matchingTag);
            matchingList.forEach(matching -> matchingTag.add(matching.writeNBT()));
            for (final ContainerListener containerListener : matchingContainer.getListeners())
                if (containerListener instanceof ServerPlayer)
                    NetworkHandler.sendToPlayer(new SmartNBTMessage(windowId, smartNBT), (ServerPlayer) containerListener);
        }
    }
}