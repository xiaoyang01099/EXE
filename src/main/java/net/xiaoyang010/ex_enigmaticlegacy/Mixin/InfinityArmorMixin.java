package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import morph.avaritia.item.InfinityArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model.InfinityArmorModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.client.lib.LibResources;

import java.util.function.Consumer;

@Mixin(InfinityArmorItem.class)
public class InfinityArmorMixin implements IPhantomInkable {

    @Unique
    private static final String ex_enigmaticlegacy$PHANTOM_INK_TAG = "phantomInk";

    @Unique
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public HumanoidModel<?> getArmorModel(LivingEntity entity, ItemStack stack,
                                                  EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                return ex_enigmaticlegacy$createInfinityArmorModel(entity, stack, slot, defaultModel);
            }
        });
    }

    @Unique
    private static HumanoidModel<?> ex_enigmaticlegacy$createInfinityArmorModel(
            LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {

        if (ex_enigmaticlegacy$hasPhantomInk(stack)) {
            HumanoidModel<?> invisibleModel = new HumanoidModel<>(HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F).getRoot().bake(64, 64));
            invisibleModel.setAllVisible(false);
            return invisibleModel;
        }

        boolean isLegs = (slot == EquipmentSlot.LEGS);

        try {
            ModelPart bakedMesh = InfinityArmorModel.createMeshes(
                    new CubeDeformation(1.0F),
                    0.0F,
                    isLegs
            ).getRoot().bake(64, 64);

            InfinityArmorModel armorModel = new InfinityArmorModel(bakedMesh);

            armorModel.update(entity, stack, slot);

            if (defaultModel != null) {
                armorModel.crouching = defaultModel.crouching;
                armorModel.riding = defaultModel.riding;
                armorModel.young = defaultModel.young;

                ex_enigmaticlegacy$syncModelPoses(defaultModel, armorModel, slot);
            }

            return armorModel;

        } catch (Exception e) {
            e.printStackTrace();
            return defaultModel;
        }
    }

    @Unique
    private static boolean ex_enigmaticlegacy$hasPhantomInk(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.getBoolean("phantomInk");
        }
        return false;
    }

    @Unique
    private static void ex_enigmaticlegacy$syncModelPoses(HumanoidModel<?> source, HumanoidModel<?> target, EquipmentSlot slot) {
        try {
            if (slot == EquipmentSlot.LEGS) {
                target.rightLeg.copyFrom(source.rightLeg);
                target.leftLeg.copyFrom(source.leftLeg);
            } else {
                target.head.copyFrom(source.head);
                target.hat.copyFrom(source.hat);
                target.body.copyFrom(source.body);
                target.rightArm.copyFrom(source.rightArm);
                target.leftArm.copyFrom(source.leftArm);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean hasPhantomInk(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.getBoolean(ex_enigmaticlegacy$PHANTOM_INK_TAG);
        }
        return false;
    }

    @Override
    public void setPhantomInk(ItemStack stack, boolean ink) {
        CompoundTag tag = stack.getOrCreateTag();
        if (ink) {
            tag.putBoolean(ex_enigmaticlegacy$PHANTOM_INK_TAG, true);
        } else {
            tag.remove(ex_enigmaticlegacy$PHANTOM_INK_TAG);
        }
    }

    @Unique
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (hasPhantomInk(stack)) {
            return LibResources.MODEL_INVISIBLE_ARMOR;
        }

        return "ex_enigmaticlegacy:textures/models/armor/infinity_armor.png";
    }
}