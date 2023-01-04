package com.github.unchama.datarepository

import cats.Functor

/**
 * `K` => `V` の関数オブジェクトのうち、リポジトリとして考えることができるオブジェクトのtrait。
 *
 * 主に依存性注入での使用を想定している。
 */
trait KeyedDataRepository[K, V] extends PartialFunction[K, V]

object KeyedDataRepository {

  def unlift[K, V](f: K => Option[V]): KeyedDataRepository[K, V] =
    new KeyedDataRepository[K, V] {
      override def apply(x: K): V = f(x).get

      override def isDefinedAt(x: K): Boolean = f(x).isDefined
    }

  implicit def functor[K]: Functor[KeyedDataRepository[K, *]] =
    new Functor[KeyedDataRepository[K, *]] {
      override def map[A, B](
        fa: KeyedDataRepository[K, A]
      )(f: A => B): KeyedDataRepository[K, B] = {
        new KeyedDataRepository[K, B] {
          override def isDefinedAt(x: K): Boolean = fa.isDefinedAt(x)

          override def apply(v1: K): B = f(fa(v1))
        }
      }
    }

}
