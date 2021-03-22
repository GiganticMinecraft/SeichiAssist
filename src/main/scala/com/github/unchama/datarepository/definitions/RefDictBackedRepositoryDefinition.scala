package com.github.unchama.datarepository.definitions

import cats.Monad
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryDefinition {

  import cats.implicits._

  def usingUuidRefDictWithEffectfulDefault[
    F[_] : Monad, Player, R
  ](refDict: RefDict[F, UUID, R])(getDefaultValue: F[R]): RepositoryDefinition.SinglePhased[F, Player, R] = {
    val initialization: SinglePhasedRepositoryInitialization[F, R] =
      (uuid, _) => refDict
        .read(uuid)
        .flatMap {
          case Some(value) => Monad[F].pure(value)
          case None => getDefaultValue
        }
        .map(PrefetchResult.Success.apply)

    val finalization: RepositoryFinalization[F, UUID, R] =
      RepositoryFinalization.withoutAnyFinalization((uuid, r) => refDict.write(uuid, r))

    RepositoryDefinition.SinglePhased.withoutTappingAction(initialization, finalization)
  }


  def usingUuidRefDict[F[_] : Monad, Player, R](refDict: RefDict[F, UUID, R])
                                               (defaultValue: R): RepositoryDefinition.SinglePhased[F, Player, R] =
    usingUuidRefDictWithEffectfulDefault(refDict)(Monad[F].pure(defaultValue))

}
