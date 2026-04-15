package org.xiaoyang.ex_enigmaticlegacy.Font.Jade;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Font.ModRarities;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public class ItemEntityWaveNameProvider implements IEntityComponentProvider {
    public static final ItemEntityWaveNameProvider INSTANCE = new ItemEntityWaveNameProvider();
    private static final ResourceLocation UID = new ResourceLocation("ex_enigmaticlegacy", "item_wave_name");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return -5000;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        if (!ModRarities.shouldAnimate(stack)) return;

        IWaveName.WaveStyle style = resolveStyle(stack);
        if (style == null) return;

        String rawText = ChatFormatting.stripFormatting(stack.getHoverName().getString());
        if (rawText == null || rawText.isEmpty()) return;

        tooltip.remove(Identifiers.CORE_OBJECT_NAME);

        WaveNameElement element = new WaveNameElement(stack, style, rawText);
        element.tag(Identifiers.CORE_OBJECT_NAME);
        tooltip.add(0, element);
    }

    static IWaveName.WaveStyle resolveStyle(ItemStack stack) {
        if (stack.getItem() instanceof IWaveName wni) {
            return wni.getWaveStyle(stack);
        }
        return ModRarities.getStyle(stack);
    }
}