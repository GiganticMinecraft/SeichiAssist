package com.github.unchama.generic

import cats.arrow.FunctionK
import cats.effect.{IO, SyncIO}
import cats.~>

/**
 * 文脈FからGへの(自明な)変換を与える型クラス。
 *
 * [[cats.arrow.FunctionK]] と同じだが、より自明な、
 * 例えば [[cats.effect.SyncIO]] から [[cats.effect.IO]] のような変換を与えるオブジェクトとして機能する。
 */
trait ContextCoercion[F[_], G[_]] extends (F ~> G)

final class CoercibleComputation[F[_], A](val fa: F[A]) extends AnyVal {
  def coerceTo[G[_]](implicit coercion: ContextCoercion[F, G]): G[A] = coercion(fa)
}

private[generic] abstract class ContextCoercionOps {

  import scala.language.implicitConversions

  implicit def coercibleComputation[F[_], A](fa: F[A]): CoercibleComputation[F, A] = new CoercibleComputation(fa)
}

object ContextCoercion extends ContextCoercionOps {

  def apply[F[_], G[_], A](fa: F[A])(implicit coercion: ContextCoercion[F, G]): G[A] = coercion(fa)

  def fromFunctionK[F[_], G[_]](functionK: F ~> G): ContextCoercion[F, G] = {
    new ContextCoercion[F, G] {
      def apply[A](fa: F[A]): G[A] = functionK(fa)
    }
  }

  implicit def identityCoercion[F[_]]: ContextCoercion[F, F] = fromFunctionK(FunctionK.id)

  implicit val catsEffectSyncIOToIOCoercion: ContextCoercion[SyncIO, IO] = fromFunctionK {
    λ[SyncIO ~> IO](_.toIO)
  }

}
