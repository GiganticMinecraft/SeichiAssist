package com.github.unchama.seichiassist.subsystems.gachaprize.usecase

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}

import java.time.LocalDate

class GachaPrizeUseCase[F[_]: Sync, ItemStack](
  implicit gachaPrizeListPersistence: GachaPrizeListPersistence[F, ItemStack],
  gachaEventPersistence: GachaEventPersistence[F]
) {

  import cats.implicits._

  def createGachaEvent(gachaEvent: GachaEvent): F[Unit] = for {
    _ <- gachaEventPersistence.createGachaEvent(gachaEvent)
    _ <- gachaPrizeListPersistence.duplicateDefaultGachaPrizes(gachaEvent)
  } yield ()

  private def isHolding(gachaEvent: GachaEvent): F[Boolean] = for {
    now <- Sync[F].delay(LocalDate.now())
    isAfterStartDateOrFirstDay = now.equals(gachaEvent.startDate) || now.isAfter(
      gachaEvent.startDate
    )
    isBeforeEndDateOrEndDay = now.equals(gachaEvent.endDate) || now.isBefore(gachaEvent.endDate)
  } yield isAfterStartDateOrFirstDay && isBeforeEndDateOrEndDay

  private def holdingGachaEvent: F[Option[GachaEvent]] = for {
    gachaEvents <- gachaEventPersistence.gachaEvents
    holdingEvent <- gachaEvents.findM(isHolding)
  } yield holdingEvent

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

  def listOfNow: F[Vector[GachaPrize[ItemStack]]] = for {
    gachaPrizes <- gachaPrizeListPersistence.list
    holingGachaEvent <- holdingGachaEvent
  } yield gachaPrizes.filter(_.gachaEvent == holingGachaEvent)

}
