package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.block;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;


import java.util.function.Supplier;

public class SnowFlowerBlock extends FlowerBlock {
    public SnowFlowerBlock() {
        super(MobEffects.MOVEMENT_SPEED, 100, Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .explosionResistance(120000000000.0F));
    }
}
