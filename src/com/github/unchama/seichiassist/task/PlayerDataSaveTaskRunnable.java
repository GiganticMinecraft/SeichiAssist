package com.github.unchama.seichiassist.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

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
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.plugin.sql;

	final String table = SeichiAssist.PLAYERDATA_TABLENAME;

	PlayerData playerdata;
	String command;
	int i;
	//ondisableからの呼び出し時のみtrueにしておくフラグ
	boolean isOnDisable;
	public static String exc;
	Statement stmt = null;
	ResultSet rs = null;

	public PlayerDataSaveTaskRunnable(PlayerData _playerdata,boolean _isondisable) {
		command = "";
		i = 0;
		playerdata = _playerdata;
		//ondisableからの呼び出し時のみtrueにしておくフラグ
		isOnDisable = _isondisable;
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

		command = "update " + table
				+ " set"

				//名前更新処理
				+ " name = '" + playerdata.name + "'"

				//各種数値更新処理
				+ ",effectflag = " + Boolean.toString(playerdata.effectflag)
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
				+ ",totalbreaknum = " + Integer.toString(playerdata.totalbreaknum)
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
				+ ",playtick = " + Integer.toString(playerdata.playtick)
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + Boolean.toString(playerdata.dispkilllogflag)
				+ ",pvpflag = " + Boolean.toString(playerdata.pvpflag)
				+ ",effectpoint = " + Integer.toString(playerdata.activeskilldata.effectpoint)
				+ ",mana = " + Double.toString(playerdata.activeskilldata.mana.getMana())

				//MineStack機能の数値更新処理
				+ ",stack_dirt = " + Integer.toString(playerdata.minestack.dirt)
				+ ",stack_gravel = " + Integer.toString(playerdata.minestack.gravel)
				+ ",stack_cobblestone = " + Integer.toString(playerdata.minestack.cobblestone)
				+ ",stack_stone = " + Integer.toString(playerdata.minestack.stone)
				+ ",stack_sand = " + Integer.toString(playerdata.minestack.sand)
				+ ",stack_sandstone = " + Integer.toString(playerdata.minestack.sandstone)
				+ ",stack_netherrack = " + Integer.toString(playerdata.minestack.netherrack)
				+ ",stack_ender_stone = " + Integer.toString(playerdata.minestack.ender_stone)
				+ ",stack_grass = " + Integer.toString(playerdata.minestack.grass)
				+ ",stack_quartz = " + Integer.toString(playerdata.minestack.quartz)
				+ ",stack_quartz_ore = " + Integer.toString(playerdata.minestack.quartz_ore)
				+ ",stack_soul_sand = " + Integer.toString(playerdata.minestack.soul_sand)
				+ ",stack_magma = " + Integer.toString(playerdata.minestack.magma)
				+ ",stack_coal = " + Integer.toString(playerdata.minestack.coal)
				+ ",stack_coal_ore = " + Integer.toString(playerdata.minestack.coal_ore)
				+ ",stack_iron_ore = " + Integer.toString(playerdata.minestack.iron_ore)
				+ ",stack_packed_ice = " + Integer.toString(playerdata.minestack.packed_ice);


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
 		}else if(i >= 4&&!result){
 			//諦める
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + playerdata.name + "のプレイヤーデータ保存失敗");
 			cancel();
 			return;
 		}else if(result){
 			//処理完了
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + playerdata.name + "のプレイヤーデータ保存完了");
 			cancel();
 			return;
 		}else{
 			//再試行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + playerdata.name + "のプレイヤーデータ保存再試行(" + (i+1) + "回目)");
 			i++;
 			return;
 		}
	}

}
