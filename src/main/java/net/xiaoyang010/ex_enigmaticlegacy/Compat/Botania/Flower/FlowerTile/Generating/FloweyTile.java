package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import java.util.*;

public class FloweyTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 8;
    private static final Logger log = LoggerFactory.getLogger(FloweyTile.class);
    private static final int MAX_SOULS = 10;
    private static final double ORBIT_RADIUS = 1.5;
    private static final double ORBIT_HEIGHT = 1.2;
    private static final double ORBIT_SPEED = 0.02;
    private static final double BREATH_SPEED = 0.05;
    private static final double BREATH_AMPLITUDE = 0.15;
    private final LinkedHashMap<String, ItemStack> collectedSouls = new LinkedHashMap<>();
    private float rotationAngle = 0;
    private boolean hasCompletedReward = false;

    public FloweyTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.FLOWEYTILE.get(), blockPos, blockState);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level == null) return;

        if (!level.isClientSide) {
            detectAndCollectSouls();

            emptyManaIntoCollector();
            if (level.getGameTime() % 5 == 0) {
                int baseMana = getBaseManaGen();
                int bonusMana = collectedSouls.size() * 5000;
                int totalMana = baseMana + bonusMana;

                if (isComplete()) {
                    totalMana *= 2;
                }

                addMana(totalMana);
            }

            resetNearbyFlowers();
        }

        if (level.isClientSide) {
            renderSoulOrbit();
            renderManaParticles();
        }
    }

    private void detectAndCollectSouls() {
        if (collectedSouls.size() >= MAX_SOULS) {
            if (!hasCompletedReward) {
                giveCompletionReward();
                hasCompletedReward = true;
            }
            return;
        }

        AABB searchBox = new AABB(getBlockPos()).inflate(2.0);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox);

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();

            if (stack.is(ModTags.Items.DETERMINATION)) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

                if (itemId != null) {
                    String itemName = itemId.toString();

                    if (!collectedSouls.containsKey(itemName)) {
                        collectedSouls.put(itemName, stack.copy());
                        itemEntity.discard();

                        playCollectionSound();

                        setChanged();
                        sync();

                        log.info("Flowey collected determination: {} ({}/{})",
                                itemName, collectedSouls.size(), MAX_SOULS);
                        break;
                    }
                }
            }
        }
    }

    private void playCollectionSound() {
        float pitch = 1.0F + (collectedSouls.size() * 0.1F);

        if (collectedSouls.size() == MAX_SOULS) {
            level.playSound(null, getBlockPos(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.BLOCKS, 1.5F, 1.0F);
        } else if (collectedSouls.size() >= 7) {
            level.playSound(null, getBlockPos(),
                    SoundEvents.BEACON_POWER_SELECT,
                   SoundSource.BLOCKS, 1.0F, pitch);
        } else {
            level.playSound(null, getBlockPos(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.BLOCKS, 1.0F, pitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderSoulOrbit() {
        if (collectedSouls.isEmpty()) return;

        rotationAngle += ORBIT_SPEED;
        if (rotationAngle > Math.PI * 2) rotationAngle -= Math.PI * 2;

        float partialTick = Minecraft.getInstance().getFrameTime();
        double time = level.getGameTime() + partialTick;
        double breathOffset = Math.sin(time * BREATH_SPEED) * BREATH_AMPLITUDE;

        Vec3 center = Vec3.atCenterOf(getBlockPos()).add(0, ORBIT_HEIGHT, 0);

        int index = 0;
        for (Map.Entry<String, ItemStack> entry : collectedSouls.entrySet()) {
            String soulId = entry.getKey();
            ItemStack stack = entry.getValue();

            double angle = rotationAngle + (index * 2 * Math.PI / collectedSouls.size());

            double x = center.x + Math.cos(angle) * ORBIT_RADIUS;
            double y = center.y + breathOffset;
            double z = center.z + Math.sin(angle) * ORBIT_RADIUS;

            renderFloatingItem(stack, x, y, z, angle);

            int color = getSoulColor(soulId);
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;

            BotaniaAPI.instance().sparkleFX(level, x, y, z, red, green, blue, 0.5F, 3);

            if (level.random.nextInt(3) == 0) {
                double innerX = x + (level.random.nextDouble() - 0.5) * 0.3;
                double innerY = y + (level.random.nextDouble() - 0.5) * 0.3;
                double innerZ = z + (level.random.nextDouble() - 0.5) * 0.3;
                BotaniaAPI.instance().sparkleFX(level, innerX, innerY, innerZ,
                        red, green, blue, 0.3F, 2);
            }

            index++;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderFloatingItem(ItemStack stack, double x, double y, double z, double rotation) {
    }

    private void renderManaParticles() {
        double particleChance = 1F - (double) getMana() / (double) getMaxMana() / 3.5F;

        if (Math.random() > particleChance) {
            Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
            double x = getBlockPos().getX() + offset.x + 0.3 + Math.random() * 0.5;
            double y = getBlockPos().getY() + offset.y + 0.5 + Math.random() * 0.5;
            double z = getBlockPos().getZ() + offset.z + 0.3 + Math.random() * 0.5;

            int color = getColor();
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;

            BotaniaAPI.instance().sparkleFX(level, x, y, z, red, green, blue,
                    (float) Math.random(), 5);
        }
    }

    private void giveCompletionReward() {
        level.playSound(null, getBlockPos(),
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.BLOCKS, 2.0F, 1.0F);

        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 3;
            double x = getBlockPos().getX() + 0.5 + Math.cos(angle) * radius;
            double y = getBlockPos().getY() + Math.random() * 3;
            double z = getBlockPos().getZ() + 0.5 + Math.sin(angle) * radius;

            float red = (float) Math.random();
            float green = (float) Math.random();
            float blue = (float) Math.random();

            BotaniaAPI.instance().sparkleFX(level, x, y, z, red, green, blue, 1.0F, 10);
        }

        log.info("Flowey has collected all 10 souls! Power level: MAXIMUM");
    }

    private int getSoulColor(String soulId) {
        if (soulId.contains("red_determination")) return 0xFF0000;
        if (soulId.contains("orange_determination")) return 0xFF8000;
        if (soulId.contains("yellow_determination")) return 0xFFFF00;
        if (soulId.contains("green_determination")) return 0x00FF00;
        if (soulId.contains("cyan_determination")) return 0x00FFFF;
        if (soulId.contains("blue_determination")) return 0x0000FF;
        if (soulId.contains("lighter_purple")) return 0xDA70D6;
        if (soulId.contains("deeper_purple")) return 0x8000FF;
        if (soulId.contains("pink_determination")) return 0xFF69B4;
        if (soulId.contains("black_determination")) return 0x2F2F2F;
        return 0xFFFFFF;
    }

    private int getBaseManaGen() {
        return 10000;
    }

    private void resetNearbyFlowers() {
        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dz = -RANGE; dz <= RANGE; dz++) {
                BlockPos pos = getEffectivePos().offset(dx, 0, dz);
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof TileEntityGeneratingFlower) {
                    TileEntityGeneratingFlower flower = (TileEntityGeneratingFlower) tile;
                    if (!flower.isRemoved()) {
                        flower.ticksExisted = 0;
                    }
                }
            }
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);

        ListTag soulsTag = new ListTag();
        for (Map.Entry<String, ItemStack> entry : collectedSouls.entrySet()) {
            CompoundTag soulTag = new CompoundTag();
            soulTag.putString("SoulId", entry.getKey());
            soulTag.put("ItemStack", entry.getValue().save(new CompoundTag()));
            soulsTag.add(soulTag);
        }
        cmp.put("CollectedSouls", soulsTag);
        cmp.putBoolean("HasCompletedReward", hasCompletedReward);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);

        collectedSouls.clear();
        ListTag soulsTag = cmp.getList("CollectedSouls", 10); // 10 = CompoundTag
        for (int i = 0; i < soulsTag.size(); i++) {
            CompoundTag soulTag = soulsTag.getCompound(i);
            String soulId = soulTag.getString("SoulId");
            ItemStack stack = ItemStack.of(soulTag.getCompound("ItemStack"));
            collectedSouls.put(soulId, stack);
        }
        hasCompletedReward = cmp.getBoolean("HasCompletedReward");
    }

    @Override
    public int getMaxMana() {
        int baseCap = 10000;
        int bonusCap = collectedSouls.size() * 10000;
        return baseCap + bonusCap;
    }

    @Override
    public int getColor() {
        return 0xFFFF00;
    }

    @Override
    @Nullable
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 5);
    }

    @Nullable
    public BlockPos findClosestTarget() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        IManaCollector closestCollector = network.getClosestCollector(
                this.getBlockPos(), this.getLevel(), this.getBindingRadius());
        return closestCollector == null ? null : closestCollector.getManaReceiverPos();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new GeneratingWandHud(this)).cast());
    }

    public LinkedHashMap<String, ItemStack> getCollectedSouls() {
        return collectedSouls;
    }

    public int getCollectedSoulsCount() {
        return collectedSouls.size();
    }

    public boolean isComplete() {
        return collectedSouls.size() >= MAX_SOULS;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public double getOrbitRadius() {
        return ORBIT_RADIUS;
    }

    public double getOrbitHeight() {
        return ORBIT_HEIGHT;
    }
}