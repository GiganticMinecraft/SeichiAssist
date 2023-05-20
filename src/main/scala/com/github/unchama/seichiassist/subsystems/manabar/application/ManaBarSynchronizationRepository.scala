package com.github.unchama.seichiassist.subsystems.manabar.application

import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.datarepository.definitions.FiberAdjoinedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.mana.ManaReadApi
import io.chrisdavenport.log4cats.ErrorLogger

object ManaBarSynchronizationRepository {

  type BossBarWithPlayer[F[_], P] = MinecraftBossBar[F] { type Player = P }

  import cats.implicits._

  def withContext[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[
    G,
    *[_]
  ]: ErrorLogger, Player: HasUuid](
    manaApi: ManaReadApi[F, G, Player]
  )(createFreshBossBar: G[BossBarWithPlayer[F, Player]]): RepositoryDefinition[G, Player, _] = {
    FiberAdjoinedRepositoryDefinition
      .extending {
        RepositoryDefinition
          .Phased
          .SinglePhased
          .withSupplierAndTrivialFinalization[G, Player, BossBarWithPlayer[F, Player]](
            createFreshBossBar
          )
      }
      .withAnotherTappingAction { (player, pair) =>
        val (bossBar, promise) = pair

        val synchronization =
          fs2
            .Stream
            .eval(manaApi.readManaAmount(player))
            .translate(ContextCoercion.asFunctionK[G, F])
            .append(
              manaApi.manaAmountUpdates.through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
            )
            .evalTap(ManaBarManipulation.write[F](_, bossBar))

        val programToRunAsync =
          bossBar.players.add(player) >>
            Concurrent[F].start[Nothing] {
              StreamExtra.compileToRestartingStream("[ManaBarSynchronization]")(synchronization)
            } >>= promise.complete

        EffectExtra.runAsyncAndForget[F, G, Unit](programToRunAsync)
      }
  }
}
