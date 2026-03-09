package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.AlphirinePortal;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.AncientAlphirineRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class AncientAlphirineTile extends TileEntityFunctionalFlower {
    protected static final int MANA_REQUIRED = 4500;

    public AncientAlphirineTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AncientAlphirineTile> {
        public FunctionalWandHud(AncientAlphirineTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        BlockPos pos = getBlockPos();
        Level level = getLevel();

        if (level == null) return;

        if (ticksExisted % 10 == 0 && getMana() >= MANA_REQUIRED) {
            AABB searchArea = new AABB(pos).inflate(1.0D, 0.0D, 1.0D);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchArea);

            for (ItemEntity itemEntity : items) {
                if (!itemEntity.isRemoved() && itemEntity.getItem().getCount() >= 1) {
                    ItemStack itemStack = itemEntity.getItem();

                    Optional<AncientAlphirineRecipe> recipeOptional = findMatchingRecipe(level, itemStack);

                    if (recipeOptional.isPresent()) {
                        AncientAlphirineRecipe recipe = recipeOptional.get();

                        if (level.isClientSide) {
                            spawnParticle(level, itemEntity);
                        } else {

                            if (itemStack.getCount() > 1) {
                                itemStack.shrink(1);
                            } else {
                                itemEntity.discard();
                            }

                            if (level.random.nextInt(101) <= recipe.getChance()) {
                                spawnPortal(level, recipe.getResultItem().copy(), pos);
                                addMana(-MANA_REQUIRED);
                            } else {
                                addMana(-MANA_REQUIRED / 10);
                            }

                            setChanged();
                        }
                        return;
                    }
                }
            }
        }
    }

    private Optional<AncientAlphirineRecipe> findMatchingRecipe(Level level, ItemStack input) {
        RecipeManager recipeManager = level.getRecipeManager();

        Container container = new SimpleContainer(1);
        container.setItem(0, input);

        return recipeManager.getAllRecipesFor(ModRecipes.ANCIENT_ALPHIRINE_TYPE)
                .stream()
                .filter(recipe -> recipe.matches(container, level))
                .findFirst();
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 1);
    }

    private void spawnPortal(Level level, ItemStack stack, BlockPos flowerPos) {
        AlphirinePortal portal = new AlphirinePortal(level);

        double itemX = flowerPos.getX() + 0.5D + (level.random.nextDouble() * 2.0D - 1.0D);
        double itemY = flowerPos.getY() + 1.2D + (level.random.nextDouble() - 0.5D);
        double itemZ = flowerPos.getZ() + 0.5D + (level.random.nextDouble() * 2.0D - 1.0D);

        portal.setPos(itemX, itemY, itemZ);
        portal.setStack(stack);
        level.addFreshEntity(portal);
    }

    private void spawnParticle(Level level, ItemEntity itemEntity) {
        if (level.isClientSide) {
            for (int i = 0; i < 10; ++i) {
                double mx = (level.random.nextDouble() - 0.5D) * 0.2D;
                double my = (level.random.nextDouble() - 0.5D) * 0.2D;
                double mz = (level.random.nextDouble() - 0.5D) * 0.2D;

                BotaniaAPI.instance().sparkleFX(level,
                        itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        1.0F, 0.8F, 0.8F,
                        (float) Math.random(), 5);
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return 180000;
    }

    @Override
    public int getColor() {
        return 0xD0C0C8;
    }
}