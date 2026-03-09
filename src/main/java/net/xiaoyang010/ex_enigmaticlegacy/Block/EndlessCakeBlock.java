package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.Random;

public class EndlessCakeBlock extends CakeBlock {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 6);

    public EndlessCakeBlock() {
        super(Properties.of(Material.CAKE).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (consumeCake(world, pos, state, player).consumesAction()) {
            return InteractionResult.SUCCESS;
        }
        if (itemstack.isEmpty()) {
            return InteractionResult.CONSUME;
        }

        return consumeCake(world, pos, state, player);
    }

    private InteractionResult consumeCake(Level world, BlockPos pos, BlockState state, Player player) {
        player.awardStat(Stats.EAT_CAKE_SLICE);
        player.getFoodData().eat(99, 99F);
        world.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);

        MobEffect[] effects = new MobEffect[]{
                MobEffects.MOVEMENT_SPEED,
                ModEffects.DAMAGE_REDUCTION.get(),
                MobEffects.DIG_SPEED,
                MobEffects.DAMAGE_BOOST,
                MobEffects.HEAL,
                MobEffects.REGENERATION,
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.FIRE_RESISTANCE,
                MobEffects.WATER_BREATHING,
                MobEffects.INVISIBILITY,
                MobEffects.NIGHT_VISION,
                MobEffects.HEALTH_BOOST,
                MobEffects.ABSORPTION,
                MobEffects.SATURATION,
                MobEffects.SLOW_FALLING,
                MobEffects.CONDUIT_POWER,
                MobEffects.HERO_OF_THE_VILLAGE,
                MobEffects.LUCK,
                ModEffects.CREEPER_FRIENDLY.get(),
                MobEffects.DOLPHINS_GRACE
        };

        Random random = new Random();
        boolean giveFlight = random.nextInt(100) < 10;

        if (giveFlight && player.isCreative()) {
            if (world.isClientSide())
                player.sendMessage(new TranslatableComponent("info.ex_enigmaticlegacy.flying.error"), player.getUUID());
        } else if (giveFlight) {
            if (world.isClientSide)
                player.sendMessage(new TranslatableComponent("info.ex_enigmaticlegacy.flying.star"), player.getUUID());
            if (!world.isClientSide)
                player.addEffect(new MobEffectInstance(ModEffects.FLYING.get(), 24000, 0));
        } else {
            MobEffect randomEffect = effects[random.nextInt(effects.length)];
            MobEffectInstance effectInstance = new MobEffectInstance(randomEffect, 24000, 9);
            if (!world.isClientSide)
                player.addEffect(effectInstance);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlockss.ENDLESS_CAKE.get());
    }
}
