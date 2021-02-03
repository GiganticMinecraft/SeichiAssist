package com.github.unchama.seichiassist.util

import com.github.unchama.seichiassist.util.typeclass.OrderedCollection

import scala.util.Random

object OrderedCollectionOps {
  implicit class SampleOps[E](col: OrderedCollection[E]) {
    def sample: E = col(new Random().nextInt(col.size))
  }
}
