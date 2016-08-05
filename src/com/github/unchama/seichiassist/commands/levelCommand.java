package com.github.unchama.seichiassist.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class levelCommand implements TabExecutor{
	public SeichiAssist plugin;


	public levelCommand(SeichiAssist plugin){
		this.plugin = plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}
	// /gacha set 0.01 (現在手にもってるアイテムが確率0.01でガチャに出現するように設定）
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
	String label, String[] args) {


		if(args[0].equalsIgnoreCase("reset")){
			//コマンドがlevel reset だった時の処理

			//level reset より多い引数を指定した場合
			if(args.length != 1){
				sender.sendMessage("/level resetで全員のレベル計算をリセットし、レベルアップを再度可能にします");
				return true;
			}
			//すべてのプレイヤーデータについて処理
			for(PlayerData playerdata:SeichiAssist.playermap.values()){
				//整地レベルを1に設定
				playerdata.setLevel(1);
				//メッセージ送信
				sender.sendMessage(playerdata.name+"のレベルを" + playerdata.level + "に設定しました");
				//プレイヤーがオンラインの時表示名を変更
				if(!playerdata.isOffline()){
					Player player = SeichiAssist.plugin.getServer().getPlayer(playerdata.name);
					playerdata.setDisplayName(player);
				}
			}
			return true;
		}
		return false;
	}



}
