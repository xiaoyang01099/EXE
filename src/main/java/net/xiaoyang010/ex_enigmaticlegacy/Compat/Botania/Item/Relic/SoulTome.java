package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySoulEnergy;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.proxy.IProxy;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class SoulTome extends Item implements ICursed, INoEMCItem {
    private static final float SOUL_TOME_DIVISOR = 10.0F;

    public SoulTome(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemSoulTome1.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome2.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome3.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome4.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome5.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome6.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome7.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome8.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemSoulTome9.lore"));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }

        tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    public boolean spawnSoul(Level world, LivingEntity player, LivingEntity target, @NotNull EntityType<EntitySoulEnergy> SoulEnergyEntityType) {
        if (player instanceof Player playerEntity) {
            ItemStack stack = playerEntity.getMainHandItem();
            if (stack.getItem() instanceof SoulTome) {
                var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
                if (relicCap.isPresent()) {
                    IRelic relic = relicCap.orElse(null);
                    if (relic != null && !relic.isRightPlayer(playerEntity)) {
                        return false;
                    }
                }
            }
        }

        if (!world.isClientSide) {
            Vector3 originalPos = Vector3.fromEntityCenter(player);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vector3 vector = originalPos.add(new Vector3(lookVec).multiply(1.0));
            vector.y += 0.5;
            Vector3 motion = new Vector3(lookVec).multiply(1.25);

            EntitySoulEnergy orb = new EntitySoulEnergy(SoulEnergyEntityType, world, player, target);
            orb.setPos(vector.x, vector.y, vector.z);
            orb.setDeltaMovement(motion.x, motion.y, motion.z);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, player.getSoundSource(), 2.0F, 0.8F + world.random.nextFloat() * 0.2F);
            world.addFreshEntity(orb);
            return true;
        }
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        player.setDeltaMovement(player.getDeltaMovement().multiply(0.5, 1.0, 0.5));

        int searchRange = 20;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(
                        player.getX() - searchRange, player.getY() - searchRange, player.getZ() - searchRange,
                        player.getX() + searchRange, player.getY() + searchRange, player.getZ() + searchRange
                )
        );

        entities.remove(player);

        int count = this.getUseDuration(stack) - remainingUseDuration;

        if (count > 20) {
            for (LivingEntity entity : entities) {
                if (player.distanceTo(entity) <= 3.0F) {
                    Vector3 entityVec = Vector3.fromEntityCenter(entity);
                    Vector3 playerVec = Vector3.fromEntityCenter(player);
                    Vector3 diff = entityVec.copy().sub(playerVec).multiply(1.0F / player.distanceTo(entity) * 3.0F);

                    if (!level.isClientSide) {
                        Vec3 startPos = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
                        Vec3 endPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());

                        for (int i = 0; i <= 3; i++) {
                            IProxy.INSTANCE.lightningFX(
                                    startPos,
                                    endPos,
                                    6.0F,
                                    0x8A2BE2,
                                    0xFFFFFF
                            );
                        }

                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.LIGHTNING_BOLT_THUNDER, player.getSoundSource(), 1.0F, 0.8F);
                    }

                    entity.hurt(new ModDamageSources.DamageSourceTLightning(player),
                            (float)(20.0F + 80.0F * Math.random()));
                    entity.setDeltaMovement(diff.x, diff.y + 1.0F, diff.z);
                }
            }
        }

        if (count > 20 && count % 4 == 0) {
            LivingEntity randomEntity = null;
            if (!entities.isEmpty()) {
                randomEntity = entities.get(level.random.nextInt(entities.size()));
            }

            if (randomEntity != null && !level.isClientSide) {
                float soulDamage = randomEntity.getMaxHealth() / SOUL_TOME_DIVISOR;
                if (soulDamage > 20.0F) {
                    soulDamage = 20.0F;
                } else if (soulDamage < 1.0F) {
                    soulDamage = 1.0F;
                }

                randomEntity.hurt(new ModDamageSources.DamageSourceSoulDrain(player), soulDamage);
                this.spawnSoul(level, randomEntity, player, ModEntities.SOUL_ENERGY.get());
            }
        }
    }
}