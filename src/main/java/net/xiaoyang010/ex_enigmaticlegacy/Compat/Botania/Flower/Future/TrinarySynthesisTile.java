package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrinarySynthesisTile extends TileEntityFunctionalFlower{

    public enum WorkMode {
        ARCANE_MECHANIST(0, "arcane_mechanist", 50, 0x00AAFF),
        TRINKET_RESONATOR(1, "trinket_resonator", 20, 0xAA00FF),
        FUSION_CATALYST(2, "fusion_catalyst", 200, 0xFFAA00);

        public final int id;
        public final String name;
        public final int manaCost;
        public final int color;

        WorkMode(int id, String name, int manaCost, int color) {
            this.id = id;
            this.name = name;
            this.manaCost = manaCost;
            this.color = color;
        }

        public static WorkMode fromId(int id) {
            for (WorkMode mode : values()) {
                if (mode.id == id) return mode;
            }
            return ARCANE_MECHANIST;
        }

        public WorkMode next() {
            return values()[(this.id + 1) % values().length];
        }
    }

    private static final String TAG_MODE = "WorkMode";
    private static final String TAG_LEARNING_DATA = "LearningData";
    private static final String TAG_COOLDOWN = "Cooldown";

    private static final int RANGE = 9;
    private static final int TICK_INTERVAL = 20;

    private WorkMode currentMode = WorkMode.ARCANE_MECHANIST;
    private CompoundTag learningData = new CompoundTag();
    private int cooldown = 0;
    private int tickCounter = 0;

    public TrinarySynthesisTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<TrinarySynthesisTile> {
        public FunctionalWandHud(TrinarySynthesisTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        if (!canWork()) return;

        switch (currentMode) {
            case ARCANE_MECHANIST:
                tickArcaneMechanist();
                break;
            case TRINKET_RESONATOR:
                tickTrinketResonator();
                break;
            case FUSION_CATALYST:
                tickFusionCatalyst();
                break;
        }
    }

    private boolean canWork() {
        return this.getMana() >= currentMode.manaCost;
    }

    private boolean consumeMana(int amount) {
        if (this.getMana() >= amount) {
            this.addMana(-amount);
            return true;
        }
        return false;
    }

    private void tickArcaneMechanist() {
        if (!consumeMana(currentMode.manaCost)) return;

        List<KineticBlockEntity> mechanics = getKineticBlockEntitiesInRange();

        for (KineticBlockEntity kinetic : mechanics) {
            if (kinetic.getSpeed() != 0) {
                enhanceKineticMachine(kinetic);
                spawnManaParticles(kinetic.getBlockPos(), currentMode.color);
            }
        }

        updateLearningData("common_kinetics", mechanics);
    }

    private void tickTrinketResonator() {
        if (!consumeMana(currentMode.manaCost)) return;

        List<Player> players = getPlayersInRange();

        for (Player player : players) {
            Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper()
                    .getCuriosHandler(player).resolve();

            if (curiosOpt.isPresent()) {
                ICuriosItemHandler curios = curiosOpt.get();
                enhanceEnigmaticLegacyItems(player, curios);
                spawnTrinketParticles(player, currentMode.color);
            }
        }
    }

    private void tickFusionCatalyst() {
        if (!consumeMana(currentMode.manaCost)) return;

        List<Player> players = getPlayersInRange();
        List<KineticBlockEntity> mechanics = getKineticBlockEntitiesInRange();

        for (Player player : players) {
            for (KineticBlockEntity kinetic : mechanics) {
                if (player.distanceToSqr(Vec3.atCenterOf(kinetic.getBlockPos())) <= RANGE * RANGE) {
                    createSynergyField(player, kinetic);
                }
            }
        }

        spawnFusionParticles();
        cooldown = 100;
    }

    private List<KineticBlockEntity> getKineticBlockEntitiesInRange() {
        List<KineticBlockEntity> kineticEntities = new ArrayList<>();
        BlockPos center = getBlockPos();

        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (checkPos.distSqr(center) <= RANGE * RANGE) {
                        BlockEntity blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof KineticBlockEntity) {
                            kineticEntities.add((KineticBlockEntity) blockEntity);
                        }
                    }
                }
            }
        }

        return kineticEntities;
    }

    private List<Player> getPlayersInRange() {
        return level.getEntitiesOfClass(Player.class,
                new AABB(getBlockPos()).inflate(RANGE),
                player -> player != null && player.isAlive());
    }

    private void enhanceKineticMachine(KineticBlockEntity kinetic) {
        try {
            CompoundTag kineticNBT = kinetic.saveWithFullMetadata();
            long currentTime = level.getGameTime();

            if (kineticNBT.contains("TrinaryKineticEnhancement")) {
                long enhancementTime = kineticNBT.getLong("TrinaryKineticEnhancement");
                if (currentTime < enhancementTime) {
                    spawnManaParticles(kinetic.getBlockPos(), currentMode.color);
                    return;
                }
            }

            float currentStress = kinetic.calculateStressApplied();
            float currentSpeed = kinetic.getSpeed();

            applyKineticEnhancement(kinetic, kineticNBT, currentTime, currentStress, currentSpeed);

            if (kinetic instanceof IMultiBlockEntityContainer) {
                enhanceMultiBlockStructure(kinetic);
            }

            enhanceSpecificMachineTypes(kinetic, kineticNBT);

            playKineticEnhancementSound(kinetic.getBlockPos());

            spawnManaParticles(kinetic.getBlockPos(), currentMode.color);

        } catch (Exception e) {
            spawnManaParticles(kinetic.getBlockPos(), currentMode.color);
            e.printStackTrace();
        }
    }

    /**
     * 增强Enigmatic Legacy饰品
     */
    private void enhanceEnigmaticLegacyItems(Player player, ICuriosItemHandler curios) {
        try {
            curios.getCurios().forEach((slotType, stackHandler) -> {
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    var stack = stackHandler.getStacks().getStackInSlot(i);
                    if (!stack.isEmpty() && isEnigmaticLegacyItem(stack)) {
                        applyTrinketEnhancement(player, stack);
                    }
                }
            });
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    private boolean isEnigmaticLegacyItem(ItemStack stack) {
        var registryName = stack.getItem().getRegistryName();
        return registryName != null && registryName.getNamespace().equals("enigmaticlegacy");
    }

    /**
     * 应用饰品增强效果
     */
    private void applyTrinketEnhancement(Player player, ItemStack stack) {
        try {
            if (stack.isEmpty() || !isEnigmaticLegacyItem(stack)) {
                return;
            }

            var item = stack.getItem();

            // 设置增强标记和持续时间（20秒）
            var nbt = stack.getOrCreateTag();
            long currentTime = level.getGameTime();
            nbt.putLong("TrinaryEnhancementTime", currentTime + 400);
            nbt.putBoolean("TrinaryEnhanced", true);

            // 1. 法术石增强 - 减少冷却时间
            if (item instanceof com.integral.enigmaticlegacy.api.items.ISpellstone) {
                enhanceSpellstone(player, stack, nbt);
            }

            // 2. 多方块挖掘工具增强 - 提升挖掘效率
            if (item instanceof com.integral.enigmaticlegacy.api.items.IMultiblockMiningTool) {
                enhanceMultiblockTool(player, stack, nbt);
            }

            // 3. 可污染物品增强 - 临时净化效果
            if (item instanceof com.integral.enigmaticlegacy.api.items.ITaintable taintable) {
                enhanceTaintable(player, stack, nbt, taintable);
            }

            // 4. 高级药水物品增强 - 提升药水效果
            if (item instanceof com.integral.enigmaticlegacy.api.items.IAdvancedPotionItem) {
                enhancePotionItem(player, stack, nbt);
            }

            // 5. 基础工具增强 - 提升挖掘速度
            if (item instanceof com.integral.enigmaticlegacy.api.items.IBaseTool) {
                enhanceBaseTool(player, stack, nbt);
            }

            // 6. 诅咒物品增强 - 减少负面效果
            if (item instanceof com.integral.enigmaticlegacy.api.items.ICursed) {
                enhanceCursedItem(player, stack, nbt);
            }

            // 7. 永久水晶增强 - 提升能量效率
            if (item instanceof com.integral.enigmaticlegacy.api.items.IPermanentCrystal) {
                enhancePermanentCrystal(player, stack, nbt);
            }

            // 播放增强音效
            playEnhancementSound(player);

        } catch (Exception e) {
            // 静默处理异常，避免崩溃
            e.printStackTrace();
        }
    }

    /**
     * 增强法术石 - 减少冷却时间
     */
    private void enhanceSpellstone(Player player, ItemStack stack, CompoundTag nbt) {
        // 设置冷却时间减少50%
        nbt.putFloat("CooldownReduction", 0.5f);
        nbt.putString("EnhancementType", "spellstone_cooldown");

        // 如果玩家有法术石冷却，尝试减少
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            nbt.putBoolean("ReduceCooldown", true);
        }
    }

    /**
     * 增强多方块挖掘工具 - 提升挖掘效率和范围
     */
    private void enhanceMultiblockTool(Player player, ItemStack stack, CompoundTag nbt) {
        // 增加挖掘效率
        nbt.putFloat("EfficiencyBonus", 2.0f);
        nbt.putInt("RangeBonus", 1); // 增加1格挖掘范围
        nbt.putString("EnhancementType", "multiblock_efficiency");

        // 临时启用区域效果（如果被禁用）
        var multiblockTool = (com.integral.enigmaticlegacy.api.items.IMultiblockMiningTool) stack.getItem();
        if (!multiblockTool.areaEffectsAllowed(stack)) {
            nbt.putBoolean("TempAreaEffects", true);
        }
    }

    /**
     * 增强可污染物品 - 临时净化效果
     */
    private void enhanceTaintable(Player player, ItemStack stack, CompoundTag nbt,
                                  com.integral.enigmaticlegacy.api.items.ITaintable taintable) {
        if (taintable.isTainted(stack)) {
            // 临时净化被污染的物品
            nbt.putBoolean("TempPurified", true);
            nbt.putString("EnhancementType", "taint_purification");
        } else {
            // 为未被污染的物品提供抗污染保护
            nbt.putBoolean("TaintResistance", true);
            nbt.putString("EnhancementType", "taint_resistance");
        }
    }

    /**
     * 增强高级药水物品 - 提升药水效果
     */
    private void enhancePotionItem(Player player, ItemStack stack, CompoundTag nbt) {
        var potionItem = (com.integral.enigmaticlegacy.api.items.IAdvancedPotionItem) stack.getItem();

        // 根据药水类型提供不同增强
        switch (potionItem.getPotionType()) {
            case COMMON:
                nbt.putFloat("PotionDurationBonus", 1.5f); // 持续时间+50%
                break;
            case ULTIMATE:
                nbt.putFloat("PotionAmplifierBonus", 1.2f); // 效果强度+20%
                break;
        }
        nbt.putString("EnhancementType", "potion_boost");
    }

    /**
     * 增强基础工具 - 提升挖掘速度
     */
    private void enhanceBaseTool(Player player, ItemStack stack, CompoundTag nbt) {
        var baseTool = (com.integral.enigmaticlegacy.api.items.IBaseTool) stack.getItem();

        // 提升挖掘效率
        float originalEfficiency = baseTool.getEfficiency();
        nbt.putFloat("OriginalEfficiency", originalEfficiency);
        nbt.putFloat("EfficiencyMultiplier", 1.5f); // 效率提升50%
        nbt.putString("EnhancementType", "tool_efficiency");
    }

    /**
     * 增强诅咒物品 - 减少负面效果
     */
    private void enhanceCursedItem(Player player, ItemStack stack, CompoundTag nbt) {
        // 减少诅咒物品的负面效果
        nbt.putFloat("CurseReduction", 0.3f); // 减少30%负面效果
        nbt.putString("EnhancementType", "curse_mitigation");

        // 如果是邪恶物品，提供额外保护
        if (stack.getItem() instanceof com.integral.enigmaticlegacy.api.items.IEldritch) {
            nbt.putBoolean("EldritchProtection", true);
        }
    }

    /**
     * 增强永久水晶 - 提升能量效率
     */
    private void enhancePermanentCrystal(Player player, ItemStack stack, CompoundTag nbt) {
        // 提升水晶的能量效率
        nbt.putFloat("EnergyEfficiency", 1.3f); // 效率提升30%
        nbt.putFloat("EnergyRegenBonus", 0.2f); // 能量回复+20%
        nbt.putString("EnhancementType", "crystal_efficiency");
    }

    /**
     * 播放增强音效
     */
    private void playEnhancementSound(Player player) {
        if (!level.isClientSide) {
            // 使用EL的音效系统
            try {
                level.playSound(null, player.blockPosition(),
                        com.integral.enigmaticlegacy.EnigmaticLegacy.soundChargedOn,
                        SoundSource.PLAYERS,
                        0.8f, 1.2f + (float)(Math.random() * 0.4f));
            } catch (Exception e) {
                // 如果EL音效不可用，使用原版音效
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS,
                        0.5f, 1.5f);
            }
        }
    }

    /**
     * 应用机械增强效果
     */
    private void applyKineticEnhancement(KineticBlockEntity kinetic, CompoundTag nbt,
                                         long currentTime, float currentStress, float currentSpeed) {
        // 设置增强标记和持续时间（20秒）
        nbt.putLong("TrinaryKineticEnhancement", currentTime + 400);
        nbt.putBoolean("TrinaryEnhanced", true);
        nbt.putFloat("OriginalStress", currentStress);
        nbt.putFloat("OriginalSpeed", currentSpeed);

        // 应用不同类型的增强
        if (Math.abs(currentSpeed) > 0) {
            // 1. 效率增强 - 提升25%效率
            nbt.putFloat("EfficiencyBonus", 1.25f);

            // 2. 应力减少 - 减少20%应力消耗
            nbt.putFloat("StressReduction", 0.8f);

            // 3. 速度稳定化 - 减少速度波动
            nbt.putBoolean("SpeedStabilized", true);

            // 4. 能量效率提升
            nbt.putFloat("EnergyEfficiency", 1.3f);

            // 尝试通过反射直接修改机械属性（谨慎使用）
            try {
                enhanceKineticThroughReflection(kinetic, 1.25f, 0.8f);
            } catch (Exception e) {
                scheduleKineticBehaviorModification(kinetic);
            }
        }

        kinetic.load(nbt);
        kinetic.setChanged();
    }

    /**
     * 通过反射增强机械属性（高级功能）
     */
    private void enhanceKineticThroughReflection(KineticBlockEntity kinetic, float efficiencyBonus, float stressReduction) {
        try {
            Class<?> kineticClass = kinetic.getClass();

            try {
                java.lang.reflect.Field stressField = findFieldRecursively(kineticClass, "stress");
                if (stressField != null) {
                    stressField.setAccessible(true);
                    Object currentStressValue = stressField.get(kinetic);
                    if (currentStressValue instanceof Float) {
                        float newStress = ((Float) currentStressValue) * stressReduction;
                        stressField.set(kinetic, newStress);
                    }
                }
            } catch (Exception e) {
            }

            try {
                java.lang.reflect.Field speedField = findFieldRecursively(kineticClass, "speed");
                if (speedField != null) {
                    speedField.setAccessible(true);
                    Object currentSpeedValue = speedField.get(kinetic);
                    if (currentSpeedValue instanceof Float) {
                        float currentSpeed = (Float) currentSpeedValue;
                        float newSpeed = currentSpeed * (currentSpeed > 0 ? efficiencyBonus : 1.0f);
                        speedField.set(kinetic, newSpeed);
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            throw new RuntimeException("Reflection enhancement failed", e);
        }
    }

    /**
     * 递归查找字段（包括父类）
     */
    private java.lang.reflect.Field findFieldRecursively(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 安排机械行为修改（替代反射的安全方案）
     */
    private void scheduleKineticBehaviorModification(KineticBlockEntity kinetic) {
        // 通过调度器在下一tick修改机械行为
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                    serverLevel.getServer().getTickCount() + 1,
                    () -> {
                        try {
                            kinetic.onSpeedChanged(kinetic.getSpeed() * 1.05f); // 轻微速度提升
                            kinetic.setChanged();
                        } catch (Exception e) {
                        }
                    }
            ));
        }
    }

    /**
     * 增强多方块结构
     */
    private void enhanceMultiBlockStructure(KineticBlockEntity kinetic) {
        try {
            var multiBlock = (IMultiBlockEntityContainer) kinetic;

            if (multiBlock.isController()) {
                int width = multiBlock.getWidth();
                int height = multiBlock.getHeight();

                var controllerNBT = kinetic.saveWithFullMetadata();
                controllerNBT.putBoolean("MultiBlockEnhanced", true);
                controllerNBT.putLong("MultiBlockEnhancementTime", level.getGameTime() + 400);
                controllerNBT.putFloat("StructureEfficiencyBonus", 1.2f);

                kinetic.load(controllerNBT);

                if (multiBlock instanceof IMultiBlockEntityContainer.Fluid) {
                    enhanceFluidHandling((IMultiBlockEntityContainer.Fluid) multiBlock);
                }

                // 如果有物品处理能力，增强物品处理
                if (multiBlock instanceof IMultiBlockEntityContainer.Inventory) {
                    enhanceInventoryHandling((IMultiBlockEntityContainer.Inventory) multiBlock);
                }
            }
        } catch (Exception e) {
            // 多方块增强失败，不影响基础功能
        }
    }

    /**
     * 增强流体处理
     */
    private void enhanceFluidHandling(IMultiBlockEntityContainer.Fluid fluidHandler) {
        if (fluidHandler.hasTank()) {
            try {
                var tank = fluidHandler.getTank(0);

                var blockEntity = (BlockEntity) fluidHandler;
                var nbt = blockEntity.saveWithFullMetadata();
                nbt.putFloat("FluidProcessingBonus", 1.4f); // 流体处理效率+40%
                nbt.putBoolean("FluidEnhanced", true);

                blockEntity.load(nbt);
            } catch (Exception e) {
                // 流体增强失败
            }
        }
    }

    /**
     * 增强物品处理
     */
    private void enhanceInventoryHandling(IMultiBlockEntityContainer.Inventory inventoryHandler) {
        if (inventoryHandler.hasInventory()) {
            try {
                // 增强物品处理效率
                var blockEntity = (BlockEntity) inventoryHandler;
                var nbt = blockEntity.saveWithFullMetadata();
                nbt.putFloat("ItemProcessingBonus", 1.3f); // 物品处理效率+30%
                nbt.putBoolean("InventoryEnhanced", true);

                blockEntity.load(nbt);
            } catch (Exception e) {
                // 物品增强失败
            }
        }
    }

    /**
     * 特殊机械类型的额外增强
     */
    private void enhanceSpecificMachineTypes(KineticBlockEntity kinetic, CompoundTag nbt) {
        String blockName = kinetic.getBlockState().getBlock().getRegistryName().getPath();

        switch (blockName) {
            case "mechanical_drill":
                nbt.putFloat("DrillSpeedBonus", 1.5f);
                nbt.putFloat("DrillDurabilityBonus", 0.7f); // 减少30%磨损
                break;

            case "mechanical_saw":
                nbt.putFloat("SawEfficiencyBonus", 1.4f);
                nbt.putInt("SawRangeBonus", 1); // 增加1格范围
                break;

            case "mechanical_press":
                nbt.putFloat("PressForceBonus", 1.3f);
                nbt.putFloat("PressSpeedBonus", 1.2f);
                break;

            case "millstone":
                nbt.putFloat("MillstoneOutputBonus", 1.25f); // 产出+25%
                break;

            case "mechanical_mixer":
                nbt.putFloat("MixerSpeedBonus", 1.35f);
                nbt.putBoolean("MixerAutoOutput", true); // 自动输出
                break;

            case "encased_fan":
                nbt.putFloat("FanRangeBonus", 1.5f); // 影响范围+50%
                nbt.putFloat("FanEffectBonus", 1.3f); // 效果强度+30%
                break;

            case "rotation_speed_controller":
                nbt.putFloat("SpeedControllerPrecision", 1.4f); // 精度提升
                nbt.putFloat("SpeedControllerStability", 1.2f); // 稳定性提升
                break;

            default:
                nbt.putFloat("GenericEfficiencyBonus", 1.15f);
                break;
        }
    }

    /**
     * 播放机械增强音效
     */
    private void playKineticEnhancementSound(BlockPos pos) {
        if (!level.isClientSide) {
            level.playSound(null, pos,
                    SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS,
                    0.6f, 1.5f + (float)(Math.random() * 0.3f));

            level.playSound(null, pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    0.4f, 0.8f + (float)(Math.random() * 0.4f));
        }
    }

    /**
     * 检查机械增强是否激活
     */
    public static boolean isKineticEnhancementActive(KineticBlockEntity kinetic, long currentTime) {
        try {
            var nbt = kinetic.saveWithFullMetadata();
            if (!nbt.getBoolean("TrinaryEnhanced")) {
                return false;
            }

            long enhancementTime = nbt.getLong("TrinaryKineticEnhancement");
            if (currentTime >= enhancementTime) {
                clearKineticEnhancement(kinetic, nbt);
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清除过期的机械增强效果
     */
    private static void clearKineticEnhancement(KineticBlockEntity kinetic, CompoundTag nbt) {
        try {
            // 恢复原始值
            if (nbt.contains("OriginalStress")) {
            }

            nbt.remove("TrinaryEnhanced");
            nbt.remove("TrinaryKineticEnhancement");
            nbt.remove("EfficiencyBonus");
            nbt.remove("StressReduction");
            nbt.remove("SpeedStabilized");
            nbt.remove("EnergyEfficiency");

            kinetic.load(nbt);
            kinetic.setChanged();

            kinetic.onSpeedChanged(kinetic.getSpeed());

        } catch (Exception e) {
        }
    }

    /**
     * 检查增强效果是否仍然有效
     */
    public static boolean isEnhancementActive(ItemStack stack, long currentTime) {
        var nbt = stack.getTag();
        if (nbt == null || !nbt.getBoolean("TrinaryEnhanced")) {
            return false;
        }

        long enhancementTime = nbt.getLong("TrinaryEnhancementTime");
        if (currentTime >= enhancementTime) {
            nbt.remove("TrinaryEnhanced");
            nbt.remove("TrinaryEnhancementTime");
            nbt.remove("EnhancementType");
            return false;
        }

        return true;
    }

    /**
     * 获取增强效果的剩余时间（秒）
     */
    public static int getEnhancementTimeLeft(ItemStack stack, long currentTime) {
        var nbt = stack.getTag();
        if (nbt == null || !nbt.getBoolean("TrinaryEnhanced")) {
            return 0;
        }

        long enhancementTime = nbt.getLong("TrinaryEnhancementTime");
        return Math.max(0, (int)((enhancementTime - currentTime) / 20));
    }

    private void createSynergyField(Player player, KineticBlockEntity kinetic) {
        enhanceKineticMachine(kinetic);

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper()
                .getCuriosHandler(player).resolve();
        curiosOpt.ifPresent(iCuriosItemHandler -> enhanceEnigmaticLegacyItems(player, iCuriosItemHandler));
    }

    private void spawnManaParticles(BlockPos target, int color) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 start = Vec3.atCenterOf(getBlockPos());
            Vec3 end = Vec3.atCenterOf(target);
            Vec3 direction = end.subtract(start).normalize();

            for (int i = 0; i < 10; i++) {
                Vec3 pos = start.add(direction.scale(i * 0.5));
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.x, pos.y, pos.z, 1,
                        0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    private void spawnTrinketParticles(Player player, int color) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            for (int i = 0; i < 5; i++) {
                double angle = (level.getGameTime() + i * 72) * 0.1;
                double x = playerPos.x + Math.cos(angle) * 2;
                double z = playerPos.z + Math.sin(angle) * 2;
                double y = playerPos.y + 1 + Math.sin(angle * 2) * 0.5;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnFusionParticles() {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = Vec3.atCenterOf(getBlockPos());
            for (int i = 0; i < 20; i++) {
                double angle = i * 18;
                double radius = 1.5;
                double x = center.x + Math.cos(Math.toRadians(angle)) * radius;
                double z = center.z + Math.sin(Math.toRadians(angle)) * radius;
                double y = center.y + 1.5;

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1, 0, 0.1, 0, 0.05);
            }
        }
    }

    private void updateLearningData(String category, List<?> data) {
        int currentCount = learningData.getInt(category);
        learningData.putInt(category, currentCount + data.size());
    }

    public void switchMode(Player player) {
        currentMode = currentMode.next();
        player.sendMessage(new TranslatableComponent(
                "trinary_synthesis.mode_switched"
        ), player.getUUID());
        setChanged();
    }

    @Override
    public boolean acceptsRedstone() {
        return false;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), RANGE);
    }

    @Override
    public int getMaxMana() {
        return 1000;
    }

    @Override
    public int getColor() {
        return currentMode.color;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_MODE, currentMode.id);
        tag.put(TAG_LEARNING_DATA, learningData);
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        currentMode = WorkMode.fromId(tag.getInt(TAG_MODE));
        learningData = tag.getCompound(TAG_LEARNING_DATA);
        cooldown = tag.getInt(TAG_COOLDOWN);
    }

    public WorkMode getCurrentMode() {
        return currentMode;
    }

    public CompoundTag getLearningData() {
        return learningData.copy();
    }

    public int getCooldown() {
        return cooldown;
    }
}