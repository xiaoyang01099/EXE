package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientWhitelistCache {
    private static final Set<String> WHITELIST = Collections.synchronizedSet(new HashSet<>()); // 客户端实体伤害白名单缓存。

    // 使用服务端同步的新列表替换客户端白名单缓存。
    public static void updateWhitelist(List<String> entries) {
        WHITELIST.clear();
        WHITELIST.addAll(entries);
    }

    // 判断实体类型是否存在于客户端白名单缓存中。
    public static boolean isInWhitelist(EntityType<?> type) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key != null && WHITELIST.contains(key.toString());
    }
}
