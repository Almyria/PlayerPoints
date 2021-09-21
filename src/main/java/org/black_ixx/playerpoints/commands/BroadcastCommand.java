package org.black_ixx.playerpoints.commands;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.manager.CommandManager;
import org.black_ixx.playerpoints.manager.LocaleManager;
import org.black_ixx.playerpoints.util.PointsUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand extends PointsCommand {

    public BroadcastCommand() {
        super("broadcast", CommandManager.CommandAliases.BROADCAST);
    }

    @Override
    public void execute(PlayerPoints plugin, CommandSender sender, String[] args) {
        LocaleManager localeManager = plugin.getManager(LocaleManager.class);
        if (args.length < 1) {
            localeManager.sendMessage(sender, "command-broadcast-usage");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer player = PointsUtils.getPlayerByName(args[0]);
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                localeManager.sendMessage(sender, "unknown-player", StringPlaceholders.single("player", args[0]));
                return;
            }

            int points = plugin.getAPI().look(player.getUniqueId());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                localeManager.sendMessage(onlinePlayer, "command-broadcast-message", StringPlaceholders.builder("player", player.getName())
                        .addPlaceholder("amount", PointsUtils.formatPoints(points))
                        .addPlaceholder("currency", localeManager.getCurrencyName(points)).build());
            }
        });
    }

    @Override
    public List<String> tabComplete(PlayerPoints plugin, CommandSender sender, String[] args) {
        return args.length != 1 ? Collections.emptyList() : PointsUtils.getPlayerTabComplete(args[0]);
    }

}
