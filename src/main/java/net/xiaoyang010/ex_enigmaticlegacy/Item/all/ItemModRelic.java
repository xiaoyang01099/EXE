package net.xiaoyang010.ex_enigmaticlegacy.Item.all;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import vazkii.botania.common.item.relic.ItemRelic;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemModRelic extends ItemRelic {
    private final String relicName;

    public ItemModRelic(String name, Properties properties) {
        super(properties.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).setNoRepair());
        this.relicName = name;
    }

    // 便利构造函数，使用默认属性
    public ItemModRelic(String name) {
        this(name, new Properties().stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        String translationKey = this.getDescriptionId(stack);
        return EComponent.translatable(translationKey);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        // 父类已经处理了圣物绑定相关的逻辑
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
        if (Screen.hasShiftDown()) {
            String bind = getSoulbindUsernameS(stack, world);
            if (bind.isEmpty()) {
                addStringToTooltip(EComponent.translatable("botaniamisc.relicUnbound"), tooltip);
            } else {
                addStringToTooltip(EComponent.translatable("botaniamisc.relicSoulbound", bind), tooltip);
            }
        } else {
            addStringToTooltip(EComponent.translatable("botaniamisc.shiftinfo"), tooltip);
        }

        super.appendHoverText(stack, world, tooltip, flags);
    }

    public void addStringToTooltip(Component component, List<Component> tooltip) {
        if (component instanceof MutableComponent mutable) {
            String text = component.getString();
            if (text.contains("&")) {
                // 将 & 颜色代码转换为 § 颜色代码
                String converted = text.replaceAll("&", "§");
                tooltip.add(EComponent.literal(converted));
                return;
            }
        }
        tooltip.add(component);
    }

    public static String getSoulbindUsernameS(ItemStack stack, Level world) {
        var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic != null) {
            UUID uuid = relic.getSoulbindUUID();
            if (uuid != null) {
                return getPlayerNameFromUUID(uuid, world);
            }
        }
        return "";
    }

    public static String getSoulbindUsernameS(ItemStack stack) {
        return getSoulbindUsernameS(stack, null);
    }

    private static String getPlayerNameFromUUID(UUID uuid, Level world) {
        if (world != null && !world.isClientSide && world instanceof ServerLevel serverLevel) {
            // 服务端：尝试从游戏配置文件缓存获取玩家名称
            try {
                GameProfileCache profileCache = serverLevel.getServer().getProfileCache();
                if (profileCache != null) {
                    Optional<String> nameOpt = profileCache.get(uuid).map(profile -> profile.getName());
                    if (nameOpt.isPresent()) {
                        return nameOpt.get();
                    }
                }
            } catch (Exception e) {
            }
        }

        return uuid.toString().substring(0, 8) + "...";
    }

    public static boolean isRightPlayer(Player player, ItemStack stack) {
        var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic != null) {
            return relic.isRightPlayer(player);
        }
        return true;
    }

    public String getRelicName() {
        return this.relicName;
    }
}