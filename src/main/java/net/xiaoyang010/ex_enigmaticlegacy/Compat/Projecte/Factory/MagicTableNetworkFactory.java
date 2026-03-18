package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableConvertPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableCustomAmountPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableGearPacket;

public class MagicTableNetworkFactory {

    public static void register(SimpleChannel channel, int startId) {
        channel.registerMessage(
                startId,
                MagicTableConvertPacket.class,
                MagicTableConvertPacket::encode,
                MagicTableConvertPacket::decode,
                MagicTableConvertPacket::handle
        );

        channel.registerMessage(
                startId + 1,
                MagicTableGearPacket.class,
                MagicTableGearPacket::encode,
                MagicTableGearPacket::decode,
                MagicTableGearPacket::handle
        );

        channel.registerMessage(
                startId + 2,
                MagicTableCustomAmountPacket.class,
                MagicTableCustomAmountPacket::encode,
                MagicTableCustomAmountPacket::decode,
                MagicTableCustomAmountPacket::handle
        );

        channel.registerMessage(
                startId + 3,
                EMCWandRequestPacket.class,
                EMCWandRequestPacket::encode,
                EMCWandRequestPacket::decode,
                EMCWandRequestPacket::handle
        );

        channel.registerMessage(
                startId + 4,
                EMCWandSetValuePacket.class,
                EMCWandSetValuePacket::encode,
                EMCWandSetValuePacket::decode,
                EMCWandSetValuePacket::handle
        );

        channel.registerMessage(
                startId + 5,
                EMCWandItemListPacket.class,
                EMCWandItemListPacket::encode,
                EMCWandItemListPacket::decode,
                EMCWandItemListPacket::handle
        );

        channel.registerMessage(
                startId + 6,
                EMCWandResultPacket.class,
                EMCWandResultPacket::encode,
                EMCWandResultPacket::decode,
                EMCWandResultPacket::handle
        );

        channel.registerMessage(
                startId + 7,
                EMCWandResetPacket.class,
                EMCWandResetPacket::toBytes,
                EMCWandResetPacket::new,
                EMCWandResetPacket::handle
        );
    }
}