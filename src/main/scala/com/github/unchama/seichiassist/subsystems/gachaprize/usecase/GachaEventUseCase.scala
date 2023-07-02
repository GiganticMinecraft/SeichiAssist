package com.github.unchama.seichiassist.subsystems.gachaprize.usecase

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventPersistence
}

class GachaEventUseCase[F[_]: Sync, ItemStack](
  implicit gachaEventPersistence: GachaEventPersistence[F],
  gachaPrizePersistence: GachaPrizeListPersistence[F, ItemStack]
) {

  import cats.implicits._

  def createGachaEvent(gachaEvent: GachaEvent): F[Unit] = for {
    _ <- gachaEventPersistence.createGachaEvent(gachaEvent)
    _ <- gachaPrizePersistence.duplicateDefaultGachaPrizes(gachaEvent)
  } yield ()

}
