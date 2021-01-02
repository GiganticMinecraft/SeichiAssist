package com.github.unchama.datarepository

import cats.Functor

/**
 * `K` => `V` の関数オブジェクトのうち、リポジトリとして考えることができるオブジェクトのtrait。
 *
 * 主に依存性注入での使用を想定している。
 */
trait KeyedDataRepository[K, V] extends (K => V) {

  override def apply(v1: K): V

}

object KeyedDataRepository {

  implicit def functor[K]: Functor[KeyedDataRepository[K, *]] =
    new Functor[KeyedDataRepository[K, *]] {
      override def map[A, B](fa: KeyedDataRepository[K, A])(f: A => B): KeyedDataRepository[K, B] =
        v1 => f(fa(v1))
    }

}
