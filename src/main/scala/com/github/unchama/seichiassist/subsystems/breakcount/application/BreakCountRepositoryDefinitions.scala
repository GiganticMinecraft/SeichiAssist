package com.github.unchama.seichiassist.subsystems.breakcount.application

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}

import java.util.UUID

object BreakCountRepositoryDefinitions {

  def initialization[F[_] : Sync](persistence: SeichiAmountDataPersistence[F]): SinglePhasedRepositoryInitialization[F, Ref[F, SeichiAmountData]] =
    SinglePhasedRepositoryInitialization.forRefCell(
      RefDictBackedRepositoryInitialization
        .usingUuidRefDict(persistence)(Applicative[F].pure(SeichiAmountData.initial))
    )

  def finalization[F[_] : Sync](persistence: SeichiAmountDataPersistence[F]): RepositoryFinalization[F, UUID, Ref[F, SeichiAmountData]] =
    RepositoryFinalization.liftToRefFinalization[F, UUID, SeichiAmountData](
      RefDictBackedRepositoryFinalization
        .using(persistence)(identity)
    )

}
