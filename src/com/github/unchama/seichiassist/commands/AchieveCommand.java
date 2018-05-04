package com.github.unchama.seichiassist.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;

public class AchieveCommand implements TabExecutor{
	public SeichiAssist plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;



	public AchieveCommand(SeichiAssist plugin){
		this.plugin = plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return null;
	}

	public boolean isInt(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
	String label, String[] args) {

		Sql sql = SeichiAssist.sql;

		//final String table = SeichiAssist.PLAYERDATA_TABLENAME;

		//String sqlname;
		//Player sqlp;
		//final UUID sqluuid;
		//String sqlcommand;
		//int sqlresult;
		//String sqlexc;
		//Boolean sqlflag;
		//int sqli;
		//Statement sqlstmt = null;
		//ResultSet sqlrs = null;
		//String db;

		//db = SeichiAssist.config.getDB();
		//sqlcommand = "";
		//sqlresult = 0 ;
		//sqlflag = true;
		//sqli = 0;


		//プレイヤーを取得
		Player sendplayer = (Player)sender;

		//プレイヤーからの送信でない時処理終了
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GREEN + "このコマンドはゲーム内から実行してください。");
			return true;
		//不正な数の引数を指定した場合(2より小さい場合 or 3より大きい場合 →lengthが2～3以外の場合)
		}else if(2 > args.length ||args.length > 3){
			sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");
			sender.sendMessage("【HINT】<プレイヤー名>を「ALL」にし、<give/deprive>の代わりに<server/world>を入力すると");
			sender.sendMessage("実行者が参加しているサーバー/ワールド内の全員に対して実績解除処理を実行します。");
			return true;
		//<実績No>が数字でない場合スキップ
		}else if(isInt(args[0])){
			if(6999 < Integer.parseInt(args[0]) && Integer.parseInt(args[0]) < 8000){
				//指定した実績Noの二つ名データがconfigにない場合
				if((SeichiAssist.config.getTitle1(Integer.parseInt(args[0])) == null||SeichiAssist.config.getTitle1(Integer.parseInt(args[0])) == "")&&
					(SeichiAssist.config.getTitle2(Integer.parseInt(args[0])) == null||SeichiAssist.config.getTitle2(Integer.parseInt(args[0])) == "")&&
					(SeichiAssist.config.getTitle3(Integer.parseInt(args[0])) == null||SeichiAssist.config.getTitle3(Integer.parseInt(args[0])) == "")){
					sender.sendMessage("【実行エラー】存在しない実績Noが指定されました");
					return true;
				}else {
					//念のためnull分岐
					if(args[1] != null){
						if(args[1].equals("ALL") || args[1].equals("all")){
						//<プレイヤー名>が"ALL"の場合
							//「server」全体配布処理
							try{
								if(args[2].equals("server")){
									for(Player p :Bukkit.getServer().getOnlinePlayers()){
										player = p;
										UUID uuid = p.getUniqueId();
										playerdata = playermap.get(uuid);

										//該当実績を既に取得している場合処理をスキップ
										if(!playerdata.TitleFlags.get(Integer.parseInt(args[0]))){
											playerdata.TitleFlags.set(Integer.parseInt(args[0]));
											player.sendMessage("運営チームよりNo" + args[0] + "の実績が配布されました。");
										}
									}
									sender.sendMessage("【配布完了】No" + args[0] +"の実績をサーバー内全員に配布しました。");
									return true;
								}
								//「world」全員配布処理
								else if(args[2].equals("world")){
									for(Player p :Bukkit.getServer().getOnlinePlayers()){
										player = p;
										UUID uuid = p.getUniqueId();
										playerdata = playermap.get(uuid);

										//送信者と同じワールドにいれば配布
										if(p.getWorld().getName() == sendplayer.getWorld().getName() ){
											//該当実績を既に取得している場合処理をスキップ
											if(!playerdata.TitleFlags.get(Integer.parseInt(args[0]))){
												playerdata.TitleFlags.set(Integer.parseInt(args[0]));
												player.sendMessage("運営チームよりNo" + args[0] + "の実績が配布されました。");
											}
										}
									}
									sender.sendMessage("【配布完了】No" + args[0] +"の実績をサーバー内全員に配布しました。");
									return true;

								}else if(args[2].equals("user")){
									//ユーザー「ALL」がいた場合専用の処理
									//相手がオンラインかどうか
									Player givenplayer = Bukkit.getServer().getPlayer(args[1]);
							        if (givenplayer == null) {
							            sender.sendMessage(args[1] + " は現在このサーバーにログインしていません。");
							            return true;
							        }
									UUID givenuuid = givenplayer.getUniqueId();
									PlayerData givenplayerdata = playermap.get(givenuuid);
									//該当実績を既に取得している場合処理をスキップ
									if(!givenplayerdata.TitleFlags.get(Integer.parseInt(args[0]))){
										givenplayerdata.TitleFlags.set(Integer.parseInt(args[0]));
										givenplayer.sendMessage("運営チームよりNo" + args[0] + "の実績が配布されました。");
										sender.sendMessage("【配布完了】No" + args[0] +"の実績を配布しました。");
									}else {
										sender.sendMessage("既に該当実績を獲得しています。");
									}
									return true;
								}else {
									//<server/world/user>ではない場合
									sender.sendMessage("全員配布を行いたい場合は、用途に応じてコマンドの最後に以下の記述を追加してください。");
									sender.sendMessage("サーバー全員に配布→「server」、ワールド全員に配布→「world」");
									sender.sendMessage("※もし「ALL」というユーザーが存在し、該当者のみに配布したい場合は最後に「user」と入力してください。");
									return true;
								}
							}
							catch(ArrayIndexOutOfBoundsException e){
								//arg[2]が空白の場合確実に発生するので分岐
								sender.sendMessage("全員配布を行いたい場合は、用途に応じてコマンドの最後に以下の記述を追加してください。");
								sender.sendMessage("サーバー全員に配布→「server」、ワールド全員に配布→「world」");
								sender.sendMessage("※もし「ALL」というユーザーが存在し、該当者のみに配布したい場合は最後に「user」と入力してください。");
								return true;
							}
						}else {
							//<プレイヤー名>が"ALL"以外の場合
							//相手がオンラインかどうか
							Player givenplayer = Bukkit.getServer().getPlayer(args[1]);
								if (givenplayer == null) {
									sender.sendMessage(args[1] + " は現在サーバにいないため、予約付与システムを利用します。");
								//sqlをusernameで操作
								if (sql.writegiveachvNo((Player) sender, args[1], args[0])) {
									sender.sendMessage(args[1] + "へ、実績No"+ args[0] + "の付与の予約が完了しました。");
								}
								return true;
					        }
							UUID givenuuid = givenplayer.getUniqueId();
							PlayerData givenplayerdata = playermap.get(givenuuid);
							try{
								if(args[2].equals("give")){
									//該当実績を既に取得している場合処理をスキップ
									if(!givenplayerdata.TitleFlags.get(Integer.parseInt(args[0]))){
										givenplayerdata.TitleFlags.set(Integer.parseInt(args[0]));
										givenplayer.sendMessage("運営チームよりNo" + args[0] + "の実績が配布されました。");
										sender.sendMessage("【配布完了】No" + args[0] +"の実績を配布しました。");
									}else {
										sender.sendMessage("既に該当実績を獲得しています。");
									}
									return true;
								}else if(args[2].equals("deprive")){
									//該当実績を既に取得していない場合処理をスキップ
									if(givenplayerdata.TitleFlags.get(Integer.parseInt(args[0]))){
										givenplayerdata.TitleFlags.set(Integer.parseInt(args[0]),false);
										sender.sendMessage("【剥奪完了】No" + args[0] +"の実績を剥奪しました。");
									}else {
										sender.sendMessage("該当実績を獲得していません。");
									}
									return true;
								}else {
									sender.sendMessage("実績を付与したい場合は「give」を、剥奪したい場合は「deprive」を、");
									sender.sendMessage("それぞれコマンドの最後に入力してください。");
									return true;
								}
							}catch(ArrayIndexOutOfBoundsException e){
								sender.sendMessage("実績を付与したい場合は「give」を、剥奪したい場合は「deprive」を、");
								sender.sendMessage("それぞれコマンドの最後に入力してください。");
								return true;
							}
						}

					}else {
						sender.sendMessage("【実行エラー】プレイヤー名が未入力です");
						return true;
					}
				}
			}else{
				sender.sendMessage("【実行エラー】解禁コマンドが使用できるのはNo7000～7999の実績です。");
				sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");
				return true;
			}
		}else{
			sender.sendMessage("【実行エラー】実績Noの項目は半角数字で入力してください");
			sender.sendMessage(ChatColor.RED + "/unlockachv <実績No> <プレイヤー名> <give/deprive>");
			return true;
		}
	}
}
