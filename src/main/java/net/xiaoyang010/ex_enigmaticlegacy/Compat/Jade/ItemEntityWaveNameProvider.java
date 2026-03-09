package net.xiaoyang010.ex_enigmaticlegacy.Compat.Jade;

import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;

public class ItemEntityWaveNameProvider implements IEntityComponentProvider {

    public static final ItemEntityWaveNameProvider INSTANCE = new ItemEntityWaveNameProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        if (!ModRarities.shouldAnimate(stack)) return;

        IWaveName.WaveStyle style = resolveStyle(stack);

        String rawText = ChatFormatting.stripFormatting(stack.getHoverName().getString());
        if (rawText == null || rawText.isEmpty()) return;

        tooltip.remove(CorePlugin.TAG_OBJECT_NAME);

        WaveNameElement element = new WaveNameElement(stack, style, rawText);
        element.tag(CorePlugin.TAG_OBJECT_NAME);
        tooltip.add(0, element);
    }

    static IWaveName.WaveStyle resolveStyle(ItemStack stack) {
        if (stack.getItem() instanceof IWaveName wni) {
            return wni.getWaveStyle(stack);
        }
        Rarity r = stack.getRarity();
        if (r == ModRarities.HOLY)    return IWaveName.WaveStyle.HOLY;
        if (r == ModRarities.FALLEN)  return IWaveName.WaveStyle.FALLEN;
        if (r == ModRarities.MIRACLE) return IWaveName.WaveStyle.MIRACLE;
        return IWaveName.WaveStyle.RAINBOW;
    }
}