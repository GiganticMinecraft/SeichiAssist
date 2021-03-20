package com.github.unchama.datarepository.definitions

import cats.Monad
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryDefinition {

  import cats.implicits._

  def usingUuidRefDict[F[_] : Monad, Player, R](refDict: RefDict[F, UUID, R])
                                               (defaultValue: R): RepositoryDefinition[F, Player, R] = {
    val initialization: SinglePhasedRepositoryInitialization[F, R] =
      (uuid, _) => refDict
        .read(uuid)
        .map(_.getOrElse(defaultValue))
        .map(PrefetchResult.Success.apply)

    val finalization: RepositoryFinalization[F, UUID, R] = new RepositoryFinalization[F, UUID, R] {
      override val persistPair: (UUID, R) => F[Unit] = (uuid, r) => refDict.write(uuid, r)
      override val finalizeBeforeUnload: (UUID, R) => F[Unit] = (_, _) => Monad[F].unit
    }

    RepositoryDefinition.SinglePhased.withoutTappingAction(initialization, finalization)
  }

}
