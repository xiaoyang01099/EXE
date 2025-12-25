package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.Future.GearFlowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModIntegrationFlowers;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.block.BlockSpecialFlower;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static vazkii.botania.common.block.BlockMod.createTickerHelper;

public class GearFlower extends BlockSpecialFlower implements EntityBlock {

    public static final BooleanProperty REVERSE_MODE = BooleanProperty.create("reverse_mode");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final IntegerProperty SPEED_LEVEL = IntegerProperty.create("speed_level", 0, 3);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public GearFlower(MobEffect stewEffect, int stewDuration, Properties props, Supplier<BlockEntityType<? extends TileEntitySpecialFlower>> blockEntityType) {
        super(stewEffect, stewDuration, props, blockEntityType);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(REVERSE_MODE, false)
                .setValue(WORKING, false)
                .setValue(SPEED_LEVEL, 0)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(REVERSE_MODE, WORKING, SPEED_LEVEL, POWERED);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModIntegrationFlowers.GEAR_FLOWER.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GearFlowerTile(ModIntegrationFlowers.GEAR_FLOWER_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModIntegrationFlowers.GEAR_FLOWER_TILE.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tickFlower());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GearFlowerTile tile) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (isWandItem(heldItem)) {
                return InteractionResult.PASS;
            }

            if (heldItem.isEmpty() || player.isCrouching()) {
                return toggleMode(state, level, pos, player, tile);
            }

