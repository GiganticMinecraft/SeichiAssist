package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

import java.time.LocalDateTime

class FairyPlayerJoinGreeter(
  implicit fairyPersistence: FairyPersistence[IO],
  fairySpeech: FairySpeech[IO, Player]
) extends Listener {

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    val player = e.getPlayer
    val uuid = player.getUniqueId
    val eff = for {
      _ <- fairyPersistence.createPlayerData(uuid)
      isUsing <- fairyPersistence.isFairyUsing(uuid)
      endTime <- fairyPersistence.fairyEndTime(uuid)
    } yield {
      if (isUsing) {
        if (endTime.get.endTimeOpt.get.isBefore(LocalDateTime.now())) {
          // 終了時間が今よりも過去だったとき(つまり有効時間終了済み)
          player.sendMessage(s"$LIGHT_PURPLE${BOLD}妖精は何処かへ行ってしまったようだ...")
          fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = false).unsafeRunSync()
        } else {
          // まだ終了時間ではない(つまり有効時間内)
          implicit val ioCE: ConcurrentEffect[IO] =
            IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)

          fairySpeech.welcomeBack(player).unsafeRunSync()
        }
      } else SyncIO.unit
    }
    eff.unsafeRunSync()
  }

}
