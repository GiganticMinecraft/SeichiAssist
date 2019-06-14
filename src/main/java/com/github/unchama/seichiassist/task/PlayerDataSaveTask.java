package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.MineStackObjectList;
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
public class PlayerDataSaveTask extends BukkitRunnable{
	final private SeichiAssist plugin = SeichiAssist.Companion.getInstance();
	final private DatabaseGateway databaseGateway = SeichiAssist.Companion.getDatabaseGateway();
	final private int serverId = SeichiAssist.Companion.getSeichiAssistConfig().getServerNum();
	final private boolean isOnDisable;
	final private boolean logoutflag;
	final PlayerData playerdata;

	/**
	 * @param _playerdata 保存するプレーヤーデータ
	 * @param _isondisable ondisableからの呼び出し時のみtrueにしておくフラグ
	 * @param _logoutflag loginflag折る時にtrueにしておくフラグ
	 */
	public PlayerDataSaveTask(PlayerData _playerdata, boolean _isondisable, boolean _logoutflag) {
		playerdata = _playerdata;
		isOnDisable = _isondisable;
		logoutflag = _logoutflag;
	}

	private void updatePlayerMineStack(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.getUuid().toString();
		for (final MineStackObj mineStackObj : MineStackObjectList.INSTANCE.getMinestacklist()) {
			final String iThObjectName = mineStackObj.getMineStackObjName();
			final long iThObjectAmount = playerdata.getMinestack().getStackedAmountOf(mineStackObj);

			final String updateCommand = "insert into seichiassist.mine_stack"
					+ "(player_uuid, object_name, amount) values "
					+ "('" + playerUuid + "', '" + iThObjectName + "', '" + iThObjectAmount +  "') "
					+ "on duplicate key update amount = values(amount)";

			stmt.executeUpdate(updateCommand);
		}
	}

