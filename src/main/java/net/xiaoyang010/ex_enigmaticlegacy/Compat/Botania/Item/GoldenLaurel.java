package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.BlockSpecialFlower;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import javax.annotation.Nullable;
import java.util.List;

public class GoldenLaurel extends ItemBauble {

    private static final int MANA_PER_TICK = 15;
    private static final int EFFECT_RANGE = 10;
    private static final float PARTICLE_RADIUS = 1.0F;
    private static final int PARTICLES_PER_CIRCLE = 8;
    private static final float PARTICLE_Y_OFFSET = 1.0F;

    public GoldenLaurel(Properties props) {
        super(props);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity entity) {

        if (!(entity instanceof Player player) || entity.level.isClientSide) {
            return;
        }

        Level level = player.level;

        if (level.isClientSide) {
            spawnDivineParticles(player);
        } else {
            handleManaAndFlowers(player);
        }
    }

    private void spawnDivineParticles(Player player) {
        Level level = player.level;
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        // 创建两个旋转的粒子圈
        for (int ring = 0; ring < 2; ring++) {
            double yOffset = PARTICLE_Y_OFFSET + ring * 0.5;
            float angle = (player.tickCount + ring * 45) * 0.1F; // 使两个圈旋转速度不同

            for (int i = 0; i < PARTICLES_PER_CIRCLE; i++) {
                float ringAngle = (i * 360F / PARTICLES_PER_CIRCLE) + angle;
                double x = playerX + Math.cos(Math.toRadians(ringAngle)) * PARTICLE_RADIUS;
                double z = playerZ + Math.sin(Math.toRadians(ringAngle)) * PARTICLE_RADIUS;
                double y = playerY + yOffset;

                // 使用发光粒子和金色粉尘粒子组合
                level.addParticle(ParticleTypes.END_ROD,
                        x, y, z,
                        0, 0, 0);

                level.addParticle(new DustParticleOptions(
                                new Vector3f(1.0F, 0.9F, 0.3F), 1.0F),
                        x, y, z,
                        0, 0, 0);
            }
        }
    }

    private void handleManaAndFlowers(Player player) {
        Level world = player.level;
        BlockPos playerPos = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-EFFECT_RANGE, -EFFECT_RANGE, -EFFECT_RANGE),
                playerPos.offset(EFFECT_RANGE, EFFECT_RANGE, EFFECT_RANGE))) {

            if (world.getBlockEntity(pos) instanceof IManaReceiver manaReceiver) {
                manaReceiver.receiveMana(MANA_PER_TICK);
            }

            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof BlockSpecialFlower) {
                world.getBlockState(pos).tick((ServerLevel) world, pos, world.random);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 检查玩家是否持有不死图腾
        if (!player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) &&
                !player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {

            // 检查玩家是否装备了月桂冠
            ItemStack laurel = EquipmentHandler.findOrEmpty(this, player);
            if (!laurel.isEmpty()) {
                event.setCanceled(true);

                // 恢复生命值并给予无敌效果
                player.setHealth(player.getMaxHealth());
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, true, false));

                // 播放效果音效
                Level world = player.level;
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.GOLDEN_LAUREL, // 使用正确的音效
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                // 使用后破碎
                laurel.shrink(1);

                // 发送提示消息
                player.sendMessage(
                        new TranslatableComponent("message.ex_enigmaticlegacy.laurel_protection")
                                .withStyle(ChatFormatting.GOLD),
                        player.getUUID()
                );
            }
        }
    }

    @Override
    public void onEquipped(ItemStack stack, LivingEntity entity) {
        super.onEquipped(stack, entity);
        // 装备时显示提示信息
        if (entity instanceof Player player) {
            player.sendMessage(
                    new TranslatableComponent("message.ex_enigmaticlegacy.laurel_blessing")
                            .withStyle(ChatFormatting.GOLD),
                    player.getUUID()
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
                                List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, world, tooltip, flags);
        // 添加物品提示信息
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc3")
                .withStyle(ChatFormatting.GRAY));
    }
}