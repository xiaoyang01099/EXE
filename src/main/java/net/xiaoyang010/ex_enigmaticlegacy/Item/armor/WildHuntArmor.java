/*package net.xiaoyang010.ex_enigmaticlegacy.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
import net.xiaoyang010.ex_enigmaticlegacy.client.renderer.model.ModelArmorWildHunt;
import net.xiaoyang010.ex_enigmaticlegacy.init.ModArmor;
import net.xiaoyang010.ex_enigmaticlegacy.init.ModTabs;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;

import java.awt.*;
import java.util.function.Consumer;


public class WildHuntArmor extends ItemManasteelArmor {
    private static ItemStack[] armorset;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "wild_hunt_armor"), "main");

    public WildHuntArmor(EquipmentSlot slot, Properties props) {
        super(slot, AdvancedBotanyAPI.wildHuntArmor,
                props.tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack,
                                                  EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                var model = new ModelArmorWildHunt(Minecraft.getInstance().getEntityModels()
                        .bakeLayer(LAYER_LOCATION), armorSlot.getIndex());
                setupModelForSlot(armorSlot, model);
                return model;
            }
        });
    }

    // 添加模型层定义的静态方法
    @OnlyIn(Dist.CLIENT)
    public static LayerDefinition createBodyLayer() {
        return ModelArmorWildHunt.createBodyLayer();
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return AdvancedBotanyAPI.rarityWildHunt;
    }

    public ItemStack[] getArmorSetStacks() {
        if (armorset == null) {
            armorset = new ItemStack[]{
                    new ItemStack(ModArmor.WILD_HUNT_HELM.get()),
                    new ItemStack(ModArmor.WILD_HUNT_CHEST.get()),
                    new ItemStack(ModArmor.WILD_HUNT_LEGS.get()),
                    new ItemStack(ModArmor.WILD_HUNT_BOOTS.get())
            };
        }
        return armorset;
    }

    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        return switch (slot) {
            case HEAD -> stack.getItem() == ModArmor.WILD_HUNT_HELM.get();
            case CHEST -> stack.getItem() == ModArmor.WILD_HUNT_CHEST.get();
            case LEGS -> stack.getItem() == ModArmor.WILD_HUNT_LEGS.get();
            case FEET -> stack.getItem() == ModArmor.WILD_HUNT_BOOTS.get();
            default -> false;
        };
    }

    private void setupModelForSlot(EquipmentSlot slot, ModelArmorWildHunt model) {
        model.getHead().visible = slot == EquipmentSlot.HEAD;
        model.chest.visible = slot == EquipmentSlot.CHEST;
        model.leftArm.visible = slot == EquipmentSlot.CHEST;
        model.rightArm.visible = slot == EquipmentSlot.CHEST;
        model.leftLeg.visible = slot == EquipmentSlot.LEGS;
        model.rightLeg.visible = slot == EquipmentSlot.LEGS;
        model.leftBoot.visible = slot == EquipmentSlot.FEET;
        model.rightBoot.visible = slot == EquipmentSlot.FEET;
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return "ex_enigmaticlegacy:textures/model/armor/wild_hunt_armor.png";
    }


    private void addArmorSetDescription(ItemStack stack, List list) {
        for (int i = 0; i < 3; i++) {
            list.add(String.valueOf(new TranslatableComponent("ex_enigmaticlegacy.armorset.wild_hunt.desc" + i)));
        }
    }
}*/