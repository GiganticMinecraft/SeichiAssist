package com.github.unchama.seichiassist.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import com.github.unchama.seichiassist.*;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.DatabaseGateway;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BukkitSerialization;

/**
 * プレイヤーデータをDBに保存する処理(非同期で実行すること)
 * DBにセーブしたい値が増えた/減った場合は更新すること
 * @author unchama
 *
 */
public class PlayerDataSaveTaskRunnable extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.instance;
	private DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;
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
		databaseGateway.ensureConnection();
		try {
			stmt = databaseGateway.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		//引数のplayerdataをsqlにデータを送信
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command;

		command = "update " + db + "." + table
				+ " set"

				//名前更新処理
				+ " name = '" + playerdata.name + "'"

				//各種数値更新処理
				+ ",effectflag = " + playerdata.effectflag
				+ ",minestackflag = " + playerdata.minestackflag
				+ ",messageflag = " + playerdata.messageflag
				+ ",activemineflagnum = " + playerdata.activeskilldata.mineflagnum
				+ ",assaultflag = " + playerdata.activeskilldata.assaultflag
				+ ",activeskilltype = " + playerdata.activeskilldata.skilltype
				+ ",activeskillnum = " + playerdata.activeskilldata.skillnum
				+ ",assaultskilltype = " + playerdata.activeskilldata.assaulttype
				+ ",assaultskillnum = " + playerdata.activeskilldata.assaultnum
				+ ",arrowskill = " + playerdata.activeskilldata.arrowskill
				+ ",multiskill = " + playerdata.activeskilldata.multiskill
				+ ",breakskill = " + playerdata.activeskilldata.breakskill
				+ ",fluidcondenskill = " + playerdata.activeskilldata.fluidcondenskill
				+ ",watercondenskill = " + playerdata.activeskilldata.watercondenskill
				+ ",lavacondenskill = " + playerdata.activeskilldata.lavacondenskill
				+ ",effectnum = " + playerdata.activeskilldata.effectnum
				+ ",gachapoint = " + playerdata.gachapoint
				+ ",gachaflag = " + playerdata.gachaflag
				+ ",level = " + playerdata.level
				+ ",rgnum = " + playerdata.rgnum
				+ ",totalbreaknum = " + playerdata.totalbreaknum
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
				+ ",playtick = " + playerdata.playtick
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + playerdata.dispkilllogflag
				+ ",worldguardlogflag = " + playerdata.dispworldguardlogflag

				+ ",multipleidbreakflag = " + playerdata.multipleidbreakflag

				+ ",pvpflag = " + playerdata.pvpflag
				+ ",effectpoint = " + playerdata.activeskilldata.effectpoint
				+ ",mana = " + playerdata.activeskilldata.mana.getMana()
				+ ",expvisible = " + playerdata.expbar.isVisible()
				+ ",totalexp = " + playerdata.totalexp
				+ ",expmarge = " + playerdata.expmarge
				+ ",everysound = " + playerdata.everysoundflag
				+ ",everymessage = " + playerdata.everymessageflag

				+",displayTypeLv = " + playerdata.displayTypeLv
				+",displayTitle1No = " + playerdata.displayTitle1No
				+",displayTitle2No = " + playerdata.displayTitle2No
				+",displayTitle3No = " + playerdata.displayTitle3No
				+",giveachvNo = " + playerdata.giveachvNo
				+",achvPointMAX = " + playerdata.achvPointMAX
				+",achvPointUSE = " + playerdata.achvPointUSE
				+",achvChangenum = " + playerdata.achvChangenum
				+",starlevel = " + playerdata.starlevel
				+",starlevel_Break = " + playerdata.starlevel_Break
				+",starlevel_Time = " + playerdata.starlevel_Time
				+",starlevel_Event = " + playerdata.starlevel_Event

				+",lastcheckdate = '" + playerdata.lastcheckdate + "'"
				+",ChainJoin = " + playerdata.ChainJoin
				+",TotalJoin = " + playerdata.TotalJoin
				+",LimitedLoginCount = " + playerdata.LimitedLoginCount;

				//MineStack機能の数値更新処理

				//MineStack関連は全てfor文に変更
				if(SeichiAssist.minestack_sql_enable){
					for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
						command += ",stack_"+SeichiAssist.minestacklist.get(i).getMineStackObjName()+ " = "
							+ playerdata.minestack.getNum(i);
					}
				}

				//サブホームのデータ
				command +=  ",homepoint_" + SeichiAssist.config.getServerNum() + " = '" + playerdata.SubHomeToString() + "'"
						+ ",subhome_name_" + SeichiAssist.config.getServerNum() + " = '" + playerdata.SubHomeNameToString() + "'"
				//建築
				+ ",build_lv = " + playerdata.build_lv_get()
				+ ",build_count = " + playerdata.build_count_get().toString()
				+ ",build_count_flg = " + playerdata.build_count_flg_get()

				//投票
				+ ",canVotingFairyUse = " + playerdata.usingVotingFairy
				+ ",newVotingFairyTime = '" + playerdata.VotingFairyTimeToString() + "'"
				+ ",VotingFairyRecoveryValue = " + playerdata.VotingFairyRecoveryValue
				+ ",hasVotingFairyMana = " + playerdata.hasVotingFairyMana
				+ ",toggleGiveApple = " + playerdata.toggleGiveApple
				+ ",toggleVotingFairy = " + playerdata.toggleVotingFairy
				+ ",p_apple = " + playerdata.p_apple

				//貢献度pt
				+",added_mana = " + playerdata.added_mana

				+",GBstage = " + playerdata.GBstage
				+",GBexp = " + playerdata.GBexp
				+",GBlevel = " + playerdata.GBlevel
				+",isGBStageUp = " + playerdata.isGBStageUp;

				//実績のフラグ(BitSet)保存用変換処理
				long[] TitleArray = playerdata.TitleFlags.toLongArray();
				String[] TitleNums = Arrays.stream(TitleArray).mapToObj(Long::toHexString).toArray(String[]::new);
				String FlagString = String.join(",", TitleNums);
				command += ",TitleFlags = '" + FlagString + "'" ;

		//グリッド式保護設定保存
		for (int i = 0; i <= config.getTemplateKeepAmount() - 1; i++) {
			command += ",ahead_" + i + " = " + playerdata.getTemplateMap().get(i).getAheadAmount();
			command += ",behind_" + i + " = " + playerdata.getTemplateMap().get(i).getBehindAmount();
			command += ",right_" + i + " = " + playerdata.getTemplateMap().get(i).getRightAmount();
			command += ",left_" + i + " = " + playerdata.getTemplateMap().get(i).getLeftAmount();
		}

		//正月イベント
		command += ",hasNewYearSobaGive = " + playerdata.hasNewYearSobaGive;
		command += ",newYearBagAmount = " + playerdata.newYearBagAmount;

		//バレンタインイベント
		command += ",hasChocoGave = " + playerdata.hasChocoGave;

		ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
		for (final ActiveSkillEffect activeSkillEffect : activeskilleffect) {
			String sqlname = activeSkillEffect.getsqlName();
			int num = activeSkillEffect.getNum();
			boolean flag = playerdata.activeskilldata.effectflagmap.get(num);
			command = command +
					"," + sqlname + " = " + flag;
		}
		ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
		for (final ActiveSkillPremiumEffect activeSkillPremiumEffect : premiumeffect) {
			String sqlname = activeSkillPremiumEffect.getsqlName();
			int num = activeSkillPremiumEffect.getNum();
			boolean flag = playerdata.activeskilldata.premiumeffectflagmap.get(num);
			command = command +
					"," + sqlname + " = " + flag;
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
		}else if(/*i >= 4&&*/!result){
 			//諦める
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + playerdata.name + "のプレイヤーデータ保存失敗");
 			cancel();
		}else if(result){
 			//処理完了
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + playerdata.name + "のプレイヤーデータ保存完了");
 			cancel();
		}/*else{
 			//再試行
 			instance.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + playerdata.name + "のプレイヤーデータ保存再試行(" + (i+1) + "回目)");
 			i++;
 			return;
 		}*/
	}
}
