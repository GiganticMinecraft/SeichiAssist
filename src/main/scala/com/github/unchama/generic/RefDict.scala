package com.github.unchama.generic

import cats.{Contravariant, ~>}

/**
 * [[Key]] をキーとした参照セルの辞書型データ構造の抽象。 読み書きは [[F]] のコンテキストで行われる作用として記述される。
 *
 * (特にDBや別マシンにあるKVストアなどでは)アクセスが並列に行われる可能性があることから、 このI/Fは一切の等式を保証しない。
 */
trait RefDict[F[_], Key, Value] {
  self =>

  def read(key: Key): F[Option[Value]]

  def write(key: Key, value: Value): F[Unit]

  final def mapK[G[_]](fK: F ~> G): RefDict[G, Key, Value] =
    new RefDict[G, Key, Value] {
      override def read(key: Key): G[Option[Value]] = fK(self.read(key))

      override def write(key: Key, value: Value): G[Unit] = fK(self.write(key, value))
    }

  final def coerceContextTo[G[_]](implicit ev: ContextCoercion[F, G]): RefDict[G, Key, Value] =
    mapK(ev)
}

object RefDict {

  implicit def contravariantFunctor[F[_], Value]: Contravariant[RefDict[F, *, Value]] =
    new Contravariant[RefDict[F, *, Value]] {
      override def contramap[A, B](fa: RefDict[F, A, Value])(f: B => A): RefDict[F, B, Value] =
        new RefDict[F, B, Value] {
          override def read(key: B): F[Option[Value]] = fa.read(f(key))

          override def write(key: B, value: Value): F[Unit] = fa.write(f(key), value)
        }
    }

}
