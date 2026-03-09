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
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).fireResistant().rarity(Rarity.EPIC));
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
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && stack.getItem() instanceof WeatherStone) {
            if (player.isShiftKeyDown()) {
                cycleWeather(level);
            } else {
                cycleTime(level);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void cycleTime(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            timeIndex = (timeIndex + 1) % TIME_POINTS.length;
            long newTime = TIME_POINTS[timeIndex];
            serverLevel.setDayTime(newTime);
        }
    }

    private void cycleWeather(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ServerLevelData levelData = (ServerLevelData) serverLevel.getLevelData();
            weatherIndex = (weatherIndex + 1) % 3;

            switch (weatherIndex) {
                case 0:
                    levelData.setRaining(false);
                    levelData.setThundering(false);
                    serverLevel.setWeatherParameters(12000, 0, false, false); // 设置晴天持续时间
                    break;
                case 1:
                    levelData.setRaining(true);
                    levelData.setThundering(false);
                    serverLevel.setWeatherParameters(0, 12000, true, false); // 设置雨天持续时间
                    break;
                case 2:
                    levelData.setRaining(true);
                    levelData.setThundering(true);
                    serverLevel.setWeatherParameters(0, 12000, true, true); // 设置雷雨天持续时间
                    break;
            }
        }
    }
}
