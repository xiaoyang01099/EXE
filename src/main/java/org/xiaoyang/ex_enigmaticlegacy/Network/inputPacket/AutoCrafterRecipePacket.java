package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import com.yuo.endless.Recipe.EndlessRecipes;
import com.yuo.endless.Recipe.ExtremeCraftRecipe;
import com.yuo.endless.Recipe.ExtremeCraftShapeRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

import java.util.function.Supplier;

public class AutoCrafterRecipePacket {
    private final ResourceLocation recipeId;
    private final BlockPos pos;

    public AutoCrafterRecipePacket(FriendlyByteBuf buffer) {
        recipeId = buffer.readResourceLocation();
        pos = buffer.readBlockPos();
    }

    public AutoCrafterRecipePacket(ResourceLocation recipeId, BlockPos pos) {
        this.recipeId = recipeId;
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeId);
        buf.writeBlockPos(pos);
    }

    public static void handler(AutoCrafterRecipePacket msg, Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            BlockEntity block = level.getBlockEntity(msg.pos);

            if (!(block instanceof TileEntityExtremeAutoCrafter autoCrafter)) return;

            if (ResourceLocation.tryParse("null").toString().equals(msg.recipeId.toString())) {
                autoCrafter.setRecipe(null);
                autoCrafter.setChanged();
                return;
            }

            for (ExtremeCraftRecipe recipe : level.getRecipeManager()
                    .getAllRecipesFor(EndlessRecipes.EXTREME_CRAFT_RECIPE.get())) {
                if (recipe.getId().equals(msg.recipeId)) {
                    autoCrafter.setRecipe(recipe);
                    autoCrafter.setChanged();
                    return;
                }
            }

            for (ExtremeCraftShapeRecipe recipe : level.getRecipeManager()
                    .getAllRecipesFor(EndlessRecipes.EXTREME_CRAFT_SHAPE_RECIPE.get())) {
                if (recipe.getId().equals(msg.recipeId)) {
                    autoCrafter.setRecipe(recipe);
                    autoCrafter.setChanged();
                    return;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}