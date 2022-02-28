package com.github.unchama.datarepository.definitions

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition.Phased
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.AsymmetricSignallingRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import fs2.Pipe
import io.chrisdavenport.log4cats.ErrorLogger

object SignallingRepositoryDefinition {

  import FiberAdjoinedRepositoryDefinition.FiberAdjoined
  import cats.implicits._

  def withPublishSink[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[
    G,
    *[_]
  ]: ErrorLogger, Player: HasUuid, T](publishSink: Pipe[F, (Player, T), Unit])(
    definition: RepositoryDefinition.Phased[G, Player, T]
  ): Phased.TwoPhased[G, Player, Ref[G, T] FiberAdjoined F] = {
    FiberAdjoinedRepositoryDefinition
      .extending(definition.toTwoPhased)
      .flatXmapWithPlayer { player =>
        {
          case (initialValue, fiberPromise) =>
            AsymmetricSignallingRef[G, F, T](initialValue)
              .flatTap { ref =>
                EffectExtra.runAsyncAndForget[F, G, Unit] {
                  Concurrent[F].start[Nothing] {
                    ref.valuesAwait.use[F, Nothing] { stream =>
                      StreamExtra.compileToRestartingStream[F, Nothing](
                        "[SignallingRepositoryDefinition]"
                      ) {
                        stream.map(player -> _).through(publishSink)
                      }
                    }
                  } >>= fiberPromise.complete
                }
              }
              .widen[Ref[G, T]]
              .map(_ -> fiberPromise)
        }
      } { case (ref, fiberPromise) => ref.get.map(_ -> fiberPromise) }
  }

  def withPublishSinkHidden[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[
    G,
    *[_]
  ]: ErrorLogger, Player: HasUuid, T](publishSink: Pipe[F, (Player, T), Unit])(
    definition: RepositoryDefinition.Phased[G, Player, T]
  ): RepositoryDefinition[G, Player, Ref[G, T]] =
    withPublishSink(publishSink)(definition).map(_._1)
}
