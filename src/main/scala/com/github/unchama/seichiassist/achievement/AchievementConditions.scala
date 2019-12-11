package com.github.unchama.seichiassist.achievement

import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate, LocalTime, Month}

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

import scala.concurrent.duration.FiniteDuration

object AchievementConditions {
  def playerDataPredicate(predicate: PlayerData => IO[Boolean]): PlayerPredicate = { player =>
    IO { SeichiAssist.playermap(player.getUniqueId) }.flatMap(predicate)
  }

  def hasUnlocked(id: Int): PlayerPredicate = playerDataPredicate(d => IO { d.TitleFlags.contains(id) })

  def dependsOn[A: WithPlaceholder](id: Int, condition: AchievementCondition[A]): HiddenAchievementCondition[A] = {
    HiddenAchievementCondition(hasUnlocked(id), condition)
  }

  def brokenBlockRankingPosition_<=(n: Int): AchievementCondition[Int] = {
    val predicate = playerDataPredicate(d => IO { d.calcPlayerRank() <= n })

    AchievementCondition(predicate, "「整地神ランキング」" + _ + "位達成", n)
  }

  def placedBlockAmount_>=(amount: BigDecimal, localizedAmount: String): AchievementCondition[String] = {
    val predicate = playerDataPredicate(d => IO {
      val playerBuildCount: BigDecimal = d.buildCount.count

      playerBuildCount >= amount
    })

    AchievementCondition(predicate, "建築量が " + _ + "を超える", localizedAmount)
  }

  def brokenBlockAmount_>=(amount: Long, localizedAmount: String): AchievementCondition[String] = {
    val predicate = playerDataPredicate(d => IO { d.totalbreaknum >= amount })

    AchievementCondition(predicate, "整地量が " + _ + "を超える", localizedAmount)
  }

  def totalPlayTime_>=(duration: FiniteDuration, localizedDuration: String): AchievementCondition[String] = {
    import com.github.unchama.concurrent.syntax._

    val predicate = playerDataPredicate(d => IO { d.playTick.ticks.toMillis >= duration.toMillis })

    AchievementCondition(predicate, "参加時間が " + _ + " を超える", localizedDuration)
  }

  def consecutiveLoginDays_>=(n: Int): AchievementCondition[String] = {
    val predicate = playerDataPredicate(d => IO { d.loginStatus.consecutiveLoginDays >= n })

    AchievementCondition(predicate, "連続ログイン日数が " + _ + " に到達", s"${n}日")
  }

  def totalPlayedDays_>=(n: Int): AchievementCondition[String] = {
    val predicate = playerDataPredicate(d => IO { d.loginStatus.totalLoginDay >= n })

    AchievementCondition(predicate, "通算ログイン日数が " + _ + " に到達", s"${n}日")
  }

  def voteCount_>=(n: Int): AchievementCondition[String] = {
    val predicate = playerDataPredicate(d => IO { d.p_vote_forT >= n })

    AchievementCondition(predicate, "JMS投票数が " + _ + " を超える", n.toString)
  }

  def playedIn(month: Month): AchievementCondition[String] = {
    val predicate: PlayerPredicate = _ => IO { LocalDate.now().getMonth == month }

    AchievementCondition(predicate, _ + "月にプレイ", month.getValue.toString)
  }

  def playedOn(month: Month, dayOfMonth: Int, dateSpecification: String): AchievementCondition[String] = {
    val predicate: PlayerPredicate = _ =>
      IO {
        LocalDate.now().getMonth == month &&
          LocalDate.now().getDayOfMonth == dayOfMonth
      }

    AchievementCondition(predicate, _ + "にプレイ", dateSpecification)
  }

  def playedOn(month: Month, weekOfMonth: Int, dayOfWeek: DayOfWeek, dateSpecification: String): AchievementCondition[String] = {
    val predicate: PlayerPredicate = _ =>
      IO {
        val now = LocalDate.now()

        // 現在の月の第[[weekOfMonth]][[dayOfWeek]]曜日
        val dayOfWeekOnWeekOfTheMonth = now.`with`(TemporalAdjusters.dayOfWeekInMonth(weekOfMonth, dayOfWeek))

        now.getMonth == month && now == dayOfWeekOnWeekOfTheMonth
      }

    AchievementCondition(predicate, _ + "にプレイ", dateSpecification)
  }

  object SecretAchievementConditions {
    val conditionFor8001: HiddenAchievementCondition[Unit] = {
      val shouldDisplay: PlayerPredicate = { _ =>
        IO {
          LocalTime.now().getSecond == 0 && LocalTime.now().getMinute == 0
        }
      }

      val shouldUnlock: PlayerPredicate = { player =>
        IO {
          val stackIsAsRequired: ItemStack => Boolean = { stack =>
            import scala.util.chaining._

            stack != null &&
              stack.getType == Material.SKULL_ITEM &&
              stack.getItemMeta.asInstanceOf[SkullMeta].pipe(meta =>
                meta.hasOwner && meta.getOwningPlayer.getName == "unchama"
              )
          }

          import com.github.unchama.menuinventory.syntax._
          (0 until 4.chestRows.slotCount)
            .map(player.getInventory.getItem(_))
            .forall(stackIsAsRequired)
        }
      }

      HiddenAchievementCondition(shouldDisplay, AchievementCondition(shouldUnlock, _ => "器を満たす奇跡の少女", ()))
    }

    val conditionFor8002: HiddenAchievementCondition[Unit] = {
      val shouldDisplay: PlayerPredicate =
        playerDataPredicate(p => IO {
          p.totalbreaknum % 1000000L == 0L && p.totalbreaknum != 0L
        })

      val unlockCondition: PlayerPredicate =
        playerDataPredicate(p => IO {
          p.totalbreaknum % 1000000L == 777777L
        })

      HiddenAchievementCondition(shouldDisplay, AchievementCondition(unlockCondition, _ => "[[[[[[LuckyNumber]]]]]]", ()))
    }

    val unlockConditionFor8003: PlayerPredicate =
      playerDataPredicate(p => IO {
        p.playTick % (20 * 60 * 60 * 8) <= (20 * 60)
      })

    val conditionFor8003: HiddenAchievementCondition[Unit] = {
      val shouldDisplay: PlayerPredicate =
        playerDataPredicate(p => IO {
          p.playTick % (20 * 60 * 60) >= 0 && p.playTick % (20 * 60 * 60) <= (20 * 60)
        })

      HiddenAchievementCondition(shouldDisplay, AchievementCondition(_ => IO.pure(false), _ => "定時分働いたら記録を確認！", ()))
    }
  }
}
