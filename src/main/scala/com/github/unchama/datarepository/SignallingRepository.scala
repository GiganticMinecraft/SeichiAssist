package com.github.unchama.datarepository

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.AsymmetricSignallingRef
import fs2.Stream
import fs2.concurrent.Topic

object SignallingRepository {

  import cats.implicits._

  trait RepositoryFactory[F[_], Key, Value] {

    /**
     * 可変参照を作成する関数 `(Key, Value) => F[Ref[F, Value]]` から [[KeyedDataRepository]] を作成する。
     *
     * 作成された [[KeyedDataRepository]] の可変参照セルが必ず `refCreator` によって作成されたということを保証するため、
     * この関数は [[Ref]] について多相的になっている。
     */
    def instantiate[
      Ref[_[_], _]
    ](refCreator: (Key, Value) => F[Ref[F, Value]]): F[KeyedDataRepository[Key, Ref[F, Value]]]

  }

  /**
   * [[Value]] の可変参照を値に持つ [[KeyedDataRepository]] と、
   * それに対する更新を通知する [[Stream]] の組を作成する。
   */
  def apply[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Key, Value
  ](factory: RepositoryFactory[G, Key, Value],
    subscriptionMaxQueue: Int = 10): G[(KeyedDataRepository[Key, Ref[G, Value]], Stream[F, (Key, Value)])] =
    for {
      topic <- Topic.in[G, F, Option[(Key, Value)]](None)
      repository <- factory.instantiate[Ref] { case (key, value) =>
        for {
          ref <- AsymmetricSignallingRef[G, F, Value](value)
          _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
            val updateStream = ref.signal.discrete.map(Some(key, _): Option[(Key, Value)])
            topic.publish(updateStream).compile.drain
          }
        } yield ref: Ref[G, Value]
      }
    } yield {
      val filteredTopic = topic.subscribe(subscriptionMaxQueue).mapFilter(x => x)

      (repository, filteredTopic)
    }

}
