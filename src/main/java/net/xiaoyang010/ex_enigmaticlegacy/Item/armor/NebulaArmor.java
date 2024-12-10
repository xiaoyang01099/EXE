package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.model.ModelArmorNebula;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;

import java.util.function.Consumer;

public abstract class NebulaArmor extends ItemManasteelArmor implements IManaItem {
    private static final String TAG_MANA = "mana";
    private static final int MAX_MANA = 250000;
    private static ItemStack[] armorSet;

    public NebulaArmor(EquipmentSlot slot, Properties props) {
        super(slot, AdvancedBotanyAPI.nebulaArmorMaterial,
                        props.tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                                .durability(1000)
                                .rarity(AdvancedBotanyAPI.rarityNebula));
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public net.minecraft.client.model.HumanoidModel<?> getArmorModel(LivingEntity entity,
                                                                             ItemStack stack, EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> defaultModel) {

                var meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.1F), 0.0F);
                var layerDefinition = LayerDefinition.create(meshDefinition, 64, 128);
                var modelPart = layerDefinition.bakeRoot();

                return new ModelArmorNebula(modelPart, slot);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static LayerDefinition createArmorLayer() {
        return ModelArmorNebula.createArmorLayer();
    }

    public ItemStack[] getArmorSet() {
        if (armorSet == null) {
            armorSet = new ItemStack[]{
                    new ItemStack(ModArmor.NEBULA_HELMET.get()),
                    new ItemStack(ModArmor.NEBULA_CHESTPLATE.get()),
                    new ItemStack(ModArmor.NEBULA_LEGGINGS.get()),
                    new ItemStack(ModArmor.NEBULA_BOOTS.get())
            };
        }
        return armorSet;
    }

    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        switch(slot) {
            case HEAD:
                return stack.getItem() == ModArmor.NEBULA_HELMET.get() ||
                        stack.getItem() == ModArmor.NEBULA_HELMET_REVEAL.get();
            case CHEST:
                return stack.getItem() == ModArmor.NEBULA_CHESTPLATE.get();
            case LEGS:
                return stack.getItem() == ModArmor.NEBULA_LEGGINGS.get();
            case FEET:
                return stack.getItem() == ModArmor.NEBULA_BOOTS.get();
            default:
                return false;
        }
    }


    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (!world.isClientSide() && getMana() != getMaxMana() &&
                ManaItemHandler.instance().requestManaExactForTool(stack, player, 1000, true)) {
            addMana(1000);
        }
    }

    @Override
    public int getMana() {
        return 0;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {

    }

    /*@Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (!world.isClientSide() && getManaInternal(stack) != MAX_MANA &&
                ManaItemHandler.instance().requestManaExactForTool(stack, player, 1000, true)) {
            setManaInternal(stack, getManaInternal(stack) + 1000);
        }
    }*/

    protected int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    protected void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.min(mana, MAX_MANA));
    }

    @Override
    public int getDamage(ItemStack stack) {
        float mana = getManaInternal(stack);
        return 1000 - (int)(mana / MAX_MANA * 1000.0F);
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;  // 允许从魔力池接收魔力
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack) {
        return true;  // 允许从物品接收魔力
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return false;  // 不允许导出魔力到魔力池
    }

    @Override
    public boolean canExportManaToItem(ItemStack stack) {
        return false;  // 不允许导出魔力到物品
    }

    @Override
    public boolean isNoExport() {
        return true;  // 标记为不可导出魔力
    }

    public abstract float getEfficiency(ItemStack stack, Player player);
}