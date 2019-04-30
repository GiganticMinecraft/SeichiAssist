package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GridTemplate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.subhome.SubHome;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.util.ActionStatus;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.github.unchama.util.ActionStatus.Fail;
import static com.github.unchama.util.ActionStatus.Ok;

/**
 * プレイヤーデータをDBに保存する処理(非同期で実行すること)
 * DBにセーブしたい値が増えた/減った場合は更新すること
 * @author unchama
 *
 */
public class PlayerDataSaveTaskRunnable extends BukkitRunnable{
	final private SeichiAssist plugin = SeichiAssist.instance;
	final private DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;
	final private int serverId = SeichiAssist.config.getServerNum();
	final private boolean isOnDisable;
	final private boolean logoutflag;
	final PlayerData playerdata;

	/**
	 * @param _playerdata 保存するプレーヤーデータ
	 * @param _isondisable ondisableからの呼び出し時のみtrueにしておくフラグ
	 * @param _logoutflag loginflag折る時にtrueにしておくフラグ
	 */
	public PlayerDataSaveTaskRunnable(PlayerData _playerdata,boolean _isondisable,boolean _logoutflag) {
		playerdata = _playerdata;
		isOnDisable = _isondisable;
		logoutflag = _logoutflag;
	}

	private void updatePlayerMineStack(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.uuid.toString();
		for (final MineStackObj mineStackObj : SeichiAssist.minestacklist) {
			final String iThObjectName = mineStackObj.getMineStackObjName();
			final long iThObjectAmount = playerdata.minestack.getStackedAmountOf(mineStackObj);

			final String updateCommand = "insert into seichiassist.mine_stack"
					+ "(player_uuid, object_name, amount) values "
					+ "('" + playerUuid + "', '" + iThObjectName + "', '" + iThObjectAmount +  "') "
					+ "on duplicate key update amount = values(amount)";

			stmt.executeUpdate(updateCommand);
		}
	}

	private void updateSubHome() throws SQLException {
		final String playerUuid = playerdata.uuid.toString();
		for (Map.Entry<Integer, SubHome> subHomeEntry : playerdata.getSubHomeEntries()) {
			final int subHomeId = subHomeEntry.getKey();
			final SubHome subHome = subHomeEntry.getValue();
			final Location subHomeLocation = subHome.getLocation();

			final String template = "insert into seichiassist.sub_home set "
					+ "player_id = ?, server_id = ?, id = ?, name = ?, location_x = ?, location_y = ?, "
					+ "location_z = ?, world_name = ?";

			try (PreparedStatement statement = databaseGateway.con.prepareStatement(template)) {
				statement.setString(1, playerUuid);
				statement.setInt(2, serverId);
				statement.setInt(3, subHomeId);
				statement.setString(4, subHome.name);
				statement.setInt(5, (int) subHomeLocation.getX());
				statement.setInt(6, (int) subHomeLocation.getY());
				statement.setInt(7, (int) subHomeLocation.getZ());
				statement.setString(8, subHomeLocation.getWorld().getName());

				statement.executeUpdate();
			}
		}
	}

	private void updateGridTemplate(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.uuid.toString();

		// 既存データをすべてクリアする
		stmt.executeUpdate("delete from seichiassist.grid_template where designer_uuid = " + playerUuid);

		// 各グリッドテンプレートについてデータを保存する
		for (Map.Entry<Integer, GridTemplate> templateEntry : playerdata.getTemplateMap().entrySet()) {
			final int gridTemplateId = templateEntry.getKey();
			final GridTemplate gridTemplate = templateEntry.getValue();

			final String updateCommand = "insert into seichiassist.grid_template set " +
					"id = " + gridTemplateId + ", " +
					"designer_uuid = " + playerUuid + ", " +
					"ahead_length = "  + gridTemplate.getAheadAmount()  + ", " +
					"behind_length = " + gridTemplate.getBehindAmount() + ", " +
					"right_length = "  + gridTemplate.getRightAmount()  + ", " +
					"left_length = "   + gridTemplate.getLeftAmount();

			stmt.executeUpdate(updateCommand);
		}
	}

	private void updateActiveSkillEffectUnlockState(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.uuid.toString();
		ActiveSkillEffect[] activeSkillEffects = ActiveSkillEffect.values();
		final Set<ActiveSkillEffect> obtainedEffects = playerdata.activeskilldata.obtainedSkillEffects;

		final String removeCommand = "delete from "
				+ "seichiassist.unlocked_active_skill_effect "
				+ "where player_uuid like '" + playerUuid + "'";
		stmt.executeUpdate(removeCommand);

		for (final ActiveSkillEffect activeSkillEffect : activeSkillEffects) {
			String effectName = activeSkillEffect.getsqlName();
			boolean isEffectUnlocked = obtainedEffects.contains(activeSkillEffect);

			if (isEffectUnlocked) {
				final String updateCommand = "insert into "
						+ "seichiassist.unlocked_active_skill_effect(player_uuid, effect_name) "
						+ "values (" + playerUuid + ", " + effectName + ")";

				stmt.executeUpdate(updateCommand);
			}
		}
	}

