package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class EndlessCakeBlock extends CakeBlock {

    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 6);

    // 保存玩家的飞行剩余时间
    private static final HashMap<UUID, Integer> flightTimers = new HashMap<>();
    // 保存玩家的缓降状态，确保着陆时取消缓降效果
    private static final HashMap<UUID, Boolean> slowFallStatus = new HashMap<>();

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
        if (world.isClientSide) {
            if (consumeCake(world, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (itemstack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
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
            player.sendMessage(new TextComponent("你现在是创造，这个效果没有用的"), player.getUUID());
        } else if (giveFlight) {

            player.sendMessage(new TextComponent("你现在有20分钟的飞行体验时间啦！"), player.getUUID());

            // 设置飞行计时器为24000 ticks (20分钟)
            flightTimers.put(player.getUUID(), 20 * 60 * 20 );
            slowFallStatus.put(player.getUUID(), false); // 初始时不处于缓降状态
        } else {
            // 随机给予增益效果
            MobEffect randomEffect = effects[random.nextInt(effects.length)];
            MobEffectInstance effectInstance = new MobEffectInstance(randomEffect, 24000, 9); // 20分钟，等级9
            player.addEffect(effectInstance);
        }

        return InteractionResult.SUCCESS;
    }

    // 在每个 tick 检查玩家飞行时间
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        UUID playerUUID = player.getUUID();

        if (!player.level.isClientSide) {
            if (flightTimers.containsKey(playerUUID)) {
                int remainingTime = flightTimers.get(playerUUID);

                if (remainingTime > 0) {
                    // 给玩家飞行能力（仅限生存模式）
                    player.getAbilities().mayfly = true;
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                    flightTimers.put(playerUUID, remainingTime - 1); // 递减飞行时间
                } else {
                    // 飞行时间结束，取消飞行并提示玩家
                    if (!player.isCreative()) {  // 只在非创造模式下移除飞行
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                        player.sendMessage(new TextComponent("你的体验结束啦,如果你还在飞行，请别怕，神秘的力量会把你安全送到地面"), player.getUUID());
                    }

                    // 如果玩家仍然在空中，给予缓降效果
                    if (!player.isOnGround()) {
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, Integer.MAX_VALUE, 1)); // 无限缓降
                        slowFallStatus.put(playerUUID, true); // 标记玩家处于缓降状态
                    }

                    flightTimers.remove(playerUUID); // 移除飞行计时器
                }
            }

            // 如果玩家处于缓降状态，检查其是否已着陆
            if (slowFallStatus.getOrDefault(playerUUID, false) && player.isOnGround()) {
                // 玩家已着陆，移除缓降效果
                player.removeEffect(MobEffects.SLOW_FALLING);
                slowFallStatus.remove(playerUUID); // 移除缓降状态
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
        return new ItemStack(ModBlockss.ENDLESS_CAKE.get());
    }
}
