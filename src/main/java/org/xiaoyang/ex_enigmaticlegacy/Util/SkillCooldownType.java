package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.network.chat.Component;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;

import java.util.function.IntSupplier;

// SkillCooldownType 集中定义玩家技能冷却 key、名称和客户端冷却时间。
public enum SkillCooldownType {
    DIMENSION_SLASH("dimension_slash", "skill.exe.dimension_slash", ConfigFile::flySwordDimensionSlashCooldown), // 次元斩冷却定义。
    SUMMON_FLY_SWORD("summon_fly_sword", "skill.exe.summon_fly_sword", () -> 200), // 召唤飞剑冷却定义。
    BATTO_SLASH("batto_slash", "skill.exe.batto_slash", () -> 100), // 拔刀斩冷却定义。
    HEAVENLY_THUNDER("heavenly_thunder", "skill.exe.heavenly_thunder", TridentPlusConfig::heavenlyThunderCooldownTicks); // 天雷技能冷却定义。

    private final String key; // 服务端和客户端统一使用的技能冷却 key。
    private final String nameKey; // 技能名称翻译键。
    private final IntSupplier cooldownTicks; // 客户端基础冷却 tick 提供器。

    SkillCooldownType(String key, String nameKey, IntSupplier cooldownTicks) {
        this.key = key;
        this.nameKey = nameKey;
        this.cooldownTicks = cooldownTicks;
    }

    public String key() {
        return this.key;
    }

    public Component displayName() {
        return Component.translatable(this.nameKey);
    }

    public int cooldownTicks() {
        return Math.max(1, this.cooldownTicks.getAsInt());
    }

    public int serverCooldownTicks() {
        return ServerSkillCooldowns.serverCooldownTicks(cooldownTicks());
    }
}
