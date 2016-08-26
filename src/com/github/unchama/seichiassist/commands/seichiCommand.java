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

		}else if(args[0].equalsIgnoreCase("bug")){
			//seichi bug と入力したとき
			if(args.length != 2){
				//引数が２でない時の処理
				sender.sendMessage("/seichi bug 2 で全ての登録されているプレイヤーに詫び券(ガチャ券）を2枚配布します。");
				return true;
			}
			//全てのプレイヤーに詫び券を設定
			addSorryForBug(sender,Util.toInt(args[1]));
			return true;

		}else if(args.length == 3 || args.length == 4){
			//seichi player duration(ticks) amplifier id で登録できるようにする。
			//プレイヤー名を取得
			String name = Util.getName(args[0]);
			//プレイヤーをサーバーから取得
			Player player = plugin.getServer().getPlayer(name);

			if(!name.equalsIgnoreCase("all")){
				//プレイヤー名がallでない時の処理

				//持続時間を取得
				int duration = Util.toInt(args[1]);
				//effect値を取得
				double amplifier = Util.toDouble(args[2]);
				//メッセージを設定
				int id = 0;

				if(player == null){
					//プレイヤーが取得できなかったとき
					sender.sendMessage("指定されたプレイヤーは一度も鯖に接続していないか存在しません。");
					sender.sendMessage("/seichi unchama 1200 10.0 のように、player名と持続時間（ticks:１秒＝20tick)、上昇値(小数点以下ok)を入力してください。");
					return true;
				}

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

				//プレイヤーデータを取得
				PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
				//エフェクトデータリストにこの効果を追加
				playerdata.effectdatalist.add(new EffectData(duration,amplifier,id));
				//メッセージ送信
				sender.sendMessage(ChatColor.LIGHT_PURPLE + name + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました");
			}else{
				//player名がallだった時の処理


				//持続時間を取得
				int duration = Util.toInt(args[1]);
				//effect値を取得
				double amplifier = Util.toDouble(args[2]);
				//メッセージを格納
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
	private void addSorryForBug(CommandSender sender,int num) {
		for(PlayerData playerdata : SeichiAssist.playermap.values()){
			playerdata.numofsorryforbug += num;
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + num +"個のガチャ券をお詫びとして" + playerdata.name + "のデータに更新しました");
		}
		//MySqlの値も処理
		if(!sql.addAllPlayerBug(num)){
			sender.sendMessage("mysqlに保存されている全プレイヤーへの詫びガチャの加算に失敗しました");
		}else{
			sender.sendMessage("mysqlに保存されている全プレイヤーへ詫びガチャを加算しました");
		}
	}
}
