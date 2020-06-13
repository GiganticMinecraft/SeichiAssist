package com.github.unchama.buildassist.command;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.data.PlayerData;
import com.github.unchama.buildassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.TypeConverter;
import com.github.unchama.seichiassist.util.exp.IExperienceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * {@code fly}コマンドを定義する。
 */
public final class FlyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label,
                             final String[] args) {
        //プレイヤーからの送信でない時処理終了
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN
                    + "fly機能を利用したい場合は、末尾に「利用したい時間(分単位)」の数値を、");
            sender.sendMessage(ChatColor.GREEN
                    + "fly機能を中断したい場合は、末尾に「finish」を記入してください。");
            return true;
        }

        if (args.length == 1) {
            //プレイヤーを取得
            // safe cast
            final Player player = (Player) sender;
            //UUIDを取得
            final UUID uuid = player.getUniqueId();
            //playerdataを取得
            final PlayerData playerdata = BuildAssist.playermap().getOrElse(uuid, () -> null);
            //プレイヤーデータが無い場合は処理終了
            if (playerdata == null) {
                return false;
            }

            final IExperienceManager expman = new ExperienceManager(player);

            int flytime = playerdata.flyMinute;
            final boolean endlessFly = playerdata.doesEndlessFly;
            final String query = args[0].toLowerCase();
            if (query.equals("finish")) {
                playerdata.isFlying = false;
                playerdata.flyMinute = 0;
                playerdata.doesEndlessFly = false;
                player.setAllowFlight(false);
                player.setFlying(false);
                sender.sendMessage(ChatColor.GREEN
                        + "fly効果を停止しました。");
            } else if (query.equals("endless")) {

                if (!expman.hasExp(BuildAssist.config().getFlyExp())) {
                    sender.sendMessage(ChatColor.GREEN
                            + "所持している経験値が、必要経験値量(" + BuildAssist.config().getFlyExp() + ")に達していません。");
                } else {
                    playerdata.isFlying = true;
                    playerdata.doesEndlessFly = true;
                    playerdata.flyMinute = 0;
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    sender.sendMessage(ChatColor.GREEN
                            + "無期限でfly効果をONにしました。");
                }

            } else if (TypeConverter.isParsableToInteger(query)) {
                final int minutes = Integer.parseInt(query);
                if (minutes <= 0) {
                    sender.sendMessage(ChatColor.GREEN
                            + "時間指定の数値は「1」以上の整数で行ってください。");
                    return true;
                } else if (!expman.hasExp(BuildAssist.config().getFlyExp())) {
                    sender.sendMessage(ChatColor.GREEN
                            + "所持している経験値が、必要経験値量(" + BuildAssist.config().getFlyExp() + ")に達していません。");
                } else {
                    if (endlessFly) {
                        sender.sendMessage(ChatColor.GREEN
                                + "無期限飛行モードは解除されました。");
                    }
                    flytime += minutes;
                    playerdata.isFlying = true;
                    playerdata.flyMinute = flytime;
                    playerdata.doesEndlessFly = false;
                    sender.sendMessage(ChatColor.YELLOW + "【flyコマンド認証】効果の残り時間はあと"
                            + flytime + "分です。");
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            } else {
                sender.sendMessage(ChatColor.GREEN
                        + "fly機能を利用したい場合は、末尾に「利用したい時間(分単位)」の数値を、");
                sender.sendMessage(ChatColor.GREEN
                        + "fly機能を中断したい場合は、末尾に「finish」を記入してください。");
            }
            return true;
        }
        return false;
    }
}
