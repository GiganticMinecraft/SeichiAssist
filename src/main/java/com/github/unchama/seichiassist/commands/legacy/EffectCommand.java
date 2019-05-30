package com.github.unchama.seichiassist.commands.legacy;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EffectCommand implements CommandExecutor {

	// TODO これはここにあるべきではない
	private static void toggleEffect(PlayerData playerData) {
		int newEffectFlag = (playerData.effectflag + 1) % 6;
		Player player = Bukkit.getPlayer(playerData.uuid);

		if (newEffectFlag == 0) {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(無制限)");
		} else if (newEffectFlag == 1) {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(127制限)");
		} else if (newEffectFlag == 2) {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(200制限)");
		} else if (newEffectFlag == 3) {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(400制限)");
		} else if (newEffectFlag == 4) {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(600制限)");
		} else {
			player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF");
		}

		player.sendMessage(ChatColor.GREEN + "再度 /ef コマンドを実行することでトグルします。");

		//切り替えたフラグを反映
		playerData.effectflag = newEffectFlag;
	}

	// TODO これはここにあるべきではない
	private static void toggleMessageFlag(PlayerData playerData) {
		boolean newMessageFlag = !playerData.messageflag;
		Player player = Bukkit.getPlayer(playerData.uuid);

		if (newMessageFlag){
			player.sendMessage(ChatColor.GREEN + "内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)");
		}else{
			player.sendMessage(ChatColor.GREEN + "内訳表示:OFF");
		}

		playerData.messageflag = newMessageFlag;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//プレイヤーからの送信でない時処理終了
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}

		PlayerData senderPlayerData = SeichiAssist.playermap.get(((Player) sender).getUniqueId());

		if (args.length < 1) {
			toggleEffect(senderPlayerData);
			return true;
		}

		if (args[0].equalsIgnoreCase("smart")) {
			toggleMessageFlag(senderPlayerData);
			return true;
		}

		return false;
	}
}
