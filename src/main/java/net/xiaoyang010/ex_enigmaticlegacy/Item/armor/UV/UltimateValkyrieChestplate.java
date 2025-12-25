package net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UltimateValkyrieChestplate extends UltimateValkyrie {
    UUID uuid =UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");

    public UltimateValkyrieChestplate(Properties properties) {
        super(EquipmentSlot.CHEST, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 3, false, false));

            float healthPercentage = player.getHealth() / player.getMaxHealth();
            if (healthPercentage < 0.5F) {
                int resistanceLevel = (int) ((1.0F - healthPercentage) * 10);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, Math.min(resistanceLevel, 5), false, false));
            }
        }
        super.onArmorTick(stack, level, player);
    }


    @Override
    public Component getName(ItemStack stack) {
        return Component.nullToEmpty("§c武神重甲");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipComponents, flag);
        tooltipComponents.add(Component.nullToEmpty("§7》§6屹立不倒:"));
        tooltipComponents.add(Component.nullToEmpty("§7将大多数伤害最小化,且血量越少防御越高。"));
        tooltipComponents.add(Component.nullToEmpty("§7》§c越战越勇:"));
        tooltipComponents.add(Component.nullToEmpty("§7每击杀一个敌人都会提高伤害。"));
        tooltipComponents.add(Component.nullToEmpty("§7》§5凋零之触:"));
        tooltipComponents.add(Component.nullToEmpty("§7攻击造成5秒凋零V并吸取30%生命值。"));
        tooltipComponents.add(Component.nullToEmpty("§7》§d全套强化:"));
        tooltipComponents.add(Component.nullToEmpty("§7• 吸血和凋零效果翻倍（60%吸血，凋零X）"));
        tooltipComponents.add(Component.nullToEmpty("§7• 凋零骷髅成为友军，攻击力×5"));
        tooltipComponents.add(Component.nullToEmpty("§7• 击杀骷髅时召唤增强凋零骷髅"));
        tooltipComponents.add(Component.nullToEmpty("§7• 空手右键发射凋零骷髅头（消耗1经验）"));
    }
}