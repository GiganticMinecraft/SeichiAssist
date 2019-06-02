package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GridTemplate;
import com.github.unchama.seichiassist.data.LimitedLoginEvent;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.util.MillisecondTimer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * プレイヤーデータロードを実施する処理(非同期で実行すること)
 * DBから読み込みたい値が増えた/減った場合は更新すること
 * @author unchama
 *
 */
public class PlayerDataLoadTask extends BukkitRunnable{

	private SeichiAssist plugin = SeichiAssist.instance;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;
	private static Config config = SeichiAssist.config;

	private LimitedLoginEvent LLE = new LimitedLoginEvent();

	private Player p;
	PlayerData playerdata;
	final UUID uuid;
	private final String stringUuid;
	private boolean flag;
	private int i;
	private String db;
	private MillisecondTimer timer;

	public PlayerDataLoadTask(PlayerData playerData) {
		timer = MillisecondTimer.getInitializedTimerInstance();
		db = SeichiAssist.config.getDB();
		p = Bukkit.getPlayer(playerData.getUuid());
		playerdata = playerData;
		uuid = playerData.getUuid();
		stringUuid = uuid.toString().toLowerCase();
		flag = true;
		i = 0;
	}

	private void updateLoginInfo(Statement stmt) throws SQLException {
		final String loginInfoUpdateCommand = "update "
				+ db + "." + DatabaseConstants.PLAYERDATA_TABLENAME + " "
				+ "set loginflag = true, "
				+ "lastquit = cast(now() as datetime) "
				+ "where uuid like '" + stringUuid + "'";

		stmt.executeUpdate(loginInfoUpdateCommand);
	}

	private void loadSubHomeData(Statement stmt) throws SQLException {
		final String subHomeDataQuery = "select * from "
				+ db + "." + DatabaseConstants.SUB_HOME_TABLENAME + " where "
				+ "player_uuid like '" + stringUuid + "' and "
				+ "server_id = " + config.getServerNum();

		try (ResultSet resultSet = stmt.executeQuery(subHomeDataQuery)) {
			while (resultSet.next()) {
				final int subHomeId = resultSet.getInt("id");
				final String subHomeName = resultSet.getString("name");
				final int locationX = resultSet.getInt("location_x");
				final int locationY = resultSet.getInt("location_y");
				final int locationZ = resultSet.getInt("location_z");
				final String worldName = resultSet.getString("world_name");

				final World world = Bukkit.getWorld(worldName);
				final Location location = new Location(world,locationX, locationY, locationZ);

				playerdata.setSubHomeLocation(location, subHomeId);
				playerdata.setSubHomeName(subHomeName, subHomeId);
			}
		}
	}

	private void loadMineStack(Statement stmt) throws SQLException {
		final String mineStackDataQuery = "select * from "
				+ db + "." + DatabaseConstants.MINESTACK_TABLENAME + " where "
				+ "player_uuid like '" + stringUuid + "'";

		/* TODO これはここにあるべきではない
         * 格納可能なアイテムのリストはプラグインインスタンスの中に動的に持たれるべきで、
         * そのリストをラップするオブジェクトに同期された形でこのオブジェクトがもたれるべきであり、
         * ロードされるたびに再計算されるべきではない
         */
		final Map<String, MineStackObj> nameObjectMappings;
        {
            final HashMap<String, MineStackObj> resultMap = new HashMap<>();

            SeichiAssist
                    .minestacklist
                    .forEach(object -> resultMap.put(object.getMineStackObjName(), object));

            nameObjectMappings = resultMap;
        }

		try (ResultSet resultSet = stmt.executeQuery(mineStackDataQuery)) {
			while (resultSet.next()) {
				final String objectName = resultSet.getString("object_name");
				final long objectAmount = resultSet.getLong("amount");

				final MineStackObj mineStackObj = nameObjectMappings.get(objectName);

				playerdata.getMinestack().setStackedAmountOf(mineStackObj, objectAmount);
			}
		}
	}

