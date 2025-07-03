package net.xiaoyang010.ex_enigmaticlegacy.Util;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;

public class EnderLavenderTeleporter implements ITeleporter {
    private static final EnderLavenderTeleporter instance = new EnderLavenderTeleporter();

    private EnderLavenderTeleporter() {
    }

    public static EnderLavenderTeleporter getInstance() {
        return instance;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(entity.position(), Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }
}