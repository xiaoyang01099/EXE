package net.xiaoyang010.ex_enigmaticlegacy.Compat.Jade;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;

import java.util.ArrayList;
import java.util.List;

public class ExEWailaTooltipEventHandler {

    public static final ExEWailaTooltipEventHandler INSTANCE = new ExEWailaTooltipEventHandler();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onWailaTooltip(WailaTooltipEvent event) {
        Accessor<?> accessor = event.getAccessor();
        if (accessor == null) return;
        if (!(accessor instanceof EntityAccessor entityAccessor)) return;
        if (!(entityAccessor.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        ITooltip tooltip = event.getTooltip();
        int size = tooltip.size();

        List<Integer> replaceIndices = new ArrayList<>();
        List<IWaveName.WaveStyle> replaceStyles = new ArrayList<>();
        List<String> replaceTexts = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<IElement> leftElements = tooltip.get(i, IElement.Align.LEFT);
            if (leftElements == null || leftElements.isEmpty()) continue;

            for (IElement element : leftElements) {
                if (element instanceof WaveNameElement) break;

                Component msg = element.getCachedMessage();
                if (msg == null) continue;

                IWaveName.WaveStyle waveStyle = extractWaveStyle(msg);
                if (waveStyle != null) {
                    String rawText = ChatFormatting.stripFormatting(msg.getString());
                    if (rawText != null && !rawText.isEmpty()) {
                        replaceIndices.add(i);
                        replaceStyles.add(waveStyle);
                        replaceTexts.add(rawText);
                    }
                    break;
                }
            }
        }

        if (replaceIndices.isEmpty()) return;

        List<LineData> allLines = new ArrayList<>();
        int currentSize = tooltip.size();
        for (int i = 0; i < currentSize; i++) {
            List<IElement> leftElements = tooltip.get(i, IElement.Align.LEFT);
            List<IElement> rightElements = tooltip.get(i, IElement.Align.RIGHT);
            allLines.add(new LineData(
                    leftElements != null ? new ArrayList<>(leftElements) : new ArrayList<>(),
                    rightElements != null ? new ArrayList<>(rightElements) : new ArrayList<>()
            ));
        }

        List<ResourceLocation> allTags = new ArrayList<>();
        for (int i = 0; i < currentSize; i++) {
            List<IElement> left = tooltip.get(i, IElement.Align.LEFT);
            if (left != null) {
                for (IElement el : left) {
                    ResourceLocation tag = el.getTag();
                    if (tag != null && !allTags.contains(tag)) {
                        allTags.add(tag);
                    }
                }
            }
        }

        for (ResourceLocation tag : allTags) {
            tooltip.remove(tag);
        }

        tooltip.clear();

        for (int i = 0; i < allLines.size(); i++) {
            int replaceIdx = replaceIndices.indexOf(i);
            if (replaceIdx >= 0) {
                WaveNameElement waveElement = new WaveNameElement(
                        stack,
                        replaceStyles.get(replaceIdx),
                        replaceTexts.get(replaceIdx)
                );
                ResourceLocation tag = new ResourceLocation(
                        ExEnigmaticlegacyMod.MODID, "wave_body_" + i);
                waveElement.tag(tag);
                tooltip.add(waveElement);
            } else {
                LineData line = allLines.get(i);
                if (!line.left.isEmpty()) {
                    tooltip.add(line.left.get(0));
                    for (int j = 1; j < line.left.size(); j++) {
                        tooltip.append(line.left.get(j));
                    }
                }
                for (IElement rightEl : line.right) {
                    tooltip.append(rightEl);
                }
            }
        }
    }

    private static IWaveName.WaveStyle extractWaveStyle(Component component) {
        IWaveName.WaveStyle style = checkStyleColor(component.getStyle());
        if (style != null) return style;

        for (Component sibling : component.getSiblings()) {
            style = checkStyleColor(sibling.getStyle());
            if (style != null) return style;
            style = extractWaveStyle(sibling);
            if (style != null) return style;
        }
        return null;
    }

    private static IWaveName.WaveStyle checkStyleColor(Style style) {
        if (style == null) return null;
        TextColor color = style.getColor();
        if (color == null) return null;

        int value = color.getValue();
        if (value == ModRarities.MARK_GLITCH)       return IWaveName.WaveStyle.GLITCH;
        if (value == ModRarities.MARK_WAVE_HOLY)    return IWaveName.WaveStyle.HOLY;
        if (value == ModRarities.MARK_WAVE_FALLEN)  return IWaveName.WaveStyle.FALLEN;
        if (value == ModRarities.MARK_WAVE_MIRACLE) return IWaveName.WaveStyle.MIRACLE;
        if (value == ModRarities.MARK_TEAR)         return IWaveName.WaveStyle.TEAR;
        if (value == ModRarities.MARK_DISSOLVE)     return IWaveName.WaveStyle.DISSOLVE;
        if (value == ModRarities.MARK_GLOW_STAR)    return IWaveName.WaveStyle.GLOW_STAR;
        if (value == ModRarities.MARK_RAINBOW)      return IWaveName.WaveStyle.RAINBOW;
        if (value == ModRarities.MARK_SHATTER)      return IWaveName.WaveStyle.SHATTER;
        return null;
    }

    private static class LineData {
        final List<IElement> left;
        final List<IElement> right;

        LineData(List<IElement> left, List<IElement> right) {
            this.left = left;
            this.right = right;
        }
    }
}