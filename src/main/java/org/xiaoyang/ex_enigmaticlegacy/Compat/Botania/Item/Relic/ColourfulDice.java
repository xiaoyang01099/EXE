package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModArmors;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.api.emc.INoEMCItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.Relic;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;
import vazkii.botania.common.lib.ResourceLocationHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ColourfulDice extends RelicItem implements INoEMCItem {
    private static final String NBT_OBTAINED_ITEMS = "ColourfulDiceObtained";

    public ColourfulDice(Properties props) {
        super(props);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<Relic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null) {
                @Override
                public boolean shouldDamageWrongPlayer() {
                    return false;
                }
            });
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                          @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    public static final Supplier<List<ItemStack>> COLOURFUL_RELIC_STACKS = Suppliers.memoize(() -> List.of(
            new ItemStack(ModItems.VOID_GRIMOIRE.get()),
            new ItemStack(ModItems.MANAITA.get()),
            new ItemStack(ModItems.GOLDEN_LAUREL.get()),
            new ItemStack(ModArmors.TERRO_RCROWN.get()),
            new ItemStack(ModItems.POCKET_WARDROBE.get()),
            new ItemStack(ModItems.ADMIN_CONTROLLER.get())
    ));

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && entity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                Relic relic = relicCap.orElse(null);
                if (relic != null) {
                    relic.tickBinding(player);
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            Relic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        } else {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                BotaniaSounds.diceOfFate, SoundSource.PLAYERS, 1F,
                0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        List<Integer> possible = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (!hasItemAlready(player, i)) {
                possible.add(i);
            }
        }

        if (!possible.isEmpty()) {
            int relicIdx = possible.get(world.random.nextInt(possible.size()));
            player.sendSystemMessage(Component.translatable("botaniamisc.CdiceRoll", relicIdx + 1)
                    .withStyle(ChatFormatting.DARK_GREEN));

            markItemAsObtained(player, relicIdx);

            var toGive = COLOURFUL_RELIC_STACKS.get().get(relicIdx).copy();
            return InteractionResultHolder.success(toGive);
        } else {
            int roll = world.random.nextInt(6) + 1;
            ResourceLocation tableId = ResourceLocationHelper.prefix("dice/roll_" + roll);

            ServerLevel serverLevel = (ServerLevel) world;

            LootTable table = serverLevel.getServer()
                    .getLootData()
                    .getLootTable(tableId);

            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.GIFT);

            List<ItemStack> generated = table.getRandomItems(lootParams);
            for (ItemStack drop : generated) {
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            }

            String langKey = generated.isEmpty()
                    ? "botaniamisc.CdudDiceRoll"
                    : "botaniamisc.CdiceRoll";
            player.sendSystemMessage(Component.translatable(langKey, roll)
                    .withStyle(ChatFormatting.DARK_GREEN));

            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flags) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        tooltip.add(Component.translatable(""));
        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.colourful_dice.tooltip.line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.colourful_dice.tooltip.line2")
                .withStyle(ChatFormatting.GRAY));

        TooltipHandler.addOnShift(tooltip, () -> {
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.colourful_dice.tooltip.rewards")
                    .withStyle(ChatFormatting.GOLD));

            for (int i = 0; i < COLOURFUL_RELIC_STACKS.get().size(); i++) {
                ItemStack rewardStack = COLOURFUL_RELIC_STACKS.get().get(i);
                tooltip.add(Component.translatable("• ").append(rewardStack.getHoverName())
                        .withStyle(ChatFormatting.YELLOW));
            }

            String name = stack.getDescriptionId() + ".poem";
            for (int i = 0; i < 4; i++) {
                tooltip.add(Component.translatable(name + i)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        });
    }

    private boolean hasItemAlready(Player player, int relicId) {
        if (relicId < 0 || relicId >= 6 || !(player instanceof ServerPlayer)) {
            return true;
        }

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound("ex_enigmaticlegacy");

        return modData.getBoolean(NBT_OBTAINED_ITEMS + "_" + relicId);
    }

    private void markItemAsObtained(Player player, int relicId) {
        if (relicId < 0 || relicId >= 6 || !(player instanceof ServerPlayer)) {
            return;
        }

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound("ex_enigmaticlegacy");

        modData.putBoolean(NBT_OBTAINED_ITEMS + "_" + relicId, true);
        persistentData.put("ex_enigmaticlegacy", modData);
    }
}