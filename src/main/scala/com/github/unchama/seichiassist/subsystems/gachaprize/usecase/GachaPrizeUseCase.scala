package com.github.unchama.seichiassist.subsystems.gachaprize.usecase

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}

class GachaPrizeUseCase[F[_]: Sync, ItemStack](
  implicit gachaPrizeListPersistence: GachaPrizeListPersistence[F, ItemStack]
) {

  import cats.implicits._

  def addGachaPrize(gachaPrizeById: GachaPrizeId => GachaPrize[ItemStack]): F[Unit] = for {
    gachaPrizeList <- gachaPrizeListPersistence.list
    gachaPrizeId = GachaPrizeId(
      if (gachaPrizeList.nonEmpty) gachaPrizeList.map(_.id.id).max + 1 else 1
    )
    gachaPrize = gachaPrizeById(gachaPrizeId)
    _ <- gachaPrizeListPersistence.addGachaPrize(gachaPrize)
  } yield ()

  def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    originalGachaPrizes <- gachaPrizeListPersistence.list
    _ <- gachaPrizeListPersistence.removeGachaPrize(gachaPrizeId)
  } yield originalGachaPrizes.exists(_.id == gachaPrizeId)

}
