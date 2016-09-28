package com.github.unchama.seichiassist.task;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Util;

public class LoadPlayerDataTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.plugin.sql;

	final String table = SeichiAssist.PLAYERDATA_TABLENAME;

	String name;
	Player p;
	final UUID uuid;
	final String struuid;
	String command;
	public static String exc;
	Boolean flag;
	int i;
	Statement stmt2 = null;
	ResultSet rs2 = null;

	public LoadPlayerDataTaskRunnable(Player _p) {
		p = _p;
		name = Util.getName(p);
		uuid = p.getUniqueId();
		struuid = uuid.toString().toLowerCase();
		command = "";
		flag = true;
		i = 0;
		//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
		try {
			stmt2 = sql.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void run() {

 	 		command = "select loginflag from " + table
 	 				+ " where uuid = '" + struuid + "'";
 	 		try{
 				rs2 = stmt2.executeQuery(command);
 				while (rs2.next()) {
 					   flag = rs2.getBoolean("loginflag");
 					  }
 				rs2.close();
 			} catch (SQLException e) {
 				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
 				exc = e.getMessage();
 				e.printStackTrace();
 				i++;
 				return;
 			}

 	 		if(i >= 10&&flag){
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
			command = "update " + table
					+ " set loginflag = true"
					+ ",lastquit = cast( now() as datetime )"
					+ " where uuid like '" + struuid + "'";
			try {
				stmt2.executeUpdate(command);
			} catch (SQLException e) {
				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
				exc = e.getMessage();
				e.printStackTrace();
				i++;
				return;
			}

			//PlayerDataを新規作成
			PlayerData playerdata = new PlayerData(p);

			//playerdataをsqlデータから得られた値で更新
			command = "select * from " + table
					+ " where uuid like '" + struuid + "'";
			try{
				rs2 = stmt2.executeQuery(command);
				while (rs2.next()) {
					//各種数値
	 				playerdata.effectflag = rs2.getBoolean("effectflag");
	 				playerdata.minestackflag = rs2.getBoolean("minestackflag");
	 				playerdata.messageflag = rs2.getBoolean("messageflag");
	 				playerdata.activeskilldata.mineflagnum = rs2.getInt("activemineflagnum");
	 				playerdata.activeskilldata.assaultflag = rs2.getBoolean("assaultflag");
	 				playerdata.activeskilldata.skilltype = rs2.getInt("activeskilltype");
	 				playerdata.activeskilldata.skillnum = rs2.getInt("activeskillnum");
	 				playerdata.activeskilldata.assaulttype = rs2.getInt("assaultskilltype");
	 				playerdata.activeskilldata.assaultnum = rs2.getInt("assaultskillnum");
	 				playerdata.activeskilldata.arrowskill = rs2.getInt("arrowskill");
	 				playerdata.activeskilldata.multiskill = rs2.getInt("multiskill");
	 				playerdata.activeskilldata.breakskill = rs2.getInt("breakskill");
	 				playerdata.activeskilldata.condenskill = rs2.getInt("condenskill");
	 				playerdata.activeskilldata.effectnum = rs2.getInt("effectnum");
	 				playerdata.gachapoint = rs2.getInt("gachapoint");
	 				playerdata.gachaflag = rs2.getBoolean("gachaflag");
	 				playerdata.level = rs2.getInt("level");
	 				playerdata.numofsorryforbug = rs2.getInt("numofsorryforbug");
	 				playerdata.rgnum = rs2.getInt("rgnum");
	 				playerdata.inventory = BukkitSerialization.fromBase64(rs2.getString("inventory").toString());
	 				playerdata.dispkilllogflag = rs2.getBoolean("killlogflag");
	 				playerdata.pvpflag = rs2.getBoolean("pvpflag");
	 				playerdata.totalbreaknum = rs2.getInt("totalbreaknum");
	 				playerdata.playtick = rs2.getInt("playtick");
	 				playerdata.p_givenvote = rs2.getInt("p_givenvote");
	 				playerdata.activeskilldata.effectpoint = rs2.getInt("effectpoint");
	 				playerdata.activeskilldata.premiumeffectpoint = rs2.getInt("premiumeffectpoint");
	 				//マナの情報
	 				playerdata.activeskilldata.mana.setMana(rs2.getDouble("mana"));

	 				ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
	 				for(int i = 0 ; i < activeskilleffect.length ; i++){
	 					int num = activeskilleffect[i].getNum();
	 					String sqlname = activeskilleffect[i].getsqlName();
	 					playerdata.activeskilldata.effectflagmap.put(num, rs2.getBoolean(sqlname));
	 				}
	 				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
	 				for(int i = 0 ; i < premiumeffect.length ; i++){
	 					int num = premiumeffect[i].getNum();
	 					String sqlname = premiumeffect[i].getsqlName();
	 					playerdata.activeskilldata.premiumeffectflagmap.put(num, rs2.getBoolean(sqlname));
	 				}
	 				//MineStack機能の数値
	 				playerdata.minestack.dirt = rs2.getInt("stack_dirt");
	 				playerdata.minestack.gravel = rs2.getInt("stack_gravel");
	 				playerdata.minestack.cobblestone = rs2.getInt("stack_cobblestone");
	 				playerdata.minestack.stone = rs2.getInt("stack_stone");
	 				playerdata.minestack.sand = rs2.getInt("stack_sand");
	 				playerdata.minestack.sandstone = rs2.getInt("stack_sandstone");
	 				playerdata.minestack.netherrack = rs2.getInt("stack_netherrack");
	 				playerdata.minestack.ender_stone = rs2.getInt("stack_ender_stone");
	 				playerdata.minestack.grass = rs2.getInt("stack_grass");
	 				playerdata.minestack.quartz = rs2.getInt("stack_quartz");
	 				playerdata.minestack.quartz_ore = rs2.getInt("stack_quartz_ore");
	 				playerdata.minestack.soul_sand = rs2.getInt("stack_soul_sand");
	 				playerdata.minestack.magma = rs2.getInt("stack_magma");
	 				playerdata.minestack.coal = rs2.getInt("stack_coal");
	 				playerdata.minestack.coal_ore = rs2.getInt("stack_coal_ore");
	 				playerdata.minestack.iron_ore = rs2.getInt("stack_iron_ore");
	 				playerdata.minestack.packed_ice = rs2.getInt("stack_packed_ice");
				  }
				rs2.close();
			} catch (SQLException | IOException e) {
				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
				exc = e.getMessage();
				e.printStackTrace();
				i++;
				return;
			}
			//念のためstatement閉じておく
			try {
				stmt2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(SeichiAssist.DEBUG){
				p.sendMessage("sqlデータで更新しました");
			}
			//更新したplayerdataをplayermapに追加
			playermap.put(uuid, playerdata);

			p.sendMessage(ChatColor.GREEN + "プレイヤーデータ取得完了");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + p.getName() + "のプレイヤーデータ取得完了");

			//join時とonenable時、プレイヤーデータを最新の状態に更新
			playerdata.updateonJoin(p);

			return;
	}

}
