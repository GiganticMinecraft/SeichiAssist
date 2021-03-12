package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence}

import java.util.UUID

object BuildAmountDataRepositoryDefinitions {

  import scala.util.chaining._

  def initialization[
    F[_] : Sync
  ](persistence: BuildAmountDataPersistence[F]): SinglePhasedRepositoryInitialization[F, Ref[F, BuildAmountData]] =
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence)(Applicative[F].pure(BuildAmountData.initial))
      .pipe(SinglePhasedRepositoryInitialization.forRefCell[F, BuildAmountData])

  def finalization[
    F[_] : Sync
  ](persistence: BuildAmountDataPersistence[F]): RepositoryFinalization[F, UUID, Ref[F, BuildAmountData]] =
    RefDictBackedRepositoryFinalization
      .using(persistence)(identity[UUID])
      .pipe(RepositoryFinalization.liftToRefFinalization[F, UUID, BuildAmountData])

}
