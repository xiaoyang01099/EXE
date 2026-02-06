package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;


import net.minecraft.world.entity.player.Player;

public class UtilExperience {

    public static double getExpTotal(Player player) {
        int level = player.experienceLevel;
        double totalExp = getXpForLevel(level);
        double progress = Math.round(player.getXpNeededForNextLevel() * player.experienceProgress);
        totalExp += (int) progress;
        return totalExp;
    }

    public static void drainExp(Player player, float amount) {
        double totalExp = getExpTotal(player);
        if (totalExp - amount < 0) {
            return;
        }
        setXp(player, (int) (totalExp - amount));
    }

    public static int getXpToGainLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    public static int getXpForLevel(int level) {
        if (level <= 15) {
            return level * level + 6 * level;
        } else if (level <= 30) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    public static int getLevelForXp(int xp) {
        int level = 0;
        while (getXpForLevel(level) < xp) {
            level++;
        }
        return level - 1;
    }

    public static void setXp(Player player, int xp) {
        player.totalExperience = xp;
        player.experienceLevel = getLevelForXp(xp);

        int xpForCurrentLevel = getXpForLevel(player.experienceLevel);
        player.experienceProgress = (float) (xp - xpForCurrentLevel) / (float) player.getXpNeededForNextLevel();
    }
}