	private void loadGridTemplate(Statement stmt) throws SQLException {
		final String gridTemplateDataQuery = "select * from "
				+ db + "." + DatabaseConstants.GRID_TEMPLATE_TABLENAME + " where "
				+ "designer_uuid like '" + stringUuid + "'";

		try (ResultSet resultSet = stmt.executeQuery(gridTemplateDataQuery)) {
			while (resultSet.next()) {
				final int templateId = resultSet.getInt("id");

				final int aheadLength = resultSet.getInt("ahead_length");
				final int behindLength = resultSet.getInt("behind_length");
				final int rightLength = resultSet.getInt("right_length");
				final int leftLength = resultSet.getInt("left_length");

				final GridTemplate template = new GridTemplate(aheadLength, behindLength, rightLength, leftLength);

				playerdata.getTemplateMap().put(templateId, template);
			}
		}
	}

    private void loadSkillEffectUnlockState(Statement stmt) throws SQLException {
        final String unlockedSkillEffectQuery = "select * from "
                + db + "." + DatabaseConstants.SKILL_EFFECT_TABLENAME + " where "
                + "player_uuid like '" + stringUuid + "'";

        try (ResultSet resultSet = stmt.executeQuery(unlockedSkillEffectQuery)) {
            while (resultSet.next()) {
                final String effectName = resultSet.getString("effect_name");

                final ActiveSkillEffect effect = ActiveSkillEffect.fromSqlName(effectName);
                playerdata.getActiveskilldata().obtainedSkillEffects.add(effect);
            }
        }
    }

    private void loadSkillPremiumEffectUnlockState(Statement stmt) throws SQLException {
        final String unlockedSkillEffectQuery = "select * from "
                + db + "." + DatabaseConstants.SKILL_PREMIUM_EFFECT_TABLENAME + " where "
                + "player_uuid like '" + stringUuid + "'";

        try (ResultSet resultSet = stmt.executeQuery(unlockedSkillEffectQuery)) {
            while (resultSet.next()) {
                final String effectName = resultSet.getString("effect_name");

                final ActiveSkillPremiumEffect effect = ActiveSkillPremiumEffect.fromSqlName(effectName);
                playerdata.getActiveskilldata().obtainedSkillPremiumEffects.add(effect);
            }
        }
	}

