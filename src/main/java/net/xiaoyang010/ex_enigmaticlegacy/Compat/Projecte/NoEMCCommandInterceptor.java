package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoEMCCommandInterceptor {

    private static final Pattern PE_COMMAND_PATTERN = Pattern.compile(
            "^/projecte\\s+(setemc|removeemc)\\s+(\\S+)(?:\\s+(\\d+))?",
            Pattern.CASE_INSENSITIVE
    );

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        String commandLine = event.getParseResults().getReader().getString().trim();

        Matcher matcher = PE_COMMAND_PATTERN.matcher(commandLine);
        if (matcher.find()) {
            String subCommand = matcher.group(1).toLowerCase();
            String itemName = matcher.group(2);

            if (isItemForbidden(itemName)) {
                event.setCanceled(true);

                var source = event.getParseResults().getContext().getSource();
                Component errorMessage = EComponent.literal("§c无法对该物品执行EMC操作：该物品实现了INoEMCItem接口！")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF5555)));

                source.sendFailure(errorMessage);

                System.out.println("NoEMC: Blocked ProjectE command for forbidden item: " + itemName + " (command: " + subCommand + ")");
            }
        }
    }

    private static boolean isItemForbidden(String itemName) {
        try {
            String[] formats = {
                    itemName,
                    itemName.contains(":") ? itemName : "minecraft:" + itemName,
                    itemName.contains(":") ? itemName : "ex_enigmaticlegacy:" + itemName
            };

            for (String format : formats) {
                try {
                    var resourceLocation = new ResourceLocation(format);
                    var item = ForgeRegistries.ITEMS.getValue(resourceLocation);

                    if (item != null && EMCHelper.isEMCForbidden(item)) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
