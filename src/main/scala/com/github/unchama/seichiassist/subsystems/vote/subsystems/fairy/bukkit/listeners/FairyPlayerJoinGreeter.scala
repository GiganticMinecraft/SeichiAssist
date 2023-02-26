package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners

import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.time.LocalDateTime

class FairyPlayerJoinGreeter(
  implicit fairyPersistence: FairyPersistence[IO],
  fairySpeech: FairySpeech[IO, Player],
  concurrentEffect: ConcurrentEffect[IO]
) extends Listener {

  import cats.implicits._

  @EventHandler(priority = EventPriority.HIGHEST)
  def onAsyncPlayerPreLogin(e: AsyncPlayerPreLoginEvent): Unit = {
    fairyPersistence.initializePlayerData(e.getUniqueId).unsafeRunAsyncAndForget()
  }

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    val player = e.getPlayer
    val uuid = player.getUniqueId
    val program = for {
      isUsing <- fairyPersistence.isFairyUsing(uuid)
      endTime <- fairyPersistence.fairyEndTime(uuid)
      isEnd <- IO(LocalDateTime.now()).map(now => endTime.exists(_.endTime.isBefore(now)))
      _ <- {
        fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = false) >> IO(
          player.sendMessage(s"$LIGHT_PURPLE${BOLD}妖精は何処かへ行ってしまったようだ...")
        )
        // 終了時間が今よりも過去だったとき(つまり有効時間終了済み)
      }.whenA(isUsing && isEnd)
      _ <- fairySpeech.welcomeBack(player).whenA(isUsing && !isEnd)
    } yield ()
    program.unsafeRunSync()
  }

}
