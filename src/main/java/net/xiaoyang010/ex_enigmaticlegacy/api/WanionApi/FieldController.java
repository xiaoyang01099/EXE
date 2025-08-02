package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IController;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IField;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.INBTMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldController implements IController<FieldController, IField<?>>, INBTMessage
{
    private final Map<String, IField<?>> fieldControlMap = new Object2ObjectOpenHashMap<>();
    private final Collection<IField<?>> values = fieldControlMap.values();
    private final Container container;

    public FieldController(@Nonnull final Container container)
    {
        this.container = container;
    }

    public FieldController(@Nonnull final Container container, final IField<?>... fields)
    {
        this(container);
        add(fields);
    }

    public FieldController(@Nonnull final Container container, @Nonnull final List<IField<?>> fieldList)
    {
        this(container);
        fieldList.forEach(this::add);
    }

    public FieldController(@Nonnull final Container container, @Nonnull final Map<String, IField<?>> fieldMap)
    {
        this(container);
        fieldControlMap.putAll(fieldMap);
    }

    public void add(@Nonnull final IField<?>... fields)
    {
        for (final IField<?> field : fields)
            fieldControlMap.put(field.getFieldName(), field);
    }

    public Collection<IField<?>> getInstances()
    {
        return values;
    }

    public IField<?> getField(@Nonnull final String name)
    {
        return fieldControlMap.get(name);
    }

    @Nonnull
    @Override
    public List<IField<?>> compareContents(@Nonnull final FieldController otherFieldController)
    {
        final List<IField<?>> differences = new ArrayList<>();
        for (final IField<?> field : values) {
            final IField<?> otherField = otherFieldController.fieldControlMap.get(field.getFieldName());
            if (!field.equals(otherField))
                differences.add(field);
        }
        return differences;
    }

    @Override
    public void addListener(final int windowId, @Nonnull final WContainer<?> wContainer, @Nonnull final ServerPlayer serverPlayer)
    {
        NetworkHelper.addFieldListener(windowId, wContainer, serverPlayer);
    }

    @Override
    public void detectAndSendChanges(final int windowId, @Nonnull final WContainer<?> wContainer)
    {
        NetworkHelper.detectAndSendFieldChanges(windowId, wContainer);
    }

    @Nonnull
    public CompoundTag writeNBT()
    {
        final CompoundTag fieldNBT = new CompoundTag();
        final ListTag fieldTagList = new ListTag();
        fieldNBT.put("field", fieldTagList);
        values.forEach(field -> fieldTagList.add(field.writeNBT()));
        return fieldNBT;
    }

    @Override
    public void afterWriteNBT(@Nonnull final CompoundTag smartNBT)
    {
        final ListTag fieldTagList = smartNBT.getList("field", Tag.TAG_COMPOUND);
        if (fieldTagList.isEmpty())
            return;
        for (int i = 0; i < fieldTagList.size(); i++) {
            final CompoundTag fieldTag = fieldTagList.getCompound(i);
            final IField<?> field = fieldControlMap.get(fieldTag.getString("fieldName"));
            if (field != null)
                field.afterWriteNBT(fieldTag);
        }
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag smartNBT)
    {
        final ListTag fieldTagList = smartNBT.getList("field", Tag.TAG_COMPOUND);
        if (fieldTagList.isEmpty())
            return;
        for (int i = 0; i < fieldTagList.size(); i++) {
            final CompoundTag fieldTag = fieldTagList.getCompound(i);
            final IField<?> field = fieldControlMap.get(fieldTag.getString("fieldName"));
            if (field != null)
                field.readNBT(fieldTag);
        }
        container.setChanged();
    }

    @Nonnull
    @Override
    public FieldController copy()
    {
        return new FieldController(container, values.stream().<IField<?>>map(IField::copy).collect(Collectors.toList()));
    }

    @Override
    public void receiveNBT(@Nonnull final CompoundTag compoundTag)
    {
        final String fieldName = compoundTag.contains("fieldName") ? compoundTag.getString("fieldName") : null;
        final IField<?> field = fieldName != null ? fieldControlMap.get(fieldName) : null;
        if (field != null) {
            field.receiveNBT(compoundTag);
            container.setChanged();
        }
    }
}