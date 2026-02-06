package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * 污染相关事件
 */
public class CorruptionEvent extends Event {

    private final Level level;
    private final BlockPos pos;
    private final int corruptionLevel;

    public CorruptionEvent(Level level, BlockPos pos, int corruptionLevel) {
        this.level = level;
        this.pos = pos;
        this.corruptionLevel = corruptionLevel;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getCorruptionLevel() {
        return corruptionLevel;
    }

    /**
     * 污染增加事件
     */
    public static class Increase extends CorruptionEvent {
        private final int amount;

        public Increase(Level level, BlockPos pos, int corruptionLevel, int amount) {
            super(level, pos, corruptionLevel);
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    /**
     * 污染减少事件
     */
    public static class Decrease extends CorruptionEvent {
        private final int amount;

        public Decrease(Level level, BlockPos pos, int corruptionLevel, int amount) {
            super(level, pos, corruptionLevel);
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    /**
     * 污染扩散事件
     */
    public static class Spread extends CorruptionEvent {
        private final BlockPos targetPos;

        public Spread(Level level, BlockPos pos, int corruptionLevel, BlockPos targetPos) {
            super(level, pos, corruptionLevel);
            this.targetPos = targetPos;
        }

        public BlockPos getTargetPos() {
            return targetPos;
        }
    }

    /**
     * 污染达到临界值事件
     */
    public static class Critical extends CorruptionEvent {
        public Critical(Level level, BlockPos pos, int corruptionLevel) {
            super(level, pos, corruptionLevel);
        }
    }
}
