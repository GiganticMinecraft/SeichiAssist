package com.github.unchama.util.collection

import cats.data.NonEmptyList
import cats.effect.Sync

import scala.util.Random

class RandomizedCollection[Element, F[_] : Sync](collection: NonEmptyList[Element]) {

  val pickOne: F[Element] = Sync[F].delay(collection.toList(Random.nextInt(collection.size)))

}
