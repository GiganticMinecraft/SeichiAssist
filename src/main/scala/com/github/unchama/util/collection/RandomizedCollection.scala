package com.github.unchama.util.collection

import cats.data.NonEmptyList
import cats.effect.Sync

import scala.util.Random

class RandomizedCollection[Element](collection: NonEmptyList[Element]) {

  def pickOne[F[_] : Sync]: F[Element] = Sync[F].delay {
    collection.toList(Random.nextInt(collection.size))
  }

}
