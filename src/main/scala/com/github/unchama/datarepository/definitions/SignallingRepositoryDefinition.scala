package com.github.unchama.datarepository.definitions

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.AsymmetricSignallingRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import fs2.Pipe
import io.chrisdavenport.log4cats.ErrorLogger

object SignallingRepositoryDefinition {

  import cats.implicits._

  def forPlayerTopic[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]] : ErrorLogger,
    Player: HasUuid, T
  ](publishSink: Pipe[F, (Player, T), Unit])
   (definition: RepositoryDefinition[G, Player, T]): RepositoryDefinition[G, Player, Ref[G, T]] = {
    definition.toTwoPhased.flatXmapWithPlayer { player =>
      initialValue =>
        AsymmetricSignallingRef[G, F, T](initialValue)
          .flatTap { ref =>
            EffectExtra.runAsyncAndForget[F, G, Unit] {
              ref
                .valuesAwait
                .use { stream =>
                  // FIXME: This *never* returns. It is likely that this is not garbage collected.
                  //  We might need to find a way to
                  //   - restart the stream when the downstream stream fails
                  //   - unsubscribe when the player exits
                  //  We should be able to achieve this by returning a CancelToken or something on this flatXmapWithPlayer
                  StreamExtra.compileToRestartingStream {
                    stream.map(player -> _).through(publishSink)
                  }
                }
            }
          }
          .widen[Ref[G, T]]
    } { ref => ref.get }
  }
}
