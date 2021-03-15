package com.github.unchama.datarepository.template

import cats.Applicative
import com.github.unchama.generic.RefDict
import com.github.unchama.minecraft.algebra.HasUuid

import java.util.UUID

object RefDictBackedRepositoryFinalization {

  def using[F[_] : Applicative, Player, Key, R](refDict: RefDict[F, Key, R])
                                               (keyExtractor: Player => Key): RepositoryFinalization[F, Player, R] =
    new RepositoryFinalization[F, Player, R] {
      override val persistPair: (Player, R) => F[Unit] = (player, data) => refDict.write(keyExtractor(player), data)
      override val finalizeBeforeUnload: (Player, R) => F[Unit] = (_, _) => Applicative[F].unit
    }

  def usingUuidRefDict[
    F[_]: Applicative, Player: HasUuid, R
  ](refDict: RefDict[F, UUID, R]): RepositoryFinalization[F, Player, R] =
    using[F, Player, UUID, R](refDict)(HasUuid[Player].asFunction)
}