	private void loadPlayerData(Statement stmt) throws SQLException, IOException {
		//playerdataをsqlデータから得られた値で更新
		final String command = "select * from " + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME
				+ " where uuid like '" + stringUuid + "'";

		try (final ResultSet rs = stmt.executeQuery(command)) {
			while (rs.next()) {
				//各種数値
				playerdata.setLoaded(true);
				playerdata.setEffectflag(rs.getInt("effectflag"));
				playerdata.setMinestackflag(rs.getBoolean("minestackflag"));
				playerdata.setMessageflag(rs.getBoolean("messageflag"));
				playerdata.getActiveskilldata().mineflagnum = rs.getInt("activemineflagnum");
				playerdata.getActiveskilldata().assaultflag = rs.getBoolean("assaultflag");
				playerdata.getActiveskilldata().skilltype = rs.getInt("activeskilltype");
				playerdata.getActiveskilldata().skillnum = rs.getInt("activeskillnum");
				playerdata.getActiveskilldata().assaulttype = rs.getInt("assaultskilltype");
				playerdata.getActiveskilldata().assaultnum = rs.getInt("assaultskillnum");
				playerdata.getActiveskilldata().arrowskill = rs.getInt("arrowskill");
				playerdata.getActiveskilldata().multiskill = rs.getInt("multiskill");
				playerdata.getActiveskilldata().breakskill = rs.getInt("breakskill");
				playerdata.getActiveskilldata().fluidcondenskill = rs.getInt("fluidcondenskill");
				playerdata.getActiveskilldata().watercondenskill = rs.getInt("watercondenskill");
				playerdata.getActiveskilldata().lavacondenskill = rs.getInt("lavacondenskill");
				playerdata.getActiveskilldata().effectnum = rs.getInt("effectnum");
				playerdata.setGachapoint(rs.getInt("gachapoint"));
				playerdata.setGachaflag(rs.getBoolean("gachaflag"));
				playerdata.setLevel(rs.getInt("level"));
				playerdata.setNumofsorryforbug(rs.getInt("numofsorryforbug"));
				playerdata.setRgnum(rs.getInt("rgnum"));
				playerdata.setInventory(BukkitSerialization.fromBase64forPocket(rs.getString("inventory")));
				playerdata.setDispkilllogflag(rs.getBoolean("killlogflag"));
				playerdata.setDispworldguardlogflag(rs.getBoolean("worldguardlogflag"));

				playerdata.setMultipleidbreakflag(rs.getBoolean("multipleidbreakflag"));

				playerdata.setPvpflag(rs.getBoolean("pvpflag"));
				playerdata.setTotalbreaknum(rs.getLong("totalbreaknum"));
				playerdata.setPlaytick(rs.getInt("playtick"));
				playerdata.setP_givenvote(rs.getInt("p_givenvote"));
				playerdata.getActiveskilldata().effectpoint = rs.getInt("effectpoint");
				playerdata.getActiveskilldata().premiumeffectpoint = rs.getInt("premiumeffectpoint");
				//マナの情報
				playerdata.getActiveskilldata().mana.setMana(rs.getDouble("mana"));
				playerdata.getExpbar().setVisible(rs.getBoolean("expvisible"));

				playerdata.setTotalexp(rs.getInt("totalexp"));

				playerdata.setExpmarge(rs.getByte("expmarge"));
				playerdata.setShareinv((!("".equals(rs.getString("shareinv"))) && rs.getString("shareinv") != null));
				playerdata.setEverysoundflag(rs.getBoolean("everysound"));
				playerdata.setEverymessageflag(rs.getBoolean("everymessage"));

				playerdata.setSelectHomeNum(0);
				playerdata.setSetHomeNameNum(0);
				playerdata.setSubHomeNameChange(false);

				//実績、二つ名の情報
				playerdata.setDisplayTypeLv(rs.getBoolean("displayTypeLv"));
				playerdata.setDisplayTitle1No(rs.getInt("displayTitle1No"));
				playerdata.setDisplayTitle2No(rs.getInt("displayTitle2No"));
				playerdata.setDisplayTitle3No(rs.getInt("displayTitle3No"));
				playerdata.setP_vote_forT(rs.getInt("p_vote"));
				playerdata.setGiveachvNo(rs.getInt("giveachvNo"));
				playerdata.setAchvPointMAX(rs.getInt("achvPointMAX"));
				playerdata.setAchvPointUSE(rs.getInt("achvPointUSE"));
				playerdata.setAchvChangenum(rs.getInt("achvChangenum"));
				playerdata.setAchvPoint((playerdata.getAchvPointMAX() + (playerdata.getAchvChangenum() * 3)) - playerdata.getAchvPointUSE());

				//スターレベルの情報
				playerdata.setStarlevel(rs.getInt("starlevel"));
				playerdata.setStarlevel_Break(rs.getInt("starlevel_Break"));
				playerdata.setStarlevel_Time(rs.getInt("starlevel_Time"));
				playerdata.setStarlevel_Event(rs.getInt("starlevel_Event"));

				//期間限定ログインイベント専用の累計ログイン日数
				playerdata.setLimitedLoginCount(rs.getInt("LimitedLoginCount"));

				//連続・通算ログインの情報、およびその更新
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				if (rs.getString("lastcheckdate").equals("") || rs.getString("lastcheckdate") == null) {
					playerdata.setLastcheckdate(sdf.format(cal.getTime()));
				} else {
					playerdata.setLastcheckdate(rs.getString("lastcheckdate"));
				}
				playerdata.setChainJoin(rs.getInt("ChainJoin"));
				playerdata.setTotalJoin(rs.getInt("TotalJoin"));
				if (playerdata.getChainJoin() == 0) {
					playerdata.setChainJoin(1);
				}
				if (playerdata.getTotalJoin() == 0) {
					playerdata.setTotalJoin(1);
				}

				try {
					Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
					Date LastDate = sdf.parse(playerdata.getLastcheckdate());
					long TodayLong = TodayDate.getTime();
					long LastLong = LastDate.getTime();

					long datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24);
					if (datediff > 0) {
						LLE.getLastcheck(playerdata.getLastcheckdate());
						playerdata.setTotalJoin(playerdata.getTotalJoin() + 1);
						if (datediff == 1) {
							playerdata.setChainJoin(playerdata.getChainJoin() + 1);
						} else {
							playerdata.setChainJoin(1);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				playerdata.setLastcheckdate(sdf.format(cal.getTime()));

				//連続投票の更新
				String lastvote = rs.getString("lastvote");
				if ("".equals(lastvote) || lastvote == null) {
					playerdata.setChainVote(0);
				} else {
					try {
						Date TodayDate = sdf.parse(sdf.format(cal.getTime()));
						Date LastDate = sdf.parse(lastvote);
						long TodayLong = TodayDate.getTime();
						long LastLong = LastDate.getTime();

						long datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24);
						if (datediff <= 1 || datediff >= 0) {
							playerdata.setChainVote(rs.getInt("chainvote"));
						} else {
							playerdata.setChainVote(0);
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
					playerdata.setTitleFlags(TitleFlags);
				} catch (NullPointerException e) {
					playerdata.setTitleFlags(new BitSet(10000));
					playerdata.getTitleFlags().set(1);
				}

				//建築
				playerdata.build_lv_set(rs.getInt("build_lv"));
				playerdata.build_count_set(new BigDecimal(rs.getString("build_count")));
				playerdata.build_count_flg_set(rs.getByte("build_count_flg"));

				//マナ妖精
				playerdata.setUsingVotingFairy(rs.getBoolean("canVotingFairyUse"));
				playerdata.setVotingFairyRecoveryValue(rs.getInt("VotingFairyRecoveryValue"));
				playerdata.setHasVotingFairyMana(rs.getInt("hasVotingFairyMana"));
				playerdata.setToggleGiveApple(rs.getInt("toggleGiveApple"));
				playerdata.setToggleVotingFairy(rs.getInt("toggleVotingFairy"));
				playerdata.SetVotingFairyTime(rs.getString("newVotingFairyTime"), p);
				playerdata.setP_apple(rs.getLong("p_apple"));


				playerdata.setContribute_point(rs.getInt("contribute_point"));
				playerdata.setAdded_mana(rs.getInt("added_mana"));

				playerdata.setGBstage(rs.getInt("GBstage"));
				playerdata.setGBexp(rs.getInt("GBexp"));
				playerdata.setGBlevel(rs.getInt("GBlevel"));
				playerdata.setGBStageUp(rs.getBoolean("isGBStageUp"));
				playerdata.setAnniversary(rs.getBoolean("anniversary"));

				// 1周年記念
				if (playerdata.getAnniversary()) {
					p.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary");
					p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
				}

				//正月イベント用
				playerdata.setHasNewYearSobaGive(rs.getBoolean("hasNewYearSobaGive"));
				playerdata.setNewYearBagAmount(rs.getInt("newYearBagAmount"));

				//バレンタインイベント用
				playerdata.setHasChocoGave(rs.getBoolean("hasChocoGave"));
			}
		}
	}

	@Override
	public void run() {
		//対象プレイヤーがオフラインなら処理終了
		if(SeichiAssist.instance.getServer().getPlayer(uuid) == null){
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "はオフラインの為取得処理を中断");
			cancel();
			return;
		}
		//sqlコネクションチェック
		databaseGateway.ensureConnection();

		final Statement stmt;
		//同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
		try {
			stmt = databaseGateway.con.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
			cancel();
			return;
		}

 		//ログインフラグの確認を行う
		final String table = SeichiAssist.PLAYERDATA_TABLENAME;
		final String loginFlagSelectionQuery = "select loginflag from " +
				db + "." + table + " " +
				"where uuid = '" + stringUuid + "'";
		try (ResultSet rs = stmt.executeQuery(loginFlagSelectionQuery)) {
			while (rs.next()) {	flag = rs.getBoolean("loginflag"); }
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();
			cancel();
			return;
		}

 		if(i >= 4 && flag) {
 			//強制取得実行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "のplayerdata強制取得実行");
 			cancel();
 		} else if(!flag) {
 			//flagが折れてたので普通に取得実行
 			cancel();
 		} else {
 			//再試行
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + p.getName() + "のloginflag=false待機…(" + (i+1) + "回目)");
 			i++;
 			return;
 		}

		try {
			loadPlayerData(stmt);
			updateLoginInfo(stmt);
			loadGridTemplate(stmt);
			loadMineStack(stmt);
			loadSkillEffectUnlockState(stmt);
			loadSkillPremiumEffectUnlockState(stmt);
			loadSubHomeData(stmt);
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();

			//コネクション復活後にnewインスタンスのデータで上書きされるのを防止する為削除しておく
			playermap.remove(uuid);

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
		if(playerdata.getAdded_mana() < playerdata.getContribute_point()){
			int addMana;
			addMana = playerdata.getContribute_point() - playerdata.getAdded_mana();
			playerdata.isContribute(p, addMana);
		}
		timer.sendLapTimeMessage(ChatColor.GREEN + p.getName() + "のプレイヤーデータ読込完了");
	}
}
