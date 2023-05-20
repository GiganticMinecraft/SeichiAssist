package com.github.unchama.seichiassist.task.global

import cats.effect.{IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import org.bukkit.Bukkit

import scala.concurrent.duration.FiniteDuration

object PlayerDataRecalculationRoutine {

  def apply()(
    implicit onMainThread: OnMinecraftServerThread[IO],
    context: RepeatingTaskContext
  ): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      if (SeichiAssist.DEBUG) 10.seconds else 1.minute
    }

    val routineOnMainThread = SyncIO {
      import scala.jdk.CollectionConverters._

      // オンラインプレイヤーの人数を取得
      val onlinePlayers = Bukkit.getServer.getOnlinePlayers.asScala

      // プレイヤーマップに記録されているすべてのplayerdataについての処理
      for (player <- onlinePlayers) {
        val playerData = SeichiAssist.playermap(player.getUniqueId)

        // 総プレイ時間更新
        playerData.updatePlayTick()

        import SeichiAchievement._
        import cats.implicits._

        /*
         * 実績解除判定
         */
        autoUnlockedAchievements
          .filterNot(achievement => playerData.TitleFlags.contains(achievement.id))
          .map { achievement =>
            achievement.asUnlockable.shouldUnlockFor(player).map((achievement.id, _))
          }
          .toList
          .sequence
          .map(_.flatMap {
            case (achievementId, true) => Some(achievementId)
            case _                     => None
          })
          .flatMap(unlockTargets =>
            IO {
              playerData.TitleFlags.addAll(unlockTargets)
              unlockTargets.map("実績No" + _ + "が解除されました！おめでとうございます！").foreach(player.sendMessage)
            }
          )
          .unsafeRunSync()

        // GiganticBerserk
        playerData.GBcd = 0

      }
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(
      getRepeatInterval,
      onMainThread.runAction(routineOnMainThread)
    )
  }
}
