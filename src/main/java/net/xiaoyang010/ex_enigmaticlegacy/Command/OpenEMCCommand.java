package net.xiaoyang010.ex_enigmaticlegacy.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.EMCWandHelper;

public class OpenEMCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openEMC")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");

                                    EMCWandHelper.setAuthorized(target.getUUID(), enabled);

                                    context.getSource().sendSuccess(
                                            new TranslatableComponent(
                                                    enabled ? "command.ex_enigmaticlegacy.openemc.enabled"
                                                            : "command.ex_enigmaticlegacy.openemc.disabled",
                                                    target.getDisplayName()),
                                            true);

                                    target.displayClientMessage(
                                            new TranslatableComponent(
                                                    enabled ? "command.ex_enigmaticlegacy.openemc.notify_enabled"
                                                            : "command.ex_enigmaticlegacy.openemc.notify_disabled"),
                                            false);

                                    return 1;
                                })))
        );
    }
}