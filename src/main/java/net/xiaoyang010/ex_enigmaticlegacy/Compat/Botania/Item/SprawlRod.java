package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySeed;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.api.block.IFloatingFlower.IslandType;
import vazkii.botania.common.item.ItemGrassSeeds;
import vazkii.botania.common.item.IFloatingFlowerVariant;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Optional;

import static vazkii.botania.api.block.IFloatingFlower.IslandType.*;

public class SprawlRod extends Item {
    private static final int MANA_COST = 760;
    private static final int MAX_USE_TIME = 128;
    private static final int MAX_MANA = 10000;
    private static final String TAG_MANA = "mana";

    public SprawlRod(Properties properties) {
        super(properties.stacksTo(1).setNoRepair());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!hasSufficientMana(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }

        ItemStack seed = findGrassSeeds(player);
        if (seed == null) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTime = this.getUseDuration(stack) - timeLeft;
        fireSeed(stack, world, player, useTime);
    }

    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (entity instanceof Player player) {
            fireSeed(stack, world, player, MAX_USE_TIME);
        }
        return stack;
    }

    private void fireSeed(ItemStack rod, Level world, Player player, int useTime) {
        if (!consumeMana(rod, player)) {
            return;
        }

        ItemStack seedStack = null;
        int seedSlot = -1;

        for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isGrassSeeds(stack)) {
                seedStack = stack;
                seedSlot = i;
                break;
            }
        }

        if (seedStack == null || seedSlot == -1) {
            return;
        }

        if (!world.isClientSide) {
            EntitySeed entitySeed = createEntitySeed(world, player);
            if (entitySeed != null) {
                entitySeed.setSeed(seedStack.copy());

                int maxArea = ConfigHandler.NebulaRodConfig.getSprawlRodMaxArea();
                entitySeed.setRadius((int)(Math.min((float)useTime, (float)MAX_USE_TIME) / (float)MAX_USE_TIME * (float)maxArea));
                entitySeed.setAttacker(player.getName().getString());

                Vec3 look = player.getLookAngle();
                double speed = ConfigHandler.NebulaRodConfig.getSprawlRodSpeed();
                entitySeed.setDeltaMovement(look.x * speed, look.y * speed + 0.1, look.z * speed);

                world.addFreshEntity(entitySeed);

                if (seedStack.getCount() > 1) {
                    seedStack.shrink(1);
                } else {
                    player.getInventory().setItem(seedSlot, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (world.isClientSide) {
            int time = this.getUseDuration(stack) - remainingUseDuration;
            if (time % 2 == 0 && time != 0) {
                return;
            }

            ItemStack seed = findGrassSeeds(player);
            if (seed == null) {
                return;
            }

            int ticks = Math.min(MAX_USE_TIME, time);
            float fTicks = (float)ticks / (float)MAX_USE_TIME;
            Vec3 look = player.getLookAngle();
            double posX = player.getX() + look.x * 1.4F + (Math.random() - 0.5F) * fTicks * 0.3F;
            double posY = player.getY() + player.getEyeHeight() + look.y * 1.4F + (Math.random() - 0.5F) * fTicks * 0.3F;
            double posZ = player.getZ() + look.z * 1.4F + (Math.random() - 0.5F) * fTicks * 0.3F;

            Color color = getSeedColor(seed);
            float particleSize = 0.5F * fTicks - (float)(Math.random() * 0.1F);

            BotaniaAPI.instance().sparkleFX(world, posX, posY, posZ,
                    (float)color.getRed() / 255.0F,
                    (float)color.getGreen() / 255.0F,
                    (float)color.getBlue() / 255.0F,
                    particleSize,
                    6);
        }
    }

    private boolean hasSufficientMana(ItemStack stack, Player player) {
        int rodMana = getManaInternal(stack);
        if (rodMana >= MANA_COST) {
            return true;
        }

        int remainingCost = MANA_COST - rodMana;
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, remainingCost, false);
    }

    private boolean consumeMana(ItemStack stack, Player player) {
        int rodMana = getManaInternal(stack);

        if (rodMana >= MANA_COST) {
            setManaInternal(stack, rodMana - MANA_COST);
            return true;
        } else {
            int remainingCost = MANA_COST - rodMana;
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, remainingCost, true)) {
                setManaInternal(stack, 0);
                return true;
            }
        }
        return false;
    }

    protected int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    protected void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.min(mana, MAX_MANA));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ManaItemCapabilityProvider(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new ManaBarTooltip(getManaInternal(stack) / (float) MAX_MANA));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    private ItemStack findGrassSeeds(Player player) {
        for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isGrassSeeds(stack)) {
                return stack;
            }
        }
        return null;
    }

    private boolean isGrassSeeds(ItemStack stack) {
        return stack.getItem() instanceof ItemGrassSeeds ||
                (ModItems.grassSeeds != null && stack.getItem() == ModItems.grassSeeds);
    }

    private EntitySeed createEntitySeed(Level world, Player player) {
        return new EntitySeed(ModEntities.ENTITY_SEED.get(), world, player);
    }

    public static Color getSeedColor(ItemStack seed) {
        if (seed.getItem() instanceof ItemGrassSeeds) {
            ItemGrassSeeds grassSeeds = (ItemGrassSeeds) seed.getItem();
            if (grassSeeds instanceof IFloatingFlowerVariant) {
                IslandType type = grassSeeds.getIslandType(seed);
                return getColorForIslandType(type);
            }
        }

        int meta = seed.getDamageValue();
        return getColorForDamage(meta);
    }

    private static Color getColorForIslandType(IslandType type) {
        if (type.equals(GRASS)) {
            return new Color(0, 102, 0);
        } else if (type.equals(PODZOL)) {
            return new Color(127, 94, 0);
        } else if (type.equals(MYCEL)) {
            return new Color(69, 0, 84);
        } else if (type.equals(DRY)) {
            return new Color(102, 127, 13);
        } else if (type.equals(GOLDEN)) {
            return new Color(191, 178, 0);
        } else if (type.equals(VIVID)) {
            return new Color(0, 127, 25);
        } else if (type.equals(SCORCHED)) {
            return new Color(191, 0, 0);
        } else if (type.equals(INFUSED)) {
            return new Color(0, 140, 140);
        } else if (type.equals(MUTATED)) {
            return new Color(102, 25, 102);
        }
        return new Color(0, 102, 0);
    }

    private static Color getColorForDamage(int meta) {
        float r = 0.0F;
        float g = 0.4F;
        float b = 0.0F;

        switch (meta) {
            case 1:
                r = 0.5F; g = 0.37F; b = 0.0F;
                break;
            case 2:
                r = 0.27F; g = 0.0F; b = 0.33F;
                break;
            case 3:
                r = 0.4F; g = 0.5F; b = 0.05F;
                break;
            case 4:
                r = 0.75F; g = 0.7F; b = 0.0F;
                break;
            case 5:
                r = 0.0F; g = 0.5F; b = 0.1F;
                break;
            case 6:
                r = 0.75F; g = 0.0F; b = 0.0F;
                break;
            case 7:
                r = 0.0F; g = 0.55F; b = 0.55F;
                break;
            case 8:
                r = 0.4F; g = 0.1F; b = 0.4F;
                break;
        }

        return new Color(r, g, b);
    }

    private static class ManaItemCapabilityProvider implements ICapabilityProvider {
        private final ItemStack stack;
        private final LazyOptional<IManaItem> manaItemOptional;

        public ManaItemCapabilityProvider(ItemStack stack) {
            this.stack = stack;
            this.manaItemOptional = LazyOptional.of(() -> new ManaItemImpl(stack));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                return manaItemOptional.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static class ManaItemImpl implements IManaItem {
        private final ItemStack stack;

        public ManaItemImpl(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return stack.getOrCreateTag().getInt(TAG_MANA);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            int current = getMana();
            stack.getOrCreateTag().putInt(TAG_MANA, Math.min(current + mana, MAX_MANA));
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }
}