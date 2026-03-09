package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.InfinityGaiaSpreaderTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.AsgardFlowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.block.tile.TileTerraPlate;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.relic.ItemRelic;
import vazkii.botania.common.item.relic.RelicImpl;

import java.awt.*;
import java.util.List;

public class GoldenLaurel extends ItemRelic implements ICurioItem, INoEMCItem {
    private static final int MANA_PER_TICK = 1500;
    private static final int EFFECT_RANGE = 10;

    public GoldenLaurel(Properties props) {
        super(props);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic == null || (entity instanceof Player && !relic.isRightPlayer((Player) entity))) {
                return;
            }
        } else {
            return;
        }

        onWornTick(stack, entity);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && (!(entity instanceof Player) || relic.isRightPlayer((Player) entity))) {
                onEquipped(stack, entity);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        onUnequipped(stack, slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                return relic != null && relic.isRightPlayer(player);
            }
            return false;
        }
        return false;
    }

    public void onWornTick(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        Level level = player.level;

        if (level.isClientSide) {
            spawnDivineParticles(player);
        } else {
            handleManaAndFlowers(player);
            if (level.getGameTime() % 3 == 0) {
                int range = 5;
                BlockPos playerPos = player.getOnPos();
                for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-range, 0, -range), playerPos.offset(range, 1, range))) {

                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof TilePool tilePool) {
                        if (!tilePool.isFull())
                            tilePool.receiveMana(1600);
                    } else if (blockEntity instanceof TileEntityFunctionalFlower functionalFlower) {
                        if (functionalFlower.getMana() < functionalFlower.getMaxMana()) {
                            functionalFlower.addMana(1600);
                        }
                    } else if (blockEntity instanceof TileEntityGeneratingFlower generatingFlower) {
                        if (generatingFlower.getMana() < generatingFlower.getMaxMana()) {
                            generatingFlower.addMana(1600);
                        }
                    } else if (blockEntity instanceof TileTerraPlate terraPlate) {
                        if (terraPlate.canReceiveManaFromBursts() && !terraPlate.isFull()) {
                            terraPlate.receiveMana(1600);
                        }
                    }
                }
            }
        }
    }

    private void spawnDivineParticles(Player player) {
        Level level = player.level;
        double playerX = player.getX();
        double playerY = player.getY() + player.getEyeHeight() + 1;
        double playerZ = player.getZ();

        float time = level.getGameTime() * 0.03F;

        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * 2 * Math.PI / 5 + time);
            double radius = 0.8;

            radius += Math.sin(time * 2) * 0.05;

            double x = playerX + Math.cos(angle) * radius;
            double y = playerY + Math.sin(time * 0.5) * 0.05;
            double z = playerZ + Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.GLOW,
                    x, y, z,
                    0, 0.001, 0);

            if (i % 2 == 0) {
                float hue = (time + i * 0.2f) % 1.0f;
                int color = Color.HSBtoRGB(hue, 0.7f, 1.0f);
                Vector3f particleColor = new Vector3f(
                        ((color >> 16) & 0xFF) / 255f,
                        ((color >> 8) & 0xFF) / 255f,
                        (color & 0xFF) / 255f
                );

                level.addParticle(new DustParticleOptions(particleColor, 0.7F),
                        x, y, z,
                        0, 0.001, 0);
            }
        }

        for (int i = 0; i < 2; i++) {
            if (level.random.nextFloat() < 0.3) {
                double angle = time * 2 + i * (Math.PI / 2);
                double dist = 1.2 + Math.sin(time + i) * 0.2;
                double x = playerX + Math.cos(angle) * dist;
                double y = playerY + Math.sin(time + i) * 0.2;
                double z = playerZ + Math.sin(angle) * dist;

                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        0, 0.02, 0);

                level.addParticle(ParticleTypes.END_ROD,
                        x, y, z,
                        0, 0.02, 0);

                level.addParticle(ParticleTypes.PORTAL,
                        x, y, z,
                        0, 0.02, 0);
            }
        }

        if (level.getGameTime() % 10 == 0) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double x = playerX + Math.cos(angle) * 0.3;
            double z = playerZ + Math.sin(angle) * 0.3;

            level.addParticle(ParticleTypes.END_ROD,
                    x, playerY - 0.2, z,
                    0, 0.05, 0);
        }

        double decorRadius = 1.5;
        for (int i = 0; i < 4; i++) {
            double decorAngle = time * 0.5 + (i * Math.PI / 4);
            float yOffset = (float) Math.sin(time + i) * 0.2f;

            double x = playerX + Math.cos(decorAngle) * decorRadius;
            double y = playerY + yOffset;
            double z = playerZ + Math.sin(decorAngle) * decorRadius;

            level.addParticle(ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    0, 0.01, 0);
        }
    }

    private void handleManaAndFlowers(Player player) {
        Level world = player.level;
        BlockPos playerPos = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-EFFECT_RANGE, -EFFECT_RANGE, -EFFECT_RANGE),
                playerPos.offset(EFFECT_RANGE, EFFECT_RANGE, EFFECT_RANGE))) {

            if (world.getBlockEntity(pos) instanceof IManaReceiver manaReceiver) {
                if (world.getBlockEntity(pos) instanceof AsgardFlowerTile
                        || world.getBlockEntity(pos) instanceof InfinityGaiaSpreaderTile) {
                    continue;
                }

                if (manaReceiver instanceof TileEntityFunctionalFlower functionalFlower) {
                    functionalFlower.addMana(MANA_PER_TICK);
                    functionalFlower.tickFlower();
                }
                else if (manaReceiver instanceof TileEntityGeneratingFlower generatingFlower) {
                    generatingFlower.tickFlower();
                    generatingFlower.addMana(MANA_PER_TICK);
                    generatingFlower.setChanged();
                    if (world instanceof ServerLevel) {
                        generatingFlower.sync();
                    }
                }
                else if (!manaReceiver.isFull()) {
                    manaReceiver.receiveMana(MANA_PER_TICK);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) &&
                !player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {

            ItemStack laurel = EquipmentHandler.findOrEmpty(this, player);
            if (!laurel.isEmpty()) {
                var relicCap = laurel.getCapability(BotaniaForgeCapabilities.RELIC);
                if (relicCap.isPresent()) {
                    IRelic relic = relicCap.orElse(null);
                    if (relic != null && relic.isRightPlayer(player)) {
                        event.setCanceled(true);

                        player.setHealth(player.getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, true, false));

                        Level world = player.level;
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.GOLDEN_LAUREL,
                                SoundSource.PLAYERS, 1.0F, 1.0F);

                        fillManaInRange(player);
                        knockbackEntities(player);
                        laurel.shrink(1);

                        player.sendMessage(
                                new TranslatableComponent("message.ex_enigmaticlegacy.laurel_protection")
                                        .withStyle(ChatFormatting.GOLD),
                                player.getUUID()
                        );
                    }
                }
            }
        }
    }

    private void fillManaInRange(Player player) {
        Level world = player.level;
        BlockPos playerPos = player.blockPosition();

        if (world instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 50; i++) {
                double x = player.getX() + (world.random.nextDouble() - 0.5D) * EFFECT_RANGE * 2;
                double y = player.getY() + (world.random.nextDouble() - 0.5D) * EFFECT_RANGE * 2;
                double z = player.getZ() + (world.random.nextDouble() - 0.5D) * EFFECT_RANGE * 2;

                float hue = world.random.nextFloat();
                int color = Color.HSBtoRGB(hue, 0.7f, 1.0f);
                Vector3f particleColor = new Vector3f(
                        ((color >> 16) & 0xFF) / 255f,
                        ((color >> 8) & 0xFF) / 255f,
                        (color & 0xFF) / 255f
                );

                serverLevel.sendParticles(new DustParticleOptions(particleColor, 2.0F),
                        x, y, z,
                        1, 0, 0, 0, 0);
            }
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-EFFECT_RANGE, -EFFECT_RANGE, -EFFECT_RANGE),
                playerPos.offset(EFFECT_RANGE, EFFECT_RANGE, EFFECT_RANGE))) {

            if (world.getBlockEntity(pos) instanceof IManaReceiver manaReceiver) {
                if (manaReceiver instanceof TileEntityFunctionalFlower functionalFlower) {
                    int maxMana = functionalFlower.getMaxMana();
                    functionalFlower.addMana(maxMana);
                } else if (manaReceiver instanceof IManaPool pool) {
                    for (int i = 0; i < 5; i++) {
                        pool.receiveMana(1000000);
                    }
                }
            }

            if (world.getBlockEntity(pos) instanceof TileEntityGeneratingFlower generatingFlower) {
                int maxMana = generatingFlower.getMaxMana();
                generatingFlower.addMana(maxMana);
                generatingFlower.addMana(maxMana);
                generatingFlower.setChanged();
                if (world instanceof ServerLevel) {
                    generatingFlower.sync();
                }
            }
        }
    }

    private void knockbackEntities(Player player) {
        Level world = player.level;
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(EFFECT_RANGE),
                entity -> entity != player);

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - player.getX();
            double dz = entity.getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance != 0) {
                double strength = 15.0;
                entity.setDeltaMovement(
                        (dx / distance) * strength,
                        1.0,
                        (dz / distance) * strength
                );

                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    public void onEquipped(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.sendMessage(
                    new TranslatableComponent("message.ex_enigmaticlegacy.laurel_blessing")
                            .withStyle(ChatFormatting.GOLD),
                    player.getUUID()
            );
        }
    }

    public void onUnequipped(ItemStack stack, LivingEntity entity) {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
                                List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, world, tooltip, flags);

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.golden_laurel.desc3")
                .withStyle(ChatFormatting.GRAY));
    }
}