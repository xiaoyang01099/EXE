package org.xiaoyang.ex_enigmaticlegacy.Font.Jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Font.ModRarities;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.ui.TextElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExEWailaTooltipEventHandler implements JadeTooltipCollectedCallback {

    public static final ExEWailaTooltipEventHandler INSTANCE = new ExEWailaTooltipEventHandler();

    @Override
    public void onTooltipCollected(ITooltip tooltip, Accessor<?> accessor) {
        if (accessor == null) return;
        if (!(accessor instanceof EntityAccessor entityAccessor)) return;
        if (!(entityAccessor.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;
        if (!(tooltip instanceof Tooltip tooltipImpl)) return;

        List<Component> itemTooltipLines;
        try {
            itemTooltipLines = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
        } catch (Exception e) {
            return;
        }
        if (itemTooltipLines == null || itemTooltipLines.isEmpty()) return;

        Map<String, IWaveName.WaveStyle> markedTextMap = new HashMap<>();
        Map<String, Component> originalComponentMap = new HashMap<>();

        for (Component line : itemTooltipLines) {
            String rawText = ChatFormatting.stripFormatting(line.getString());
            if (rawText == null || rawText.isEmpty()) continue;
            rawText = rawText.trim();

            IWaveName.WaveStyle waveStyle = extractWaveStyle(line);
            if (waveStyle != null) {
                markedTextMap.put(rawText, waveStyle);
            }

            if (!originalComponentMap.containsKey(rawText)) {
                originalComponentMap.put(rawText, line);
            }
        }

        List<Tooltip.Line> lines = tooltipImpl.lines;
        int size = lines.size();

        for (int i = 0; i < size; i++) {
            Tooltip.Line line = lines.get(i);
            List<IElement> leftElements = line.getAlignedElements(IElement.Align.LEFT);
            if (leftElements == null || leftElements.isEmpty()) continue;

            boolean alreadyCustom = false;
            for (IElement el : leftElements) {
                if (el instanceof WaveNameElement || el instanceof ColoredTextElement) {
                    alreadyCustom = true;
                    break;
                }
            }
            if (alreadyCustom) continue;

            StringBuilder lineTextBuilder = new StringBuilder();
            FormattedText originalFormattedText = null;

            for (IElement element : leftElements) {
                if (originalFormattedText == null && element instanceof TextElement textElement) {
                    originalFormattedText = textElement.text;
                }
                String msg = element.getCachedMessage();
                if (msg != null && !msg.isEmpty()) {
                    String stripped = ChatFormatting.stripFormatting(msg);
                    if (stripped != null) {
                        lineTextBuilder.append(stripped);
                    }
                }
            }
            String rawText = lineTextBuilder.toString().trim();
            if (rawText.isEmpty()) continue;

            ResourceLocation originalTag = null;
            for (IElement el : leftElements) {
                if (el.getTag() != null) {
                    originalTag = el.getTag();
                    break;
                }
            }

            IWaveName.WaveStyle waveStyle = markedTextMap.get(rawText);
            if (waveStyle != null) {
                WaveNameElement waveElement = new WaveNameElement(stack, waveStyle, rawText);
                ResourceLocation tag = new ResourceLocation(Exe.MODID, "wave_line_" + i);
                waveElement.tag(tag);

                leftElements.clear();
                leftElements.add(waveElement);

                List<IElement> rightElements = line.getAlignedElements(IElement.Align.RIGHT);
                if (rightElements != null) {
                    rightElements.clear();
                }
                continue;
            }

            Component originalComponent = originalComponentMap.get(rawText);
            if (originalComponent != null) {
                ColoredTextElement coloredElement = new ColoredTextElement(originalComponent);
                if (originalTag != null) {
                    coloredElement.tag(originalTag);
                }

                leftElements.clear();
                leftElements.add(coloredElement);
                continue;
            }

            if (originalFormattedText != null) {
                int color = extractFormattedTextColor(originalFormattedText);
                if (color != 0) {
                    ColoredTextElement coloredElement = new ColoredTextElement(originalFormattedText, color);
                    if (originalTag != null) {
                        coloredElement.tag(originalTag);
                    }

                    leftElements.clear();
                    leftElements.add(coloredElement);
                }
            }
        }
    }

    private static int extractFormattedTextColor(FormattedText text) {
        if (text instanceof Component component) {
            if (component.getStyle() != null && component.getStyle().getColor() != null) {
                return 0xFF000000 | component.getStyle().getColor().getValue();
            }
            for (Component sibling : component.getSiblings()) {
                if (sibling.getStyle() != null && sibling.getStyle().getColor() != null) {
                    return 0xFF000000 | sibling.getStyle().getColor().getValue();
                }
            }
        }
        return 0xFFFFFFFF;
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
}