package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeMod;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DBHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Redirect(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"
            )
    )
    private double adjustReachAttributeDisplay(AttributeModifier modifier, Player player, TooltipFlag flag) {
        double baseAmount = modifier.getAmount();
        UUID modifierId = modifier.getId();

        if (DBHelper.BASE_ENTITY_REACH_UUID.equals(modifierId)) {
            return baseAmount + player.getAttributeBaseValue(ForgeMod.ATTACK_RANGE.get());
        }
        else if (DBHelper.BASE_BLOCK_REACH_UUID.equals(modifierId)) {
            return baseAmount + player.getAttributeBaseValue(ForgeMod.REACH_DISTANCE.get());
        }

        return baseAmount;
    }
}
