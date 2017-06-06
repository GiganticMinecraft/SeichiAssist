package com.github.unchama.seichiassist.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;

public class PlayerDataSaveTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.plugin;
	//private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.plugin.sql;

	final String table = SeichiAssist.PLAYERDATA_TABLENAME;

	PlayerData playerdata;
	String command;
	int i;
	//ondisableからの呼び出し時のみtrueにしておくフラグ
	boolean isOnDisable;
	//loginflag折る時にtrueにしておくフラグ
	boolean logoutflag;
	public static String exc;
	String db;
	Statement stmt = null;
	ResultSet rs = null;

	public PlayerDataSaveTaskRunnable(PlayerData _playerdata,boolean _isondisable,boolean _logoutflag) {
		db = SeichiAssist.config.getDB();
		command = "";
		i = 0;
		playerdata = _playerdata;
		//ondisableからの呼び出し時のみtrueにしておくフラグ
		isOnDisable = _isondisable;
		//loginflag折る時にtrueにしておくフラグ
		logoutflag = _logoutflag;
	}

	@Override
	public void run() {
		//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
		//sqlコネクションチェック
		sql.checkConnection();
		try {
			stmt = sql.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		//引数のplayerdataをsqlにデータを送信
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "";

		command = "update " + db + "." + table
				+ " set"

				//名前更新処理
				+ " name = '" + playerdata.name + "'"

				//各種数値更新処理
				+ ",effectflag = " + Integer.toString(playerdata.effectflag)
				+ ",minestackflag = " + Boolean.toString(playerdata.minestackflag)
				+ ",messageflag = " + Boolean.toString(playerdata.messageflag)
				+ ",activemineflagnum = " + Integer.toString(playerdata.activeskilldata.mineflagnum)
				+ ",assaultflag = " + Boolean.toString(playerdata.activeskilldata.assaultflag)
				+ ",activeskilltype = " + Integer.toString(playerdata.activeskilldata.skilltype)
				+ ",activeskillnum = " + Integer.toString(playerdata.activeskilldata.skillnum)
				+ ",assaultskilltype = " + Integer.toString(playerdata.activeskilldata.assaulttype)
				+ ",assaultskillnum = " + Integer.toString(playerdata.activeskilldata.assaultnum)
				+ ",arrowskill = " + Integer.toString(playerdata.activeskilldata.arrowskill)
				+ ",multiskill = " + Integer.toString(playerdata.activeskilldata.multiskill)
				+ ",breakskill = " + Integer.toString(playerdata.activeskilldata.breakskill)
				+ ",condenskill = " + Integer.toString(playerdata.activeskilldata.condenskill)
				+ ",effectnum = " + Integer.toString(playerdata.activeskilldata.effectnum)
				+ ",gachapoint = " + Integer.toString(playerdata.gachapoint)
				+ ",gachaflag = " + Boolean.toString(playerdata.gachaflag)
				+ ",level = " + Integer.toString(playerdata.level)
				+ ",rgnum = " + Integer.toString(playerdata.rgnum)
				+ ",totalbreaknum = " + Long.toString(playerdata.totalbreaknum)
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
				+ ",playtick = " + Integer.toString(playerdata.playtick)
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + Boolean.toString(playerdata.dispkilllogflag)
				+ ",worldguardlogflag = " + Boolean.toString(playerdata.dispworldguardlogflag)

				+ ",multipleidbreakflag = " + Boolean.toString(playerdata.multipleidbreakflag)

				+ ",pvpflag = " + Boolean.toString(playerdata.pvpflag)
				+ ",effectpoint = " + Integer.toString(playerdata.activeskilldata.effectpoint)
				+ ",mana = " + Double.toString(playerdata.activeskilldata.mana.getMana())
				+ ",expvisible = " + Boolean.toString(playerdata.expbar.isVisible())
				+ ",totalexp = " + Integer.toString(playerdata.totalexp)
				+ ",expmarge = " + Byte.toString(playerdata.expmarge)
				+ ",everysound = " + Boolean.toString(playerdata.everysoundflag)

				+",displayTypeLv = " + Boolean.toString(playerdata.displayTypeLv)
				+",displayTitle1No = " + Integer.toString(playerdata.displayTitle1No)
				+",displayTitle2No = " + Integer.toString(playerdata.displayTitle2No)
				+",displayTitle3No = " + Integer.toString(playerdata.displayTitle3No)
				+",giveachvNo = " + Integer.toString(playerdata.giveachvNo)
				+",achvPointMAX = " + Integer.toString(playerdata.achvPointMAX)
				+",achvPointUSE = " + Integer.toString(playerdata.achvPointUSE)
				+",achvChangenum = " + Integer.toString(playerdata.achvChangenum)

				+",lastcheckdate = '" + playerdata.lastcheckdate + "'"
				+",ChainJoin = " + Integer.toString(playerdata.ChainJoin)
				+",TotalJoin = " + Integer.toString(playerdata.TotalJoin);

				//MineStack機能の数値更新処理

				//MineStack関連は全てfor文に変更
				if(SeichiAssist.minestack_sql_enable){
					for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
						command += ",stack_"+SeichiAssist.minestacklist.get(i).getMineStackObjName()+ " = "
							+ Integer.toString(playerdata.minestack.getNum(i));
					}
				}

				/*
				+ ",stack_dirt = " + Integer.toString(playerdata.minestack.dirt)
				+ ",stack_gravel = " + Integer.toString(playerdata.minestack.gravel)
				+ ",stack_cobblestone = " + Integer.toString(playerdata.minestack.cobblestone)
				+ ",stack_stone = " + Integer.toString(playerdata.minestack.stone)
				+ ",stack_sand = " + Integer.toString(playerdata.minestack.sand)
				+ ",stack_sandstone = " + Integer.toString(playerdata.minestack.sandstone)
				+ ",stack_netherrack = " + Integer.toString(playerdata.minestack.netherrack)
				+ ",stack_ender_stone = " + Integer.toString(playerdata.minestack.ender_stone)
				+ ",stack_obsidian = " + Integer.toString(playerdata.minestack.obsidian)
				+ ",stack_grass = " + Integer.toString(playerdata.minestack.grass)
				+ ",stack_quartz = " + Integer.toString(playerdata.minestack.quartz)
				+ ",stack_quartz_ore = " + Integer.toString(playerdata.minestack.quartz_ore)
				+ ",stack_soul_sand = " + Integer.toString(playerdata.minestack.soul_sand)
				+ ",stack_magma = " + Integer.toString(playerdata.minestack.magma)
				+ ",stack_coal = " + Integer.toString(playerdata.minestack.coal)
				+ ",stack_coal_ore = " + Integer.toString(playerdata.minestack.coal_ore)
				+ ",stack_iron_ore = " + Integer.toString(playerdata.minestack.iron_ore)
				+ ",stack_packed_ice = " + Integer.toString(playerdata.minestack.packed_ice)
				+ ",stack_gold_ore = " + Integer.toString(playerdata.minestack.gold_ore)
				+ ",stack_lapis_ore = " + Integer.toString(playerdata.minestack.lapis_ore)
				+ ",stack_emerald_ore = " + Integer.toString(playerdata.minestack.emerald_ore)
				+ ",stack_redstone_ore = " + Integer.toString(playerdata.minestack.redstone_ore)
				+ ",stack_diamond_ore = " + Integer.toString(playerdata.minestack.diamond_ore)
				+ ",stack_log = " + Integer.toString(playerdata.minestack.log)
				+ ",stack_log_2 = " + Integer.toString(playerdata.minestack.log_2)
				+ ",stack_wood = " + Integer.toString(playerdata.minestack.wood)
				+ ",stack_hard_clay = " + Integer.toString(playerdata.minestack.hard_clay)
				+ ",stack_stained_clay = " + Integer.toString(playerdata.minestack.stained_clay)
				+ ",stack_fence = " + Integer.toString(playerdata.minestack.fence)
				+ ",stack_lapis_lazuli = " + Integer.toString(playerdata.minestack.lapis_lazuli) //テスト
				+ ",stack_granite = " + Integer.toString(playerdata.minestack.granite) //追加
				+ ",stack_diorite = " + Integer.toString(playerdata.minestack.diorite) //追加
				+ ",stack_andesite = " + Integer.toString(playerdata.minestack.andesite) //追加
				+ ",stack_red_sand = " + Integer.toString(playerdata.minestack.red_sand) //追加
				+ ",stack_red_sandstone = " + Integer.toString(playerdata.minestack.red_sandstone) //追加
				+ ",stack_log1 = " + Integer.toString(playerdata.minestack.log1) //追加
				+ ",stack_log2 = " + Integer.toString(playerdata.minestack.log2) //追加
				+ ",stack_log3 = " + Integer.toString(playerdata.minestack.log3) //追加
				+ ",stack_log_21 = " + Integer.toString(playerdata.minestack.log_21) //追加
				+ ",stack_stained_clay1 = " + Integer.toString(playerdata.minestack.stained_clay1) //追加
				+ ",stack_stained_clay4 = " + Integer.toString(playerdata.minestack.stained_clay4) //追加
				+ ",stack_stained_clay8 = " + Integer.toString(playerdata.minestack.stained_clay8) //追加
				+ ",stack_stained_clay12 = " + Integer.toString(playerdata.minestack.stained_clay12) //追加
				+ ",stack_stained_clay14 = " + Integer.toString(playerdata.minestack.stained_clay14) //追加
				+ ",stack_emerald = " + Integer.toString(playerdata.minestack.emerald) //追加
				+ ",stack_redstone = " + Integer.toString(playerdata.minestack.redstone) //追加
				+ ",stack_diamond = " + Integer.toString(playerdata.minestack.diamond) //追加
				+ ",stack_clay = " + Integer.toString(playerdata.minestack.clay) //追加
				+ ",stack_glowstone = " + Integer.toString(playerdata.minestack.glowstone) //追加
				+ ",stack_dirt1 = " + Integer.toString(playerdata.minestack.dirt1)
				+ ",stack_dirt2 = " + Integer.toString(playerdata.minestack.dirt2)
				+ ",stack_mycel = " + Integer.toString(playerdata.minestack.mycel)
				+ ",stack_snow_block = " + Integer.toString(playerdata.minestack.snow_block)
				+ ",stack_ice = " + Integer.toString(playerdata.minestack.ice)
				+ ",stack_dark_oak_fence = " + Integer.toString(playerdata.minestack.dark_oak_fence)
				+ ",stack_mossy_cobblestone = " + Integer.toString(playerdata.minestack.mossy_cobblestone)
				+ ",stack_rails = " + Integer.toString(playerdata.minestack.rails)
				+ ",stack_exp_bottle = " + Integer.toString(playerdata.minestack.exp_bottle)
				+ ",stack_huge_mushroom_1 = " + Integer.toString(playerdata.minestack.huge_mushroom_1)
				+ ",stack_huge_mushroom_2 = " + Integer.toString(playerdata.minestack.huge_mushroom_2)
				+ ",stack_web = " + Integer.toString(playerdata.minestack.web)
				+ ",stack_string = " + Integer.toString(playerdata.minestack.string)
				+ ",stack_wood5 = " + Integer.toString(playerdata.minestack.wood5)
				+ ",stack_sapling = " + Integer.toString(playerdata.minestack.sapling)
				+ ",stack_sapling1 = " + Integer.toString(playerdata.minestack.sapling1)
				+ ",stack_sapling2 = " + Integer.toString(playerdata.minestack.sapling2)
				+ ",stack_sapling3 = " + Integer.toString(playerdata.minestack.sapling3)
				+ ",stack_sapling4 = " + Integer.toString(playerdata.minestack.sapling4)
				+ ",stack_sapling5 = " + Integer.toString(playerdata.minestack.sapling5)
				+ ",stack_leaves = " + Integer.toString(playerdata.minestack.leaves)
				+ ",stack_leaves1 = " + Integer.toString(playerdata.minestack.leaves1)
				+ ",stack_leaves2 = " + Integer.toString(playerdata.minestack.leaves2)
				+ ",stack_leaves3 = " + Integer.toString(playerdata.minestack.leaves3)
				+ ",stack_leaves_2 = " + Integer.toString(playerdata.minestack.leaves_2)
				+ ",stack_leaves_21 = " + Integer.toString(playerdata.minestack.leaves_21)
				+ ",stack_gachaimo = " + Integer.toString(playerdata.minestack.gachaimo)
				*/

				//サブホームのデータ
				command +=  ",homepoint_" + SeichiAssist.config.getServerNum() + " = '" + playerdata.SubHomeToString() + "'"
				//建築
				+ ",build_lv = " + Integer.toString(playerdata.build_lv_get())
				+ ",build_count = " + Integer.toString(playerdata.build_count_get())
				+ ",build_count_flg = " + Byte.toString(playerdata.build_count_flg_get());

				//実績のフラグ(BitSet)保存用変換処理
				long[] TitleArray = playerdata.TitleFlags.toLongArray();
		        String[] TitleNums = Arrays.stream(TitleArray).mapToObj(Long::toHexString).toArray(String[]::new);
		        String FlagString = String.join(",", TitleNums);
		        command += ",TitleFlags = '" + FlagString + "'" ;


		ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
		for(int i = 0; i < activeskilleffect.length ; i++){
			String sqlname = activeskilleffect[i].getsqlName();
			int num = activeskilleffect[i].getNum();
			Boolean flag = playerdata.activeskilldata.effectflagmap.get(num);
			command = command +
					"," + sqlname + " = " + Boolean.toString(flag);
		}
		ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
		for(int i = 0; i < premiumeffect.length ; i++){
			String sqlname = premiumeffect[i].getsqlName();
			int num = premiumeffect[i].getNum();
			Boolean flag = playerdata.activeskilldata.premiumeffectflagmap.get(num);
			command = command +
					"," + sqlname + " = " + Boolean.toString(flag);
		}

		//loginflag折る処理
		if(logoutflag){
			command = command +
					",loginflag = false";
		}

		//最後の処理
		command = command + " where uuid like '" + struuid + "'";

		boolean result;

		try {
			stmt.executeUpdate(command);
			result = true;
		}catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			result = false;
		}

 		if(isOnDisable){
 			//ondisableメソッドからの呼び出しの時の処理
 			if(result){
 				plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + playerdata.name + "のプレイヤーデータ保存完了");
 			}else{
 				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + playerdata.name + "のプレイヤーデータ保存失敗");
 			}
 			return;
 		}else if(/*i >= 4&&*/!result){
 			//諦める
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + playerdata.name + "のプレイヤーデータ保存失敗");
 			cancel();
 			return;
 		}else if(result){
 			//処理完了
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + playerdata.name + "のプレイヤーデータ保存完了");
 			cancel();
 			return;
 		}/*else{
 			//再試行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + playerdata.name + "のプレイヤーデータ保存再試行(" + (i+1) + "回目)");
 			i++;
 			return;
 		}*/
	}

}
