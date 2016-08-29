package com.github.unchama.seichiassist.commands;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class seichiCommand implements TabExecutor {
	SeichiAssist plugin;
	Sql sql = SeichiAssist.plugin.sql;

	public seichiCommand(SeichiAssist _plugin){
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
		if(args.length == 0){
			return false;

		}else if(args[0].equalsIgnoreCase("help")){

			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
			sender.sendMessage(ChatColor.RED + "/seichi reload");
			sender.sendMessage("config.ymlの設定値を再読み込みします");
			sender.sendMessage(ChatColor.RED + "/seichi debugmode");
			sender.sendMessage("デバッグモードのON,OFFを切り替えます");
			sender.sendMessage(ChatColor.RED + "/seichi <playername/all> <duration(tick)> <amplifier(double)> <id>");
			sender.sendMessage("指定されたプレイヤーに採掘速度上昇効果を付与します\nall指定で全プレイヤー対象");
			sender.sendMessage("idを指定すると上昇値に説明文を付加出来ます。指定なしだと5が入ります");
			sender.sendMessage("id=0 不明な上昇値\nid=1 接続人数から\nid=2 採掘量から\nid=3 ドラゲナイタイムから\nid=4 投票から\nid=5 コマンド入力から(イベントや不具合等)");
			/*
			 * id=0 不明な上昇値
			 * id=1 接続人数から
			 * id=2 採掘量から
			 * id=3 ドラゲナイタイムから
			 * id=4 投票から
			 * id=5 コマンド入力から(イベントや不具合等)
			 */
			return true;

		}else if(args[0].equalsIgnoreCase("reload")){
			//gacha reload
			SeichiAssist.config.reloadConfig();
			sender.sendMessage("config.ymlの設定値を再読み込みしました");
			return true;
		}else if(args[0].equalsIgnoreCase("debugmode")){
			//debugフラグ反転処理

			//メッセージフラグを反転
			SeichiAssist.DEBUG = !SeichiAssist.DEBUG;
			if (SeichiAssist.DEBUG){
				sender.sendMessage(ChatColor.GREEN + "デバッグモードを有効にしました");
			}else{
				sender.sendMessage(ChatColor.GREEN + "デバッグモードを無効にしました");
			}
			plugin.stopAllTaskRunnable();
			plugin.startTaskRunnable();

			return true;

		}else if(args.length == 3 || args.length == 4){
			//seichi player duration(ticks) amplifier id で登録できるようにする。
			//プレイヤー名を取得
			String name = Util.getName(args[0]);
			//プレイヤーをサーバーから取得
			Player player = plugin.getServer().getPlayer(name);

			//メッセージを設定
			int id = 0;
			if(args.length == 4){
				//引数が４つの場合
				//numを取得
				int num = Util.toInt(args[3]);
				if(num == 0){
					id = 0;
				}else if(num == 1){
					id = 1;
				}else if(num == 2){
					id = 2;
				}else if(num == 3){
					id = 3;
				}else if(num == 4){
					id = 4;
				}else if(num == 5){
					id = 5;
				}else{
					id = 5;
					sender.sendMessage("不明なidが指定されているので、プレイヤーへの説明文には\n「コマンド入力による上昇値」と表示されます");
				}
			}else{
				//引数が3つの場合
				id = 5;
				sender.sendMessage("idが指定されていないので、プレイヤーへの説明文には\n「コマンド入力による上昇値」と表示されます");
			}
			//持続時間を取得
			int duration = Util.toInt(args[1]);
			//effect値を取得
			double amplifier = Util.toDouble(args[2]);

			if(!name.equalsIgnoreCase("all")){
				//プレイヤー名がallでない時の処理

				if(player == null){
					//プレイヤーが取得できなかったとき
					sender.sendMessage("指定されたプレイヤーはオンラインでは無いか、存在しません");
					return true;
				}

				//プレイヤーデータを取得
				PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
				//エフェクトデータリストにこの効果を追加
				playerdata.effectdatalist.add(new EffectData(duration,amplifier,id));
				//メッセージ送信
				sender.sendMessage(ChatColor.LIGHT_PURPLE + name + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました");
			}else{
				//player名がallだった時の処理

				//全てのプレイヤーデータについて処理
				for(PlayerData playerdata: SeichiAssist.playermap.values()){
					//エフェクトデータリストにこの効果を追加
					playerdata.effectdatalist.add(new EffectData(duration,amplifier,id));
				}
				//メッセージ送信
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "全てのプレイヤーに上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました");
			}
			return true;
		}
		return false;
	}
}
