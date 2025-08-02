package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EvilBlockHandler {

    private static final ConcurrentHashMap<UUID, Boolean> playerHasEvilBlock = new ConcurrentHashMap<>();
    private static int colorTick = 0; // 用于控制颜色变化的计数器

    @SubscribeEvent
    public static void onItemPickup(TickEvent.PlayerTickEvent event) {
        if (event.player.getInventory().contains(new ItemStack(ModBlockss.EVILBLOCK.get()))) {
            event.player.getPersistentData().putBoolean("hasEvilBlock", true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        colorTick++; // 每个客户端tick更新一次计数器
        if (colorTick > 100) { // 控制颜色变化的速度
            colorTick = 0;
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onTooltipRender(RenderTooltipEvent.GatherComponents event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            ItemStack stack = event.getItemStack();

            if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
                boolean hasEvilBlock = player.getPersistentData().getBoolean("hasEvilBlock");

                List<Either<FormattedText, TooltipComponent>> tooltipComponents = event.getTooltipElements();

                if (!hasEvilBlock) {
                    // 如果玩家没有获得 EvilBlock，则显示乱码并应用波浪效果和颜色变化
                    String obfuscatedName = applyWaveEffect("Mystery Ingot");
                    Component obfuscatedText = new TextComponent(obfuscatedName);
                    tooltipComponents.clear(); // 清空原始 tooltip 内容
                    tooltipComponents.add(Either.left(obfuscatedText)); // 添加波浪乱码文本
                } else if (hasEvilBlock && !tooltipComponents.isEmpty()) {
                    // 如果玩家已经获得 EvilBlock，则显示彩色正常名称
                    String coloredName = applyColorEffect(stack.getHoverName().getString());
                    Component coloredText = new TextComponent(coloredName);
                    tooltipComponents.set(0, Either.left(coloredText));
                }
            }
        }

        private static String applyWaveEffect(String text) {
            StringBuilder result = new StringBuilder();
            int textLength = text.length();
            for (int i = 0; i < textLength; i++) {
                char c = text.charAt(i);
                String colorCode = getColorCode(i, textLength);
                result.append(colorCode).append("§k").append(c);
            }
            return result.toString() + "§r";
        }

        private static String applyColorEffect(String text) {
            StringBuilder result = new StringBuilder();
            int textLength = text.length();
            for (int i = 0; i < textLength; i++) {
                char c = text.charAt(i);
                String colorCode = getColorCode(i, textLength);
                result.append(colorCode).append(c);
            }
            return result.toString() + "§r";
        }

        private static String getColorCode(int index, int totalLength) {
            // 根据字符的位置返回不同的颜色代码，形成波浪效果
            int wavePosition = (colorTick + index) % totalLength;
            int colorIndex = wavePosition % 14;
            switch (colorIndex) {
                case 0: return "§c"; // 红色
                case 1: return "§6"; // 橙色
                case 2: return "§e"; // 黄色
                case 3: return "§a"; // 绿色
                case 4: return "§b"; // 青色
                case 5: return "§d"; // 粉色
                case 6: return "§1"; // 深蓝色
                case 7: return "§2"; // 深绿色
                case 8: return "§3"; // 青蓝色
                case 9: return "§4"; // 深红色
                case 10: return "§5"; // 紫色
                case 11: return "§7"; // 灰色
                case 12: return "§8"; // 深灰色
                case 13: return "§9"; // 淡蓝色
                default: return "§f"; // 白色，作为默认颜色
            }
        }
    }
}
