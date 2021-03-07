package com.github.unchama.seichiassist.subsystems.halfhourranking

import cats.effect.{Async, Concurrent, Timer}
import cats.{Applicative, Functor}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, MinecraftServerThreadShift, SendMinecraftMessage}
import com.github.unchama.minecraft.bukkit.actions.{BroadcastBukkitMessage, SendBukkitMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.halfhourranking.application.AnnounceRankingRecord
import com.github.unchama.seichiassist.subsystems.halfhourranking.domain.RankingRecord
import org.bukkit.entity.Player

object System {

  import cats.implicits._
  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

  import scala.concurrent.duration._

  def backgroundProcess[
    F[_]
    : MinecraftServerThreadShift
    : Timer
    : Concurrent,
    G[_]
    : ContextCoercion[*[_], F]
    : Functor
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    implicit val sendBukkitMessage: SendMinecraftMessage[F, Player] = SendBukkitMessage[F]
    implicit val broadcastBukkitMessage: BroadcastMinecraftMessage[F] = BroadcastBukkitMessage[F]

    breakCountReadAPI
      .batchedIncreases(30.minutes)
      .map(RankingRecord.apply)
      .evalTap(AnnounceRankingRecord[F, G, Player](breakCountReadAPI)(p => Applicative[F].pure(p.getDisplayName)))
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }
}
