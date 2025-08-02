package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IController;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MatchingController implements IController<MatchingController, AbstractMatching<?>>
{
    private final Int2ObjectMap<AbstractMatching<?>> matchingControlMap = new Int2ObjectOpenHashMap<>();
    private final Collection<AbstractMatching<?>> values = matchingControlMap.values();
    private final Container container;

    public MatchingController(@Nonnull final Container container)
    {
        this.container = container;
    }

    public MatchingController(@Nonnull final Container container, final AbstractMatching<?>... matchings)
    {
        this(container);
        add(matchings);
    }

    public MatchingController(@Nonnull final Container container, @Nonnull final List<AbstractMatching<?>> matchingList)
    {
        this(container);
        matchingList.forEach(this::add);
    }

    public MatchingController(@Nonnull final Container container, @Nonnull final Int2ObjectMap<AbstractMatching<?>> matchingMap)
    {
        this(container);
        matchingControlMap.putAll(matchingMap);
    }

    public void add(@Nonnull final AbstractMatching<?>... matchings)
    {
        for (final AbstractMatching<?> matching : matchings)
            matchingControlMap.put(matching.getNumber(), matching);
    }

    public Collection<AbstractMatching<?>> getInstances()
    {
        return values;
    }

    public AbstractMatching<?> getMatching(final int number)
    {
        return matchingControlMap.get(number);
    }

    @Nonnull
    @Override
    public List<AbstractMatching<?>> compareContents(@Nonnull final MatchingController otherMatchingController)
    {
        final List<AbstractMatching<?>> differences = new ArrayList<>();
        for (final AbstractMatching<?> matching : values) {
            final AbstractMatching<?> otherMatching = otherMatchingController.matchingControlMap.get(matching.hashCode());
            if (!matching.equals(otherMatching))
                differences.add(matching);
        }
        return differences;
    }

    @Override
    public void addListener(final int windowId, @Nonnull final WContainer<?> wContainer, @Nonnull final ServerPlayer serverPlayer)
    {
        NetworkHelper.addMatchingListener(windowId, wContainer, serverPlayer);
    }

    @Override
    public void detectAndSendChanges(final int windowId, @Nonnull final WContainer<?> wContainer)
    {
        NetworkHelper.detectAndSendMatchingChanges(windowId, wContainer);
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT()
    {
        final CompoundTag matchingNBT = new CompoundTag();
        final ListTag matchingTagList = new ListTag();
        matchingNBT.put("matching", matchingTagList);
        values.forEach(matching -> matchingTagList.add(matching.writeNBT()));
        return matchingNBT;
    }

    @Override
    public void afterWriteNBT(@Nonnull final CompoundTag smartNBT)
    {
        final ListTag matchingTagList = smartNBT.getList("matching", Tag.TAG_COMPOUND);
        if (matchingTagList.isEmpty())
            return;
        for (int i = 0; i < matchingTagList.size(); i++) {
            final CompoundTag matchingTag = matchingTagList.getCompound(i);
            final AbstractMatching<?> matching = matchingControlMap.get(matchingTag.getInt("number"));
            if (matching != null)
                matching.afterWriteNBT(matchingTag);
        }
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag smartNBT)
    {
        final ListTag matchingTagList = smartNBT.getList("matching", Tag.TAG_COMPOUND);
        if (matchingTagList.isEmpty())
            return;
        for (int i = 0; i < matchingTagList.size(); i++) {
            final CompoundTag matchingTag = matchingTagList.getCompound(i);
            final AbstractMatching<?> matching = matchingControlMap.get(matchingTag.getInt("number"));
            if (matching != null)
                matching.readNBT(matchingTag);
        }
        container.setChanged();
    }

    @Nonnull
    @Override
    public MatchingController copy()
    {
        return new MatchingController(container, values.stream().map(AbstractMatching::copy).collect(Collectors.toList()));
    }
}