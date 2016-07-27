package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.EffectData;
import com.github.unchama.seichiassist.data.PlayerData;

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
		if(args.length == 0){
			SeichiAssist.gachadatalist.clear();
			Config.reloadConfig();
			sender.sendMessage("SeichiAssistのconfig.ymlをリロードしました。");
			return true;
		}else if(args.length > 0){
			//seichi player duration(ticks) amplifier で登録できるようにする。
			if(args.length != 3 && args.length != 4){
				sender.sendMessage("/seichi unchama 1200 10.0 のように、player名と持続時間（ticks:１秒＝20tick)、上昇値(小数点以下ok)を入力してください。");
				return true;
			}
			String pname = args[0].toLowerCase();
			if(!pname.equalsIgnoreCase("all")){
				if(!SeichiAssist.playermap.containsKey(pname)){
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
						message  = "ドラゲナイタイム（対象："+ pname +"）からの上昇値:" + amplifier;
					}
				}else{
					//引数が３つの場合
					message = "外部（対象："+ pname +"）からの上昇値:" + amplifier;

				}
				sender.sendMessage(pname + "に上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
				PlayerData playerdata = SeichiAssist.playermap.get(pname);
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
				for(String pn : SeichiAssist.playermap.keySet()){
					PlayerData playerdata = SeichiAssist.playermap.get(pn);
					playerdata.effectdatalist.add(new EffectData(duration,amplifier,message));
				}
				sender.sendMessage("全てのプレイヤーに上昇値"+amplifier+"を" + Util.toTimeString(duration/20) + "追加しました。");
			}
			return true;
		}
		return false;
	}
}
