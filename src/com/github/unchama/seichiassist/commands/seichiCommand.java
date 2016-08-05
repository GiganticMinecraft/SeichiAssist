package com.github.unchama.seichiassist.commands;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class seichiCommand implements TabExecutor {
	SeichiAssist plugin;

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

		if(args[0].equalsIgnoreCase("load")){
			//seichi load と入力したとき
			SeichiAssist.gachadatalist.clear();
			SeichiAssist.config.reloadConfig();
			sender.sendMessage("SeichiAssistのconfig.ymlを強制的にロードしました。");
			return true;


		}else if(args[0].equalsIgnoreCase("bug")){
			//seichi bug と入力したとき
			if(args.length != 2){
				//引数が２でない時の処理
				sender.sendMessage("/seichi bug 2 で全ての登録されているプレイヤーに詫び券(ガチャ券）を2枚配布します。");
			}
			//全てのプレイヤーに詫び券を設定
			addSorryForBug(sender,Util.toInt(args[1]));
			return true;


		/*}else if(args[0].equalsIgnoreCase("test")){
			//DEBUG用コマンド
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください。");
				return true;
			}
			Player player = (Player) sender;
			String name = Util.getName(player);
			if(args[1].equalsIgnoreCase("set")){
				PlayerInventory pinventory = player.getInventory();
				Inventory inventory = SeichiAssist.plugin.getServer().createInventory(null, 9 * 3, "拡張インベントリ");
				for(int i = 9,k = 0; i<36 ; i++,k++){
					inventory.setItem(k, pinventory.getItem(i));
				}
				String string = BukkitSerialization.toBase64(inventory);
				sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"inventory",string, name);
			}else if(args[1].equalsIgnoreCase("get")){
				String string = sql.selectstring(SeichiAssist.PLAYERDATA_TABLENAME, name, "inventory");
				Inventory inventory;
				try {
					 inventory = BukkitSerialization.fromBase64(string);
					 player.openInventory(inventory);
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}
			return true;
*/
		}else if(args.length == 3 || args.length == 4){
			//seichi player duration(ticks) amplifier で登録できるようにする。
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
				String message = null;

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
						//numが０の時
						//投票の時のメッセージ
						message = "投票からの上昇値:" + amplifier;
					}else if(num == 1){
						//numが１の時
						//どらげないたいむの時のメッセージ
						message  = "ドラゲナイタイム（対象："+ name +"）からの上昇値:" + amplifier;
					}
				}else{
					//引数が３つの場合
					message = "外部（対象："+ name +"）からの上昇値:" + amplifier;

				}
				//メッセージ送信
				sender.sendMessage(ChatColor.LIGHT_PURPLE + name + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
				//プレイヤーデータを取得
				PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
				//エフェクトデータリストにこの効果を追加
				playerdata.effectdatalist.add(new EffectData(duration,amplifier,message));
			}else{
				//player名がallだった時の処理


				//持続時間を取得
				int duration = Util.toInt(args[1]);
				//effect値を取得
				double amplifier = Util.toDouble(args[2]);
				//メッセージを格納
				String message = null;

				if(args.length == 4){
					//引数が４つの場合
					//numを取得
					int num = Util.toInt(args[3]);
					if(num == 0){
						//numが０の時
						sender.sendMessage("投票値を全員に付与することはできません。ドラゲナイタイムのフラグは1です。");
						return true;
					}else if(num == 1){
						//numが１の時
						//どらげないたいむの時のメッセージ
						message  = "ドラゲナイタイム（対象：全員）からの上昇値:" + amplifier;
					}
				}else{
					//引数が３つの場合
					message = "外部からの上昇値（対象：全員）:" + amplifier;
				}

				//全てのプレイヤーデータについて処理
				for(PlayerData playerdata: SeichiAssist.playermap.values()){
					//エフェクトデータリストにこの効果を追加
					playerdata.effectdatalist.add(new EffectData(duration,amplifier,message));
				}
				//メッセージ送信
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "全てのプレイヤーに上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
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


	}
}
