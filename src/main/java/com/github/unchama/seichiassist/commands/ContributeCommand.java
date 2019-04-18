package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * @author unicroak
 * TODO: Implement CommandExecutor instead of TabExecutor
 */
public class ContributeCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        final Sql sql = SeichiAssist.sql;

        if (args.length != 3 || args[0].equalsIgnoreCase("HELP")) {
            showHelpMessage(sender);

            return true;
        }

        final String commandType = args[0];
        final Player givenPlayer = Bukkit.getServer().getPlayer(args[1]);
        final Optional<Integer> pointOptional = TypeConverter.tryToParseInt(args[2]);
        if (!pointOptional.isPresent()) {
            sender.sendMessage(ChatColor.RED + "貢献ポイントは半角数字で入力してください");

            return true;
        }
        final int point = pointOptional.get();

        final PlayerData playerData = SeichiAssist.playermap.get(givenPlayer.getUniqueId());

        switch (commandType.toUpperCase()) {
            case "ADD":
            case "INCREMENT":
                if (!sql.setContribute(sender, givenPlayer.getName(), point)) return true;

                playerData.contribute_point += point;
                playerData.isContribute(givenPlayer, point);

                sender.sendMessage(ChatColor.GREEN + givenPlayer.getName() + "に貢献度ポイント" + point + "を追加しました");

                return true;
            case "REMOVE":
            case "DECREMENT":
                if (!sql.setContribute(sender, givenPlayer.getName(), -point)) return true;

                playerData.contribute_point -= point;
                playerData.isContribute(givenPlayer, -point);

                sender.sendMessage(ChatColor.GREEN + givenPlayer.getName() + "の貢献度ポイントを" + point + "減少させました");

                return true;
            default:
                showHelpMessage(sender);

                return true;
        }
    }

    private static void showHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[コマンドリファレンス]");
        sender.sendMessage(ChatColor.RED + "/contribute add <プレイヤー名> <増加分ポイント>");
        sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分増加させます");

        sender.sendMessage(ChatColor.RED + "/contribute remove <プレイヤー名> <減少分ポイント>");
        sender.sendMessage("指定されたプレイヤーの貢献度ptを指定分減少させます(入力ミス回避用)");
    }

    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        return null; // :(
    }

}
