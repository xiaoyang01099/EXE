package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorText {

    public static int bc ;
    private static final ChatFormatting[] color ={ChatFormatting.RED,ChatFormatting.GOLD,ChatFormatting.YELLOW,ChatFormatting.GREEN,ChatFormatting.AQUA,ChatFormatting.LIGHT_PURPLE,ChatFormatting.BLUE};

    public static String formatting(String input, ChatFormatting[] colours, double delay) {
        StringBuilder sb = new StringBuilder(input.length() * 5);
        if (delay <= 0.0D)
            delay = 0.0001D;
        int offset = (int)Math.floor((System.currentTimeMillis() & 0x3FFFL) / delay) % colours.length;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(colours[(colours.length + i - offset) % colours.length].toString());
            sb.append(c);
            bc = i;
        }
        return sb.toString();
    }
    public static String formattinG(String input, ChatFormatting[] colours, double delay) {
        StringBuilder sb = new StringBuilder(input.length() * 5);
        if (delay <= 0.0D)
            delay = 0.0001D;
        int offset = (int)Math.floor((System.currentTimeMillis() & 0x3FFFL) / delay) % colours.length;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            sb.append(colours[new Random().nextInt(colours.length - 1)]);
            sb.append(c);
            bc = i;
        }
        return sb.toString();
    }
    static int colori=0,m=0;
    public static Integer formattinG(double delay) {
        if (m<=delay){
            m++;
            return 0xFF0f0f0f | colori;
        }else {
            m=0;
            colori = new Random().nextInt();
            return 0xFF0f0f0f | colori;
        }
    }



    public static String GetColor1(String input) {
        return formatting(input, color, 100.0D);
        //                  ^      ^     ^
        //               输入文本  颜色  延迟
    }
    public static String GetColor1I(String input) {
        return formattinG(input, color, 100.0D);
        //                  ^      ^     ^
        //               输入文本  颜色  延迟
    }
    public static String GetGreen(String input) {
        return formatting(input, new ChatFormatting[]{ChatFormatting.GREEN}, 100.0D);
        //                  ^      ^     ^
        //               输入文本  颜色  延迟
    }
    public static String getGray(String s){
        return formatting(s,new ChatFormatting[]{ChatFormatting.DARK_GRAY},100.0D);
    }
}
