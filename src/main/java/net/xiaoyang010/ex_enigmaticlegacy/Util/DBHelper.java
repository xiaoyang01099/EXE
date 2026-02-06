package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.UUID;

public final class DBHelper {
    public static final IntegerProperty DOUBLE = IntegerProperty.create("double", 0, 4);
    public static final TranslatableComponent CONTAINER_TITLE = new TranslatableComponent("container.ex_enigmaticlegacy.double_crafting");
    public static final UUID BASE_ENTITY_REACH_UUID = UUID.fromString("BDB63367-5830-46B0-B974-DC6DA3DEDC26");
    public static final UUID BASE_BLOCK_REACH_UUID = UUID.fromString("1E1B9A06-440F-4344-A35D-4B644C2A030E");

    private DBHelper(){}

    public static int directionToType(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return 4;
            }
            case EAST -> {
                return 1;
            }
            case SOUTH -> {
                return 2;
            }
            case WEST -> {
                return 3;
            }
            default -> throw new IllegalStateException("Invalid cardinal point from: " + ExEnigmaticlegacyMod.MODID);
        }
    }

    public static Direction typeToDirection(int type) {
        switch (type) {
            case 4 -> {
                return Direction.NORTH;
            }
            case 1 -> {
                return Direction.EAST;
            }
            case 2 -> {
                return Direction.SOUTH;
            }
            case 3 -> {
                return Direction.WEST;
            }
            default -> {
                return null;
            }
        }
    }
}
