package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class effectCommand implements TabExecutor {
	SeichiAssist plugin;

	public effectCommand(SeichiAssist _plugin){
		plugin = _plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1,
			String arg2, String[] arg3) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		//プレイヤーからの送信でない時処理終了
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			//コマンド長が０の時の処理

			//プレイヤーを取得
			Player player = (Player)sender;
			//UUIDを取得
			UUID uuid = player.getUniqueId();
			//playerdataを取得
			PlayerData playerdata = SeichiAssist.playermap.get(uuid);
			//エフェクトフラグを反転
			boolean effectflag = !playerdata.effectflag;
			if (effectflag){
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
			}else{
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF(ONに戻したい時は再度コマンドを実行します。)");
			}
			//反転したフラグで更新
			playerdata.effectflag = effectflag;
			return true;
		}else if(args.length == 1){
			//コマンド長が１の時
			if(args[0].equalsIgnoreCase("smart")){
				//コマンドがef smartの時の処理

				//プレイヤーを取得
				Player player = (Player)sender;
				//UUIDを取得
				UUID uuid = player.getUniqueId();
				//playerdataを取得
				PlayerData playerdata = SeichiAssist.playermap.get(uuid);
				//メッセージフラグを反転
				boolean messageflag = !playerdata.messageflag;
				if (messageflag){
					sender.sendMessage(ChatColor.GREEN + "内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)");
				}else{
					sender.sendMessage(ChatColor.GREEN + "内訳表示:OFF");
				}
				//反転したフラグで更新
				playerdata.messageflag = messageflag;
				return true;
			}
		}
		return false;
	}

}
