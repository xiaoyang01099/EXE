package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.DragonWingArmorMaterial;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.SMaterial;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.ZMaterial;

public class ModMaterials {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, "ex_enigmaticlegacy");

    // 定义自定义盔甲材料
    public static final ZMaterial MANAITA_CHESTPLATE_MATERIAL = new ZMaterial();
    public static final SMaterial TERRO_RCROWN = new SMaterial();
    public static final DragonWingArmorMaterial DRAGON_WINGS = new DragonWingArmorMaterial();
}
