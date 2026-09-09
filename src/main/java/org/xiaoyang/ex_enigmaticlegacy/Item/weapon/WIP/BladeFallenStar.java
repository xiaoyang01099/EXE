package org.xiaoyang.ex_enigmaticlegacy.Item.weapon.WIP;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.EXEAPI;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BladeFallenStar extends SwordItem {
    private static final Map<UUID, YuhuaData> YUHUA_ENTITIES = new HashMap<>();
    private static final int   MAX_HITS        = 3;
    private static final float YUHUA_THRESHOLD = 0.25f;
    private static final int   YUHUA_DURATION  = 40; // 2秒

    private static class YuhuaData {
        int hitCount;
        boolean pendingDeath;
        int ticksAlive;

        YuhuaData(int hitCount, boolean pendingDeath) {
            this.hitCount     = hitCount;
            this.pendingDeath = pendingDeath;
            this.ticksAlive   = 0;
        }
    }

    public BladeFallenStar() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 120, -2.4F, new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            YuhuaExecutions.action(serverPlayer, player.isShiftKeyDown() ? 1 : 0);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    public static boolean isYuhua(UUID uuid) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.isSameThread()) {
            var entry = YuhuaExecutions.data(server).entries.get(uuid);
            return entry != null && entry.stone;
        }
        return YuhuaNetwork.clientLocked.test(uuid);
    }
}