	private void updateSubHome() throws SQLException {
		final String playerUuid = playerdata.getUuid().toString();
		for (Map.Entry<Integer, SubHome> subHomeEntry : playerdata.getSubHomeEntries()) {
			final int subHomeId = subHomeEntry.getKey();
			final SubHome subHome = subHomeEntry.getValue();
			final Location subHomeLocation = subHome.getLocation();

			final String template = "insert into seichiassist.sub_home"
					+ "(player_uuid,server_id,id,name,location_x,location_y,location_z,world_name) values "
					+ "(?,?,?,?,?,?,?,?) "
					+ "on duplicate key update "
					+ "name = values(name), "
					+ "location_x = values(location_x), "
					+ "location_y = values(location_y), "
					+ "location_z = values(location_z), "
					+ "world_name = values(world_name)";

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
		final String playerUuid = playerdata.getUuid().toString();

		// 既存データをすべてクリアする
		stmt.executeUpdate("delete from seichiassist.grid_template where designer_uuid = '" + playerUuid + "'");

		// 各グリッドテンプレートについてデータを保存する
		for (Map.Entry<Integer, GridTemplate> templateEntry : playerdata.getTemplateMap().entrySet()) {
			final int gridTemplateId = templateEntry.getKey();
			final GridTemplate gridTemplate = templateEntry.getValue();

			final String updateCommand = "insert into seichiassist.grid_template set " +
					"id = " + gridTemplateId + ", " +
					"designer_uuid = '" + playerUuid + "', " +
					"ahead_length = "  + gridTemplate.getAheadAmount()  + ", " +
					"behind_length = " + gridTemplate.getBehindAmount() + ", " +
					"right_length = "  + gridTemplate.getRightAmount()  + ", " +
					"left_length = "   + gridTemplate.getLeftAmount();

			stmt.executeUpdate(updateCommand);
		}
	}

	private void updateActiveSkillEffectUnlockState(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.getUuid().toString();
		ActiveSkillEffect[] activeSkillEffects = ActiveSkillEffect.values();
		final Set<ActiveSkillEffect> obtainedEffects = playerdata.getActiveskilldata().obtainedSkillEffects;

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
						+ "values ('" + playerUuid + "', '" + effectName + "')";

				stmt.executeUpdate(updateCommand);
			}
		}
	}

	private void updateActiveSkillPremiumEffectUnlockState(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.getUuid().toString();
		ActiveSkillPremiumEffect[] activeSkillPremiumEffects = ActiveSkillPremiumEffect.values();
		final Set<ActiveSkillPremiumEffect> obtainedEffects = playerdata.getActiveskilldata().obtainedSkillPremiumEffects;

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
						+ "values ('" + playerUuid + "', '" + effectName + "')";

				stmt.executeUpdate(updateCommand);
			}
		}
	}

	private void updatePlayerDataColumns(Statement stmt) throws SQLException {
		final String playerUuid = playerdata.getUuid().toString();

		//実績のフラグ(BitSet)保存用変換処理
		long[] titleArray = playerdata.getTitleFlags().toLongArray();
		String[] titleNums = Arrays.stream(titleArray).mapToObj(Long::toHexString).toArray(String[]::new);
		String flagString = String.join(",", titleNums);

		final String command = "update seichiassist.playerdata set"
				//名前更新処理
				+ " name = '" + playerdata.getName() + "'"

				//各種数値更新処理
				+ ",effectflag = " + playerdata.getEffectflag()
				+ ",minestackflag = " + playerdata.getMinestackflag()
				+ ",messageflag = " + playerdata.getMessageflag()
				+ ",activemineflagnum = " + playerdata.getActiveskilldata().mineflagnum
				+ ",assaultflag = " + playerdata.getActiveskilldata().assaultflag
				+ ",activeskilltype = " + playerdata.getActiveskilldata().skilltype
				+ ",activeskillnum = " + playerdata.getActiveskilldata().skillnum
				+ ",assaultskilltype = " + playerdata.getActiveskilldata().assaulttype
				+ ",assaultskillnum = " + playerdata.getActiveskilldata().assaultnum
				+ ",arrowskill = " + playerdata.getActiveskilldata().arrowskill
				+ ",multiskill = " + playerdata.getActiveskilldata().multiskill
				+ ",breakskill = " + playerdata.getActiveskilldata().breakskill
				+ ",fluidcondenskill = " + playerdata.getActiveskilldata().fluidcondenskill
				+ ",watercondenskill = " + playerdata.getActiveskilldata().watercondenskill
				+ ",lavacondenskill = " + playerdata.getActiveskilldata().lavacondenskill
				+ ",effectnum = " + playerdata.getActiveskilldata().effectnum
				+ ",gachapoint = " + playerdata.getGachapoint()
				+ ",gachaflag = " + playerdata.getGachaflag()
				+ ",level = " + playerdata.getLevel()
				+ ",rgnum = " + playerdata.getRgnum()
				+ ",totalbreaknum = " + playerdata.getTotalbreaknum()
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.getInventory()) + "'"
				+ ",playtick = " + playerdata.getPlaytick()
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + playerdata.getDispkilllogflag()
				+ ",worldguardlogflag = " + playerdata.getDispworldguardlogflag()

				+ ",multipleidbreakflag = " + playerdata.getMultipleidbreakflag()

				+ ",pvpflag = " + playerdata.getPvpflag()
				+ ",effectpoint = " + playerdata.getActiveskilldata().effectpoint
				+ ",mana = " + playerdata.getActiveskilldata().mana.getMana()
				+ ",expvisible = " + playerdata.getExpbar().isVisible()
				+ ",totalexp = " + playerdata.getTotalexp()
				+ ",expmarge = " + playerdata.getExpmarge()
				+ ",everysound = " + playerdata.getEverysoundflag()
				+ ",everymessage = " + playerdata.getEverymessageflag()

				+ ",displayTypeLv = " + playerdata.getDisplayTypeLv()
				+ ",displayTitle1No = " + playerdata.getDisplayTitle1No()
				+ ",displayTitle2No = " + playerdata.getDisplayTitle2No()
				+ ",displayTitle3No = " + playerdata.getDisplayTitle3No()
				+ ",giveachvNo = " + playerdata.getGiveachvNo()
				+ ",achvPointMAX = " + playerdata.getAchvPointMAX()
				+ ",achvPointUSE = " + playerdata.getAchvPointUSE()
				+ ",achvChangenum = " + playerdata.getAchvChangenum()
				+ ",starlevel = " + playerdata.getStarlevel()
				+ ",starlevel_Break = " + playerdata.getStarlevel_Break()
				+ ",starlevel_Time = " + playerdata.getStarlevel_Time()
				+ ",starlevel_Event = " + playerdata.getStarlevel_Event()

				+ ",lastcheckdate = '" + playerdata.getLastcheckdate() + "'"
				+ ",ChainJoin = " + playerdata.getChainJoin()
				+ ",TotalJoin = " + playerdata.getTotalJoin()
				+ ",LimitedLoginCount = " + playerdata.getLimitedLoginCount()

				//建築
				+ ",build_lv = " + playerdata.build_lv_get()
				+ ",build_count = " + playerdata.build_count_get().toString()
				+ ",build_count_flg = " + playerdata.build_count_flg_get()

				//投票
				+ ",canVotingFairyUse = " + playerdata.getUsingVotingFairy()
				+ ",newVotingFairyTime = '" + playerdata.VotingFairyTimeToString() + "'"
				+ ",VotingFairyRecoveryValue = " + playerdata.getVotingFairyRecoveryValue()
				+ ",hasVotingFairyMana = " + playerdata.getHasVotingFairyMana()
				+ ",toggleGiveApple = " + playerdata.getToggleGiveApple()
				+ ",toggleVotingFairy = " + playerdata.getToggleVotingFairy()
				+ ",p_apple = " + playerdata.getP_apple()

				//貢献度pt
				+ ",added_mana = " + playerdata.getAdded_mana()

				+ ",GBstage = " + playerdata.getGBstage()
				+ ",GBexp = " + playerdata.getGBexp()
				+ ",GBlevel = " + playerdata.getGBlevel()
				+ ",isGBStageUp = " + playerdata.isGBStageUp()
				+ ",TitleFlags = '" + flagString + "'"

				//正月イベント
				+ ",hasNewYearSobaGive = " + playerdata.getHasNewYearSobaGive()
				+ ",newYearBagAmount = " + playerdata.getNewYearBagAmount()

				//バレンタインイベント
				+ ",hasChocoGave = " + playerdata.getHasChocoGave()

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
				? ChatColor.GREEN + playerdata.getName() + "のプレイヤーデータ保存完了"
				: ChatColor.RED + playerdata.getName() + "のプレイヤーデータ保存失敗";
		plugin.getServer().getConsoleSender().sendMessage(resultMessage);
		if (!isOnDisable) cancel();
	}
}
