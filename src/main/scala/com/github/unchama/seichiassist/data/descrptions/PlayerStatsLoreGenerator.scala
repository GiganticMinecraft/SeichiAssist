package com.github.unchama.seichiassist.data.descrptions

import cats.effect.IO
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.text.WarningsGenerator
import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.seichiassist.{LevelThresholds, SeichiAssist}
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/05/05
 */
class PlayerStatsLoreGenerator(private val playerData: PlayerData) {
  private val targetPlayer: Player = Bukkit.getPlayer(playerData.uuid)

  /**
   * Player統計のLoreを返します.
   */
  def computeLore(): IO[List[String]] = IO {
    val generator = new WarningsGenerator(targetPlayer)

    import generator._

    List(
      List(seichiLevelDescription()),
      levelProgressionDescription(),
      noRewardsOutsideSeichiWorld,
      passiveSkillDescription(),
      List(
        totalBreakAmountDescription(),
        rankingDescription()
      ),
      rankingDiffDescription(),
      List(
        totalLoginTimeDescrpition(),
        totalLoginDaysDescrption(),
        totalChainLoginDaysDescription()
      ),
      totalChainVoteDaysDescription(),
      List(
        s"$DARK_GRAY※1分毎に更新",
        s"${GREEN}統計データは",
        s"${GREEN}各サバイバルサーバー間で",
        s"${GREEN}共有されます"
      ),
      expBarDescription()
    ).flatten
  }

  /**
   * 木の棒メニュー等で用いられる整地レベルの説明文
   * スターレベルを保持していたら,スターレベルも同時に表示します.
   */
  private def seichiLevelDescription(): String = {
    val starLevel = playerData.totalStarLevel
    val level = playerData.level

    if (starLevel <= 0) {
      s"${AQUA}整地レベル:$level"
    } else {
      s"${AQUA}整地レベル:$level☆$starLevel"
    }
  }

  /**
   * 次のレベルまでの残り必要整地量の説明文
   */
  private def levelProgressionDescription(): List[String] = {
    if (playerData.level < LevelThresholds.levelExpThresholds.size) {
      //TODO:この計算は,ここにあるべきではない.
      val expRequiredToLevelUp = LevelThresholds.levelExpThresholds(playerData.level) - playerData.totalbreaknum

      List(s"${AQUA}次のレベルまで:$expRequiredToLevelUp")
    } else {
      Nil
    }
  }

  /**
   * パッシブスキルの説明文
   */
  private def passiveSkillDescription(): List[String] = {
    List(
      s"${DARK_GRAY}パッシブスキル効果：",
      s"${DARK_GRAY}1ブロック整地ごとに",
      s"$DARK_GRAY${PlayerData.passiveSkillProbability}%の確率で",
      s"$DARK_GRAY${playerData.getPassiveExp}のマナを獲得"
    )
  }

  /**
   * 総整地量の説明文
   */
  private def totalBreakAmountDescription(): String = s"${AQUA}総整地量：${playerData.totalbreaknum}"

  /**
   * ランキングの順位の説明文
   */
  private def rankingDescription(): String =
    s"${GOLD}ランキング：${playerData.calcPlayerRank()}位$GRAY(${SeichiAssist.ranklist.size}人中)"

  /**
   * 一つ前のランキングのプレイヤーとの整地量の差を表す説明文を返します.
   */
  private def rankingDiffDescription(): List[String] =
    if (playerData.calcPlayerRank() != 1) {
      val playerRanking = playerData.calcPlayerRank()
      val rankData = SeichiAssist.ranklist(playerRanking - 2)
      val differenceToTheBest = rankData.totalbreaknum - playerData.totalbreaknum

      List(s"$AQUA${playerRanking - 1}位(${rankData.name})との差：$differenceToTheBest")
    } else {
      Nil
    }

  /**
   * 総ログイン時間の説明文
   */
  private def totalLoginTimeDescrpition(): String =
    s"${GRAY}総ログイン時間：${TypeConverter.toTimeString(TypeConverter.toSecond(playerData.playTick))}"

  /**
   * 通算ログイン日数の説明文
   */
  private def totalLoginDaysDescrption(): String = s"${GRAY}通算ログイン日数：${playerData.loginStatus.totalLoginDay}日"

  /**
   * 連続ログイン日数の説明文
   */
  private def totalChainLoginDaysDescription(): String = s"${GRAY}連続ログイン日数：${playerData.loginStatus.consecutiveLoginDays}日"

  /**
   * 連続投票日数の説明文.
   */
  private def totalChainVoteDaysDescription(): List[String] =
    if (playerData.ChainVote > 0)
      List(s"$RESET${GRAY}連続投票日数：${playerData.ChainVote}日")
    else
      Nil

  /**
   * Expバーの説明文.
   */
  private def expBarDescription(): List[String] =
    if (playerData.settings.isExpBarVisible) List(
      s"${GREEN}整地量バーを表示",
      s"$DARK_RED${UNDERLINE}クリックで非表示"
    ) else List(
      s"${RED}整地量バーを非表示",
      s"$DARK_GREEN${UNDERLINE}クリックで表示"
    )
}
