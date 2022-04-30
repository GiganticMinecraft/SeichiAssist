package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

//TODO このクラスがどう利用されるのかをはっきりさせて責務のあるクラスに昇華させたい
final class GachaPrizesData[F[_]: Sync] {

  private val gachaPrizes: Ref[F, Vector[GachaPrize]] =
    Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)

  def getGachaPrizes: F[Vector[GachaPrize]] = gachaPrizes.get

}
