package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// EntityUtil 处理实体白名单相关判断。
public class EntityUtil {
    public static final Map<UUID, MovementLockData> MOVEMENT_LOCKS = new HashMap<>(); // 运行时移动锁定表，不写入实体 tag。

    // 判断实体类型是否在伤害白名单中。
    public static boolean isInDamageWhitelist(EntityType<?> entityType) {
        HashMap<String, Boolean> whitelist = ConfigFile.entityDamageWhitelistMap();
        if (whitelist.isEmpty()) {
            return false;
        }

        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (key == null) {
            return false;
        }
        return Boolean.TRUE.equals(whitelist.get(key.toString()));
    }

    // 判断实体是否在伤害白名单中。
    public static boolean isInDamageWhitelist(Entity entity) {
        return entity != null && isInDamageWhitelist(entity.getType());
    }

    // 给实体添加移动锁定，锚点使用实体当前位置。
    public static void lockMovement(LivingEntity entity, int ticks) {
        if (entity == null) return;
        lockMovement(entity, ticks, entity.position());
    }

    // 给实体添加移动锁定，并把实体固定在指定锚点。
    public static void lockMovement(LivingEntity entity, int ticks, Vec3 anchor) {
        if (entity == null || entity.level().isClientSide()) return;
        long expireTime = entity.level().getGameTime() + Math.max(1, ticks);
        MovementLockData oldData = MOVEMENT_LOCKS.get(entity.getUUID());
        Vec3 lockAnchor = anchor;
        if (oldData != null && oldData.expireTime >= entity.level().getGameTime()) {
            lockAnchor = oldData.anchor;
        }
        MOVEMENT_LOCKS.put(entity.getUUID(), new MovementLockData(expireTime, lockAnchor));
        tickMovementLock(entity);
    }

    // 判断实体是否处于移动锁定中。
    public static boolean isMovementLocked(LivingEntity entity) {
        if (entity == null) return false;
        MovementLockData data = MOVEMENT_LOCKS.get(entity.getUUID());
        if (data == null) return false;
        if (data.expireTime < entity.level().getGameTime() || !entity.isAlive()) {
            clearMovementLock(entity);
            return false;
        }
        return true;
    }

    // 每 tick 应用移动锁定，停止寻路并拉回锚点。
    public static void tickMovementLock(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        MovementLockData data = MOVEMENT_LOCKS.get(entity.getUUID());
        if (data == null) return;

        if (data.expireTime < entity.level().getGameTime() || !entity.isAlive()) {
            clearMovementLock(entity);
            return;
        }

        // 移动锁定是运行时控制：清速度、停寻路、拉回锚点，不污染实体 persistent data。
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        entity.teleportTo(data.anchor.x, data.anchor.y, data.anchor.z);
    }

    // 清理实体移动锁定数据。
    public static void clearMovementLock(LivingEntity entity) {
        if (entity == null) return;
        MOVEMENT_LOCKS.remove(entity.getUUID());
    }

    // MovementLockData 保存运行时移动锁定的过期时间和锚点。
    public static class MovementLockData {
        public final long expireTime; // 移动锁定过期游戏时间。
        public final Vec3 anchor; // 移动锁定锚点。

        public MovementLockData(long expireTime, Vec3 anchor) {
            this.expireTime = expireTime;
            this.anchor = anchor;
        }
    }
}
