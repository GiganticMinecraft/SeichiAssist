package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author unicroak
 * TODO: Implement CommandExecutor instead of TabExecutor
 */
public final class AchievementCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3 || !(sender instanceof Player)) {
            return false;
        }

        final Player operator = (Player) sender;

        final CommandOperation commandOperation;
        try {
            commandOperation = CommandOperation.valueOf(args[0]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        final int achievementNumber;
        try {
            achievementNumber = Integer.valueOf(args[1]);
        } catch (NumberFormatException ex) {
            return false;
        }

        final Set<Player> operatedPlayerSet;
        switch (args[2].toUpperCase()) {
            case "ALL":
                operatedPlayerSet = new HashSet<>(Bukkit.getServer().getOnlinePlayers());
                break;
            case "WORLD":
                operatedPlayerSet = new HashSet<>(operator.getWorld().getPlayers());
                break;
            default:
                Player foundPlayer = Bukkit.getPlayer(args[2]);
                if (foundPlayer == null) return false;

                // TODO: OfflinePlayerへの対応

                operatedPlayerSet = Collections.singleton(foundPlayer);
        }

        operatedPlayerSet.forEach(player -> commandOperation.operate(operator, player, achievementNumber));

        return false;
    }

    private enum CommandOperation {
        GIVE("配布", (player, achievementNumber) -> {
            final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
            playerData.TitleFlags.set(achievementNumber);

            player.sendMessage("運営チームよりNo" + achievementNumber + "の実績が配布されました。");
        }),
        DEPRIVE("剥奪", (player, achievementNumber) -> {
            final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
            playerData.TitleFlags.set(achievementNumber, false);
        });

        private final String operationName;
        private final BiConsumer<Player, Integer> operate;

        CommandOperation(String operationName, BiConsumer<Player, Integer> operate) {
            this.operationName = operationName;
            this.operate = operate;
        }

        void operate(Player operator, Player operatedPlayer, int achievementNumber) {
            operate
                    .andThen((player, number) -> operator.sendMessage(ChatColor.GREEN + player.getName() + "に対しNo" + number + "の実績を" + operationName + "しました"))
                    .accept(operatedPlayer, achievementNumber);
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null; // :(
    }

}
