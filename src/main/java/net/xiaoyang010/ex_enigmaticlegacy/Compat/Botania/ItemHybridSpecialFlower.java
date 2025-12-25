package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import vazkii.botania.xplat.BotaniaConfig;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemHybridSpecialFlower extends BlockItem {
    private static final TagKey<Item> HYBRID = ModTags.Items.HYBRID_SPECIAL_FLOWERS;

    public ItemHybridSpecialFlower(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (BotaniaConfig.client() != null) {
            if (world != null) {
                if (stack.is(HYBRID)) {
                    tooltip.add(new TranslatableComponent("ex_enigmaticlegacy.flowerType.hybrid").withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
                }
            }
        }
    }
}
