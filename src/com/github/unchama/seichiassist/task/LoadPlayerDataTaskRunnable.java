package com.github.unchama.seichiassist.task;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.BitSet;
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
	String db;

	public LoadPlayerDataTaskRunnable(Player _p) {
		db = SeichiAssist.config.getDB();
		p = _p;
		name = Util.getName(p);
		uuid = p.getUniqueId();
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

		//PlayerDataを新規作成
		PlayerData playerdata = new PlayerData(p);

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
 				playerdata.activeskilldata.condenskill = rs.getInt("condenskill");
 				playerdata.activeskilldata.effectnum = rs.getInt("effectnum");
 				playerdata.gachapoint = rs.getInt("gachapoint");
 				playerdata.gachaflag = rs.getBoolean("gachaflag");
 				playerdata.level = rs.getInt("level");
 				playerdata.numofsorryforbug = rs.getInt("numofsorryforbug");
 				playerdata.rgnum = rs.getInt("rgnum");
 				playerdata.inventory = BukkitSerialization.fromBase64(rs.getString("inventory").toString());
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

 				//subhomeの情報
 				playerdata.SetSubHome(rs.getString("homepoint_" + SeichiAssist.config.getServerNum()));

 				//実績、二つ名の情報
 				playerdata.displayTypeLv = rs.getBoolean("displayTypeLv");
 				playerdata.displayTitleNo = rs.getInt("displayTitleNo");
 				playerdata.p_vote_forT = rs.getInt("p_vote");
 				playerdata.giveachvNo = rs.getInt("giveachvNo");

 				//実績解除フラグのBitSet型への復元処理
 				//初回nullエラー回避のための分岐
 				try {
 				String[] Titlenums = rs.getString("TitleFlags").toString().split(",");
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
 				playerdata.build_count_set(rs.getInt("build_count"));
 				playerdata.build_count_flg_set(rs.getByte("build_count_flg"));


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
 				if(SeichiAssist.minestack_sql_enable==true){
 					for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
 						int temp_num = rs.getInt("stack_"+SeichiAssist.minestacklist.get(i).getMineStackObjName());
 						playerdata.minestack.setNum(i, temp_num);
 					}
 				}
 				/*
 				playerdata.minestack.dirt = rs.getInt("stack_dirt");
 				playerdata.minestack.gravel = rs.getInt("stack_gravel");
 				playerdata.minestack.cobblestone = rs.getInt("stack_cobblestone");
 				playerdata.minestack.stone = rs.getInt("stack_stone");
 				playerdata.minestack.sand = rs.getInt("stack_sand");
 				playerdata.minestack.sandstone = rs.getInt("stack_sandstone");
 				playerdata.minestack.netherrack = rs.getInt("stack_netherrack");
 				playerdata.minestack.ender_stone = rs.getInt("stack_ender_stone");
 				playerdata.minestack.obsidian = rs.getInt("stack_obsidian");
 				playerdata.minestack.grass = rs.getInt("stack_grass");
 				playerdata.minestack.quartz = rs.getInt("stack_quartz");
 				playerdata.minestack.quartz_ore = rs.getInt("stack_quartz_ore");
 				playerdata.minestack.soul_sand = rs.getInt("stack_soul_sand");
 				playerdata.minestack.magma = rs.getInt("stack_magma");
 				playerdata.minestack.coal = rs.getInt("stack_coal");
 				playerdata.minestack.coal_ore = rs.getInt("stack_coal_ore");
 				playerdata.minestack.iron_ore = rs.getInt("stack_iron_ore");
 				playerdata.minestack.packed_ice = rs.getInt("stack_packed_ice");
 				playerdata.minestack.gold_ore = rs.getInt("stack_gold_ore");
 				playerdata.minestack.lapis_ore = rs.getInt("stack_lapis_ore");
 				playerdata.minestack.emerald_ore = rs.getInt("stack_emerald_ore");
 				playerdata.minestack.redstone_ore = rs.getInt("stack_redstone_ore");
 				playerdata.minestack.diamond_ore = rs.getInt("stack_diamond_ore");
 				playerdata.minestack.log = rs.getInt("stack_log");
 				playerdata.minestack.log_2 = rs.getInt("stack_log_2");
 				playerdata.minestack.wood = rs.getInt("stack_wood");
 				playerdata.minestack.hard_clay = rs.getInt("stack_hard_clay");
 				playerdata.minestack.stained_clay = rs.getInt("stack_stained_clay");
 				playerdata.minestack.fence = rs.getInt("stack_fence");
 				playerdata.minestack.lapis_lazuli = rs.getInt("stack_lapis_lazuli"); //テスト
 				playerdata.minestack.granite = rs.getInt("stack_granite"); //追加
 				playerdata.minestack.diorite = rs.getInt("stack_diorite"); //追加
 				playerdata.minestack.andesite = rs.getInt("stack_andesite"); //追加
 				playerdata.minestack.red_sand = rs.getInt("stack_red_sand"); //追加
 				playerdata.minestack.red_sandstone = rs.getInt("stack_red_sandstone"); //追加
 				playerdata.minestack.log1 = rs.getInt("stack_log1"); //追加
 				playerdata.minestack.log2 = rs.getInt("stack_log2"); //追加
 				playerdata.minestack.log3 = rs.getInt("stack_log3"); //追加
 				playerdata.minestack.log_21 = rs.getInt("stack_log_21"); //追加
 				playerdata.minestack.stained_clay1 = rs.getInt("stack_stained_clay1"); //追加
 				playerdata.minestack.stained_clay4 = rs.getInt("stack_stained_clay4"); //追加
 				playerdata.minestack.stained_clay8 = rs.getInt("stack_stained_clay8"); //追加
 				playerdata.minestack.stained_clay12 = rs.getInt("stack_stained_clay12"); //追加
 				playerdata.minestack.stained_clay14 = rs.getInt("stack_stained_clay14"); //追加
 				playerdata.minestack.emerald = rs.getInt("stack_emerald"); //追加
 				playerdata.minestack.redstone = rs.getInt("stack_redstone"); //追加
 				playerdata.minestack.diamond = rs.getInt("stack_diamond"); //追加
 				playerdata.minestack.clay = rs.getInt("stack_clay"); //追加
 				playerdata.minestack.glowstone = rs.getInt("stack_glowstone"); //追加
 				playerdata.minestack.dirt1 = rs.getInt("stack_dirt1");
 				playerdata.minestack.dirt2 = rs.getInt("stack_dirt2");
 				playerdata.minestack.mycel = rs.getInt("stack_mycel");
 				playerdata.minestack.snow_block = rs.getInt("stack_snow_block");
 				playerdata.minestack.ice = rs.getInt("stack_ice");
 				playerdata.minestack.dark_oak_fence = rs.getInt("stack_dark_oak_fence");
 				playerdata.minestack.mossy_cobblestone = rs.getInt("stack_mossy_cobblestone");
 				playerdata.minestack.rails = rs.getInt("stack_rails");
 				playerdata.minestack.exp_bottle = rs.getInt("stack_exp_bottle");
 				playerdata.minestack.huge_mushroom_1 = rs.getInt("stack_huge_mushroom_1");
 				playerdata.minestack.huge_mushroom_2 = rs.getInt("stack_huge_mushroom_2");
 				playerdata.minestack.web = rs.getInt("stack_web");
 				playerdata.minestack.string = rs.getInt("stack_string");
 				playerdata.minestack.wood5 = rs.getInt("stack_wood5");
 				playerdata.minestack.sapling = rs.getInt("stack_sapling");
 				playerdata.minestack.sapling1 = rs.getInt("stack_sapling1");
 				playerdata.minestack.sapling2 = rs.getInt("stack_sapling2");
 				playerdata.minestack.sapling3 = rs.getInt("stack_sapling3");
 				playerdata.minestack.sapling4 = rs.getInt("stack_sapling4");
 				playerdata.minestack.sapling5 = rs.getInt("stack_sapling5");
 				playerdata.minestack.leaves = rs.getInt("stack_leaves");
 				playerdata.minestack.leaves1 = rs.getInt("stack_leaves1");
 				playerdata.minestack.leaves2 = rs.getInt("stack_leaves2");
 				playerdata.minestack.leaves3 = rs.getInt("stack_leaves3");
 				playerdata.minestack.leaves_2 = rs.getInt("stack_leaves_2");
 				playerdata.minestack.leaves_21 = rs.getInt("stack_leaves_21");
 				playerdata.minestack.gachaimo = rs.getInt("stack_gachaimo");
 				*/
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
