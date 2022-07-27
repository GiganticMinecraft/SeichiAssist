package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service

import cats.data
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO, LiftIO, Sync}
import com.github.unchama.generic.syntax.KleisliCombine
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpawnGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySpawnRequestResult
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound

class FairySpawnService[F[_]: ConcurrentEffect, G[_], Player](
  player: Player,
  gateway: FairySpawnGateway[F]
) {

  def spawn(implicit breakCountAPI: BreakCountAPI[G, F, Player]): F[FairySpawnRequestResult] =
    Sync[F].delay {
      val playerLevel = breakCountAPI
        .seichiAmountDataRepository(player)
        .read
        .toIO
        .unsafeRunSync()
        .levelCorrespondingToExp
        .level

      if (playerLevel < 10)
        return Sync[F].pure(FairySpawnRequestResult.NotEnoughSeichiLevel)


    }

  private def spawnFailedEffect(message: String): F[Unit] = {
    LiftIO[IO].liftIO {
      SequentialEffect(
        MessageEffect(message),
        FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
      ).followedBy(data.Kleisli {player => })
    }.
  }

}
