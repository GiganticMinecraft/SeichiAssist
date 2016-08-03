package com.github.unchama.seichiassist.commands;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;
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
		if(args[0].equalsIgnoreCase("reload")){
			SeichiAssist.gachadatalist.clear();
			SeichiAssist.config.reloadConfig();
			sender.sendMessage("SeichiAssistのconfig.ymlをリロードしました。");
			return true;
		}else if(args[0].equalsIgnoreCase("bug")){
			if(args.length != 2){
				sender.sendMessage("/seichi bug 2 で全ての登録されているプレイヤーに詫び券(ガチャ券）を2枚配布します。");
			}
			addSorryForBug(sender,Util.toInt(args[1]));
			return true;
		}else if(args[0].equalsIgnoreCase("test")){
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

		}else if(args.length == 3 || args.length == 4){
			//seichi player duration(ticks) amplifier で登録できるようにする。

			String name = Util.getName(args[0]);
			Player player = plugin.getServer().getPlayer(name);

			if(!name.equalsIgnoreCase("all")){
				int duration = Util.toInt(args[1]);
				double amplifier = Util.toDouble(args[2]);
				String message = null;
				if(player == null){
					sender.sendMessage("指定されたプレイヤーは一度も鯖に接続していないか存在しません。");
					sender.sendMessage("/seichi unchama 1200 10.0 のように、player名と持続時間（ticks:１秒＝20tick)、上昇値(小数点以下ok)を入力してください。");
					return true;
				}
				if(args.length == 4){
					//引数が４つの場合
					int num = Util.toInt(args[3]);
					if(num == 0){
						//投票の時のメッセージ
						message = "投票からの上昇値:" + amplifier;
					}else if(num == 1){
						//どらげないたいむの時のメッセージ
						message  = "ドラゲナイタイム（対象："+ name +"）からの上昇値:" + amplifier;
					}
				}else{
					//引数が３つの場合
					message = "外部（対象："+ name +"）からの上昇値:" + amplifier;

				}
				sender.sendMessage(ChatColor.LIGHT_PURPLE + name + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
				PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
				playerdata.effectdatalist.add(new EffectData(duration,amplifier,message));
			}else{
				int duration = Util.toInt(args[1]);
				double amplifier = Util.toDouble(args[2]);
				String message = null;

				if(args.length == 4){
					//引数が４つの場合
					int num = Util.toInt(args[3]);
					if(num == 0){
						sender.sendMessage("投票値を全員に付与することはできません。ドラゲナイタイムのフラグは1です。");
						return true;
					}else if(num == 1){
						//どらげないたいむの時のメッセージ
						message  = "ドラゲナイタイム（対象：全員）からの上昇値:" + amplifier;
					}
				}else{
					//引数が３つの場合
					message = "外部からの上昇値（対象：全員）:" + amplifier;
				}

				for(UUID uuid : SeichiAssist.playermap.keySet()){
					PlayerData playerdata = SeichiAssist.playermap.get(uuid);
					playerdata.effectdatalist.add(new EffectData(duration,amplifier,message));
				}
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "全てのプレイヤーに上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
			}
			return true;
		}
		return false;
	}
	private void addSorryForBug(CommandSender sender,int num) {
		List<String> namelist = sql.getNameList(SeichiAssist.PLAYERDATA_TABLENAME);
		for(String name : namelist){
			int numofsorryforbug = sql.selectint(SeichiAssist.PLAYERDATA_TABLENAME,name, "numofsorryforbug");
			numofsorryforbug += num;
			sql.insert(SeichiAssist.PLAYERDATA_TABLENAME,"numofsorryforbug", numofsorryforbug, name);
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + num +"個のガチャ券をお詫びとして" + name + "のデータに更新しました");
		}

	}
}
