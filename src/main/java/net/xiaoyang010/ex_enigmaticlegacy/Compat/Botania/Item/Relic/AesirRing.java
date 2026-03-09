package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.item.IWireframeCoordinateListProvider;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.relic.ItemOdinRing;
import vazkii.botania.common.item.relic.ItemRelicBauble;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class AesirRing extends ItemRelicBauble implements IWireframeCoordinateListProvider {

    public AesirRing(Properties props) {
        super(props);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @SubscribeEvent
    public void onDropped(ItemTossEvent event) {
        ItemEntity entityItem = (ItemEntity) event.getEntity();
        if (entityItem != null && !entityItem.getItem().isEmpty() && !entityItem.level.isClientSide) {
            ItemStack stack = entityItem.getItem();
            if (stack.getItem() == this) {
                entityItem.discard();

                var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
                UUID user = relic != null ? relic.getSoulbindUUID() : null;

                for (Item item : new Item[]{ModItems.thorRing, ModItems.lokiRing, ModItems.odinRing}) {
                    ItemStack newStack = new ItemStack(item);

                    if (user != null) {
                        var newRelic = IXplatAbstractions.INSTANCE.findRelic(newStack);
                        if (newRelic != null) {
                            newRelic.bindToUUID(user);
                        }
                    }

                    ItemEntity newEntity = new ItemEntity(
                            entityItem.level,
                            entityItem.getX(),
                            entityItem.getY(),
                            entityItem.getZ(),
                            newStack
                    );

                    newEntity.setDeltaMovement(entityItem.getDeltaMovement());

                    newEntity.setPickUpDelay(entityItem.hasPickUpDelay() ? 40 : 10);

                    newEntity.age = entityItem.age;

                    entityItem.level.addFreshEntity(newEntity);
                }
            }
        }
    }

    @Override
    public void onValidPlayerWornTick(Player player) {
        if (ModItems.odinRing instanceof ItemOdinRing odinRing) {
            odinRing.onValidPlayerWornTick(player);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.MAX_HEALTH,
                new AttributeModifier(getBaubleUUID(stack), "Aesir Ring", 20, AttributeModifier.Operation.ADDITION));
        return attributes;
    }

    @Nullable
    @Override
    public List<BlockPos> getWireframesToDraw(Player player, ItemStack stack) {
        if (ModItems.lokiRing instanceof IWireframeCoordinateListProvider provider) {
            return provider.getWireframesToDraw(player, stack);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockPos getSourceWireframe(Player player, ItemStack stack) {
        if (ModItems.lokiRing instanceof IWireframeCoordinateListProvider provider) {
            return provider.getSourceWireframe(player, stack);
        }
        return null;
    }

}