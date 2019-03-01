package com.github.unchama.seichiassist.task;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.GridTemplate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.LimitedLoginEvent;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Timer;

public class LoadPlayerDataTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.sql;
	private static Config config = SeichiAssist.config;

	final String table = SeichiAssist.PLAYERDATA_TABLENAME;
	LimitedLoginEvent LLE = new LimitedLoginEvent();

	String name;
	Player p;
	PlayerData playerdata;
	final UUID uuid;
	final String struuid;
	String command;
	public static String exc;
	Boolean flag;
	int i;
	Statement stmt = null;
	ResultSet rs = null;
	String db;
	Timer timer;

	public LoadPlayerDataTaskRunnable(PlayerData playerData) {
		timer = new Timer(Timer.MILLISECOND);
		timer.start();
		db = SeichiAssist.config.getDB();
		p = Bukkit.getPlayer(playerData.uuid);
		playerdata = playerData;
		name = playerData.name;
		uuid = playerData.uuid;
		struuid = uuid.toString().toLowerCase();
		command = "";
		flag = true;
		i = 0;
	}

	@Override
	public void run() {

		//対象プレイヤーがオフラインなら処理終了
		if(SeichiAssist.plugin.getServer().getPlayer(uuid) == null){
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "はオフラインの為取得処理を中断");
			cancel();
			return;
		}
		//sqlコネクションチェック
		sql.checkConnection();
		//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
		try {
			stmt = sql.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

 		//ログインフラグの確認を行う
		command = "select loginflag from " + db + "." + table
 				+ " where uuid = '" + struuid + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   flag = rs.getBoolean("loginflag");
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			cancel();
			return;
		}

 		if(i >= 4&&flag){
 			//強制取得実行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "のplayerdata強制取得実行");
 			cancel();
 		}else if(!flag){
 			//flagが折れてたので普通に取得実行
 			cancel();
 		}else{
 			//再試行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + p.getName() + "のloginflag=false待機…(" + (i+1) + "回目)");
 			i++;
 			return;
 		}

		//loginflag書き換え&lastquit更新処理
		command = "update " + db + "." + table
				+ " set loginflag = true"
				+ ",lastquit = cast( now() as datetime )"
				+ " where uuid like '" + struuid + "'";
		try {
			stmt.executeUpdate(command);
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			cancel();
			return;
		}

		//playerdataをsqlデータから得られた値で更新
		command = "select * from " + db + "." + table
				+ " where uuid like '" + struuid + "'";
		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				//各種数値
				playerdata.loaded = true;
 				playerdata.effectflag = rs.getInt("effectflag");
 				playerdata.minestackflag = rs.getBoolean("minestackflag");
 				playerdata.messageflag = rs.getBoolean("messageflag");
 				playerdata.activeskilldata.mineflagnum = rs.getInt("activemineflagnum");
 				playerdata.activeskilldata.assaultflag = rs.getBoolean("assaultflag");
 				playerdata.activeskilldata.skilltype = rs.getInt("activeskilltype");
 				playerdata.activeskilldata.skillnum = rs.getInt("activeskillnum");
 				playerdata.activeskilldata.assaulttype = rs.getInt("assaultskilltype");
 				playerdata.activeskilldata.assaultnum = rs.getInt("assaultskillnum");
 				playerdata.activeskilldata.arrowskill = rs.getInt("arrowskill");
 				playerdata.activeskilldata.multiskill = rs.getInt("multiskill");
 				playerdata.activeskilldata.breakskill = rs.getInt("breakskill");
 				playerdata.activeskilldata.fluidcondenskill = rs.getInt("fluidcondenskill");
 				playerdata.activeskilldata.watercondenskill = rs.getInt("watercondenskill");
 				playerdata.activeskilldata.lavacondenskill = rs.getInt("lavacondenskill");
 				playerdata.activeskilldata.effectnum = rs.getInt("effectnum");
 				playerdata.gachapoint = rs.getInt("gachapoint");
 				playerdata.gachaflag = rs.getBoolean("gachaflag");
 				playerdata.level = rs.getInt("level");
 				playerdata.numofsorryforbug = rs.getInt("numofsorryforbug");
 				playerdata.rgnum = rs.getInt("rgnum");
 				playerdata.inventory = BukkitSerialization.fromBase64forPocket(rs.getString("inventory"));
 				playerdata.dispkilllogflag = rs.getBoolean("killlogflag");
 				playerdata.dispworldguardlogflag = rs.getBoolean("worldguardlogflag");

 				playerdata.multipleidbreakflag = rs.getBoolean("multipleidbreakflag");

 				playerdata.pvpflag = rs.getBoolean("pvpflag");
 				playerdata.totalbreaknum = rs.getLong("totalbreaknum");
 				playerdata.playtick = rs.getInt("playtick");
 				playerdata.p_givenvote = rs.getInt("p_givenvote");
 				playerdata.activeskilldata.effectpoint = rs.getInt("effectpoint");
 				playerdata.activeskilldata.premiumeffectpoint = rs.getInt("premiumeffectpoint");
 				//マナの情報
 				playerdata.activeskilldata.mana.setMana(rs.getDouble("mana"));
 				playerdata.expbar.setVisible(rs.getBoolean("expvisible"));

 				playerdata.totalexp = rs.getInt("totalexp");

 				playerdata.expmarge = rs.getByte("expmarge");
 				playerdata.shareinv = (rs.getString("shareinv") != "" && rs.getString("shareinv") != null);
 				playerdata.everysoundflag = rs.getBoolean("everysound");
 				playerdata.everymessageflag = rs.getBoolean("everymessage");

 				//subhomeの情報
 				playerdata.SetSubHome(rs.getString("homepoint_" + SeichiAssist.config.getServerNum()));
 				playerdata.setSubHomeName(rs.getString("subhome_name_" + SeichiAssist.config.getServerNum()));
 				playerdata.selectHomeNum = 0;
 				playerdata.setHomeNameNum = 0;
 				playerdata.isSubHomeNameChange = false;

 				//実績、二つ名の情報
 				playerdata.displayTypeLv = rs.getBoolean("displayTypeLv");
 				playerdata.displayTitle1No = rs.getInt("displayTitle1No");
 				playerdata.displayTitle2No = rs.getInt("displayTitle2No");
 				playerdata.displayTitle3No = rs.getInt("displayTitle3No");
 				playerdata.p_vote_forT = rs.getInt("p_vote");
 				playerdata.giveachvNo = rs.getInt("giveachvNo");
 				playerdata.achvPointMAX = rs.getInt("achvPointMAX");
 				playerdata.achvPointUSE = rs.getInt("achvPointUSE");
 				playerdata.achvChangenum =rs.getInt("achvChangenum");
 				playerdata.achvPoint = (playerdata.achvPointMAX + (playerdata.achvChangenum * 3)) - playerdata.achvPointUSE ;

 				//スターレベルの情報
 				playerdata.starlevel =rs.getInt("starlevel");
 				playerdata.starlevel_Break =rs.getInt("starlevel_Break");
 				playerdata.starlevel_Time =rs.getInt("starlevel_Time");
 				playerdata.starlevel_Event =rs.getInt("starlevel_Event");

 				//期間限定ログインイベント専用の累計ログイン日数
 				playerdata.LimitedLoginCount =rs.getInt("LimitedLoginCount");

 				//連続・通算ログインの情報、およびその更新
 		        Calendar cal = Calendar.getInstance();
 		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 				if(rs.getString("lastcheckdate") == "" || rs.getString("lastcheckdate") == null){
 					playerdata.lastcheckdate = sdf.format(cal.getTime());
 				}else {
 					playerdata.lastcheckdate = rs.getString("lastcheckdate");
 				}
 				playerdata.ChainJoin = rs.getInt("ChainJoin");
 				playerdata.TotalJoin = rs.getInt("TotalJoin");
 				if(playerdata.ChainJoin == 0){
 					playerdata.ChainJoin = 1 ;
 				}
 				if(playerdata.TotalJoin == 0){
 					playerdata.TotalJoin = 1 ;
 				}

 		        try {
					Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
					Date LastDate = sdf.parse(playerdata.lastcheckdate);
					long TodayLong = TodayDate.getTime();
					long LastLong = LastDate.getTime();

					long datediff = (TodayLong - LastLong)/(1000 * 60 * 60 * 24 );
					if(datediff > 0){
						LLE.getLastcheck(playerdata.lastcheckdate);
						playerdata.TotalJoin ++ ;
						if(datediff == 1 ){
							playerdata.ChainJoin ++ ;
						}else {
							playerdata.ChainJoin = 1 ;
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
 		        playerdata.lastcheckdate = sdf.format(cal.getTime());

 		        //連続投票の更新
 		        String lastvote = rs.getString("lastvote");
 		        if(lastvote == "" || lastvote == null){
 		        	playerdata.ChainVote = 0;
 		        }else {
 		        	try {
 						Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
 						Date LastDate = sdf.parse(lastvote);
 						long TodayLong = TodayDate.getTime();
 						long LastLong = LastDate.getTime();

 						long datediff = (TodayLong - LastLong)/(1000 * 60 * 60 * 24 );
 						if(datediff <= 1 || datediff >= 0){
 							playerdata.ChainVote = rs.getInt("chainvote");
 						}else{
 							playerdata.ChainVote = 0;
 						}
 					} catch (ParseException e) {
 						e.printStackTrace();
 					}
 		        }

 				//実績解除フラグのBitSet型への復元処理
 				//初回nullエラー回避のための分岐
 				try {
 				String[] Titlenums = rs.getString("TitleFlags").split(",");
 		        long[] Titlearray = Arrays.stream(Titlenums).mapToLong(x -> Long.parseUnsignedLong(x, 16)).toArray();
 		        BitSet TitleFlags = BitSet.valueOf(Titlearray);
 		        playerdata.TitleFlags = TitleFlags ;
 				}
 				catch(NullPointerException e){
 					playerdata.TitleFlags = new BitSet(10000);
 					playerdata.TitleFlags.set(1);
 				}

 				//建築
 				playerdata.build_lv_set(rs.getInt("build_lv"));
 				playerdata.build_count_set(new BigDecimal(rs.getString("build_count")));
 				playerdata.build_count_flg_set(rs.getByte("build_count_flg"));

				//マナ妖精
				playerdata.usingVotingFairy = rs.getBoolean("canVotingFairyUse");
				playerdata.VotingFairyRecoveryValue = rs.getInt("VotingFairyRecoveryValue");
				playerdata.hasVotingFairyMana = rs.getInt("hasVotingFairyMana");
				playerdata.toggleGiveApple = rs.getInt("toggleGiveApple");
				playerdata.toggleVotingFairy = rs.getInt("toggleVotingFairy");
				playerdata.SetVotingFairyTime(rs.getString("newVotingFairyTime"),p);
				playerdata.p_apple = rs.getLong("p_apple");


				playerdata.contribute_point = rs.getInt("contribute_point");
				playerdata.added_mana = rs.getInt("added_mana");

				playerdata.GBstage = rs.getInt("GBstage");
				playerdata.GBexp = rs.getInt("GBexp");
				playerdata.GBlevel = rs.getInt("GBlevel");
				playerdata.isGBStageUp = rs.getBoolean("isGBStageUp");

 				// 1周年記念
 				if (playerdata.anniversary = rs.getBoolean("anniversary")) {
 					p.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary");
 					p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
 				}

 				ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
 				for(int i = 0 ; i < activeskilleffect.length ; i++){
 					int num = activeskilleffect[i].getNum();
 					String sqlname = activeskilleffect[i].getsqlName();
 					playerdata.activeskilldata.effectflagmap.put(num, rs.getBoolean(sqlname));
 				}
 				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
 				for(int i = 0 ; i < premiumeffect.length ; i++){
 					int num = premiumeffect[i].getNum();
 					String sqlname = premiumeffect[i].getsqlName();
 					playerdata.activeskilldata.premiumeffectflagmap.put(num, rs.getBoolean(sqlname));
 				}
 				//MineStack機能の数値

				//MineStack関連をすべてfor文に変更
 				if(SeichiAssist.minestack_sql_enable){
 					for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
 						int temp_num = rs.getInt("stack_"+SeichiAssist.minestacklist.get(i).getMineStackObjName());
 						playerdata.minestack.setNum(i, temp_num);
 					}
 				}

				//グリッド式保護の保存データを読み込み
				Map<Integer, GridTemplate> saveMap = new HashMap<>();
				for (int i = 0; i <= config.getTemplateKeepAmount() - 1 ; i++) {
					GridTemplate template = new GridTemplate(rs.getInt("ahead_" + i), rs.getInt("behind_" + i),
							rs.getInt("right_" + i), rs.getInt("left_" + i));
					saveMap.put(i, template);
				}
				playerdata.setTemplateMap(saveMap);

				//正月イベント用
				playerdata.hasNewYearSobaGive = rs.getBoolean("hasNewYearSobaGive");
				playerdata.newYearBagAmount = rs.getInt("newYearBagAmount");

				//バレンタインイベント用
				playerdata.hasChocoGave = rs.getBoolean("hasChocoGave");
			  }
			rs.close();
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();

			//コネクション復活後にnewインスタンスのデータで上書きされるのを防止する為削除しておく
			playermap.remove(uuid);

			//既に宣言済み
			//cancel();
			return;
		}

		//念のためstatement閉じておく
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(SeichiAssist.DEBUG){
			p.sendMessage("sqlデータで更新しました");
		}
		//更新したplayerdataをplayermapに追加
		playermap.put(uuid, playerdata);

		//期間限定ログインイベント判別処理
		LLE.TryGetItem(p);

		//貢献度pt増加によるマナ増加があるかどうか
		if(playerdata.added_mana < playerdata.contribute_point){
			int addMana;
			addMana = playerdata.contribute_point - playerdata.added_mana;
			playerdata.isContribute(p, addMana);
		}
		timer.sendLapTimeMessage(ChatColor.GREEN + p.getName() + "のプレイヤーデータ読込完了");
		return;
	}
}
