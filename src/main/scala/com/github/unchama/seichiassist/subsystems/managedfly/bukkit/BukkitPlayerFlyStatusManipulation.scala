package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import cats.data.Kleisli
import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.concurrent.{MinecraftServerThreadShift, NonServerThreadContextShift}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.managedfly.application._
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus}
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import org.bukkit.ChatColor.{GREEN, RED}
import org.bukkit.entity.Player

class BukkitPlayerFlyStatusManipulation[
  AsyncContext[_] : Timer : Concurrent : MinecraftServerThreadShift : NonServerThreadContextShift
](implicit configuration: SystemConfiguration)
  extends PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]] {

  import cats.implicits._

  /**
   * 飛行に必要な経験値をプレーヤーが持っていることを保証するアクション。
   * このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  override val ensurePlayerExp: Kleisli[AsyncContext, Player, Unit] = Kleisli { player =>
    val expManager = new ExperienceManager(player)

    for {
      _ <- MinecraftServerThreadShift[AsyncContext].shift
      hasExp <- Sync[AsyncContext].delay {
        expManager.hasExp(configuration.expConsumptionAmount)
      }
      _ <- NonServerThreadContextShift[AsyncContext].shift
      _ <- if (hasExp) Sync[AsyncContext].unit else Sync[AsyncContext].raiseError(PlayerExpNotEnough)
    } yield ()
  }
  /**
   * 飛行に必要な経験値をプレーヤーに消費させるアクション。
   * このアクションは [[PlayerExpNotEnough]] を `raiseError` してよい。
   */
  override val consumePlayerExp: Kleisli[AsyncContext, Player, Unit] = Kleisli { player =>
    val expManager = new ExperienceManager(player)

    for {
      _ <- MinecraftServerThreadShift[AsyncContext].shift
      consumed <- Sync[AsyncContext].delay {
        if (expManager.hasExp(configuration.expConsumptionAmount)) {
          expManager.changeExp(-configuration.expConsumptionAmount)
          true
        } else {
          false
        }
      }
      _ <- if (consumed)
        Sync[AsyncContext].unit
      else
        Sync[AsyncContext].raiseError(PlayerExpNotEnough)
    } yield ()
  }

  /**
   * プレーヤーがアイドル状態であるかを判定するアクション。
   */
  override val isPlayerIdle: Kleisli[AsyncContext, Player, Boolean] = Kleisli { player: Player =>
    Sync[AsyncContext].delay {
      SeichiAssist.playermap(player.getUniqueId).idleMinute >= 10
    }
  }

  /**
   * プレーヤーの飛行状態を[[PlayerFlyStatus]]に基づいてセットするアクション。
   */
  override val synchronizeFlyStatus: PlayerFlyStatus => Kleisli[AsyncContext, Player, Unit] = { status =>
    Kleisli { player =>
      val shouldBeFlying = status match {
        case Flying(_) => true
        case NotFlying => false
      }

      MinecraftServerThreadShift[AsyncContext].shift >> Sync[AsyncContext].delay {
        player.setAllowFlight(shouldBeFlying)
        player.setFlying(shouldBeFlying)
      }
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
   * [[InternalInterruption]] に対応して、プレーヤーへセッションが終了することを通知するアクション。
   */
  override val sendNotificationsOnInterruption: InternalInterruption => Kleisli[AsyncContext, Player, Unit] =
    exception => {
      val messages = exception match {
        case PlayerExpNotEnough => List(
          s"${RED}fly効果の発動に必要な経験値が不足しているため、",
          s"${RED}fly効果を終了しました"
        )
        case FlyDurationExpired => List(
          s"${GREEN}fly効果が終了しました"
        )
      }

      sendMessages(messages)
    }
}
