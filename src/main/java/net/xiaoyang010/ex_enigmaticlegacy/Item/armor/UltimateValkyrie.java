package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.integral.enigmaticlegacy.api.items.ICursed;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.WitherArmorLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieBoots;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieChestplate;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieHelmet;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieLeggings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class UltimateValkyrie extends ArmorItem implements ICursed {
    public UltimateValkyrie(EquipmentSlot slot, Properties properties) {
        super(ValkyrieMaterial.INSTANCE, slot, properties);
    }

    @Override
    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.nullToEmpty("§7》§e全套-能量护甲:"));
        tooltipComponents.add(Component.nullToEmpty("§7可以抵御25次致命伤害，冷却20秒。"));
        super.appendHoverText(stack, level, tooltipComponents, flag);
    }

    public static boolean isFullSuit(Player player) {
        return player.getInventory().armor.get(0).getItem() instanceof UltimateValkyrieBoots
                && player.getInventory().armor.get(1).getItem() instanceof UltimateValkyrieLeggings
                && player.getInventory().armor.get(2).getItem() instanceof UltimateValkyrieChestplate
                && player.getInventory().armor.get(3).getItem() instanceof UltimateValkyrieHelmet;
    }

//    public static boolean isFullSuit(Player player) {
//        return player.getInventory().armor.get(2).getItem() instanceof UltimateValkyrie
//                && player.getInventory().armor.get(3).getItem() instanceof UltimateValkyrie;
//    }
}