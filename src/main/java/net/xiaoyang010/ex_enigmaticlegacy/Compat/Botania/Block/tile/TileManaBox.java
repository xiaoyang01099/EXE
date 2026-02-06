package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.ISparkEntity;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileMod;
import vazkii.botania.common.block.tile.mana.IThrottledPacket;
import vazkii.botania.common.handler.ManaNetworkHandler;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ParametersAreNonnullByDefault
public class TileManaBox extends TileMod implements IManaPool, IKeyLocked, ISparkAttachable, IThrottledPacket, IWandHUD, IWandable {
    private static final String TAG_MANA = "mana";
    private static final String TAG_COLOR = "color";
    private static final String TAG_MANA_CAP = "manaCap";
    private static final String TAG_CAN_ACCEPT = "canAccept";
    private static final String TAG_CAN_SPARE = "canSpare";
    private static final String TAG_FRAGILE = "fragile";
    private static final String TAG_INPUT_KEY = "inputKey";
    private static final String TAG_OUTPUT_KEY = "outputKey";
    private static final String TAG_REWARDED_COLORS = "rewardedColors";
    private static final String TAG_REWARDED_COLORS_COUNT = "lastRewardColorCount";
    private static final String TAG_GIVEN_FULL_REWARD = "hasGivenFullReward";
    private String inputKey = "";
    private String outputKey = "";
    public DyeColor color = DyeColor.WHITE;
    private Set<DyeColor> nearbyColors = new HashSet<>();
    private static final int CRAFT_EFFECT_EVENT = 0;
    private static final int CHARGE_EFFECT_EVENT = 1;
    public static final int MAX_MANA = 2500000;
    private int mana = 0;
    private int ticks = 0;
    private int flowerSpawnTimer = 0;
    private int lastColorCount = 0;
    private int lastRewardColorCount = 0;
    private int rewardedColorCount = 0;
    public int manaCap = MAX_MANA;
    private boolean canAccept = true;
    private boolean canSpare = true;
    private boolean sendPacket = false;
    private boolean hasGivenFullReward = false;
    public boolean fragile = false;

