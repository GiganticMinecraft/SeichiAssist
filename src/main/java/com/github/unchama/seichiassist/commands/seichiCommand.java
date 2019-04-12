package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

import net.md_5.bungee.api.ChatColor;

public class seichiCommand implements TabExecutor {
	SeichiAssist plugin;
	Sql sql = SeichiAssist.sql;

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
			sender.sendMessage("config.ymlのdebugmodeの値が1の場合のみ、コンソールから使用可能");
			sender.sendMessage(ChatColor.RED + "/seichi <playername/all> <duration(tick)> <amplifier(double)> <id>");
			sender.sendMessage("指定されたプレイヤーに採掘速度上昇効果を付与します");
			sender.sendMessage("all指定で全プレイヤー対象");
			sender.sendMessage("同じ鯖にログイン中の人にしか適用されません");
			sender.sendMessage("idを指定すると上昇値に説明文を付加出来ます。指定なしだと5が入ります");
			sender.sendMessage("id=0 不明な上昇値");
			sender.sendMessage("id=1 接続人数から");
			sender.sendMessage("id=2 採掘量から");
			sender.sendMessage("id=3 ドラゲナイタイムから");
			sender.sendMessage("id=4 投票から");
			sender.sendMessage("id=5 コマンド入力から(イベントや不具合等)");
			sender.sendMessage(ChatColor.RED + "/seichi openpocket <プレイヤー名>");
			sender.sendMessage("対象プレイヤーの四次元ポケットを開く");
			sender.sendMessage("編集結果はオンラインのプレイヤーにのみ反映されます");
			sender.sendMessage(ChatColor.RED + "/seichi anniversary");
			sender.sendMessage("1周年記念フラグを立てる（コンソール限定コマンド）");

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
			if(SeichiAssist.config.getDebugMode()==1){
				//メッセージフラグを反転
				SeichiAssist.DEBUG = !SeichiAssist.DEBUG;
				if (SeichiAssist.DEBUG){
					sender.sendMessage(ChatColor.GREEN + "デバッグモードを有効にしました");
				}else{
					sender.sendMessage(ChatColor.GREEN + "デバッグモードを無効にしました");
				}
				plugin.stopAllTaskRunnable();
				plugin.startTaskRunnable();
			} else {
				sender.sendMessage(ChatColor.RED + "このコマンドは現在の設定では実行できません");
				sender.sendMessage(ChatColor.RED + "config.ymlのdebugmodeの値を1に書き換えて再起動またはreloadしてください");
			}

			return true;
		}else if(args[0].equalsIgnoreCase("openpocket")){
			//seichi openpocket <playername>
			if(args.length != 2){
				//引数が2じゃない時の処理
				sender.sendMessage(ChatColor.RED + "/seichi openpocket <プレイヤー名>");
				sender.sendMessage("対象プレイヤーの四次元ポケットを開く");
				sender.sendMessage("編集結果はオンラインのプレイヤーにのみ反映されます");
				return true;
			}else{
				//引数が2の時の処理

				/*
				 * コンソールからのコマンドは処理しない - ここから
				 */
				if (!(sender instanceof Player)) {
					sender.sendMessage("このコマンドはゲーム内から実行してください");
					return true;
				}
				Player player = (Player) sender;
				/*
				 * ここまで
				 */

				//対象プレイヤー名を取得
				String name = Util.getName(args[1]);
				//対象プレイヤーをサーバーから取得
				Player targetplayer = plugin.getServer().getPlayer(name);
				if(targetplayer != null){
					//対象プレイヤーがオンラインの時の処理
					//対象プレイヤーのuuid取得
					UUID uuid = targetplayer.getUniqueId();
					//対象プレイヤーのplayerdata取得
					PlayerData targetplayerdata = SeichiAssist.playermap.get(uuid);
					//playerdataが取得できなかった場合処理終了
					if(targetplayerdata == null){
						sender.sendMessage(name + "はオンラインですが、何故かplayerdataが見つかりませんでした(要報告)");
						return true;
					}
					player.openInventory(targetplayerdata.inventory);
					return true;
				}else{
					//対象プレイヤーがオフラインの時の処理
					sender.sendMessage(ChatColor.RED + "対象プレイヤーはオフラインです。編集結果は反映されません");
					//プレイヤーがオフラインの時の処理
					@SuppressWarnings("deprecation")
					UUID uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
					//mysqlからinventory持ってくる
					Inventory inventory = sql.selectInventory(uuid);
					//inventoryが取得できなかった場合処理終了
					if(inventory == null){
						sender.sendMessage("mysqlからインベントリを取得できませんでした");
						return true;
					}
					player.openInventory(inventory);
					return true;
				}
			}


		}else if(args.length == 3 || args.length == 4){
			//seichi player duration(ticks) amplifier id で登録できるようにする。

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

			//プレイヤー名をlowercaseする
			String name = Util.getName(args[0]);

			if(!name.equalsIgnoreCase("all")){
				//プレイヤー名がallでない時の処理

				//プレイヤーをサーバーから取得
				Player player = plugin.getServer().getPlayer(name);

				if(player == null){
					//プレイヤーが取得できなかったとき
					sender.sendMessage("指定されたプレイヤー(" + name + ")はオンラインでは無いか、存在しません");
					return true;
				}

				//プレイヤーデータを取得
				PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
				//念のためエラー分岐
				if(playerdata == null){
					player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[seichiコマンドエフェクト付与処理]でエラー発生");
					plugin.getLogger().warning("playerdataがありません。開発者に報告してください");
					return true;
				}
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

		// seichi anniversary
		else if (args[0].equalsIgnoreCase("anniversary")) {
			if (sender instanceof Player) {
				sender.sendMessage("コンソール専用コマンドです");
				return true;
			}
			// SQLの全登録データをtrueに変更
			sql.setAnniversary(true, null);
			sender.sendMessage("Anniversaryアイテムの配布を開始しました。");
			return true;
		}
		return false;
	}
}
