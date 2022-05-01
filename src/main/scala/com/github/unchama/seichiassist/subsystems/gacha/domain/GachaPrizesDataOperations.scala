package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

final class GachaPrizesDataOperations[F[_]: Sync] {

  import cats.implicits._

  private val gachaPrizes: Ref[F, Vector[GachaPrize]] =
    Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)

  def getGachaPrizes: F[Vector[GachaPrize]] = gachaPrizes.get

  def loadGachaPrizes(gachaPersistence: GachaPersistence[F]): F[Unit] = for {
    prizes <- gachaPersistence.list
  } yield gachaPrizes.set(prizes)

  def addGachaPrize(gachaPrize: GachaPrize): F[Unit] = for {
    prizes <- gachaPrizes.get
  } yield {
    gachaPrizes.set(prizes ++ Vector(gachaPrize))
  }

  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    prizes <- gachaPrizes.get
  } yield {
    val targetPrize = prizes.filter(_.id == gachaPrizeId)
    if (targetPrize.nonEmpty) {
      gachaPrizes.set(prizes.diff(targetPrize))
      true
    } else false
  }

}
