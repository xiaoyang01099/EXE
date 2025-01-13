package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class PlumedBelt extends ItemBauble {

    public PlumedBelt(Properties rarity) {
        super(new Properties().stacksTo(1).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA));
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
                if (source == DamageSource.FLY_INTO_WALL ||
                        source == DamageSource.FALL ||
                        source == DamageSource.CRAMMING ||
                        source.isExplosion()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}