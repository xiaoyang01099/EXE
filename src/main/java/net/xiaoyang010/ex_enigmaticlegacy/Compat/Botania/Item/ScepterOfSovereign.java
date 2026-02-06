package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.BlockModFlower;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ScepterOfSovereign extends Item {
    private static final int MAX_STORED_SOULS = 10;
    private static final int MANA_COST = 10000;
    private static final int PLANT_SEARCH_RANGE = 3;
    private static final int REQUIRED_PLANTS = 9;
    private static final String TAG_SOULS = "StoredSouls";
    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_ENTITY_NBT = "EntityNBT";
    private static final String TAG_DEATH_TIME = "DeathTime";
    private static final String TAG_OWNER_UUID = "OwnerUUID";

    public ScepterOfSovereign(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            ItemStack soulFragment = findSoulFragment(player);
            if (soulFragment.isEmpty()) {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.no_soul_fragment")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            CompoundTag nbt = stack.getOrCreateTag();
            if (!nbt.contains(TAG_SOULS)) {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.no_souls")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            ListTag soulsList = nbt.getList(TAG_SOULS, Tag.TAG_COMPOUND);
            if (soulsList.isEmpty()) {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.no_souls")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            if (!hasMana(player)) {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.no_mana", MANA_COST)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            List<BlockPos> plantPositions = findNearbyPlants(serverLevel, player.blockPosition());
            if (plantPositions.size() < REQUIRED_PLANTS) {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.no_plants",
                                plantPositions.size(), REQUIRED_PLANTS)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            CompoundTag soulData = soulsList.getCompound(soulsList.size() - 1);

            if (reviveEntity(serverLevel, player, soulData)) {
                soulFragment.shrink(1);
                consumeMana(player);
                consumePlants(serverLevel, plantPositions, REQUIRED_PLANTS);
                soulsList.remove(soulsList.size() - 1);
                if (soulsList.isEmpty()) {
                    nbt.remove(TAG_SOULS);
                } else {
                    nbt.put(TAG_SOULS, soulsList);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);

                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.success")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );

                return InteractionResultHolder.success(stack);
            } else {
                player.displayClientMessage(
                        new TranslatableComponent("item.scepter_of_sovereign.failed")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private boolean hasMana(Player player) {
        return ManaItemHandler.instance().requestManaExact(new ItemStack(this), player, ScepterOfSovereign.MANA_COST, true);
    }

    private void consumeMana(Player player) {
        ManaItemHandler.instance().requestManaExact(new ItemStack(this), player, ScepterOfSovereign.MANA_COST, false);
    }

    private List<BlockPos> findNearbyPlants(ServerLevel level, BlockPos center) {
        List<BlockPos> plants = new ArrayList<>();

        for (int x = -PLANT_SEARCH_RANGE; x <= PLANT_SEARCH_RANGE; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -PLANT_SEARCH_RANGE; z <= PLANT_SEARCH_RANGE; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (isPlant(state)) {
                        plants.add(pos);
                    }
                }
            }
        }

        return plants;
    }

    private boolean isPlant(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof BlockModFlower) {
            return true;
        }

        if (state.is(BlockTags.FLOWERS) ||
                state.is(BlockTags.TALL_FLOWERS) ||
                state.is(BlockTags.CROPS) ||
                state.is(BlockTags.SAPLINGS) ||
                state.is(BlockTags.SMALL_FLOWERS)) {
            return true;
        }

        return block == Blocks.GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.BROWN_MUSHROOM ||
                block == Blocks.RED_MUSHROOM ||
                block == Blocks.CRIMSON_FUNGUS ||
                block == Blocks.WARPED_FUNGUS ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.BAMBOO ||
                block == Blocks.SUGAR_CANE ||
                block == Blocks.CACTUS ||
                block == Blocks.SEAGRASS ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.KELP ||
                block == Blocks.KELP_PLANT ||
                block == Blocks.LILY_PAD ||
                block == Blocks.VINE ||
                block == Blocks.TWISTING_VINES ||
                block == Blocks.WEEPING_VINES ||
                block == Blocks.COCOA ||
                block == Blocks.CRIMSON_ROOTS ||
                block == Blocks.WARPED_ROOTS ||
                block == Blocks.NETHER_SPROUTS ||
                block == Blocks.WARPED_WART_BLOCK ||
                block == Blocks.NETHER_WART ||
                block == Blocks.CHORUS_PLANT ||
                block == Blocks.CHORUS_FLOWER;
    }

    private void consumePlants(ServerLevel level, List<BlockPos> plants, int amount) {
        List<BlockPos> shuffled = new ArrayList<>(plants);
        java.util.Collections.shuffle(shuffled);

        for (int i = 0; i < Math.min(amount, shuffled.size()); i++) {
            BlockPos pos = shuffled.get(i);
            BlockState state = level.getBlockState(pos);

            level.destroyBlock(pos, false);

            level.playSound(null, pos, state.getSoundType().getBreakSound(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private ItemStack findSoulFragment(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.DEAD_SUBSTANCE.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean reviveEntity(ServerLevel level, Player player, CompoundTag soulData) {
        try {
            String entityTypeString = soulData.getString(TAG_ENTITY_TYPE);
            Optional<EntityType<?>> entityTypeOpt = EntityType.byString(entityTypeString);

            if (entityTypeOpt.isEmpty()) {
                return false;
            }

            EntityType<?> entityType = entityTypeOpt.get();

            Vec3 lookVec = player.getLookAngle();
            BlockPos spawnPos = player.blockPosition().offset(
                    (int) (lookVec.x * 2),
                    0,
                    (int) (lookVec.z * 2)
            );

            Entity entity = entityType.create(level);
            if (entity == null) {
                return false;
            }

            CompoundTag entityNBT = soulData.getCompound(TAG_ENTITY_NBT);
            entity.load(entityNBT);

            entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            if (entity instanceof TamableAnimal tamable) {
                UUID ownerUUID = soulData.getUUID(TAG_OWNER_UUID);
                tamable.setOwnerUUID(ownerUUID);
                tamable.setTame(true);
            }

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setHealth(livingEntity.getMaxHealth());
            }

            level.addFreshEntity(entity);

            spawnRevivalParticles(level, entity.position());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void spawnRevivalParticles(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            float r = level.random.nextFloat() * 0.5f + 0.5f;
            float g = level.random.nextFloat() * 0.5f + 0.5f;
            float b = level.random.nextFloat() * 0.5f + 0.5f;

            WispParticleData data = WispParticleData.wisp(
                    0.3F + level.random.nextFloat() * 0.2F,
                    r, g, b,
                    1
            );
            level.sendParticles(data,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0.05, 0, 0.02
            );
        }

        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2 * i) / 20;
            double radius = 1.5;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + level.random.nextDouble() * 2;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    1.0F + level.random.nextFloat(),
                    0.3F, 1.0F, 0.3F,
                    5
            );
            level.sendParticles(sparkle, x, y, z, 1, 0, 0, 0, 0);
        }

        for (int i = 0; i < 16; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.3;
            double vy = (level.random.nextDouble() - 0.5) * 0.3;
            double vz = (level.random.nextDouble() - 0.5) * 0.3;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    1.5F,
                    1.0F, 0.8F, 0.2F,
                    10
            );
            level.sendParticles(sparkle,
                    pos.x, pos.y + 1, pos.z,
                    1, vx, vy, vz, 0.1
            );
        }

        for (int i = 0; i < 40; i++) {
            double t = i / 40.0;
            double angle = t * Math.PI * 4;
            double radius = 1.0 - t * 0.5;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + t * 3;

            WispParticleData wisp = WispParticleData.wisp(
                    0.2F,
                    (float) (0.2F + t * 0.3F),
                    (float) (0.8F - t * 0.3F),
                    (float) (1.0F - t * 0.5F),
                    1
            );
            level.sendParticles(wisp, x, y, z, 1, 0, 0, 0, 0);
        }
    }


    public static void storeSoul(ItemStack totem, LivingEntity entity, Player owner) {
        if (!(entity instanceof TamableAnimal)) {
            return;
        }

        CompoundTag nbt = totem.getOrCreateTag();
        ListTag soulsList = nbt.getList(TAG_SOULS, Tag.TAG_COMPOUND);

        if (soulsList.size() >= MAX_STORED_SOULS) {
            soulsList.remove(0);
        }

        CompoundTag soulData = new CompoundTag();
        soulData.putString(TAG_ENTITY_TYPE, EntityType.getKey(entity.getType()).toString());
        soulData.putLong(TAG_DEATH_TIME, System.currentTimeMillis());
        soulData.putUUID(TAG_OWNER_UUID, owner.getUUID());

        CompoundTag entityNBT = new CompoundTag();
        entity.save(entityNBT);
        soulData.put(TAG_ENTITY_NBT, entityNBT);

        soulsList.add(soulData);
        nbt.put(TAG_SOULS, soulsList);

        owner.displayClientMessage(
                new TranslatableComponent("item.scepter_of_sovereign.soul_captured",
                        entity.getDisplayName().getString())
                        .withStyle(ChatFormatting.AQUA),
                true
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.tooltip1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.tooltip2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.nullToEmpty(""));
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.cost")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.cost.soul_fragment")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.cost.mana", MANA_COST)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.cost.plants", REQUIRED_PLANTS)
                .withStyle(ChatFormatting.GREEN));

        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(TAG_SOULS)) {
            ListTag soulsList = nbt.getList(TAG_SOULS, Tag.TAG_COMPOUND);
            int soulsCount = soulsList.size();
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.stored_souls", soulsCount, MAX_STORED_SOULS)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            if (flag.isAdvanced() && soulsCount > 0) {
                tooltip.add(new TranslatableComponent("item.scepter_of_sovereign.souls_list")
                        .withStyle(ChatFormatting.YELLOW));
                for (int i = soulsList.size() - 1; i >= Math.max(0, soulsList.size() - 3); i--) {
                    CompoundTag soulData = soulsList.getCompound(i);
                    String entityType = soulData.getString(TAG_ENTITY_TYPE);
                    String entityName = EntityType.byString(entityType)
                            .map(type -> type.getDescription().getString())
                            .orElse("Unknown");
                    tooltip.add(new TranslatableComponent("  - %s", entityName)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
