package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class EComponent {
    public static TranslatableComponent translatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    public static TextComponent literal(String key) {
        return new TextComponent(key);
    }

    public static Component empty() {
        return TextComponent.EMPTY;
    }
}