            displayFlowerInfo(player, tile);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 检查物品是否是森林法杖
     */
    private boolean isWandItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemName = stack.getItem().getRegistryName().toString();
        return itemName.contains("wand") || itemName.contains("lexicon") ||
                itemName.equals("botania:twig_wand") || itemName.equals("botania:dreamwood_wand");
    }

    /**
     * 切换工作模式
     */
    private InteractionResult toggleMode(BlockState state, Level level, BlockPos pos, Player player, GearFlowerTile tile) {
        try {
            boolean newMode = !state.getValue(REVERSE_MODE);
            tile.setReverseMode(newMode);

            // 安全地更新方块状态
            BlockState newState = state.setValue(REVERSE_MODE, newMode);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }

            // 播放声音
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.8f,
                    newMode ? 1.2f : 0.8f);

            // 发送消息
            String modeKey = newMode ? "gear_flower.mode.kinetic_to_mana" : "gear_flower.mode.mana_to_kinetic";
            player.sendMessage(new TranslatableComponent(modeKey)
                    .withStyle(style -> style.withColor(newMode ? 0x00FF00 : 0x0099FF)), player.getUUID());

            // 粒子效果
            spawnModeChangeParticles(level, pos, newMode);

            return InteractionResult.SUCCESS;
        } catch (Exception e) {
            // 错误处理
            player.sendMessage(new TranslatableComponent("gear_flower.error.mode_switch")
                    .withStyle(style -> style.withColor(0xFF0000)), player.getUUID());
            return InteractionResult.FAIL;
        }
    }

    private void displayFlowerInfo(Player player, GearFlowerTile tile) {
        try {
            boolean reverseMode = tile.isReverseMode();
            int mana = tile.getMana();
            int maxMana = tile.getMaxMana();
            float currentStress = tile.getCurrentStress();
            float maxStress = tile.getMaxStress();
            float rpm = tile.getCurrentRPM();

            String modeKey = reverseMode ? "gear_flower.mode.kinetic_to_mana" : "gear_flower.mode.mana_to_kinetic";
            Component modeText = new TranslatableComponent(modeKey)
                    .withStyle(style -> style.withColor(reverseMode ? 0x00FF00 : 0x0099FF));

            if (reverseMode) {
                Component stressText = new TranslatableComponent("gear_flower.stress",
                        String.format("%.1f", currentStress), String.format("%.1f", maxStress))
                        .withStyle(style -> style.withColor(0xFFAA00));

                Component rpmText = new TranslatableComponent("gear_flower.rpm", String.format("%.1f", Math.abs(rpm)))
                        .withStyle(style -> style.withColor(0xFF6600));

                Component manaText = new TranslatableComponent("gear_flower.mana_generated", mana, maxMana)
                        .withStyle(style -> style.withColor(0x00FF00));

                player.sendMessage(modeText, player.getUUID());
                player.sendMessage(stressText, player.getUUID());
                player.sendMessage(rpmText, player.getUUID());
                player.sendMessage(manaText, player.getUUID());
            } else {
                Component manaText = new TranslatableComponent("gear_flower.mana_consumed", mana, maxMana)
                        .withStyle(style -> style.withColor(0x0099FF));

                Component outputStressText = new TranslatableComponent("gear_flower.stress_output",
                        String.format("%.1f", currentStress))
                        .withStyle(style -> style.withColor(0xFFAA00));

                Component outputRpmText = new TranslatableComponent("gear_flower.rpm_output", String.format("%.1f", rpm))
                        .withStyle(style -> style.withColor(0xFF6600));

                player.sendMessage(modeText, player.getUUID());
                player.sendMessage(manaText, player.getUUID());
                player.sendMessage(outputStressText, player.getUUID());
                player.sendMessage(outputRpmText, player.getUUID());
            }

            float efficiency = tile.getEfficiency();
            Component efficiencyText = new TranslatableComponent("gear_flower.efficiency",
                    String.format("%.1f%%", efficiency * 100))
                    .withStyle(style -> style.withColor(0x99FF99));
            player.sendMessage(efficiencyText, player.getUUID());
        } catch (Exception e) {
            player.sendMessage(new TranslatableComponent("gear_flower.error.info_display")
                    .withStyle(style -> style.withColor(0xFF0000)), player.getUUID());
        }
    }

    private void spawnModeChangeParticles(Level level, BlockPos pos, boolean reverseMode) {
        if (level instanceof ServerLevel serverLevel) {
            try {
                Vec3 center = Vec3.atCenterOf(pos).add(0, 0.5, 0);
                Random random = level.getRandom();

                for (int i = 0; i < 16; i++) {
                    double angle = (i / 16.0) * 2 * Math.PI;
                    double radius = 0.4 + random.nextDouble() * 0.3;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    double y = center.y + random.nextDouble() * 0.4;

                    if (reverseMode) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
                    } else {
                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                serverLevel.sendParticles(ParticleTypes.POOF,
                        center.x, center.y, center.z, 5,
                        0.2, 0.2, 0.2, 0.05);
            } catch (Exception e) {
                // 粒子生成失败，不影响功能
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);

        if (!state.getValue(WORKING)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GearFlowerTile tile) {
            animateWorkingEffects(state, level, pos, random, tile);
        }
    }

    private void animateWorkingEffects(BlockState state, Level level, BlockPos pos, Random random, GearFlowerTile tile) {
        try {
            Vec3 center = Vec3.atCenterOf(pos);
            boolean reverseMode = state.getValue(REVERSE_MODE);
            int speedLevel = state.getValue(SPEED_LEVEL);

            int particleChance = Math.max(1, 6 - speedLevel);

            if (random.nextInt(particleChance) == 0) {
                long time = level.getGameTime();
                double rotationSpeed = (speedLevel + 1) * 0.1;
                double angle = (time * rotationSpeed) % (2 * Math.PI);

                for (int i = 0; i < 8; i++) {
                    double gearAngle = angle + (i / 8.0) * 2 * Math.PI;
                    double radius = 0.3 + (speedLevel * 0.1);
                    double x = center.x + Math.cos(gearAngle) * radius;
                    double z = center.z + Math.sin(gearAngle) * radius;
                    double y = center.y + 0.2 + random.nextDouble() * 0.6;

                    if (reverseMode) {
                        level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.05, 0);
                    } else {
                        double velX = Math.cos(gearAngle) * 0.03;
                        double velZ = Math.sin(gearAngle) * 0.03;
                        level.addParticle(ParticleTypes.ENCHANT, x, y, z, velX, 0.01, velZ);
                    }
                }
            }

            if (speedLevel >= 2 && random.nextInt(8) == 0) {
                double x = center.x + (random.nextDouble() - 0.5) * 0.6;
                double y = center.y + 0.8 + random.nextDouble() * 0.3;
                double z = center.z + (random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.02, 0);
            }

            if (speedLevel == 3 && random.nextInt(15) == 0) {
                double x = center.x + (random.nextDouble() - 0.5) * 0.4;
                double y = center.y + 0.9;
                double z = center.z + (random.nextDouble() - 0.5) * 0.4;
                level.addParticle(ParticleTypes.LAVA, x, y, z, 0, 0.01, 0);
            }
        } catch (Exception e) {
            // 粒子效果失败，不影响功能
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.description")
                .withStyle(style -> style.withColor(0x9999FF)));

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.modes")
                .withStyle(style -> style.withColor(0xFFFF99)));

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.mode.mana_to_kinetic")
                .withStyle(style -> style.withColor(0x0099FF)));

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.mode.kinetic_to_mana")
                .withStyle(style -> style.withColor(0x00FF00)));

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.usage")
                .withStyle(style -> style.withColor(0x99FF99)));

        tooltip.add(new TranslatableComponent("gear_flower.tooltip.efficiency")
                .withStyle(style -> style.withColor(0xFFAA00)));
    }

    private void displayDetailedWandInfo(Player player, GearFlowerTile tile) {
        try {
            player.sendMessage(new TranslatableComponent("gear_flower.wand.header")
                    .withStyle(style -> style.withColor(0xFFFF00)), player.getUUID());

            boolean reverseMode = tile.isReverseMode();
            String modeKey = reverseMode ? "gear_flower.mode.kinetic_to_mana" : "gear_flower.mode.mana_to_kinetic";

            player.sendMessage(new TranslatableComponent("gear_flower.wand.current_mode",
                    new TranslatableComponent(modeKey))
                    .withStyle(style -> style.withColor(reverseMode ? 0x00FF00 : 0x0099FF)), player.getUUID());

            player.sendMessage(new TranslatableComponent("gear_flower.wand.conversion_rate",
                    tile.getConversionRate())
                    .withStyle(style -> style.withColor(0xFFAA00)), player.getUUID());

            player.sendMessage(new TranslatableComponent("gear_flower.wand.max_stress",
                    String.format("%.1f", tile.getMaxStress()))
                    .withStyle(style -> style.withColor(0xFF6600)), player.getUUID());

            player.sendMessage(new TranslatableComponent("gear_flower.wand.efficiency",
                    String.format("%.1f%%", tile.getEfficiency() * 100))
                    .withStyle(style -> style.withColor(0x99FF99)), player.getUUID());

            long workingTime = tile.getTotalWorkingTime();
            if (workingTime > 0) {
                player.sendMessage(new TranslatableComponent("gear_flower.wand.working_time",
                        workingTime / 20) // 转换为秒
                        .withStyle(style -> style.withColor(0x9999FF)), player.getUUID());
            }

            // 显示当前状态
            if (tile.isWorking()) {
                player.sendMessage(new TranslatableComponent("gear_flower.wand.status.working")
                        .withStyle(style -> style.withColor(0x00FF00)), player.getUUID());
            } else {
                player.sendMessage(new TranslatableComponent("gear_flower.wand.status.idle")
                        .withStyle(style -> style.withColor(0xFF6600)), player.getUUID());
            }
        } catch (Exception e) {
            player.sendMessage(new TranslatableComponent("gear_flower.error.wand_info")
                    .withStyle(style -> style.withColor(0xFF0000)), player.getUUID());
        }
    }

    private void spawnWandParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            try {
                Vec3 center = Vec3.atCenterOf(pos).add(0, 0.5, 0);

                for (int ring = 0; ring < 2; ring++) {
                    double radius = 0.4 + ring * 0.3;
                    int particleCount = 12 + ring * 8;

                    for (int i = 0; i < particleCount; i++) {
                        double angle = (i / (double) particleCount) * 2 * Math.PI;
                        if (ring == 1) angle += Math.PI / particleCount;

                        double x = center.x + Math.cos(angle) * radius;
                        double z = center.z + Math.sin(angle) * radius;
                        double y = center.y + (ring * 0.2);

                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                x, y, z, 1,
                                0.02, 0.02, 0.02, 0.01);
                    }
                }

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        center.x, center.y, center.z, 8,
                        0.3, 0.3, 0.3, 0.1);
            } catch (Exception e) {
                // 粒子生成失败，不影响功能
            }
        }
    }

    /**
     * 更新方块状态 - 改进版本
     */
    public void updateBlockState(Level level, BlockPos pos, GearFlowerTile tile) {
        try {
            BlockState currentState = level.getBlockState(pos);
            if (currentState.getBlock() != this) return; // 确保是正确的方块

            BlockState newState = currentState
                    .setValue(WORKING, tile.isWorking())
                    .setValue(SPEED_LEVEL, tile.getSpeedLevel())
                    .setValue(POWERED, tile.hasSufficientPower())
                    .setValue(REVERSE_MODE, tile.isReverseMode());

            if (!currentState.equals(newState)) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        } catch (Exception e) {
            // 状态更新失败，记录但不中断程序
            System.err.println("Failed to update GearFlower block state at " + pos + ": " + e.getMessage());
        }
    }
}