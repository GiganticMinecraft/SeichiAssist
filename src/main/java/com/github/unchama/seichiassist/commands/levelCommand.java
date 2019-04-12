package com.github.unchama.seichiassist.commands;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.OfflineUUID;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class levelCommand implements TabExecutor{
	public SeichiAssist plugin;
	Sql sql = SeichiAssist.sql;


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

		if(args.length == 0){
			return false;

		}else if(args[0].equalsIgnoreCase("help")){

			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
			sender.sendMessage(ChatColor.RED + "/level reset");
			sender.sendMessage("全員のレベル計算をリセットし、レベルアップを再度可能にします。");
			sender.sendMessage("MySQLのレベルも初期化されます。経験値テーブルが変更された場合に使用します");

			sender.sendMessage(ChatColor.RED + "/level set <プレイヤー名> <レベル>");
			sender.sendMessage("指定されたプレイヤーのレベルを変更して整地量も変更します(デバッグモード時のみ使用可能)");

			return true;

		}else if(args[0].equalsIgnoreCase("reset")){
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
			//MySqlの値も処理
			if(!sql.resetAllPlayerLevel()){
				sender.sendMessage("mysqlのレベルの初期化に失敗しました");
			}else{
				sender.sendMessage("mysqlに保存されている全プレイヤーのレベルを初期化しました");
			}
			return true;
		}else if(args[0].equalsIgnoreCase("set")){
			if(SeichiAssist.DEBUG){
				if(args.length != 3){
					sender.sendMessage("/level set unchama 1 のように、変更したいプレイヤーとレベルを入力してください");
					return true;
				}
				int num = Util.toInt(args[2]);

				//すべてのプレイヤーデータについて処理
				//for(PlayerData playerdata:SeichiAssist.playermap.values()){
			    	OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
			    		UUID uuid = null;
			    		if(op!=null){
			    			uuid = op.getUniqueId();
			    		} else {
			    			try {
			    				uuid = OfflineUUID.getUUID(args[1], true);
			    			} catch (SocketTimeoutException e) {
			    				// TODO 自動生成された catch ブロック
			    				//e.printStackTrace();
			    			} catch (IllegalArgumentException e) {
			    				// TODO 自動生成された catch ブロック
			    				//e.printStackTrace();
			    			} catch (IOException e) {
			    				// TODO 自動生成された catch ブロック
			    				//e.printStackTrace();
			    			}
			    		}
						PlayerData playerdata = null;
						if(uuid!=null){
							playerdata = SeichiAssist.playermap.get(uuid); //get(UUID);
							//System.out.println("UUID!=null");

							//sender.sendMessage(SeichiAssist.playermap.toString());
							//sender.sendMessage(uuid.toString());
							/*
							if(playerdata!=null){
								System.out.println("playerdata!=null");
							} else {
								System.out.println("playerdata==null");
							}
							*/
						} else {
							//System.out.println("UUID==null");
						}
						if(playerdata!=null){
							 if (op.hasPlayedBefore()) {
								 //整地レベルを1に設定
								 //playerdata.setLevel(num);
								 if(num>=1 && num<=200){
									playerdata.setLevelandTotalbreaknum(num);
									//アクティブスキルポイントのリセット処理
									playerdata.activeskilldata.reset();
									//playerdata.updataLevel((Player)op);
									//メッセージ送信
									sender.sendMessage(playerdata.name+"のレベルを" + playerdata.level + "に設定しました");
									sender.sendMessage(playerdata.name+"の整地量を" + playerdata.totalbreaknum + "に設定しました");
									//プレイヤーがオンラインの時表示名を変更
									if(!playerdata.isOffline()){
										Player player = SeichiAssist.plugin.getServer().getPlayer(playerdata.name);
										playerdata.setDisplayName(player);
									}
									//}
									//MySqlの値も処理
									if(!sql.resetPlayerLevelandBreaknum(uuid)){
									sender.sendMessage("mysqlのレベルと整地量の設定に失敗しました");
									}else{
										sender.sendMessage("mysqlに保存されている指定したプレイヤーのレベルと整地量を設定しました");
									}
								} else {
									sender.sendMessage("指定したレベルに設定できません");
								}
							 } else {
								 sender.sendMessage("指定されたプレイヤーは存在しますがプレイ履歴がありません");
							 }
						} else {
							 if (op.hasPlayedBefore()) {
								 //整地レベルを1に設定
								 //playerdata.setLevel(num);
								 if(num>=1 && num<=200){
									//playerdata.setLevelandTotalbreaknum(num);
									//メッセージ送信
									//sender.sendMessage(playerdata.name+"のレベルを" + playerdata.level + "に設定しました");
									//sender.sendMessage(playerdata.name+"の整地量を" + playerdata.totalbreaknum + "に設定しました");
									//プレイヤーがオンラインの時表示名を変更
									//if(!playerdata.isOffline()){
									//	Player player = SeichiAssist.plugin.getServer().getPlayer(playerdata.name);
									//	playerdata.setDisplayName(player);
									//}
									//}
									//MySqlの値も処理
									if(!sql.resetPlayerLevelandBreaknum(uuid, num)){
									sender.sendMessage("mysqlのレベルと整地量の設定に失敗しました");
									}else{
										sender.sendMessage("mysqlに保存されている指定したプレイヤーのレベルと整地量を設定しました");
									}
								} else {
									sender.sendMessage("指定したレベルに設定できません");
								}
							 } else {
								 sender.sendMessage("指定されたプレイヤーはプレイ履歴がありません");
							 }
				    	}
			} else {
				sender.sendMessage("このコマンドはデバッグモード時のみ使用可能です");
			}

			return true;
		}
		return false;
	}



}
