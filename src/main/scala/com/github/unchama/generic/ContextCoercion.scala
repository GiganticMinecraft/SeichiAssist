package com.github.unchama.generic

import cats.arrow.FunctionK
import cats.~>

/**
 * 文脈FからGへの(自明な)変換を与える型クラス。
 *
 * [[cats.arrow.FunctionK]] と同じだが、より自明な、
 * 例えば [[cats.effect.SyncIO]] から [[cats.effect.IO]] のような変換を与えるオブジェクトとして機能する。
 */
trait ContextCoercion[F[_], G[_]] extends (F ~> G)

object ContextCoercion {

  def apply[F[_], G[_], A](fa: F[A])(implicit coercion: ContextCoercion[F, G]): G[A] = coercion(fa)

  def fromFunctionK[F[_], G[_]](functionK: F ~> G): ContextCoercion[F, G] = {
    new ContextCoercion[F, G] {
      def apply[A](fa: F[A]): G[A] = functionK(fa)
    }
  }

  implicit def identityCoercion[F[_]]: ContextCoercion[F, F] = fromFunctionK(FunctionK.id)

}
