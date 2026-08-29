package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordItem;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SwordAuraCastC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.WhitelistSyncS2CPacket;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof FlySwordItem)) return;
        playSwordAuraSound(event.getEntity());
        NetworkHandler.sendToServer(new SwordAuraCastC2SPacket());
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof FlySwordItem)) return;
        if (event.getLevel().isClientSide()) {
            playSwordAuraSound(event.getEntity());
            return;
        }
        FlySwordItem.trySpawnSwordAura(event.getEntity());
    }

    public static void playSwordAuraSound(Player player) {
        if (player == null || !player.level().isClientSide()) return;
        player.level().playLocalSound(
                player.getX(), player.getY(), player.getZ(),
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (ConfigFile.entityDamageWhitelistMap().isEmpty()) {
            ForgeGameEvent.rebuildEntityDamageWhitelistMap();
        }
        NetworkHandler.sendToPlayer(new WhitelistSyncS2CPacket(new ArrayList<>(ConfigFile.entityDamageWhitelistMap().keySet())), serverPlayer);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        EntityUtil.tickMovementLock(event.getEntity());
    }
}
