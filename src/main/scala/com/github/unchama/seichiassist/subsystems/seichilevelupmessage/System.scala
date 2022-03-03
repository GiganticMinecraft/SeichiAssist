package com.github.unchama.seichiassist.subsystems.seichilevelupmessage

import cats.effect.Async
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.seichilevelupmessage.domain.MessageTable
import io.chrisdavenport.log4cats.ErrorLogger

object System {

  def backgroundProcess[F[_]: Async: SendMinecraftMessage[*[_], Player]: ErrorLogger, G[
    _
  ], Player](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    StreamExtra.compileToRestartingStream("[SeichiLevelUpMessage]") {
      breakCountReadAPI.seichiLevelUpdates.evalMap {
        case (player, diff) =>
          SendMinecraftMessage[F, Player].list(player, MessageTable.messagesOnDiff(diff))
      }
    }
  }
}
