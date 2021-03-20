package com.github.unchama.datarepository.template

import cats.Monad
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryInitialization {

  import cats.implicits._

  def usingUuidRefDict[F[_] : Monad, R](refDict: RefDict[F, UUID, R])
                                       (getDefaultValue: F[R]): SinglePhasedRepositoryInitialization[F, R] =
    (uuid, _) => refDict.read(uuid).flatMap {
      case Some(value) => Monad[F].pure(value)
      case None => getDefaultValue
    }.map(PrefetchResult.Success.apply)
}
