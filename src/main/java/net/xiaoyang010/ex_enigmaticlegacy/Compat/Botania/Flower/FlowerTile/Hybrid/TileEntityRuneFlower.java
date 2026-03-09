package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Hybrid;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileEntityHybridFlower;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.ModTags;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

public class TileEntityRuneFlower extends TileEntityHybridFlower {

    private static final int MAX_MANA = 10000;
    private static final int FLOWER_COLOR = 0x8A2BE2; // 紫色基调
    private static final int WORK_INTERVAL = 20; // 每秒工作一次
    private static final int SCAN_RANGE = 5; // 扫描范围
    private static final String TAG_LOSSLESS_TARGETS = "losslessTargets";
    private static final int LOSSLESS_DURATION = 600; // 30秒 (30 * 20 ticks)
    private static final int LOSSLESS_SCAN_RANGE = 16; // 扫描范围16格
    private static final String TAG_SIN_TARGETS = "sinTargets";
    private static final int SIN_DURATION = 600; // 30秒
    private static final int SIN_SCAN_RANGE = 12; // 作用范围12格

    // 存储受影响的魔力设备及其剩余时间
    private final Map<BlockPos, Integer> losslessTransferTargets = new HashMap<>();

    // 存储受七宗罪符文影响的玩家及其剩余时间
    private final Map<UUID, Integer> sinEffectPlayers = new HashMap<>();

    // 符文魔力转化率
    private static final Map<Item, Integer> RUNE_MANA_VALUES = new HashMap<>();
    private static final Map<Item, RuneDrops> RUNE_DROP_TABLE = new HashMap<>();

    private int workCounter = 0;
    private List<ItemStack> cachedRunes = new ArrayList<>();

    static {
        // 初始化符文魔力值（元素符文）
        RUNE_MANA_VALUES.put(ModItems.runeWater, 650);
        RUNE_MANA_VALUES.put(ModItems.runeFire, 650);
        RUNE_MANA_VALUES.put(ModItems.runeEarth, 650);
        RUNE_MANA_VALUES.put(ModItems.runeAir, 650);

        // 季节符文
        RUNE_MANA_VALUES.put(ModItems.runeSpring, 1350);
        RUNE_MANA_VALUES.put(ModItems.runeSummer, 1350);
        RUNE_MANA_VALUES.put(ModItems.runeAutumn, 1350);
        RUNE_MANA_VALUES.put(ModItems.runeWinter, 1350);

        // 魔力符文
        RUNE_MANA_VALUES.put(ModItems.runeMana, 3250);

        // 七宗罪符文
        RUNE_MANA_VALUES.put(ModItems.runeLust, 5500);
        RUNE_MANA_VALUES.put(ModItems.runeGluttony, 5500);
        RUNE_MANA_VALUES.put(ModItems.runeGreed, 5500);
        RUNE_MANA_VALUES.put(ModItems.runeSloth, 5500);
        RUNE_MANA_VALUES.put(ModItems.runeWrath, 5500);
        RUNE_MANA_VALUES.put(ModItems.runeEnvy, 5500);
        RUNE_MANA_VALUES.put(ModItems.runePride, 5500);

        // 初始化符文掉落表
        initializeDropTables();
    }

    private static void initializeDropTables() {
        // 元素符文掉落
        RUNE_DROP_TABLE.put(ModItems.runeWater, new RuneDrops(
                new DropEntry(ModItems.manaSteel, 0.6f, 1),
                new DropEntry(ModItems.manaPearl, 0.4f, 2)
        ));
        RUNE_DROP_TABLE.put(ModItems.runeFire, new RuneDrops(
                new DropEntry(ModItems.manaSteel, 0.6f, 1),
                new DropEntry(ModItems.manaPearl, 0.4f, 2)
        ));
        RUNE_DROP_TABLE.put(ModItems.runeEarth, new RuneDrops(
                new DropEntry(ModItems.manaSteel, 0.6f, 1),
                new DropEntry(ModItems.manaPearl, 0.4f, 2)
        ));
        RUNE_DROP_TABLE.put(ModItems.runeAir, new RuneDrops(
                new DropEntry(ModItems.manaSteel, 0.6f, 1),
                new DropEntry(ModItems.manaPearl, 0.4f, 2)
        ));

        // 季节符文掉落
        RuneDrops seasonDrops = new RuneDrops(
                new DropEntry(ModItems.manaPearl, 0.4f, 2),
                new DropEntry(ModItems.runeWater, 0.1f, 1) // 可能掉落元素符文
        );
        RUNE_DROP_TABLE.put(ModItems.runeSpring, seasonDrops);
        RUNE_DROP_TABLE.put(ModItems.runeSummer, seasonDrops);
        RUNE_DROP_TABLE.put(ModItems.runeAutumn, seasonDrops);
        RUNE_DROP_TABLE.put(ModItems.runeWinter, seasonDrops);

        // 魔力符文掉落
        RUNE_DROP_TABLE.put(ModItems.runeMana, new RuneDrops(
                new DropEntry(ModItems.manaDiamond, 1.0f, 1),
                new DropEntry(ModItems.manaSteel, 0.2f, 1)
        ));

        // 七宗罪符文掉落
        RuneDrops sinDrops = new RuneDrops(
                new DropEntry(ModItems.manaweaveCloth, 0.3f, 1),
                new DropEntry(ModItems.terrasteel, 0.05f, 1)
        );
        RUNE_DROP_TABLE.put(ModItems.runeLust, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runeGluttony, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runeGreed, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runeSloth, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runeWrath, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runeEnvy, sinDrops);
        RUNE_DROP_TABLE.put(ModItems.runePride, sinDrops);
    }

