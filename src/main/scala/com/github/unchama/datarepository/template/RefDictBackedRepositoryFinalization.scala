package com.github.unchama.datarepository.template

import cats.Applicative
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryFinalization {
  def usingUuidRefDict[F[_] : Applicative, R](refDict: RefDict[F, UUID, R]): RepositoryFinalization[F, UUID, R] =
    new RepositoryFinalization[F, UUID, R] {
      override val persistPair: (UUID, R) => F[Unit] = (uuid, data) => refDict.write(uuid, data)
      override val finalizeBeforeUnload: (UUID, R) => F[Unit] = (_, _) => Applicative[F].unit
    }
}
