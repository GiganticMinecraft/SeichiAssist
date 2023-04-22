package com.github.unchama.datarepository.definitions

import cats.Monad
import cats.effect.MonadThrow
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.RefDict
import com.github.unchama.generic.effect.concurrent.Retry.retryUntilSucceeds

import java.util.UUID

object RefDictBackedRepositoryDefinition {

  import cats.implicits._

  def usingUuidRefDictWithEffectfulDefault[F[_]: MonadThrow, Player, R](
    refDict: RefDict[F, UUID, R]
  )(getDefaultValue: F[R]): RepositoryDefinition.Phased.SinglePhased[F, Player, R] = {
    val initialization: SinglePhasedRepositoryInitialization[F, R] =
      (uuid, _) =>
        refDict
          .read(uuid)
          .flatMap {
            case Some(value) => Monad[F].pure(value)
            case None        => getDefaultValue
          }
          .map(PrefetchResult.Success.apply)

    val finalization: RepositoryFinalization[F, UUID, R] =
      RepositoryFinalization.withoutAnyFinalization((uuid, r) => retryUntilSucceeds(refDict.write(uuid, r)))

    RepositoryDefinition.Phased.SinglePhased.withoutTappingAction(initialization, finalization)
  }

  def usingUuidRefDictWithoutDefault[F[_]: MonadThrow, Player, R](
    refDict: RefDict[F, UUID, R]
  ): RepositoryDefinition.Phased.SinglePhased[F, Player, Option[R]] = {
    val initialization: SinglePhasedRepositoryInitialization[F, Option[R]] =
      (uuid, _) => refDict.read(uuid).map(PrefetchResult.Success.apply)

    val finalization: RepositoryFinalization[F, UUID, Option[R]] =
      RepositoryFinalization.withoutAnyFinalization((uuid, optR) =>
        optR.fold(Monad[F].pure(()))(r => retryUntilSucceeds(refDict.write(uuid, r)))
      )

    RepositoryDefinition.Phased.SinglePhased.withoutTappingAction(initialization, finalization)
  }

  def usingUuidRefDict[F[_]: MonadThrow, Player, R](refDict: RefDict[F, UUID, R])(
    defaultValue: R
  ): RepositoryDefinition.Phased.SinglePhased[F, Player, R] =
    usingUuidRefDictWithEffectfulDefault(refDict)(Monad[F].pure(defaultValue))

}
