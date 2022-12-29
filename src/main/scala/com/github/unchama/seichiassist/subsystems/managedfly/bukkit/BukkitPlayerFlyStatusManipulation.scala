package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import cats.data.Kleisli
import cats.effect.{Concurrent, Sync, SyncIO, Timer}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import com.github.unchama.seichiassist.subsystems.managedfly.application._
import com.github.unchama.seichiassist.subsystems.managedfly.domain._
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import org.bukkit.ChatColor.{GRAY, GREEN, RED}
import org.bukkit.entity.Player

class BukkitPlayerFlyStatusManipulation[AsyncContext[
  _
]: Timer: Concurrent: OnMinecraftServerThread](
  implicit configuration: SystemConfiguration,
  idleTimeAPI: IdleTimeAPI[AsyncContext, Player]
) extends PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]] {

  import cats.implicits._

  /**
   * 飛行に必要な経験値をプレーヤーが持っていることを保証するアクション。 このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  override val ensurePlayerExp: Kleisli[AsyncContext, Player, Unit] = Kleisli { player =>
    val expManager = new ExperienceManager(player)

    for {
      hasExp <- OnMinecraftServerThread[AsyncContext].runAction(SyncIO {
        expManager.hasExp(configuration.expConsumptionAmount)
      })
      _ <-
        if (hasExp) Sync[AsyncContext].unit
        else Sync[AsyncContext].raiseError(PlayerExpNotEnough)
    } yield ()
  }

  /**
   * 飛行に必要な経験値をプレーヤーに消費させるアクション。 このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  override val consumePlayerExp: Kleisli[AsyncContext, Player, Unit] = Kleisli { player =>
    val expManager = new ExperienceManager(player)

    for {
      consumed <- OnMinecraftServerThread[AsyncContext].runAction(SyncIO {
        if (expManager.hasExp(configuration.expConsumptionAmount)) {
          expManager.changeExp(-configuration.expConsumptionAmount)
          true
        } else {
          false
        }
      })
      _ <-
        if (consumed)
          Sync[AsyncContext].unit
        else
          Sync[AsyncContext].raiseError(PlayerExpNotEnough)
    } yield ()
  }

  /**
   * プレーヤーがアイドル状態であるかを判定するアクション。
   */
  override val isPlayerIdle: Kleisli[AsyncContext, Player, IdleStatus] = Kleisli {
    player: Player =>
      for {
        currentIdleMinute <- idleTimeAPI.currentIdleMinute(player)
      } yield {
        if (currentIdleMinute.minutes >= 10) Idle
        else HasMovedRecently
      }
  }

  /**
   * プレーヤーの飛行状態を[[PlayerFlyStatus]]に基づいてセットするアクション。
   */
  override val synchronizeFlyStatus: PlayerFlyStatus => Kleisli[AsyncContext, Player, Unit] = {
    status =>
      Kleisli { player =>
        val shouldBeFlying = status match {
          case Flying(_) => true
          case NotFlying => false
        }

        OnMinecraftServerThread[AsyncContext].runAction(SyncIO {
          player.setAllowFlight(shouldBeFlying)
          player.setFlying(shouldBeFlying)
        })
      }
  }

  private val sendMessages: List[String] => Kleisli[AsyncContext, Player, Unit] = { messages =>
    Kleisli { player =>
      Sync[AsyncContext].delay {
        player.sendMessage(messages.toArray)
      }
    }
  }

  /**
   * 放置状態も考慮して、プレーヤーに残飛行時間の通知を送るアクション。
   */
  override val notifyRemainingDuration
    : (IdleStatus, RemainingFlyDuration) => Kleisli[AsyncContext, Player, Unit] =
    (status, duration) => {
      val message = status match {
        case Idle => s"${GRAY}放置時間中のflyは無期限で継続中です(経験値は消費しません)"
        case HasMovedRecently =>
          duration match {
            case RemainingFlyDuration.Infinity =>
              s"${GREEN}fly効果は無期限で継続中です"
            case RemainingFlyDuration.PositiveMinutes(minutes) =>
              s"${GREEN}fly効果はあと${minutes}分です"
          }
      }

      sendMessages(List(message))
    }

  /**
   * [[InternalInterruption]] に対応して、プレーヤーへセッションが終了することを通知するアクション。
   */
  override val sendNotificationsOnInterruption
    : InternalInterruption => Kleisli[AsyncContext, Player, Unit] =
    exception => {
      val messages = exception match {
        case PlayerExpNotEnough =>
          List(s"${RED}fly効果の発動に必要な経験値が不足しているため、", s"${RED}fly効果を終了しました")
        case FlyDurationExpired => List(s"${GREEN}fly効果が終了しました")
      }

      sendMessages(messages)
    }
}
