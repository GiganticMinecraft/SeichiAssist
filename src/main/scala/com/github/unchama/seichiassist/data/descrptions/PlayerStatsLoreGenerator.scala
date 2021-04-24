package com.github.unchama.seichiassist.data.descrptions

import cats.effect.IO
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiExpAmount, SeichiStarLevel}
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility
import com.github.unchama.seichiassist.subsystems.ranking.domain.SeichiRanking
import com.github.unchama.seichiassist.text.WarningsGenerator
import com.github.unchama.seichiassist.util.TypeConverter
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/05/05
 */
class PlayerStatsLoreGenerator(playerData: PlayerData,
                               seichiRanking: SeichiRanking,
                               seichiAmountData: SeichiAmountData,
                               expBarVisibility: BreakCountBarVisibility) {
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
      List(totalBreakAmountDescription()),
      rankingDescription().toList,
      rankingDiffDescription().toList,
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
   * 木の棒メニュー等で用いられる整地Lvの説明文
   * スターレベルを保持していたら,スターレベルも同時に表示します.
   */
  private def seichiLevelDescription(): String = {
    val starLevel = seichiAmountData.starLevelCorrespondingToExp
    val level = seichiAmountData.levelCorrespondingToExp.level

    if (starLevel != SeichiStarLevel.zero) {
      s"${AQUA}整地Lv:$level☆${starLevel.level}"
    } else {
      s"${AQUA}整地Lv:$level"
    }
  }

  /**
   * 次のレベルまでの残り必要整地量の説明文
   */
  private def levelProgressionDescription(): List[String] = {
    if (seichiAmountData.starLevelCorrespondingToExp == SeichiStarLevel.zero) {
      List(s"${AQUA}次のレベルまで:${seichiAmountData.levelProgress.expAmountToNextLevel.amount}")
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
  private def totalBreakAmountDescription(): String = s"${AQUA}総整地量：${seichiAmountData.expAmount.amount}"

  /**
   * ランキングの順位の説明文
   */
  private def rankingDescription(): Option[String] =
    seichiRanking
      .positionOf(targetPlayer.getName)
      .map { rank =>
        s"${GOLD}ランキング：${rank}位$GRAY(${seichiRanking.recordCount}人中)"
      }

  /**
   * 一つ前のランキングのプレイヤーとの整地量の差を表す説明文を返します.
   */
  private def rankingDiffDescription(): Option[String] =
    seichiRanking
      .positionAndRecordOf(targetPlayer.getName)
      .flatMap { case (position, record) =>
        if (position > 1) {
          val positionOneAbove = position - 1
          val recordOneAbove = seichiRanking.recordsWithPositions(positionOneAbove - 1)._2
          val difference =
            SeichiExpAmount.orderedMonus.subtractTruncate(
              recordOneAbove.seichiAmountData.expAmount,
              record.seichiAmountData.expAmount
            )
          Some(
            s"$AQUA${positionOneAbove}位(${recordOneAbove.playerName})との差：${difference.amount}"
          )
        } else
          None
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
  private def expBarDescription(): List[String] = {
    expBarVisibility match {
      case BreakCountBarVisibility.ShownSeichiBreakAmount =>
        List(
          s"${GREEN}整地量バーを表示",
          s"$DARK_RED${UNDERLINE}クリックで建築量バーを表示"
        )
      case BreakCountBarVisibility.ShownBuildAmount =>
        List(
          s"${GREEN}建築量バーを表示",
          s"$DARK_RED${UNDERLINE}クリックでGiganticBerserkバーを表示"
        )
      case BreakCountBarVisibility.ShownGiganticBerserkAmount =>
        List(
          s"${GREEN}GiganticBerserkバーを表示",
          s"$DARK_RED${UNDERLINE}クリックでバーを非表示"
        )
      case BreakCountBarVisibility.Hidden =>
        List(
          s"${RED}バーを非表示",
          s"$DARK_GREEN${UNDERLINE}クリックで整地量バーを表示"
        )
    }
  }
}
