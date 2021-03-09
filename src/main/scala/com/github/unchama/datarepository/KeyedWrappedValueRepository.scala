package com.github.unchama.datarepository

import cats.effect.Sync

import scala.collection.concurrent.TrieMap

/**
 * [[K]] をキーとした、[[V]] の可変参照を終域に持つ [[KeyedDataRepository]]。
 *
 * @tparam F   可変参照を作成するコンテキスト
 * @tparam Ref 可変参照の型コンストラクタ
 */
abstract class KeyedWrappedValueRepository[K, F[_] : Sync, Ref[_], V] extends KeyedDataRepository[K, Ref[V]] {
  protected val map: TrieMap[K, Ref[V]]
  protected val initializeRef: (K, V) => F[Ref[V]]

  protected final def addPair(k: K, v: V): F[Unit] = {
    Sync[F].flatMap(initializeRef(k, v))(ref => Sync[F].delay(map.addOne(k, ref)))
  }

  override final def apply(v1: K): Ref[V] = map(v1)
}
