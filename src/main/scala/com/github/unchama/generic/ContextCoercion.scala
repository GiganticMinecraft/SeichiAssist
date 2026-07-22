package com.github.unchama.generic

import cats.arrow.FunctionK
import cats.effect.std.Dispatcher
import cats.effect.{IO, Sync, SyncIO}
import cats.~>

/**
 * 外部の同期APIとの境界で、`F`を同期的に実行する能力。
 *
 * Cats Effect 3では`SyncEffect`が廃止されたため、同期実行が必要な境界だけでこの能力を明示的に要求する。
 */
trait UnsafeSyncRunner[F[_]] {
  def unsafeRunSync[A](fa: F[A]): A
}

object UnsafeSyncRunner {
  def apply[F[_]](implicit runner: UnsafeSyncRunner[F]): UnsafeSyncRunner[F] = runner

  implicit val syncIORunner: UnsafeSyncRunner[SyncIO] = new UnsafeSyncRunner[SyncIO] {
    override def unsafeRunSync[A](fa: SyncIO[A]): A = fa.unsafeRunSync()
  }

  implicit def dispatcherBackedRunner[F[_]](
    implicit dispatcher: Dispatcher[F]
  ): UnsafeSyncRunner[F] = new UnsafeSyncRunner[F] {
    override def unsafeRunSync[A](fa: F[A]): A = dispatcher.unsafeRunSync(fa)
  }
}

/**
 * 文脈FからGへの(自明な)変換を与える型クラス。
 *
 * [[cats.arrow.FunctionK]] と同じだが、より自明な、 例えば [[cats.effect.SyncIO]] から [[cats.effect.IO]]
 * のような変換を与えるオブジェクトとして機能する。
 */
trait ContextCoercion[F[_], G[_]] extends (F ~> G)

final class CoercibleComputation[F[_], A](val fa: F[A]) extends AnyVal {
  def coerceTo[G[_]](implicit coercion: ContextCoercion[F, G]): G[A] = coercion(fa)
}

private[generic] abstract class ContextCoercionOps {

  import scala.language.implicitConversions

  implicit def coercibleComputation[F[_], A](fa: F[A]): CoercibleComputation[F, A] =
    new CoercibleComputation(fa)
}

object ContextCoercion extends ContextCoercionOps {

  def apply[F[_], G[_], A](fa: F[A])(implicit coercion: ContextCoercion[F, G]): G[A] = coercion(
    fa
  )

  def asFunctionK[F[_], G[_]](implicit ev: ContextCoercion[F, G]): F ~> G = ev

  def fromFunctionK[F[_], G[_]](functionK: F ~> G): ContextCoercion[F, G] = {
    new ContextCoercion[F, G] {
      def apply[A](fa: F[A]): G[A] = functionK(fa)
    }
  }

  implicit def identityCoercion[F[_]]: ContextCoercion[F, F] = fromFunctionK(FunctionK.id)

  implicit def syncEffectToSync[F[_]: UnsafeSyncRunner, G[_]: Sync]: ContextCoercion[F, G] = {
    fromFunctionK(new FunctionK[F, G] {
      def apply[A](fa: F[A]): G[A] =
        Sync[G].delay(UnsafeSyncRunner[F].unsafeRunSync(fa))
    })
  }

  implicit val catsEffectSyncIOToIOCoercion: ContextCoercion[SyncIO, IO] = fromFunctionK {
    new FunctionK[SyncIO, IO] {
      def apply[A](fa: SyncIO[A]): IO[A] = fa.to[IO]
    }
  }

}
