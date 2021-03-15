package com.github.unchama.seichiassist.subsystems.gachapoint.application.process

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.BatchUsageSemaphore

object ConvertPointToTickets {

  def stream[
    F[_], G[_], Player
  ](repository: KeyedDataRepository[Player, BatchUsageSemaphore[F, G]]): fs2.Stream[F, Unit] =
    ???

}