    public TileEntityRuneFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return FLOWER_COLOR;
    }

    @Override
    public boolean acceptsRedstone() {
        return true; // 接受红石信号控制
    }

    @Override
    public boolean canWork() {
        // 功能模式：有魔力且红石信号为低电平时可工作
        return getWorkMode() == WorkMode.FUNCTIONAL
                && getMana() > 0
                && redstoneSignal == 0
                && workCounter >= WORK_INTERVAL;
    }

    @Override
    public void doFunctionalWork() {
        if (level == null || level.isClientSide) return;

        workCounter = 0;
        cachedRunes = scanNearbyRunes();

        if (cachedRunes.isEmpty()) return;

        // 根据符文组合执行对应效果
        executeRuneCombination(cachedRunes);
    }

    @Override
    public boolean canGenerate() {
        // 产能模式：有可分解的符文时可工作
        return getWorkMode() == WorkMode.GENERATING
                && workCounter >= WORK_INTERVAL;
    }

    @Override
    public int doGeneratingWork() {
        if (level == null || level.isClientSide) return 0;

        workCounter = 0;

        // 扫描并分解符文
        List<ItemEntity> runeItems = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(worldPosition).inflate(SCAN_RANGE),
                entity -> isValidRune(entity.getItem()));

        if (runeItems.isEmpty()) return 0;

        int totalMana = 0;
        ItemEntity targetRune = runeItems.get(0);
        ItemStack runeStack = targetRune.getItem();

        // 获取符文魔力值
        Integer manaValue = RUNE_MANA_VALUES.get(runeStack.getItem());
        if (manaValue != null && getMana() + manaValue <= getMaxMana()) {
            totalMana = manaValue;

            // 处理掉落物
            processRuneDrops(runeStack.getItem());

            // 消耗符文
            runeStack.shrink(1);
            if (runeStack.isEmpty()) {
                targetRune.discard();
            }

            // 播放音效和粒子效果
            level.playSound(null, worldPosition,
                    ModSounds.runeAltarCraft,
                    SoundSource.BLOCKS, 0.5F, 1.2F);
        }

        return totalMana;
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        workCounter++;
        updateLosslessTransfer();
        updateSinEffect();
    }

    /**
     * 更新无损耗传输效果
     */
    private void updateLosslessTransfer() {
        if (level == null || level.isClientSide) return;

        // 递减所有目标的计时器
        Iterator<Map.Entry<BlockPos, Integer>> iterator = losslessTransferTargets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            int newTime = entry.getValue() - 1;

            if (newTime <= 0) {
                // 效果结束，移除目标
                iterator.remove();

                // 播放效果结束的粒子
                BlockPos pos = entry.getKey();
                BotaniaAPI.instance().sparkleFX(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        0.5F, 0.5F, 0.5F, 1.0F, 10);
            } else {
                entry.setValue(newTime);

                // 每20tick播放一次粒子效果
                if (newTime % 20 == 0) {
                    BlockPos pos = entry.getKey();
                    spawnLosslessParticles(pos);
                }
            }
        }
    }

    /**
     * 为无损耗传输目标生成粒子效果
     */
    private void spawnLosslessParticles(BlockPos pos) {
        if (level == null) return;

        // 金色粒子表示无损耗状态
        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;

            BotaniaAPI.instance().sparkleFX(level,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + 0.5 + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1.0F, 0.84F, 0.0F, // 金色
                    0.8F, 8);
        }
    }

    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 10);
    }

    /**
     * 扫描周围的符文（3x3范围的地面物品）
     */
    private List<ItemStack> scanNearbyRunes() {
        if (level == null) return Collections.emptyList();

        List<ItemStack> runes = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = worldPosition.offset(x, 0, z);
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                        new AABB(checkPos),
                        entity -> isValidRune(entity.getItem()));

                for (ItemEntity item : items) {
                    runes.add(item.getItem().copy());
                    if (runes.size() >= 3) break;
                }
                if (runes.size() >= 3) break;
            }
            if (runes.size() >= 3) break;
        }

        return runes;
    }

    /**
     * 执行符文组合效果
     */
    private void executeRuneCombination(List<ItemStack> runes) {
        if (level == null) return;

        Set<Item> runeTypes = new HashSet<>();
        Map<Item, Integer> runeCount = new HashMap<>();

        for (ItemStack stack : runes) {
            Item item = stack.getItem();
            runeTypes.add(item);
            runeCount.put(item, runeCount.getOrDefault(item, 0) + 1);
        }

        // 检查双魔力符文（需要2个魔力符文）
        if (runeCount.getOrDefault(ModItems.runeMana, 0) >= 2) {
            effectDoubleMana();
            return; // 优先处理双魔力符文效果
        }

        // 元素符文组合
        if (runeTypes.contains(ModItems.runeEarth) && runeTypes.contains(ModItems.runeWater)) {
            effectEarthWater();
        } else if (runeTypes.contains(ModItems.runeFire) && runeTypes.contains(ModItems.runeAir)) {
            effectFireAir();
        } else if (runeTypes.contains(ModItems.runeEarth) && runeTypes.contains(ModItems.runeFire)) {
            effectEarthFire();
        } else if (runeTypes.contains(ModItems.runeWater) && runeTypes.contains(ModItems.runeAir)) {
            effectWaterAir();
        }
        // 季节符文组合
        else if (runeTypes.contains(ModItems.runeSpring) && runeTypes.contains(ModItems.runeSummer)) {
            effectSpringSummer();
        } else if (runeTypes.contains(ModItems.runeAutumn) && runeTypes.contains(ModItems.runeWinter)) {
            effectAutumnWinter();
        } else if (runeTypes.contains(ModItems.runeSpring) && runeTypes.contains(ModItems.runeAutumn)) {
            effectSpringAutumn();
        }
        // 高阶符文组合
        else if (Collections.frequency(new ArrayList<>(runeTypes), ModItems.runeMana) >= 2) {
            effectDoubleMana();
        } else if (runeTypes.contains(ModItems.runeMana) && isElementalRune(runeTypes)) {
            effectManaElemental();
        } else if (isSinRuneCombination(runeTypes)) {
            effectSinCombination();
        }
    }

    // ========== 元素符文效果 ==========

    /**
     * 地+水：沃土转化 + 自动湿润
     */
    private void effectEarthWater() {
        int manaCost = 1000;
        if (getMana() < manaCost) return;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-2, -1, -2),
                worldPosition.offset(2, 1, 2))) {

            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                // 转化为耕地
                level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
            } else if (state.is(Blocks.FARMLAND)) {
                // 保持湿润状态
                level.setBlock(pos, state.setValue(
                        FarmBlock.MOISTURE, 7), 3);
            }
        }

        addMana(-manaCost);
    }

    /**
     * 火+风：魔力热风 + 火焰抗性
     */
    private void effectFireAir() {
        int manaCost = 900;
        if (getMana() < manaCost) return;

        // 给予附近玩家火焰抗性
        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(8));

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
        }

        // 点燃周围可燃方块
        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-4, -2, -4),
                worldPosition.offset(4, 2, 4))) {

            BlockState state = level.getBlockState(pos);
            if (state.isAir() && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }

        addMana(-manaCost);
    }

    /**
     * 地+火：自动熔炼
     */
    private void effectEarthFire() {
        int manaCostPerItem = 1000;

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(worldPosition).inflate(3),
                entity -> canSmelt(entity.getItem()));

        for (ItemEntity item : items) {
            if (getMana() < manaCostPerItem) break;

            ItemStack result = getSmeltResult(item.getItem());
            if (!result.isEmpty()) {
                ItemEntity newItem = new ItemEntity(level,
                        item.getX(), item.getY(), item.getZ(), result);
                level.addFreshEntity(newItem);

                item.getItem().shrink(1);
                if (item.getItem().isEmpty()) {
                    item.discard();
                }

                addMana(-manaCostPerItem);
            }
        }
    }

    /**
     * 水+风：驱散负面效果 + 速度buff
     */
    private void effectWaterAir() {
        int manaCost = 2000;
        if (getMana() < manaCost) return;

        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(5));

        for (Player player : players) {
            // 移除负面效果
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

            // 添加速度效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0));
        }

        addMana(-manaCost);
    }

    // ========== 季节符文效果 ==========

    /**
     * 春+夏：加速植物生长
     */
    private void effectSpringSummer() {
        int manaCost = 1860;
        if (getMana() < manaCost) return;

        if (level == null || level.isClientSide) return;

        int acceleratedCount = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-2, -1, -2),
                worldPosition.offset(2, 1, 2))) {

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            // 1. 加速作物生长（原版作物）
            if (block instanceof CropBlock crop) {
                if (!crop.isMaxAge(state)) {
                    int currentAge = crop.getMaxAge();
                    int maxAge = crop.getMaxAge();
                    // 增加2-3个生长阶段
                    int newAge = Math.min(maxAge, currentAge + 2 + level.random.nextInt(2));
                    level.setBlock(pos, crop.getStateForAge(newAge), 3);
                    acceleratedCount++;
                    spawnGrowthParticles(pos, 0.2F, 0.8F, 0.2F); // 绿色
                }
            }
            // 2. 加速树苗生长
            else if (block instanceof SaplingBlock saplingBlock) {
                if (level instanceof ServerLevel serverLevel) {
                    // 30%概率直接长成树
                    if (level.random.nextFloat() < 0.3f) {
                        saplingBlock.advanceTree(serverLevel, pos, state, level.random);
                        acceleratedCount++;
                        spawnGrowthParticles(pos, 0.4F, 0.6F, 0.2F); // 黄绿色
                    }
                }
            }
            // 3. 加速神秘花生长（单花 -> 双花）
            else if (state.is(ModTags.Blocks.MYSTICAL_FLOWERS) &&
                    !state.is(ModTags.Blocks.DOUBLE_MYSTICAL_FLOWERS)) {
                // 20%概率转换为双花
                if (level.random.nextFloat() < 0.2f && level.getBlockState(pos.above()).isAir()) {
                    BlockState doubleFlower = getDoubleFlowerFromSingle(block);
                    if (doubleFlower != null) {
                        // 放置双花的下半部分
                        level.setBlock(pos, doubleFlower.setValue(
                                DoublePlantBlock.HALF,
                                DoubleBlockHalf.LOWER), 3);
                        // 放置双花的上半部分
                        level.setBlock(pos.above(), doubleFlower.setValue(
                                DoublePlantBlock.HALF,
                                DoubleBlockHalf.UPPER), 3);
                        acceleratedCount++;
                        spawnGrowthParticles(pos, 0.8F, 0.2F, 0.8F); // 紫色
                    }
                }
            }
            // 4. 加速特殊花工作
            else if (state.is(ModTags.Blocks.SPECIAL_FLOWERS)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TileEntitySpecialFlower specialFlower) {
                    // 使用反射调用 protected 方法
                    accelerateSpecialFlower(specialFlower, 5);
                    acceleratedCount++;
                    spawnGrowthParticles(pos, 0.6F, 0.4F, 0.8F);
                }
            }
            // 5. 加速所有可用骨粉的方块
            else if (block instanceof BonemealableBlock bonemealable) {
                if (bonemealable.isValidBonemealTarget(level, pos, state, false) &&
                        level instanceof ServerLevel serverLevel &&
                        level.random.nextFloat() < 0.5f) { // 50%概率
                    bonemealable.performBonemeal(serverLevel, level.random, pos, state);
                    acceleratedCount++;
                    spawnGrowthParticles(pos, 0.4F, 0.9F, 0.4F); // 亮绿色
                }
            }
            // 6. 在草地/泥土上催生植物
            else if (state.is(BlockTags.DIRT)) {
                BlockState above = level.getBlockState(pos.above());
                if (above.isAir() && level.random.nextFloat() < 0.15f) { // 15%概率
                    BlockState plantState = getRandomPlant();
                    if (plantState != null && plantState.canSurvive(level, pos.above())) {
                        level.setBlock(pos.above(), plantState, 3);
                        acceleratedCount++;
                        spawnGrowthParticles(pos.above(), 0.4F, 0.9F, 0.4F); // 亮绿色
                    }
                }
            }
        }

        if (acceleratedCount > 0) {
            addMana(-manaCost);

            // 播放生长音效
            level.playSound(null, worldPosition,
                    SoundEvents.BONE_MEAL_USE,
                    SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    /**
     * 从单花获取对应的双花
     */
    private BlockState getDoubleFlowerFromSingle(Block singleFlower) {
        ResourceLocation flowerKey =
                Registry.BLOCK.getKey(singleFlower);

        // 确保是Botania的花
        if (!BotaniaAPI.MODID.equals(flowerKey.getNamespace())) {
            return null;
        }

        String path = flowerKey.getPath();
        if (path.startsWith("flower_")) {
            String color = path.substring(7);
            String doubleFlowerPath = "double_flower_" + color;

            Block doubleFlower = Registry.BLOCK.getOptional(
                    new ResourceLocation(
                            BotaniaAPI.MODID,
                            doubleFlowerPath
                    )
            ).orElse(null);

            return doubleFlower != null ? doubleFlower.defaultBlockState() : null;
        }

        return null;
    }

    /**
     * 获取随机植物（优先神秘花）
     */
    private BlockState getRandomPlant() {
        float rand = level.random.nextFloat();

        if (rand < 0.4f) {
            // 40%概率生成神秘花
            return getRandomMysticalFlower();
        } else if (rand < 0.7f) {
            // 30%概率生成原版花
            return getRandomVanillaFlower();
        } else {
            // 30%概率生成草
            return Blocks.GRASS.defaultBlockState();
        }
    }

    /**
     * 从tag中获取随机神秘花
     */
    private BlockState getRandomMysticalFlower() {
        List<Block> mysticalFlowers = new ArrayList<>();

        // 遍历神秘花tag，排除双花
        for (Holder<Block> holder :
                Registry.BLOCK.getTagOrEmpty(ModTags.Blocks.MYSTICAL_FLOWERS)) {
            Block block = holder.value();
            // 只要单花
            if (!block.defaultBlockState().is(ModTags.Blocks.DOUBLE_MYSTICAL_FLOWERS)) {
                mysticalFlowers.add(block);
            }
        }

        if (mysticalFlowers.isEmpty()) {
            return null;
        }

        Block randomFlower = mysticalFlowers.get(level.random.nextInt(mysticalFlowers.size()));
        return randomFlower.defaultBlockState();
    }

    /**
     * 从tag中获取随机原版花
     */
    private BlockState getRandomVanillaFlower() {
        List<Block> flowers = new ArrayList<>();

        // 使用原版的小型花tag
        for (Holder<Block> holder :
               Registry.BLOCK.getTagOrEmpty(BlockTags.SMALL_FLOWERS)) {
            flowers.add(holder.value());
        }

        if (flowers.isEmpty()) {
            // 备选方案：硬编码几个常见花
            Block[] defaultFlowers = {
                    Blocks.DANDELION,
                    Blocks.POPPY,
                    Blocks.BLUE_ORCHID,
                    Blocks.ALLIUM,
                    Blocks.AZURE_BLUET,
                    Blocks.OXEYE_DAISY
            };
            return defaultFlowers[level.random.nextInt(defaultFlowers.length)].defaultBlockState();
        }

        Block randomFlower = flowers.get(level.random.nextInt(flowers.size()));
        return randomFlower.defaultBlockState();
    }

    /**
     * 生成生长粒子效果
     */
    private void spawnGrowthParticles(BlockPos pos, float r, float g, float b) {
        if (level == null) return;

        // 生成5个上升的粒子
        for (int i = 0; i < 5; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;

            BotaniaAPI.instance().sparkleFX(level,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    r, g, b, 0.8F, 15);
        }
    }

    /**
     * 使用反射加速特殊花
     */
    private void accelerateSpecialFlower(TileEntitySpecialFlower flower, int times) {
        try {
            Method tickFlowerMethod =
                    TileEntitySpecialFlower.class.getDeclaredMethod("tickFlower");
            tickFlowerMethod.setAccessible(true);

            for (int i = 0; i < times; i++) {
                tickFlowerMethod.invoke(flower);
            }
        } catch (Exception e) {
            flower.ticksExisted += times;
        }
    }

    /**
     * 秋+冬：冻结液体 + 冰霜护盾
     */
    private void effectAutumnWinter() {
        int manaCost = 1000;
        if (getMana() < manaCost) return;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-3, -1, -3),
                worldPosition.offset(3, 1, 3))) {

            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
            } else if (state.is(Blocks.LAVA)) {
                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }

        // 给予附近玩家伤害吸收
        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(5));

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
        }

        addMana(-manaCost);
    }

    /**
     * 春+秋：自动收割
     */
    private void effectSpringAutumn() {
        int manaCost = 999;
        if (getMana() < manaCost) return;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-2, -1, -2),
                worldPosition.offset(2, 1, 2))) {

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CropBlock crop) {
                if (crop.isMaxAge(state)) {
                    // 收割作物
                    List<ItemStack> drops = Block
                            .getDrops(state, (ServerLevel) level,
                                    pos, null);

                    // 10%概率双倍掉落
                    if (level.random.nextFloat() < 0.1f) {
                        List<ItemStack> bonusDrops = new ArrayList<>(drops);
                        drops.addAll(bonusDrops);
                    }

                    for (ItemStack drop : drops) {
                        ItemEntity itemEntity = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                        level.addFreshEntity(itemEntity);
                    }

                    level.setBlock(pos, crop.getStateForAge(0), 3);
                }
            }
        }

        addMana(-manaCost);
    }

    // ========== 高阶符文效果 ==========

    /**
     * 双魔力符文：无损耗传输
     */
    private void effectDoubleMana() {
        int manaCost = 3000;
        if (getMana() < manaCost) return;

        if (level == null) return;

        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();

        // 扫描范围内的所有魔力收集器（传导器、发射器等）
        Set<IManaCollector> collectors = network.getAllCollectorsInWorld(level);
        int affectedCount = 0;

        for (IManaCollector collector : collectors) {
            BlockPos collectorPos = collector.getManaReceiverPos();

            // 检查是否在范围内
            double distance = Math.sqrt(worldPosition.distSqr(collectorPos));
            if (distance <= LOSSLESS_SCAN_RANGE) {
                // 添加或刷新无损耗效果
                losslessTransferTargets.put(collectorPos, LOSSLESS_DURATION);
                affectedCount++;

                // 播放激活粒子效果
                spawnActivationParticles(collectorPos);
            }
        }

        // 扫描范围内的所有魔力池（也可以作为传输节点）
        Set<IManaPool> pools = network.getAllPoolsInWorld(level);

        for (IManaPool pool : pools) {
            BlockPos poolPos = pool.getManaReceiverPos();

            double distance = Math.sqrt(worldPosition.distSqr(poolPos));
            if (distance <= LOSSLESS_SCAN_RANGE) {
                losslessTransferTargets.put(poolPos, LOSSLESS_DURATION);
                affectedCount++;

                spawnActivationParticles(poolPos);
            }
        }

        if (affectedCount > 0) {
            addMana(-manaCost);

            // 播放激活音效
            level.playSound(null, worldPosition,
                    ModSounds.enchanterForm,
                    SoundSource.BLOCKS, 1.0F, 1.5F);

            setChanged();
            sync();
        }
    }

    /**
     * 播放激活粒子效果
     */
    private void spawnActivationParticles(BlockPos pos) {
        if (level == null) return;

        // 从花到目标的连线粒子
        double startX = worldPosition.getX() + 0.5;
        double startY = worldPosition.getY() + 0.5;
        double startZ = worldPosition.getZ() + 0.5;

        double endX = pos.getX() + 0.5;
        double endY = pos.getY() + 0.5;
        double endZ = pos.getZ() + 0.5;

        // 生成连线粒子
        for (int i = 0; i < 20; i++) {
            double progress = i / 20.0;
            double x = startX + (endX - startX) * progress;
            double y = startY + (endY - startY) * progress;
            double z = startZ + (endZ - startZ) * progress;

            BotaniaAPI.instance().sparkleFX(level, x, y, z,
                    1.0F, 0.84F, 0.0F, 1.2F, 15);
        }

        // 目标位置爆发效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            BotaniaAPI.instance().sparkleFX(level,
                    endX + offsetX, endY + offsetY, endZ + offsetZ,
                    1.0F, 0.84F, 0.0F, 1.0F, 20);
        }
    }

    /**
     * 魔力符文 + 元素符文：工具充能
     */
    private void effectManaElemental() {
        int manaCost = 5000;
        if (getMana() < manaCost) return;

        if (level == null) return;

        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(5));

        boolean workDone = false;

        for (Player player : players) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.isEmpty()) continue;

            // 方式1: 通过Capability充能（标准植物魔法道具）
            if (mainHand.getItem() instanceof IManaItem manaItem) {
                int currentMana = manaItem.getMana();
                int maxMana = manaItem.getMaxMana();

                if (currentMana < maxMana) {
                    int manaToAdd = Math.min(manaCost, maxMana - currentMana);
                    if (manaToAdd > 0) {
                        manaItem.addMana(manaToAdd);
                        workDone = true;
                        spawnManaChargeParticles(player);
                        level.playSound(null, player.blockPosition(),
                                ModSounds.manaPoolCraft,
                                SoundSource.PLAYERS, 0.3F, 1.0F);
                    }
                }
            }
            // 方式2: 通过Capability充能（使用Forge Capability系统）
            else {
                LazyOptional<IManaItem> manaCap = mainHand.getCapability(
                        vazkii.botania.api.BotaniaForgeCapabilities.MANA_ITEM);

                if (manaCap.isPresent()) {
                    manaCap.ifPresent(manaItem -> {
                        int currentMana = manaItem.getMana();
                        int maxMana = manaItem.getMaxMana();

                        if (currentMana < maxMana) {
                            int manaToAdd = Math.min(manaCost, maxMana - currentMana);
                            if (manaToAdd > 0) {
                                manaItem.addMana(manaToAdd);

                                // 强制标记物品已修改
                                mainHand.setTag(mainHand.getTag());

                                spawnManaChargeParticles(player);
                                level.playSound(null, player.blockPosition(),
                                        ModSounds.manaPoolCraft,
                                        SoundSource.PLAYERS, 0.3F, 1.0F);
                            }
                        }
                    });
                    workDone = true;
                }

                // 方式3: 如果都不是魔力物品，则修复耐久
                else if (mainHand.isDamaged() && mainHand.isDamageableItem()) {
                    mainHand.setDamageValue(0);
                    workDone = true;

                    spawnRepairParticles(player);
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.ANVIL_USE,
                            SoundSource.PLAYERS, 0.5F, 1.5F);
                }
            }
        }

        if (workDone) {
            addMana(-manaCost);
        }
    }

    /**
     * 播放魔力充能粒子效果
     */
    private void spawnManaChargeParticles(Player player) {
        if (level == null) return;

        for (int i = 0; i < 10; i++) {
            double angle = (level.getGameTime() + i * 36) * 0.1;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = i * 0.1;

            BotaniaAPI.instance().sparkleFX(level,
                    player.getX() + offsetX,
                    player.getY() + 1.0 + offsetY,
                    player.getZ() + offsetZ,
                    0.0F, 0.5F, 1.0F,
                    1.2F, 12);
        }
    }

    /**
     * 播放修复粒子效果
     */
    private void spawnRepairParticles(Player player) {
        if (level == null) return;

        for (int i = 0; i < 15; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            BotaniaAPI.instance().sparkleFX(level,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    0.0F, 1.0F, 0.3F,
                    1.0F, 15);
        }
    }

    /**
     * 七宗罪符文组合：额外掉落
     */
    private void effectSinCombination() {
        int manaCost = 3560;
        if (getMana() < manaCost) return;

        if (level == null) return;

        // 扫描范围内的所有玩家
        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(SIN_SCAN_RANGE));

        int affectedCount = 0;
        for (Player player : players) {
            // 添加或刷新七宗罪效果
            sinEffectPlayers.put(player.getUUID(), SIN_DURATION);
            affectedCount++;

            // 给玩家发送消息
            if (!level.isClientSide) {
                player.displayClientMessage(
                        new TextComponent("§6§l七宗罪之力附体！挖掘时有概率获得额外掉落！"),
                        true
                );
            }

            // 播放粒子效果
            spawnSinEffectParticles(player);
        }

        if (affectedCount > 0) {
            addMana(-manaCost);

            // 播放音效
            level.playSound(null, worldPosition,
                    ModSounds.gaiaTrap,
                    SoundSource.BLOCKS, 1.0F, 0.8F);

            setChanged();
            sync();
        }
    }

    /**
     * 更新七宗罪效果
     */
    private void updateSinEffect() {
        if (level == null || level.isClientSide) return;

        Iterator<Map.Entry<UUID, Integer>> iterator = sinEffectPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int newTime = entry.getValue() - 1;

            if (newTime <= 0) {
                // 效果结束
                iterator.remove();

                // 通知玩家效果结束
                Player player = level.getPlayerByUUID(entry.getKey());
                if (player != null) {
                    player.displayClientMessage(
                            new TextComponent("§7七宗罪效果已消失"),
                            true
                    );
                }
            } else {
                entry.setValue(newTime);

                // 每40tick播放一次粒子效果
                if (newTime % 40 == 0) {
                    Player player = level.getPlayerByUUID(entry.getKey());
                    if (player != null) {
                        spawnSinEffectParticles(player);
                    }
                }
            }
        }
    }

    /**
     * 为玩家生成七宗罪粒子效果
     */
    private void spawnSinEffectParticles(Player player) {
        if (level == null) return;

        // 深红色粒子环绕玩家
        for (int i = 0; i < 5; i++) {
            double angle = (level.getGameTime() + i * 72) * 0.1;
            double offsetX = Math.cos(angle) * 0.8;
            double offsetZ = Math.sin(angle) * 0.8;

            BotaniaAPI.instance().sparkleFX(level,
                    player.getX() + offsetX,
                    player.getY() + 1.0,
                    player.getZ() + offsetZ,
                    0.6F, 0.0F, 0.2F, // 深红色
                    1.0F, 10);
        }
    }

    /**
     * 检查玩家是否有七宗罪效果
     */
    public boolean hasPlayerSinEffect(UUID playerUUID) {
        return sinEffectPlayers.containsKey(playerUUID) && sinEffectPlayers.get(playerUUID) > 0;
    }

    // ========== 辅助方法 ==========

    private boolean isValidRune(ItemStack stack) {
        return RUNE_MANA_VALUES.containsKey(stack.getItem());
    }

    private boolean isElementalRune(Set<Item> runes) {
        return runes.contains(ModItems.runeWater) || runes.contains(ModItems.runeFire) ||
                runes.contains(ModItems.runeEarth) || runes.contains(ModItems.runeAir);
    }

    private boolean isSinRuneCombination(Set<Item> runes) {
        int sinRuneCount = 0;
        Item[] sinRunes = {ModItems.runeLust, ModItems.runeGluttony, ModItems.runeGreed,
                ModItems.runeSloth, ModItems.runeWrath, ModItems.runeEnvy, ModItems.runePride};

        for (Item sinRune : sinRunes) {
            if (runes.contains(sinRune)) sinRuneCount++;
        }

        return sinRuneCount >= 2;
    }

    private boolean canSmelt(ItemStack stack) {
        return !getSmeltResult(stack).isEmpty();
    }

    private ItemStack getSmeltResult(ItemStack stack) {
        // 简化的熔炼逻辑
        if (stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE)) {
            return new ItemStack(Items.IRON_INGOT);
        } else if (stack.is(Items.GOLD_ORE) || stack.is(Items.DEEPSLATE_GOLD_ORE)) {
            return new ItemStack(Items.GOLD_INGOT);
        } else if (stack.is(Items.COPPER_ORE) || stack.is(Items.DEEPSLATE_COPPER_ORE)) {
            return new ItemStack(Items.COPPER_INGOT);
        }
        // 可以添加更多熔炼配方
        return ItemStack.EMPTY;
    }

    private void processRuneDrops(Item runeItem) {
        if (level == null) return;

        RuneDrops drops = RUNE_DROP_TABLE.get(runeItem);
        if (drops == null) return;

        for (DropEntry entry : drops.entries) {
            if (level.random.nextFloat() < entry.chance) {
                ItemStack dropStack = new ItemStack(entry.item, entry.count);
                ItemEntity itemEntity = new ItemEntity(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 1.0,
                        worldPosition.getZ() + 0.5,
                        dropStack);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    // ========== 修改 NBT 读写方法 ==========

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);

        // 保存无损耗传输目标
        ListTag targetsList = new ListTag();
        for (Map.Entry<BlockPos, Integer> entry : losslessTransferTargets.entrySet()) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            targetTag.putInt("time", entry.getValue());
            targetsList.add(targetTag);
        }
        cmp.put(TAG_LOSSLESS_TARGETS, targetsList);

        // 保存七宗罪效果玩家
        ListTag sinList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : sinEffectPlayers.entrySet()) {
            CompoundTag sinTag = new CompoundTag();
            sinTag.putUUID("player", entry.getKey());
            sinTag.putInt("time", entry.getValue());
            sinList.add(sinTag);
        }
        cmp.put(TAG_SIN_TARGETS, sinList);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);

        // 读取无损耗传输目标
        losslessTransferTargets.clear();
        if (cmp.contains(TAG_LOSSLESS_TARGETS)) {
            ListTag targetsList = cmp.getList(TAG_LOSSLESS_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < targetsList.size(); i++) {
                CompoundTag targetTag = targetsList.getCompound(i);
                BlockPos pos = NbtUtils.readBlockPos(targetTag.getCompound("pos"));
                int time = targetTag.getInt("time");
                losslessTransferTargets.put(pos, time);
            }
        }

        // 读取七宗罪效果玩家
        sinEffectPlayers.clear();
        if (cmp.contains(TAG_SIN_TARGETS)) {
            ListTag sinList = cmp.getList(TAG_SIN_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < sinList.size(); i++) {
                CompoundTag sinTag = sinList.getCompound(i);
                UUID playerUUID = sinTag.getUUID("player");
                int time = sinTag.getInt("time");
                sinEffectPlayers.put(playerUUID, time);
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new RuneFlowerWandHud(this)).cast());
    }

    public static class RuneFlowerWandHud extends HybridWandHud<TileEntityRuneFlower> {

        public RuneFlowerWandHud(TileEntityRuneFlower flower) {
            super(flower);
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            super.renderHUD(ms, mc);
        }
    }

    // ========== 内部类 ==========

    private static class RuneDrops {
        final List<DropEntry> entries;

        RuneDrops(DropEntry... entries) {
            this.entries = Arrays.asList(entries);
        }
    }

    private static class DropEntry {
        final Item item;
        final float chance;
        final int count;

        DropEntry(Item item, float chance, int count) {
            this.item = item;
            this.chance = chance;
            this.count = count;
        }
    }
}