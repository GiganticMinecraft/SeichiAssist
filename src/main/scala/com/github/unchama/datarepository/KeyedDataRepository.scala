package com.github.unchama.datarepository

/**
 * `K` => `V` の関数オブジェクトのうち、リポジトリとして考えることができるオブジェクトのtrait。
 *
 * 主に依存性注入での使用を想定している。
 */
trait KeyedDataRepository[K, V] extends (K => V) {

  override def apply(v1: K): V

}
