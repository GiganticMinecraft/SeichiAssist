package com.github.unchama.seichiassist.subsystems.expbottlestack.domain

import cats.effect.Sync

import scala.util.Random

case class BottleCount(amount: Int) extends AnyVal {

  // 経験値瓶一つに付きもたらされる経験値量は3..11。ソースはGamepedia
  def randomlyGenerateExpAmount[F[_] : Sync]: F[Int] = Sync[F].delay {
    (1 to amount)
      .map(_ => Random.nextInt(9 /* Exclusive */) + 3)
      .sum
  }
}