	private void updateActiveSkillPremiumEffectUnlockState(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.uuid.toString();
		ActiveSkillPremiumEffect[] activeSkillPremiumEffects = ActiveSkillPremiumEffect.values();
		final Set<ActiveSkillPremiumEffect> obtainedEffects = playerdata.activeskilldata.obtainedSkillPremiumEffects;

		final String removeCommand = "delete from "
				+ "seichiassist.unlocked_active_skill_premium_effect where "
				+ "player_uuid like '" + playerUuid + "'";
		stmt.executeUpdate(removeCommand);

		for (final ActiveSkillPremiumEffect activeSkillPremiumEffect : activeSkillPremiumEffects) {
			String effectName = activeSkillPremiumEffect.getsqlName();
			boolean isEffectUnlocked = obtainedEffects.contains(activeSkillPremiumEffect);

			if (isEffectUnlocked) {
				final String updateCommand = "insert into "
						+ "seichiassist.unlocked_active_skill_premium_effect(player_uuid, effect_name) "
						+ "values (" + playerUuid + ", " + effectName + ")";

				stmt.executeUpdate(updateCommand);
			}
		}
	}

	private void updatePlayerDataColumns(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.uuid.toString();

		//実績のフラグ(BitSet)保存用変換処理
		long[] titleArray = playerdata.TitleFlags.toLongArray();
		String[] titleNums = Arrays.stream(titleArray).mapToObj(Long::toHexString).toArray(String[]::new);
		String flagString = String.join(",", titleNums);

		final String command = "update seichiassist.playerdata set"
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

				+ ",displayTypeLv = " + playerdata.displayTypeLv
				+ ",displayTitle1No = " + playerdata.displayTitle1No
				+ ",displayTitle2No = " + playerdata.displayTitle2No
				+ ",displayTitle3No = " + playerdata.displayTitle3No
				+ ",giveachvNo = " + playerdata.giveachvNo
				+ ",achvPointMAX = " + playerdata.achvPointMAX
				+ ",achvPointUSE = " + playerdata.achvPointUSE
				+ ",achvChangenum = " + playerdata.achvChangenum
				+ ",starlevel = " + playerdata.starlevel
				+ ",starlevel_Break = " + playerdata.starlevel_Break
				+ ",starlevel_Time = " + playerdata.starlevel_Time
				+ ",starlevel_Event = " + playerdata.starlevel_Event

				+ ",lastcheckdate = '" + playerdata.lastcheckdate + "'"
				+ ",ChainJoin = " + playerdata.ChainJoin
				+ ",TotalJoin = " + playerdata.TotalJoin
				+ ",LimitedLoginCount = " + playerdata.LimitedLoginCount

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
				+ ",added_mana = " + playerdata.added_mana

				+ ",GBstage = " + playerdata.GBstage
				+ ",GBexp = " + playerdata.GBexp
				+ ",GBlevel = " + playerdata.GBlevel
				+ ",isGBStageUp = " + playerdata.isGBStageUp
				+ ",TitleFlags = '" + flagString + "'"

				//正月イベント
				+ ",hasNewYearSobaGive = " + playerdata.hasNewYearSobaGive
				+ ",newYearBagAmount = " + playerdata.newYearBagAmount

				//バレンタインイベント
				+ ",hasChocoGave = " + playerdata.hasChocoGave

				//loginflagを折る
				+ ", loginflag = " + !logoutflag

				+ " where uuid like '" + playerUuid + "'";

		stmt.executeUpdate(command);
	}

	private ActionStatus executeUpdate() {
		try {
			//sqlコネクションチェック
			databaseGateway.ensureConnection();

			//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
			final Statement localStatement = databaseGateway.con.createStatement();
			updatePlayerDataColumns(localStatement);
			updatePlayerMineStack(localStatement);
			updateGridTemplate(localStatement);
			updateSubHome();
			updateActiveSkillEffectUnlockState(localStatement);
			updateActiveSkillPremiumEffectUnlockState(localStatement);
			return Ok;
		} catch (SQLException exception) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exception.printStackTrace();
			return Fail;
		}
	}

	@Override
	public void run() {
		final String resultMessage = executeUpdate() == Ok
				? ChatColor.GREEN + playerdata.name + "のプレイヤーデータ保存完了"
				: ChatColor.RED + playerdata.name + "のプレイヤーデータ保存失敗";
		plugin.getServer().getConsoleSender().sendMessage(resultMessage);
		if (!isOnDisable) cancel();
	}
}
