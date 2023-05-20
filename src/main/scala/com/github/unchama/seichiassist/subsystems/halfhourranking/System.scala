package com.github.unchama.seichiassist.subsystems.halfhourranking

import cats.effect.{Concurrent, Timer}
import cats.{Applicative, Functor}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{
  BroadcastMinecraftMessage,
  OnMinecraftServerThread,
  SendMinecraftMessage
}
import com.github.unchama.minecraft.bukkit.actions.{BroadcastBukkitMessage, SendBukkitMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.halfhourranking.application.AnnounceRankingRecord
import com.github.unchama.seichiassist.subsystems.halfhourranking.domain.RankingRecord
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

  import scala.concurrent.duration._

  def backgroundProcess[F[_]: OnMinecraftServerThread: Timer: Concurrent: ErrorLogger, G[
    _
  ]: ContextCoercion[*[_], F]: Functor](
    implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]
  ): F[Nothing] = {
    implicit val sendBukkitMessage: SendMinecraftMessage[F, Player] = SendBukkitMessage[F]
    implicit val broadcastBukkitMessage: BroadcastMinecraftMessage[F] =
      BroadcastBukkitMessage[F]

    StreamExtra.compileToRestartingStream("[HalfHourRanking]") {
      breakCountReadAPI
        .batchedIncreases(30.minutes)
        .map(RankingRecord.apply)
        .evalTap(
          AnnounceRankingRecord[F, G, Player](breakCountReadAPI)(p =>
            Applicative[F].pure(p.getName)
          )
        )
    }
  }
}
