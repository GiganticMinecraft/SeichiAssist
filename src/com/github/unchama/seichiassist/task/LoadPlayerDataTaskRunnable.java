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
	Statement stmt = null;
	ResultSet rs = null;

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
			stmt = sql.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void run() {

 	 		command = "select loginflag from " + table
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
			command = "update " + table
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

			//PlayerDataを新規作成
			PlayerData playerdata = new PlayerData(p);

			//playerdataをsqlデータから得られた値で更新
			command = "select * from " + table
					+ " where uuid like '" + struuid + "'";
			try{
				rs = stmt.executeQuery(command);
				while (rs.next()) {
					//各種数値
	 				playerdata.effectflag = rs.getBoolean("effectflag");
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
	 				playerdata.activeskilldata.condenskill = rs.getInt("condenskill");
	 				playerdata.activeskilldata.effectnum = rs.getInt("effectnum");
	 				playerdata.gachapoint = rs.getInt("gachapoint");
	 				playerdata.gachaflag = rs.getBoolean("gachaflag");
	 				playerdata.level = rs.getInt("level");
	 				playerdata.numofsorryforbug = rs.getInt("numofsorryforbug");
	 				playerdata.rgnum = rs.getInt("rgnum");
	 				playerdata.inventory = BukkitSerialization.fromBase64(rs.getString("inventory").toString());
	 				playerdata.dispkilllogflag = rs.getBoolean("killlogflag");
	 				playerdata.pvpflag = rs.getBoolean("pvpflag");
	 				playerdata.totalbreaknum = rs.getInt("totalbreaknum");
	 				playerdata.playtick = rs.getInt("playtick");
	 				playerdata.p_givenvote = rs.getInt("p_givenvote");
	 				playerdata.activeskilldata.effectpoint = rs.getInt("effectpoint");
	 				playerdata.activeskilldata.premiumeffectpoint = rs.getInt("premiumeffectpoint");
	 				//マナの情報
	 				playerdata.activeskilldata.mana.setMana(rs.getDouble("mana"));

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
	 				playerdata.minestack.dirt = rs.getInt("stack_dirt");
	 				playerdata.minestack.gravel = rs.getInt("stack_gravel");
	 				playerdata.minestack.cobblestone = rs.getInt("stack_cobblestone");
	 				playerdata.minestack.stone = rs.getInt("stack_stone");
	 				playerdata.minestack.sand = rs.getInt("stack_sand");
	 				playerdata.minestack.sandstone = rs.getInt("stack_sandstone");
	 				playerdata.minestack.netherrack = rs.getInt("stack_netherrack");
	 				playerdata.minestack.ender_stone = rs.getInt("stack_ender_stone");
	 				playerdata.minestack.grass = rs.getInt("stack_grass");
	 				playerdata.minestack.quartz = rs.getInt("stack_quartz");
	 				playerdata.minestack.quartz_ore = rs.getInt("stack_quartz_ore");
	 				playerdata.minestack.soul_sand = rs.getInt("stack_soul_sand");
	 				playerdata.minestack.magma = rs.getInt("stack_magma");
	 				playerdata.minestack.coal = rs.getInt("stack_coal");
	 				playerdata.minestack.coal_ore = rs.getInt("stack_coal_ore");
	 				playerdata.minestack.iron_ore = rs.getInt("stack_iron_ore");
	 				playerdata.minestack.packed_ice = rs.getInt("stack_packed_ice");
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
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + p.getName() + "のプレイヤーデータ取得完了");

			return;
	}

}
