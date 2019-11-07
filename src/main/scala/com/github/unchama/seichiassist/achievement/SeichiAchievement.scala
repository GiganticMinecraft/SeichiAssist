package com.github.unchama.seichiassist.achievement

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import enumeratum.{Enum, EnumEntry}
import org.bukkit.entity.Player

sealed abstract class SeichiAchievement extends EnumEntry

object SeichiAchievement extends Enum[SeichiAchievement] {
  type PlayerPredicate = Player => IO[Boolean]
  type ParameterizedText[A] = A => String

  case class AutoUnlocked[A](id: Int, condition: AchievementCondition[A]) extends SeichiAchievement
  case class ManuallyUnlocked[A](id: Int, condition: AchievementCondition[A]) extends SeichiAchievement
  case class HiddenAtFirst[A](id: Int, condition: HiddenAchievementCondition[A]) extends SeichiAchievement
  case class GrantedByConsole[A](id: Int, condition: String) extends SeichiAchievement

  object Conditions {
    def hasUnlocked(id: Int): PlayerPredicate = { player => IO {
      SeichiAssist.playermap(player.getUniqueId).TitleFlags.contains(id)
    } }

    def dependsOn[A: WithPlaceholder](id: Int, condition: AchievementCondition[A]): HiddenAchievementCondition[A] = {
      HiddenAchievementCondition(hasUnlocked(id), condition)
    }

    def brokenBlockRankingPosition_<=(n: Int): AchievementCondition[Int] = {
      val predicate = { player: Player => IO {
        SeichiAssist.playermap(player.getUniqueId).calcPlayerRank() <= n
      } }

      AchievementCondition(predicate, "「整地神ランキング」" + _ + "位達成", n)
    }

    def brokenBlockAmount_>=(amount: Long, localizedAmount: String): AchievementCondition[String] = {
      val predicate = { player: Player => IO {
        SeichiAssist.playermap(player.getUniqueId).totalbreaknum >= amount
      } }

      AchievementCondition(predicate, "条件：整地量が " + _ + "を超える", localizedAmount)
    }
  }

  import Conditions._
  import WithPlaceholder._

  // 整地神ランキング
  object No_1001 extends AutoUnlocked(1001, brokenBlockRankingPosition_<=(1))
  object No_1002 extends AutoUnlocked(1002, brokenBlockRankingPosition_<=(5))
  object No_1003 extends AutoUnlocked(1003, brokenBlockRankingPosition_<=(27))
  object No_1004 extends AutoUnlocked(1004, brokenBlockRankingPosition_<=(50))
  object No_1005 extends AutoUnlocked(1005, brokenBlockRankingPosition_<=(750))
  object No_1006 extends AutoUnlocked(1006, brokenBlockRankingPosition_<=(1000))
  object No_1007 extends AutoUnlocked(1007, brokenBlockRankingPosition_<=(2500))
  object No_1008 extends AutoUnlocked(1008, brokenBlockRankingPosition_<=(5000))
  object No_1009 extends AutoUnlocked(1009, brokenBlockRankingPosition_<=(10000))
  object No_1010 extends AutoUnlocked(1010, brokenBlockRankingPosition_<=(100))
  object No_1011 extends AutoUnlocked(1011, brokenBlockRankingPosition_<=(250))
  object No_1012 extends AutoUnlocked(1012, brokenBlockRankingPosition_<=(500))

  // 整地量
  object No_3001 extends HiddenAtFirst(3001, dependsOn(3002, brokenBlockAmount_>=(2147483646L, "int型の壁")))
  object No_3002 extends AutoUnlocked(3002, brokenBlockAmount_>=(1000000000L, "10億"))
  object No_3003 extends AutoUnlocked(3003, brokenBlockAmount_>=(500000000L, "5億"))
  object No_3004 extends AutoUnlocked(3004, brokenBlockAmount_>=(100000000L, "1億"))
  object No_3005 extends AutoUnlocked(3005, brokenBlockAmount_>=(50000000L, "5000万"))
  object No_3006 extends AutoUnlocked(3006, brokenBlockAmount_>=(10000000L, "1000万"))
  object No_3007 extends AutoUnlocked(3007, brokenBlockAmount_>=(5000000L, "500万"))
  object No_3008 extends AutoUnlocked(3008, brokenBlockAmount_>=(1000000L, "100万"))
  object No_3009 extends AutoUnlocked(3009, brokenBlockAmount_>=(500000L, "50万"))
  object No_3010 extends AutoUnlocked(3010, brokenBlockAmount_>=(100000L, "10万"))
  object No_3011 extends AutoUnlocked(3011, brokenBlockAmount_>=(10000L, "1万"))
  object No_3012 extends HiddenAtFirst(3012, dependsOn(3001, brokenBlockAmount_>=(3000000000L, "30億")))
  object No_3013 extends HiddenAtFirst(3013, dependsOn(3001, brokenBlockAmount_>=(4000000000L, "40億")))
  object No_3014 extends HiddenAtFirst(3014, dependsOn(3001, brokenBlockAmount_>=(5000000000L, "50億")))
  object No_3015 extends HiddenAtFirst(3015, dependsOn(3014, brokenBlockAmount_>=(6000000000L, "60億")))
  object No_3016 extends HiddenAtFirst(3016, dependsOn(3015, brokenBlockAmount_>=(7000000000L, "70億")))
  object No_3017 extends HiddenAtFirst(3017, dependsOn(3016, brokenBlockAmount_>=(8000000000L, "80億")))
  object No_3018 extends HiddenAtFirst(3018, dependsOn(3017, brokenBlockAmount_>=(9000000000L, "90億")))
  object No_3019 extends HiddenAtFirst(3019, dependsOn(3018, brokenBlockAmount_>=(10000000000L, "100億")))


  val values: IndexedSeq[SeichiAchievement] = findValues
}
