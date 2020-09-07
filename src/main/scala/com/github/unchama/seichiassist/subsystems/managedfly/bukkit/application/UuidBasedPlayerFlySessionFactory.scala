package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.application

import java.util.UUID

import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems.managedfly.application.PlayerFlySessionFactory
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus}
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

class UuidBasedPlayerFlySessionFactory[
  AsyncContext[_] : Timer : Concurrent : MinecraftServerThreadShift
](val playerUuid: UUID, val expConsumptionAmount: Int) extends PlayerFlySessionFactory[AsyncContext] {

  lazy val getPlayer: AsyncContext[Player] = Sync[AsyncContext].delay {
    Bukkit.getServer.getPlayer(playerUuid)
  }

  import cats.implicits._

  private val ensureExp: Player => AsyncContext[Unit] = { player =>
    val expManager = new ExperienceManager(player)

    for {
      _ <- MinecraftServerThreadShift[AsyncContext].shift
      hasExp <- Sync[AsyncContext].delay {
        expManager.hasExp(expConsumptionAmount)
      }
      _ <- if (hasExp)
        Sync[AsyncContext].unit
      else
        Sync[AsyncContext].raiseError(PlayerExpNotEnough)
    } yield ()
  }

  private val consumeExp: Player => AsyncContext[Unit] = { player =>
    val expManager = new ExperienceManager(player)

    for {
      _ <- MinecraftServerThreadShift[AsyncContext].shift
      consumed <- Sync[AsyncContext].delay {
        if (expManager.hasExp(expConsumptionAmount)) {
          expManager.changeExp(-expConsumptionAmount)
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

  private val setFlyStatus: PlayerFlyStatus => Player => AsyncContext[Unit] = { status =>
    player =>
      val shouldBeFlying = status match {
        case Flying(_) => true
        case NotFlying => false
      }

      MinecraftServerThreadShift[AsyncContext].shift >> Sync[AsyncContext].delay {
        player.setAllowFlight(shouldBeFlying)
        player.setFlying(shouldBeFlying)
      }
  }

  private val sendMessages: List[String] => Player => AsyncContext[Unit] = { messages =>
    player =>
      Sync[AsyncContext].delay {
        player.sendMessage(messages.toArray)
      }
  }

  override val ensurePlayerExp: AsyncContext[Unit] = getPlayer >>= ensureExp

  override val consumePlayerExp: AsyncContext[Unit] = getPlayer >>= consumeExp

  override val synchronizeFlyStatus: PlayerFlyStatus => AsyncContext[Unit] = status => {
    getPlayer >>= setFlyStatus(status)
  }

  override val handleInterruptions: InternalException => AsyncContext[Unit] = { exception =>
    val messages = exception match {
      case PlayerExpNotEnough => List(
        s"${RED}fly効果の発動に必要な経験値が不足しているため、",
        s"${RED}fly効果を終了しました"
      )
      case FlyDurationExpired => List(
        s"${GREEN}fly効果が終了しました"
      )
    }

    getPlayer >>= sendMessages(messages)
  }

  // TODO: 放置判定が効くようにする
  override val isPlayerIdle: AsyncContext[Boolean] = Sync[AsyncContext].pure(false)
}
