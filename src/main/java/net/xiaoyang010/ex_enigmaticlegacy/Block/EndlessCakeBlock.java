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
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EndlessCakeBlock extends CakeBlock {
    private static final List<String> playersWithFlyEffect = new ArrayList<>();
    public static final String NBT_FLYING = "exe:flying";

    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 6);

    public EndlessCakeBlock() {
        super(Block.Properties.of(Material.CAKE).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));

        MinecraftForge.EVENT_BUS.register(this); // 注册事件
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
        player.getFoodData().eat(99, 99F); // 增加饱食度，不受饱食度限制
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
                MobEffects.LUCK
        };

        Random random = new Random();
        boolean giveFlight = random.nextInt(100) < 10; // 10% 概率给飞行能力

        if (giveFlight && player.isCreative()) {
            // 如果玩家在创造模式，不做飞行操作，创造模式自带飞行
            if (world.isClientSide())
                player.sendMessage(new TranslatableComponent("info.ex_enigmaticlegacy.flying.error"), player.getUUID());
        } else if (giveFlight) {
            if (world.isClientSide)
                player.sendMessage(new TranslatableComponent("info.ex_enigmaticlegacy.flying.star"), player.getUUID());
            if (!world.isClientSide)
                player.addEffect(new MobEffectInstance(ModEffects.FLYING.get(), 24000, 0));
        } else {
            // 随机给予增益效果
            MobEffect randomEffect = effects[random.nextInt(effects.length)];
            MobEffectInstance effectInstance = new MobEffectInstance(randomEffect, 24000, 9); // 20分钟，等级9
            if (!world.isClientSide)
                player.addEffect(effectInstance);
        }

        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public void playerFall(LivingFallEvent event){
        LivingEntity living = event.getEntityLiving();
        if (living instanceof Player player) {
            boolean flying = player.getPersistentData().getBoolean(NBT_FLYING);
            if (flying && !player.level.isClientSide){ //取消玩家摔落
                player.getPersistentData().remove(NBT_FLYING); //重置标记
                event.setDamageMultiplier(0);
            }
        }
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        // 返回该方块的掉落物为自身
        return new ItemStack(ModBlocks.ENDLESS_CAKE.get());
    }
}
