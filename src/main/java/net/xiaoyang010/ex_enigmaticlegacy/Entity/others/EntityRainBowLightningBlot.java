package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

public class EntityRainBowLightningBlot extends LightningBolt {

    public EntityRainBowLightningBlot(EntityType<? extends LightningBolt> p_20865_, Level p_20866_) {
        super(p_20865_, p_20866_);
    }

    public EntityRainBowLightningBlot(ServerLevel level) {
        super(EntityType.LIGHTNING_BOLT, level);
    }
}
