package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyUsingState
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.ChatColor._

import java.time.LocalDateTime

class FairyPlayerJoinGreeter(implicit fairyAPI: FairyAPI[IO, SyncIO, Player]) extends Listener {

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    val player = e.getPlayer
    val eff = for {
      usingState <- fairyAPI.fairyUsingState(player)
      endTime <- fairyAPI.fairyEndTime(player)
    } yield {
      if (usingState == FairyUsingState.Using) {
        if (endTime.get.endTimeOpt.get.isBefore(LocalDateTime.now())) {
          // 終了時間が今よりも過去だったとき(つまり有効時間終了済み)
          player.sendMessage(s"$LIGHT_PURPLE${BOLD}妖精は何処かへ行ってしまったようだ...")
          fairyAPI.updateFairyUsingState(player, FairyUsingState.NotUsing).unsafeRunSync()
        } else {
          // まだ終了時間ではない(つまり有効時間内)
          implicit val ioCE: ConcurrentEffect[IO] =
            IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)

          new FairySpeech[IO, SyncIO]().welcomeBack(player).unsafeRunSync()
        }
      } else SyncIO.unit
    }
    eff.unsafeRunSync()
  }

}
