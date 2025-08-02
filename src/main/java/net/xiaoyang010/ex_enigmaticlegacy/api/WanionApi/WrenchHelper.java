package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.google.common.collect.Lists;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class WrenchHelper {
    public static final WrenchHelper INSTANCE = new WrenchHelper();
    public final HashSet<Item> wrenches = new HashSet<>();

    private WrenchHelper() {
        final List<Item> wrenchList = Lists.newArrayList(
//                Item.get("thermalfoundation:wrench"),					Item.getByNameOrId("appliedenergistics2:certus_quartz_wrench"),
//                Item.getByNameOrId("appliedenergistics2:nether_quartz_wrench"), Item.getByNameOrId("enderio:item_yeta_wrench"),
//                Item.getByNameOrId("ic2:wrench"), 								Item.getByNameOrId("ic2:electric_wrench"),
//                Item.getByNameOrId("redstonearsenal:tool.wrench_flux"),			Item.getByNameOrId("redstonearsenal:tool.battlewrench_flux"),
//                Item.getByNameOrId("pneumaticcraft:pneumatic_wrench")
        );
        wrenchList.stream().filter(Objects::nonNull).forEach(wrenches::add);
    }

    public boolean isWrench(@Nonnull final ItemStack stack) {
        return wrenches.contains(stack.getItem());
    }
}