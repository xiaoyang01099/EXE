package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public class PlumedBelt extends BaubleItem {

    public PlumedBelt(Properties rarity) {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player) {
            entity.fallDistance = 0f;
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasPlumeBelt = CuriosApi.getCuriosHelper()
                    .findCurios(player, stack -> stack.getItem() instanceof PlumedBelt)
                    .stream()
                    .findFirst()
                    .isPresent();

            if (hasPlumeBelt) {
                DamageSource source = event.getSource();

                if (source.is(DamageTypeTags.IS_FALL) ||
                        source.is(DamageTypeTags.IS_EXPLOSION) ||
                        source.is(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH) ||
                        source.getMsgId().equals("flyIntoWall")) {
                    event.setCanceled(true);
                }
            }
        }
    }
}