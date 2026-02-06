package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.IManaConverter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 污染监测器
 * 用于检测魔力设备的污染等级
 */
public class ItemCorruptionDetector extends Item {

    public ItemCorruptionDetector(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity tile = level.getBlockEntity(pos);

        if (tile instanceof IManaConverter converter) {
            displayConverterInfo(player, converter);
            return InteractionResult.SUCCESS;
        } else if (tile instanceof TileCursedManaPool pool) {
            displayPoolInfo(player, pool);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }


    private void displayConverterInfo(Player player, IManaConverter converter) {
        int corruption = converter.getCorruptionLevel();
        ManaCorruptionManager.CorruptionLevel level = ManaCorruptionManager.CorruptionLevel.fromValue(corruption);
        float efficiency = converter.getConversionEfficiency() * 100;

        ChatFormatting color = getCorruptionColor(level);

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.converter_title")
                        .withStyle(ChatFormatting.GOLD),
                false
        );

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.corruption",
                        corruption, level.name())
                        .withStyle(color),
                false
        );

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.efficiency",
                        String.format("%.1f%%", efficiency))
                        .withStyle(efficiency > 70 ? ChatFormatting.GREEN : ChatFormatting.YELLOW),
                false
        );

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.status",
                        converter.isConverting() ? "运行中" : "空闲")
                        .withStyle(converter.isConverting() ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                false
        );
    }

    private void displayPoolInfo(Player player, TileCursedManaPool pool) {
        int cursedMana = pool.getCurrentCursedMana();
        int maxMana = pool.getMaxCursedMana();
        float percentage = (float) cursedMana / maxMana * 100;

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.pool_title")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                false
        );

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.mana",
                        cursedMana, maxMana, String.format("%.1f%%", percentage))
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );

        player.displayClientMessage(
                new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.color",
                        pool.getCursedColor().getName())
                        .withStyle(ChatFormatting.AQUA),
                false
        );
    }

    private ChatFormatting getCorruptionColor(ManaCorruptionManager.CorruptionLevel level) {
        switch (level) {
            case NONE: return ChatFormatting.GREEN;
            case LOW: return ChatFormatting.YELLOW;
            case MEDIUM: return ChatFormatting.GOLD;
            case HIGH: return ChatFormatting.RED;
            case EXTREME: return ChatFormatting.DARK_RED;
            default: return ChatFormatting.GRAY;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.corruption_detector.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
