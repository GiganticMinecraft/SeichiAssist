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

  /**
   * 可変参照を値に持つ [[KeyedDataRepository]] と、
   * それに対する更新を通知する [[Stream]] の組を作成する。
   *
   * 返される [[Stream]] は、[[KeyedDataRepository]] が保持するレコードの書き換えのみ出力し、
   * 追加や削除を出力しない。
   */
  def apply[F[_]]: ApplyPartiallyApplied[F] = new ApplyPartiallyApplied[F]()

  /**
   * Uses the [[https://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  //noinspection ScalaUnusedSymbol
  final class ApplyPartiallyApplied[F[_]] private[SignallingRepository](private val dummy: Boolean = true) extends AnyVal {
    def apply[
      G[_] : Sync, Key, Value, Repository[ref[_], x] <: KeyedDataRepository[Key, ref[x]]
    ](factory: RefRepositoryFactory[G, Key, Value, Repository],
      subscriptionMaxQueue: Int = 10)(implicit F: ConcurrentEffect[F], GtoF: ContextCoercion[G, F])
    : G[(Repository[Ref[G, *], Value], Stream[F, (Key, Value)])] =
      for {
        topic <- Topic.in[G, F, Option[(Key, Value)]](None)
        repository <- factory.instantiate[Ref[G, *]] { case (key, value) =>
          for {
            ref <- AsymmetricSignallingRef[G, F, Value](value)
            _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
              val keyedUpdateStream = ref.subscribeToUpdates.map(Some(key, _))
              topic.publish(keyedUpdateStream).compile.drain
            }
          } yield ref: Ref[G, Value]
        }
      } yield {
        val filteredTopic = topic.subscribe(subscriptionMaxQueue).mapFilter(x => x)

        (repository, filteredTopic)
      }
  }

}
