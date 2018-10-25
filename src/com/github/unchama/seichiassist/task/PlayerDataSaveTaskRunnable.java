package com.github.unchama.seichiassist.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;

public class PlayerDataSaveTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.plugin;
	//private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Sql sql = SeichiAssist.sql;
	private static Config config = SeichiAssist.config;

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
				+ ",fluidcondenskill = " + Integer.toString(playerdata.activeskilldata.fluidcondenskill)
				+ ",watercondenskill = " + Integer.toString(playerdata.activeskilldata.watercondenskill)
				+ ",lavacondenskill = " + Integer.toString(playerdata.activeskilldata.lavacondenskill)
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
				+ ",everymessage = " + Boolean.toString(playerdata.everymessageflag)

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

				//サブホームのデータ
				command +=  ",homepoint_" + SeichiAssist.config.getServerNum() + " = '" + playerdata.SubHomeToString() + "'"
						+ ",subhome_name_" + SeichiAssist.config.getServerNum() + " = '" + playerdata.SubHomeNameToString() + "'"
				//建築
				+ ",build_lv = " + Integer.toString(playerdata.build_lv_get())
				+ ",build_count = " + playerdata.build_count_get().toString()
				+ ",build_count_flg = " + Byte.toString(playerdata.build_count_flg_get())

				//投票
				+ ",canVotingFairyUse = " + Boolean.toString(playerdata.canVotingFairyUse)
				+ ",newVotingFairyTime = '" + playerdata.VotingFairyTimeToString() + "'"
				+ ",VotingFairyRecoveryValue = " + Integer.toString(playerdata.VotingFairyRecoveryValue)
				+ ",hasVotingFairyMana = " + Integer.toString(playerdata.hasVotingFairyMana)

				//貢献度pt
				+",added_mana = " + Integer.toString(playerdata.added_mana)

				+",GBstage = " + Integer.toString(playerdata.GBstage)
				+",GBexp = " + Integer.toString(playerdata.GBexp)
				+",GBlevel = " + Integer.toString(playerdata.GBlevel)
				+",isGBStageUp = " + Boolean.toString(playerdata.isGBStageUp);

				//実績のフラグ(BitSet)保存用変換処理
				long[] TitleArray = playerdata.TitleFlags.toLongArray();
		        String[] TitleNums = Arrays.stream(TitleArray).mapToObj(Long::toHexString).toArray(String[]::new);
		        String FlagString = String.join(",", TitleNums);
		        command += ",TitleFlags = '" + FlagString + "'" ;

		//グリッド式保護設定保存
		for (int i = 0; i <= config.getTemplateKeepAmount() - 1; i++) {
			command += ",ahead_" + i + " = " + Integer.toString(playerdata.getTemplateMap().get(i).getAheadAmount());
			command += ",behind_" + i + " = " + Integer.toString(playerdata.getTemplateMap().get(i).getBehindAmount());
			command += ",right_" + i + " = " + Integer.toString(playerdata.getTemplateMap().get(i).getRightAmount());
			command += ",left_" + i + " = " + Integer.toString(playerdata.getTemplateMap().get(i).getLeftAmount());
		}

		//正月イベント
		command += ",hasNewYearSobaGive = " + Boolean.toString(playerdata.hasNewYearSobaGive);
		command += ",newYearBagAmount = " + Integer.toString(playerdata.newYearBagAmount);

		//バレンタインイベント
		command += ",hasChocoGave = " + Boolean.toString(playerdata.hasChocoGave);

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
