package com.github.unchama.seichiassist.subsystems.manabar.application

import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.datarepository.definitions.FiberAdjoinedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.mana.domain.LevelCappedManaAmount
import io.chrisdavenport.log4cats.ErrorLogger

object ManaBarSynchronizationRepository {

  type BossBarWithPlayer[F[_], P] = MinecraftBossBar[F] {type Player = P}

  import cats.implicits._
  import cats.effect.implicits._

  def withContext[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]] : ErrorLogger,
    Player: HasUuid
  ](manaValues: fs2.Stream[F, (Player, LevelCappedManaAmount)])
   (createFreshBossBar: G[BossBarWithPlayer[F, Player]]): RepositoryDefinition[G, Player, _] = {
    FiberAdjoinedRepositoryDefinition.extending {
      RepositoryDefinition.SinglePhased
        .withSupplierAndTrivialFinalization[G, Player, BossBarWithPlayer[F, Player]](createFreshBossBar)
    }.withAnotherTappingAction { (player, pair) =>
      val (bossBar, promise) = pair

      val synchronization =
        manaValues
          .through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
          .evalTap(ManaBarManipulation.write[F](_, bossBar))

      val programToRunAsync =
        bossBar.players.add(player) >>
          StreamExtra.compileToRestartingStream[F, Nothing]("[ManaBarSynchronization]")(synchronization).start >>=
          promise.complete

      EffectExtra.runAsyncAndForget[F, G, Unit](programToRunAsync)
    }
  }
}
