package com.github.unchama.seichiassist.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;

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
			SeichiAssist.gachadatalist.clear();
			SeichiAssist.config.reloadConfig();
			sender.sendMessage("SeichiAssistのconfig.ymlをリロードしました。");
			return true;
		}else if(args[0].equals("bug")){
			if(args.length != 2){
				sender.sendMessage("/seichi bug 2 で全ての登録されているプレイヤーに詫び券(ガチャ券）を2枚配布します。");
			}
			addSorryForBug(sender,Util.toInt(args[1]));
			return true;
		}else if(args.length > 0){
			//seichi player duration(ticks) amplifier で登録できるようにする。
			if(args.length != 3 && args.length != 4){
				sender.sendMessage("/seichi unchama 1200 10.0 のように、player名と持続時間（ticks:１秒＝20tick)、上昇値(小数点以下ok)を入力してください。");
				return true;
			}
			String name = Util.getName(args[0]);
			Player player = plugin.getServer().getPlayer(name);
			if(player == null){
				sender.sendMessage("指定されたプレイヤーは一度も鯖に接続していないか存在しません。");
				return true;
			}
			if(!name.equalsIgnoreCase("all")){
				if(!sql.isExists(name)){
					sender.sendMessage("指定されたプレイヤーは一度も鯖に接続していません。");
					return true;
				}
				int duration = Util.toInt(args[1]);
				double amplifier = Util.toDouble(args[2]);
				String message = null;

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
				sender.sendMessage(name + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
				PlayerData playerdata = SeichiAssist.playermap.get(name);
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
				sender.sendMessage("全てのプレイヤーに上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
			}
			return true;
		}
		return false;
	}
	private void addSorryForBug(CommandSender sender,int num) {
		ResultSet rs = sql.getTable();
		if(rs == null){
			Util.sendEveryMessage("テーブル取得に失敗しました。");
			return ;
		}
		try {
			while (rs.next()){
				String name = rs.getString("name");
				int numofsorryforbug = sql.selectint(name, "numofsorryforbug");
				numofsorryforbug += num;
				sql.insert("numofsorryforbug", numofsorryforbug, name);
				sender.sendMessage(num+"個のガチャ券をお詫びとして" + name + "のデータに更新しました");
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Util.sendEveryMessage("ガチャ券配布の計算に失敗しました。");
			return;
		}
	}
}
