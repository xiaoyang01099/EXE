package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GearFlowerTile extends TileEntityFunctionalFlower implements IRotate, IHaveGoggleInformation {

    private static final int MAX_MANA = 10000;
    private static final int MANA_PER_TICK = 20; // 每tick消耗的mana
    private static final float BASE_STRESS_OUTPUT = 64.0f; // 基础应力输出
    private static final float BASE_RPM_OUTPUT = 32.0f; // 基础转速输出
    private static final float STRESS_TO_MANA_RATIO = 0.5f; // 应力转mana比率
    private static final float RPM_TO_MANA_RATIO = 1.0f; // 转速转mana比率
    private static final int CONVERSION_COOLDOWN = 10; // 转换冷却时间

    // 工作状态
    private boolean reverseMode = false; // false=mana转动力, true=动力转mana
    private boolean working = false;
    private int cooldown = 0;
    private long totalWorkingTime = 0;

    // Create动力系统相关
    private float currentStress = 0;
    private float currentRPM = 0;
    private float receivedStress = 0;
    private float receivedRPM = 0;

    // 效率相关
    private float efficiency = 1.0f;
    private int consecutiveWorkingTicks = 0;

    // 同步相关
    private int syncTimer = 0;
    private boolean needsSync = false;

    public GearFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null) return;

        // 处理冷却
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        boolean wasWorking = working;
        float oldStress = currentStress;
        float oldRPM = currentRPM;
        boolean oldMode = reverseMode;

        working = false;

        if (reverseMode) {
            // 动力转mana模式
            working = processKineticToMana();
        } else {
            // mana转动力模式
            working = processManaToKinetic();
        }

        // 更新效率
        updateEfficiency();

        // 更新工作时间统计
        if (working) {
            totalWorkingTime++;
            consecutiveWorkingTicks++;
        } else {
            consecutiveWorkingTicks = 0;
        }

        // 检查是否需要同步
        if (!level.isClientSide) {
            boolean significantChange = wasWorking != working ||
                    Math.abs(oldStress - currentStress) > 1.0f ||
                    Math.abs(oldRPM - currentRPM) > 1.0f ||
                    oldMode != reverseMode;

            syncTimer++;
            if (significantChange || syncTimer >= 20) { // 每秒强制同步一次
                syncTimer = 0;
                syncToClient();
                updateBlockState();
            }
        }

        setChanged();
    }

    /**
     * 处理动力转mana - 修复版本
     */
    private boolean processKineticToMana() {
        // 检查是否有足够的动力输入
        if (receivedStress <= 0.1f || Math.abs(receivedRPM) <= 0.1f) {
            currentStress = 0;
            currentRPM = 0;
            return false;
        }

        if (getMana() >= getMaxMana()) {
            return false; // mana已满
        }

        // 计算mana生成量 - 修正计算公式
        float effectiveStress = Math.min(receivedStress, BASE_STRESS_OUTPUT * 2.0f);
        float effectiveRPM = Math.min(Math.abs(receivedRPM), BASE_RPM_OUTPUT * 2.0f);

        float stressMana = effectiveStress * STRESS_TO_MANA_RATIO * efficiency;
        float rpmMana = effectiveRPM * RPM_TO_MANA_RATIO * efficiency;
        int manaGenerated = Math.max(1, Math.round((stressMana + rpmMana) / 20.0f)); // 每秒转换，最少1点

        if (manaGenerated > 0) {
            int actualManaAdded = Math.min(manaGenerated, getMaxMana() - getMana());
            addMana(actualManaAdded);

            // 更新显示用的数值
            currentStress = effectiveStress;
            currentRPM = effectiveRPM;

            cooldown = CONVERSION_COOLDOWN;
            return true;
        }

        return false;
    }

    /**
     * 处理mana转动力
     */
    private boolean processManaToKinetic() {
        if (getMana() < MANA_PER_TICK) {
            currentStress = 0;
            currentRPM = 0;
            return false;
        }

        // 计算输出的动力
        float targetStress = BASE_STRESS_OUTPUT * efficiency;
        float targetRPM = BASE_RPM_OUTPUT * efficiency;

        // 消耗mana
        addMana(-MANA_PER_TICK);

        currentStress = targetStress;
        currentRPM = targetRPM;

        cooldown = CONVERSION_COOLDOWN;
        return true;
    }

    /**
     * 更新效率
     */
    private void updateEfficiency() {
        // 基础效率
        efficiency = 1.0f;

        // 连续工作奖励
        if (consecutiveWorkingTicks > 200) { // 10秒后开始提升
            float bonus = Math.min(0.5f, (consecutiveWorkingTicks - 200) / 1200.0f); // 最多50%奖励
            efficiency += bonus;
        }

        // 周围环境奖励（如果周围有齿轮、机械方块等）
        float environmentBonus = calculateEnvironmentBonus();
        efficiency += environmentBonus;

        // 限制效率范围
        efficiency = Math.max(0.5f, Math.min(2.0f, efficiency));
    }

    /**
     * 计算环境奖励
     */
    private float calculateEnvironmentBonus() {
        float bonus = 0;

        // 检查周围是否有Create的机械方块
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = worldPosition.relative(direction);
            BlockState checkState = level.getBlockState(checkPos);

            // 如果是Create的机械方块，给予奖励
            if (checkState.getBlock() instanceof IRotate) {
                bonus += 0.05f;
            }
        }

        return Math.min(0.3f, bonus); // 最多30%环境奖励
    }

    @Override
    public boolean hasShaftTowards(LevelReader levelReader, BlockPos blockPos, BlockState blockState, Direction direction) {
        return direction == Direction.UP || direction == Direction.DOWN;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    /**
     * 接收来自Create网络的动力 - 修复版本
     */
    public void receiveRotation(float stress, float rpm) {
        if (reverseMode && !level.isClientSide) {
            this.receivedStress = Math.abs(stress);
            this.receivedRPM = rpm;
            this.needsSync = true; // 标记需要同步
        }
    }

    /**
     * 获取动力输出 - 新增方法，供Create系统调用
     */
    public float getStressOutput() {
        return reverseMode ? 0 : currentStress;
    }

    public float getRPMOutput() {
        return reverseMode ? 0 : currentRPM;
    }

    // Getter和Setter方法
    public boolean isReverseMode() {
        return reverseMode;
    }

    public void setReverseMode(boolean reverseMode) {
        boolean changed = this.reverseMode != reverseMode;
        this.reverseMode = reverseMode;

        if (changed) {
            if (reverseMode) {
                currentStress = 0;
                currentRPM = 0;
            } else {
                receivedStress = 0;
                receivedRPM = 0;
            }
            this.needsSync = true;
            setChanged();
        }
    }

    public boolean isWorking() {
        return working;
    }

    public int getSpeedLevel() {
        if (!working) return 0;

        float rpm = reverseMode ? receivedRPM : currentRPM;
        if (Math.abs(rpm) < 16) return 1;
        if (Math.abs(rpm) < 32) return 2;
        return 3;
    }

    public boolean hasSufficientPower() {
        if (reverseMode) {
            return receivedStress > 0.1f && Math.abs(receivedRPM) > 0.1f;
        } else {
            return getMana() >= MANA_PER_TICK;
        }
    }

    public float getCurrentStress() {
        return reverseMode ? receivedStress : currentStress;
    }

    public float getMaxStress() {
        return BASE_STRESS_OUTPUT * 2.0f; // 考虑效率加成
    }

    public float getCurrentRPM() {
        return reverseMode ? receivedRPM : currentRPM;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public long getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public int getConversionRate() {
        if (reverseMode) {
            return Math.round((STRESS_TO_MANA_RATIO + RPM_TO_MANA_RATIO) * efficiency * 20);
        } else {
            return MANA_PER_TICK;
        }
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 1);
    }

    // 数据同步方法 - 修复版本
    private void syncToClient() {
        if (!level.isClientSide && needsSync) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            needsSync = false;
        }
    }

    private void updateBlockState() {
        if (getBlockState().getBlock() instanceof GearFlower gearFlower) {
            gearFlower.updateBlockState(level, worldPosition, this);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeToPacketNBT(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // NBT数据保存和加载
    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putBoolean("reverseMode", reverseMode);
        cmp.putBoolean("working", working);
        cmp.putInt("cooldown", cooldown);
        cmp.putLong("totalWorkingTime", totalWorkingTime);
        cmp.putFloat("currentStress", currentStress);
        cmp.putFloat("currentRPM", currentRPM);
        cmp.putFloat("receivedStress", receivedStress);
        cmp.putFloat("receivedRPM", receivedRPM);
        cmp.putFloat("efficiency", efficiency);
        cmp.putInt("consecutiveWorkingTicks", consecutiveWorkingTicks);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        reverseMode = cmp.getBoolean("reverseMode");
        working = cmp.getBoolean("working");
        cooldown = cmp.getInt("cooldown");
        totalWorkingTime = cmp.getLong("totalWorkingTime");
        currentStress = cmp.getFloat("currentStress");
        currentRPM = cmp.getFloat("currentRPM");
        receivedStress = cmp.getFloat("receivedStress");
        receivedRPM = cmp.getFloat("receivedRPM");
        efficiency = cmp.getFloat("efficiency");
        consecutiveWorkingTicks = cmp.getInt("consecutiveWorkingTicks");
    }

    // Create护目镜信息显示
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("gui.goggles.kinetic_stats")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        if (reverseMode) {
            Lang.translate("gui.goggles.generator_stats")
                    .style(ChatFormatting.DARK_GREEN)
                    .forGoggles(tooltip);

            Lang.number(receivedStress)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .translate("gui.goggles.at_current_speed")
                    .forGoggles(tooltip, 1);

            Lang.number(Math.abs(receivedRPM))
                    .translate("generic.unit.rpm")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);
        } else {
            Lang.translate("gui.goggles.consumer_stats")
                    .style(ChatFormatting.DARK_RED)
                    .forGoggles(tooltip);

            Lang.number(currentStress)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .translate("gui.goggles.at_current_speed")
                    .forGoggles(tooltip, 1);

            Lang.number(currentRPM)
                    .translate("generic.unit.rpm")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);
        }

        // 显示mana信息
        tooltip.add(new TextComponent("§bMana: §f" + getMana() + "/" + getMaxMana()));

        // 显示效率
        tooltip.add(new TextComponent("§6Efficiency: §f" + String.format("%.1f%%", efficiency * 100)));

        return true;
    }

    // Capability支持
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // 这里可以添加对Create动力系统的Capability支持
        return super.getCapability(cap, side);
    }
}