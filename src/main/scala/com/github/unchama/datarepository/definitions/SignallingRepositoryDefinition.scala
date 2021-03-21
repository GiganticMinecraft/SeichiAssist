package com.github.unchama.datarepository.definitions

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.AsymmetricSignallingRef
import com.github.unchama.minecraft.algebra.HasUuid
import fs2.Pipe

object SignallingRepositoryDefinition {

  import cats.implicits._

  def forPlayerTopic[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player: HasUuid, T
  ](publishSink: Pipe[F, (Player, T), Unit])
   (definition: RepositoryDefinition[G, Player, T]): RepositoryDefinition[G, Player, Ref[G, T]] = {
    definition.toTwoPhased.xmapWithPlayer { player =>
      initialValue =>
        AsymmetricSignallingRef[G, F, T](initialValue)
          .flatTap { ref =>
            EffectExtra.runAsyncAndForget[F, G, Unit] {
              ref
                .values
                .discrete
                .map(player -> _)
                .through(publishSink)
                .compile.drain
            }
          }
          .widen[Ref[G, T]]
    } { ref => ref.get }
  }
}
