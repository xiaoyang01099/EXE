package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

import java.util.function.Supplier;

public class AutoCrafterRecipePacket {
    private final ResourceLocation recipeId;
    private final BlockPos pos;
    public AutoCrafterRecipePacket(FriendlyByteBuf buffer) {
        Minecraft instance = Minecraft.getInstance();
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
            ServerLevel level = player.getLevel();
            BlockEntity block = level.getBlockEntity(msg.pos);
            if (block instanceof TileEntityExtremeAutoCrafter autoCrafter){
                if (ResourceLocation.tryParse("null").toString().equals(msg.recipeId.toString())){
                    autoCrafter.setRecipe(null);
                    autoCrafter.setChanged();
                }else
                    for (ExtremeCraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get())) {
                        if (recipe.getId().toString().equals(msg.recipeId.toString())){
                            autoCrafter.setRecipe(recipe);
                            autoCrafter.setChanged();
                        }
                    }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
