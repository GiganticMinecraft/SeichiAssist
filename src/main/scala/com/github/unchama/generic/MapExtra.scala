package com.github.unchama.generic

import cats.kernel.CommutativeSemigroup

object MapExtra {

  import cats.implicits._

  /**
   * `grouping` によりキーを分類し、 [[CommutativeSemigroup]] にて分類された値を合成する。
   *
   * 任意の `g: K => T`、 `k: K`、 `map: Map[K, V]` について、 `collapseKeys(g)(map).get(k) ==
   * map.filterKeys(k1 => g(k) == g(k1)).combineAll` を満たす。
   */
  def collapseKeysThrough[T, K, V: CommutativeSemigroup](
    grouping: K => T
  )(map: Map[K, V]): Map[K, V] =
    map
      .groupBy { case (k, _) => grouping(k) }
      .toList
      .mapFilter {
        case (_, groupedMap) =>
          groupedMap.toList match {
            case ::((k, v), remaining) =>
              Some {
                val values: List[V] = remaining.map(_._2)

                k -> values.foldLeft(v)(CommutativeSemigroup[V].combine)
              }
            case Nil => None // 到達不能であるはず
          }
      }
      .toMap

  /**
   * @param map
   *   元となるMap
   * @param base
   *   キーの集合
   * @param default
   *   埋める値
   * @tparam K
   *   キー
   * @tparam V
   *   値
   * @return
   */
  def fillOnBaseSet[K, V](map: Map[K, V], base: Set[K], default: V): Map[K, V] = {
    val keys = map.keys.toSet
    require(keys.subsetOf(base))
    map ++ (base -- keys).map(a => a -> default)
  }

  /**
   * `cond`がtrueであれば`value`を、falseであれば空のMapを返す
   */
  def when[K, V](cond: Boolean)(value: => Map[K, V]): Map[K, V] =
    if (cond) value else Map.empty
}
