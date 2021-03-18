package com.github.unchama.seichiassist.subsystems.seichilevelupmessage

import cats.effect.Async
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.seichilevelupmessage.domain.MessageTable

object System {

  import cats.implicits._

  def backgroundProcess[
    F[_] : Async : SendMinecraftMessage[*[_], Player],
    G[_],
    Player
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    breakCountReadAPI
      .seichiLevelUpdates
      .evalMap { case (player, diff) =>
        SendMinecraftMessage[F, Player].list(player, MessageTable.messagesOnDiff(diff))
      }
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never[Nothing])
  }
}
