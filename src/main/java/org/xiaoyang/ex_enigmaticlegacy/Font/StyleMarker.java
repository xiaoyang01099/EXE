package org.xiaoyang.ex_enigmaticlegacy.Font;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class StyleMarker {
    private static final Map<Integer, IWaveName.WaveStyle> MARK_MAP = Map.of(
            ModRarities.MARK_WAVE_HOLY,    IWaveName.WaveStyle.HOLY,
            ModRarities.MARK_WAVE_FALLEN,  IWaveName.WaveStyle.FALLEN,
            ModRarities.MARK_WAVE_MIRACLE, IWaveName.WaveStyle.MIRACLE,
            ModRarities.MARK_GLITCH,       IWaveName.WaveStyle.GLITCH,
            ModRarities.MARK_TEAR,         IWaveName.WaveStyle.TEAR,
            ModRarities.MARK_DISSOLVE,     IWaveName.WaveStyle.DISSOLVE,
            ModRarities.MARK_GLOW_STAR,    IWaveName.WaveStyle.GLOW_STAR,
            ModRarities.MARK_RAINBOW,      IWaveName.WaveStyle.RAINBOW,
            ModRarities.MARK_SHATTER,      IWaveName.WaveStyle.SHATTER
    );

    public static Style waveHoly()    { return mark(ModRarities.MARK_WAVE_HOLY);    }
    public static Style waveFallen()  { return mark(ModRarities.MARK_WAVE_FALLEN);  }
    public static Style waveMiracle() { return mark(ModRarities.MARK_WAVE_MIRACLE); }
    public static Style glitch()      { return mark(ModRarities.MARK_GLITCH);       }
    public static Style tear()        { return mark(ModRarities.MARK_TEAR);         }
    public static Style dissolve()    { return mark(ModRarities.MARK_DISSOLVE);     }
    public static Style glowStar()    { return mark(ModRarities.MARK_GLOW_STAR);    }
    public static Style rainbow()     { return mark(ModRarities.MARK_RAINBOW);      }
    public static Style shatter()     { return mark(ModRarities.MARK_SHATTER);      }

    private static Style mark(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }

    public static IWaveName.WaveStyle extractStyle(FormattedText text) {
        AtomicReference<IWaveName.WaveStyle> found = new AtomicReference<>(null);
        text.visit((style, str) -> {
            if (str.isEmpty()) return Optional.empty();
            if (style.getColor() == null) return Optional.of("stop");
            IWaveName.WaveStyle ws = MARK_MAP.get(style.getColor().getValue());
            if (ws != null) found.set(ws);
            return Optional.of("stop");
        }, Style.EMPTY);
        return found.get();
    }
}