package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

case class GiftBundle(map: Map[Gift, Int]) {
  def combinePair(gift: Gift, count: Int): GiftBundle = GiftBundle {
    map.updatedWith(gift) {
      case Some(value) => Some(value + count)
      case None => Some(count)
    }
  }

  def combine(bundle: GiftBundle): GiftBundle =
    bundle
      .map.toList
      .foldLeft(this)((bundle, pair) => bundle.combinePair(pair._1, pair._2))
}

object GiftBundle {

  val empty: GiftBundle = GiftBundle(Map.empty)

  def ofSinglePair(gift: Gift, count: Int): GiftBundle = empty.combinePair(gift, count)

}
