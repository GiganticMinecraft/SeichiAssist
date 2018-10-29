package com.github.unchama.seichiassist.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
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
		//プレイヤーを取得
		Player player = (Player)sender;
		//プレイヤーネーム
		//String name = Util.getName(player);
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//プレイヤーからの送信でない時処理終了
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		}else if(args.length == 0){
			//エフェクトフラグを反転
			int effectflag = (playerdata.effectflag + 1) % 6;
			if (effectflag == 0) {
                sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(無制限)");
            } else if (effectflag == 1) {
			    sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(127制限)");
			} else if (effectflag == 2) {
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(200制限)");
			} else if (effectflag == 3) {
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(400制限)");
			} else if (effectflag == 4) {
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON(600制限)");
			} else {
				sender.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF");
			}
			sender.sendMessage(ChatColor.GREEN + "再度コマンドを実行することでトグルします。");
			//反転したフラグで更新
			playerdata.effectflag = effectflag;
			return true;
		}else if(args.length == 1){
			//コマンド長が１の時
			if(args[0].equalsIgnoreCase("smart")){
				//コマンドがef smartの時の処理

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


			}else if (args[0].equalsIgnoreCase("demo")){
				//ガチャ券を1000回試行してみる処理
				int i = 0;
				double p = 0.0;
				int gigantic = 0;
				int big = 0;
				int regular = 0;
				int potato = 0;
				while(1000 > i){
					p = runGachaDemo();
					if(p < 0.001){
						gigantic ++;
					}else if(p < 0.01){
						big ++;
					}else if(p < 0.1){
						regular ++;
					}else{
						potato ++;
					}
					i++;
				}
				sender.sendMessage(
						ChatColor.AQUA + "" + ChatColor.BOLD + "ガチャ券" + i + "回試行結果\n"
						+ ChatColor.RESET + "ギガンティック："+ gigantic +"回("+ ((double)gigantic/(double)i*100.0) +"%)\n"
						+ "大当たり："+ big +"回("+ ((double)big/(double)i*100.0) +"%)\n"
						+ "当たり："+ regular +"回("+ ((double)regular/(double)i*100.0) +"%)\n"
						+ "ハズレ："+ potato +"回("+ ((double)potato/(double)i*100.0) +"%)\n"
						);
				return true;
			}
		}
		return false;
	}
	private double runGachaDemo() {
		double sum = 1.0;
		double rand = 0.0;

		rand = Math.random();

		for (GachaData gachadata : SeichiAssist.gachadatalist) {
		    sum -= gachadata.probability;
		    if (sum <= rand) {
                return gachadata.probability;
            }
		}
		return 1.0;
	}
}
