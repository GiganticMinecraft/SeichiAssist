package com.github.unchama.datarepository.template

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.AsymmetricSignallingRef

object SignallingRepositoryInitialization {

  import cats.implicits._

  private def buildSignallingRefAgainst[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Key,
    Value
  ](topic: Topic[F, Option[(Key, Value)]])
   (key: Key, initialValue: Value): G[Ref[G, Value]] =
    for {
      ref <- AsymmetricSignallingRef[G, F, Value](initialValue)
      _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
        val keyedUpdateStream = ref.values.discrete.map(Some(key, _))
        topic.publish(keyedUpdateStream).compile.drain
      }
    } yield ref: Ref[G, Value]

  def againstPlayerTopic[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player,
    Value
  ](topic: Topic[F, Option[(Player, Value)]])
   (initialization: TwoPhasedRepositoryInitialization[G, Player, Value]): TwoPhasedRepositoryInitialization[G, Player, Ref[G, Value]] =
    initialization.extendPreparation { player =>
      initialValue =>
        buildSignallingRefAgainst(topic)(player, initialValue)
    }
}
