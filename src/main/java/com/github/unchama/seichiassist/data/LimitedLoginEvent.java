package com.github.unchama.seichiassist.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
//import org.bukkit.metadata.FixedMetadataValue;
//import org.bukkit.plugin.java.JavaPlugin;
import com.github.unchama.seichiassist.util.Util;

public class LimitedLoginEvent {
	private static Config config = SeichiAssist.config;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;
	String lastcheckdate ;

//    private JavaPlugin plugin;

//	public void BlockLineUp(JavaPlugin plugin) {
//		this.plugin = plugin;
//		plugin.getServer().getPluginManager().registerEvents(this, plugin);
//	}


	public void getLastcheck(String s){
		lastcheckdate = s ;
	}

	//ここで処理対象のユーザーと、そのtitleNoを拾って処理を行う。
	public void TryGetItem(Player p){
		player = p;
		UUID uuid = p.getUniqueId();
		playerdata = playermap.get(uuid);
		ItemStack skull = Util.getskull(Util.getName(player));
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

		if (!(lastcheckdate == "" || lastcheckdate == null)){
			try{
			    Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
			    Date LastDate = sdf.parse(lastcheckdate);
			    Date LLEStart = sdf.parse(config.getLimitedLoginEventStart());
			    Date LLEEnd = sdf.parse(config.getLimitedLoginEventEnd());

				long TodayLong = TodayDate.getTime();
				long LastLong = LastDate.getTime();
				long LLEStartLong = LLEStart.getTime();
				long LLEEndLong = LLEEnd.getTime();

				int eventlogin = playerdata.LimitedLoginCount ;
				int checknum = 0;
				int ItemNo = 0;
				int Amount = 0;

				//開催期間内かどうか
				if((((TodayLong - LLEStartLong)/(1000 * 60 * 60 * 24 )) >= 0) &&
						(((TodayLong - LLEEndLong)/(1000 * 60 * 60 * 24 )) <= 0)) {
					//最終ログインが開催期間内だったか
					if(!((((LastLong - LLEStartLong)/(1000 * 60 * 60 * 24 )) >= 0) &&
							(((LastLong - LLEEndLong)/(1000 * 60 * 60 * 24 )) <= 0))) {
						//開催期間内初のログイン時、開催終了後初のログイン時にここを処理
						//期間限定の累計ログイン数のデータをリセットする。
						eventlogin = 0 ;
					}

					eventlogin = eventlogin + 1 ;
					checknum = 0;
					do{
						ItemNo = Integer.parseInt(config.getLimitedLoginEventItem(checknum));
						Amount = Integer.parseInt(config.getLimitedLoginEventAmount(checknum));
						switch(ItemNo){
							case 1://配布対象「ガチャ券」
								String MessageT = "";
								if (checknum ==0){
									MessageT = "限定ログボ！";
								}else{
									MessageT = "限定ログボ" + eventlogin + "日目記念！";
								}
								p.sendMessage("【"+ MessageT +"】"+ Amount +"個のガチャ券をプレゼント！");
								int count = 1;
								while(count <= Amount ){
									if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
										Util.addItem(player,skull);
									}else{
										Util.dropItem(player,skull);
									}
									count++;
								}
								break;

							case 2://配布対象「未定」
								//配布処理記入場所
								//今後の追加のためのサンプルです。
								break;
						}

						checknum = checknum + eventlogin ;
					}while(checknum == eventlogin);
				}

				playerdata.LimitedLoginCount = eventlogin ;

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}
}
