package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.server.level.ServerLevel;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class WeatherStone extends Item {

    private static final long[] TIME_POINTS = {0L, 6000L, 12000L, 18000L, 23000L, 3000L};
    private int timeIndex = 0;
    private int weatherIndex = 0; // 0:晴天, 1:雨天, 2:雷雨天

    public WeatherStone() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemstack) {
        return new ItemStack(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand); // 当前手持的物品

        // 确保玩家手中拿着的是 WeatherStone
        if (!level.isClientSide && stack.getItem() instanceof WeatherStone) { // 只在服务器端执行效果
            if (player.isShiftKeyDown()) {
                // Shift + 右键：切换天气
                cycleWeather(level);
            } else {
                // 普通右键：切换时间
                cycleTime(level);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void cycleTime(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            timeIndex = (timeIndex + 1) % TIME_POINTS.length; // 按顺序循环
            long newTime = TIME_POINTS[timeIndex];
            serverLevel.setDayTime(newTime); // 设置时间
        }
    }

    private void cycleWeather(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ServerLevelData levelData = (ServerLevelData) serverLevel.getLevelData();
            weatherIndex = (weatherIndex + 1) % 3; // 按顺序循环 (晴天, 雨天, 雷雨天)

            switch (weatherIndex) {
                case 0:
                    // 晴天
                    levelData.setRaining(false);
                    levelData.setThundering(false);
                    serverLevel.setWeatherParameters(12000, 0, false, false); // 设置晴天持续时间
                    break;
                case 1:
                    // 雨天
                    levelData.setRaining(true);
                    levelData.setThundering(false);
                    serverLevel.setWeatherParameters(0, 12000, true, false); // 设置雨天持续时间
                    break;
                case 2:
                    // 雷雨天
                    levelData.setRaining(true);
                    levelData.setThundering(true);
                    serverLevel.setWeatherParameters(0, 12000, true, true); // 设置雷雨天持续时间
                    break;
            }
        }
    }
}
