package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.tile.mana.TileSpreader;

@Mixin(value = TileSpreader.class)
public interface ManaSpreaderBlockEntityAccessor {

    @Accessor(value = "receiver", remap = false)
    IManaReceiver getReceiver();

}