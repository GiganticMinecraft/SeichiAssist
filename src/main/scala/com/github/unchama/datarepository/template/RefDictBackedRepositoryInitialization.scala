package com.github.unchama.datarepository.template

import cats.{Contravariant, Monad}
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryInitialization {

  import cats.implicits._

  def using[F[_] : Monad, R](refDict: RefDict[F, (UUID, String), R])
                            (getDefaultValue: F[R]): SinglePhasedRepositoryInitialization[F, R] =
    (uuid, name) => refDict.read((uuid, name)).flatMap {
      case Some(value) => Monad[F].pure(value)
      case None => getDefaultValue.map(PrefetchResult.Success.apply)
    }

  def usingUuidRefDict[F[_] : Monad, R](refDict: RefDict[F, UUID, R])
                                       (getDefaultValue: F[R]): SinglePhasedRepositoryInitialization[F, R] =
    using(Contravariant[RefDict[F, *, R]].contramap(refDict)(_._1))(getDefaultValue)

}
