package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.recipe.IOrechidRecipe;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;
import vazkii.botania.common.block.subtile.functional.SubTileOrechid;
import vazkii.botania.common.crafting.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class OrechidEndiumTile extends SubTileOrechid {
    private static final int COST = 20000;

    public OrechidEndiumTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.ORECHIDENDIUMTILE.get(), blockPos, blockState);
    }

    public boolean canOperate() {
        return this.getLevel().dimensionType().hasCeiling();
    }

    public RecipeType<? extends IOrechidRecipe> getRecipeType() {
        return ModRecipeTypes.ORECHID_IGNEM_TYPE;
    }

    public int getCost() {
        return 20000;
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<OrechidEndiumTile> {
        public FunctionalWandHud(OrechidEndiumTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level != null && level.isClientSide() && level.getGameTime() % 5 == 0 && level.random.nextInt(3) == 0) {
        }

        if (level == null || level.isClientSide) {
            return;
        }

        double particleChance = 1F - (double) getMana() / (double) getMaxMana() / 3.5F;
        int color = getColor();
        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        if (Math.random() > particleChance) {
            Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
            double x = getBlockPos().getX() + offset.x;
            double y = getBlockPos().getY() + offset.y;
            double z = getBlockPos().getZ() + offset.z;
            BotaniaAPI.instance().sparkleFX(level, x + 0.3 + Math.random() * 0.5, y + 0.5 + Math.random() * 0.5, z + 0.3 + Math.random() * 0.5, red, green, blue, (float) Math.random(), 5);
        }

        if (level.getGameTime() % 20 == 0 && getMana() >= 20000) {
            BlockPos pos = this.worldPosition;
            int range = 1;
            Iterable<BlockPos> blockPos = BlockPos.betweenClosed(pos.offset(-range, 0, -range), pos.offset(range, range, range));
            for (BlockPos blockPo : blockPos) {
                if (level.random.nextDouble() > 0.5) continue;
                BlockState state = level.getBlockState(blockPo);
                if (state.is(BlockTags.BASE_STONE_NETHER) || state.getBlock() == Blocks.END_STONE){
                    addMana(-20000);
                    level.destroyBlock(blockPo, false);
                    level.setBlockAndUpdate(blockPo, getOre());
                    level.playSound(null, blockPo, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }

        }
    }

    //获取随机矿物
    private BlockState getOre(){
        List<Block> blocks = new ArrayList<>();
        for (Block block : Registry.BLOCK) {
            if (block instanceof OreBlock)
                blocks.add(block);
        }
        if (blocks.isEmpty() || level == null) return Blocks.AIR.defaultBlockState();
        return blocks.get(level.random.nextInt(blocks.size())).defaultBlockState();
    }

    @Override
    public int getMaxMana() {
        return 1000000;
    }

    @Override
    public int getColor() {
        return 0x800080;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Rectangle(getEffectivePos(), new AABB(-8, -4, -8, 8, 4, 8));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

}
