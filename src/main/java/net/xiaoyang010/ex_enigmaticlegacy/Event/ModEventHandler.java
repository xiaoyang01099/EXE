package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityPotato;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.BelieverTile;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import vazkii.botania.common.block.decor.BlockTinyPotato;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
            event.getToolTip().add(new TranslatableComponent("item.ex_enigmaticlegacy.prismaticradianceingot.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void rightBlock(RightClickBlock event) {
        LivingEntity living = event.getEntityLiving();
        if (living instanceof Player player) {
            BlockHitResult hitVec = event.getHitVec();
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) {
                BlockPos pos = hitVec.getBlockPos();
                Level world = player.getLevel();
                BlockState state = world.getBlockState(pos);

                // 检查是普通小土豆还是无尽小土豆
                boolean isInfinityPotato = state.getBlock() instanceof InfinityPotato;
                boolean isTinyPotato = state.getBlock() instanceof BlockTinyPotato;

                // 如果点击的是其中一种土豆
                if (isInfinityPotato || isTinyPotato) {
                    int range = 4;
                    BlockPos pos1 = pos.offset(-range, 0, -range);
                    BlockPos pos2 = pos.offset(range, 1, range);
                    for (BlockPos blockPos : BlockPos.betweenClosed(pos1, pos2)) {
                        BlockEntity entity = world.getBlockEntity(blockPos);
                        if (entity instanceof BelieverTile believer) {
                            believer.addRightMana(isInfinityPotato); // 传递土豆类型
                        }
                    }
                }
            }
        }
    }
}

