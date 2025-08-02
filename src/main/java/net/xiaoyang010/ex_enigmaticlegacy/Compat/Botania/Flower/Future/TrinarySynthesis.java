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

public class TrinarySynthesis extends BlockSpecialFlower implements EntityBlock, IWandable {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 2);
    public static final BooleanProperty ENHANCED = BooleanProperty.create("enhanced");

    public TrinarySynthesis(MobEffect stewEffect, int stewDuration, Properties props, Supplier<BlockEntityType<? extends TileEntitySpecialFlower>> blockEntityType) {
        super(stewEffect, stewDuration, props, blockEntityType);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(ENHANCED, false)
                .setValue(MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, MODE, ENHANCED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrinarySynthesisTile(ModIntegrationFlowers.TRINARY_SYNTHESIS_TILE.get(), pos, state);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModIntegrationFlowers.TRINARY_SYNTHESIS.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModIntegrationFlowers.TRINARY_SYNTHESIS_TILE.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tickFlower());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrinarySynthesisTile tile) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.isEmpty() || player.isCrouching()) {
                tile.switchMode(player);
                updateBlockState(level, pos, state, tile);

                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME, SoundSource.BLOCKS,
                        0.8f, 1.0f + (tile.getCurrentMode().id * 0.2f));

                spawnModeChangeParticles(level, pos, tile.getCurrentMode().color);

                return InteractionResult.SUCCESS;
            }

            displayFlowerInfo(player, tile);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void updateBlockState(Level level, BlockPos pos, BlockState state, TrinarySynthesisTile tile) {
        BlockState newState = state
                .setValue(MODE, tile.getCurrentMode().id)
                .setValue(ACTIVE, tile.getMana() > 0)
                .setValue(ENHANCED, tile.getCooldown() > 0);

        if (!state.equals(newState)) {
            level.setBlock(pos, newState, 3);
        }
    }


    private void displayFlowerInfo(Player player, TrinarySynthesisTile tile) {
        TrinarySynthesisTile.WorkMode currentMode = tile.getCurrentMode();
        int mana = tile.getMana();
        int maxMana = tile.getMaxMana();
        int cooldown = tile.getCooldown();

        Component modeText = new TranslatableComponent("trinary_synthesis.mode." + currentMode.name)
                .withStyle(style -> style.withColor(currentMode.color));

        Component manaText = new TranslatableComponent("trinary_synthesis.mana", mana, maxMana)
                .withStyle(style -> style.withColor(0x00FF00));

        if (cooldown > 0) {
            Component cooldownText = new TranslatableComponent("trinary_synthesis.cooldown", cooldown / 20)
                    .withStyle(style -> style.withColor(0xFF6600));
            player.sendMessage(cooldownText, player.getUUID());
        }

        player.sendMessage(modeText, player.getUUID());
        player.sendMessage(manaText, player.getUUID());

        CompoundTag learningData = tile.getLearningData();
        if (!learningData.isEmpty()) {
            int kinetics = learningData.getInt("common_kinetics");
            if (kinetics > 0) {
                Component learningText = new TranslatableComponent("trinary_synthesis.learning", kinetics)
                        .withStyle(style -> style.withColor(0x9999FF));
                player.sendMessage(learningText, player.getUUID());
            }
        }
    }

    private void spawnModeChangeParticles(Level level, BlockPos pos, int color) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = Vec3.atCenterOf(pos).add(0, 0.8, 0);
            Random random = level.getRandom();

            for (int i = 0; i < 12; i++) {
                double angle = (i / 12.0) * 2 * Math.PI;
                double x = center.x + Math.cos(angle) * 0.5;
                double z = center.z + Math.sin(angle) * 0.5;
                double y = center.y + random.nextDouble() * 0.5;

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        x, y, z, 1,
                        0.1, 0.1, 0.1, 0.1);
            }

            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                    center.x, center.y, center.z, 3,
                    0.2, 0.2, 0.2, 0.05);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);

        if (!state.getValue(ACTIVE)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrinarySynthesisTile tile) {
            animateFlowerEffects(state, level, pos, random, tile);
        }
    }

    private void animateFlowerEffects(BlockState state, Level level, BlockPos pos, Random random, TrinarySynthesisTile tile) {
        Vec3 center = Vec3.atCenterOf(pos);
        TrinarySynthesisTile.WorkMode mode = tile.getCurrentMode();

        if (random.nextInt(4) == 0) {
            double x = center.x + (random.nextDouble() - 0.5) * 0.8;
            double y = center.y + 0.3 + random.nextDouble() * 0.8;
            double z = center.z + (random.nextDouble() - 0.5) * 0.8;

            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0, 0.02, 0);
        }

        switch (mode) {
            case ARCANE_MECHANIST:
                if (random.nextInt(8) == 0) {
                    double x = center.x + (random.nextDouble() - 0.5) * 1.2;
                    double z = center.z + (random.nextDouble() - 0.5) * 1.2;
                    double y = center.y + 0.5;
                    level.addParticle(ParticleTypes.CRIT, x, y, z, 0, 0.1, 0);
                }
                break;

            case TRINKET_RESONATOR:
                if (random.nextInt(6) == 0) {
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double radius = 0.8 + random.nextDouble() * 0.4;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    double y = center.y + 0.3 + random.nextDouble() * 0.6;
                    level.addParticle(ParticleTypes.WITCH, x, y, z, 0, 0.02, 0);
                }
                break;

            case FUSION_CATALYST:
                if (random.nextInt(3) == 0) {
                    double x = center.x + (random.nextDouble() - 0.5) * 1.5;
                    double y = center.y + 0.8 + random.nextDouble() * 0.5;
                    double z = center.z + (random.nextDouble() - 0.5) * 1.5;
                    level.addParticle(ParticleTypes.END_ROD, x, y, z,
                            (random.nextDouble() - 0.5) * 0.1, 0.05,
                            (random.nextDouble() - 0.5) * 0.1);
                }
                break;
        }

        if (state.getValue(ENHANCED) && random.nextInt(2) == 0) {
            double x = center.x + (random.nextDouble() - 0.5) * 0.6;
            double y = center.y + 1.0 + random.nextDouble() * 0.3;
            double z = center.z + (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0.03, 0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.description")
                .withStyle(style -> style.withColor(0x9999FF)));

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.modes")
                .withStyle(style -> style.withColor(0xFFFF99)));

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.mode.arcane_mechanist")
                .withStyle(style -> style.withColor(0x00AAFF)));

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.mode.trinket_resonator")
                .withStyle(style -> style.withColor(0xAA00FF)));

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.mode.fusion_catalyst")
                .withStyle(style -> style.withColor(0xFFAA00)));

        tooltip.add(new TranslatableComponent("trinary_synthesis.tooltip.usage")
                .withStyle(style -> style.withColor(0x99FF99)));
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player == null) return false;

        Level level = player.level;
        if (level.isClientSide) {
            return true;
        }

        BlockPos pos = player.blockPosition();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrinarySynthesisTile tile) {
            displayDetailedInfo(player, tile);

            spawnWandParticles(level, pos);

            return true;
        }

        return false;
    }

    private void displayDetailedInfo(Player player, TrinarySynthesisTile tile) {
        TrinarySynthesisTile.WorkMode currentMode = tile.getCurrentMode();

        player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.header")
                .withStyle(style -> style.withColor(0xFFFF00)), player.getUUID());

        player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.current_mode",
                new TranslatableComponent("trinary_synthesis.mode." + currentMode.name))
                .withStyle(style -> style.withColor(currentMode.color)), player.getUUID());

        player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.mana_cost", currentMode.manaCost)
                .withStyle(style -> style.withColor(0x00FF00)), player.getUUID());

        player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.range", 9)
                .withStyle(style -> style.withColor(0x99FF99)), player.getUUID());

        if (tile.getCooldown() > 0) {
            player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.cooldown", tile.getCooldown() / 20)
                    .withStyle(style -> style.withColor(0xFF6600)), player.getUUID());
        }

        CompoundTag learningData = tile.getLearningData();
        if (!learningData.isEmpty()) {
            player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.learning_data")
                    .withStyle(style -> style.withColor(0x9999FF)), player.getUUID());

            int kinetics = learningData.getInt("common_kinetics");
            if (kinetics > 0) {
                player.sendMessage(new TranslatableComponent("trinary_synthesis.wand.kinetics_enhanced", kinetics)
                        .withStyle(style -> style.withColor(0x00AAFF)), player.getUUID());
            }
        }
    }

    private void spawnWandParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = Vec3.atCenterOf(pos).add(0, 0.5, 0);

            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 4 * Math.PI;
                double height = (i / 20.0) * 2;
                double radius = 0.3 + (i / 20.0) * 0.7;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double y = center.y + height;

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        x, y, z, 1,
                        0.05, 0.05, 0.05, 0.02);
            }
        }
    }
}