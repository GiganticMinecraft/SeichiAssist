package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

final class GachaPrizesDataOperations[F[_]: Sync: NonServerThreadContextShift] {

  import cats.implicits._

  private val gachaPrizes: Ref[F, Vector[GachaPrize]] =
    Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)

  def loadGachaPrizes(gachaPersistence: GachaPersistence[F]): F[Unit] = for {
    prizes <- gachaPersistence.list
    _ <- gachaPrizes.set(prizes)
  } yield ()

  def gachaPrizeExists(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    gachaPrizes <- gachaPrizes.get
  } yield gachaPrizes.exists(_.id == gachaPrizeId)

  def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize): F[Unit] = for {
    prizes <- gachaPrizes.get
    newList = prizes ++ Vector(gachaPrize(GachaPrizeId(prizes.size + 1)))
    _ <- gachaPrizes.set(newList)
  } yield ()

  def getGachaPrize(gachaPrizeId: GachaPrizeId): F[Option[GachaPrize]] = for {
    prizes <- gachaPrizes.get
  } yield prizes.find(_.id == gachaPrizeId)

  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    prizes <- gachaPrizes.get
  } yield {
    val targetPrize = prizes.filter(_.id == gachaPrizeId)
    if (targetPrize.nonEmpty) {
      gachaPrizes.set(prizes.diff(targetPrize))
      true
    } else false
  }

  def getGachaPrizesList: F[Vector[GachaPrize]] = gachaPrizes.get

}
