package com.github.unchama.datarepository.template

import cats.Applicative
import com.github.unchama.generic.RefDict

import java.util.UUID

object RefDictBackedRepositoryFinalization {

  def using[F[_] : Applicative, Player, Key, R](refDict: RefDict[F, Key, R])
                                               (keyExtractor: Player => Key): RepositoryFinalization[F, Player, R] =
    new RepositoryFinalization[F, Player, R] {
      override val persistPair: (Player, R) => F[Unit] = (player, data) => refDict.write(keyExtractor(player), data)
      override val finalizeBeforeUnload: (Player, R) => F[Unit] = (_, _) => Applicative[F].unit
    }

  def usingUuidRefDict[F[_] : Applicative, R](refDict: RefDict[F, UUID, R]): RepositoryFinalization[F, UUID, R] =
    using[F, UUID, UUID, R](refDict)(identity)
}
