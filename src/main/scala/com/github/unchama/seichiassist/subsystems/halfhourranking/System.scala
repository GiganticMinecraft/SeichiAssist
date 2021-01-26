package com.github.unchama.seichiassist.subsystems.halfhourranking

import cats.Applicative
import cats.effect.{Async, Concurrent, Timer}
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, MinecraftServerThreadShift, SendMinecraftMessage}
import com.github.unchama.minecraft.bukkit.actions.{BroadcastBukkitMessage, SendBukkitMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.halfhourranking.application.AnnounceRankingRecord
import com.github.unchama.seichiassist.subsystems.halfhourranking.domain.RankingRecord
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  import scala.concurrent.duration._

  def backgroundProcess[
    F[_]
    : MinecraftServerThreadShift
    : Timer
    : Concurrent, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    implicit val sendBukkitMessage: SendMinecraftMessage[F, Player] = SendBukkitMessage[F]
    implicit val broadcastBukkitMessage: BroadcastMinecraftMessage[F] = BroadcastBukkitMessage[F]

    StreamExtra
      .foldGate(
        breakCountReadAPI.seichiAmountIncreases,
        fs2.Stream.awakeEvery[F](30.minutes),
        RankingRecord.empty[Player]
      )(record => (record.addCount _).tupled)
      .evalTap(AnnounceRankingRecord[F, Player](p => Applicative[F].pure(p.getDisplayName)))
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }
}
