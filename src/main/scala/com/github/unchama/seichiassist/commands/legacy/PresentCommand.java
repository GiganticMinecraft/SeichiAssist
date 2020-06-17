package com.github.unchama.seichiassist.commands.legacy;

import java.util.List;
import java.util.UUID;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

/**
* /<コマンド名未定> で識別されるコマンド。
*/
public class PresentCommand implements CommandExecutor {
    /**
    * 配布モード。ALLはオフラインプレイヤーを含めた全部のプレイヤー。LOGINは現在ログインしているプレイヤー。ONEは特定の誰か。
    */
    public enum PresentMode {
        ALL,
        LOGIN,
        ONE,;
    }
    private final SeichiAssist plugin = SeichiAssist.instance();

    @Override
    public List<String> onTabComplete(CommandSender paramCommandSender,
            Command paramCommand, String paramString,
            String[] paramArrayOfString) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        
        // プレイヤーからの送信でない時処理終了
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
            return true;
        }
        
        // プレイヤーを取得
        Player player = (Player) sender;
        
        // 運営かどうか。 TODO: 判定ロジック
        final boolean isAdmin = false;
        if (!isAdmin) {
            sender.sendMessage("このコマンドは運営以外は使用できません。");
            return false;
        }
        // modeを取る以上、引数があってほしい
        if (args.length == 0) return false;
        
        // 大文字小文字でブレるのはストレスなので小文字同士で比較
        final String mode = args[0].toLowerCase();
        final ItemStack stack = player.getInventory().getItemInMainHand();
        if (mode.equals("all")) {
            // TODO: 非同期
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                sendItem(p, stack);
            }
        } else if (mode.equals("login")) {
            // TODO: 非同期
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                sendItem(p, stack);
            }
        } else if (mode.equals("one")) {
            if (args.length == 2) {
                String name = args[1];
                Player target = Bukkit.getOfflinePlayer(name);
                if (target == null) {
                    sender.sendMessage(name + "というプレイヤーは存在しません");
                    return true;
                } else {
                    sendItem(target, stack);
                }
            } else {
                // 引数の個数が誤っている
                return false;
            }
        } else {
            return false;
        }

    }

    /**
    * ここでアイテムを実際に送信したりする。
    * @param target 送信するプレイヤー
    * @param item 送信するアイテム
    */
    public static boolean sendItem(OfflinePlayer target, ItemStack item) {
        throw new RuntimeException("This method is not implemented yet.");
    }
    
    /**
    * ここでアイテムを実際に送信したりする。
    * @param target 送信するプレイヤー
    * @param item 送信するアイテム
    */
    public static boolean sendItem(Player target, ItemStack item) {
        throw new RuntimeException("This method is not implemented yet.");
    }
}
