package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RecipeTransferPacket {
    private final ResourceLocation recipeId;

    public RecipeTransferPacket(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public RecipeTransferPacket(FriendlyByteBuf buf) {
        this.recipeId = buf.readResourceLocation();
    }

    public static void encode(RecipeTransferPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.recipeId);
    }

    public static RecipeTransferPacket decode(FriendlyByteBuf buf) {
        return new RecipeTransferPacket(buf);
    }

    public static void handle(RecipeTransferPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof StarlitSanctumMenu menu)) {
                return;
            }

            var recipeOpt = player.level.getRecipeManager().byKey(packet.recipeId);

            if (recipeOpt.isEmpty() || !(recipeOpt.get() instanceof StarlitSanctumRecipe recipe)) {
                return;
            }

            List<TransferItem> requiredItems = new ArrayList<>();

            var patternGroups = recipe.getPatternGroups();
            int[] blockStarts = {0, 9, 18};

            for (int blockIndex = 0; blockIndex < 3; blockIndex++) {
                var pattern = patternGroups.get(blockIndex);
                int startCol = blockStarts[blockIndex];

                for (int row = 0; row < 18; row++) {
                    for (int col = 0; col < 9; col++) {
                        int patternIndex = row * 9 + col;
                        int slotIndex = row * 27 + (startCol + col);

                        if (patternIndex < pattern.size()) {
                            var ingredient = pattern.get(patternIndex);
                            if (!ingredient.isEmpty()) {
                                requiredItems.add(new TransferItem(slotIndex, ingredient, 1));
                            }
                        }
                    }
                }
            }

            if (!recipe.getLeftInput().isEmpty()) {
                requiredItems.add(new TransferItem(486, recipe.getLeftInput(), recipe.getLeftInputCount()));
            }
            if (!recipe.getRightInput().isEmpty()) {
                requiredItems.add(new TransferItem(487, recipe.getRightInput(), recipe.getRightInputCount()));
            }

            for (int i = 0; i < 488; i++) {
                Slot slot = menu.getSlot(i);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty()) {
                    if (!player.getInventory().add(slotStack)) {
                        player.drop(slotStack, false);
                    }
                    slot.set(ItemStack.EMPTY);
                }
            }
            for (TransferItem item : requiredItems) {
                ItemStack stack = findAndExtractItem(player, item.ingredient, item.count);
                if (!stack.isEmpty()) {
                    if (item.slotIndex == 486 && !stack.is(StarlitSanctumTile.STARLIT)) {
                        player.getInventory().add(stack);
                        continue;
                    }

                    Slot slot = menu.getSlot(item.slotIndex);
                    slot.set(stack);
                }
            }

            menu.broadcastChanges();
        });
        ctx.get().setPacketHandled(true);
    }

    private static ItemStack findAndExtractItem(ServerPlayer player,
                                                Ingredient ingredient,
                                                int count) {
        var inventory = player.getInventory();
        int remaining = count;
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (ingredient.test(stack)) {
                if (result.isEmpty()) {
                    result = stack.copy();
                    result.setCount(0);
                }

                int extractCount = Math.min(remaining, stack.getCount());
                result.grow(extractCount);
                stack.shrink(extractCount);
                remaining -= extractCount;

                if (stack.isEmpty()) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        return result;
    }

    private static class TransferItem {
        final int slotIndex;
        final Ingredient ingredient;
        final int count;

        TransferItem(int slotIndex, Ingredient ingredient, int count) {
            this.slotIndex = slotIndex;
            this.ingredient = ingredient;
            this.count = count;
        }
    }
}