    public TileManaBox(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_BOX_TILE.get(), pos, state);
    }

    public TileManaBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileManaBox self) {
        if (!ManaNetworkHandler.instance.isPoolIn(level, self)) {
            BotaniaAPI.instance().getManaNetworkInstance()
                    .fireManaNetworkEvent(self, ManaBlockType.POOL, ManaNetworkAction.ADD);
        }

        if (self.sendPacket && self.ticks % 10 == 0) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
            self.sendPacket = false;
        }

        if (self.ticks % 20 == 0) {
            self.updateRainbowArray(level, pos);
        }

        self.ticks++;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileManaBox self) {
        double particleChance = 1.0 - (double) self.getCurrentMana() / self.manaCap * 0.1;
        if (Math.random() > particleChance) {
            int colorValue = self.color.getFireworkColor();

            float red = ((colorValue >> 16) & 0xFF) / 255.0F;
            float green = ((colorValue >> 8) & 0xFF) / 255.0F;
            float blue = (colorValue & 0xFF) / 255.0F;

            WispParticleData data = WispParticleData.wisp(
                    (float) Math.random() / 3.0F,
                    red, green, blue,
                    2.0F
            );

            level.addParticle(data,
                    pos.getX() + 0.3 + Math.random() * 0.5,
                    pos.getY() + 0.6 + Math.random() * 0.25,
                    pos.getZ() + Math.random(),
                    0.0, (float) Math.random() / 25.0F, 0.0);
        }
    }

    private void updateRainbowArray(Level level, BlockPos pos) {
        Set<DyeColor> foundColors = new HashSet<>();
        foundColors.add(this.color);

        int rangeX = 3;
        int rangeY = 4;
        int rangeZ = 3;

        for (int dx = -rangeX; dx <= rangeX; dx++) {
            for (int dy = -rangeY; dy <= rangeY; dy++) {
                for (int dz = -rangeZ; dz <= rangeZ; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }

                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);

                    if (be instanceof TileManaBox neighborBox) {
                        foundColors.add(neighborBox.getColor());
                    }
                }
            }
        }

        this.nearbyColors = foundColors;
        int colorCount = foundColors.size();

        int newManaCap = MAX_MANA * colorCount;
        if (this.manaCap != newManaCap) {
            this.manaCap = newManaCap;
            setChanged();
        }

        if (colorCount != lastColorCount) {
            lastColorCount = colorCount;
            if (colorCount >= 2) {
                triggerRainbowEffect();
            }
        }

        if (colorCount >= 2) {
            applyPlayerBuffs(level, pos, colorCount);
        }

        if (colorCount >= 3) {
            flowerSpawnTimer++;
            if (flowerSpawnTimer >= 200) {
                spawnMysticalFlowers(level, pos);
                flowerSpawnTimer = 0;
            }
        } else {
            flowerSpawnTimer = 0;
        }

        if (colorCount >= 1) {
            checkAndGiveRewards(level, pos, colorCount);
        }
    }

    private void checkAndGiveRewards(Level level, BlockPos pos, int colorCount) {
        if (getCurrentMana() < manaCap) {
            return;
        }

        if (colorCount > rewardedColorCount) {
            int newColors = colorCount - rewardedColorCount;

            giveBasicReward(level, pos, newColors);

            rewardedColorCount = colorCount;
            setChanged();

            if (colorCount >= 14 && !hasGivenFullReward) {
                hasGivenFullReward = true;
                giveFullReward(level, pos);
                setChanged();
            }
        }
    }

    private void giveBasicReward(Level level, BlockPos pos, int newColorCount) {
        if (!(level instanceof ServerLevel)) return;

        AABB range = new AABB(pos).inflate(16.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);

        if (players.isEmpty()) return;

        Player player = players.get(0);
        int oreCount = newColorCount * 30;

        giveVanillaOres(player, oreCount);

        giveBotaniaOres(player, oreCount);

        player.displayClientMessage(
                new TextComponent(
                        "§6[彩虹魔力箱] §e新增 " + newColorCount + " 种颜色！获得 " + oreCount + " 个矿石奖励！"
                ),
                false
        );
    }

    private void giveFullReward(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) return;

        AABB range = new AABB(pos).inflate(16.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);

        if (players.isEmpty()) return;

        Player player = players.get(0);

        giveVanillaOres(player, 64);
        giveBotaniaOres(player, 64);

        giveBotaniaEquipment(player, 2);

        player.displayClientMessage(
                new TextComponent(
                        "§d§l[彩虹魔力箱] §6§l完美共鸣！§e获得额外64个矿石和2件魔法装备！"
                ),
                false
        );

        spawnFullRewardEffect(level, pos);
    }

    private void giveVanillaOres(Player player, int count) {
        Item[] vanillaOres = {
                Items.COAL,
                Items.IRON_ORE,
                Items.GOLD_ORE,
                Items.DIAMOND,
                Items.EMERALD,
                Items.LAPIS_LAZULI,
                Items.REDSTONE,
                Items.NETHERITE_INGOT
        };

        for (Item ore : vanillaOres) {
            giveItemToPlayer(player, new ItemStack(ore, count));
        }
    }

    private void giveBotaniaOres(Player player, int count) {
        Item[] botaniaOres = {
                ModItems.manaDiamond.asItem(),
                ModItems.manaPearl.asItem(),
                ModItems.manaSteel.asItem(),
                ModItems.manaweaveCloth.asItem(),
                ModItems.manaPowder.asItem(),
                ModItems.manaString.asItem(),

                ModItems.terrasteel.asItem(),

                ModItems.elementium.asItem(),
                ModItems.pixieDust.asItem(),
                ModItems.dragonstone.asItem()
        };

        for (Item ore : botaniaOres) {
            giveItemToPlayer(player, new ItemStack(ore, Math.min(count, 64)));
        }
    }

    private void giveBotaniaEquipment(Player player, int count) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Random random = serverLevel.random;

        Item[] equipment = {
                ModItems.terraSword,
                ModItems.starSword,
                ModItems.thunderSword,

                ModItems.terraPick,
                ModItems.manasteelPick,
                ModItems.elementiumPick,

                ModItems.tinyPlanet,
                ModItems.manaRing,
                ModItems.auraRing,
                ModItems.magnetRing,
                ModItems.waterRing,
                ModItems.miningRing,
                ModItems.swapRing,

                ModItems.manasteelHelm,
                ModItems.manasteelChest,
                ModItems.manasteelLegs,
                ModItems.manasteelBoots,
                ModItems.elementiumHelm,
                ModItems.elementiumChest,
                ModItems.elementiumLegs,
                ModItems.elementiumBoots,
                ModItems.terrasteelHelm,
                ModItems.terrasteelChest,
                ModItems.terrasteelLegs,
                ModItems.terrasteelBoots
        };

        for (int i = 0; i < count; i++) {
            Item randomItem = equipment[random.nextInt(equipment.length)];
            giveItemToPlayer(player, new ItemStack(randomItem, 1));
        }
    }

    private void giveItemToPlayer(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void applyPlayerBuffs(Level level, BlockPos pos, int colorCount) {
        int effectLevel = Math.min(colorCount - 1, 4);
        int duration = 6000;

        AABB range = new AABB(pos).inflate(16.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(ModEffects.CREEPER_FRIENDLY.get(), duration, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, duration, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, effectLevel, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, effectLevel, false, false));
        }
    }

    private void spawnFullRewardEffect(Level level, BlockPos pos) {
        if (!level.isClientSide) return;

        for (int i = 0; i < 100; i++) {
            DyeColor randomColor = DyeColor.values()[level.random.nextInt(DyeColor.values().length)];
            int colorValue = randomColor.getFireworkColor();
            float red = ((colorValue >> 16) & 0xFF) / 255.0F;
            float green = ((colorValue >> 8) & 0xFF) / 255.0F;
            float blue = (colorValue & 0xFF) / 255.0F;

            SparkleParticleData data = SparkleParticleData.sparkle(
                    1.0F + (float) Math.random() * 2.0F,
                    red, green, blue,
                    60
            );

            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 3.0;

            level.addParticle(data,
                    pos.getX() + 0.5 + Math.cos(angle) * radius,
                    pos.getY() + 0.5 + Math.random() * 2.0,
                    pos.getZ() + 0.5 + Math.sin(angle) * radius,
                    (Math.random() - 0.5) * 0.3,
                    Math.random() * 0.5,
                    (Math.random() - 0.5) * 0.3);
        }
    }

    private void spawnMysticalFlowers(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Random random = serverLevel.random;
        int spawned = 0;

        for (int attempt = 0; attempt < 16 && spawned < 8; attempt++) {
            int offsetX = random.nextInt(11) - 5;
            int offsetZ = random.nextInt(11) - 5;
            BlockPos flowerPos = pos.offset(offsetX, 0, offsetZ);

            while (flowerPos.getY() > level.getMinBuildHeight() && level.isEmptyBlock(flowerPos)) {
                flowerPos = flowerPos.below();
            }
            flowerPos = flowerPos.above();

            if (level.isEmptyBlock(flowerPos) && level.getBlockState(flowerPos.below()).isSolidRender(level, flowerPos.below())) {
                DyeColor randomColor = DyeColor.values()[random.nextInt(DyeColor.values().length)];
                BlockState flowerState = getFlowerForColor(randomColor);

                if (flowerState != null) {
                    level.setBlock(flowerPos, flowerState, 3);

                    spawnFlowerParticles(level, flowerPos, randomColor);
                    spawned++;
                }
            }
        }
    }

    private void spawnFlowerParticles(Level level, BlockPos pos, DyeColor color) {
        int colorValue = color.getFireworkColor();
        float red = ((colorValue >> 16) & 0xFF) / 255.0F;
        float green = ((colorValue >> 8) & 0xFF) / 255.0F;
        float blue = (colorValue & 0xFF) / 255.0F;

        for (int i = 0; i < 10; i++) {
            SparkleParticleData data = SparkleParticleData.sparkle(
                    (float) Math.random() * 0.5F + 0.5F,
                    red, green, blue,
                    20
            );
            level.addParticle(data,
                    pos.getX() + 0.5 + (Math.random() - 0.5) * 0.3,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.3,
                    0.0, 0.1, 0.0);
        }
    }

    private void triggerRainbowEffect() {
        if (level != null && level.isClientSide) {
            for (DyeColor color : nearbyColors) {
                int colorValue = color.getFireworkColor();
                float red = ((colorValue >> 16) & 0xFF) / 255.0F;
                float green = ((colorValue >> 8) & 0xFF) / 255.0F;
                float blue = (colorValue & 0xFF) / 255.0F;

                for (int i = 0; i < 5; i++) {
                    SparkleParticleData data = SparkleParticleData.sparkle(
                            (float) Math.random(),
                            red, green, blue,
                            30
                    );
                    level.addParticle(data,
                            worldPosition.getX() + Math.random(),
                            worldPosition.getY() + Math.random(),
                            worldPosition.getZ() + Math.random(),
                            0.0, 0.05, 0.0);
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        BotaniaAPI.instance().getManaNetworkInstance()
                .fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.REMOVE);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        BotaniaAPI.instance().getManaNetworkInstance()
                .fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.ADD);
    }

    public boolean isFull() {
        Block blockBelow = level.getBlockState(worldPosition.below()).getBlock();
        return blockBelow != ModBlocks.manaVoid && getCurrentMana() >= manaCap;
    }

    @Override
    public void receiveMana(int mana) {
        int old = this.mana;
        this.mana = Math.max(0, Math.min(getCurrentMana() + mana, manaCap));
        if (old != this.mana) {
            setChanged();
            markDispatchable();
        }
    }

    public static int calculateComparatorLevel(int mana, int max) {
        int val = (int) ((double) mana / max * 15.0);
        if (mana > 0) {
            val = Math.max(val, 1);
        }
        return val;
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == CRAFT_EFFECT_EVENT) {
            if (level.isClientSide) {
                int colorValue = this.color.getFireworkColor();
                float red = ((colorValue >> 16) & 0xFF) / 255.0F;
                float green = ((colorValue >> 8) & 0xFF) / 255.0F;
                float blue = (colorValue & 0xFF) / 255.0F;

                for (int i = 0; i < 25; i++) {
                    float r = red * (0.8F + (float)Math.random() * 0.4F);
                    float g = green * (0.8F + (float)Math.random() * 0.4F);
                    float b = blue * (0.8F + (float)Math.random() * 0.4F);

                    SparkleParticleData data = SparkleParticleData.sparkle(
                            (float) Math.random(), r, g, b, 10
                    );
                    level.addParticle(data,
                            worldPosition.getX() + 0.5 + Math.random() * 0.4 - 0.2,
                            worldPosition.getY() + 0.75,
                            worldPosition.getZ() + 0.5 + Math.random() * 0.4 - 0.2,
                            0.0, 0.0, 0.0);
                }
            }
            return true;
        }
        else if (event == CHARGE_EFFECT_EVENT) {
            if (level.isClientSide) {
                boolean outputting = param == 1;
                Vector3 itemVec = Vector3.fromBlockEntityCenter(this).add(0, Math.random() * 0.3 - 0.15, 0);
                Vector3 tileVec = Vector3.fromBlockEntity(this).add(0.2 + Math.random() * 0.6, 0.0, 0.2 + Math.random() * 0.6);

                Vector3 start = outputting ? tileVec : itemVec;
                Vector3 end = outputting ? itemVec : tileVec;

                BotaniaAPI.instance().sparkleFX(level,
                        start.x, start.y, start.z,
                        1.0F, 0.4F, 1.0F, 1.0F, 5);
            }
            return true;
        }
        return super.triggerEvent(event, param);
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_COLOR, color.getId());
        cmp.putInt(TAG_MANA_CAP, manaCap);
        cmp.putBoolean(TAG_CAN_ACCEPT, canAccept);
        cmp.putBoolean(TAG_CAN_SPARE, canSpare);
        cmp.putBoolean(TAG_FRAGILE, fragile);
        cmp.putString(TAG_INPUT_KEY, inputKey);
        cmp.putString(TAG_OUTPUT_KEY, outputKey);
        cmp.putInt(TAG_REWARDED_COLORS_COUNT, lastRewardColorCount);
        cmp.putInt(TAG_REWARDED_COLORS, rewardedColorCount);
        cmp.putBoolean(TAG_GIVEN_FULL_REWARD, hasGivenFullReward);
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        mana = cmp.getInt(TAG_MANA);
        color = DyeColor.byId(cmp.getInt(TAG_COLOR));

        if (cmp.contains(TAG_MANA_CAP)) {
            manaCap = cmp.getInt(TAG_MANA_CAP);
        }
        if (cmp.contains(TAG_CAN_ACCEPT)) {
            canAccept = cmp.getBoolean(TAG_CAN_ACCEPT);
        }
        if (cmp.contains(TAG_CAN_SPARE)) {
            canSpare = cmp.getBoolean(TAG_CAN_SPARE);
        }
        fragile = cmp.getBoolean(TAG_FRAGILE);
        if (cmp.contains(TAG_INPUT_KEY)) {
            inputKey = cmp.getString(TAG_INPUT_KEY);
        }
        if (cmp.contains(TAG_OUTPUT_KEY)) {
            outputKey = cmp.getString(TAG_OUTPUT_KEY);
        }
        if (cmp.contains(TAG_REWARDED_COLORS_COUNT)) {
            lastRewardColorCount = cmp.getInt(TAG_REWARDED_COLORS_COUNT);
        }
        if (cmp.contains(TAG_REWARDED_COLORS)) {
            rewardedColorCount = cmp.getInt(TAG_REWARDED_COLORS);
        }
        if (cmp.contains(TAG_GIVEN_FULL_REWARD)) {
            hasGivenFullReward = cmp.getBoolean(TAG_GIVEN_FULL_REWARD);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHUD(PoseStack ms, Minecraft mc) {
        ItemStack pool = new ItemStack(getBlockState().getBlock());
        String name = pool.getHoverName().getString();

        if (nearbyColors.size() >= 2) {
            name += " §6[Rainbow×" + nearbyColors.size() + "]";
            if (rewardedColorCount > 0) {
                name += " §a[Awarded×" + rewardedColorCount + "]";
            }
        }

        int hudColor = this.color.getFireworkColor();
        BotaniaAPIClient.instance().drawSimpleManaHUD(ms, hudColor, getCurrentMana(), manaCap, name);
    }

    @Override
    public boolean onUsedByWand(@org.jetbrains.annotations.Nullable Player player, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return canAccept;
    }

    @Override
    public boolean isOutputtingPower() {
        return canSpare;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public String getInputKey() {
        return inputKey;
    }

    @Override
    public String getOutputKey() {
        return outputKey;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(IManaSpark entity) {
    }

    @Nullable
    private BlockState getFlowerForColor(DyeColor color) {
        return switch (color) {
            case WHITE -> ModBlocks.whiteFlower.defaultBlockState();
            case ORANGE -> ModBlocks.orangeFlower.defaultBlockState();
            case MAGENTA -> ModBlocks.magentaFlower.defaultBlockState();
            case LIGHT_BLUE -> ModBlocks.lightBlueFlower.defaultBlockState();
            case YELLOW -> ModBlocks.yellowFlower.defaultBlockState();
            case LIME -> ModBlocks.limeFlower.defaultBlockState();
            case PINK -> ModBlocks.pinkFlower.defaultBlockState();
            case GRAY -> ModBlocks.grayFlower.defaultBlockState();
            case LIGHT_GRAY -> ModBlocks.lightGrayFlower.defaultBlockState();
            case CYAN -> ModBlocks.cyanFlower.defaultBlockState();
            case PURPLE -> ModBlocks.purpleFlower.defaultBlockState();
            case BLUE -> ModBlocks.blueFlower.defaultBlockState();
            case BROWN -> ModBlocks.brownFlower.defaultBlockState();
            case GREEN -> ModBlocks.greenFlower.defaultBlockState();
            case RED -> ModBlocks.redFlower.defaultBlockState();
            case BLACK -> ModBlocks.blackFlower.defaultBlockState();
        };
    }

    @Nullable
    @Override
    public IManaSpark getAttachedSpark() {
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class,
                new AABB(worldPosition, worldPosition.offset(1, 1, 1)),
                e -> e instanceof ISparkEntity);

        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (IManaSpark) e;
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return LazyOptional.of(() -> (IManaReceiver) this).cast();
        }

        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> (IWandHUD) this).cast();
        }

        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            return LazyOptional.of(() -> (ISparkAttachable) this).cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public int getAvailableSpaceForMana() {
        int space = Math.max(0, manaCap - getCurrentMana());
        if (space > 0) {
            return space;
        }
        return level.getBlockState(worldPosition.below()).getBlock() == ModBlocks.manaVoid ? manaCap : 0;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public void setColor(DyeColor color) {
        this.color = color;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void markDispatchable() {
        sendPacket = true;
    }

    public void setMana(int mana) {
        this.mana = mana;
        setChanged();
    }
}