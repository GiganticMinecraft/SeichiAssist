package com.github.unchama.seichiassist.data.descrptions

import com.github.unchama.seichiassist.LevelThresholds
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.text.Templates
import com.github.unchama.seichiassist.text.WarningsGenerator
import com.github.unchama.seichiassist.util.TypeConverter
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/05/05
 */
class PlayerStatsLoreGenerator(private val playerData: PlayerData) {
  private val targetPlayer: Player = Bukkit.getPlayer(playerData.uuid)

  /**
   * Player統計のLoreを返します.
   */
  @Suppress("RedundantSuspendModifier")
  suspend fun computeLore(): List<String> {
    return with(WarningsGenerator(targetPlayer)) {
      listOf(
          listOf(seichiLevelDescription()),
          levelProgressionDescription(),
          noRewardsOutsideSeichiWorld,
          passiveSkillDescription(),
          listOf(
              totalBreakAmountDescription(),
              rankingDescription()
          ),
          rankingDiffDescription(),
          listOf(
              totalLoginTimeDescrpition(),
              totalLoginDaysDescrption(),
              totalChainLoginDaysDescription()
          ),
          totalChainVoteDaysDescription(),
          Templates.playerInfoDescrpition,
          expBarDescription()
      ).flatten()
    }
  }

  /**
   * 木の棒メニュー等で用いられる整地レベルの説明文
   * スターレベルを保持していたら,スターレベルも同時に表示します.
   */
  private fun seichiLevelDescription(): String {
    val starLevel = playerData.totalStarLevel
    val level = playerData.level

    return if (starLevel <= 0) {
      "${AQUA}整地レベル:$level"
    } else {
      "${AQUA}整地レベル:$level☆$starLevel"
    }
  }

  /**
   * 次のレベルまでの残り必要整地量の説明文
   */
  private fun levelProgressionDescription(): List<String> {
    return if (playerData.level < LevelThresholds.levelExpThresholds.size) {
      //TODO:この計算は,ここにあるべきではない.
      val expRequiredToLevelUp = LevelThresholds.levelExpThresholds[playerData.level] - playerData.totalbreaknum

      listOf("${AQUA}次のレベルまで:$expRequiredToLevelUp")
    } else {
      emptyList()
    }
  }

  /**
   * パッシブスキルの説明文
   */
  private fun passiveSkillDescription(): List<String> {
    return listOf(
        "${DARK_GRAY}パッシブスキル効果：",
        "${DARK_GRAY}1ブロック整地ごとに",
        "$DARK_GRAY${PlayerData.passiveSkillProbability}%の確率で",
        "$DARK_GRAY${playerData.getPassiveExp()}のマナを獲得"
    )
  }

  /**
   * 総整地量の説明文
   */
  private fun totalBreakAmountDescription(): String = "${AQUA}総整地量：${playerData.totalbreaknum}"

  /**
   * ランキングの順位の説明文
   */
  private fun rankingDescription(): String =
      "${GOLD}ランキング：${playerData.calcPlayerRank()}位$GRAY(${SeichiAssist.ranklist.size}人中)"

  /**
   * 一つ前のランキングのプレイヤーとの整地量の差を表す説明文を返します.
   */
  private fun rankingDiffDescription(): List<String> =
      if (playerData.calcPlayerRank() != 1) {
        val playerRanking = playerData.calcPlayerRank()
        val rankData = SeichiAssist.ranklist[playerRanking - 2]
        val differenceToTheBest = rankData.totalbreaknum - playerData.totalbreaknum

        listOf("$AQUA${playerRanking - 1}位(${rankData.name})との差：$differenceToTheBest")
      } else {
        emptyList()
      }

  /**
   * 総ログイン時間の説明文
   */
  private fun totalLoginTimeDescrpition(): String =
      "${GRAY}総ログイン時間：${TypeConverter.toTimeString(TypeConverter.toSecond(playerData.playTick))}"

  /**
   * 通算ログイン日数の説明文
   */
  private fun totalLoginDaysDescrption(): String = "${GRAY}通算ログイン日数：${playerData.loginStatus.totalLoginDay}日"

  /**
   * 連続ログイン日数の説明文
   */
  private fun totalChainLoginDaysDescription(): String = "${GRAY}連続ログイン日数：${playerData.loginStatus.chainLoginDay}日"

  /**
   * 連続投票日数の説明文.
   */
  private fun totalChainVoteDaysDescription(): List<String> =
      if (playerData.ChainVote > 0)
        listOf("連続投票日数：${playerData.ChainVote}日")
      else
        emptyList()

  /**
   * Expバーの説明文.
   */
  private fun expBarDescription(): List<String> {
    return if (playerData.expbar.isVisible) {
      listOf(
          "${GREEN}整地量バーを表示",
          "$DARK_RED${UNDERLINE}クリックで非表示"
      )
    } else {
      listOf(
          "${RED}整地量バーを非表示",
          "$DARK_GREEN${UNDERLINE}クリックで表示"
      )
    }
  }
}
